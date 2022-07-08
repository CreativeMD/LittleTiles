package team.creative.littletiles.common.level;

import java.util.Collection;
import java.util.HashSet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.creative.creativecore.common.level.NeighborUpdateCollector;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.mc.BlockTile;

public class LittleNeighborUpdateCollector extends NeighborUpdateCollector {
    
    public static final LittleNeighborUpdateCollector EMPTY = new LittleNeighborUpdateCollector(null) {
        
        @Override
        public void add(BlockPos pos) {}
        
        @Override
        public void add(BlockEntity te) {}
        
        @Override
        public void add(Collection<BlockPos> positions) {}
        
        @Override
        protected void processPosition(BlockPos pos, HashSet<BlockPos> notifiedBlocks) {}
        
        @Override
        public void process() {}
    };
    
    public LittleNeighborUpdateCollector(Level level, Collection<BlockPos> positions) {
        super(level, positions);
    }
    
    public LittleNeighborUpdateCollector(Level level) {
        super(level);
    }
    
    @Override
    protected void processPosition(BlockPos pos, HashSet<BlockPos> notifiedBlocks) {
        BETiles be = BlockTile.loadBE(level, pos);
        if (be != null)
            be.updateTiles(false);
        else
            super.processPosition(pos, notifiedBlocks);
    }
    
}
