/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.devops.ci.pipeline;

import com.wl4g.devops.ci.config.CiCdProperties;
import com.wl4g.devops.ci.core.PipelineContext;
import com.wl4g.devops.ci.core.PipelineJobExecutor;
import com.wl4g.devops.ci.service.DependencyService;
import com.wl4g.devops.ci.service.TaskHistoryService;
import com.wl4g.devops.common.bean.share.AppInstance;
import com.wl4g.devops.common.utils.cli.SSH2Utils.CommandResult;
import com.wl4g.devops.common.utils.codec.AES;
import com.wl4g.devops.dao.ci.ProjectDao;
import com.wl4g.devops.dao.ci.TaskHisBuildCommandDao;
import com.wl4g.devops.dao.ci.TaskSignDao;
import com.wl4g.devops.support.concurrent.locks.JedisLockManager;
import com.wl4g.devops.support.cli.ProcessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static com.wl4g.devops.ci.utils.LogHolder.logDefault;
import static com.wl4g.devops.common.constants.CiDevOpsConstants.PROJECT_PATH;
import static com.wl4g.devops.common.utils.cli.SSH2Utils.executeWithCommand;
import static com.wl4g.devops.common.utils.lang.Collections2.safeList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.util.List;

/**
 * Abstract based deploy provider.
 *
 * @author Wangl.sir <983708408@qq.com>
 * @author vjay
 * @date 2019-05-05 17:17:00
 */
public abstract class AbstractPipelineProvider implements PipelineProvider {
	final protected Logger log = LoggerFactory.getLogger(getClass());

	/** Pipeline context wrapper. */
	final protected PipelineContext context;

	@Autowired
	protected CiCdProperties config;
	@Autowired
	protected PipelineJobExecutor jobExecutor;
	@Autowired
	protected BeanFactory beanFactory;
	@Autowired
	protected JedisLockManager lockManager;
	@Autowired
	protected ProcessManager processManager;

	@Autowired
	protected DependencyService dependencyService;
	@Autowired
	protected TaskHistoryService taskHistoryService;
	@Autowired
	protected TaskHisBuildCommandDao taskHisBuildCommandDao;
	@Autowired
	protected ProjectDao projectDao;
	@Autowired
	protected TaskSignDao taskSignDao;

	private String vcsSourceFileFingerprint;
	private String assetsFileFingerprint;

	public AbstractPipelineProvider(PipelineContext context) {
		notNull(context, "Pipeline context must not be null.");
		this.context = context;
	}

	/**
	 * Get pipeline context.
	 */
	public PipelineContext getContext() {
		return context;
	}

	// --- Fingerprints. ---

	@Override
	public String getVcsSourceFileFingerprint() {
		return vcsSourceFileFingerprint;
	}

	@Override
	public String getAssetsFileFingerprint() {
		return assetsFileFingerprint;
	}

	protected void setupVcsSourceFileFingerprint(String vcsSourceFileFingerprint) {
		hasText(vcsSourceFileFingerprint, "vcsSourceFileFingerprint must not be empty.");
		this.vcsSourceFileFingerprint = vcsSourceFileFingerprint;
	}

	protected void setupAssetsFileFingerprint(String assetsFileFingerprint) {
		hasText(assetsFileFingerprint, "assetsFileFingerprint must not be empty.");
		this.assetsFileFingerprint = assetsFileFingerprint;
	}

	// --- Functions. ---

	/**
	 * Execution remote commands
	 * 
	 * @param remoteHost
	 * @param user
	 * @param command
	 * @param sshkey
	 * @return
	 * @throws Exception
	 */
	@Override
	public void doRemoteCommand(String remoteHost, String user, String command, String sshkey) throws Exception {
		hasText(command, "Commands must not be empty.");

		// Remote timeout(Ms)
		long timeoutMs = config.getRemoteCommandTimeoutMs(getContext().getInstances().size());
		logDefault("Transfer remote execution for %s@%s, timeout(%s) => command(%s)", user, remoteHost, timeoutMs, command);
		// Execution command.
		CommandResult result = executeWithCommand(remoteHost, user, getUsableCipherSSHKey(sshkey), command, timeoutMs);

		logDefault("\n%s@%s -> [stdout]\n", user, remoteHost);
		if (!isBlank(result.getMessage())) {
			logDefault(result.getMessage());
		}
		logDefault("\n%s@%s -> [stderr]\n", user, remoteHost);
		if (!isBlank(result.getErrmsg())) {
			logDefault(result.getErrmsg());
		}

	}

	/**
	 * Deciphering usable cipher SSH2 key.
	 * 
	 * @param sshkey
	 * @return
	 * @throws Exception
	 */
	@Override
	public char[] getUsableCipherSSHKey(String sshkey) throws Exception {
		// Obtain text-plain privateKey(RSA)
		String cipherKey = config.getTranform().getCipherKey();
		char[] sshkeyPlain = new AES(cipherKey).decrypt(sshkey).toCharArray();
		if (log.isInfoEnabled()) {
			log.info("Transfer plain sshkey: {} => {}", cipherKey, "******");
		}

		logDefault("Transfer plain sshkey: %s => %s", cipherKey, "******");
		return sshkeyPlain;
	}

	/**
	 * Execution distribution transfer to remote instances for executable file.
	 */
	protected void doExecuteTransferToRemoteInstances() {
		// Creating transfer instances jobs.
		List<Runnable> jobs = safeList(getContext().getInstances()).stream().map(i -> newTransferJob(i)).collect(toList());

		// Submit jobs for complete.
		if (!isEmpty(jobs)) {
			if (log.isInfoEnabled()) {
				log.info("Transfer jobs starting...  for instances({}), {}", jobs.size(), getContext().getInstances());
			}
			jobExecutor.submitForComplete(jobs, config.getTranform().getTransferTimeoutMs());
		}

	}

	/**
	 * Resolve placeholder variables.
	 * 
	 * @param command
	 * @param projectPath
	 * @return
	 */
	protected String resolvePlaceholderVariables(String command, String projectPath) {
		command = command.replaceAll(PROJECT_PATH, projectPath);// projectPath
		return command;
	}

	/**
	 * Create pipeline transfer job.
	 * 
	 * @param instance
	 * @return
	 */
	protected abstract Runnable newTransferJob(AppInstance instance);

}