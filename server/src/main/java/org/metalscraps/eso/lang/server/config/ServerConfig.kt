package org.metalscraps.eso.lang.server.config

import org.metalscraps.eso.lang.lib.config.ESOConfig
import org.metalscraps.eso.lang.lib.config.ESOConfigOptions
import org.metalscraps.eso.lang.server.config.ServerConfig.ServerConfigOptions.*
import java.nio.file.Path
import java.nio.file.Paths

class ServerConfig(configDirPath: Path, configPath: Path) : ESOConfig(configDirPath, configPath) {

    internal val gcpProjectName: String
        get() = getConf(GCP_PROJECT_NAME)

    internal val gcpProjectZone: String
        get() = getConf(GCP_PROJECT_ZONE)

    internal val gcpCompressServerInstanceName: String
        get() = getConf(GCP_COMPRESS_SERVER_INSTANCE_NAME)

    internal val gcpPermJsonPath: Path
        get() = Paths.get(getConf(GCP_PERM_JSON_PATH))

    internal val workDir: Path
        get() = Paths.get(getConf(WORK_DIR))

    enum class ServerConfigOptions(private val v: String) : ESOConfigOptions {

        GCP_PROJECT_NAME("gcp_project_name"),
        GCP_PROJECT_ZONE("gcp_project_zone"),
        GCP_COMPRESS_SERVER_INSTANCE_NAME("gcp_compress_server_instance_name"),
        GCP_PERM_JSON_PATH("gcp_perm_json_path"),
        WORK_DIR("work_dir");

        override fun toString(): String { return v }
    }
}