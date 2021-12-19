//============================================================================//
//                                                                            //
//            Copyright © 2015 - 2022 Sandpolis Software Foundation           //
//                                                                            //
//  This source file is subject to the terms of the Mozilla Public License    //
//  version 2. You may not use this file except in compliance with the MPLv2. //
//                                                                            //
//============================================================================//
package org.s7s.core.deployer;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.s7s.core.deployer.DeployerConfig.InstallCfg;
import org.s7s.core.deployer.DeployerConfig.KiloCfg;
import org.s7s.core.deployer.DeployerConfig.KiloCfg.KiloModuleCfg;
import org.s7s.core.deployer.DeployerConfig.NetworkCfg;
import org.s7s.core.instance.Entrypoint;

public record DeployerConfig(String agent_type, NetworkCfg network, InstallCfg install, KiloCfg java) {

	public static final DeployerConfig EMBEDDED = load();

	public static DeployerConfig load() {

		try (var in = DeployerConfig.class.getResourceAsStream("/org.s7s.core.deployer.json")) {
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
