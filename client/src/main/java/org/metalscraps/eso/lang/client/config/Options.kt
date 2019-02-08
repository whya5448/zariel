package org.metalscraps.eso.lang.client.config

import org.metalscraps.eso.lang.lib.config.ESOConfigOptions

enum class Options private constructor(private val v: String) : ESOConfigOptions {

    LANG_VERSION("ver"),
    ZANATA_MANAGER_X("x"),
    ZANATA_MANAGER_Y("y"),
    ZANATA_MANAGER_WIDTH("width"),
    ZANATA_MANAGER_HEIGHT("height"),
    ZANATA_MANAGER_OPACITY("opacity"),
    LAUNCH_ESO_AFTER_UPDATE("launchESOafterUpdate"),
    UPDATE_LANG("doLangUpdate"),
    UPDATE_DESTINATIONS("doDestnationsUpdate"),
    ENABLE_ZANATA_LISTENER("enableZanataListener");

    override fun toString(): String { return v }
}
