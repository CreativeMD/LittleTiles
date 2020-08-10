package com.creativemd.littletiles.common.api.block;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.mc.BlockUtils;
import com.creativemd.creativecore.common.utils.sorting.BlockSelector;
import com.creativemd.creativecore.common.utils.sorting.BlockSelector.BlockSelectorAnd;
import com.creativemd.creativecore.common.utils.sorting.BlockSelector.BlockSelectorBlock;
import com.creativemd.creativecore.common.utils.sorting.BlockSelector.BlockSelectorClass;
import com.creativemd.creativecore.common.utils.sorting.BlockSelector.BlockSelectorProperty;
import com.creativemd.creativecore.common.utils.sorting.BlockSelector.BlockSelectorState;
import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.creativecore.common.utils.type.PairList;
import com.creativemd.littletiles.common.entity.EntitySizedTNTPrimed;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

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

public class SpecialBlockHandler {
	
	public static PairList<BlockSelector, ISpecialBlockHandler> specialHandlers = new PairList<>();
	
	public static final ISpecialBlockHandler EMPTY_HANDLER = new ISpecialBlockHandler() {};
	
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
			public boolean onBlockActivated(IParentTileList parent, LittleTile tile, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
				if (heldItem != null && (heldItem.getItem() == Items.FLINT_AND_STEEL || heldItem.getItem() == Items.FIRE_CHARGE)) {
					if (!parent.getWorld().isRemote)
						explodeTile(parent, tile, player, false);
					parent.getTe().updateTiles(x -> x.get(parent).remove(tile));
					
					if (heldItem.getItem() == Items.FLINT_AND_STEEL)
						heldItem.damageItem(1, player);
					else if (!player.capabilities.isCreativeMode)
						heldItem.shrink(1);
					
					return true;
				}
				return false;
			}
			
			@Override
			public void onTileExplodes(IParentTileList parent, LittleTile tile, Explosion explosion) {
				explodeTile(parent, tile, explosion.getExplosivePlacedBy(), true);
			}
			
			public void explodeTile(IParentTileList parent, LittleTile tile, EntityLivingBase entity, boolean randomFuse) {
				BlockPos pos = parent.getPos();
				LittleVec size = tile.getSize();
				LittleVec min = tile.getMinVec();
				LittleGridContext context = parent.getContext();
				EntitySizedTNTPrimed entitytntprimed = new EntitySizedTNTPrimed(parent.getWorld(), pos.getX() + min.getPosX(context) + size.getPosX(context) / 2, pos.getY() + min.getPosY(context) + size.getPosY(context) / 2, pos.getZ() + min.getPosZ(
				        context) + size.getPosZ(context) / 2, entity, context, size);
				if (randomFuse)
					entitytntprimed.setFuse((short) (parent.getWorld().rand.nextInt(entitytntprimed.getFuse() / 4) + entitytntprimed.getFuse() / 8));
				parent.getWorld().spawnEntity(entitytntprimed);
				parent.getWorld().playSound((EntityPlayer) null, entitytntprimed.posX, entitytntprimed.posY, entitytntprimed.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}
			
		});
		
		SpecialBlockHandler.registerSpecialHandler(Blocks.CRAFTING_TABLE, new ISpecialBlockHandler() {
			
			@Override
			public boolean onBlockActivated(IParentTileList parent, LittleTile tile, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
				if (parent.getWorld().isRemote) {
					return true;
				} else {
					player.displayGui(new BlockWorkbench.InterfaceCraftingTable(parent.getWorld(), parent.getPos()) {
						
						@Override
						public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
							return new ContainerWorkbench(playerInventory, parent.getWorld(), parent.getPos()) {
								
								@Override
								public boolean canInteractWith(EntityPlayer playerIn) {
									return true;
								}
							};
						}
					});
					player.addStat(StatList.CRAFTING_TABLE_INTERACTION);
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
			public void rotatePreview(Rotation rotation, LittlePreview preview, LittleVec doubledCenter) {
				IBlockState state = BlockUtils.getState(preview.getBlock(), preview.getMeta());
				Axis axis = logAxisToNormal(state.getValue(BlockLog.LOG_AXIS));
				if (axis != null)
					preview.getTileData().setString("block", preview.getBlockName() + ":" + preview.getBlock().getMetaFromState(state.withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.fromFacingAxis(RotationUtils.rotate(axis, rotation)))));
			}
			
		});
		
		SpecialBlockHandler.registerSpecialHandler(new BlockSelectorAnd(new BlockSelectorClass(BlockRotatedPillar.class), new BlockSelectorProperty(BlockRotatedPillar.AXIS)), new ISpecialBlockHandler() {
			
			@Override
			public void rotatePreview(Rotation rotation, LittlePreview preview, LittleVec doubledCenter) {
				preview.getTileData().setInteger("meta", RotationUtils.rotate(BlockUtils.getState(preview.getBlock(), preview.getMeta()).getValue(BlockRotatedPillar.AXIS), rotation).ordinal());
			}
			
		});
	}
	
}
