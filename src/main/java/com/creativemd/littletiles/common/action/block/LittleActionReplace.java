package com.creativemd.littletiles.common.action.block;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionCombined;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.LittleActionInteract;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.config.SpecialServerConfig;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleBoxes;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;

public class LittleActionReplace extends LittleActionInteract {
	
	public LittleTilePreview toReplace;
	
	public LittleActionReplace(BlockPos blockPos, EntityPlayer player, LittleTilePreview toReplace) {
		super(blockPos, player);
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
		toReplace = LittleTilePreview.loadPreviewFromNBT(readNBT(buf));
	}
	
	@Override
	protected boolean isRightClick() {
		return false;
	}
	
	public LittleAbsolutePreviews replacedTiles;
	public LittleBoxes boxes;
	
	@Override
	protected boolean action(World world, TileEntityLittleTiles te, LittleTile tile, ItemStack stack, EntityPlayer player, RayTraceResult moving, BlockPos pos, boolean secondMode) throws LittleActionException {
		
		if (tile.isStructureBlock)
			return false;
		
		if (!world.isRemote) {
			BreakEvent event = new BreakEvent(world, te.getPos(), te.getBlockTileState(), player);
			MinecraftForge.EVENT_BUS.post(event);
			if (event.isCanceled()) {
				sendBlockResetToClient((EntityPlayerMP) player, pos, te);
				return false;
			}
		}
		
		replacedTiles = new LittleAbsolutePreviews(pos, te.getContext());
		boxes = new LittleBoxes(pos, te.getContext());
		
		if (SpecialServerConfig.isTransparenceyRestricted(player))
			isAllowedToPlacePreview(player, toReplace);
		
		if (BlockTile.selectEntireBlock(player, secondMode)) {
			List<LittleTile> toRemove = new ArrayList<>();
			for (LittleTile toDestroy : te.getTiles()) {
				if (!toDestroy.isStructureBlock && tile.canBeCombined(toDestroy) && toDestroy.canBeCombined(tile)) {
					replacedTiles.addTile(toDestroy);
					boxes.addBox(toDestroy);
					toRemove.add(toDestroy);
				}
			}
			
			if (toRemove.isEmpty())
				return false;
			
			addPreviewToInventory(player, replacedTiles);
			
			List<PlacePreviewTile> previews = new ArrayList<>();
			for (LittleTile toDestroy : toRemove) {
				toDestroy.destroy();
				LittleTilePreview preview = toReplace.copy();
				preview.box = toDestroy.box;
				previews.add(preview.getPlaceableTile(null, true, null));
			}
			
			ArrayList<LittleTile> unplaceableTiles = new ArrayList<LittleTile>();
			LittleActionPlaceRelative.placeTiles(world, player, te.getContext(), previews, null, PlacementMode.normal, pos, stack, unplaceableTiles, null, EnumFacing.EAST);
			addTilesToInventoryOrDrop(player, unplaceableTiles);
			
		} else {
			replacedTiles.addTile(tile);
			boxes.addBox(tile);
			addPreviewToInventory(player, replacedTiles);
			
			tile.destroy();
			
			LittlePreviews toBePlaced = new LittlePreviews(te.getContext());
			toReplace.box = tile.box;
			toBePlaced.addPreview(null, toReplace, te.getContext());
			drainPreviews(player, toBePlaced);
			
			List<PlacePreviewTile> previews = new ArrayList<>();
			previews.add(toReplace.getPlaceableTile(null, true, null));
			
			ArrayList<LittleTile> unplaceableTiles = new ArrayList<LittleTile>();
			LittleActionPlaceRelative.placeTiles(world, player, te.getContext(), previews, null, PlacementMode.normal, pos, stack, unplaceableTiles, null, EnumFacing.EAST);
			addTilesToInventoryOrDrop(player, unplaceableTiles);
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
	
}
