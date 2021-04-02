package com.creativemd.littletiles.common.api;

import java.util.List;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.client.render.tile.LittleRenderBox;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.place.PlacementMode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ILittlePlacer extends ILittleTool {
    
    public boolean hasLittlePreview(ItemStack stack);
    
    public LittlePreviews getLittlePreview(ItemStack stack);
    
    public default LittlePreviews getLittlePreview(ItemStack stack, boolean allowLowResolution) {
        return getLittlePreview(stack);
    }
    
    public void saveLittlePreview(ItemStack stack, LittlePreviews previews);
    
    @Override
    public default void rotate(EntityPlayer player, ItemStack stack, Rotation rotation, boolean client) {
        LittlePreviews previews = getLittlePreview(stack, false);
        if (previews.isEmpty())
            return;
        previews.rotatePreviews(rotation, previews.getContext().rotationCenter);
        saveLittlePreview(stack, previews);
    }
    
    @Override
    public default void flip(EntityPlayer player, ItemStack stack, Axis axis, boolean client) {
        LittlePreviews previews = getLittlePreview(stack, false);
        if (previews.isEmpty())
            return;
        previews.flipPreviews(axis, previews.getContext().rotationCenter);
        saveLittlePreview(stack, previews);
    }
    
    public default LittleGridContext getPreviewsContext(ItemStack stack) {
        if (stack.hasTagCompound())
            return LittleGridContext.get(stack.getTagCompound());
        return LittleGridContext.get();
    }
    
    public boolean containsIngredients(ItemStack stack);
    
    @SideOnly(Side.CLIENT)
    public default float getPreviewAlphaFactor() {
        return 1;
    }
    
    @SideOnly(Side.CLIENT)
    public default boolean shouldCache() {
        return true;
    }
    
    public default PlacementMode getPlacementMode(ItemStack stack) {
        if (stack.hasTagCompound())
            return PlacementMode.getModeOrDefault(stack.getTagCompound().getString("mode"));
        return PlacementMode.getDefault();
    }
    
    public default boolean snapToGridByDefault() {
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
    
    @SideOnly(Side.CLIENT)
    public default List<LittleRenderBox> getPositingCubes(World world, BlockPos pos, ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("structure")) {
            LittleStructureType type = LittleStructureRegistry.getStructureType(stack.getTagCompound().getCompoundTag("structure").getString("id"));
            if (type != null)
                return type.getPositingCubes(world, pos, stack);
        }
        return null;
    }
    
}
