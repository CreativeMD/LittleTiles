package com.creativemd.littletiles.common.action.block;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.config.SpecialServerConfig;
import com.creativemd.littletiles.common.packet.LittleBlockPacket;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class LittleActionBoxes extends LittleAction {
	
	public List<LittleTileBox> boxes;
	
	public LittleActionBoxes(List<LittleTileBox> boxes) {
		this.boxes = boxes;
	}
	
	public LittleActionBoxes() {
		
	}
	
	public abstract void action(World world, EntityPlayer player, BlockPos pos, IBlockState state, List<LittleTileBox> boxes) throws LittleActionException;

	@Override
	protected boolean action(EntityPlayer player) throws LittleActionException {
		if(boxes.isEmpty())
			return false;
		
		boolean placed = false;
		World world = player.world;
		HashMapList<BlockPos, LittleTileBox> boxesMap = new HashMapList<>();
		
		if(SpecialServerConfig.isEditLimited(player))
		{
			if(LittleTileBox.getSurroundingBox(boxes).getPercentVolume() > SpecialServerConfig.maxEditBlocks)
				throw new SpecialServerConfig.NotAllowedToEditException();
		}
		
		for (int i = 0; i < boxes.size(); i++) {
			boxes.get(i).split(boxesMap);
		}
		
		for (Iterator<Entry<BlockPos, ArrayList<LittleTileBox>>> iterator = boxesMap.entrySet().iterator(); iterator.hasNext();) {
			Entry<BlockPos, ArrayList<LittleTileBox>> entry = iterator.next();
			BlockPos pos = entry.getKey();
			IBlockState state = world.getBlockState(pos);
			if(!isAllowedToInteract(player, pos, false, EnumFacing.EAST))
			{
				world.notifyBlockUpdate(pos, state, state, 3);
				continue ;
			}
			
			placed = true;
			action(world, player, pos, state, entry.getValue());
		}
		
		world.playSound(null, player.getPosition(), SoundEvents.ENTITY_ITEMFRAME_ADD_ITEM, SoundCategory.BLOCKS, 1, 1);
		return placed;
	}

	@Override
	public void writeBytes(ByteBuf buf) {
		buf.writeInt(boxes.size());
		for (LittleTileBox box : boxes) {
			writeLittleBox(box, buf);
		}
	}

	@Override
	public void readBytes(ByteBuf buf) {
		int size = buf.readInt();
		boxes = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			boxes.add(readLittleBox(buf));
		}
	}
	
}
