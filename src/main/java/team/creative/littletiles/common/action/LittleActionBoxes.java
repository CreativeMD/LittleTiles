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
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.creativecore.common.network.CanBeNull;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.type.map.HashMapList;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.action.exception.AreaProtected;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.action.exception.NotAllowedToEditException;
import team.creative.littletiles.common.action.source.LittleActionSource;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.config.LittlePermissionBuild;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.grid.LittleGrid;
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
    
    public abstract void action(Level level, LittleActionSource source, BlockPos pos, BlockState state, List<LittleBox> boxes, LittleGrid grid) throws LittleActionException;
    
    @Override
    public Boolean action(LittleActionSource source) throws LittleActionException {
        if (boxes.isEmpty())
            return true;
        
        Level level = source.getActionLevel();
        if (levelUUID != null) {
            LittleEntity animation = LittleTiles.ANIMATION_HANDLERS.find(level.isClientSide, levelUUID);
            if (animation == null)
                throw new MissingAnimationException(levelUUID);
            
            level = (Level) animation.getSubLevel();
        }
        
        if (source.isPlayer() && LittleTiles.CONFIG.isEditLimited(source.asPlayer())) {
            LittlePermissionBuild config = LittleTiles.CONFIG.build.get(source.asPlayer());
            if (boxes.getSurroundingBox().getPercentVolume(boxes.grid) > config.editBlockLimit.value)
                throw new NotAllowedToEditException(config);
        }
        
        isAllowedToUse(source, boxes);
        
        HashMapList<BlockPos, LittleBox> boxesMap = boxes.generateBlockWise();
        MutableInt affectedBlocks = new MutableInt();
        
        try {
            for (BlockPos pos : boxesMap.keySet()) {
                BETiles be = LittleAction.loadBE(source, level, pos, null, false, 0);
                if (be != null)
                    continue;
                BlockState state = level.getBlockState(pos);
                if (state.is(BlockTags.REPLACEABLE))
                    continue;
                else if (LittleAction.isBlockValid(state) && LittleAction.canConvertBlock(source, level, pos, state, affectedBlocks.incrementAndGet()))
                    continue;
            }
        } catch (LittleActionException e) {
            for (BlockPos pos : boxesMap.keySet())
                sendBlockResetToClient(level, source, pos);
            throw e;
        }
        
        boolean areaProtected = false;
        
        for (Iterator<Entry<BlockPos, ArrayList<LittleBox>>> iterator = boxesMap.entrySet().iterator(); iterator.hasNext();) {
            Entry<BlockPos, ArrayList<LittleBox>> entry = iterator.next();
            BlockPos pos = entry.getKey();
            BlockState state = level.getBlockState(pos);
            if (!isAllowedToInteract(level, source, pos, false, Facing.EAST)) {
                if (!level.isClientSide)
                    sendBlockResetToClient(level, source, pos);
                continue;
            }
            if (requiresBreak() && !fireBlockBreakEvent(level, pos, source)) {
                areaProtected = true;
                continue;
            }
            
            action(level, source, pos, state, entry.getValue(), boxes.grid);
        }
        
        actionDone(level, source);
        
        source.playSound(SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1, 1);
        
        if (areaProtected)
            throw new AreaProtected();
        return true;
    }
    
    public void actionDone(Level level, LittleActionSource source) {}
    
    protected LittleActionBoxes assignMirror(LittleActionBoxes action, Axis axis, LittleBoxAbsolute box) {
        action.boxes = this.boxes.copy();
        action.boxes.mirror(axis, box);
        return action;
    }
    
    @Override
    public void include(LittleBoxes boxes) {
        this.boxes.include(boxes);
    }
    
    @Override
    public void exclude(LittleBoxes boxes) {
        this.boxes.exclude(boxes);
    }
    
    @Override
    public boolean wasSuccessful(Boolean result) {
        return result;
    }
    
    @Override
    public Boolean failed() {
        return false;
    }
    
    public boolean requiresBreak() {
        return true;
    }
    
}
