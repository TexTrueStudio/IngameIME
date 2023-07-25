package city.windmill.ingameime.forge

import city.windmill.ingameime.client.ConfigHandler
import city.windmill.ingameime.IngameIME
import city.windmill.ingameime.client.IMEHandler
import city.windmill.ingameime.client.KeyHandler
import city.windmill.ingameime.client.ScreenHandler
import city.windmill.ingameime.client.gui.OverlayScreen
import city.windmill.ingameime.client.jni.ExternalBaseIME
import city.windmill.ingameime.forge.ScreenEvents.EDIT_CARET
import city.windmill.ingameime.forge.ScreenEvents.EDIT_CLOSE
import city.windmill.ingameime.forge.ScreenEvents.EDIT_OPEN
import city.windmill.ingameime.forge.ScreenEvents.SCREEN_CHANGED
import city.windmill.ingameime.forge.ScreenEvents.WINDOW_SIZE_CHANGED
import me.shedaniel.architectury.event.events.GuiEvent
import me.shedaniel.architectury.event.events.client.ClientLifecycleEvent
import me.shedaniel.architectury.event.events.client.ClientScreenInputEvent
import me.shedaniel.architectury.registry.KeyBindings
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.world.InteractionResult
import net.minecraftforge.fml.ExtensionPoint
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.network.FMLNetworkConstants
import org.apache.commons.lang3.tuple.Pair
import thedarkcolour.kotlinforforge.forge.LOADING_CONTEXT
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.runForDist
import java.util.function.BiPredicate
import java.util.function.Supplier

@Mod(IngameIME.MODID)
object IngameIMEClientForge {
    val modEventBus = MOD_BUS

    /**
     * Track mouse move
     */
    private var prevX = 0
    private var prevY = 0

    init {
        //Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
        LOADING_CONTEXT.registerExtensionPoint(ExtensionPoint.DISPLAYTEST) {
            Pair.of(Supplier { FMLNetworkConstants.IGNORESERVERONLY }, BiPredicate { _, _ -> true })
        }

        runForDist({
            if (Util.getPlatform() == Util.OS.WINDOWS) {
                IngameIME.LOGGER.info("it is Windows OS! Loading mod...")

                modEventBus.addListener(::onInitializeClient)
            } else {
                IngameIME.LOGGER.warn("This mod cant work in ${Util.getPlatform()} !")
            }
        }) {
            IngameIME.LOGGER.warn("This mod cant work in a DelicateServer!")
        }
    }

    private fun onInitializeClient(event: FMLClientSetupEvent) {
        IngameIME.onInitClient()
        KeyBindings.registerKeyBinding(KeyHandler.toggleKey)
        ClientLifecycleEvent.CLIENT_STARTED.register(ClientLifecycleEvent.ClientState {
            ConfigHandler.initialConfig()

            GuiEvent.RENDER_POST.register(GuiEvent.ScreenRenderPost { _, graphics, mouseX, mouseY, delta ->
                //Track mouse move here
                if (mouseX != prevX || mouseY != prevY) {
                    ScreenEvents.SCREEN_MOUSE_MOVE.invoker().onMouseMove(prevX, prevY, mouseX, mouseY)

                    prevX = mouseX
                    prevY = mouseY
                }

                OverlayScreen.render(graphics, mouseX, mouseY, delta)
            })
            ScreenEvents.SCREEN_MOUSE_MOVE.register(ScreenEvents.MouseMove { _, _, _, _ ->
                IMEHandler.IMEState.onMouseMove()
            })
            ClientScreenInputEvent.KEY_PRESSED_PRE.register(ClientScreenInputEvent.KeyPressed { _, _, keyCode, scanCode, modifiers ->
                if (KeyHandler.KeyState.onKeyDown(keyCode, scanCode, modifiers))
                    InteractionResult.CONSUME
                else
                    InteractionResult.PASS
            })
            ClientScreenInputEvent.KEY_RELEASED_PRE.register(ClientScreenInputEvent.KeyReleased { _, _, keyCode, scanCode, modifiers ->
                if (KeyHandler.KeyState.onKeyUp(keyCode, scanCode, modifiers))
                    InteractionResult.CONSUME
                else
                    InteractionResult.PASS
            })
            /*
            if (ModList.get().isLoaded("satin"))
                ResolutionChangeCallback.EVENT.register(ResolutionChangeCallback { _, _ ->
                    ExternalBaseIME.FullScreen = Minecraft.getInstance().window.isFullscreen
                })
            else {
                WINDOW_SIZE_CHANGED.register(ScreenEvents.WindowSizeChanged { _, _ ->
                    ExternalBaseIME.FullScreen = Minecraft.getInstance().window.isFullscreen
                })
            }
             */
            WINDOW_SIZE_CHANGED.register(ScreenEvents.WindowSizeChanged { _, _ ->
                ExternalBaseIME.FullScreen = Minecraft.getInstance().window.isFullscreen
            })
            with(ScreenHandler.ScreenState) {
                SCREEN_CHANGED.register(ScreenEvents.ScreenChanged(::onScreenChange))
            }
            with(ScreenHandler.ScreenState.EditState) {
                EDIT_OPEN.register(ScreenEvents.EditOpen(::onEditOpen))
                EDIT_CARET.register(ScreenEvents.EditCaret(::onEditCaret))
                EDIT_CLOSE.register(ScreenEvents.EditClose(::onEditClose))
            }
            //Ensure native dll are loaded, or crash the game
            IngameIME.LOGGER.info("Current IME State:${ExternalBaseIME.State}")
        })
        //Ensure native dll are loaded, or crash the game
        IngameIME.LOGGER.info("Current IME State:${ExternalBaseIME.State}")
    }
}