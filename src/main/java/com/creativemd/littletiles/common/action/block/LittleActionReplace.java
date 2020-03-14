package com.creativemd.littletiles.common.action.block;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionCombined;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.LittleActionInteract;
import com.creativemd.littletiles.common.block.BlockTile;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleAbsoluteBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tile.registry.LittleTileRegistry;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.ingredient.LittleInventory;
import com.creativemd.littletiles.common.util.place.PlacementMode;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;

public class LittleActionReplace extends LittleActionInteract {
	
	public LittlePreview toReplace;
	
	public LittleActionReplace(World world, BlockPos blockPos, EntityPlayer player, LittlePreview toReplace) {
		super(world, blockPos, player);
		this.toReplace = toReplace;
	}
	
	public LittleActionReplace() {
		
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		super.writeBytes(buf);
		NBTTagCompound nbt = new NBTTagCompound();
		toReplace.writeToNBT(nbt);
		writeNBT(buf, nbt);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		super.readBytes(buf);
		toReplace = LittleTileRegistry.loadPreview(readNBT(buf));
	}
	
	@Override
	protected boolean isRightClick() {
		return false;
	}
	
	public LittleAbsolutePreviews replacedTiles;
	public LittleBoxes boxes;
	
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
		
		replacedTiles = new LittleAbsolutePreviews(pos, te.getContext());
		boxes = new LittleBoxes(pos, te.getContext());
		
		if (LittleTiles.CONFIG.isTransparencyRestricted(player))
			isAllowedToPlacePreview(player, toReplace);
		
		if (BlockTile.selectEntireBlock(player, secondMode)) {
			List<LittleTile> toRemove = new ArrayList<>();
			LittlePreviews toBePlaced = new LittlePreviews(te.getContext());
			List<PlacePreview> previews = new ArrayList<>();
			
			for (LittleTile toDestroy : te) {
				if (!toDestroy.isChildOfStructure() && tile.canBeCombined(toDestroy) && toDestroy.canBeCombined(tile)) {
					replacedTiles.addTile(toDestroy);
					boxes.addBox(toDestroy);
					
					toBePlaced.addTile(toDestroy);
					LittlePreview preview = toReplace.copy();
					preview.box = toDestroy.box;
					previews.add(preview.getPlaceableTile(null, true, null, null));
					
					toRemove.add(toDestroy);
				}
			}
			
			if (toRemove.isEmpty())
				return false;
			
			LittleInventory inventory = new LittleInventory(player);
			
			try {
				inventory.startSimulation();
				take(player, inventory, getIngredients(toBePlaced));
				give(player, inventory, getIngredients(replacedTiles));
			} finally {
				inventory.stopSimulation();
			}
			
			te.updateTiles((x) -> {
				for (LittleTile toDestroy : toRemove)
					toDestroy.destroy(x);
			});
			
			ArrayList<LittleTile> unplaceableTiles = new ArrayList<LittleTile>();
			LittleActionPlaceStack.placeTiles(world, player, te.getContext(), previews, null, PlacementMode.normal, pos, stack, unplaceableTiles, null, EnumFacing.EAST);
			giveOrDrop(player, inventory, unplaceableTiles);
		} else {
			if (tile.isChildOfStructure())
				return false;
			
			LittleInventory inventory = new LittleInventory(player);
			
			replacedTiles.addTile(tile);
			boxes.addBox(tile);
			
			LittlePreviews toBePlaced = new LittlePreviews(te.getContext());
			toReplace.box = tile.box;
			toBePlaced.addPreview(null, toReplace, te.getContext());
			
			try {
				inventory.startSimulation();
				take(player, inventory, getIngredients(toBePlaced));
				give(player, inventory, getIngredients(replacedTiles));
			} finally {
				inventory.stopSimulation();
			}
			
			te.updateTiles((x) -> tile.destroy(x));
			
			List<PlacePreview> previews = new ArrayList<>();
			previews.add(toReplace.getPlaceableTile(null, true, null, null));
			
			ArrayList<LittleTile> unplaceableTiles = new ArrayList<LittleTile>();
			LittleActionPlaceStack.placeTiles(world, player, te.getContext(), previews, null, PlacementMode.normal, pos, stack, unplaceableTiles, null, EnumFacing.EAST);
			giveOrDrop(player, inventory, unplaceableTiles);
		}
		
		world.playSound((EntityPlayer) null, pos, tile.getSound().getBreakSound(), SoundCategory.BLOCKS, (tile.getSound().getVolume() + 1.0F) / 2.0F, tile.getSound().getPitch() * 0.8F);
		
		return true;
	}
	
	@Override
	public boolean canBeReverted() {
		return true;
	}
	
	@Override
	public LittleAction revert() throws LittleActionException {
		return new LittleActionCombined(new LittleActionDestroyBoxes(boxes), new LittleActionPlaceAbsolute(replacedTiles, PlacementMode.normal));
	}
	
	@Override
	public LittleAction flip(Axis axis, LittleAbsoluteBox absoluteBox) {
		LittleBoxes boxes = this.boxes.copy();
		boxes.flip(axis, absoluteBox);
		
		LittleAbsolutePreviews previews = new LittleAbsolutePreviews(boxes.pos, boxes.context);
		for (LittleBox box : boxes) {
			LittlePreview preview = toReplace.copy();
			preview.box = box;
			previews.addWithoutCheckingPreview(preview);
		}
		return new LittleActionCombined(new LittleActionDestroyBoxes(boxes), new LittleActionPlaceAbsolute(previews, PlacementMode.normal));
	}
}
