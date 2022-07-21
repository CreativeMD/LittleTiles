package team.creative.littletiles.common.block.little.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.mc.TickUtils;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.math.box.LittleBox;

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
    public static LittleTileContext selectFocused(BlockGetter level, BlockPos pos, Player player) {
        return selectFocused(level, pos, player, TickUtils.getDeltaFrameTime(player.level));
    }
    
    @OnlyIn(Dist.CLIENT)
    public static LittleTileContext selectFocused(BlockGetter level, BlockPos pos, Player player, float partialTickTime) {
        BETiles te = BlockTile.loadBE(level, pos);
        if (te != null) {
            LittleTileContext context = te.getFocusedTile(player, partialTickTime);
            if (context != null)
                return context;
        }
        return FAILED;
    }
}
