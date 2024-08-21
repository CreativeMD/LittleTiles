package team.creative.littletiles.client.render.block;

import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.client.render.face.RenderBoxFace;
import team.creative.creativecore.client.render.face.RenderBoxFaceSpecial;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.type.list.IndexedCollector;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.mod.rubidium.RubidiumManager;
import team.creative.littletiles.client.mod.rubidium.pipeline.LittleRenderPipelineRubidium;
import team.creative.littletiles.client.render.cache.AdditionalBufferReceiver;
import team.creative.littletiles.client.render.cache.BlockBufferCache;
import team.creative.littletiles.client.render.cache.IBlockBufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.build.RenderingBlockContext;
import team.creative.littletiles.client.render.cache.build.RenderingThread;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.face.LittleFace;
import team.creative.littletiles.common.math.face.LittleFaceState;
import team.creative.littletiles.common.math.face.LittleServerFace;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.attribute.LittleStructureAttribute;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.mixin.client.render.LevelRendererAccessor;
import team.creative.littletiles.mixin.client.render.ViewAreaAccessor;

@OnlyIn(Dist.CLIENT)
public class BERenderManager {
    
    public static RenderChunkExtender getRenderChunk(Level level, BlockPos pos) {
        if (level instanceof LittleLevel little)
            return little.getRenderManager().getRenderChunk(pos);
        if (RubidiumManager.installed())
            return LittleRenderPipelineRubidium.getChunk(pos);
        return (RenderChunkExtender) ((ViewAreaAccessor) ((LevelRendererAccessor) Minecraft.getInstance().levelRenderer).getViewArea()).getChunkAt(pos);
    }
    
    private BETiles be;
    
    private volatile AtomicInteger blocked = new AtomicInteger(0);
    private volatile byte requestedIndex = -1;
    private int renderState = -1;
    
    private boolean queued = false;
    private boolean eraseBoxCache = false;
    
    public boolean hasLightChanged = false;
    private boolean neighbourChanged = false;
    
    private double cachedRenderDistance = 0;
    private AABB cachedRenderBoundingBox = null;
    private boolean requireRenderingBoundingBoxUpdate = false;
    
    private final BlockBufferCache bufferCache = new BlockBufferCache();
    public final ChunkLayerMap<IndexedCollector<LittleRenderBox>> boxCache = new ChunkLayerMap<>();
    
    public BERenderManager(BETiles be) {
        this.be = be;
    }
    
    public void setBe(BETiles be) {
        this.be = be;
    }
    
    public boolean isInQueue() {
        return queued;
    }
    
    public void chunkUpdate(RenderChunkExtender chunk) {
        synchronized (this) {
            boolean doesNeedUpdate = neighbourChanged || hasLightChanged || requestedIndex == -1 || bufferCache.hasInvalidBuffers();
            if (renderState != RenderingThread.CURRENT_RENDERING_INDEX) {
                eraseBoxCache = true;
                doesNeedUpdate = true;
            }
            
            hasLightChanged = false;
            neighbourChanged = false;
            
            if (doesNeedUpdate)
                queue(eraseBoxCache, chunk);
        }
    }
    
    public boolean isBlocked() {
        return blocked.get() > 0;
    }
    
    public boolean getAndSetBlocked() {
        return blocked.getAndIncrement() > 0;
    }
    
    public void unsetBlocked() {
        blocked.decrementAndGet();
    }
    
    public void tilesChanged() {
        requireRenderingBoundingBoxUpdate = true;
        cachedRenderDistance = 0;
        queue(true, null);
    }
    
    public void markRenderBoundingBoxDirty() {
        requireRenderingBoundingBoxUpdate = true;
    }
    
    public double getMaxRenderDistance() {
        if (cachedRenderDistance == 0) {
            double renderDistance = 64;
            for (LittleStructure structure : be.rendering())
                renderDistance = Math.max(renderDistance, structure.getMaxRenderDistance());
            cachedRenderDistance = renderDistance;
        }
        return cachedRenderDistance;
    }
    
    public AABB getRenderBoundingBox() {
        if (requireRenderingBoundingBoxUpdate || cachedRenderBoundingBox == null) {
            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double minZ = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE;
            double maxY = -Double.MAX_VALUE;
            double maxZ = -Double.MAX_VALUE;
            boolean found = false;
            for (LittleStructure structure : be.rendering()) {
                AABB box = structure.getRenderBoundingBox();
                if (box == null)
                    continue;
                box = box.move(be.getBlockPos());
                minX = Math.min(box.minX, minX);
                minY = Math.min(box.minY, minY);
                minZ = Math.min(box.minZ, minZ);
                maxX = Math.max(box.maxX, maxX);
                maxY = Math.max(box.maxY, maxY);
                maxZ = Math.max(box.maxZ, maxZ);
                found = true;
            }
            if (found)
                cachedRenderBoundingBox = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
            else
                cachedRenderBoundingBox = new AABB(be.getBlockPos());
            
            requireRenderingBoundingBoxUpdate = false;
        }
        return cachedRenderBoundingBox;
    }
    
    public void onNeighbourChanged() {
        neighbourChanged = true;
        queue(false, null);
    }
    
    public void queue(boolean eraseBoxCache, @Nullable RenderChunkExtender chunk) {
        synchronized (this) {
            requestedIndex++;
            
            this.eraseBoxCache |= eraseBoxCache;
            
            if (!queued && RenderingThread.queue(be, chunk))
                queued = true;
        }
    }
    
    public int startBuildingCache() {
        synchronized (this) {
            if (eraseBoxCache) {
                boxCache.clear();
                eraseBoxCache = false;
            }
            blocked.incrementAndGet();
            return requestedIndex;
        }
        
    }
    
    public boolean finishBuildingCache(int index, ChunkLayerMap<BufferCache> buffers, int renderState, boolean force) {
        synchronized (this) {
            this.renderState = renderState;
            boolean done = force || (index == requestedIndex && this.renderState == renderState);
            if (done)
                queued = false;
            this.hasLightChanged = false;
            bufferCache.setBuffers(buffers);
            return done;
        }
    }
    
    public void resetRenderingState() {
        queued = false;
        requestedIndex = -1;
    }
    
    public void chunkUnload() {
        synchronized (this) {
            bufferCache.setEmpty();
            boxCache.clear();
            cachedRenderBoundingBox = null;
        }
    }
    
    public IBlockBufferCache buffers() {
        return bufferCache;
    }
    
    /** This method has to be called before block receives update and has possibly rendered. Otherwise problems will occur. If not sure rather use {@link BERenderManager.additionalBuffers()} */
    public void additionalBuffersEarly(Consumer<AdditionalBufferReceiver> consumer) {
        bufferCache.executeAdditional(consumer);
    }
    
    public void additionalBuffers(Consumer<AdditionalBufferReceiver> consumer) {
        if (isInQueue()) {
            bufferCache.executeAdditional(consumer);
            if (!isInQueue()) // The rendering has been finished in the meantime ... in that case the additional should be removed again
                bufferCache.clearAdditional();
        }
    }
    
    public void setBuffersEmpty() {
        bufferCache.setEmpty();
    }
    
    public boolean hasAdditionalBuffers() {
        return bufferCache.hasAdditional();
    }
    
    public void beforeBuilding(RenderingBlockContext context) {
        if (neighbourChanged) {
            neighbourChanged = false;
            
            for (Entry<RenderType, IndexedCollector<LittleRenderBox>> entry : boxCache.tuples()) {
                if (entry.getValue() == null)
                    continue;
                for (LittleRenderBox cube : entry.getValue())
                    for (int k = 0; k < Facing.VALUES.length; k++) {
                        Facing facing = Facing.VALUES[k];
                        if (cube.box == null)
                            continue;
                        LittleFaceState state = cube.box.getFaceState(facing);
                        
                        if (state.outside())
                            calculateFaces(facing, state, context, (LittleTile) cube.customData, cube.box, cube);
                    }
            }
        }
    }
    
    private void calculateFaces(Facing facing, LittleFaceState state, RenderingBlockContext context, @Nullable LittleTile tile, LittleBox box, LittleRenderBox cube) {
        if (state.coveredFully()) {
            cube.setFace(facing, RenderBoxFace.NOT_RENDER);
            return;
        }
        
        if (tile != null && tile.isTranslucent() && state.partially()) {
            LittleFace face = cube.box.generateFace(be.getGrid(), facing);
            BETiles toCheck = be;
            if (state.outside()) {
                toCheck = context.getNeighbour(facing);
                face.move(facing);
            }
            if (toCheck.shouldFaceBeRendered(face, tile))
                cube.setFace(facing, new RenderBoxFaceSpecial(face.generateFans(), (float) face.grid.pixelLength));
            else
                cube.setFace(facing, RenderBoxFace.NOT_RENDER);
            
            cube.customData = tile;
        } else
            cube.setFace(facing, RenderBoxFace.RENDER);
    }
    
    public IndexedCollector<LittleRenderBox> getRenderingBoxes(RenderingBlockContext context, RenderType layer) {
        IndexedCollector<LittleRenderBox> cachedCubes = boxCache.get(layer);
        if (cachedCubes != null)
            return cachedCubes;
        
        IndexedCollector<LittleRenderBox> boxes = new IndexedCollector<>();
        LittleServerFace serverFace = new LittleServerFace(be);
        
        for (IParentCollection parent : be.groups()) {
            if (parent instanceof IStructureParentCollection s)
                boxes.startSection(s.getIndex());
            else
                boxes.startSection(-1);
            
            for (LittleTile tile : parent) {
                if (!tile.canRenderInLayer(layer))
                    continue;
                
                for (LittleBox box : tile) {
                    box.hasOrCreateFaceState(parent, tile, serverFace);
                    
                    // Check for sides which does not need to be rendered
                    LittleRenderBox cube = parent.getRenderingBox(tile, box, layer);
                    if (cube == null)
                        continue;
                    
                    for (int k = 0; k < Facing.VALUES.length; k++)
                        calculateFaces(Facing.VALUES[k], cube.box.getFaceState(Facing.VALUES[k]), context, tile, box, cube);
                    
                    boxes.add(cube);
                }
                
            }
            
            if (LittleStructureAttribute.extraRendering(parent.getAttribute())) {
                try {
                    LittleStructure structure = parent.getStructure();
                    structure.checkConnections();
                    structure.getRenderingBoxes(be.getBlockPos(), layer, boxes);
                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                
            }
            
            boxes.endSection();
        }
        
        if (boxes.isEmpty())
            boxes = null;
        
        boxCache.put(layer, boxes);
        return boxes;
    }
    
    public RenderChunkExtender getRenderChunk() {
        return getRenderChunk(be.getLevel(), be.getBlockPos());
    }
}
