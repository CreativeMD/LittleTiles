package com.creativemd.littletiles.common.utils;

import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Nullable;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.blocks.ISpecialLittleBlock;
import com.creativemd.littletiles.common.entity.EntitySizedTNTPrimed;
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
		super.saveTileExtra(nbt);
		nbt.setString("block", Block.REGISTRY.getNameForObject(block).toString());
		nbt.setInteger("meta", meta);
	}

	@Override
	public void loadTileExtra(NBTTagCompound nbt) {
		super.loadTileExtra(nbt);
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
		super.copyExtra(tile);
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
		block.randomDisplayTick(getBlockState(), worldIn, pos, rand);
	}
	
	public void explodeTile(EntityLivingBase entity, boolean randomFuse)
	{
		BlockPos pos = te.getPos();
		LittleTileSize size = boundingBoxes.get(0).getSize();
        EntitySizedTNTPrimed entitytntprimed = new EntitySizedTNTPrimed(te.getWorld(), (double)((float)pos.getX() + cornerVec.getPosX()/2 + size.getPosX()/2), (double)(pos.getY() + cornerVec.getPosY()/2 + size.getPosY()/2), (double)((float)pos.getZ() + cornerVec.getPosZ()/2 + size.getPosZ()/2), entity, size);
        if(randomFuse)
        	entitytntprimed.setFuse((short)(te.getWorld().rand.nextInt(entitytntprimed.getFuse() / 4) + entitytntprimed.getFuse() / 8));
        te.getWorld().spawnEntityInWorld(entitytntprimed);
        te.getWorld().playSound((EntityPlayer)null, entitytntprimed.posX, entitytntprimed.posY, entitytntprimed.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
	}
	
	@Override
	public void onTileExplodes(Explosion explosion)
	{
		if(block instanceof BlockTNT)
			explodeTile(explosion.getExplosivePlacedBy(), true);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if(super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ))
			return true;
		if(block instanceof BlockTNT)
		{
			if (heldItem != null && (heldItem.getItem() == Items.FLINT_AND_STEEL || heldItem.getItem() == Items.FIRE_CHARGE))
	        {
	            if (!worldIn.isRemote && !this.boundingBoxes.isEmpty())
	            {
	            	explodeTile(playerIn, false);
	            }
	            destroy();
	            //worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);

	            if (heldItem.getItem() == Items.FLINT_AND_STEEL)
	            {
	                heldItem.damageItem(1, playerIn);
	            }
	            else if (!playerIn.capabilities.isCreativeMode)
	            {
	                --heldItem.stackSize;
	            }

	            return true;
	        }
		}
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
		return super.doesProvideSolidFace(facing) && !translucent;
	}

	@Override
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
	
}
