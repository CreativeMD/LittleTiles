package com.creativemd.littletiles.common.action.block;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.mc.WorldUtils;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.LittleActionInteract;
import com.creativemd.littletiles.common.block.BlockTile;
import com.creativemd.littletiles.common.item.ItemLittleWrench;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleAbsoluteBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tile.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.ingredient.LittleInventory;
import com.creativemd.littletiles.common.util.place.PlacementMode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;

public class LittleActionDestroy extends LittleActionInteract {
	
	public LittleActionDestroy(World world, BlockPos blockPos, EntityPlayer player) {
		super(world, blockPos, player);
	}
	
	public LittleActionDestroy() {
		
	}
	
	@Override
	public boolean canBeReverted() {
		return destroyedTiles != null || structurePreview != null;
	}
	
	public LittleAbsolutePreviews destroyedTiles;
	public StructurePreview structurePreview;
	
	@Override
	public LittleAction revert() {
		if (structurePreview != null)
			return structurePreview.getPlaceAction();
		destroyedTiles.convertToSmallest();
		return new LittleActionPlaceAbsolute(destroyedTiles, PlacementMode.normal);
	}
	
	@Override
	protected boolean action(World world, TileEntityLittleTiles te, LittleTile tile, ItemStack stack, EntityPlayer player, RayTraceResult moving, BlockPos pos, boolean secondMode) throws LittleActionException {
		
		if (!world.isRemote) {
			BreakEvent event = new BreakEvent(world, te.getPos(), te.getBlockTileState(), player);
			MinecraftForge.EVENT_BUS.post(event);
			if (event.isCanceled()) {
				sendBlockResetToClient(world, (EntityPlayerMP) player, te);
				return false;
			}
		}
		
		if (tile.isChildOfStructure()) {
			boolean loaded = tile.isConnectedToStructure() && tile.connection.getStructure(world).load() && tile.connection.getStructure(world).loadChildren();
			if (loaded || player.getHeldItemMainhand().getItem() instanceof ItemLittleWrench) {
				if (loaded) {
					structurePreview = new StructurePreview(tile.connection.getStructure(world));
					if (needIngredients(player) && !player.world.isRemote)
						WorldUtils.dropItem(world, tile.connection.getStructure(world).getStructureDrop(), pos);
					tile.te.updateTiles((x) -> tile.destroy(x));
				} else {
					if (secondMode) {
						List<LittleTile> toRemove = new ArrayList<>();
						for (LittleTile teTile : tile.te) {
							boolean teLoaded = teTile.isChildOfStructure() && teTile.isConnectedToStructure() && teTile.connection.getStructure(world).load() && teTile.connection.getStructure(world).loadChildren();
							if (!teLoaded)
								toRemove.add(teTile);
						}
						tile.te.updateTiles((x) -> x.removeAll(toRemove));
					} else
						tile.te.updateTiles((x) -> x.remove(tile));
				}
			} else
				throw new LittleActionException.StructureNotLoadedException();
		} else {
			LittleInventory inventory = new LittleInventory(player);
			destroyedTiles = new LittleAbsolutePreviews(pos, te.getContext());
			List<LittleTile> tiles = new ArrayList<>();
			
			if (BlockTile.selectEntireBlock(player, secondMode)) {
				List<LittleTile> remains = new ArrayList<>();
				for (LittleTile toDestory : te) {
					if (!toDestory.isChildOfStructure()) {
						destroyedTiles.addTile(toDestory); // No need to use addPreivew as all previews are inside one block
						tiles.add(toDestory);
					} else
						remains.add(toDestory);
				}
				
				giveOrDrop(player, inventory, tiles);
				
				te.updateTiles((x) -> {
					x.clear();
					x.addAll(remains);
				});
				
			} else {
				destroyedTiles.addTile(tile); // No need to use addPreivew as all previews are inside one block
				
				checkAndGive(player, inventory, getIngredients(destroyedTiles));
				
				tile.te.updateTiles((x) -> tile.destroy(x));
			}
		}
		
		world.playSound((EntityPlayer) null, pos, tile.getSound().getBreakSound(), SoundCategory.BLOCKS, (tile.getSound().getVolume() + 1.0F) / 2.0F, tile.getSound().getPitch() * 0.8F);
		
		return true;
	}
	
	@Override
	protected boolean isRightClick() {
		return false;
	}
	
	@Override
	public LittleAction flip(Axis axis, LittleAbsoluteBox box) {
		LittleBoxes boxes;
		if (structurePreview != null) {
			boxes = new LittleBoxes(structurePreview.previews.pos, structurePreview.previews.getContext());
			boxes.add(structurePreview.previews.get(0).box);
		} else if (destroyedTiles != null) {
			destroyedTiles.convertToSmallest();
			boxes = new LittleBoxes(blockPos, destroyedTiles.getContext());
			for (LittlePreview preview : destroyedTiles)
				boxes.add(preview.box);
		} else
			return null;
		
		boxes.flip(axis, box);
		return new LittleActionDestroyBoxes(boxes);
	}
	
	public static class StructurePreview {
		
		public LittleAbsolutePreviews previews;
		public boolean requiresItemStack;
		public LittleStructure structure;
		
		public StructurePreview(LittleStructure structure) {
			if (!structure.load())
				throw new RuntimeException("Structure is not loaded, can't create preview of it!");
			previews = structure.getAbsolutePreviews(structure.getMainTile().te.getPos());
			requiresItemStack = previews.getStructureType().canOnlyBePlacedByItemStack();
			
			this.structure = structure;
		}
		
		public LittleAction getPlaceAction() {
			if (requiresItemStack)
				return new LittleActionPlaceAbsolute.LittleActionPlaceAbsolutePremade(previews, PlacementMode.all, false);
			return new LittleActionPlaceAbsolute(previews, PlacementMode.all, false);
		}
		
		@Override
		public int hashCode() {
			return previews.structure.hashCode();
		}
		
		@Override
		public boolean equals(Object paramObject) {
			if (paramObject instanceof StructurePreview)
				return structure == ((StructurePreview) paramObject).structure;
			if (paramObject instanceof LittleStructure)
				return structure == paramObject;
			return false;
		}
	}
	
}
