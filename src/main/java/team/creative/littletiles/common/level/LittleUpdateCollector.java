package team.creative.littletiles.common.level;

import java.util.Collection;
import java.util.HashSet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.level.NeighborUpdateCollector;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.mc.BlockTile;

public class LittleUpdateCollector extends NeighborUpdateCollector {
    
    public LittleUpdateCollector(Level level, Collection<BlockPos> positions) {
        super(level, positions);
    }
    
    public LittleUpdateCollector() {
        super();
    }
    
    @Override
    protected void processPosition(Level level, BlockPos pos, HashSet<BlockPos> notifiedBlocks) {
        BETiles be = BlockTile.loadBE(level, pos);
        if (be != null)
            be.updateTiles(false);
        else
            super.processPosition(level, pos, notifiedBlocks);
    }
    
}
