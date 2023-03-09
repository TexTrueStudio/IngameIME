package city.windmill.ingameime.forge.mixin;

import city.windmill.ingameime.forge.IngameIMEForge;
import city.windmill.ingameime.forge.ScreenEvents;
import com.mojang.blaze3d.vertex.PoseStack;
import kotlin.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.lang.reflect.Field;

@Mixin(BookEditScreen.class)
abstract class MixinBookEditScreen {
    @Inject(method = "convertLocalToScreen",
            at = @At("TAIL"))
    private void onCaret_Book(Object pos2i, CallbackInfoReturnable<Object> cir) {
        try {
            Field pos2ix = pos2i.getClass().getDeclaredField("x");
            Field pos2iy = pos2i.getClass().getDeclaredField("y");
            pos2ix.setAccessible(true);
            pos2iy.setAccessible(true);
            IngameIMEForge.INSTANCE.getINGAMEIME_BUS().post(new ScreenEvents.EditCaret(this, new Pair<>((Integer) pos2ix.get(cir.getReturnValue()), (Integer) pos2iy.get(cir.getReturnValue()))));
        } catch (Exception ignored) {

        }
    }

    @Inject(method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font;draw(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/util/FormattedCharSequence;FFI)I"),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onCaret_Book(PoseStack poseStack, int i, int j, float f, CallbackInfo ci,
                              int k, FormattedCharSequence formattedCharSequence, int m, int n) {
        IngameIMEForge.INSTANCE.getINGAMEIME_BUS().post(new ScreenEvents.EditCaret(this, new Pair<>(
                k + 36 + (114 + n) / 2
                        - Minecraft.getInstance().font.width("_"),
                50
        )));
    }
}
