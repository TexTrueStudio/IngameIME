package city.windmill.ingameime

import city.windmill.ingameime.client.ConfigHandler
import dev.architectury.platform.Platform
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object IngameIME {
    const val MODID = "ingameime"
    const val MODNAME = "IngameIME"
    val LOGGER: Logger = LoggerFactory.getLogger(MODNAME)

    fun registerConfigScreen() {
        Platform.getMod(MODID).registerConfigurationScreen { parent ->
            ConfigHandler.createConfigScreen().setParentScreen(parent).build() }
    }
}