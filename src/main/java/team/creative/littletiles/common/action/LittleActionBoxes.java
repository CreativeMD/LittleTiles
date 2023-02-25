package team.creative.littletiles.common.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang3.mutable.MutableInt;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.creativecore.common.network.CanBeNull;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.type.map.HashMapList;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.config.LittleTilesConfig.NotAllowedToEditException;
import team.creative.littletiles.common.entity.level.LittleEntity;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.level.handler.LittleAnimationHandlers;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.structure.exception.MissingAnimationException;

public abstract class LittleActionBoxes extends LittleAction<Boolean> {
    
    public LittleBoxes boxes;
    @CanBeNull
    public UUID levelUUID;
    
    public LittleActionBoxes(Level level, LittleBoxes boxes) {
        this.boxes = boxes;
        if (level instanceof ISubLevel)
            this.levelUUID = ((ISubLevel) level).getHolder().getUUID();
        else
            this.levelUUID = null;
    }
    
    public LittleActionBoxes(UUID levelUUID, LittleBoxes boxes) {
        this.boxes = boxes;
        this.levelUUID = levelUUID;
    }
    
    public LittleActionBoxes() {}
    
    public abstract void action(Level level, Player player, BlockPos pos, BlockState state, List<LittleBox> boxes, LittleGrid grid) throws LittleActionException;
    
    @Override
    public Boolean action(Player player) throws LittleActionException {
        if (boxes.isEmpty())
            return true;
        
        Level level = player.level;
        if (levelUUID != null) {
            LittleEntity animation = LittleAnimationHandlers.find(level.isClientSide, levelUUID);
            if (animation == null)
                throw new MissingAnimationException(levelUUID);
            
            level = (Level) animation.getSubLevel();
        }
        
        if (LittleTiles.CONFIG.isEditLimited(player)) {
            if (boxes.getSurroundingBox().getPercentVolume(boxes.grid) > LittleTiles.CONFIG.build.get(player).maxEditBlocks)
                throw new NotAllowedToEditException(player);
        }
        
        HashMapList<BlockPos, LittleBox> boxesMap = boxes.generateBlockWise();
        MutableInt affectedBlocks = new MutableInt();
        
        try {
            for (BlockPos pos : boxesMap.keySet()) {
                BETiles be = LittleAction.loadBE(player, level, pos, null, false, 0);
                if (be != null)
                    continue;
                BlockState state = level.getBlockState(pos);
                if (state.getMaterial().isReplaceable())
                    continue;
                else if (LittleAction.isBlockValid(state) && LittleAction.canConvertBlock(player, level, pos, state, affectedBlocks.incrementAndGet()))
                    continue;
            }
        } catch (LittleActionException e) {
            for (BlockPos pos : boxesMap.keySet())
                sendBlockResetToClient(level, player, pos);
            throw e;
        }
        
        for (Iterator<Entry<BlockPos, ArrayList<LittleBox>>> iterator = boxesMap.entrySet().iterator(); iterator.hasNext();) {
            Entry<BlockPos, ArrayList<LittleBox>> entry = iterator.next();
            BlockPos pos = entry.getKey();
            BlockState state = level.getBlockState(pos);
            if (!isAllowedToInteract(level, player, pos, false, Facing.EAST)) {
                if (!level.isClientSide)
                    sendBlockResetToClient(level, player, pos);
                continue;
            }
            
            actionDone(level, player);
            
            action(level, player, pos, state, entry.getValue(), boxes.grid);
        }
        
        level.playSound(null, player, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1, 1);
        return true;
    }
    
    public void actionDone(Level level, Player player) {}
    
    protected LittleActionBoxes assignMirror(LittleActionBoxes action, Axis axis, LittleBoxAbsolute box) {
        action.boxes = this.boxes.copy();
        action.boxes.mirror(axis, box);
        return action;
    }
    
    @Override
    public boolean wasSuccessful(Boolean result) {
        return result;
    }
    
    @Override
    public Boolean failed() {
        return false;
    }
    
}
