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
                    //addListener(::registerKeys)
                    addListener(::onClientSetup)
                    //addListener(::enqueueIMC)
                }
            } else
                IngameIMEClient.LOGGER.warn("This mod cant work in ${Util.getPlatform()} !")
        }) { IngameIMEClient.LOGGER.warn("This mod cant work in a DelicateServer!") }
    }

    /*
    private fun registerKeys(event: RegisterKeyMappingsEvent) {
        event.register(KeyHandler.toggleKey)
    }
     */

    private fun onClientSetup(event: FMLClientSetupEvent) {
        IngameIMEClient.registerConfigScreen()
        event.enqueueWork {
            IngameIMEClient.onInitClient()
        }
        KeyMappingRegistry.register(KeyHandler.toggleKey)
        //Ensure native dll are loaded, or crash the game
        IngameIMEClient.LOGGER.info("Current IME State:${ExternalBaseIME.State}")
    }

    /*
    @Suppress("UNUSED_PARAMETER")
    private fun enqueueIMC(event: InterModEnqueueEvent) {

        with(FORGE_BUS) {
            addListener<ScreenEvent.Render.Post> {
                OverlayScreen.render(it.poseStack, it.mouseX, it.mouseY, it.partialTick)
            }
            addListener<ScreenEvent.KeyPressed.Pre> {
                it.isCanceled = KeyHandler.KeyState.onKeyDown(it.keyCode, it.scanCode, it.modifiers)
            }
            addListener<ScreenEvent.KeyReleased.Pre> {
                it.isCanceled = KeyHandler.KeyState.onKeyUp(it.keyCode, it.scanCode, it.modifiers)
            }
        }
        with(INGAMEIME_BUS) {
            addListener<LegacyScreenEvents.WindowSizeChanged> {
                ExternalBaseIME.FullScreen = Minecraft.getInstance().window.isFullscreen
            }
            addListener<LegacyScreenEvents.ScreenChanged> {
                ScreenHandler.ScreenState.onScreenChange(it.oldScreen, it.newScreen)
            }
            addListener<LegacyScreenEvents.EditOpen> {
                ScreenHandler.ScreenState.EditState.onEditOpen(it.edit, it.caretPos)
            }
            addListener<LegacyScreenEvents.EditCaret> {
                ScreenHandler.ScreenState.EditState.onEditCaret(it.edit, it.caretPos)
            }
            addListener<LegacyScreenEvents.EditClose> {
                ScreenHandler.ScreenState.EditState.onEditClose(it.edit)
            }
        }
        ConfigHandler.initialConfig()
        //Ensure native dll are loaded, or crash the game
        LOGGER.info("Current IME State:${ExternalBaseIME.State}")
    }
    */
}