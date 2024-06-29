package team.creative.littletiles.mixin.embeddium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.jellysquid.mods.sodium.client.model.quad.BakedQuadView;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderCache;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import me.jellysquid.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderTask;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import me.jellysquid.mods.sodium.client.util.task.CancellationToken;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import me.jellysquid.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.creative.creativecore.client.render.box.QuadGeneratorContext;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.list.IndexedCollector;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.mod.embeddium.BlockRendererAccess;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.cache.build.RenderingBlockContext;
import team.creative.littletiles.client.render.mc.RebuildTaskExtender;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.block.entity.BETiles;

@Mixin(ChunkBuilderMeshingTask.class)
public abstract class ChunkBuilderMeshingTaskMixin extends ChunkBuilderTask implements RebuildTaskExtender {

    @Unique
    public ChunkLayerMap<BufferCollection> caches;
    @Unique
    public ChunkBuildContext buildContext;
    @Unique
    SingletonList<BakedQuad> bakedQuadWrapper = new SingletonList<>(null);
    @Unique
    BlockPos.MutableBlockPos modelOffset = new BlockPos.MutableBlockPos();
    @Unique
    BlockPos.MutableBlockPos modelOffset2 = new BlockPos.MutableBlockPos();
    @Unique
    RandomSource rand = RandomSource.create();
    @Shadow
    @Final
    private ChunkRenderContext renderContext;

    @Inject(at = @At("HEAD"), remap = false, require = 1,
            method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;")
    public void performBuildStart(ChunkBuildContext buildContext, CancellationToken cancellationSource, CallbackInfoReturnable<ChunkBuildOutput> info) {
        this.buildContext = buildContext;
    }

    @WrapOperation(
            method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
            remap = false, require = 1, at = @At(value = "INVOKE",
            target = "Lme/jellysquid/mods/sodium/client/world/WorldSlice;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;",
            remap = true))
    public BlockEntity getBlockEntity(WorldSlice instance, BlockPos pos, Operation<BlockEntity> original, @Local WorldSlice slice, @Local BlockState blockState, @Local BlockRenderCache cache) {
        BlockEntity entity = original.call(instance, pos);

        if (modelOffset == null) modelOffset = new BlockPos.MutableBlockPos();

        QuadGeneratorContext context = new QuadGeneratorContext();
        if (entity instanceof BETiles be) {
            // Time for cursed
            modelOffset.set(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
            int[] colors = new int[4];

            RenderingBlockContext data = new RenderingBlockContext(be, (RenderChunkExtender) slice);
            data.beforeBuilding();

            BlockRenderer blockRenderer = cache.getBlockRenderer();

            for (RenderType layer : RenderType.chunkBufferLayers()) {
                IndexedCollector<LittleRenderBox> cubes = be.render.getRenderingBoxes(data, layer);

                if (cubes == null)
                    continue;

                for (LittleRenderBox cube : cubes) {
                    BlockState modelState = cube.state;
                    rand.setSeed(modelState.getSeed(pos));

                    BakedModel blockModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(modelState);

                    for (int h = 0; h < Facing.VALUES.length; h++) {
                        Facing facing = Facing.VALUES[h];
                        if (cube.shouldRenderFace(facing)) {
                            cube.getBakedQuad(context, Minecraft.getInstance().level, pos, cube.getOffset(), modelState, blockModel, facing, layer, rand, true,
                                    ColorUtils.WHITE).forEach(quad -> {

                                ((BlockRendererAccess) blockRenderer).drawQuad(buildContext.buffers.get(DefaultMaterials.forRenderLayer(layer)), facing.toVanilla(), slice, (BakedQuadView) quad, modelState, layer,
                                        pos, modelOffset, cube.color);

                            });
                        }
                    }
                }
            }

            context.clear();
            data.clearQuadBuilding();

            /*
            be.render.boxCache.tuples().forEach(boxc -> {
                boxc.value.forEach(cube -> {
                    QuadEmitter emitter = blockRenderer.getEmitter();
                    for (int h = 0; h < Facing.VALUES.length; h++) {
                        Facing facing = Facing.VALUES[h];
                        Object quadObject = cube.getQuad(facing);
                        List<BakedQuad> quads = null;
                        if (quadObject instanceof List) {
                            quads = (List<BakedQuad>) quadObject;
                        } else if (quadObject instanceof BakedQuad quad) {
                            bakedQuadWrapper.setElement(quad);
                            quads = bakedQuadWrapper;
                        }
                        System.out.println("We are doing *something*");
                        ((BlockRendererAccess) blockRenderer).setupState(pos, modelOffset, blockState, slice, boxc.key);
                        if (quads != null && !quads.isEmpty()) {
                            Direction direction = facing.toVanilla();

                            for (BakedQuad quad : quads) {
                                emitter.fromVanilla(quad, boxc.key == RenderType.translucent() ? TRANSLUCENT : STANDARD, direction);
                                emitter.emit();
                            }
                        }
                    }

                    bakedQuadWrapper.setElement(null);
                });
            });
        */
        }
        return entity;

    }
}
