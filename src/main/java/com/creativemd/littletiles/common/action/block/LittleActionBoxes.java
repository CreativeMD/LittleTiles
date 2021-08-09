package com.creativemd.littletiles.common.action.block;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.mutable.MutableInt;

import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.LittleTilesConfig.NotAllowedToEditException;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.tile.math.box.LittleAbsoluteBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
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
    
    public abstract void action(World world, EntityPlayer player, BlockPos pos, IBlockState state, List<LittleBox> boxes, LittleGridContext context) throws LittleActionException;
    
    @Override
    protected boolean action(EntityPlayer player) throws LittleActionException {
        if (boxes.isEmpty())
            return true;
        
        World world = player.world;
        
        if (LittleTiles.CONFIG.isEditLimited(player)) {
            if (boxes.getSurroundingBox().getPercentVolume(boxes.context) > LittleTiles.CONFIG.build.get(player).maxEditBlocks)
                throw new NotAllowedToEditException(player);
        }
        
        HashMapList<BlockPos, LittleBox> boxesMap = boxes.generateBlockWise();
        MutableInt affectedBlocks = new MutableInt();
        
        try {
            for (BlockPos pos : boxesMap.keySet()) {
                TileEntityLittleTiles te = LittleAction.loadTe(player, world, pos, null, false, 0);
                if (te != null)
                    continue;
                IBlockState state = world.getBlockState(pos);
                if (state.getMaterial().isReplaceable())
                    continue;
                else if (LittleAction.isBlockValid(state) && LittleAction.canConvertBlock(player, world, pos, state, affectedBlocks.incrementAndGet()))
                    continue;
            }
        } catch (LittleActionException e) {
            for (BlockPos pos : boxesMap.keySet())
                sendBlockResetToClient(world, player, pos);
            throw e;
        }
        
        for (Iterator<Entry<BlockPos, ArrayList<LittleBox>>> iterator = boxesMap.entrySet().iterator(); iterator.hasNext();) {
            Entry<BlockPos, ArrayList<LittleBox>> entry = iterator.next();
            BlockPos pos = entry.getKey();
            IBlockState state = world.getBlockState(pos);
            if (!isAllowedToInteract(world, player, pos, false, EnumFacing.EAST)) {
                if (!world.isRemote)
                    sendBlockResetToClient(world, player, pos);
                continue;
            }
            
            action(world, player, pos, state, entry.getValue(), boxes.context);
        }
        
        actionDone(player, world);
        
        world.playSound(null, player.getPosition(), SoundEvents.ENTITY_ITEMFRAME_ADD_ITEM, SoundCategory.BLOCKS, 1, 1);
        return true;
    }
    
    public void actionDone(EntityPlayer player, World world) {}
    
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
