package team.creative.littletiles.common.api.tool;

import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.util.place.PlacementPosition;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public interface ILittleEditor extends ILittleTool {
    
    public boolean hasCustomBoxes(World world, ItemStack stack, PlayerEntity player, BlockState state, PlacementPosition pos, RayTraceResult result);
    
    /** @return a list of absolute LittleTileBoxes (not relative to the pos) */
    public LittleBoxes getBoxes(World world, ItemStack stack, PlayerEntity player, PlacementPosition pos, RayTraceResult result);
    
}