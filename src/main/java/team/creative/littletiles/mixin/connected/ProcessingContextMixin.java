package team.creative.littletiles.mixin.connected;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import team.creative.littletiles.client.mod.continuity.CtmQuadCollector;

import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

/** Retains outer transforms when Continuity outputs compact-CTM fragments into our collector. */
@Mixin(targets = "me.pepperbell.continuity.impl.client.ProcessingContextImpl", remap = false)
public abstract class ProcessingContextMixin {
    @Redirect(method = "outputTo", require = 2, at = @At(value = "INVOKE",
            target = "Lnet/fabricmc/fabric/api/renderer/v1/mesh/Mesh;outputTo(Lnet/fabricmc/fabric/api/renderer/v1/mesh/QuadEmitter;)V"))
    private void yuushyaLtCtmCompat$outputMesh(Mesh mesh, QuadEmitter emitter) {
        CtmQuadCollector.outputMesh(mesh, emitter);
    }
}
