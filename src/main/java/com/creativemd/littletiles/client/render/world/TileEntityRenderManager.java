package com.creativemd.littletiles.client.render.world;

import com.creativemd.littletiles.client.render.cache.LayeredRenderBoxCache;
import com.creativemd.littletiles.client.render.cache.LayeredRenderBufferCache;
import com.creativemd.littletiles.client.render.cache.RenderingThread;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TileEntityRenderManager {
    
    private TileEntityLittleTiles te;
    
    private int requestedIndex = -1;
    private int renderState = -1;
    
    private boolean queued = false;
    private boolean building = false;
    private boolean eraseBoxCache = false;
    
    public boolean hasLightChanged = false;
    public boolean hasNeighbourChanged = false;
    
    private double cachedRenderDistance = 0;
    private AxisAlignedBB cachedRenderBoundingBox = null;
    private boolean requireRenderingBoundingBoxUpdate = false;
    
    private final LayeredRenderBufferCache bufferCache = new LayeredRenderBufferCache();
    private final LayeredRenderBoxCache boxCache = new LayeredRenderBoxCache();
    
    public TileEntityRenderManager(TileEntityLittleTiles te) {
        this.te = te;
    }
    
    public void setTe(TileEntityLittleTiles te) {
        this.te = te;
    }
    
    public boolean isInQueue() {
        return queued;
    }
    
    public void chunkUpdate(Object chunk) {
        synchronized (this) {
            boolean doesNeedUpdate = hasNeighbourChanged || hasLightChanged || requestedIndex == -1;
            boolean eraseBoxCache = false;
            if (renderState != LittleChunkDispatcher.currentRenderState) {
                eraseBoxCache = true;
                doesNeedUpdate = true;
            }
            
            hasLightChanged = false;
            hasNeighbourChanged = false;
            
            if (doesNeedUpdate)
                queue(eraseBoxCache);
        }
    }
    
    public boolean hasAdditional() {
        return bufferCache.hasAdditional();
    }
    
    public void beforeClientReceivesUpdate() {
        bufferCache.beforeUpdate();
    }
    
    public void afterClientReceivesUpdate() {
        bufferCache.afterUpdate();
    }
    
    public void tilesChanged() {
        requireRenderingBoundingBoxUpdate = true;
        cachedRenderDistance = 0;
        queue(true);
    }
    
    public double getMaxRenderDistanceSquared() {
        if (cachedRenderDistance == 0) {
            double renderDistance = 5 * 5; // 512 blocks
            for (LittleStructure structure : te.rendering())
                renderDistance = Math.max(renderDistance, structure.getMaxRenderDistanceSquared());
            cachedRenderDistance = renderDistance;
        }
        return cachedRenderDistance;
    }
    
    public AxisAlignedBB getRenderBoundingBox() {
        if (requireRenderingBoundingBoxUpdate || cachedRenderBoundingBox == null) {
            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double minZ = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE;
            double maxY = -Double.MAX_VALUE;
            double maxZ = -Double.MAX_VALUE;
            boolean found = false;
            for (LittleStructure structure : te.rendering()) {
                AxisAlignedBB box = structure.getRenderBoundingBox();
                if (box == null)
                    continue;
                box = box.offset(te.getPos());
                minX = Math.min(box.minX, minX);
                minY = Math.min(box.minY, minY);
                minZ = Math.min(box.minZ, minZ);
                maxX = Math.max(box.maxX, maxX);
                maxY = Math.max(box.maxY, maxY);
                maxZ = Math.max(box.maxZ, maxZ);
                found = true;
            }
            if (found)
                cachedRenderBoundingBox = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
            else
                cachedRenderBoundingBox = new AxisAlignedBB(te.getPos());
            
            requireRenderingBoundingBoxUpdate = false;
        }
        return cachedRenderBoundingBox;
    }
    
    public void neighborChanged() {
        hasNeighbourChanged = true;
        queue(false);
    }
    
    public void queue(boolean eraseBoxCache) {
        synchronized (this) {
            requestedIndex++;
            
            this.eraseBoxCache |= eraseBoxCache;
            
            if (!queued) {
                if (RenderingThread.addCoordToUpdate(te))
                    queued = true;
            }
        }
    }
    
    public int startBuildingCache() {
        synchronized (this) {
            if (eraseBoxCache) {
                boxCache.clear();
                eraseBoxCache = false;
            }
            building = true;
            return requestedIndex;
        }
    }
    
    public boolean finishBuildingCache(int index, int renderState, boolean force) {
        synchronized (this) {
            this.building = false;
            this.renderState = renderState;
            boolean done = force || (index >= requestedIndex && this.renderState == renderState);
            if (done)
                queued = false;
            this.hasLightChanged = false;
            this.hasNeighbourChanged = false;
            return done;
        }
    }
    
    public void resetRenderingState() {
        synchronized (this) {
            queued = false;
            building = false;
            requestedIndex = -1;
        }
    }
    
    public void chunkUnload() {
        synchronized (this) {
            bufferCache.setEmpty();
            boxCache.clear();
            cachedRenderBoundingBox = null;
        }
    }
    
    public LayeredRenderBoxCache getBoxCache() {
        return boxCache;
    }
    
    public LayeredRenderBufferCache getBufferCache() {
        return bufferCache;
    }
    
}
