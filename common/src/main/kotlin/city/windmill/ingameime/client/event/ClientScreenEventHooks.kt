package city.windmill.ingameime.client.event

import dev.architectury.event.EventFactory
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.screens.Screen

@Environment(EnvType.CLIENT)
object ClientScreenEventHooks {
    var SCREEN_MOUSE_MOVE = EventFactory.createEventResult<MouseMove>()

    val WINDOW_SIZE_CHANGED = EventFactory.createEventResult<WindowSizeChanged>()

    val SCREEN_CHANGED = EventFactory.createEventResult<ScreenChanged>()

    val EDIT_OPEN = EventFactory.createEventResult<EditOpen>()

    val EDIT_CARET = EventFactory.createEventResult<EditCaret>()

    val EDIT_CLOSE = EventFactory.createEventResult<EditClose>()

    fun interface MouseMove {
        fun onMouseMove(prevX: Int, prevY: Int, curX: Int, curY: Int)
    }

    fun interface WindowSizeChanged {
        fun onWindowSizeChanged(sizeX: Int, sizeY: Int)
    }

    fun interface ScreenChanged {
        fun onScreenChanged(oldScreen: Screen?, newScreen: Screen?)
    }

    fun interface EditOpen {
        fun onEditOpen(edit: Any, caretPos: Pair<Int, Int>)
    }

    fun interface EditCaret {
        fun onEditCaret(edit: Any, caretPos: Pair<Int, Int>)
    }

    fun interface EditClose {
        fun onEditClose(edit: Any)
    }
}