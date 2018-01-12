package com.creativemd.littletiles.common.action.block;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.InventoryUtils;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionCombined;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.LittleActionInteract;
import com.creativemd.littletiles.common.action.block.LittleActionDestroy.StructurePreview;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.items.ItemLittleWrench;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.utils.nbt.LittleNBTCompressionTools;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;

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
	
	public List<LittleTilePreview> replacedTiles;
	public List<LittleTileBox> boxes;

	@Override
	protected boolean action(World world, TileEntityLittleTiles te, LittleTile tile, ItemStack stack,
			EntityPlayer player, RayTraceResult moving, BlockPos pos, boolean secondMode) throws LittleActionException {
		
		if(tile.isStructureBlock)
			return false;
		
		replacedTiles = new ArrayList<>();
		boxes = new ArrayList<>();
		
		if(BlockTile.selectEntireBlock(player, secondMode))
		{
			List<LittleTile> toRemove = new ArrayList<>();
			for (LittleTile toDestroy : te.getTiles()) {
				if(!toDestroy.isStructureBlock && tile.canBeCombined(toDestroy) && toDestroy.canBeCombined(tile))
				{
					LittleTilePreview preview = toDestroy.getPreviewTile();
					preview.box.addOffset(toDestroy.te.getPos());
					replacedTiles.add(preview);
					boxes.add(preview.box);
					toRemove.add(toDestroy);
				}
			}
			
			if(toRemove.isEmpty())
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
			LittleActionPlaceRelative.placeTiles(world, player, previews, null, true, pos, stack, unplaceableTiles, false, EnumFacing.EAST);
			addTilesToInventory(player, unplaceableTiles);
			
		}else{
			LittleTilePreview preview = tile.getPreviewTile();
			preview.box.addOffset(tile.te.getPos());
			replacedTiles.add(preview);
			boxes.add(preview.box);
			addPreviewToInventory(player, replacedTiles);
			
			tile.destroy();
			
			List<LittleTilePreview> toBePlaced = new ArrayList<>();
			toReplace.box = tile.box;
			toBePlaced.add(toReplace);
			drainPreviews(player, toBePlaced);
			
			List<PlacePreviewTile> previews = new ArrayList<>();
			previews.add(toReplace.getPlaceableTile(null, true, null));
			
			ArrayList<LittleTile> unplaceableTiles = new ArrayList<LittleTile>();
			LittleActionPlaceRelative.placeTiles(world, player, previews, null, true, pos, stack, unplaceableTiles, false, EnumFacing.EAST);
			addTilesToInventory(player, unplaceableTiles);
		}
		
		world.playSound((EntityPlayer)null, pos, tile.getSound().getBreakSound(), SoundCategory.BLOCKS, (tile.getSound().getVolume() + 1.0F) / 2.0F, tile.getSound().getPitch() * 0.8F);
		
		return true;
	}

	@Override
	public boolean canBeReverted() {
		return true;
	}

	@Override
	public LittleAction revert() throws LittleActionException {
		return new LittleActionCombined(new LittleActionDestroyBoxes(boxes), new LittleActionPlaceAbsolute(replacedTiles, false));
	}
	
}
