package com.sandpolis.core.deployer.init;

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
import com.sandpolis.core.deployer.DeployerConfig;
import com.sandpolis.core.foundation.S7SMavenArtifact;
import com.sandpolis.core.instance.InitTask;

public class DeployKilo extends InitTask {

	private static final Logger log = LoggerFactory.getLogger(DeployKilo.class);

	@Override
	public TaskOutcome run(TaskOutcome.Factory outcome) throws Exception {

		Path lib = Paths.get(DeployerConfig.EMBEDDED.install().directory()
				// Substitute $HOME
				.replaceAll(Pattern.quote("${HOME}"), System.getProperty("user.home")));

		try {
			log.debug("Creating directory: {}", lib);
			Files.createDirectories(lib);
		} catch (IOException e) {
			// Force install if enabled
			// TODO
		}

		// Install modules
		for (var module : DeployerConfig.EMBEDDED.kilo().modules()) {
			var artifact = S7SMavenArtifact.of(module.group(), module.artifact(), module.version(),
					module.classifier());

			// First try embedded resources
			try (var in = DeployKilo.class.getResourceAsStream("/lib/" + artifact.filename())) {
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
					log.debug("Loading module {} from network", artifact.filename());
					Files.copy(in, lib.resolve(artifact.filename()), StandardCopyOption.REPLACE_EXISTING);

					// Verify hash
					if (!asByteSource(lib.resolve(artifact.filename()).toFile()).hash(Hashing.sha256()).toString()
							.equalsIgnoreCase(module.hash())) {
						throw new RuntimeException();
					}
					continue;
				}
			}

			// Failed to locate module
			throw new RuntimeException();
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
		return "Install Kilo agent";
	}

}