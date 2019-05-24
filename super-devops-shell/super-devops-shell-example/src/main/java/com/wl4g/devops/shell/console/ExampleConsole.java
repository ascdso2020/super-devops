/*
 * Copyright 2015 the original author or authors.
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
package com.wl4g.devops.shell.console;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.wl4g.devops.shell.annotation.ShellComponent;
import com.wl4g.devops.shell.annotation.ShellMethod;
import com.wl4g.devops.shell.annotation.ShellOption;
import com.wl4g.devops.shell.bean.MixedArgument;
import com.wl4g.devops.shell.bean.SumArgument;
import com.wl4g.devops.shell.bean.SumResult;
import com.wl4g.devops.shell.processor.ShellContext;
import com.wl4g.devops.shell.service.ExampleService;

@ShellComponent
public class ExampleConsole {

	final public static String GROUP_NAME = "Example commands";

	final protected Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private ExampleService exampleService;

	/**
	 * $> sum -a 1 -b 123
	 */
	@ShellMethod(keys = "sum", group = GROUP_NAME, help = "This is an example method of summation. The parameter list is not the base type.")
	public SumResult sum(SumArgument arg) {
		return exampleService.add(arg);
	}

	/**
	 * $> sum2 -a 1 -b 123
	 */
	@ShellMethod(keys = "sum2", group = GROUP_NAME, help = "This is an example method of summation. The parameter list is the basic type.")
	public SumResult sum2(@ShellOption(opt = "a", lopt = "add1", help = "Add number") int a,
			@ShellOption(opt = "b", lopt = "add2", help = "Added number", defaultValue = "1") int b) {
		return exampleService.add(new SumArgument(a, b));
	}

	/**
	 * $> set -l 1,2 -s x3,x4
	 */
	@ShellMethod(keys = "set", group = GROUP_NAME, help = "Direct set parameter injection testing")
	public String set(@ShellOption(opt = "s", lopt = "set", help = "Set<String> type argument field") Set<String> set1,
			@ShellOption(opt = "l", lopt = "list", help = "List<Integer> type argument field") List<Integer> list) {
		return "Direct mixed set parameter injection results: set=" + set1 + ", list=" + list;
	}

	/**
	 * $> mixed -l x1,x2 -m a1=b1,a2=b2 -p aa1=bb1,aa2=bb2 -s x3,x4
	 */
	@ShellMethod(keys = "mixed", group = GROUP_NAME, help = "Mixed set type parameter injection testing")
	public String mixed(MixedArgument arg) {
		return "Bean field mixed set parameter injection test results: " + arg.toString();
	}

	/**
	 * $> sum -a 1 -b 123
	 */
	@ShellMethod(keys = "logs", group = GROUP_NAME, help = "This is a shell command that can output logs in real time.")
	public String logs(
			@ShellOption(opt = "n", lopt = "num", required = false, defaultValue = "5", help = "Input parameters (number of messages)") int num,
			ShellContext context) {

		// Open the flow message output, and the client will always be
		// blocked waiting until ShellConsoles.end() is called.
		context.begin();

		// Used to simulate an asynchronous task, constantly outputting logs
		new Thread(() -> {
			try {
				for (int i = 1; i <= num; i++) {
					String message = "This is the " + i + "th message!";
					log.info("Example log write => {}", message);

					// Print stream message
					context.printf(message);

					try {
						Thread.sleep(1500L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				context.printf("Print successfully completed!");
			} finally {
				// Must end, and must be after ShellConsoles.begin()
				context.end();
			}

		}).start();

		return "Task 'Print-Log' starting up ...";
	}

}
