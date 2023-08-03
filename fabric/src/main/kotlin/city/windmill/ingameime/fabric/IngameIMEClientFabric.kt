package city.windmill.ingameime.fabric

import city.windmill.ingameime.IngameIMEClient
import city.windmill.ingameime.client.*
import city.windmill.ingameime.client.handler.KeyHandler
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.Util

@Environment(EnvType.CLIENT)
object IngameIMEClientFabric : ClientModInitializer {

    override fun onInitializeClient() {
        IngameIMEClient.registerConfigScreen()
        if (Util.getPlatform() == Util.OS.WINDOWS) {
            IngameIMEClient.LOGGER.info("it is Windows OS! Loading mod...")

            ClientLifecycleEvents.CLIENT_STARTED.register(ClientLifecycleEvents.ClientStarted {
                IngameIMEClient.onInitClient()
            })
            KeyBindingHelper.registerKeyBinding(KeyHandler.toggleKey)
        } else
            IngameIMEClient.LOGGER.warn("This mod cant work in ${Util.getPlatform()} !")
    }
}