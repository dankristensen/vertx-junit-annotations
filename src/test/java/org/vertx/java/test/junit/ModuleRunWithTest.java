/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vertx.java.test.junit;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.vertx.java.test.TestModule;
import org.vertx.java.test.TestModules;
import org.vertx.java.test.VertxConfiguration;
import org.vertx.java.test.VertxTestBase;
import org.vertx.java.test.utils.QueueReplyHandler;

/**
 * @author swilliams
 * 
 */
@RunWith(VertxJUnit4ClassRunner.class)
@VertxConfiguration(modsDir = "src/test/mods")
@TestModules({ @TestModule(name = "test~echo0~1.0") })
public class ModuleRunWithTest extends VertxTestBase {

	private long timeout = 10L;

	@Before
	public void setup() {
		this.timeout = Long.parseLong(System.getProperty("vertx.test.timeout",
				"15"));
	}

	@Test
	public void testModuleEcho0() {

		String QUESTION = "How now brown cow?";

		final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();

		getVertx().eventBus().send("vertx.test.mods.echo0", QUESTION,
				new QueueReplyHandler<String>(queue, timeout));

		try {
			String answer = queue.poll(timeout, TimeUnit.SECONDS);
			System.out.println("answer: " + answer);
			Assert.assertTrue(QUESTION.equals(answer));

		} catch (InterruptedException e) {
			Assert.fail(e.getMessage());
		}

	}

	@Test
	@TestModule(name = "test~echo1~1.0")
	public void testModuleEcho1() {
		String QUESTION = "Is it beer oclock yet?";

		final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();

		getVertx().eventBus().send("vertx.test.mods.echo1", QUESTION,
				new QueueReplyHandler<String>(queue, timeout));

		try {
			String answer = queue.poll(timeout, TimeUnit.SECONDS);
			System.out.println("answer: " + answer);
			Assert.assertTrue(QUESTION.equals(answer));

		} catch (InterruptedException e) {
			Assert.fail(e.getMessage());
		}

	}

	@Test
	@TestModules({ @TestModule(name = "test~echo2~1.0") })
	public void testModulesEcho2() {
		String QUESTION = "What ho!";

		final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();

		getVertx().eventBus().send("vertx.test.mods.echo2", QUESTION,
				new QueueReplyHandler<String>(queue, timeout));

		try {
			String answer = queue.poll(timeout, TimeUnit.SECONDS);
			System.out.println("answer: " + answer);
			Assert.assertTrue(QUESTION.equals(answer));

		} catch (InterruptedException e) {
			Assert.fail(e.getMessage());
		}

	}

}
