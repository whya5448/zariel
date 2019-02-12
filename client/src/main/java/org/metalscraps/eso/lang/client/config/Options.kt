package org.metalscraps.eso.lang.client.config

import org.metalscraps.eso.lang.lib.config.ESOConfigOptions

enum class Options(private val v: String) : ESOConfigOptions {

    LOCAL_LANG_VERSION("local_lang_ver"),
    ZANATA_MANAGER_X("zanata_manager_x"),
    ZANATA_MANAGER_Y("zanata_manager_y"),
    ZANATA_MANAGER_WIDTH("zanata_manager_width"),
    ZANATA_MANAGER_HEIGHT("zanata_manager_height"),
    ZANATA_MANAGER_OPACITY("zanata_manager_opacity"),
    LAUNCH_ESO_AFTER_UPDATE("launchESOafterUpdate"),
    UPDATE_LANG("doUpdateLang"),
    UPDATE_DESTINATIONS("doUpdateDestnations"),
    ENABLE_ZANATA_LISTENER("enableZanataListener");

    override fun toString(): String { return v }
}
