package com.creativemd.littletiles.common.entity;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.littletiles.client.render.RenderingThread;
import com.creativemd.littletiles.client.render.entity.TERenderData;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.utils.PlacePreviewTile;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityAnimation extends Entity {
	
	public LittleStructure structure;
	public ArrayList<PlacePreviewTile> previews;
	public ArrayList<TileEntityLittleTiles> blocks;
	
	@SideOnly(Side.CLIENT)
	public HashMapList<BlockRenderLayer, TERenderData> renderData;
	
	@SideOnly(Side.CLIENT)
	public ArrayList<TileEntityLittleTiles> renderQueue;
	
	public int progress;
	
	public int duration;

	public EntityAnimation(World worldIn) {
		super(worldIn);
	}
	
	public EntityAnimation(World world, LittleStructure structure, ArrayList<TileEntityLittleTiles> blocks, ArrayList<PlacePreviewTile> previews, UUID uuid) {
		this(world);
		this.blocks = blocks;
		this.entityUniqueID = uuid;
        this.cachedUniqueIdString = this.entityUniqueID.toString();
        
        this.duration = 1000;
        
        if(world.isRemote)
        	createClient();
	}
	
	@SideOnly(Side.CLIENT)
	public void createClient()
	{
		this.renderData = new HashMapList<>();
		this.renderQueue = new ArrayList<>(blocks);
		for (Iterator<TileEntityLittleTiles> iterator = blocks.iterator(); iterator.hasNext();) {
			TileEntityLittleTiles te = iterator.next();
			te.rendering = new AtomicBoolean(false);
			RenderingThread.addCoordToUpdate(te, 0, false);
		}
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		if(blocks == null)
			setDead();
		if(progress >= duration)
		{
			
			//Try to place door, if not drop ItemStack
			
			
			setDead();
		}else
			progress++;
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
