@file:Suppress("unused")

package org.metalscraps.eso.lang.client.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.file.Paths

@Configuration
class SpringBean {

    @Bean
    fun getClientConfig(): ClientConfig {
        val appPath = Paths.get(System.getenv("localappdata") + "/" + "dc_eso_client")
        val config = ClientConfig(appPath, appPath.resolve(".config"))
        config.load(mapOf(
                ClientConfig.ClientConfigOptions.LOCAL_LANG_VERSION to 0,
                ClientConfig.ClientConfigOptions.ZANATA_MANAGER_X to 0,
                ClientConfig.ClientConfigOptions.ZANATA_MANAGER_Y to 0,
                ClientConfig.ClientConfigOptions.ZANATA_MANAGER_WIDTH to 100,
                ClientConfig.ClientConfigOptions.ZANATA_MANAGER_HEIGHT to 48,
                ClientConfig.ClientConfigOptions.ZANATA_MANAGER_OPACITY to .5f,
                ClientConfig.ClientConfigOptions.LAUNCH_ESO_AFTER_UPDATE to true,
                ClientConfig.ClientConfigOptions.UPDATE_LANG to true,
                ClientConfig.ClientConfigOptions.UPDATE_DESTINATIONS to true,
                ClientConfig.ClientConfigOptions.ENABLE_ZANATA_LISTENER to true
        ))

        return config
    }

}
