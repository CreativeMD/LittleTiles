package com.creativemd.littletiles.common.api;

import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.util.place.PlacementPosition;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public interface ILittleEditor extends ILittleTool {
    
    public boolean hasCustomBoxes(World world, ItemStack stack, EntityPlayer player, IBlockState state, PlacementPosition pos, RayTraceResult result);
    
    /** @return a list of absolute LittleTileBoxes (not relative to the pos) */
    public LittleBoxes getBoxes(World world, ItemStack stack, EntityPlayer player, PlacementPosition pos, RayTraceResult result);
    
}