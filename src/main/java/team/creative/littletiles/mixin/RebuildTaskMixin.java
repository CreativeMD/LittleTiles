package team.creative.littletiles.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.creative.littletiles.client.render.level.LittleChunkDispatcher;
import team.creative.littletiles.client.render.mc.RenderChunkLittle;
import team.creative.littletiles.common.block.entity.BETiles;

@Mixin(targets = "net/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk$RebuildTask")
public abstract class RebuildTaskMixin {
    
    @Unique
    public Set<RenderType> renderTypes;
    
    @Unique
    public ChunkBufferBuilderPack pack;
    
    public RenderChunk this$1;
    
    @Inject(at = @At("HEAD"),
            method = "compile(FFFLnet/minecraft/client/renderer/ChunkBufferBuilderPack;)Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk$RebuildTask$CompileResults;",
            require = 1)
    private void compileStart(float f1, float f2, float f3, ChunkBufferBuilderPack pack, CallbackInfoReturnable info) {
        this.pack = pack;
        LittleChunkDispatcher.startCompile(this$1);
    }
    
    @Inject(at = @At("TAIL"),
            method = "compile(FFFLnet/minecraft/client/renderer/ChunkBufferBuilderPack;)Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk$RebuildTask$CompileResults;",
            require = 1)
    private void compile(CallbackInfoReturnable info) {
        ((RenderChunkLittle) this$1).dynamicLightUpdate(false);
    }
    
    @Redirect(at = @At(value = "NEW", target = "(I)Lit/unimi/dsi/fastutil/objects/ReferenceArraySet;", remap = false),
            method = "compile(FFFLnet/minecraft/client/renderer/ChunkBufferBuilderPack;)Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk$RebuildTask$CompileResults;",
            require = 1)
    private ReferenceArraySet afterSetCreated(int capacity) {
        renderTypes = new ReferenceArraySet(capacity);
        return (ReferenceArraySet) renderTypes;
    }
    
    @Inject(at = @At("HEAD"),
            method = "handleBlockEntity(Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk$RebuildTask$CompileResults;Lnet/minecraft/world/level/block/entity/BlockEntity;)V",
            require = 1)
    private void handleBlockEntity(@Coerce Object object, BlockEntity block, CallbackInfo info) {
        if (block instanceof BETiles tiles)
            LittleChunkDispatcher.add(pack, this$1, tiles, renderTypes);
    }
    
}
