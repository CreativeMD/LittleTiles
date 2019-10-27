package com.creativemd.littletiles.common.api.blocks;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.sorting.BlockSelector;
import com.creativemd.creativecore.common.utils.sorting.BlockSelector.BlockSelectorAnd;
import com.creativemd.creativecore.common.utils.sorting.BlockSelector.BlockSelectorBlock;
import com.creativemd.creativecore.common.utils.sorting.BlockSelector.BlockSelectorClass;
import com.creativemd.creativecore.common.utils.sorting.BlockSelector.BlockSelectorProperty;
import com.creativemd.creativecore.common.utils.sorting.BlockSelector.BlockSelectorState;
import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.creativecore.common.utils.type.PairList;
import com.creativemd.littletiles.common.entity.EntitySizedTNTPrimed;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class SpecialBlockHandler {
	
	public static PairList<BlockSelector, ISpecialBlockHandler> specialHandlers = new PairList<>();
	
	public static final ISpecialBlockHandler EMPTY_HANDLER = new ISpecialBlockHandler() {
	};
	
	public static ISpecialBlockHandler getSpecialBlockHandler(Block block, int meta) {
		if (block instanceof ISpecialBlockHandler)
			return (ISpecialBlockHandler) block;
		
		for (Pair<BlockSelector, ISpecialBlockHandler> pair : specialHandlers)
			if (pair.key.is(block, meta))
				return pair.value;
		return EMPTY_HANDLER;
	}
	
	public static void registerSpecialHandler(Class<? extends Block> clazz, ISpecialBlockHandler handler) {
		registerSpecialHandler(new BlockSelectorClass(clazz), handler);
	}
	
	public static void registerSpecialHandler(Block block, int meta, ISpecialBlockHandler handler) {
		registerSpecialHandler(new BlockSelectorState(block, meta), handler);
	}
	
	public static void registerSpecialHandler(Block block, ISpecialBlockHandler handler) {
		registerSpecialHandler(new BlockSelectorBlock(block), handler);
	}
	
	public static void registerSpecialHandler(BlockSelector selector, ISpecialBlockHandler handler) {
		specialHandlers.add(selector, handler);
	}
	
	static {
		SpecialBlockHandler.registerSpecialHandler(BlockTNT.class, new ISpecialBlockHandler() {
			
			@Override
			public boolean onBlockActivated(LittleTileBlock tile, World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
				if (heldItem != null && (heldItem.getItem() == Items.FLINT_AND_STEEL || heldItem.getItem() == Items.FIRE_CHARGE)) {
					if (!worldIn.isRemote) {
						explodeTile(tile, playerIn, false);
					}
					tile.te.updateTiles((x) -> tile.destroy(x));
					
					if (heldItem.getItem() == Items.FLINT_AND_STEEL) {
						heldItem.damageItem(1, playerIn);
					} else if (!playerIn.capabilities.isCreativeMode) {
						heldItem.shrink(1);
					}
					
					return true;
				}
				return false;
			}
			
			@Override
			public void onTileExplodes(LittleTileBlock tile, Explosion explosion) {
				explodeTile(tile, explosion.getExplosivePlacedBy(), true);
			}
			
			public void explodeTile(LittleTileBlock tile, EntityLivingBase entity, boolean randomFuse) {
				BlockPos pos = tile.te.getPos();
				LittleTileSize size = tile.box.getSize();
				LittleTileVec min = tile.box.getMinVec();
				EntitySizedTNTPrimed entitytntprimed = new EntitySizedTNTPrimed(tile.te.getWorld(), pos.getX() + min.getPosX(tile.getContext()) + size.getPosX(tile.getContext()) / 2, pos.getY() + min.getPosY(tile.getContext()) + size.getPosY(tile.getContext()) / 2, pos.getZ() + min.getPosZ(tile.getContext()) + size.getPosZ(tile.getContext()) / 2, entity, tile.getContext(), size);
				if (randomFuse)
					entitytntprimed.setFuse((short) (tile.te.getWorld().rand.nextInt(entitytntprimed.getFuse() / 4) + entitytntprimed.getFuse() / 8));
				tile.te.getWorld().spawnEntity(entitytntprimed);
				tile.te.getWorld().playSound((EntityPlayer) null, entitytntprimed.posX, entitytntprimed.posY, entitytntprimed.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}
			
		});
		
		SpecialBlockHandler.registerSpecialHandler(Blocks.CRAFTING_TABLE, new ISpecialBlockHandler() {
			
			@Override
			public boolean onBlockActivated(LittleTileBlock tile, World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
				if (worldIn.isRemote) {
					return true;
				} else {
					playerIn.displayGui(new BlockWorkbench.InterfaceCraftingTable(worldIn, pos) {
						
						@Override
						public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
							return new ContainerWorkbench(playerInventory, worldIn, pos) {
								
								@Override
								public boolean canInteractWith(EntityPlayer playerIn) {
									return true;
								}
							};
						}
					});
					playerIn.addStat(StatList.CRAFTING_TABLE_INTERACTION);
					return true;
				}
			}
			
		});
		
		SpecialBlockHandler.registerSpecialHandler(new BlockSelectorAnd(new BlockSelectorClass(BlockLog.class), new BlockSelectorProperty(BlockLog.LOG_AXIS)), new ISpecialBlockHandler() {
			
			public Axis logAxisToNormal(BlockLog.EnumAxis axis) {
				switch (axis) {
				case X:
					return Axis.X;
				case Y:
					return Axis.Y;
				case Z:
					return Axis.Z;
				default:
					return null;
				}
			}
			
			@Override
			public void rotatePreview(Rotation rotation, LittleTilePreview preview, LittleTileVec doubledCenter) {
				IBlockState state = preview.getBlock().getStateFromMeta(preview.getMeta());
				Axis axis = logAxisToNormal(state.getValue(BlockLog.LOG_AXIS));
				if (axis != null)
					preview.getTileData().setInteger("meta", preview.getBlock().getMetaFromState(state.withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.fromFacingAxis(RotationUtils.rotate(axis, rotation)))));
			}
			
		});
		
		SpecialBlockHandler.registerSpecialHandler(new BlockSelectorAnd(new BlockSelectorClass(BlockRotatedPillar.class), new BlockSelectorProperty(BlockRotatedPillar.AXIS)), new ISpecialBlockHandler() {
			
			@Override
			public void rotatePreview(Rotation rotation, LittleTilePreview preview, LittleTileVec doubledCenter) {
				preview.getTileData().setInteger("meta", RotationUtils.rotate(preview.getBlock().getStateFromMeta(preview.getMeta()).getValue(BlockRotatedPillar.AXIS), rotation).ordinal());
			}
			
		});
	}
	
}
