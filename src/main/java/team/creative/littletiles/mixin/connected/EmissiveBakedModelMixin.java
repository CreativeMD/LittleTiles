package team.creative.littletiles.mixin.connected;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import team.creative.littletiles.client.mod.continuity.CtmQuadCollector;

import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

/** The outer emissive wrapper also sends an extra native mesh to our collection context. */
@Mixin(targets = "me.pepperbell.continuity.client.model.EmissiveBakedModel", remap = false)
public abstract class EmissiveBakedModelMixin {
    @Redirect(method = "emitBlockQuads", require = 1, at = @At(value = "INVOKE",
            target = "Lnet/fabricmc/fabric/api/renderer/v1/mesh/Mesh;outputTo(Lnet/fabricmc/fabric/api/renderer/v1/mesh/QuadEmitter;)V"))
    private void yuushyaLtCtmCompat$outputEmissiveMesh(Mesh mesh, QuadEmitter emitter) {
        CtmQuadCollector.outputMesh(mesh, emitter);
    }
}
