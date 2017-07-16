package com.creativemd.littletiles.common.utils;

import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Nullable;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.api.blocks.SpecialBlockHandler;
import com.creativemd.littletiles.common.blocks.ISpecialLittleBlock;
import com.creativemd.littletiles.common.entity.EntitySizedTNTPrimed;
import com.creativemd.littletiles.common.items.ItemTileContainer.BlockEntry;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleTileBlock extends LittleTile{
	
	private Block block;
	private int meta;
	
	private SpecialBlockHandler handler;
	
	private void updateSpecialHandler()
	{
		handler = SpecialBlockHandler.getSpecialBlockHandler(block, meta);
	}
	
	public boolean hasSpecialBlockHandler()
	{
		return handler != null;
	}
	
	public void setBlock(Block block, int meta)
	{
		this.block = block;
		this.meta = meta;
		updateSpecialHandler();
	}
	
	public void setMeta(int meta)
	{
		this.meta = meta;
		updateSpecialHandler();
	}
	
	public void setBlock(Block block)
	{
		this.block = block;
		updateSpecialHandler();
	}
	
	public Block getBlock()
	{
		return this.block;
	}
	
	public int getMeta()
	{
		return this.meta;
	}
	
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
		setBlock(block, meta);
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
		super.saveTileExtra(nbt);
		nbt.setString("block", Block.REGISTRY.getNameForObject(block).toString());
		nbt.setInteger("meta", meta);
	}

	@Override
	public void loadTileExtra(NBTTagCompound nbt) {
		super.loadTileExtra(nbt);
		setBlock(Block.getBlockFromName(nbt.getString("block")), nbt.getInteger("meta"));
		if(te.isClientSide())
			updateClient();
	}
	
	@Override
	public void copyExtra(LittleTile tile) {
		super.copyExtra(tile);
		if(tile instanceof LittleTileBlock)
		{
			LittleTileBlock thisTile = (LittleTileBlock) tile;
			thisTile.setBlock(block, meta);
			if(FMLCommonHandler.instance().getSide().isClient())
				thisTile.translucent = translucent;
		}
	}
	
	@Override
	public boolean isIdenticalToNBT(NBTTagCompound nbt)
	{
		return super.isIdenticalToNBT(nbt) && Block.REGISTRY.getNameForObject(block).toString().equals(nbt.getString("block")) && meta == nbt.getInteger("meta");
	}

	@Override
	public ItemStack getDrop() {
		ItemStack stack = new ItemStack(LittleTiles.blockTile);
		stack.setTagCompound(new NBTTagCompound());
		//saveTile(stack.getTagCompound());
		stack.getTagCompound().setString("tID", getID());
		//stack.getTagCompound().setInteger("bSize", 0);
		saveTileExtra(stack.getTagCompound());
		boundingBoxes.get(0).getSize().writeToNBT("size", stack.getTagCompound());
		return stack;
	}

	@Override
	public ArrayList<RenderCubeObject> getInternalRenderingCubes() {
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
		if(FMLClientHandler.instance().hasOptifine() && block.canRenderInLayer(state, BlockRenderLayer.CUTOUT))
			return layer == BlockRenderLayer.CUTOUT_MIPPED; //Should fix an Optifine bug
		//if(translucent)
			//return layer == BlockRenderLayer.TRANSLUCENT;
		try{
			return block.canRenderInLayer(getBlockState(), layer);
		}catch(Exception e){
			try{
				return block.getBlockLayer() == layer;
			}catch(Exception e2){
				return layer == BlockRenderLayer.SOLID;
			}
		}
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
		if(hasSpecialBlockHandler())
			handler.randomDisplayTick(this, stateIn, worldIn, pos, rand);
		else
			block.randomDisplayTick(getBlockState(), worldIn, pos, rand);
		
	}
	
	@Override
	public void onTileExplodes(Explosion explosion)
	{
		if(hasSpecialBlockHandler())
			handler.onTileExplodes(this, explosion);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if(super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ))
			return true;
		if(hasSpecialBlockHandler())
			return handler.onBlockActivated(this, worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
		return block.onBlockActivated(worldIn, pos, getBlockState(), playerIn, hand, side, hitX, hitY, hitZ);
	}
	
	@Override
	public void place()
	{
		super.place();
		block.onBlockAdded(te.getWorld(), te.getPos(), getBlockState());
	}
	
	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		if(glowing)
			return super.getLightValue(state, world, pos);
		return block.getLightValue(getBlockState());
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
	@SideOnly(Side.CLIENT)
	public boolean doesProvideSolidFace(EnumFacing facing) {
		return super.doesProvideSolidFace(facing) && !translucent;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canBeRenderCombined(LittleTile tile) {
		if(super.canBeRenderCombined(tile) && tile instanceof LittleTileBlock)
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
	
	@Override
	public ArrayList<LittleTileBox> getCollisionBoxes()
	{
		if(block instanceof ISpecialLittleBlock)
			return ((ISpecialLittleBlock) block).getCollisionBoxes(super.getCollisionBoxes(), this);
		return super.getCollisionBoxes();
	}

	@Override
	public BlockEntry getBlockEntry() {
		return new BlockEntry(block, meta, getPercentVolume());
	}
	
}
