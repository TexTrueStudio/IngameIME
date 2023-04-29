package city.windmill.ingameime.forge.register

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory
import net.minecraftforge.fml.ModContainer
import net.minecraftforge.fml.ModList
import java.util.concurrent.ConcurrentHashMap

class ForgeConfigScreenRegister {
    fun getMod(id: String): ModConfigScreenRegister {
        return mods.computeIfAbsent(id) { id: String? -> ModConfigScreenRegisterImpl(id) }
    }

    interface ModConfigScreenRegister {
        @OnlyIn(Dist.CLIENT)
        fun registerModConfigScreen(configScreenProvider: ModConfigScreenProvider)

        @OnlyIn(Dist.CLIENT)
        fun interface ModConfigScreenProvider {
            fun provide(parent: Screen?): Screen?
        }
    }

    internal class ModConfigScreenRegisterImpl(id: String?) : ModConfigScreenRegister {
        private val container: ModContainer

        init {
            container = ModList.get().getModContainerById(id).orElseThrow()
        }

        override fun registerModConfigScreen(configScreenProvider: ModConfigScreenRegister.ModConfigScreenProvider) {
            container.registerExtensionPoint(ConfigScreenFactory::class.java) {
                ConfigScreenFactory { minecraft: Minecraft?, screen: Screen? ->
                    configScreenProvider.provide(screen)
                }
            }
        }
    }

    companion object {
        private val forgeConfigScreenRegister = ThreadLocal.withInitial { ForgeConfigScreenRegister() }
        private val mods: MutableMap<String, ModConfigScreenRegister> = ConcurrentHashMap()
        val instance: ForgeConfigScreenRegister
            get() = forgeConfigScreenRegister.get()
    }
}