package team.creative.littletiles.common.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.littletiles.common.block.BlockTile;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.tile.parent.IParentCollection;

public class LittleTileContext {
    
    public static final LittleTileContext FAILED = new LittleTileContext(null, null, null) {
        @Override
        public boolean isComplete() {
            return false;
        }
    };
    
    public final IParentCollection parent;
    public final LittleTile tile;
    public final LittleBox box;
    
    public LittleTileContext(IParentCollection parent, LittleTile tile, LittleBox box) {
        this.parent = parent;
        this.tile = tile;
        this.box = box;
    }
    
    public boolean isComplete() {
        return true;
    }
    
    @OnlyIn(Dist.CLIENT)
    public static LittleTileContext selectFocused(LevelAccessor world, BlockPos pos, Player player) {
        return loadTeAndTile(world, pos, player, TickUtils.getPartialTickTime());
    }
    
    @OnlyIn(Dist.CLIENT)
    public static LittleTileContext selectFocused(LevelAccessor world, BlockPos pos, Player player, float partialTickTime) {
        BETiles te = BlockTile.loadBE(world, pos);
        if (te != null) {
            LittleTileContext context = te.getFocusedTile(player, partialTickTime);
            if (context != null)
                return context;
        }
        return FAILED;
    }
}
