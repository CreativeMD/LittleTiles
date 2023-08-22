package team.creative.littletiles.mixin.rubidium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.data.BuiltSectionMeshParts;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;

@Mixin(targets = "me/jellysquid/mods/sodium/client/render/chunk/region/RenderRegionManager$PendingSectionUpload")
public interface PendingSectionUploadAccessor {
    
    @Accessor(remap = false)
    public RenderSection getSection();
    
    @Accessor(remap = false)
    public BuiltSectionMeshParts getMeshData();
    
    @Accessor(remap = false)
    public TerrainRenderPass getPass();
    
}
