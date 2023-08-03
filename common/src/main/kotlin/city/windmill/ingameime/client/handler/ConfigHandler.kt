package city.windmill.ingameime.client.handler

import city.windmill.ingameime.IngameIMEClient
import city.windmill.ingameime.client.jni.ExternalBaseIME
import city.windmill.ingameime.client.jni.ICommitListener
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import me.shedaniel.clothconfig2.api.ConfigBuilder
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.ChatScreen
import net.minecraft.client.resources.language.I18n
import net.minecraft.network.chat.Component
import org.apache.logging.log4j.LogManager
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*
import kotlin.io.path.outputStream
import kotlin.io.path.reader

object ConfigHandler {
    var disableIMEInCommandMode = false
        set(value) {
            if (field != value)
                if (value) {
                    //Disable -> Enable
                    ScreenHandler.ScreenState.EditState.apply {
                        iEditstateListener = IEditStateListener { state ->
                            if (state == ScreenHandler.ScreenState.EditState.EDIT_OPEN
                                && ScreenHandler.ScreenState.currentScreen is ChatScreen
                                && (ScreenHandler.ScreenState.currentScreen as ChatScreen).initial == "/"
                            ) {
                                //Disable IME in Command Mode
                                IMEHandler.IMEState.onEditState(ScreenHandler.ScreenState.EditState.NULL_EDIT)
                                return@IEditStateListener
                            }
                            IMEHandler.IMEState.onEditState(state)
                        }
                    }
                } else {
                    //Enable -> Disable
                    ScreenHandler.ScreenState.EditState.apply {
                        iEditstateListener = IMEHandler.IMEState
                    }
                }
            field = value
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var autoReplaceSlashChar = false
        set(value) {
            if (field != value)
                if (value) {
                    ExternalBaseIME.iCommitListener = ICommitListener { commit ->
                        var result = commit
                        if (ScreenHandler.ScreenState.currentScreen is ChatScreen
                            && ScreenHandler.ScreenState.EditState.currentEdit is EditBox
                            && (ScreenHandler.ScreenState.EditState.currentEdit as EditBox).cursorPosition == 0
                            && commit.isNotEmpty() && slashCharArray.contains(commit[0])
                        ) {
                            //Change to command mode, replace the char /
                            result = "/${commit.substring(1)}"
                            //Disable IME in command mode
                            if (disableIMEInCommandMode)
                                IMEHandler.IMEState.onEditState(ScreenHandler.ScreenState.EditState.NULL_EDIT)
                        }
                        return@ICommitListener IMEHandler.IMEState.onCommit(result)
                    }
                } else {
                    ExternalBaseIME.iCommitListener = IMEHandler.IMEState
                }
            field = value
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var slashCharArray = charArrayOf('、')

    private val config = Paths.get(
        Minecraft.getInstance().gameDirectory.toString(),
        "config", "ingameime.json"
    )
    private val LOGGER = LogManager.getFormatterLogger(IngameIMEClient.MODNAME + "|Config")!!

    fun initialConfig() {
        readConfig()
    }

    fun loadDefaultConfig() {
        disableIMEInCommandMode = true
        autoReplaceSlashChar = true
        slashCharArray = charArrayOf('、')
    }

    fun readConfig() {
        try {
            JsonParser().parse(JsonReader(config.reader())).apply {
                disableIMEInCommandMode = (this as JsonObject).get("disableIMEInCommandMode").asBoolean
                autoReplaceSlashChar = this.get("autoReplaceSlashChar").asBoolean
                slashCharArray = this.get("slashChars").asJsonArray.map { it.asCharacter }.toCharArray()
            }
        } catch (e: Exception) {
            LOGGER.warn("Failed to read config:", e)
            LOGGER.warn("Loading Default config")
            loadDefaultConfig()
        }
        saveConfig()
    }

    fun saveConfig() {
        config.outputStream(
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        ).bufferedWriter().apply {
            write(
                GsonBuilder().setPrettyPrinting().create().toJson(
                    JsonObject().apply {
                        addProperty("disableIMEInCommandMode", disableIMEInCommandMode)
                        addProperty("autoReplaceSlashChar", autoReplaceSlashChar)
                        add("slashChars", JsonArray().apply { slashCharArray.onEach(::add) })
                    }
                )
            )
            flush()
            close()
        }
    }

    fun createConfigScreen(): ConfigBuilder {
        return ConfigBuilder.create()
            .setTitle(Component.literal(I18n.get("config.ingameime.title")))
            .setSavingRunnable { saveConfig() }.apply {
                getOrCreateCategory(Component.literal(I18n.get("config.ingameime.category.chat"))).apply {
                    addEntry(
                        entryBuilder()
                            .startBooleanToggle(
                                Component.literal(I18n.get("desc.ingameime.disableIMEInCommandMode")),
                                disableIMEInCommandMode
                            )
                            .setDefaultValue(true)
                            .setTooltip(Component.literal(I18n.get("tooltip.ingameime.disableIMEInCommandMode")))
                            .setSaveConsumer { result -> disableIMEInCommandMode = result }
                            .build()
                    )
                    addEntry(
                        entryBuilder()
                            .startBooleanToggle(
                                Component.literal(I18n.get("desc.ingameime.autoReplaceSlashChar")),
                                autoReplaceSlashChar
                            )
                            .setDefaultValue(true)
                            .setTooltip(Component.literal(I18n.get("tooltip.ingameime.autoReplaceSlashChar")))
                            .setSaveConsumer { result -> autoReplaceSlashChar = result }
                            .build()
                    )
                    addEntry(
                        entryBuilder().startStrList(
                            Component.literal(I18n.get("desc.ingameime.slashChars")),
                            slashCharArray.map { it.toString() }
                        )
                            .setDefaultValue(mutableListOf("、"))
                            .setTooltip(Component.literal(I18n.get("tooltip.ingameime.slashChars")))
                            .setCellErrorSupplier { str ->
                                if (str.length > 1)
                                    return@setCellErrorSupplier Optional.of(Component.literal(I18n.get("desc.ingameime.error.slashChars")))
                                return@setCellErrorSupplier Optional.empty()
                            }
                            .setSaveConsumer { result ->
                                slashCharArray = result
                                    .filterNot { it.isBlank() }
                                    .map { it[0] }
                                    .toSet()
                                    .toCharArray()
                            }
                            .build()
                    )
                }
            }
    }
}