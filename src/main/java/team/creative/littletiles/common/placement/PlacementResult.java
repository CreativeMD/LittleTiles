package team.creative.littletiles.common.placement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.ParentCollection;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.box.collection.LittleBoxesSimple;
import team.creative.littletiles.common.structure.LittleStructure;

public class PlacementResult {
    
    public final BlockPos pos;
    public final LittleGroup placedPreviews;
    public final LittleBoxes placedBoxes;
    private BlockPos lastPos = null;
    public final List<BETiles> blocks = new ArrayList<>();
    public LittleStructure parentStructure;
    
    public PlacementResult(BlockPos pos) {
        this.pos = pos;
        this.placedPreviews = new LittleGroup();
        this.placedBoxes = new LittleBoxesSimple(pos, LittleGrid.MIN);
    }
    
    public void addPlacedTile(ParentCollection parent, LittleTile tile) {
        if (lastPos == null || !lastPos.equals(parent.getPos())) {
            lastPos = parent.getPos();
            blocks.add(parent.getBE());
        }
        placedPreviews.add(parent.getGrid(), tile, tile.copy());
        placedBoxes.addBoxes(parent, tile);
    }
    
    public void broadcastChangesImmediately(ServerLevel level) {
        HashMap<LevelChunk, ChunkHolder> map = new HashMap<>();
        for (BETiles beTiles : blocks) {
            var chunk = level.getChunk(beTiles.getBlockPos());
            if (chunk instanceof LevelChunk l && !map.containsKey(l))
                map.put(l, level.getChunkSource().chunkMap.getVisibleChunkIfPresent(l.getPos().toLong()));
        }
        
        for (Entry<LevelChunk, ChunkHolder> entry : map.entrySet())
            entry.getValue().broadcastChanges(entry.getKey());
    }
    
}
