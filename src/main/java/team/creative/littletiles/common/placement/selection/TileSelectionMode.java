package team.creative.littletiles.common.placement.selection;

import java.util.ArrayList;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.tool.LittleToolSelection.SelectionRenderQueue;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.exception.AreaTooLarge;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.action.source.LittleActionSource;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.config.LittlePermissionBuild;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.item.component.SelectionComponent;
import team.creative.littletiles.common.level.context.ILittleLevelContext;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxesNoOverlap;
import team.creative.littletiles.common.math.box.collection.LittleBoxesSimple;

public class TileSelectionMode extends SelectionMode {
    
    @Override
    public SelectionScanResult scan(Level level, ItemStack stack, SelectionComponent config) {
        var nbt = config.getConfig();
        if (!nbt.contains("boxes"))
            return null;
        
        var boxes = new LittleBoxesNoOverlap(nbt.getCompound("boxes"));
        if (boxes.isEmpty())
            return null;
        
        ILittleLevelContext context = null;
        try {
            context = context(level, boxes, nbt);
        } catch (LittleActionException e) {
            return null;
        }
        
        SelectionScanResult result = new SelectionScanResult(context.asLevel(level));
        for (BlockPos pos : boxes.generateBlockWise().keySet())
            result.addBlock(pos);
        return result;
    }
    
    public ILittleLevelContext context(Level level, LittleBoxesNoOverlap boxes, CompoundTag nbt) throws LittleActionException {
        if (boxes.isEmpty())
            return null;
        if (nbt.contains("level"))
            return ILittleLevelContext.of(level, nbt.getString("level"));
        return ILittleLevelContext.of(level, boxes.pos);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public SelectionComponent leftClick(LittleActionSource source, ItemStack stack, SelectionComponent config, ILittleLevelContext level, LittleGrid positionGrid,
            BlockHitResult hit, @Nullable LittleTileContext context, boolean secondMode) {
        var nbt = config.getConfig();
        var boxes = nbt.contains("boxes") ? new LittleBoxesNoOverlap(nbt.getCompound("boxes")) : new LittleBoxesNoOverlap(hit.getBlockPos().immutable(), LittleGrid.MIN);
        ILittleLevelContext previousContext = null;
        try {
            previousContext = context(source.getActionLevel(), boxes, nbt);
        } catch (LittleActionException e) {
            // context is lost therefore remove all boxes
            boxes = new LittleBoxesNoOverlap(hit.getBlockPos().immutable(), LittleGrid.MIN);
        }
        
        if (previousContext != null && !previousContext.is(level))
            return config; // Abort if it is not the same context
            
        if (context == null || !context.isComplete())
            if (LittleAction.isBlockValid(source.getActionLevel().getBlockState(hit.getBlockPos())))
                boxes.addBox(LittleGrid.MIN, hit.getBlockPos(), LittleGrid.MIN.box());
            else
                return config;
        else if (secondMode) {
            BETiles be = BlockTile.loadBE(source.getActionLevel(), hit.getBlockPos());
            if (be != null) {
                for (Pair<IParentCollection, LittleTile> pair : be.allBoxes())
                    boxes.addBoxes(pair.key, pair.value);
            }
        } else
            boxes.addBox(context.parent.getGrid(), hit.getBlockPos(), context.box.copy());
        nbt.put("boxes", boxes.save(new CompoundTag()));
        if (level.hasId())
            nbt.putString("level", level.getId());
        else
            nbt.remove("level");
        return config.withConfig(nbt);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public SelectionComponent rightClick(LittleActionSource source, ItemStack stack, SelectionComponent config, ILittleLevelContext level, LittleGrid positionGrid,
            BlockHitResult hit, @Nullable LittleTileContext context, boolean secondMode) {
        var nbt = config.getConfig();
        if (source.asPlayer().isCrouching()) {
            nbt.remove("boxes");
            nbt.remove("level");
            return config.withConfig(nbt);
        }
        
        var boxes = nbt.contains("boxes") ? new LittleBoxesNoOverlap(nbt.getCompound("boxes")) : new LittleBoxesNoOverlap(hit.getBlockPos().immutable(), LittleGrid.MIN);
        
        ILittleLevelContext previousContext = null;
        try {
            previousContext = context(source.getActionLevel(), boxes, nbt);
        } catch (LittleActionException e) {
            // context is lost therefore remove all boxes
            boxes = new LittleBoxesNoOverlap(hit.getBlockPos().immutable(), LittleGrid.MIN);
        }
        
        if (previousContext != null && !previousContext.is(level))
            return config; // Abort if it is not the same context
            
        if (context == null || !context.isComplete())
            boxes.cutOut(LittleGrid.MIN, hit.getBlockPos(), LittleGrid.MIN.box());
        else if (secondMode)
            boxes.cutOut(LittleGrid.MIN, hit.getBlockPos(), LittleGrid.MIN.box());
        else
            boxes.cutOut(context.parent.getGrid(), hit.getBlockPos(), context.box.copy());
        if (boxes.isEmpty()) {
            nbt.remove("boxes");
            nbt.remove("level");
        } else
            nbt.put("boxes", boxes.save(new CompoundTag()));
        return config.withConfig(nbt);
    }
    
    @Override
    public LittleGroup select(Level level, LittleActionSource source, SelectionParameters selection, ItemStack stack, SelectionComponent config) throws LittleActionException {
        var nbt = config.getConfig();
        if (!nbt.contains("boxes"))
            return null;
        var boxes = new LittleBoxesNoOverlap(nbt.getCompound("boxes"));
        if (boxes.isEmpty())
            return null;
        
        ILittleLevelContext context = null;
        try {
            context = context(level, boxes, nbt);
        } catch (LittleActionException e) {
            return null;
        }
        
        var surroundingBox = boxes.getSurroundingBox();
        level = context.asLevel(level);
        
        if (source.isPlayer()) {
            LittlePermissionBuild perm = LittleTiles.CONFIG.build.get(source.asPlayer());
            if (perm.blueprintSizeLimit.isEnabled() && surroundingBox.getPercentVolume(boxes.grid) > perm.blueprintSizeLimit.value)
                throw new AreaTooLarge(perm);
        }
        
        SelectionBuilder search = new SelectionBuilder(selection);
        for (Entry<BlockPos, ArrayList<LittleBox>> entry : boxes.generateBlockWise().entrySet())
            search.addBoxes(level, entry.getKey(), new LittleBoxesSimple(entry.getKey(), boxes.getGrid(), entry.getValue()));
        
        return search.build();
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void buildRender(Level level, ItemStack stack, SelectionComponent config, SelectionRenderQueue queue) {
        var nbt = config.getConfig();
        if (!nbt.contains("boxes"))
            return;
        var boxes = new LittleBoxesNoOverlap(nbt.getCompound("boxes"));
        if (boxes.isEmpty())
            return;
        
        ILittleLevelContext context = null;
        try {
            context = context(level, boxes, nbt);
        } catch (LittleActionException e) {
            return;
        }
        
        queue.setOrigin(boxes.pos);
        queue.setContext(context);
        
        for (Entry<BlockPos, ArrayList<LittleBox>> entry : boxes.generateBlockWise().entrySet()) {
            for (LittleBox box : entry.getValue())
                queue.addBox(box.getABB(boxes.grid, entry.getKey()), true);
        }
    }
    
}
