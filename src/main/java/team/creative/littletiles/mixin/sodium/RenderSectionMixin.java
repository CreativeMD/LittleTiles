package team.creative.littletiles.mixin.sodium;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.lwjgl.opengl.GL15C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexSorting;

import net.caffeinemc.mods.sodium.client.gl.arena.GlBufferArena;
import net.caffeinemc.mods.sodium.client.gl.arena.GlBufferSegment;
import net.caffeinemc.mods.sodium.client.gl.arena.PendingUpload;
import net.caffeinemc.mods.sodium.client.gl.attribute.GlVertexFormat;
import net.caffeinemc.mods.sodium.client.gl.buffer.GlBuffer;
import net.caffeinemc.mods.sodium.client.gl.buffer.GlBufferTarget;
import net.caffeinemc.mods.sodium.client.gl.device.CommandList;
import net.caffeinemc.mods.sodium.client.gl.device.RenderDevice;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionFlags;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.data.BuiltSectionMeshParts;
import net.caffeinemc.mods.sodium.client.render.chunk.data.SectionRenderDataStorage;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.data.DynamicData;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.data.DynamicTopoData;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.data.DynamicTopoData.DynamicTopoSorter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.mod.sodium.SodiumSectionCameraPos;
import team.creative.littletiles.client.mod.sodium.buffer.SodiumAppendChunkBufferUploader;
import team.creative.littletiles.client.mod.sodium.buffer.SodiumChunkBufferDownloader;
import team.creative.littletiles.client.render.cache.LayeredBufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.mc.VertexBufferExtender;

@Mixin(RenderSection.class)
public abstract class RenderSectionMixin implements RenderChunkExtender {
    
    @Shadow(remap = false)
    @Final
    private int sectionIndex;
    
    @Shadow(remap = false)
    @Final
    private int chunkX;
    
    @Shadow(remap = false)
    @Final
    private int chunkY;
    
    @Shadow(remap = false)
    @Final
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
        Minecraft.getInstance().execute(() -> ((SodiumWorldRendererAccessor) SodiumWorldRenderer.instance()).getRenderSectionManager().scheduleRebuild(chunkX, chunkY, chunkZ,
            playerChanged));
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
        return s.getVertexAllocations()[sectionIndex];
    }
    
    public SectionRenderDataStorage getStorage(RenderRegion region, RenderType layer) {
        return region.getStorage(DefaultMaterials.forRenderLayer(layer).pass);
    }
    
    public RenderRegion getRenderRegion() {
        return ((RenderSectionManagerAccessor) ((SodiumWorldRendererAccessor) SodiumWorldRenderer.instance()).getRenderSectionManager()).getRegions().createForChunk(chunkX, chunkY,
            chunkZ);
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
        return downloadUploadedData((VertexBufferExtender) buffer, segment.getOffset() * format.getStride(), (int) (segment.getLength() * format.getStride()));
    }
    
    @Override
    public void backToRAM() {
        RenderRegion region = getRenderRegion();
        ChunkLayerMap<BufferCollection> caches = getLastUploaded();
        if (caches == null)
            return;
        
        Runnable run = () -> {
            SodiumChunkBufferDownloader downloader = new SodiumChunkBufferDownloader();
            RenderSectionManager manager = ((SodiumWorldRendererAccessor) SodiumWorldRenderer.instance()).getRenderSectionManager();
            ChunkBuilderAccessor chunkBuilder = (ChunkBuilderAccessor) manager.getBuilder();
            GlVertexFormat format = ((ChunkBuildBuffersAccessor) chunkBuilder.getLocalContext().buffers).getVertexType().getVertexFormat();
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
        RenderSectionManager manager = ((SodiumWorldRendererAccessor) SodiumWorldRenderer.instance()).getRenderSectionManager();
        RenderRegion region = getRenderRegion();
        ChunkBuilderAccessor chunkBuilder = (ChunkBuilderAccessor) manager.getBuilder();
        GlVertexFormat format = ((ChunkBuildBuffersAccessor) chunkBuilder.getLocalContext().buffers).getVertexType().getVertexFormat();
        SodiumAppendChunkBufferUploader uploader = new SodiumAppendChunkBufferUploader();
        
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
                LittleTiles.LOGGER.error("Failed to download chunk data. chunk: {}, layer: {}", this, layer);
                continue;
            }
            
            int[] extraLengthFacing = new int[ModelQuadFacing.COUNT];
            for (LayeredBufferCache layeredCache : blocks)
                for (int i = 0; i < extraLengthFacing.length; i++)
                    extraLengthFacing[i] += layeredCache.length(layer, i);
                
            uploader.set(storage.getDataPointer(sectionIndex), format, segment.getOffset(), vanillaBuffer, size, extraLengthFacing, null);
            if (layer == RenderType.translucent()) {
                uploader.setTranslucentCollector(new TranslucentGeometryCollector(((RenderSection) (Object) this).getPosition()));
                if (vanillaBuffer != null)
                    uploader.appendVanillaTranslucentData(vanillaBuffer);
            }
            
            if (segment != null) // Meshes needs to be removed after the uploader has collected the data
                storage.removeData(sectionIndex);
            
            for (LayeredBufferCache layeredCache : blocks) {
                BufferCache cache = layeredCache.get(layer);
                if (cache != null && cache.isAvailable())
                    cache.upload(uploader);
            }
            
            boolean active = ((GLRenderDeviceAccessor) RenderDevice.INSTANCE).getIsActive();
            if (!active)
                RenderDevice.enterManagedCode();
            
            PendingUpload upload = new PendingUpload(uploader.buffer());
            
            CommandList commandList = RenderDevice.INSTANCE.createCommandList();
            
            RenderRegion.DeviceResources resources = region.createResources(commandList);
            GlBufferArena arena = resources.getGeometryArena();
            
            boolean bufferChanged = arena.upload(commandList, Stream.of(upload));
            if (bufferChanged)
                region.refreshTesselation(commandList);
            
            storage.setVertexData(sectionIndex, upload.getResult(), uploader.ranges());
            
            if (layer == RenderType.translucent()) {
                var cam = new SodiumSectionCameraPos(((RenderSectionManagerAccessor) manager).getCameraPosition(), ((RenderSection) (Object) this)
                        .getOriginX(), ((RenderSection) (Object) this).getOriginY(), ((RenderSection) (Object) this).getOriginZ());
                uploader.getTranslucentCollector().finishRendering();
                BuiltSectionMeshParts mesh = new BuiltSectionMeshParts(uploader.buffer(), uploader.ranges());
                var oldData = ((RenderSection) (Object) this).getTranslucentData();
                var data = uploader.getTranslucentCollector().getTranslucentData(oldData, mesh, cam);
                
                ((RenderSectionManagerAccessor) manager).getSortTriggering().integrateTranslucentData(oldData, data, cam.getAbsoluteCameraPos(), manager::scheduleSort);
                ((RenderSection) (Object) this).setTranslucentData(data);
                
                storage.removeIndexData(sectionIndex);
                
                if (data instanceof DynamicData d) {
                    var sorter = d.getSorter();
                    sorter.writeIndexBuffer(cam, true);
                    PendingUpload indexUpload = new PendingUpload(sorter.getIndexBuffer());
                    
                    arena = resources.getIndexArena();
                    if (arena.upload(commandList, Stream.of(indexUpload)))
                        region.refreshIndexedTesselation(commandList);
                    
                    storage.setIndexData(sectionIndex, indexUpload.getResult());
                    
                    if (data instanceof DynamicTopoData topo)
                        ((RenderSectionManagerAccessor) manager).getSortTriggering().applyTriggerChanges(topo, (DynamicTopoSorter) topo.getSorter(), ((RenderSection) (Object) this)
                                .getPosition(), cam.getAbsoluteCameraPos());
                }
            }
            
            if (!active)
                RenderDevice.exitManagedCode();
            
            uploader.clear();
            
        }
        
        animatedSprites = uploader.sprites();
        
        built = true;
        flags |= 1 << RenderSectionFlags.HAS_BLOCK_GEOMETRY;
        return true;
    }
    
}
