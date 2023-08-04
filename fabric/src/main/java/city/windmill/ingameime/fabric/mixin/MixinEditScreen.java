package city.windmill.ingameime.fabric.mixin;

import city.windmill.ingameime.client.event.ClientScreenEventHooks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import kotlin.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.Material;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.lang.reflect.Field;

@Mixin({Screen.class, AbstractSignEditScreen.class})
class MixinScreen {
    @Inject(method = "removed", at = @At("TAIL"))
    private void onRemove(CallbackInfo info) {
        ClientScreenEventHooks.INSTANCE.getEDIT_CLOSE().invoker().onEditClose(this);
    }
}

@Mixin({BookEditScreen.class, SignEditScreen.class})
class MixinEditScreen {
    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        ClientScreenEventHooks.INSTANCE.getEDIT_OPEN().invoker().onEditOpen(this, new Pair<>(0, 0));
    }
}

@Mixin(BookEditScreen.class)
abstract class MixinBookEditScreen {
    @Inject(method = "renderCursor",
            at = @At(value = "INVOKE",
                    shift = At.Shift.BY,
                    by = 2,
                    target = "Lnet/minecraft/client/gui/screens/inventory/BookEditScreen;convertLocalToScreen(Lnet/minecraft/client/gui/screens/inventory/BookEditScreen$Pos2i;)Lnet/minecraft/client/gui/screens/inventory/BookEditScreen$Pos2i;")
    )
    private void onCaret_Book(PoseStack poseStack, BookEditScreen.Pos2i pos2i, boolean bl, CallbackInfo ci) {
        ClientScreenEventHooks.INSTANCE.getEDIT_CARET().invoker().onEditCaret(this, new Pair<>(pos2i.x, pos2i.y));
    }

    @Inject(method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font;draw(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/util/FormattedCharSequence;FFI)I"),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onCaret_Book(PoseStack poseStack, int i, int j, float f, CallbackInfo ci, int k, FormattedCharSequence formattedCharSequence, int m, int n) {
        ClientScreenEventHooks.INSTANCE.getEDIT_CARET().invoker().onEditCaret(this, new Pair<>(
                k + 36 + (114 + n) / 2
                        - Minecraft.getInstance().font.width("_"),
                50
        ));
    }
}

@Mixin(AbstractSignEditScreen.class)
abstract class MixinSignEditScreen extends Screen {
    private MixinSignEditScreen(Component component) {
        super(component);
    }

    @Inject(method = "renderSignText",
            at = {
                    @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/Font;drawInBatch(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;IIZ)I",
                            ordinal = 1),
                    @At(value = "INVOKE",
                            target = "net/minecraft/client/gui/screens/inventory/AbstractSignEditScreen.fill(Lcom/mojang/blaze3d/vertex/PoseStack;IIIII)V",
                            ordinal = 0)},
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onCaret_Sign(PoseStack poseStack, int i, int j, float f, CallbackInfo ci, float g, BlockState blockState, boolean bl, boolean bl2, float h, MultiBufferSource.BufferSource bufferSource, Material material, VertexConsumer vertexConsumer, float k, int l, int m, int n, int o, Matrix4f matrix4f, int p, String string, int r, int s) {
        try {
            Field m03 = matrix4f.getClass().getDeclaredField("m03");
            Field m13 = matrix4f.getClass().getDeclaredField("m13");
            m03.setAccessible(true);
            m13.setAccessible(true);
            //s(23)->x,o(17)->y
            ClientScreenEventHooks.INSTANCE.getEDIT_CARET().invoker().onEditCaret(this, new Pair<>((int) m03.get(matrix4f) + s, (int) m13.get(matrix4f) + o));
        } catch (Exception ignored) {

        }
    }
}
