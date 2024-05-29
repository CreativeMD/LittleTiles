package team.creative.littletiles.mixin.sodium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderTask;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.util.task.CancellationToken;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.caffeinemc.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3dc;
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
import team.creative.littletiles.client.mod.sodium.BlockRenderingStateAccess;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.cache.build.RenderingBlockContext;
import team.creative.littletiles.client.render.mc.RebuildTaskExtender;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.block.entity.BETiles;

@Mixin(ChunkBuilderMeshingTask.class)
public abstract class ChunkBuilderMeshingTaskMixin extends ChunkBuilderTask implements RebuildTaskExtender {

    private static final RenderMaterial STANDARD = RendererAccess.INSTANCE.getRenderer().materialFinder().find();
    private static final RenderMaterial TRANSLUCENT = RendererAccess.INSTANCE.getRenderer().materialFinder().blendMode(BlendMode.TRANSLUCENT).find();
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

    public ChunkBuilderMeshingTaskMixin(RenderSection render, int time, Vector3dc absoluteCameraPos) {
        super(render, time, absoluteCameraPos);
    }

    @Inject(at = @At("HEAD"), remap = false, require = 1,
            method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;")
    public void performBuildStart(ChunkBuildContext buildContext, CancellationToken cancellationSource, CallbackInfoReturnable<ChunkBuildOutput> info) {
        this.buildContext = buildContext;
    }

    @WrapOperation(
            method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
            remap = false, require = 1, at = @At(value = "INVOKE",
            target = "Lnet/caffeinemc/mods/sodium/client/world/LevelSlice;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;",
            remap = true))
    public BlockEntity getBlockEntity(LevelSlice instance, BlockPos pos, Operation<BlockEntity> original, @Local LevelSlice slice, @Local BlockState blockState, @Local TranslucentGeometryCollector collector, @Local BlockRenderer blockRenderer) {
        BlockEntity entity = original.call(instance, pos);
        ((BlockRenderingStateAccess) blockRenderer).setCustomTint(0xFFFFFFFF);

        QuadGeneratorContext context = new QuadGeneratorContext();
        if (entity instanceof BETiles be) {
            // Time for cursed
            modelOffset.set(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);

            RenderingBlockContext data = new RenderingBlockContext(be, (RenderChunkExtender) slice);
            data.beforeBuilding();

            for (RenderType layer : RenderType.chunkBufferLayers()) {
                IndexedCollector<LittleRenderBox> cubes = be.render.getRenderingBoxes(data, layer);

                if (cubes == null)
                    continue;

                for (LittleRenderBox cube : cubes) {
                    QuadEmitter emitter = blockRenderer.getEmitter();

                    BlockState modelState = cube.state;
                    rand.setSeed(modelState.getSeed(pos));
                    ((BlockRenderingStateAccess) blockRenderer).setCustomTint(cube.color);

                    BakedModel blockModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(modelState);
                    ((BlockRenderingStateAccess) blockRenderer).setupState(pos, modelOffset, modelState, slice, layer);

                    for (int h = 0; h < Facing.VALUES.length; h++) {
                        Facing facing = Facing.VALUES[h];
                        if (cube.shouldRenderFace(facing)) {
                            cube.getBakedQuad(context, Minecraft.getInstance().level, pos, cube.getOffset(), modelState, blockModel, facing, layer, rand, true,
                                    ColorUtils.WHITE).forEach(quad -> {
                                emitter.fromVanilla(quad, layer == RenderType.translucent() ? TRANSLUCENT : STANDARD, facing.toVanilla());
                                emitter.emit();
                            });
                        }
                    }
                }
            }
            ((BlockRenderingStateAccess) blockRenderer).setCustomTint(0xFFFFFFFF);

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
                        ((BlockRenderingStateAccess) blockRenderer).setupState(pos, modelOffset, blockState, slice, boxc.key);
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
