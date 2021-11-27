package com.sandpolis.core.deployer;

public record DeployerConfig(String agent_type, NetworkCfg network, InstallCfg install, KiloCfg kilo) {

	public static final DeployerConfig EMBEDDED = load();

	public static DeployerConfig load() {

		try (var in = Entrypoint.data().main().getResourceAsStream("/com.sandpolis.core.deployer.json")) {
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

	public static record KiloCfg(List<KiloModuleCfg> modules) {
		public static record KiloModuleCfg(String group, String artifact, String version, String classifier, String hash) {

		}
	}
}
