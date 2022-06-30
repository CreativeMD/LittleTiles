package team.creative.littletiles.client.render.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.face.CachedFaceRenderType;
import team.creative.creativecore.client.render.face.FaceRenderType;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.utils.BooleanUtils;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.client.render.cache.LayeredRenderBufferCache;
import team.creative.littletiles.client.render.cache.RenderingBlockContext;
import team.creative.littletiles.client.render.cache.RenderingThread;
import team.creative.littletiles.client.render.level.LittleChunkDispatcher;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.face.LittleFace;
import team.creative.littletiles.common.math.face.LittleFaceState;
import team.creative.littletiles.common.math.face.LittleFaces.LittleFaceSideCache;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.attribute.LittleStructureAttribute;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

@OnlyIn(Dist.CLIENT)
public class BERenderManager {
    
    private BETiles be;
    
    private int requestedIndex = -1;
    private int renderState = -1;
    
    private boolean queued = false;
    private boolean eraseBoxCache = false;
    
    public boolean hasLightChanged = false;
    private boolean[] neighbourChanged = new boolean[Facing.VALUES.length];
    
    private double cachedRenderDistance = 0;
    private AABB cachedRenderBoundingBox = null;
    private boolean requireRenderingBoundingBoxUpdate = false;
    
    private final LayeredRenderBufferCache bufferCache = new LayeredRenderBufferCache();
    public final HashMap<RenderType, List<LittleRenderBox>> boxCache = new HashMap<>();
    
    public BERenderManager(BETiles be) {
        this.be = be;
    }
    
    public void setTe(BETiles be) {
        this.be = be;
    }
    
    public boolean isInQueue() {
        return queued;
    }
    
    public void chunkUpdate(Object chunk) {
        synchronized (this) {
            boolean doesNeedUpdate = BooleanUtils.any(neighbourChanged) || hasLightChanged || requestedIndex == -1;
            if (renderState != LittleChunkDispatcher.currentRenderState) {
                eraseBoxCache = true;
                doesNeedUpdate = true;
            }
            
            hasLightChanged = false;
            BooleanUtils.reset(neighbourChanged);
            
            if (doesNeedUpdate)
                queue(eraseBoxCache);
        }
    }
    
    public void tilesChanged() {
        requireRenderingBoundingBoxUpdate = true;
        cachedRenderDistance = 0;
        queue(true);
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
    
    public void onNeighbourChanged(Facing facing) {
        neighbourChanged[facing.ordinal()] = true;
        queue(false);
    }
    
    public void queue(boolean eraseBoxCache) {
        synchronized (this) {
            requestedIndex++;
            
            this.eraseBoxCache |= eraseBoxCache;
            
            if (!queued && RenderingThread.queue(be))
                queued = true;
        }
    }
    
    public int startBuildingCache() {
        synchronized (this) {
            if (eraseBoxCache) {
                boxCache.clear();
                eraseBoxCache = false;
            }
            return requestedIndex;
        }
        
    }
    
    public boolean finishBuildingCache(int index, int renderState, boolean force) {
        synchronized (this) {
            this.renderState = renderState;
            boolean done = force || (index == requestedIndex && this.renderState == renderState);
            if (done)
                queued = false;
            this.hasLightChanged = false;
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
    
    public LayeredRenderBufferCache getBufferCache() {
        return bufferCache;
    }
    
    public void beforeBuilding(RenderingBlockContext context) {
        for (int k = 0; k < neighbourChanged.length; k++) {
            if (neighbourChanged[k]) {
                neighbourChanged[k] = false;
                Facing facing = Facing.VALUES[k];
                Direction direction = Direction.values()[k];
                BlockPos neighbourPos = be.getBlockPos().relative(direction);
                Level level = be.getLevel();
                BlockState neighbourState = level.getBlockState(neighbourPos);
                BETiles neighbourBE = BlockTile.loadBE(level, neighbourPos);
                
                for (Entry<RenderType, List<LittleRenderBox>> entry : boxCache.entrySet()) {
                    List<LittleRenderBox> renderCubes = entry.getValue();
                    if (renderCubes == null)
                        continue;
                    
                    for (int i = 0; i < renderCubes.size(); i++) {
                        LittleRenderBox cube = renderCubes.get(i);
                        
                        if (cube.getType(facing).isOutside()) {
                            boolean shouldRenderBefore = cube.renderSide(facing);
                            
                            if (neighbourState.skipRendering(cube.state, direction)) {
                                cube.setType(facing, FaceRenderType.OUTSIDE_NOT_RENDERD);
                                continue;
                            } else if (neighbourBE == null)
                                cube.setType(facing, FaceRenderType.OUTSIDE_RENDERED);
                            else {
                                LittleFace face = cube.box.generateFace(be.getGrid(), facing);
                                if (face == null)
                                    cube.setType(facing, FaceRenderType.OUTSIDE_NOT_RENDERD);
                                else {
                                    face.move(facing);
                                    LittleTile tile = (LittleTile) cube.customData;
                                    if (neighbourBE.shouldFaceBeRendered(facing.opposite(), face, tile)) {
                                        if (tile.isTranslucent() && face.isPartiallyFilled())
                                            cube.setType(facing, new CachedFaceRenderType(face.generateFans(), (float) face.grid.pixelLength, true, true));
                                        else
                                            cube.setType(facing, FaceRenderType.OUTSIDE_RENDERED);
                                    } else
                                        cube.setType(facing, FaceRenderType.OUTSIDE_NOT_RENDERD);
                                }
                            }
                            
                            if (cube.renderSide(facing)) {
                                if (!shouldRenderBefore)
                                    cube.doesNeedQuadUpdate = true;
                            } else
                                cube.setQuad(facing, null);
                        }
                    }
                }
            }
        }
    }
    
    public List<LittleRenderBox> getRenderingBoxes(RenderingBlockContext context, RenderType layer) {
        List<LittleRenderBox> cachedCubes = boxCache.get(layer);
        if (cachedCubes != null)
            return cachedCubes;
        
        List<LittleRenderBox> boxes = new ArrayList<>();
        
        be.faces.resetReader();
        LittleFaceSideCache faceCache = new LittleFaceSideCache();
        
        for (Pair<IParentCollection, LittleTile> pair : be.allTiles()) {
            LittleTile tile = pair.value;
            
            if (!tile.getBlock().canRenderInLayer(tile, layer)) {
                be.faces.jumpReader(tile.size());
                continue;
            }
            
            for (LittleBox box : tile) {
                be.faces.pull(faceCache);
                
                // Check for sides which does not need to be rendered
                LittleRenderBox cube = pair.key.getRenderingBox(tile, box, layer);
                if (cube == null)
                    continue;
                for (int k = 0; k < Facing.VALUES.length; k++) {
                    Facing facing = Facing.VALUES[k];
                    LittleFaceState state = faceCache.get(facing);
                    
                    if (state.outside())
                        cube.customData = tile;
                    
                    if (state.coveredFully()) {
                        cube.setType(facing, state.outside() ? FaceRenderType.OUTSIDE_NOT_RENDERD : FaceRenderType.INSIDE_NOT_RENDERED);
                        continue;
                    }
                    
                    if (state.outside()) {
                        if (tile.isTranslucent() && state.partially())
                            setFaceOutside(context, tile, facing, cube, cube.box.generateFace(be.getGrid(), facing));
                        else
                            cube.setType(facing, FaceRenderType.OUTSIDE_RENDERED);
                        
                    } else {
                        if (tile.isTranslucent() && state.partially()) {
                            LittleFace face = cube.box.generateFace(be.getGrid(), facing);
                            if (be.shouldFaceBeRendered(facing, face, tile))
                                cube.setType(facing, new CachedFaceRenderType(face.generateFans(), (float) face.grid.pixelLength, true, false));
                            else
                                cube.setType(facing, FaceRenderType.OUTSIDE_NOT_RENDERD);
                        } else
                            cube.setType(facing, FaceRenderType.INSIDE_RENDERED);
                    }
                }
                boxes.add(cube);
            }
            
        }
        
        for (LittleStructure structure : be.loadedStructures(LittleStructureAttribute.EXTRA_RENDERING)) {
            try {
                structure.checkConnections();
                structure.getRenderingBoxes(be.getBlockPos(), layer, boxes);
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
            
        }
        
        boxCache.put(layer, boxes);
        return boxes;
    }
    
    private static void setFaceOutside(RenderingBlockContext context, LittleTile tile, Facing facing, RenderBox cube, LittleFace face) {
        BETiles neighbour = context.getNeighbour(facing);
        face.move(facing);
        if (neighbour.shouldFaceBeRendered(facing, face, tile))
            cube.setType(facing, new CachedFaceRenderType(face.generateFans(), (float) face.grid.pixelLength, true, false));
        else
            cube.setType(facing, FaceRenderType.OUTSIDE_NOT_RENDERD);
    }
}
