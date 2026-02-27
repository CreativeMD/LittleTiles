package team.creative.littletiles.common.action.cancel;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.source.LittleActionSource;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;

public class ActionCancelContext {
    
    public final LittleAction failed;
    private List<BlockPos> positions = new ArrayList<>();
    private boolean inventoryMarked = false;
    
    public ActionCancelContext(LittleAction failed) {
        this.failed = failed;
    }
    
    public void markBE(Iterable<BETiles> blockEntities) {
        for (BETiles be : blockEntities)
            positions.add(be.getBlockPos());
    }
    
    public void mark(Iterable<BlockPos> positions) {
        for (BlockPos pos : positions)
            this.positions.add(pos);
    }
    
    public void mark(BlockPos pos) {
        positions.add(pos);
    }
    
    public void mark(LittleBoxes boxes) {
        for (BlockPos pos : boxes.generateBlockWise().keySet())
            positions.add(pos);
    }
    
    public void markSourceInventory() {
        inventoryMarked = true;
    }
    
    public void complete(LittleActionSource source) {
        var level = source.getActionLevel();
        for (BlockPos pos : positions)
            LittleAction.sendBlockResetToClient(level, source, pos);
        if (inventoryMarked)
            source.requestInventoryUpdate();
    }
    
    public void mark(LittleGroupAbsolute group) {
        for (BlockPos pos : group.getPositions())
            positions.add(pos);
    }
    
}
