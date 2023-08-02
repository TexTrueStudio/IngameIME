package city.windmill.ingameime.fabric

import city.windmill.ingameime.IngameIME
import city.windmill.ingameime.client.*
import city.windmill.ingameime.client.gui.OverlayScreen
import city.windmill.ingameime.client.jni.ExternalBaseIME
import city.windmill.ingameime.fabric.ScreenEvents.EDIT_CARET
import city.windmill.ingameime.fabric.ScreenEvents.EDIT_CLOSE
import city.windmill.ingameime.fabric.ScreenEvents.EDIT_OPEN
import city.windmill.ingameime.fabric.ScreenEvents.SCREEN_CHANGED
import city.windmill.ingameime.fabric.ScreenEvents.WINDOW_SIZE_CHANGED
import dev.architectury.event.EventResult
import dev.architectury.event.events.client.ClientGuiEvent
import dev.architectury.event.events.client.ClientLifecycleEvent
import dev.architectury.event.events.client.ClientScreenInputEvent
import ladysnake.satin.api.event.ResolutionChangeCallback
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.Util
import net.minecraft.client.Minecraft

@Environment(EnvType.CLIENT)
object IngameIMEClientFabric : ClientModInitializer {

    /**
     * Track mouse move
     */
    private var prevX = 0
    private var prevY = 0

    override fun onInitializeClient() {
        IngameIME.registerConfigScreen()
        if (Util.getPlatform() == Util.OS.WINDOWS) {
            IngameIME.LOGGER.info("it is Windows OS! Loading mod...")

            ClientLifecycleEvent.CLIENT_STARTED.register(ClientLifecycleEvent.ClientState {
                ConfigHandler.initialConfig()

                ClientGuiEvent.RENDER_POST.register(ClientGuiEvent.ScreenRenderPost { _, graphics, mouseX, mouseY, delta ->
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
                        EventResult.interruptDefault()
                    else
                        EventResult.pass()
                })
                ClientScreenInputEvent.KEY_RELEASED_PRE.register(ClientScreenInputEvent.KeyReleased { _, _, keyCode, scanCode, modifiers ->
                    if (KeyHandler.KeyState.onKeyUp(keyCode, scanCode, modifiers))
                        EventResult.interruptDefault()
                    else
                        EventResult.pass()
                })
                if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("satin"))
                    ResolutionChangeCallback.EVENT.register(ResolutionChangeCallback { _, _ ->
                        ExternalBaseIME.FullScreen = Minecraft.getInstance().window.isFullscreen
                    })
                else {
                    WINDOW_SIZE_CHANGED.register(ScreenEvents.WindowSizeChanged { _, _ ->
                        ExternalBaseIME.FullScreen = Minecraft.getInstance().window.isFullscreen
                    })
                }
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
            KeyBindingHelper.registerKeyBinding(KeyHandler.toggleKey)
        } else
            IngameIME.LOGGER.warn("This mod cant work in ${Util.getPlatform()} !")
    }
}