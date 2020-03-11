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
package com.wl4g.devops.coss.hdfs.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wl4g.devops.coss.CossEndpoint;
import com.wl4g.devops.coss.hdfs.HdfsCossEndpoint;

@Configuration
public class HdfsCossAutoConfiguration {
	final public static String KEY_PROPERTY_PREFIX = "spring.cloud.devops.coss.hdfs";

	@Bean
	@ConditionalOnProperty(name = KEY_PROPERTY_PREFIX + ".enable", matchIfMissing = false)
	@ConfigurationProperties(prefix = KEY_PROPERTY_PREFIX)
	public HdfsCossProperties hdfsCossProperties() {
		return new HdfsCossProperties();
	}

	@Bean
	@ConditionalOnBean(HdfsCossProperties.class)
	public CossEndpoint hdfsCossEndpoint(HdfsCossProperties config) {
		return new HdfsCossEndpoint(config);
	}

}
