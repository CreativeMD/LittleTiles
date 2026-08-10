package team.creative.littletiles.mixin.client.mod.sable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import team.creative.littletiles.client.mod.sable.render.SableTileMesh;
import team.creative.littletiles.client.mod.sable.render.SableTileSection;

@Mixin(SectionRenderDispatcher.RenderSection.class)
public class SableRenderSectionMixin implements SableTileSection {
    
    @Unique
    private volatile SableTileMesh.UploadedMesh tileMesh;
    
    @Override
    public SableTileMesh.UploadedMesh getTileMesh() {
        return tileMesh;
    }
    
    @Override
    public void publishTiles(SableTileMesh.UploadedMesh mesh) {
        SableTileMesh.UploadedMesh old = tileMesh;
        tileMesh = mesh;
        if (old != null)
            old.close();
    }
    
    @Inject(method = "setCompiled", at = @At("TAIL"))
    private void publishTileMesh(SectionRenderDispatcher.CompiledSection compiled, CallbackInfo callback) {
        SableTileMesh.publish((SectionRenderDispatcher.RenderSection) (Object) this);
    }
    
    @Inject(method = "releaseBuffers", at = @At("HEAD"))
    private void releaseTileMesh(CallbackInfo callback) {
        SableTileMesh.discard((SectionRenderDispatcher.RenderSection) (Object) this);
        SableTileMesh.UploadedMesh old = tileMesh;
        tileMesh = null;
        SableTileMesh.closeOnRenderThread(old);
    }
    
}
