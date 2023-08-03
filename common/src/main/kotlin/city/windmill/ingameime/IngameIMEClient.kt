package city.windmill.ingameime

import city.windmill.ingameime.client.event.ClientScreenEventHooks
import city.windmill.ingameime.client.gui.OverlayScreen
import city.windmill.ingameime.client.handler.ConfigHandler
import city.windmill.ingameime.client.handler.IMEHandler
import city.windmill.ingameime.client.handler.KeyHandler
import city.windmill.ingameime.client.handler.ScreenHandler
import city.windmill.ingameime.client.jni.ExternalBaseIME
import dev.architectury.event.EventResult
import dev.architectury.event.events.client.ClientGuiEvent
import dev.architectury.event.events.client.ClientScreenInputEvent
import dev.architectury.platform.Platform
import net.minecraft.client.Minecraft
import org.slf4j.LoggerFactory

object IngameIMEClient {
    const val MODNAME = "ContingameIME"
    const val MODID = "ingameime"
    val LOGGER = LoggerFactory.getLogger(MODNAME)
    /**
     * Track mouse move
     */
    private var prevX = 0
    private var prevY = 0

    fun registerConfigScreen() {
        Platform.getMod(MODID).registerConfigurationScreen { parent ->
            ConfigHandler.createConfigScreen().setParentScreen(parent).build()
        }
    }

    fun onInitClient() {
        ConfigHandler.initialConfig()
        ClientGuiEvent.RENDER_POST.register(ClientGuiEvent.ScreenRenderPost { _, matrices, mouseX, mouseY, delta ->
            //Track mouse move here
            if (mouseX != prevX || mouseY != prevY) {
                ClientScreenEventHooks.SCREEN_MOUSE_MOVE.invoker().onMouseMove(prevX, prevY, mouseX, mouseY)

                prevX = mouseX
                prevY = mouseY
            }

            OverlayScreen.render(matrices, mouseX, mouseY, delta)
        })
        ClientScreenEventHooks.SCREEN_MOUSE_MOVE.register(ClientScreenEventHooks.MouseMove { _, _, _, _ ->
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
        ClientScreenEventHooks.WINDOW_SIZE_CHANGED.register(ClientScreenEventHooks.WindowSizeChanged { _, _ ->
            ExternalBaseIME.FullScreen = Minecraft.getInstance().window.isFullscreen
        })
        with(ScreenHandler.ScreenState) {
            ClientScreenEventHooks.SCREEN_CHANGED.register(ClientScreenEventHooks.ScreenChanged(ScreenHandler.ScreenState.Companion::onScreenChange))
        }
        with(ScreenHandler.ScreenState.EditState) {
            ClientScreenEventHooks.EDIT_OPEN.register(ClientScreenEventHooks.EditOpen(ScreenHandler.ScreenState.EditState.Companion::onEditOpen))
            ClientScreenEventHooks.EDIT_CARET.register(ClientScreenEventHooks.EditCaret(ScreenHandler.ScreenState.EditState.Companion::onEditCaret))
            ClientScreenEventHooks.EDIT_CLOSE.register(ClientScreenEventHooks.EditClose(ScreenHandler.ScreenState.EditState.Companion::onEditClose))
        }
    }
}