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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.runner.Description;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.deploy.impl.VerticleManager;
import org.vertx.java.test.junit.annotations.TestModule;
import org.vertx.java.test.junit.annotations.TestModules;
import org.vertx.java.test.junit.annotations.TestVerticle;
import org.vertx.java.test.junit.annotations.TestVerticles;


/**
 * @author swilliams
 *
 */
public class Deployer {

  private final AtomicInteger deploymentCounter = new AtomicInteger(0);

  private final File modDir;

  private final VerticleManager manager;

  private long shutdownTimeout;

  Deployer(VerticleManager manager, File modDir, long shutdownTimeout) {
    this.manager = manager;
    this.modDir = modDir;
    this.shutdownTimeout = shutdownTimeout;
  }

  public void deploy(Description description) {
    deployModules(description.getAnnotation(TestModules.class));
    deployModule(description.getAnnotation(TestModule.class));
    deployVerticles(description.getAnnotation(TestVerticles.class));
    deployVerticle(description.getAnnotation(TestVerticle.class));
  }


  private void deployVerticles(TestVerticles verticles) {

    if (verticles == null) {
      return;
    }

    final CountDownLatch latch = new CountDownLatch(verticles.value().length);
    for (TestVerticle v : verticles.value()) {
      JsonObject config = getJsonConfig(v.jsonConfig());

      URL[] urls = findVerticleURLs(v);
      manager.deployVerticle(v.worker(), v.main(), config, urls, v.instances(), modDir, new CountDownLatchDoneHandler<String>(latch));
      deploymentCounter.incrementAndGet();
    }

    await(latch);
  }

  private void deployVerticle(TestVerticle v) {
    if (v == null) {
      return;
    }

    final CountDownLatch latch = new CountDownLatch(1);
    JsonObject config = getJsonConfig(v.jsonConfig());
    URL[] urls = findVerticleURLs(v);

    manager.deployVerticle(v.worker(), v.main(), config, urls, v.instances(), modDir, new CountDownLatchDoneHandler<String>(latch));
    deploymentCounter.incrementAndGet();

    await(latch);
  }

  private void deployModules(TestModules amodules) {
    if (amodules == null) {
      return;
    }

    final CountDownLatch latch = new CountDownLatch(amodules.value().length);

    for (TestModule m : amodules.value()) {
      JsonObject config = getJsonConfig(m.jsonConfig());
      manager.deployMod(m.name(), config, m.instances(), modDir, new CountDownLatchDoneHandler<String>(latch));
      deploymentCounter.incrementAndGet();
    }

    await(latch);
  }

  private void deployModule(TestModule m) {
    if (m == null) {
      return;
    }

    final CountDownLatch latch = new CountDownLatch(1);

    JsonObject config = getJsonConfig(m.jsonConfig());
    manager.deployMod(m.name(), config, m.instances(), modDir, new CountDownLatchDoneHandler<String>(latch));
    deploymentCounter.incrementAndGet();

    await(latch);
  }

  private URL[] findVerticleURLs(TestVerticle v) {
    Set<URL> urlSet = new HashSet<URL>();

    if (v.urls().length > 0) {
      for (String path : v.urls()) {

        try {

          URL url = new File(path).toURI().toURL();
          urlSet.add(url);

        } catch (Exception e) {
          // TODO log something here
          e.printStackTrace();
        }
      }
    }

    try {
      String main = v.main();
      if (main.indexOf(':') > -1) {
        main = main.substring(main.indexOf(':') + 1);
      }

      // check for class, prep for locating root URL
      int parts = 0;
      if (!main.endsWith(".xml")) {
        parts = main.split("\\.").length;
        main = main.replaceAll("\\.", "/");
        main = main + ".class";
      }

      // contortions to get parent, may not be entirely accurate...
      // URL url = getClass().getClassLoader().getResource(main);
      URL url = Thread.currentThread().getContextClassLoader().getResource(main);

      if (url != null) {
        Path path = Paths.get(url.toURI());

        int i = parts;
        while (i > 0) {
          path = path.getParent();
          i--;
        }

        url = path.toUri().toURL();
        urlSet.add(url);
      }

    } catch (Exception e) {
      // TODO log something here
      e.printStackTrace();
    }

    URL[] urls = new URL[urlSet.size()];
    return urlSet.toArray(urls);
  }

  private JsonObject getJsonConfig(String jsonConfig) {
    JsonObject config;

    if (jsonConfig.startsWith("file:")) {
      String filename = jsonConfig.replaceFirst("file:", "");
      Path json = new File(filename).toPath();

      try {
        Charset utf8 = Charset.forName("UTF-8");
        byte[] bytes = Files.readAllBytes(json);
        config = new JsonObject(new String(bytes, utf8));

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    else {
      config = new JsonObject(jsonConfig);
    }

    return config;
  }

  private void await(final CountDownLatch latch) {
    try {
      latch.await(shutdownTimeout, TimeUnit.SECONDS);

    } catch (InterruptedException e) {
      // TODO log something here
      e.printStackTrace();
    }
  }

  public int getDeploymentCount() {
    return deploymentCounter.get();
  }

}
