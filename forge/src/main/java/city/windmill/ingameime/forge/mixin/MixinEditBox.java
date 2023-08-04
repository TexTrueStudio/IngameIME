package city.windmill.ingameime.forge.mixin;

import city.windmill.ingameime.client.event.ClientScreenEventHooks;
import com.mojang.blaze3d.vertex.PoseStack;
import kotlin.Pair;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EditBox.class)
abstract class MixinEditBox extends AbstractWidget {
    @Shadow
    private boolean bordered;

    private MixinEditBox(int i, int j, int k, int l, Component component) {
        super(i, j, k, l, component);
    }

    @Inject(method = "setFocused", at = @At("HEAD"))
    private void onSelected(boolean selected, CallbackInfo info) {
        int x = this.getX();
        int y = this.getY();
        int caretX = bordered ? x + 4 : x;
        int caretY = bordered ? y + (height - 8) / 2 : y;
        if (selected)
            ClientScreenEventHooks.INSTANCE.getEDIT_OPEN().invoker().onEditOpen(this, new Pair<>(caretX, caretY));
            //IngameIMEForge.INSTANCE.getINGAMEIME_BUS().post(new LegacyScreenEvents.EditOpen(this, new Pair<>(caretX, caretY)));
        else
            ClientScreenEventHooks.INSTANCE.getEDIT_CLOSE().invoker().onEditClose(this);
            //IngameIMEForge.INSTANCE.getINGAMEIME_BUS().post(new LegacyScreenEvents.EditClose(this));
    }

    @Inject(method = "mouseClicked", at = @At(value = "INVOKE",
            target = "net/minecraft/util/Mth.floor(D)I",
            shift = At.Shift.BEFORE,
            ordinal = 0))
    private void onFocused(double double_1, double double_2, int int_1, CallbackInfoReturnable<Boolean> cir) {
        int x = this.getX();
        int y = this.getY();
        int caretX = bordered ? x + 4 : x;
        int caretY = bordered ? y + (height - 8) / 2 : y;
        ClientScreenEventHooks.INSTANCE.getEDIT_OPEN().invoker().onEditOpen(this, new Pair<>(caretX, caretY));
        //IngameIMEForge.INSTANCE.getINGAMEIME_BUS().post(new LegacyScreenEvents.EditOpen(this, new Pair<>(caretX, caretY)));
    }

    @Inject(method = "renderWidget",
            at = @At(value = "INVOKE", target = "java/lang/String.isEmpty()Z", ordinal = 1),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onCaret(PoseStack poseStack, int arg1, int arg2, float arg3, CallbackInfo ci, int l, int m, int n, String string, boolean bl, boolean bl2, int o, int p, int q, boolean bl3, int r) {
        ClientScreenEventHooks.INSTANCE.getEDIT_CARET().invoker().onEditCaret(this, new Pair<>(r, p));
        //IngameIMEForge.INSTANCE.getINGAMEIME_BUS().post(new LegacyScreenEvents.EditCaret(this, new Pair<>(r, p)));
    }
}
