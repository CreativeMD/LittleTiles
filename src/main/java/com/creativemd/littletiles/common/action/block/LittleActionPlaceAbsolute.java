package com.creativemd.littletiles.common.action.block;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.nbt.LittleNBTCompressionTools;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class LittleActionPlaceAbsolute extends LittleAction {
	
	public List<LittleTilePreview> previews;
	public boolean forced;
	
	public LittleActionPlaceAbsolute(List<LittleTilePreview> previews, boolean forced) {
		super();
		this.previews = previews;
		this.forced = forced;
	}
	
	public LittleActionPlaceAbsolute() {
		super();
	}

	@Override
	public boolean canBeReverted() {
		return true;
	}

	@Override
	public LittleAction revert() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public List<LittleTile> placedTiles;

	@Override
	protected boolean action(EntityPlayer player) throws LittleActionException {
		
		if(drainPreviews(player, previews))
		{
			BlockPos pos = previews.get(0).box.getMinVec().getBlockPos();
			LittleTileVec offset = new LittleTileVec(pos);
			LittleTileVec zero = new LittleTileVec(0, 0, 0);
			ArrayList<PlacePreviewTile> placePreviews = new ArrayList<>();
			
			for (LittleTilePreview preview : previews) {
				preview = preview.copy();
				preview.box.subOffset(offset);
				placePreviews.add(preview.getPlaceableTile(null, true, offset));
			}
			
			ArrayList<LittleTile> unplaceableTiles = new ArrayList<LittleTile>();
			
			placedTiles = LittleActionPlaceRelative.placeTiles(player.world, player, placePreviews, null, pos, null, unplaceableTiles, forced, EnumFacing.EAST);
			
			addTilesToInventory(player, unplaceableTiles);
			return placedTiles != null;
		}
		return false;
	}

	@Override
	public void writeBytes(ByteBuf buf) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag("list", LittleNBTCompressionTools.writePreviews(previews));
		writeNBT(buf, nbt);
		buf.writeBoolean(forced);
	}

	@Override
	public void readBytes(ByteBuf buf) {
		previews = LittleNBTCompressionTools.readPreviews(readNBT(buf).getTagList("list", 10));
		forced = buf.readBoolean();
	}

}
