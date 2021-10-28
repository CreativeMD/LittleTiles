package team.creative.littletiles.common.api.tool;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;

public interface ILittlePlacer extends ILittleTool {
    
    public boolean hasTiles(ItemStack stack);
    
    public LittleGroup getTiles(ItemStack stack);
    
    public PlacementPreview getPlacement(Level level, ItemStack stack, PlacementPosition position, boolean allowLowResolution);
    
    public void saveTiles(ItemStack stack, LittleGroup group);
    
    @Override
    public default void rotate(Player player, ItemStack stack, Rotation rotation, boolean client) {
        LittleGroup group = getTiles(stack);
        if (group == null || group.isEmpty())
            return;
        group.rotate(rotation, group.getGrid().rotationCenter);
        saveTiles(stack, group);
    }
    
    @Override
    public default void mirror(Player player, ItemStack stack, Axis axis, boolean client) {
        LittleGroup group = getTiles(stack);
        if (group == null || group.isEmpty())
            return;
        group.mirror(axis, group.getGrid().rotationCenter);
        saveTiles(stack, group);
    }
    
    public default LittleGrid getTilesGrid(ItemStack stack) {
        if (stack.hasTag())
            return LittleGrid.get(stack.getTag());
        return LittleGrid.defaultGrid();
    }
    
    public boolean containsIngredients(ItemStack stack);
    
    @OnlyIn(Dist.CLIENT)
    public default float getPreviewAlphaFactor() {
        return 1;
    }
    
    @OnlyIn(Dist.CLIENT)
    public default boolean shouldCache() {
        return true;
    }
    
    public default PlacementMode getPlacementMode(ItemStack stack) {
        if (stack.hasTag())
            return PlacementMode.getMode(stack.getTag().getString("mode"));
        return PlacementMode.REGISTRY.getDefault();
    }
    
    public default boolean snapToGridByDefault(ItemStack stack) {
        return false;
    }
    
    /** needs to be implemented by any ILittleTile which supports low resolution and
     * only uses full blocks
     * 
     * @param stack
     * @return */
    public default LittleVec getCachedSize(ItemStack stack) {
        return null;
    }
    
    /** needs to be implemented by any ILittleTile which supports low resolution and
     * only uses full blocks
     * 
     * @param stack
     * @return */
    public default LittleVec getCachedOffset(ItemStack stack) {
        return null;
    }
    
    @OnlyIn(Dist.CLIENT)
    public default List<RenderBox> getPositingCubes(Level level, BlockPos pos, ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("structure")) {
            LittleStructureType type = LittleStructureRegistry.getStructureType(stack.getTag().getCompound("structure").getString("id"));
            if (type != null)
                return type.getPositingCubes(level, pos, stack);
        }
        return null;
    }
    
}
