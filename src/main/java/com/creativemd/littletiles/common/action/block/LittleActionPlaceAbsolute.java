package com.creativemd.littletiles.common.action.block;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
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
	public boolean placeAll;
	public LittleStructure structure;
	public boolean toVanilla;
	
	public LittleActionPlaceAbsolute(List<LittleTilePreview> previews, LittleStructure structure, boolean forced, boolean placeAll, boolean toVanilla) {
		super();
		this.previews = previews;
		this.forced = forced;
		this.placeAll = placeAll;
		this.structure = structure;
		this.toVanilla = toVanilla;
	}
	
	public LittleActionPlaceAbsolute(List<LittleTilePreview> previews, boolean forced) {
		this(previews, null, forced, false, false);
	}
	
	public LittleActionPlaceAbsolute() {
		super();
	}

	@Override
	public boolean canBeReverted() {
		return true;
	}
	
	public List<LittleTileBox> boxes;

	@Override
	public LittleAction revert() {
		return new LittleActionDestroyBoxes(boxes);
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
			boxes = new ArrayList<>();
			
			for (LittleTilePreview preview : previews) {
				preview = preview.copy();
				boxes.add(preview.box.copy());
				preview.box.subOffset(offset);
				placePreviews.add(preview.getPlaceableTile(null, true, offset));
			}
			
			ArrayList<LittleTile> unplaceableTiles = new ArrayList<LittleTile>();
			placedTiles = LittleActionPlaceRelative.placeTiles(player.world, player, placePreviews, structure, placeAll, pos, null, unplaceableTiles, forced, EnumFacing.EAST);
			
			if(toVanilla)
			{
				BlockPos lastPos = null;
				for (LittleTile tile : placedTiles) {
					if(tile.te.getPos() != lastPos)
					{
						lastPos = tile.te.getPos();
						tile.te.convertBlockToVanilla();
					}
				}
			}
			
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
		buf.writeBoolean(placeAll);
		buf.writeBoolean(toVanilla);
		
		if(structure == null)
			buf.writeBoolean(false);
		else
		{
			buf.writeBoolean(true);
			NBTTagCompound structureNBT = new NBTTagCompound();
			structure.setTiles(null);
			structure.writeToNBT(structureNBT);
			structure.setTiles(new HashMapList<>());
			writeNBT(buf, structureNBT);
		}
	}

	@Override
	public void readBytes(ByteBuf buf) {
		previews = LittleNBTCompressionTools.readPreviews(readNBT(buf).getTagList("list", 10));
		forced = buf.readBoolean();
		placeAll = buf.readBoolean();
		toVanilla = buf.readBoolean();
		
		if(buf.readBoolean())
		{
			structure = LittleStructure.createAndLoadStructure(readNBT(buf), null);
			structure.setTiles(new HashMapList<>());
		}
	}

}
