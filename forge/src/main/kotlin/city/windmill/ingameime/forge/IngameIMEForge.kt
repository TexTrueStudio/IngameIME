package city.windmill.ingameime.forge

import city.windmill.ingameime.IngameIME
import city.windmill.ingameime.client.ConfigHandler
import city.windmill.ingameime.client.KeyHandler
import city.windmill.ingameime.client.ScreenHandler
import city.windmill.ingameime.client.gui.OverlayScreen
import city.windmill.ingameime.client.jni.ExternalBaseIME
import city.windmill.ingameime.forge.register.ForgeConfigScreenRegister
import net.minecraft.client.Minecraft
import net.minecraftforge.client.ClientRegistry
import net.minecraftforge.client.ConfigGuiHandler
import net.minecraftforge.client.event.ScreenEvent
import net.minecraftforge.fml.IExtensionPoint
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent
import net.minecraftforge.network.NetworkConstants
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.LOADING_CONTEXT
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.runForDist
import java.util.function.BiFunction


@Mod(IngameIME.MODID)
object IngameIMEForge {
    val INGAMEIME_BUS = MOD_BUS

    init {
        //Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
        LOADING_CONTEXT.registerExtensionPoint(IExtensionPoint.DisplayTest::class.java) {
            IExtensionPoint.DisplayTest({ NetworkConstants.IGNORESERVERONLY }, { _, _ -> true })
        }
        /*
        LOADING_CONTEXT.registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory::class.java) {
            ConfigGuiHandler.ConfigGuiFactory(BiFunction { _, parent ->
                return@BiFunction ConfigHandler.createConfigScreen().setParentScreen(parent).build()
            })
        }
         */

        ForgeConfigScreenRegister.instance.getMod(IngameIME.MODID).registerModConfigScreen { parent ->
            return@registerModConfigScreen ConfigHandler.createConfigScreen().setParentScreen(parent).build()
        }

        runForDist({
            val platform = System.getProperty("os.name").lowercase()
            if (platform.contains("win")) {
                IngameIME.LOGGER.info("it is Windows OS! Loading mod...")

                with(INGAMEIME_BUS) {
                    addListener(::onClientSetup)
                    addListener(::enqueueIMC)
                }
            } else
                IngameIME.LOGGER.warn("This mod cant work in $platform !")
        }) { IngameIME.LOGGER.warn("This mod cant work in a DelicateServer!") }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onClientSetup(event: FMLClientSetupEvent) {
        ClientRegistry.registerKeyBinding(KeyHandler.toggleKey)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun enqueueIMC(event: InterModEnqueueEvent) {
        with(FORGE_BUS) {
            addListener<ScreenEvent.DrawScreenEvent.Post> {
                OverlayScreen.render(it.poseStack, it.mouseX, it.mouseY, it.partialTicks)
            }
            addListener<ScreenEvent.KeyboardKeyPressedEvent.Pre> {
                it.isCanceled = KeyHandler.KeyState.onKeyDown(it.keyCode, it.scanCode, it.modifiers)
            }
            addListener<ScreenEvent.KeyboardKeyReleasedEvent.Pre> {
                it.isCanceled = KeyHandler.KeyState.onKeyUp(it.keyCode, it.scanCode, it.modifiers)
            }
        }
        with(INGAMEIME_BUS) {
            addListener<ScreenEvents.WindowSizeChanged> {
                ExternalBaseIME.FullScreen = Minecraft.getInstance().window.isFullscreen
            }
            addListener<ScreenEvents.ScreenChanged> {
                ScreenHandler.ScreenState.onScreenChange(it.oldScreen, it.newScreen)
            }
            addListener<ScreenEvents.EditOpen> {
                ScreenHandler.ScreenState.EditState.onEditOpen(it.edit, it.caretPos)
            }
            addListener<ScreenEvents.EditCaret> {
                ScreenHandler.ScreenState.EditState.onEditCaret(it.edit, it.caretPos)
            }
            addListener<ScreenEvents.EditClose> {
                ScreenHandler.ScreenState.EditState.onEditClose(it.edit)
            }
        }
        ConfigHandler.initialConfig()
        //Ensure native dll are loaded, or crash the game
        IngameIME.LOGGER.info("Current IME State:${ExternalBaseIME.State}")
    }
}
