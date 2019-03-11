package com.creativemd.littletiles.common.tiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.creativemd.littletiles.client.tiles.LittleRenderingCube;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.api.blocks.ISpecialBlockHandler;
import com.creativemd.littletiles.common.api.blocks.SpecialBlockHandler;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleTileBlock extends LittleTile {
	
	private Block block;
	private int meta;
	
	private ISpecialBlockHandler handler;
	
	private void updateSpecialHandler() {
		if (!(block instanceof BlockAir))
			handler = SpecialBlockHandler.getSpecialBlockHandler(block, meta);
		updateBlockState();
	}
	
	public boolean hasSpecialBlockHandler() {
		return handler != null;
	}
	
	protected void setBlock(String defaultName, Block block, int meta) {
		if (block == null || block instanceof BlockAir) {
			this.block = Blocks.AIR;
			this.meta = meta;
			this.handler = new MissingBlockHandler(defaultName);
		} else
			setBlock(block, meta);
	}
	
	public void setBlock(Block block, int meta) {
		this.block = block;
		this.meta = meta;
		updateSpecialHandler();
	}
	
	public void setMeta(int meta) {
		this.meta = meta;
		updateSpecialHandler();
	}
	
	public void setBlock(Block block) {
		this.block = block;
		updateSpecialHandler();
	}
	
	public Block getBlock() {
		return this.block;
	}
	
	public int getMeta() {
		return this.meta;
	}
	
	@SideOnly(Side.CLIENT)
	protected boolean translucent;
	protected IBlockState state = null;
	
	public IBlockState getBlockState() {
		if (state == null)
			updateBlockState();
		return state;
	}
	
	public void updateBlockState() {
		state = block.getStateFromMeta(meta);
		if (state == null)
			state = block.getDefaultState();
	}
	
	public LittleTileBlock(Block block, int meta) {
		super();
		setBlock(block, meta);
		if (FMLCommonHandler.instance().getSide().isClient())
			updateClient();
	}
	
	public LittleTileBlock(Block block) {
		this(block, 0);
	}
	
	public LittleTileBlock() {
		super();
	}
	
	public void updateClient() {
		updateTranslucent();
	}
	
	@SideOnly(Side.CLIENT)
	public void updateTranslucent() {
		translucent = getBlockState().isTranslucent() || !getBlockState().isOpaqueCube() || block.canRenderInLayer(getBlockState(), BlockRenderLayer.TRANSLUCENT) || block.canRenderInLayer(getBlockState(), BlockRenderLayer.CUTOUT);
	}
	
	@Override
	public void saveTileExtra(NBTTagCompound nbt) {
		super.saveTileExtra(nbt);
		nbt.setString("block", handler instanceof MissingBlockHandler ? ((MissingBlockHandler) handler).blockname : Block.REGISTRY.getNameForObject(block).toString());
		nbt.setInteger("meta", meta);
	}
	
	@Override
	public void loadTileExtra(NBTTagCompound nbt) {
		super.loadTileExtra(nbt);
		setBlock(nbt.getString("block"), Block.getBlockFromName(nbt.getString("block")), nbt.getInteger("meta"));
		if (te.isClientSide())
			updateClient();
	}
	
	@Override
	public void copyExtra(LittleTile tile) {
		super.copyExtra(tile);
		if (tile instanceof LittleTileBlock) {
			LittleTileBlock thisTile = (LittleTileBlock) tile;
			thisTile.handler = this.handler;
			thisTile.block = this.block;
			thisTile.meta = this.meta;
			// thisTile.setBlock(block, meta);
			if (FMLCommonHandler.instance().getEffectiveSide().isClient())
				thisTile.translucent = translucent;
		}
	}
	
	@Override
	public boolean isIdenticalToNBT(NBTTagCompound nbt) {
		return super.isIdenticalToNBT(nbt) && Block.REGISTRY.getNameForObject(block).toString().equals(nbt.getString("block")) && meta == nbt.getInteger("meta");
	}
	
	@Override
	public ItemStack getDrop() {
		/* ItemStack stack = new ItemStack(LittleTiles.blockTile);
		 * stack.setTagCompound(new NBTTagCompound());
		 * stack.getTagCompound().setString("tID", getID());
		 * saveTileExtra(stack.getTagCompound()); box.getSize().writeToNBT("size",
		 * stack.getTagCompound()); return stack; */
		return ItemBlockTiles.getStackFromPreview(getContext(), getPreviewTile());
	}
	
	@Override
	public List<LittleRenderingCube> getInternalRenderingCubes() {
		ArrayList<LittleRenderingCube> cubes = new ArrayList<>();
		if (block != Blocks.BARRIER)
			cubes.add(box.getRenderingCube(getContext(), block, meta));
		return cubes;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldBeRenderedInLayer(BlockRenderLayer layer) {
		if (FMLClientHandler.instance().hasOptifine() && block.canRenderInLayer(state, BlockRenderLayer.CUTOUT))
			return layer == BlockRenderLayer.CUTOUT_MIPPED; // Should fix an Optifine bug
			
		try {
			return block.canRenderInLayer(getBlockState(), layer);
		} catch (Exception e) {
			try {
				return block.getBlockLayer() == layer;
			} catch (Exception e2) {
				return layer == BlockRenderLayer.SOLID;
			}
		}
	}
	
	@Override
	public void onPlaced(EntityPlayer player, ItemStack stack, EnumFacing facing) {
		super.onPlaced(player, stack, facing);
		try {
			block.onBlockPlacedBy(te.getWorld(), te.getPos(), getBlockState(), player, stack);
		} catch (Exception e) {
			
		}
	}
	
	@Override
	public SoundType getSound() {
		return block.getSoundType();
	}
	
	@Override
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if (hasSpecialBlockHandler())
			handler.randomDisplayTick(this, stateIn, worldIn, pos, rand);
		else
			block.randomDisplayTick(getBlockState(), worldIn, pos, rand);
		
		if (block == Blocks.BARRIER)
			spawnBarrierParticles(pos);
	}
	
	@SideOnly(Side.CLIENT)
	private void spawnBarrierParticles(BlockPos pos) {
		Minecraft mc = Minecraft.getMinecraft();
		ItemStack itemstack = mc.player.getHeldItemMainhand();
		if (mc.playerController.getCurrentGameType() == GameType.CREATIVE && !itemstack.isEmpty() && itemstack.getItem() == Item.getItemFromBlock(Blocks.BARRIER))
			mc.world.spawnParticle(EnumParticleTypes.BARRIER, (double) ((float) pos.getX() + 0.5F), (double) ((float) pos.getY() + 0.5F), (double) ((float) pos.getZ() + 0.5F), 0.0D, 0.0D, 0.0D, new int[0]);
	}
	
	@Override
	public void onTileExplodes(Explosion explosion) {
		if (hasSpecialBlockHandler())
			handler.onTileExplodes(this, explosion);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) {
		if (super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ, action))
			return true;
		if (hasSpecialBlockHandler())
			return handler.onBlockActivated(this, worldIn, pos, getBlockState(), playerIn, hand, heldItem, side, hitX, hitY, hitZ);
		return block.onBlockActivated(worldIn, pos, getBlockState(), playerIn, hand, side, hitX, hitY, hitZ);
	}
	
	@Override
	public boolean canBeConvertedToVanilla() {
		if (!super.canBeConvertedToVanilla())
			return false;
		if (hasSpecialBlockHandler())
			return handler.canBeConvertedToVanilla(this);
		return true;
	}
	
	@Override
	public void place() {
		super.place();
		block.onBlockAdded(te.getWorld(), te.getPos(), getBlockState());
	}
	
	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (glowing)
			return super.getLightValue(state, world, pos);
		return block.getLightValue(getBlockState());
	}
	
	@Override
	public float getEnchantPowerBonus(World world, BlockPos pos) {
		return (float) block.getEnchantPowerBonus(world, pos);
	}
	
	@Override
	public boolean canBeCombined(LittleTile tile) {
		if (super.canBeCombined(tile) && tile instanceof LittleTileBlock) {
			return block == ((LittleTileBlock) tile).block && meta == ((LittleTileBlock) tile).meta;
		}
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean doesProvideSolidFace(EnumFacing facing) {
		return super.doesProvideSolidFace(facing) && !translucent && block != Blocks.BARRIER;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean canBeRenderCombined(LittleTile tile) {
		if (super.canBeRenderCombined(tile) && tile instanceof LittleTileBlock)
			return (block == ((LittleTileBlock) tile).block && meta == ((LittleTileBlock) tile).meta && block != Blocks.BARRIER && ((LittleTileBlock) tile).block != Blocks.BARRIER) || (hasSpecialBlockHandler() && handler.canBeRenderCombined(this, (LittleTileBlock) tile));
		return false;
	}
	
	@Override
	protected boolean canSawResize(EnumFacing facing, EntityPlayer player) {
		return true;
	}
	
	@Override
	public float getExplosionResistance() {
		return block.getExplosionResistance(null);
	}
	
	@Override
	public float getSlipperiness(Entity entity) {
		return block.getSlipperiness(getBlockState(), te.getWorld(), te.getPos(), entity);
	}
	
	@Override
	public boolean isMaterial(Material material) {
		if (hasSpecialBlockHandler())
			return handler.isMaterial(this, material);
		return material == block.getMaterial(state);
	}
	
	@Override
	public boolean isLiquid() {
		if (hasSpecialBlockHandler())
			return handler.isLiquid(this);
		return getBlockState().getMaterial().isLiquid();
	}
	
	@Override
	public List<LittleTileBox> getCollisionBoxes() {
		if (hasSpecialBlockHandler())
			return handler.getCollisionBoxes(this, super.getCollisionBoxes());
		return super.getCollisionBoxes();
	}
	
	@Override
	public Vec3d modifyAcceleration(World worldIn, BlockPos pos, Entity entityIn, Vec3d motion) {
		if (hasSpecialBlockHandler())
			return handler.modifyAcceleration(this, entityIn, motion);
		return super.modifyAcceleration(worldIn, pos, entityIn, motion);
	}
	
	@Override
	public LittleTilePreview getPreviewTile() {
		if (hasSpecialBlockHandler()) {
			LittleTilePreview preview = handler.getPreview(this);
			if (preview != null)
				return preview;
		}
		return super.getPreviewTile();
	}
	
	@Override
	public boolean shouldCheckForCollision() {
		if (super.shouldCheckForCollision())
			return true;
		if (hasSpecialBlockHandler())
			return handler.shouldCheckForCollision(this);
		return false;
	}
	
	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		super.onEntityCollidedWithBlock(worldIn, pos, state, entityIn);
		if (hasSpecialBlockHandler())
			handler.onEntityCollidedWithBlock(worldIn, this, pos, state, entityIn);
	}
	
	public static class MissingBlockHandler implements ISpecialBlockHandler {
		
		public final String blockname;
		
		public MissingBlockHandler(String blockname) {
			this.blockname = blockname;
		}
	}
}
