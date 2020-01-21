package com.creativemd.littletiles.common.action.block;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.config.SpecialServerConfig;
import com.creativemd.littletiles.common.tiles.vec.LittleAbsoluteBox;
import com.creativemd.littletiles.common.tiles.vec.LittleBoxes;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class LittleActionBoxes extends LittleAction {
	
	public LittleBoxes boxes;
	
	public LittleActionBoxes(LittleBoxes boxes) {
		this.boxes = boxes;
	}
	
	public LittleActionBoxes() {
		
	}
	
	public abstract void action(World world, EntityPlayer player, BlockPos pos, IBlockState state, List<LittleTileBox> boxes, LittleGridContext context) throws LittleActionException;
	
	@Override
	protected boolean action(EntityPlayer player) throws LittleActionException {
		if (boxes.isEmpty())
			return false;
		
		boolean placed = false;
		World world = player.world;
		
		if (SpecialServerConfig.isEditLimited(player)) {
			if (boxes.getSurroundingBox().getPercentVolume(boxes.context) > SpecialServerConfig.maxEditBlocks)
				throw new SpecialServerConfig.NotAllowedToEditException();
		}
		
		HashMapList<BlockPos, LittleTileBox> boxesMap = boxes.split();
		
		for (Iterator<Entry<BlockPos, ArrayList<LittleTileBox>>> iterator = boxesMap.entrySet().iterator(); iterator.hasNext();) {
			Entry<BlockPos, ArrayList<LittleTileBox>> entry = iterator.next();
			BlockPos pos = entry.getKey();
			IBlockState state = world.getBlockState(pos);
			if (!isAllowedToInteract(world, player, pos, false, EnumFacing.EAST)) {
				if (!world.isRemote)
					sendBlockResetToClient(world, (EntityPlayerMP) player, pos);
				continue;
			}
			
			placed = true;
			
			action(world, player, pos, state, entry.getValue(), boxes.context);
		}
		
		world.playSound(null, player.getPosition(), SoundEvents.ENTITY_ITEMFRAME_ADD_ITEM, SoundCategory.BLOCKS, 1, 1);
		return placed;
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		writeBoxes(boxes, buf);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		boxes = readBoxes(buf);
	}
	
	protected LittleActionBoxes assignFlip(LittleActionBoxes action, Axis axis, LittleAbsoluteBox box) {
		action.boxes = this.boxes.copy();
		action.boxes.flip(axis, box);
		return action;
	}
	
}
