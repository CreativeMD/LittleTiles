package team.creative.littletiles.api.common.tool;

import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.matrix.IntMatrix3;
import team.creative.creativecore.common.util.math.matrix.IntMatrix3c;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.item.component.MatrixDataComponent;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.placement.mode.PlacementMode;

public interface ILittlePlacer extends ILittleTool {
    
    public boolean hasTiles(ItemStack stack);
    
    public LittleGroup getTiles(ItemStack stack);
    
    public LittleGroup getLow(ItemStack stack);
    
    public default LittleGroup get(ItemStack stack, boolean low) {
        if (low)
            return getLow(stack);
        return getTiles(stack);
    }
    
    public default IntMatrix3c getMatrix(ItemStack stack) {
        if (stack.has(LittleTilesRegistry.MATRIX))
            return stack.get(LittleTilesRegistry.MATRIX).getMatrix();
        return IntMatrix3c.IDENTIY;
    }
    
    public default void transformMatrix(ItemStack stack, IntMatrix3c matrix) {
        var m = new IntMatrix3(matrix, getMatrix(stack));
        if (m.isIdentity())
            stack.remove(LittleTilesRegistry.MATRIX);
        else
            stack.set(LittleTilesRegistry.MATRIX, MatrixDataComponent.of(m));
    }
    
    public boolean containsIngredients(ItemStack stack);
    
    @OnlyIn(Dist.CLIENT)
    public default PlacementMode getPlacementMode(ItemStack stack) {
        return LittleTilesClient.ACTION_HANDLER.setting.placementMode();
    }
    
    public default boolean canSnapToGrid(ItemStack stack) {
        return true;
    }
    
    public default boolean snapToGridByDefault(ItemStack stack) {
        return false;
    }
    
    /** needs to be implemented by any ILittleTile which supports low resolution and
     * only uses full blocks
     * 
     * @param stack
     * @return */
    public default LittleVecGrid getCachedSize(ItemStack stack) {
        return null;
    }
    
    /** needs to be implemented by any ILittleTile which supports low resolution and
     * only uses full blocks
     * 
     * @param stack
     * @return */
    public default LittleVecGrid getCachedMin(ItemStack stack) {
        return null;
    }
    
}
