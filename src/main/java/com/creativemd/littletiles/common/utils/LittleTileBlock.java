package com.creativemd.littletiles.common.utils;

import java.util.ArrayList;
import java.util.Random;

import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.littletiles.LittleTiles;

import net.minecraft.block.Block;
import net.minecraft.block.Block.SoundType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class LittleTileBlock extends LittleTile{
	
	public Block block;
	public int meta;
	
	public LittleTileBlock(Block block, int meta)
	{
		super();
		this.block = block;
		this.meta = meta;
	}
	
	public LittleTileBlock(Block block)
	{
		this(block, 0);
	}
	
	public LittleTileBlock()
	{
		super();
	}
	
	@Override
	public void saveTileExtra(NBTTagCompound nbt) {
		nbt.setString("block", Block.blockRegistry.getNameForObject(block));
		nbt.setInteger("meta", meta);		
	}

	@Override
	public void loadTileExtra(NBTTagCompound nbt) {
		block = Block.getBlockFromName(nbt.getString("block"));
		meta = nbt.getInteger("meta");
	}

	@Override
	public ForgeDirection[] getValidRotation() {
		return null;
	}

	@Override
	public void copyExtra(LittleTile tile) {
		LittleTileBlock thisTile = (LittleTileBlock) tile;
		thisTile.block = block;
		thisTile.meta = meta;
	}

	@Override
	public ItemStack getDrop() {
		ItemStack stack = new ItemStack(LittleTiles.blockTile);
		stack.stackTagCompound = new NBTTagCompound();
		saveTileExtra(stack.stackTagCompound);
		return stack;
	}

	@Override
	public ArrayList<CubeObject> getRenderingCubes() {
		ArrayList<CubeObject> cubes = new ArrayList<CubeObject>();
		for (int i = 0; i < boundingBoxes.size(); i++) {
			CubeObject cube = boundingBoxes.get(i).getCube();
			cube.block = block;
			cube.meta = meta;
			cubes.add(cube);
		}
		return cubes;
	}
	
	@Override
	public void onPlaced(EntityPlayer player, ItemStack stack)
	{
		super.onPlaced(player, stack);
		block.onBlockPlacedBy(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord, player, stack);
		block.onPostBlockPlaced(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord, meta);
	}

	@Override
	public SoundType getSound() {
		return block.stepSound;
	}

	@Override
	public IIcon getIcon(int side) {
		return block.getIcon(side, meta);
	}
	
	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random random)
	{
		block.randomDisplayTick(world, x, y, z, random);
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float moveX, float moveY, float moveZ) {
		return block.onBlockActivated(world, x, y, z, player, side, moveX, moveY, moveZ);
	}
	
	@Override
	public void place()
	{
		super.place();
		block.onBlockAdded(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord);
	}
	
	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z) {
		int light = block.getLightValue(world, x, y, z);
		if(light == 0)
			return block.getLightValue();
		return light;
	}
	
	@Override
	public double getEnchantPowerBonus(World world, int x, int y, int z) {
		return block.getEnchantPowerBonus(world, x, y, z);
	}
	
}
