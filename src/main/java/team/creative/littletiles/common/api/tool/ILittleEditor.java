package team.creative.littletiles.common.api.tool;

import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.util.place.PlacementPosition;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

public interface ILittleEditor extends ILittleTool {
    
    public boolean hasCustomBoxes(Level level, ItemStack stack, Player player, BlockState state, PlacementPosition pos, HitResult result);
    
    /** @return a list of absolute LittleTileBoxes (not relative to the pos) */
    public LittleBoxes getBoxes(Level level, ItemStack stack, Player player, PlacementPosition pos, HitResult result);
    
}