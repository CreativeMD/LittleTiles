package team.creative.littletiles.mixin.client.mod.sable;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.VertexBuffer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import team.creative.littletiles.client.mod.sable.render.SableTileSection;
import team.creative.littletiles.client.render.mc.VertexBufferExtender;

/** Draws the separate tile VBO immediately after Sable's vanilla VBO for the same layer. */
@Pseudo
@Mixin(targets = "dev.ryanhcode.sable.sublevel.render.vanilla.VanillaChunkedSubLevelRenderData", remap = false)
public class SableChunkedSubLevelRenderMixin {
    
    private static final ThreadLocal<VertexBuffer> PRIMARY = new ThreadLocal<>();
    private static final ThreadLocal<VertexBuffer> SECONDARY = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> SKIP_DRAW = new ThreadLocal<>();
    
    @Redirect(method = "renderChunkedSubLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection;"
            + "getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexBuffer;"))
    private VertexBuffer selectBuffer(SectionRenderDispatcher.RenderSection section, RenderType layer) {
        VertexBuffer vanilla = section.getBuffer(layer);
        var tiles = ((SableTileSection) section).getTileMesh();
        VertexBuffer tileBuffer = tiles == null ? null : tiles.get(layer);
        if (tileBuffer == null) {
            clear();
            return vanilla;
        }
        
        // A section can become visible between Sable marking its layer as present and the
        // render-thread upload of the dedicated tile VBO. VertexBuffer.draw() assumes that
        // upload has initialized its draw mode, so do not hand Sable a partially ready VBO.
        if (!isReady(tileBuffer)) {
            if (!tiles.hasVanillaLayer(layer))
                SKIP_DRAW.set(Boolean.TRUE);
            else
                clear();
            return vanilla;
        }
        if (!tiles.hasVanillaLayer(layer)) {
            PRIMARY.set(tileBuffer);
            SECONDARY.remove();
            return tileBuffer;
        }
        
        PRIMARY.set(vanilla);
        SECONDARY.set(tileBuffer);
        return vanilla;
    }
    
    @Inject(method = "renderChunkedSubLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;bind()V", shift = At.Shift.AFTER), remap = false)
    private void drawTileBuffer(RenderType layer, ShaderInstance shader, Matrix4f modelView,
            double camX, double camY, double camZ, CallbackInfo callback) {
        VertexBuffer primary = PRIMARY.get();
        VertexBuffer secondary = SECONDARY.get();
        if (Boolean.TRUE.equals(SKIP_DRAW.get()))
            return;
        if (primary == null)
            return;
        clear();
        if (secondary == null)
            return;
        secondary.bind();
        secondary.draw();
        primary.bind();
    }
    
    @Redirect(method = "renderChunkedSubLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;draw()V"))
    private void drawSelectedBuffer(VertexBuffer buffer) {
        if (Boolean.TRUE.equals(SKIP_DRAW.get())) {
            clear();
            return;
        }
        buffer.draw();
    }
    
    @Inject(method = "renderChunkedSubLevel", at = @At("RETURN"))
    private void clearBuffers(RenderType layer, ShaderInstance shader, Matrix4f modelView,
            double camX, double camY, double camZ, CallbackInfo callback) {
        clear();
    }
    
    private static void clear() {
        PRIMARY.remove();
        SECONDARY.remove();
        SKIP_DRAW.remove();
    }
    
    private static boolean isReady(VertexBuffer buffer) {
        if (buffer == null || buffer.isInvalid())
            return false;
        return !(buffer instanceof VertexBufferExtender extender) || extender.getMode() != null;
    }
    
}
