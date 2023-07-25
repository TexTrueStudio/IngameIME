package city.windmill.ingameime

import city.windmill.ingameime.client.ConfigHandler
import me.shedaniel.architectury.platform.Platform
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


object IngameIME {
    const val MODID = "ingameime"
    const val MODNAME = "ContingameIME"
    val LOGGER: Logger = LogManager.getLogger()

    fun onInitClient() {
        Platform.getMod(MODID).registerConfigurationScreen { parent ->
            ConfigHandler.createConfigScreen().setParentScreen(parent).build()
        }
    }
}