package com.creativemd.littletiles.common.action.block;

import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionCombined;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleAbsoluteBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tile.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredient;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredients;
import com.creativemd.littletiles.common.util.ingredient.LittleInventory;
import com.creativemd.littletiles.common.util.place.Placement;
import com.creativemd.littletiles.common.util.place.PlacementHelper;
import com.creativemd.littletiles.common.util.place.PlacementMode;
import com.creativemd.littletiles.common.util.place.PlacementPosition;
import com.creativemd.littletiles.common.util.place.PlacementPreview;
import com.creativemd.littletiles.common.util.place.PlacementResult;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.world.World;

public class LittleActionPlaceStack extends LittleAction {
	
	public PlacementPosition position;
	public boolean centered;
	public boolean fixed;
	public PlacementMode mode;
	public LittlePreviews previews;
	
	public PlacementResult placedTiles;
	
	public LittleActionPlaceStack(LittlePreviews previews, PlacementPosition position, boolean centered, boolean fixed, PlacementMode mode) {
		super();
		this.position = position;
		this.centered = centered;
		this.fixed = fixed;
		this.mode = mode;
		this.previews = previews;
	}
	
	public LittleActionPlaceStack() {
		super();
	}
	
	public void checkMode(LittlePreviews previews) {
		if (previews.hasStructure() && !mode.canPlaceStructures()) {
			System.out.println("Using invalid mode for placing structure. mode=" + mode.name);
			this.mode = PlacementMode.getStructureDefault();
		}
	}
	
	public void checkMode(LittleStructure structure) {
		if (structure != null && !mode.canPlaceStructures()) {
			System.out.println("Using invalid mode for placing structure. mode=" + mode.name);
			this.mode = PlacementMode.getStructureDefault();
		}
	}
	
	@Override
	public boolean canBeReverted() {
		return true;
	}
	
	@Override
	public LittleAction revert() {
		boxes.convertToSmallest();
		
		if (destroyed != null) {
			destroyed.convertToSmallest();
			return new LittleActionCombined(new LittleActionDestroyBoxes(boxes), new LittleActionPlaceAbsolute(destroyed, PlacementMode.normal, true));
		}
		return new LittleActionDestroyBoxes(boxes);
	}
	
	public LittleBoxes boxes;
	public LittleAbsolutePreviews destroyed;
	
	@Override
	protected boolean action(EntityPlayer player) throws LittleActionException {
		ItemStack stack = player.getHeldItemMainhand();
		World world = player.world;
		
		if (!isAllowedToInteract(world, player, position.getPos(), true, EnumFacing.EAST)) {
			sendBlockResetToClient(world, (EntityPlayerMP) player, position.getPos());
			return false;
		}
		
		if (PlacementHelper.getLittleInterface(stack) != null) {
			PlacementResult tiles = placeTile(player, stack, player.world, position, centered, fixed, mode);
			
			if (!player.world.isRemote) {
				EntityPlayerMP playerMP = (EntityPlayerMP) player;
				Slot slot = playerMP.openContainer.getSlotFromInventory(playerMP.inventory, playerMP.inventory.currentItem);
				playerMP.connection.sendPacket(new SPacketSetSlot(playerMP.openContainer.windowId, slot.slotNumber, playerMP.inventory.getCurrentItem()));
			}
			return tiles != null;
		}
		return false;
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		position.writeToBytes(buf);
		buf.writeBoolean(centered);
		buf.writeBoolean(fixed);
		writePlacementMode(mode, buf);
		writePreviews(previews, buf);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		this.position = PlacementPosition.readFromBytes(buf);
		this.centered = buf.readBoolean();
		this.fixed = buf.readBoolean();
		this.mode = readPlacementMode(buf);
		this.previews = readPreviews(buf);
	}
	
	public PlacementResult placeTile(EntityPlayer player, ItemStack stack, World world, PlacementPosition position, boolean centered, boolean fixed, PlacementMode mode) throws LittleActionException {
		ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
		checkMode(previews);
		
		PlacementPreview result = PlacementHelper.getPreviews(world, previews, iTile.getPreviewsContext(stack), stack, position, centered, fixed, false, mode);
		
		if (result == null)
			return null;
		
		ItemStack toPlace = stack.copy();
		
		LittleInventory inventory = new LittleInventory(player);
		
		if (needIngredients(player)) {
			if (!iTile.containsIngredients(stack))
				canTake(player, inventory, getIngredients(result.previews));
		}
		
		Placement placement = new Placement(player, result).setStack(toPlace);
		placedTiles = placement.place();
		
		if (placedTiles != null) {
			boxes = placedTiles.placedBoxes;
			
			if (needIngredients(player)) {
				giveOrDrop(player, inventory, placement.removedTiles);
				
				if (iTile.containsIngredients(stack)) {
					stack.shrink(1);
					giveOrDrop(player, inventory, placement.unplaceableTiles);
				} else {
					LittleIngredients ingredients = LittleIngredient.extractStructureOnly(previews);
					ingredients.add(getIngredients(placedTiles.placedPreviews));
					take(player, inventory, ingredients);
				}
			}
			
			if (!placement.removedTiles.isEmpty()) {
				destroyed = new LittleAbsolutePreviews(position.getPos(), result.context);
				for (LittleTile tile : placement.removedTiles)
					destroyed.addTile(tile);
				
			}
		} else
			boxes = new LittleBoxes(position.getPos(), result.context);
		
		return placedTiles;
	}
	
	@Override
	public LittleAction flip(Axis axis, LittleAbsoluteBox box) {
		if (placedTiles == null)
			return null;
		return new LittleActionPlaceAbsolute(placedTiles.placedPreviews.copy(), mode);
	}
	
}
