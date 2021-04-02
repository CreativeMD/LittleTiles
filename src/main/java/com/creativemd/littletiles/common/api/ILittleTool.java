package com.creativemd.littletiles.common.api;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.client.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.common.container.SubContainerConfigure;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.place.IMarkMode;
import com.creativemd.littletiles.common.util.place.MarkMode;
import com.creativemd.littletiles.common.util.place.PlacementPosition;
import com.creativemd.littletiles.common.util.place.PlacementPreview;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ILittleTool {
    
    @SideOnly(Side.CLIENT)
    public default LittleGridContext getPositionContext(ItemStack stack) {
        return LittleGridContext.get();
    }
    
    public void rotate(EntityPlayer player, ItemStack stack, Rotation rotation, boolean client);
    
    public void flip(EntityPlayer player, ItemStack stack, Axis axis, boolean client);
    
    @SideOnly(Side.CLIENT)
    public default SubGuiConfigure getConfigureGUI(EntityPlayer player, ItemStack stack) {
        return null;
    }
    
    public default SubContainerConfigure getConfigureContainer(EntityPlayer player, ItemStack stack) {
        return new SubContainerConfigure(player, stack);
    }
    
    @SideOnly(Side.CLIENT)
    public default SubGuiConfigure getConfigureGUIAdvanced(EntityPlayer player, ItemStack stack) {
        return null;
    }
    
    public default SubContainerConfigure getConfigureContainerAdvanced(EntityPlayer player, ItemStack stack) {
        return new SubContainerConfigure(player, stack);
    }
    
    public default boolean sendTransformationUpdate() {
        return true;
    }
    
    @SideOnly(Side.CLIENT)
    public default IMarkMode onMark(EntityPlayer player, ItemStack stack, PlacementPosition position, RayTraceResult result, PlacementPreview previews) {
        if (previews != null)
            return new MarkMode(player, position, previews);
        return null;
    }
    
    @SideOnly(Side.CLIENT)
    public default void tick(EntityPlayer player, ItemStack stack, PlacementPosition position, RayTraceResult result) {}
    
    @SideOnly(Side.CLIENT)
    public default void render(EntityPlayer player, ItemStack stack, double x, double y, double z) {}
    
    @SideOnly(Side.CLIENT)
    public default void onDeselect(World world, ItemStack stack, EntityPlayer player) {}
    
    @SideOnly(Side.CLIENT)
    public default boolean onRightClick(World world, EntityPlayer player, ItemStack stack, PlacementPosition position, RayTraceResult result) {
        return true;
    }
    
    @SideOnly(Side.CLIENT)
    public default void onClickAir(EntityPlayer player, ItemStack stack) {}
    
    @SideOnly(Side.CLIENT)
    public default boolean onClickBlock(World world, EntityPlayer player, ItemStack stack, PlacementPosition position, RayTraceResult result) {
        return false;
    }
    
    @SideOnly(Side.CLIENT)
    public default boolean onMouseWheelClickBlock(World world, EntityPlayer player, ItemStack stack, PlacementPosition position, RayTraceResult result) {
        return false;
    }
}
