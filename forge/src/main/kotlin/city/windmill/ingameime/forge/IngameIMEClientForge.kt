package city.windmill.ingameime.forge

import city.windmill.ingameime.IngameIMEClient
import city.windmill.ingameime.client.*
import city.windmill.ingameime.client.handler.KeyHandler
import city.windmill.ingameime.client.jni.ExternalBaseIME
import dev.architectury.registry.client.keymappings.KeyMappingRegistry
import net.minecraft.Util
import net.minecraftforge.fml.IExtensionPoint
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.network.NetworkConstants
import thedarkcolour.kotlinforforge.forge.LOADING_CONTEXT
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.runForDist


@Mod(IngameIMEClient.MODID)
object IngameIMEClientForge {
    val INGAMEIME_BUS = MOD_BUS

    init {
        //Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
        LOADING_CONTEXT.registerExtensionPoint(IExtensionPoint.DisplayTest::class.java) { IExtensionPoint.DisplayTest(
                { NetworkConstants.IGNORESERVERONLY }, { _, _ -> true })
        }

        runForDist({
            if (Util.getPlatform() == Util.OS.WINDOWS) {
                IngameIMEClient.LOGGER.info("it is Windows OS! Loading mod...")

                with(INGAMEIME_BUS) {
                    addListener(::onClientSetup)
                }
            } else
                IngameIMEClient.LOGGER.warn("This mod cant work in ${Util.getPlatform()} !")
        }) { IngameIMEClient.LOGGER.warn("This mod cant work in a DelicateServer!") }
    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        IngameIMEClient.registerConfigScreen()
        event.enqueueWork {
            IngameIMEClient.onInitClient()
        }
        KeyMappingRegistry.register(KeyHandler.toggleKey)
        //Ensure native dll are loaded, or crash the game
        IngameIMEClient.LOGGER.info("Current IME State:${ExternalBaseIME.State}")
    }
}