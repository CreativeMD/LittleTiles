package team.creative.littletiles.common.placement.selection;

import java.util.ArrayList;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
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
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.item.component.SelectionComponent;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxesNoOverlap;
import team.creative.littletiles.common.math.box.collection.LittleBoxesSimple;
import team.creative.littletiles.common.math.vec.LittleVec;

public class TileSelectionMode extends SelectionMode {
    
    @Override
    public SelectionScanResult scan(Level level, ItemStack stack, SelectionComponent config) {
        var nbt = config.getConfig();
        if (!nbt.contains("boxes"))
            return null;
        
        var boxes = new LittleBoxesNoOverlap(nbt.getCompound("boxes"));
        if (boxes.isEmpty())
            return null;
        
        SelectionScanResult result = new SelectionScanResult(level);
        
        for (BlockPos pos : boxes.generateBlockWise().keySet())
            result.addBlock(pos);
        return result;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public SelectionComponent leftClick(LittleActionSource source, ItemStack stack, SelectionComponent config, LittleGrid positionGrid, BlockHitResult hit,
            @Nullable LittleTileContext context, boolean secondMode) {
        var nbt = config.getConfig();
        var boxes = nbt.contains("boxes") ? new LittleBoxesNoOverlap(nbt.getCompound("boxes")) : new LittleBoxesNoOverlap(hit.getBlockPos().immutable(), LittleGrid.MIN);
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
        return config.withConfig(nbt);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public SelectionComponent rightClick(LittleActionSource source, ItemStack stack, SelectionComponent config, LittleGrid positionGrid, BlockHitResult hit,
            @Nullable LittleTileContext context, boolean secondMode) {
        var nbt = config.getConfig();
        if (source.asPlayer().isCrouching()) {
            nbt.remove("boxes");
            return config.withConfig(nbt);
        }
        
        var boxes = nbt.contains("boxes") ? new LittleBoxesNoOverlap(nbt.getCompound("boxes")) : new LittleBoxesNoOverlap(hit.getBlockPos().immutable(), LittleGrid.MIN);
        if (context == null || !context.isComplete())
            boxes.cutOut(LittleGrid.MIN, hit.getBlockPos(), LittleGrid.MIN.box());
        else if (secondMode)
            boxes.cutOut(LittleGrid.MIN, hit.getBlockPos(), LittleGrid.MIN.box());
        else
            boxes.cutOut(context.parent.getGrid(), hit.getBlockPos(), context.box.copy());
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
        
        var surroundingBox = boxes.getSurroundingBox();
        AABB bb = surroundingBox.getBB(boxes.getGrid(), boxes.pos);
        
        if (source.isPlayer()) {
            LittlePermissionBuild perm = LittleTiles.CONFIG.build.get(source.asPlayer());
            if (perm.blueprintSizeLimit.isEnabled() && bb.getXsize() * bb.getYsize() * bb.getZsize() > perm.blueprintSizeLimit.value)
                throw new AreaTooLarge(perm);
        }
        
        SelectionBuilder search = new SelectionBuilder(selection);
        for (Entry<BlockPos, ArrayList<LittleBox>> entry : boxes.generateBlockWise().entrySet())
            search.addBoxes(level, entry.getKey(), new LittleBoxesSimple(entry.getKey(), boxes.getGrid(), entry.getValue()));
        
        for (LittleEntity animation : LittleTiles.ANIMATION_HANDLERS.get(level).find(bb))
            for (Entry<BlockPos, ArrayList<LittleBox>> entry : boxes.generateBlockWise().entrySet())
                search.addBoxes(animation.getSubLevel(), entry.getKey(), new LittleBoxesSimple(entry.getKey(), boxes.getGrid(), entry.getValue()));
            
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
        
        LittleVec vec = new LittleVec(0, 0, 0);
        for (Entry<BlockPos, ArrayList<LittleBox>> entry : boxes.generateBlockWise().entrySet()) {
            vec.set(boxes.grid, entry.getKey());
            for (LittleBox box : entry.getValue())
                queue.addBox(box.getRenderingBox(boxes.grid, vec), true);
        }
    }
    
}
