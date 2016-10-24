package com.creativemd.littletiles.common.utils;

import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.common.utils.RenderCubeObject;
import com.creativemd.littletiles.LittleTiles;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleTileBlock extends LittleTile{
	
	public Block block;
	public int meta;
	
	@SideOnly(Side.CLIENT)
	private boolean translucent;
	private IBlockState state = null;
	
	public IBlockState getBlockState()
	{
		if(state == null)
		{
			state = block.getStateFromMeta(meta);
			if(state == null)
				state = block.getDefaultState();
		}
		return state;
	}
	
	public LittleTileBlock(Block block, int meta)
	{
		super();
		this.block = block;
		this.meta = meta;
		if(FMLCommonHandler.instance().getSide().isClient())
			updateClient();
	}
	
	public LittleTileBlock(Block block)
	{
		this(block, 0);
	}
	
	public LittleTileBlock()
	{
		super();
	}
	
	public void updateClient()
	{
		updateTranslucent();
	}
	
	@SideOnly(Side.CLIENT)
	public void updateTranslucent()
	{
		translucent = block.canRenderInLayer(getBlockState(), BlockRenderLayer.TRANSLUCENT) || block.canRenderInLayer(getBlockState(), BlockRenderLayer.CUTOUT);
	}
	
	@Override
	public void saveTileExtra(NBTTagCompound nbt) {
		nbt.setString("block", Block.REGISTRY.getNameForObject(block).toString());
		nbt.setInteger("meta", meta);
	}

	@Override
	public void loadTileExtra(NBTTagCompound nbt) {
		block = Block.getBlockFromName(nbt.getString("block"));
		meta = nbt.getInteger("meta");
		if(block == null || block instanceof BlockAir){
			//System.out.println("Invalid block name! name=" + nbt.getString("block"));
			//throw new IllegalArgumentException("Invalid block name! name=" + nbt.getString("block"));
		}
		if(te.isClientSide())
			updateClient();
	}

	/*@Override
	public ForgeDirection[] getValidRotation() {
		return null;
	}*/

	@Override
	public void copyExtra(LittleTile tile) {
		if(tile instanceof LittleTileBlock)
		{
			LittleTileBlock thisTile = (LittleTileBlock) tile;
			thisTile.block = block;
			thisTile.meta = meta;
			if(FMLCommonHandler.instance().getSide().isClient())
				thisTile.translucent = translucent;
		}
	}

	@Override
	public ItemStack getDrop() {
		ItemStack stack = new ItemStack(LittleTiles.blockTile);
		stack.setTagCompound(new NBTTagCompound());
		//saveTile(stack.getTagCompound());
		stack.getTagCompound().setString("tID", getID());
		stack.getTagCompound().setInteger("bSize", 0);
		saveTileExtra(stack.getTagCompound());
		boundingBoxes.get(0).getSize().writeToNBT("size", stack.getTagCompound());
		return stack;
	}

	@Override
	public ArrayList<RenderCubeObject> getRenderingCubes() {
		ArrayList<RenderCubeObject> cubes = new ArrayList<>();
		for (int i = 0; i < boundingBoxes.size(); i++) {
			cubes.add(new RenderCubeObject(boundingBoxes.get(i).getCube(), block, meta));
		}
		return cubes;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldBeRenderedInLayer(BlockRenderLayer layer)
	{
		if(translucent)
			return layer == BlockRenderLayer.TRANSLUCENT;
		return block.canRenderInLayer(getBlockState(), layer);
	}
	
	@Override
	public void onPlaced(EntityPlayer player, ItemStack stack, EnumFacing facing)
	{
		super.onPlaced(player, stack, facing);
		try{
			block.onBlockPlacedBy(te.getWorld(), te.getPos(), getBlockState(), player, stack);
			//block.onPostBlockPlaced(te.getWorld(), te.getPos(), getBlockState());
		}catch(Exception e){
			
		}
	}

	@Override
	public SoundType getSound() {
		return block.getSoundType();
	}

	/*@Override
	public IIcon getIcon(int side) {
		return block.getIcon(side, meta);
	}*/
	
	@Override
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
	{
		block.randomDisplayTick(getBlockState(), worldIn, pos, rand);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if(super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ))
			return true;
		return block.onBlockActivated(worldIn, pos, getBlockState(), playerIn, hand, heldItem, side, hitX, hitY, hitZ);
	}
	
	@Override
	public void place()
	{
		super.place();
		block.onBlockAdded(te.getWorld(), te.getPos(), getBlockState());
	}
	
	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		//int light = block.getLightValue(world, x, y, z);
		//if(light == 0)
		return block.getLightValue(getBlockState());
		//return light;
	}
	
	@Override
	public float getEnchantPowerBonus(World world, BlockPos pos) {
		return (float) block.getEnchantPowerBonus(world, pos);
	}

	@Override
	public boolean canBeCombined(LittleTile tile) {
		if(super.canBeCombined(tile) && tile instanceof LittleTileBlock)
		{
			return block == ((LittleTileBlock) tile).block && meta == ((LittleTileBlock) tile).meta;
		}
		return false;
	}
	
	@Override
	public boolean doesProvideSolidFace(EnumFacing facing) {
		return !translucent;
	}

	@Override
	public boolean canBeRenderCombined(LittleTile tile) {
		if(tile instanceof LittleTileBlock)
			return block == ((LittleTileBlock) tile).block && meta == ((LittleTileBlock) tile).meta;// && ((LittleTileBlock) tile).translucent == translucent;
		return false;
	}

	/*@Override
	public boolean canBlockBeThreaded() {
		//return false;
		return block.getRenderType() == 0 && !(block instanceof BlockGrass);
	}*/
	
	@Override
	protected boolean canSawResize(EnumFacing facing, EntityPlayer player) {
		return true;
	}

	@Override
	public float getExplosionResistance() {
		return block.getExplosionResistance(null);
	}
	
}
