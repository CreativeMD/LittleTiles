package team.creative.littletiles.mixin.embeddium;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.embeddedt.embeddium.impl.gl.arena.GlBufferArena;
import org.embeddedt.embeddium.impl.gl.arena.GlBufferSegment;
import org.embeddedt.embeddium.impl.gl.arena.PendingUpload;
import org.embeddedt.embeddium.impl.gl.attribute.GlVertexFormat;
import org.embeddedt.embeddium.impl.gl.buffer.GlBuffer;
import org.embeddedt.embeddium.impl.gl.buffer.GlBufferTarget;
import org.embeddedt.embeddium.impl.gl.device.CommandList;
import org.embeddedt.embeddium.impl.gl.device.RenderDevice;
import org.embeddedt.embeddium.impl.model.quad.properties.ModelQuadFacing;
import org.embeddedt.embeddium.impl.render.EmbeddiumWorldRenderer;
import org.embeddedt.embeddium.impl.render.chunk.RenderSection;
import org.embeddedt.embeddium.impl.render.chunk.RenderSectionFlags;
import org.embeddedt.embeddium.impl.render.chunk.RenderSectionManager;
import org.embeddedt.embeddium.impl.render.chunk.data.SectionRenderDataStorage;
import org.embeddedt.embeddium.impl.render.chunk.region.RenderRegion;
import org.embeddedt.embeddium.impl.render.chunk.terrain.TerrainRenderPass;
import org.embeddedt.embeddium.impl.render.chunk.terrain.material.DefaultMaterials;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkMeshAttribute;
import org.lwjgl.opengl.GL15C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexSorting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.mod.embeddium.buffer.EmbeddiumChunkBufferDownloader;
import team.creative.littletiles.client.mod.embeddium.buffer.EmbeddiumChunkBufferUploader;
import team.creative.littletiles.client.render.cache.LayeredBufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.mc.VertexBufferExtender;

@Mixin(RenderSection.class)
public abstract class RenderSectionMixin implements RenderChunkExtender {
    
    @Shadow(remap = false)
    private int sectionIndex;
    
    @Shadow(remap = false)
    private int chunkX;
    
    @Shadow(remap = false)
    private int chunkY;
    
    @Shadow(remap = false)
    private int chunkZ;
    
    @Shadow(remap = false)
    private TextureAtlasSprite[] animatedSprites;
    
    @Shadow(remap = false)
    private boolean built;
    @Shadow(remap = false)
    private int flags;
    
    @Unique
    private BlockPos origin;
    
    @Unique
    private volatile int queued;
    
    @Unique
    public volatile ChunkLayerMap<BufferCollection> lastUploaded;
    
    @Override
    public int getQueued() {
        return queued;
    }
    
    @Override
    public void setQueued(int queued) {
        this.queued = queued;
    }
    
    @Override
    public ChunkLayerMap<BufferCollection> getLastUploaded() {
        return lastUploaded;
    }
    
    @Override
    public void setLastUploaded(ChunkLayerMap<BufferCollection> uploaded) {
        this.lastUploaded = uploaded;
    }
    
    @Override
    public VertexBuffer getVertexBuffer(RenderType layer) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void markReadyForUpdate(boolean playerChanged) {
        ((EmbeddiumWorldRendererAccessor) EmbeddiumWorldRenderer.instance()).getRenderSectionManager().scheduleRebuild(chunkX, chunkY, chunkZ, playerChanged);
    }
    
    @Override
    public VertexSorting createVertexSorting(double x, double y, double z) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean isEmpty(RenderType layer) {
        return getUploadedBuffer(getStorage(getRenderRegion(), layer)) == null;
    }
    
    @Override
    public MeshData.SortState getTransparencyState() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void setHasBlock(RenderType layer) {
        throw new UnsupportedOperationException();
    }
    
    public GlBufferSegment getUploadedBuffer(SectionRenderDataStorage storage) {
        SectionRenderDataStorageAccessor s = (SectionRenderDataStorageAccessor) storage;
        if (s == null)
            return null;
        return s.getAllocations()[sectionIndex];
    }
    
    public SectionRenderDataStorage getStorage(RenderRegion region, RenderType layer) {
        return region.getStorage(DefaultMaterials.forRenderLayer(layer).pass);
    }
    
    public RenderRegion getRenderRegion() {
        return ((RenderSectionManagerAccessor) ((EmbeddiumWorldRendererAccessor) EmbeddiumWorldRenderer.instance()).getRenderSectionManager()).getRegions().createForChunk(chunkX,
            chunkY, chunkZ);
    }
    
    @Override
    public synchronized void prepareUpload() {
        backToRAM();
        RenderChunkExtender.super.prepareUpload();
    }
    
    @Override
    public ByteBuffer downloadUploadedData(VertexBufferExtender buffer, long offset, int size) {
        boolean active = ((GLRenderDeviceAccessor) RenderDevice.INSTANCE).getIsActive();
        if (!active)
            RenderDevice.enterManagedCode();
        
        try {
            RenderDevice.INSTANCE.createCommandList().bindBuffer(GlBufferTarget.ARRAY_BUFFER, (GlBuffer) buffer);
            ByteBuffer result = ByteBuffer.allocateDirect(size);
            GL15C.glGetBufferSubData(GlBufferTarget.ARRAY_BUFFER.getTargetParameter(), offset, result);
            return result;
        } catch (IllegalArgumentException | IllegalStateException e) {
            if (!(e instanceof IllegalStateException))
                e.printStackTrace();
            return null;
        } finally {
            if (!active)
                RenderDevice.exitManagedCode();
        }
    }
    
    public ByteBuffer downloadSegment(GlBufferSegment segment, GlVertexFormat format) {
        GlBuffer buffer = ((GlBufferSegmentAccessor) segment).getArena().getBufferObject();
        return downloadUploadedData((VertexBufferExtender) buffer, segment.getOffset() * format.getStride(), segment.getLength() * format.getStride());
    }
    
    @Override
    public void backToRAM() {
        RenderRegion region = getRenderRegion();
        ChunkLayerMap<BufferCollection> caches = getLastUploaded();
        if (caches == null)
            return;
        
        Runnable run = () -> {
            EmbeddiumChunkBufferDownloader downloader = new EmbeddiumChunkBufferDownloader();
            RenderSectionManager manager = ((EmbeddiumWorldRendererAccessor) EmbeddiumWorldRenderer.instance()).getRenderSectionManager();
            ChunkBuilderAccessor chunkBuilder = (ChunkBuilderAccessor) manager.getBuilder();
            GlVertexFormat<ChunkMeshAttribute> format = ((ChunkBuildBuffersAccessor) chunkBuilder.getLocalContext().buffers).getVertexType().getVertexFormat();
            for (Tuple<RenderType, BufferCollection> tuple : caches.tuples()) {
                SectionRenderDataStorage storage = region.getStorage(DefaultMaterials.forRenderLayer(tuple.key).pass);
                if (storage == null)
                    continue;
                
                GlBufferSegment segment = getUploadedBuffer(storage);
                if (segment == null)
                    continue;
                
                ByteBuffer vertexData = downloadSegment(segment, format);
                if (vertexData == null) {
                    tuple.value.discard();
                    continue;
                }
                
                downloader.set(storage.getDataPointer(sectionIndex), format, segment.getOffset(), vertexData);
                tuple.value.download(downloader);
                downloader.clear();
            }
            setLastUploaded(null);
        };
        try {
            if (Minecraft.getInstance().isSameThread())
                run.run();
            else
                synchronized (this) {
                    CompletableFuture.runAsync(run, Minecraft.getInstance()).join();
                }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    
    @Override
    public boolean appendRenderData(Iterable<? extends LayeredBufferCache> blocks) {
        RenderSectionManager manager = ((EmbeddiumWorldRendererAccessor) EmbeddiumWorldRenderer.instance()).getRenderSectionManager();
        RenderRegion region = getRenderRegion();
        ChunkBuilderAccessor chunkBuilder = (ChunkBuilderAccessor) manager.getBuilder();
        GlVertexFormat<ChunkMeshAttribute> format = ((ChunkBuildBuffersAccessor) chunkBuilder.getLocalContext().buffers).getVertexType().getVertexFormat();
        EmbeddiumChunkBufferUploader uploader = new EmbeddiumChunkBufferUploader();
        
        for (RenderType layer : RenderType.chunkBufferLayers()) {
            
            int size = 0;
            for (LayeredBufferCache data : blocks)
                size += data.length(layer);
            
            if (size == 0)
                continue;
            
            TerrainRenderPass pass = DefaultMaterials.forRenderLayer(layer).pass;
            SectionRenderDataStorage storage = region.createStorage(pass);
            
            GlBufferSegment segment = getUploadedBuffer(storage);
            ByteBuffer vanillaBuffer = null;
            if (segment != null)
                vanillaBuffer = downloadSegment(segment, format);
            
            if (segment == null) {
                if (layer != RenderType.translucent()) // With special sorting enabled the data cannot be retrieved
                    LittleTiles.LOGGER.error("Failed to download chunk data. chunk: {}, layer: {}", this, layer);
                continue;
            }
            
            int[] extraLengthFacing = new int[ModelQuadFacing.COUNT];
            for (LayeredBufferCache layeredCache : blocks)
                for (int i = 0; i < extraLengthFacing.length; i++)
                    extraLengthFacing[i] += layeredCache.length(layer, i);
                
            uploader.set(storage.getDataPointer(sectionIndex), format, segment.getOffset(), vanillaBuffer, size, extraLengthFacing, null);
            
            if (segment != null) // Meshes needs to be removed after the uploader has collected the data
                storage.removeMeshes(sectionIndex);
            
            for (LayeredBufferCache layeredCache : blocks) {
                BufferCache cache = layeredCache.get(layer);
                if (cache != null && cache.isAvailable())
                    cache.upload(uploader);
            }
            
            // Maybe sort uploaded buffer????
            //if (layer == RenderType.translucent())
            
            boolean active = ((GLRenderDeviceAccessor) RenderDevice.INSTANCE).getIsActive();
            if (!active)
                RenderDevice.enterManagedCode();
            
            PendingUpload upload = new PendingUpload(uploader.buffer());
            
            CommandList commandList = RenderDevice.INSTANCE.createCommandList();
            
            RenderRegion.DeviceResources resources = region.createResources(commandList);
            GlBufferArena arena = resources.getGeometryArena();
            
            boolean bufferChanged = arena.upload(commandList, Stream.of(upload));
            if (bufferChanged)
                region.refresh(commandList);
            
            storage.setMeshes(sectionIndex, upload.getResult(), null, uploader.ranges());
            
            if (!active)
                RenderDevice.exitManagedCode();
            
            uploader.clear();
            
        }
        
        animatedSprites = uploader.sprites();
        
        //manager.markGraphDirty();
        built = true;
        flags |= 1 << RenderSectionFlags.HAS_BLOCK_GEOMETRY;
        return true;
    }
    
}
