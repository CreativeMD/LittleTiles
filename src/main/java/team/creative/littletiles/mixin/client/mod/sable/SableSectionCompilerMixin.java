package team.creative.littletiles.mixin.client.mod.sable;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexSorting;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.SectionPos;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent.AdditionalSectionRenderer;
import team.creative.littletiles.client.mod.sable.render.SableTileMesh;

/** Integrates LittleTiles' independent Sable mesh with the vanilla section compiler. */
@Mixin(value = SectionCompiler.class, priority = 2000)
public abstract class SableSectionCompilerMixin {
    
    private static final String COMPILE = "compile(Lnet/minecraft/core/SectionPos;"
            + "Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;"
            + "Lcom/mojang/blaze3d/vertex/VertexSorting;"
            + "Lnet/minecraft/client/renderer/SectionBufferBuilderPack;"
            + "Ljava/util/List;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;";
    
    @Inject(method = COMPILE, at = @At("HEAD"))
    private void beginTileMesh(SectionPos section, RenderChunkRegion region, VertexSorting sorting,
            SectionBufferBuilderPack pack, List<AdditionalSectionRenderer> additionalRenderers,
            CallbackInfoReturnable<SectionCompiler.Results> callback) {
        SableTileMesh.begin(section, sorting);
    }
    
    @Inject(method = "getOrBeginLayer", at = @At("HEAD"), cancellable = true)
    private void useTileBuilder(Map<RenderType, BufferBuilder> builders, SectionBufferBuilderPack pack,
            RenderType layer, CallbackInfoReturnable<BufferBuilder> callback) {
        BufferBuilder builder = SableTileMesh.builder(layer);
        if (builder != null)
            callback.setReturnValue(builder);
    }
    
    @Inject(method = COMPILE, at = @At("RETURN"))
    private void finishTileMesh(SectionPos section, RenderChunkRegion region, VertexSorting sorting,
            SectionBufferBuilderPack pack, List<AdditionalSectionRenderer> additionalRenderers,
            CallbackInfoReturnable<SectionCompiler.Results> callback) {
        SableTileMesh.MeshPayload payload = SableTileMesh.finish();
        if (payload == null)
            return;
        
        SectionCompiler.Results results = callback.getReturnValue();
        if (results == null) {
            payload.close();
            return;
        }
        SableTileMesh.stage(section, payload, Set.copyOf(results.renderedLayers.keySet()));
    }
    
    @Shadow
    public abstract BufferBuilder getOrBeginLayer(Map<RenderType, BufferBuilder> builders,
            SectionBufferBuilderPack pack, RenderType layer);
    
}
