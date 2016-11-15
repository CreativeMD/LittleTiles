package com.creativemd.littletiles.common.entity;

import java.util.ArrayList;
import java.util.HashMap;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.utils.PlacePreviewTile;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityAnimation extends Entity {
	
	public LittleStructure structure;
	public ArrayList<PlacePreviewTile> previews;
	
	@SideOnly(Side.CLIENT)
	public HashMap<BlockRenderLayer, ArrayList<BakedQuad>> quads;

	public EntityAnimation(World worldIn) {
		super(worldIn);
	}

	@Override
	protected void entityInit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		// TODO Auto-generated method stub
		
	}
	
	

}
