package org.metalscraps.eso.lang.server.compress.config

import org.metalscraps.eso.lang.lib.config.ESOConfig
import org.metalscraps.eso.lang.lib.config.ESOConfigOptions
import org.metalscraps.eso.lang.server.compress.config.CompressServerConfig.CompressServerConfigOptions.*

import java.nio.file.Path
import java.nio.file.Paths

class CompressServerConfig(configPath: Path) : ESOConfig(configPath) {

    internal val mainServerUserID: String
        get() = getConf(MAIN_SERVER_USER_ID)

    internal val mainServerHostName: String
        get() = getConf(MAIN_SERVER_HOST_NAME)

    internal val mainServerPort: Int
        get() = getConf(MAIN_SERVER_PORT).toInt()

    internal val mainServerPOPath: String
        get() = getConf(MAIN_SERVER_PO_PATH)

    internal val mainServerVersionDocumentPath: String
        get() = getConf(MAIN_SERVER_VERSION_DOCUMENT_PATH)

    internal val workDir: Path
        get() = Paths.get(getConf(WORK_DIR))

    enum class CompressServerConfigOptions(private val v: String) : ESOConfigOptions {

        MAIN_SERVER_USER_ID("main_server_user_id"),
        MAIN_SERVER_HOST_NAME("main_server_host_name"),
        MAIN_SERVER_PORT("main_server_port"),
        MAIN_SERVER_PO_PATH("main_server_po_path"),
        MAIN_SERVER_VERSION_DOCUMENT_PATH("main_server_version_document_path"),
        WORK_DIR("work_dir");

        override fun toString(): String { return v }
    }
}