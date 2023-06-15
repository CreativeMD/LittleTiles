package team.creative.littletiles.client.rubidium;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import me.jellysquid.mods.sodium.client.model.quad.blender.ColorBlender;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;
import team.creative.littletiles.client.render.cache.build.RenderingBlockContext;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipeline;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.common.level.little.LittleSubLevel;
import team.creative.littletiles.mixin.rubidium.BlockRenderCacheAccessor;
import team.creative.littletiles.mixin.rubidium.BlockRenderContextAccessor;
import team.creative.littletiles.mixin.rubidium.ChunkBuilderAccessor;
import team.creative.littletiles.mixin.rubidium.RenderSectionManagerAccessor;

public class LittleRenderPipelineRubidium extends LittleRenderPipeline {
    
    public static RenderChunkExtender getChunk(BlockPos pos) {
        return (RenderChunkExtender) ((RenderSectionManagerAccessor) SodiumWorldRenderer.instance().getRenderSectionManager())
                .callGetRenderSection(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getY()), SectionPos.blockToSectionCoord(pos.getZ()));
    }
    
    private ChunkBuildBuffers buildBuffers;
    private BlockRenderer renderer;
    public BlockRenderContext context = new BlockRenderContext(null);
    
    @Override
    public void buildCache(PoseStack pose, ChunkLayerMap<BufferHolder> buffers, RenderingBlockContext data, VertexFormat format, SingletonList<BakedQuad> bakedQuadWrapper) {
        Level renderLevel = data.be.getLevel();
        while (renderLevel instanceof LittleSubLevel sub && !sub.shouldUseLightingForRenderig())
            renderLevel = sub.getParent();
        
        ((BlockRenderContextAccessor) context).setWorld(renderLevel);
        
        BlockPos pos = data.be.getBlockPos();
        
        // Render vertex buffer
        /* for (Tuple<RenderType, IndexedCollector<LittleRenderBox>> entry : data.be.render.boxCache.tuples()) {
            
            ChunkModelBuilder builder = buildBuffers.get(entry.getKey());
            ChunkVertexBufferBuilder vertexBuffer = builder.getVertexBuffer();
            
            IndexedCollector<LittleRenderBox> cubes = entry.value;
            if (cubes == null || cubes.isEmpty())
                continue;
            
            IntArrayList indexes = new IntArrayList();
            for (Iterator<LittleRenderBox> iterator = cubes.sectionIterator(x -> {
                indexes.add(x);
                indexes.add(((BufferBuilderAccessor) builder).getVertices() * format.getVertexSize());
            });iterator.hasNext();) {
                LittleRenderBox cube = iterator.next();
                BlockState state = cube.state;
                
                context.update(pos, data.chunk.standardOffset(), state, null, 0, ModelData.EMPTY, entry.key);
                
                if (OptifineHelper.isShaders()) {
                    if (state.getBlock() instanceof IFakeRenderingBlock)
                        state = ((IFakeRenderingBlock) state.getBlock()).getFakeState(state);
                    OptifineHelper.pushBuffer(state, pos, data.be.getLevel(), builder);
                }
                
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
                    if (quads != null && !quads.isEmpty()) {
                        Direction direction = facing.toVanilla();
                        IndexBufferBuilder indexBuffer = builder.getIndexBuffer(ModelQuadFacing.fromDirection(direction));
                        
                        for (BakedQuad quad : quads) {
                            
                            lighter.calculate((ModelQuadView) quad, pos, lightData, direction, quad.getDirection(), quad.isShade());
                            
                            int[] colors = null;
                            
                            if (cube.color != -1) {
                                Arrays.fill(colors, ColorARGB.toABGR(cube.color));
                            } else if (quad.isTinted()) {
                                if (colorizer == null)
                                    colorizer = blockColors.getColorProvider(state);
                                
                                colors = blenders.get().getColors(renderLevel, pos, (ModelQuadView) quad, colorizer, state);
                            }
                            
                            this.writeGeometry(ctx, vertexBuffer, indexBuffer, offset, quadView, colors, lightData.br, lightData.lm);
                            TextureAtlasSprite sprite = quad.getSprite();
                            if (sprite != null)
                                builder.addSprite(sprite);
                        }
                    }
                }
                
                bakedQuadWrapper.setElement(null);
                
                if (OptifineHelper.isShaders())
                    OptifineHelper.popBuffer(builder);
                
                if (!LittleTiles.CONFIG.rendering.useQuadCache)
                    cube.deleteQuadCache();
            }
            
            if (OptifineHelper.isShaders())
                OptifineHelper.calcNormalChunkLayer(builder);
            
            buffers.put(entry.key, new ByteBufferHolder(builder.end(), indexes.toIntArray()));
        }*/
        
    }
    
    @Override
    public void reload() {
        ChunkBuilderAccessor chunkBuilder = (ChunkBuilderAccessor) SodiumWorldRenderer.instance().getRenderSectionManager().getBuilder();
        
        if (buildBuffers == null)
            buildBuffers = new ChunkBuildBuffers(chunkBuilder.getVertexType(), chunkBuilder.getRenderPassManager());
        ColorBlender colorBlender = BlockRenderCacheAccessor.callCreateBiomeColorBlender();
        //this.lightDataCache = new ArrayLightDataCache(this.worldSlice);
        //LightPipelineProvider lightPipelineProvider = new LightPipelineProvider(this.lightDataCache);
        //renderer = new BlockRenderer(Minecraft.getInstance(), lightPipelineProvider, colorBlender);
    }
    
    @Override
    public void release() {
        buildBuffers.destroy();
    }
}
