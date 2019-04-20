package org.metalscraps.eso.lang.server.config

import org.metalscraps.eso.lang.lib.config.ESOConfig
import org.metalscraps.eso.lang.lib.config.ESOConfigOptions
import org.metalscraps.eso.lang.server.config.ServerConfig.ServerConfigOptions.*
import java.nio.file.Path
import java.nio.file.Paths

class ServerConfig(configPath: Path) : ESOConfig(configPath) {

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

        GCP_PROJECT_NAME("GCP_PROJECT_NAME"),
        GCP_PROJECT_ZONE("GCP_PROJECT_ZONE"),
        GCP_COMPRESS_SERVER_INSTANCE_NAME("GCP_COMPRESS_SERVER_INSTANCE_NAME"),
        GCP_PERM_JSON_PATH("GCP_PERM_JSON_PATH"),
        WORK_DIR("ESO_SERVER_DIR");

        override fun toString(): String { return v }
    }
}