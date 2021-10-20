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
import team.creative.creativecore.common.level.CreativeLevel;
import team.creative.creativecore.common.network.CanBeNull;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.type.HashMapList;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.config.LittleTilesConfig.NotAllowedToEditException;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.level.WorldAnimationHandler;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.structure.exception.MissingAnimationException;

public abstract class LittleActionBoxes extends LittleAction {
    
    public LittleBoxes boxes;
    @CanBeNull
    public UUID levelUUID;
    
    public LittleActionBoxes(Level level, LittleBoxes boxes) {
        this.boxes = boxes;
        if (level instanceof CreativeLevel)
            this.levelUUID = ((CreativeLevel) level).parent.getUUID();
        else
            this.levelUUID = null;
    }
    
    public LittleActionBoxes(UUID levelUUID, LittleBoxes boxes) {
        this.boxes = boxes;
        this.levelUUID = levelUUID;
    }
    
    public LittleActionBoxes() {
        
    }
    
    public abstract void action(Level level, Player player, BlockPos pos, BlockState state, List<LittleBox> boxes, LittleGrid grid) throws LittleActionException;
    
    @Override
    public boolean action(Player player) throws LittleActionException {
        if (boxes.isEmpty())
            return false;
        
        boolean placed = false;
        Level level = player.level;
        if (levelUUID != null) {
            EntityAnimation animation = WorldAnimationHandler.findAnimation(level.isClientSide, levelUUID);
            if (animation == null)
                throw new MissingAnimationException(levelUUID);
            
            level = animation.fakeWorld;
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
            
            placed = true;
            
            action(level, player, pos, state, entry.getValue(), boxes.grid);
        }
        
        level.playSound(null, player, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1, 1);
        return placed;
    }
    
}
