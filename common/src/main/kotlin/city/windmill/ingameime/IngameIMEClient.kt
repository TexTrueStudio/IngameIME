package city.windmill.ingameime

import city.windmill.ingameime.client.handler.ConfigHandler
import city.windmill.ingameime.client.handler.IMEHandler
import city.windmill.ingameime.client.handler.KeyHandler
import city.windmill.ingameime.client.handler.ScreenHandler
import city.windmill.ingameime.client.event.ClientScreenEventHooks
import city.windmill.ingameime.client.gui.OverlayScreen
import city.windmill.ingameime.client.jni.ExternalBaseIME
import me.shedaniel.architectury.event.events.GuiEvent
import me.shedaniel.architectury.event.events.client.ClientScreenInputEvent
import me.shedaniel.architectury.platform.Platform
import net.minecraft.client.Minecraft
import net.minecraft.world.InteractionResult
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object IngameIMEClient {
    const val MODNAME = "ContingameIME"
    const val MODID = "ingameime"
    val LOGGER: Logger = LogManager.getLogger(MODNAME)
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
        GuiEvent.RENDER_POST.register(GuiEvent.ScreenRenderPost { _, matrices, mouseX, mouseY, delta ->
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