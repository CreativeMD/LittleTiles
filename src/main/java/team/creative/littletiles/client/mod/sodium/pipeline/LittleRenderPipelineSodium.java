package team.creative.littletiles.client.mod.sodium.pipeline;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.api.util.ColorMixer;
import net.caffeinemc.mods.sodium.client.model.color.ColorProvider;
import net.caffeinemc.mods.sodium.client.model.color.ColorProviderRegistry;
import net.caffeinemc.mods.sodium.client.model.light.LightMode;
import net.caffeinemc.mods.sodium.client.model.light.LightPipeline;
import net.caffeinemc.mods.sodium.client.model.light.LightPipelineProvider;
import net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.caffeinemc.mods.sodium.client.render.frapi.SodiumRenderer;
import net.caffeinemc.mods.sodium.client.render.frapi.helper.ColorHelper;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.render.frapi.render.AmbientOcclusionMode;
import net.caffeinemc.mods.sodium.client.render.texture.SpriteUtil;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.material.ShadeMode;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.list.IndexedCollector;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.mod.sodium.SodiumInteractor;
import team.creative.littletiles.client.mod.sodium.buffer.SodiumBufferCache;
import team.creative.littletiles.client.mod.sodium.level.LittleLevelSliceExtender;
import team.creative.littletiles.client.mod.sodium.renderer.BlockRendererExtender;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;
import team.creative.littletiles.client.render.cache.build.RenderingBlockContext;
import team.creative.littletiles.client.render.cache.build.RenderingThread;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipeline;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.level.little.LittleSubLevel;
import team.creative.littletiles.mixin.sodium.BlockRenderContextAccessor;
import team.creative.littletiles.mixin.sodium.ChunkBuildBuffersAccessor;
import team.creative.littletiles.mixin.sodium.ChunkBuilderAccessor;
import team.creative.littletiles.mixin.sodium.ChunkMeshBufferBuilderAccessor;
import team.creative.littletiles.mixin.sodium.RenderSectionManagerAccessor;
import team.creative.littletiles.mixin.sodium.SodiumWorldRendererAccessor;

public class LittleRenderPipelineSodium extends LittleRenderPipeline {
    
    public static RenderChunkExtender getChunk(long pos) {
        return (RenderChunkExtender) ((RenderSectionManagerAccessor) ((SodiumWorldRendererAccessor) SodiumWorldRenderer.instance()).getRenderSectionManager()).callGetRenderSection(
            SectionPos.x(pos), SectionPos.y(pos), SectionPos.z(pos));
    }
    
    public static ChunkVertexType getType() {
        return RenderingThread.getOrCreate(SodiumInteractor.PIPELINE).type;
    }
    
    private static final RenderMaterial[] STANDARD_MATERIALS;
    private static final RenderMaterial TRANSLUCENT_MATERIAL;
    private ChunkBuildBuffers buildBuffers;
    private ChunkVertexType type;
    private final LevelSlice slice = LittleLevelSliceExtender.create();
    private BlockRenderer renderer;
    private LittleLightDataAccess lightAccess;
    private LightPipelineProvider lighters;
    private QuadLightData cachedQuadLightData = new QuadLightData();
    public BlockRenderContext context = new BlockRenderContext(slice, null);
    private Set<TextureAtlasSprite> sprites = new ObjectOpenHashSet<>();
    private MutableBlockPos modelOffset = new MutableBlockPos();
    private IntArrayList[] indexes;
    private MutableBlockPos scratchColorPos = new MutableBlockPos();
    private int[] faceCounters = new int[ModelQuadFacing.COUNT];
    private int[] colors = new int[4];
    private Vec3d cubeCenter = new Vec3d();
    
    public LittleRenderPipelineSodium() {
        indexes = new IntArrayList[ModelQuadFacing.COUNT];
        for (int i = 0; i < indexes.length; i++)
            indexes[i] = new IntArrayList();
    }
    
    @Override
    public void buildCache(PoseStack pose, ChunkLayerMap<BufferCache> buffers, RenderingBlockContext data, VertexFormat format, SingletonList<BakedQuad> bakedQuadWrapper) {
        if (buildBuffers == null)
            reload();
        
        Level renderLevel = data.be.getLevel();
        while (renderLevel instanceof LittleSubLevel sub && !sub.shouldUseLightingForRenderig())
            renderLevel = sub.getParent();
        
        ((LittleLevelSliceExtender) (Object) slice).setLevel(renderLevel);
        ((BlockRenderContextAccessor) context).setSlice(slice);
        
        BlockPos pos = data.be.getBlockPos();
        
        lightAccess.prepare(renderLevel);
        
        renderer.prepare(buildBuffers, slice, null);
        
        LightPipeline lighter = lighters.getLighter(Minecraft.useAmbientOcclusion() && data.state.getLightEmission(data.be.getLevel(),
            pos) == 0 ? LightMode.SMOOTH : LightMode.FLAT);
        
        ColorProviderRegistry colorProvider = ((BlockRendererExtender) renderer).colorRegistry();
        data.prepareModelOffset(modelOffset, pos);
        
        ((BlockRendererExtender) renderer).setOffset(modelOffset);
        
        MutableQuadViewImpl editorQuad = ((BlockRendererExtender) renderer).getEditorQuadAndClear();
        
        // Render vertex buffer
        for (Tuple<RenderType, IndexedCollector<LittleRenderBox>> entry : data.be.render.boxCache.tuples()) {
            
            Material material = DefaultMaterials.forRenderLayer(entry.getKey());
            ChunkModelBuilder builder = buildBuffers.get(material);
            
            IndexedCollector<LittleRenderBox> cubes = entry.value;
            if (cubes == null || cubes.isEmpty())
                continue;
            
            for (int i = 0; i < ModelQuadFacing.VALUES.length; i++)
                builder.getVertexBuffer(ModelQuadFacing.VALUES[i]).start(data.sectionIndex());
            
            Arrays.fill(faceCounters, 0);
            
            for (Iterator<LittleRenderBox> iterator = cubes.sectionIterator(x -> {
                for (int i = 0; i < indexes.length; i++) {
                    indexes[i].add(x);
                    ChunkMeshBufferBuilderAccessor a = (ChunkMeshBufferBuilderAccessor) builder.getVertexBuffer(ModelQuadFacing.VALUES[i]);
                    indexes[i].add(a.getVertexCount() * a.getStride());
                }
            });iterator.hasNext();) {
                LittleRenderBox cube = iterator.next();
                BlockState state = cube.state;
                
                context.update(pos, modelOffset, state, null, 0);
                //OculusManager.setLocalPos(buildBuffers, pos);
                cubeCenter.set((cube.maxX + cube.minX) * 0.5, (cube.maxY + cube.minY) * 0.5, (cube.maxZ + cube.minZ) * 0.5);
                
                ColorProvider<BlockState> colorizer = null;
                
                /*if (OculusManager.isShaders()) {
                    if (state.getBlock() instanceof IFakeRenderingBlock fake)
                        state = fake.getFakeState(state);
                    OculusManager.setMaterialId(buildBuffers, state);
                }*/
                
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
                        
                        for (BakedQuad quad : quads) {
                            editorQuad.fromVanilla(quad, (entry.getKey() == RenderType.tripwire() || entry.getKey() == RenderType
                                    .translucent()) ? TRANSLUCENT_MATERIAL : STANDARD_MATERIALS[AmbientOcclusionMode.DEFAULT.ordinal()], direction);
                            
                            RenderMaterial mat = editorQuad.material();
                            
                            boolean hasColor = false;
                            if (cube.color != -1) {
                                int color = ColorABGR.pack(ColorUtils.red(cube.color), ColorUtils.green(cube.color), ColorUtils.blue(cube.color), ColorUtils.alpha(cube.color));
                                Arrays.fill(colors, color);
                                hasColor = true;
                            } else if (!mat.disableColorIndex() && editorQuad.hasColor()) {
                                if (colorizer == null)
                                    colorizer = colorProvider.getColorProvider(state.getBlock());
                                
                                colorizer.getColors(slice, pos, scratchColorPos, state, editorQuad, colors);
                                hasColor = true;
                            } else
                                Arrays.fill(colors, -1);
                            
                            if (hasColor)
                                for (int i = 0; i < 4; ++i)
                                    editorQuad.color(i, ColorMixer.mulComponentWise(colors[i], editorQuad.color(i)));
                                
                            lighter.calculate(editorQuad, pos, cachedQuadLightData, editorQuad.cullFace(), editorQuad.lightFace(), editorQuad.hasShade(), mat
                                    .shadeMode() == ShadeMode.ENHANCED);
                            if (mat.emissive())
                                for (int i = 0; i < 4; ++i)
                                    editorQuad.lightmap(i, 15728880);
                            else
                                for (int i = 0; i < 4; ++i)
                                    editorQuad.lightmap(i, ColorHelper.maxBrightness(editorQuad.lightmap(i), cachedQuadLightData.lm[i]));
                                
                            ((BlockRendererExtender) renderer).callBufferQuad(editorQuad, cachedQuadLightData.br, material);
                            TextureAtlasSprite sprite = editorQuad.cachedSprite();
                            if (sprite != null && SpriteUtil.hasAnimation(sprite))
                                sprites.add(sprite);
                            
                            editorQuad.clear();
                        }
                    }
                }
                
                bakedQuadWrapper.setElement(null);
                
                //OculusManager.resetBlockContext(buildBuffers);
                
                if (!LittleTiles.CONFIG.rendering.useQuadCache)
                    cube.deleteQuadCache();
            }
            
            BufferHolder[] holders = new BufferHolder[ModelQuadFacing.COUNT];
            int count = indexes[0].size() / 2;
            
            if (entry.getKey() == RenderType.translucent()) {
                // For translucent stuff the rendering data is all put into the unassigned face.
                // Which means we need to put all the data in that buffer while making sure the structure indexes still match
                IntList newIndexes = new IntArrayList();
                int size = 0;
                int vertexCount = 0;
                
                for (int i = 0; i < indexes.length; i++) {
                    ChunkMeshBufferBuilderAccessor v = (ChunkMeshBufferBuilderAccessor) builder.getVertexBuffer(ModelQuadFacing.VALUES[i]);
                    int bufferSize = v.getStride() * v.getVertexCount();
                    for (int j = 0; j < indexes[i].size(); j += 2) {
                        int start = indexes[i].getInt(j + 1);
                        int next = indexes[i].size() > j + 3 ? indexes[i].getInt(j + 3) : bufferSize;
                        addIndexGroup(newIndexes, indexes[i].getInt(j), next - start);
                    }
                    size += bufferSize;
                    vertexCount += v.getVertexCount();
                }
                
                ByteBuffer buffer = ByteBuffer.allocateDirect(size);
                Int2ObjectMap<ByteBuffer> bufferMap = new Int2ObjectArrayMap<>();
                int[] correctIndexes = new int[newIndexes.size()];
                int current = 0;
                for (int i = 0; i < newIndexes.size(); i += 2) {
                    int length = newIndexes.getInt(i + 1);
                    int index = newIndexes.getInt(i);
                    bufferMap.put(index, buffer.slice(current, length));
                    correctIndexes[i] = index;
                    correctIndexes[i + 1] = current;
                    current += length;
                }
                
                for (int i = 0; i < indexes.length; i++) {
                    ChunkMeshBufferBuilderAccessor v = (ChunkMeshBufferBuilderAccessor) builder.getVertexBuffer(ModelQuadFacing.VALUES[i]);
                    ByteBuffer threadBuffer = v.getBuffer();
                    int bufferSize = v.getStride() * v.getVertexCount();
                    int threadIndex = 0;
                    for (int j = 0; j < indexes[i].size(); j += 2) {
                        int start = indexes[i].getInt(j + 1);
                        int next = indexes[i].size() > j + 3 ? indexes[i].getInt(j + 3) : bufferSize;
                        int length = next - start;
                        if (length == 0)
                            continue;
                        var b = bufferMap.get(indexes[i].getInt(j));
                        b.put(b.position(), threadBuffer, threadIndex, length);
                        b.position(b.position() + length);
                        threadIndex += length;
                    }
                    
                    indexes[i].clear();
                }
                
                holders[ModelQuadFacing.UNASSIGNED.ordinal()] = new BufferHolder(buffer, size, vertexCount, correctIndexes);
            } else {
                for (int i = 0; i < indexes.length; i++) {
                    ChunkMeshBufferBuilderAccessor v = (ChunkMeshBufferBuilderAccessor) builder.getVertexBuffer(ModelQuadFacing.VALUES[i]);
                    if (v.getVertexCount() > 0) {
                        ByteBuffer buffer = ByteBuffer.allocateDirect(v.getStride() * v.getVertexCount());
                        ByteBuffer threadBuffer = v.getBuffer();
                        threadBuffer.limit(buffer.capacity());
                        MemoryUtil.memCopy(threadBuffer, buffer);
                        threadBuffer.limit(threadBuffer.capacity());
                        holders[i] = new BufferHolder(buffer, buffer.limit(), v.getVertexCount(), indexes[i].toIntArray());
                        indexes[i].clear();
                    }
                }
            }
            
            buffers.put(entry.key, new SodiumBufferCache(holders, new ArrayList<>(sprites), count));
            sprites.clear();
        }
        
        ((LittleLevelSliceExtender) (Object) slice).setLevel(null);
    }
    
    private void addIndexGroup(IntList list, int index, int count) {
        for (int i = 0; i < list.size(); i += 2)
            if (list.getInt(i) == index) {
                list.set(i + 1, list.getInt(i + 1) + count);
                return;
            }
        list.add(index);
        list.add(count);
    }
    
    @Override
    public void reload() {
        RenderSectionManager manager = ((SodiumWorldRendererAccessor) SodiumWorldRenderer.instance()).getRenderSectionManager();
        if (manager == null) {
            buildBuffers = null;
            renderer = null;
            return;
        }
        ChunkBuilderAccessor chunkBuilder = (ChunkBuilderAccessor) manager.getBuilder();
        type = ((ChunkBuildBuffersAccessor) chunkBuilder.getLocalContext().buffers).getVertexType();
        buildBuffers = new ChunkBuildBuffers(type);
        buildBuffers.init(null, 0);
        renderer = new BlockRenderer(new ColorProviderRegistry(Minecraft.getInstance()
                .getBlockColors()), lighters = new LightPipelineProvider(lightAccess = new LittleLightDataAccess()));
        ((BlockRendererExtender) renderer).markAsTakenOver();
    }
    
    @Override
    public void release() {
        buildBuffers.destroy();
    }
    
    static {
        TRANSLUCENT_MATERIAL = SodiumRenderer.INSTANCE.materialFinder().blendMode(BlendMode.TRANSLUCENT).find();
        STANDARD_MATERIALS = new RenderMaterial[AmbientOcclusionMode.values().length];
        AmbientOcclusionMode[] values = AmbientOcclusionMode.values();
        
        for (int i = 0; i < values.length; ++i) {
            TriState var10000;
            switch (values[i]) {
                case ENABLED:
                    var10000 = TriState.TRUE;
                    break;
                case DISABLED:
                    var10000 = TriState.FALSE;
                    break;
                case DEFAULT:
                    var10000 = TriState.DEFAULT;
                    break;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }
            
            TriState state = var10000;
            STANDARD_MATERIALS[i] = SodiumRenderer.INSTANCE.materialFinder().ambientOcclusion(state).find();
        }
        
    }
    
}
