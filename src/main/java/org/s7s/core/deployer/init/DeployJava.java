//============================================================================//
//                                                                            //
//            Copyright © 2015 - 2022 Sandpolis Software Foundation           //
//                                                                            //
//  This source file is subject to the terms of the Mozilla Public License    //
//  version 2. You may not use this file except in compliance with the MPLv2. //
//                                                                            //
//============================================================================//
package org.s7s.core.deployer.init;

import static com.google.common.io.Files.asByteSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.Hashing;
import org.s7s.core.deployer.DeployerConfig;
import org.s7s.core.foundation.S7SMavenArtifact;
import org.s7s.core.foundation.S7SSystem;
import org.s7s.core.instance.InitTask;

public class DeployJava extends InitTask {

	private static final Logger log = LoggerFactory.getLogger(DeployJava.class);

	@Override
	public TaskOutcome run(TaskOutcome.Factory outcome) throws Exception {

		Path lib = Paths.get(DeployerConfig.EMBEDDED.install().directory()
				// Substitute $HOME
				.replaceAll(Pattern.quote("${HOME}"), System.getProperty("user.home")))
				// Output to lib
				.resolve("lib");

		Path bin = lib.resolveSibling("bin");

		try {
			log.debug("Creating directory: {}", lib);
			Files.createDirectories(lib);
		} catch (IOException e) {
			// Force install if enabled
			// TODO
		}

		// Install modules
		for (var module : DeployerConfig.EMBEDDED.java().modules()) {
			var artifact = S7SMavenArtifact.of(module.group(), module.artifact(), module.version(),
					module.classifier());

			// First try embedded resources
			try (var in = DeployJava.class.getResourceAsStream("/lib/" + artifact.filename())) {
				if (in != null) {
					log.debug("Loading module {} from embedded resource", artifact.filename());
					Files.copy(in, lib.resolve(artifact.filename()), StandardCopyOption.REPLACE_EXISTING);

					// Verify hash
					if (!asByteSource(lib.resolve(artifact.filename()).toFile()).hash(Hashing.sha256()).toString()
							.equalsIgnoreCase(module.hash())) {
						throw new RuntimeException();
					}
					continue;
				}
			}

			// Next try the network
			try (var in = artifact.download()) {
				if (in != null) {
					log.debug("Downloading module: {}", artifact.filename());
					Files.copy(in, lib.resolve(artifact.filename()), StandardCopyOption.REPLACE_EXISTING);

					// Verify hash
					if (!asByteSource(lib.resolve(artifact.filename()).toFile()).hash(Hashing.sha256()).toString()
							.equalsIgnoreCase(module.hash())) {
						throw new RuntimeException("Module hash verification failed");
					}
					continue;
				}
			}

			// Failed to locate module
			throw new RuntimeException();
		}

		// Configure start script
		switch (S7SSystem.OS_TYPE) {
		case LINUX:
			Files.writeString(bin.resolve("sandpolis-agent"), """
					#!/bin/sh
					exec /usr/bin/java \
					    -Dpath.data=/var/lib/sandpolis-agent/data \
					    -Dpath.log=/var/log/sandpolis-agent \
					    -Dpath.lib=/usr/share/java/sandpolis-agent/lib \
					    --module-path /usr/share/java/sandpolis-agent/lib \
					    -m ${_module}/${_module}.Main
											""");
			break;
		}

		// Configure autostart
		if (DeployerConfig.EMBEDDED.install().autostart()) {
			log.debug("Preparing to configure autostart");
			// TODO
		}

		return outcome.succeeded();
	}

	@Override
	public String description() {
		return "Install Java agent";
	}

}
