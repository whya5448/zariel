package org.metalscraps.eso.lang.server.config

import org.metalscraps.eso.lang.lib.config.ESOConfig
import org.metalscraps.eso.lang.lib.config.ESOConfigOptions
import org.metalscraps.eso.lang.server.config.ServerConfig.ServerConfigOptions.*
import java.nio.file.Path
import java.nio.file.Paths

class ServerConfig(configPath: Path) : ESOConfig(configPath) {

    internal val workDir: Path
        get() = Paths.get(getConf(WORK_DIR))

    internal val forceUpload: Boolean
        get() = getConf(FORCE_UPLOAD).toBoolean()

    enum class ServerConfigOptions(private val v: String) : ESOConfigOptions {

        WORK_DIR("ESO_SERVER_DIR"),
        FORCE_UPLOAD("FORCE_UPLOAD");

        override fun toString(): String { return v }
    }
}