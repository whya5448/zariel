package org.metalscraps.eso.lang.client.config

import org.metalscraps.eso.lang.client.config.Options.*
import org.metalscraps.eso.lang.lib.config.ESOConfig
import java.nio.file.Path

class ClientConfig(configDirPath: Path, configPath: Path) : ESOConfig(configDirPath, configPath) {

    internal val isLaunchAfterUpdate: Boolean
        get() = getConf(LAUNCH_ESO_AFTER_UPDATE).toBoolean()

    internal val isUpdateLang: Boolean
        get() = getConf(UPDATE_LANG).toBoolean()

    internal val isUpdateDestination: Boolean
        get() = getConf(UPDATE_DESTINATIONS).toBoolean()

    internal val isEnableZanataListener: Boolean
        get() = getConf(ENABLE_ZANATA_LISTENER).toBoolean()

    internal val localLangVersion: Long
        get() = getConf(LOCAL_LANG_VERSION).toLong()

    internal val zanataManagerX: Int
        get() = getConf(ZANATA_MANAGER_X).toInt()

    internal val zanataManagerY: Int
        get() = getConf(ZANATA_MANAGER_Y).toInt()

    internal val zanataManagerW: Int
        get() = getConf(ZANATA_MANAGER_WIDTH).toInt()

    internal val zanataManagerH: Int
        get() = getConf(ZANATA_MANAGER_HEIGHT).toInt()

    internal val zanataManagerO: Float
        get() = getConf(ZANATA_MANAGER_OPACITY).toFloat()
}
