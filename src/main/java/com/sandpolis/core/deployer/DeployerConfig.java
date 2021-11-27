package com.sandpolis.core.deployer;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandpolis.core.deployer.DeployerConfig.InstallCfg;
import com.sandpolis.core.deployer.DeployerConfig.KiloCfg;
import com.sandpolis.core.deployer.DeployerConfig.KiloCfg.KiloModuleCfg;
import com.sandpolis.core.deployer.DeployerConfig.NetworkCfg;
import com.sandpolis.core.instance.Entrypoint;

public record DeployerConfig(String agent_type, NetworkCfg network, InstallCfg install, KiloCfg kilo) {

	public static final DeployerConfig EMBEDDED = load();

	public static DeployerConfig load() {

		try (var in = DeployerConfig.class.getResourceAsStream("/com.sandpolis.core.deployer.json")) {
			if (in != null) {
				return new ObjectMapper().readValue(in, DeployerConfig.class);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return null;
	}

	public static record NetworkCfg(String address) {
	}

	public static record InstallCfg(String directory, boolean autorecover, boolean autostart) {

	}

	public static record KiloCfg(KiloModuleCfg[] modules) {
		public static record KiloModuleCfg(String group, String artifact, String version, String classifier,
				String hash) {

		}
	}
}
