package com.creativemd.littletiles.common.utils;

import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class LittleTileTileEntity extends LittleTile{
	
	public LittleTileTileEntity()
	{
		
	}
	
	public LittleTileTileEntity(TileEntity tileEntity)
	{
		this.tileEntity = tileEntity;
	}
	
	public TileEntity tileEntity;
	
	@Override
	public void load(NBTTagCompound nbt)
	{
		super.load(nbt);
		tileEntity = TileEntity.createAndLoadEntity(nbt);
		if(tileEntity.isInvalid())
			setInValid();
	}
	
	@Override
	public void save(NBTTagCompound nbt)
	{
		super.save(nbt);
		if(tileEntity != null)
			tileEntity.writeToNBT(nbt);
	}
	
	@Override
	public void updateEntity()
	{
		if(tileEntity != null)
			tileEntity.updateEntity();
	}
	
	@Override
	public void onPlaced(ItemStack stack, TileEntityLittleTiles tileEntity)
	{
		super.onPlaced(stack, tileEntity);
		
	}
}
