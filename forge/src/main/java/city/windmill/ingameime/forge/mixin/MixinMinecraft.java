package city.windmill.ingameime.forge.mixin;

import city.windmill.ingameime.forge.ScreenEvents;
import city.windmill.ingameime.forge.IngameIMEForge;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
class MixinMinecraft {
    @Shadow
    public Screen screen;
    @Final
    @Shadow
    private Window window;

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void onScreenChange(Screen screenIn, CallbackInfo info) {
        ScreenEvents.INSTANCE.getSCREEN_CHANGED().invoker().onScreenChanged(screen, screenIn);
    }

    @Inject(method = "resizeDisplay", at = @At("RETURN"))
    private void onScreenSizeChanged(CallbackInfo info) {
        ScreenEvents.INSTANCE.getWINDOW_SIZE_CHANGED().invoker().onWindowSizeChanged(window.getWidth(), window.getHeight());
    }
}
