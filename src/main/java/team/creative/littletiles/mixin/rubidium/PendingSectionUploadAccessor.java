package team.creative.littletiles.mixin.rubidium;

import org.embeddedt.embeddium.impl.render.chunk.data.BuiltSectionMeshParts;
import org.embeddedt.embeddium.impl.render.chunk.terrain.TerrainRenderPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "me/jellysquid/mods/sodium/client/render/chunk/region/RenderRegionManager$PendingSectionUpload")
public interface PendingSectionUploadAccessor {
    
    @Accessor(remap = false)
    public RenderSection getSection();
    
    @Accessor(remap = false)
    public BuiltSectionMeshParts getMeshData();
    
    @Accessor(remap = false)
    public TerrainRenderPass getPass();
    
}
