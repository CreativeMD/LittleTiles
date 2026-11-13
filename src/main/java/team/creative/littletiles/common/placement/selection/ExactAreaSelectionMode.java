package team.creative.littletiles.common.placement.selection;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.tool.LittleToolSelection.SelectionRenderQueue;
import team.creative.littletiles.common.action.exception.AreaTooLarge;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.action.source.LittleActionSource;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.config.LittlePermissionBuild;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.item.component.SelectionComponent;
import team.creative.littletiles.common.level.context.ILittleLevelContext;
import team.creative.littletiles.common.math.box.LittleBoxLevel;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.mod.sable.SableContext;
import team.creative.littletiles.common.placement.PlacementHelper;

public class ExactAreaSelectionMode extends SelectionMode {
    
    @Override
    public SelectionScanResult scan(Level level, ItemStack stack, SelectionComponent config) {
        SelectionScanResult result = new SelectionScanResult(level);
        
        var nbt = config.getConfig();
        LittleBoxLevel pos1 = LittleBoxLevel.tryOf(level, nbt.get("pos1"));
        LittleBoxLevel pos2 = LittleBoxLevel.tryOf(level, nbt.get("pos2"));
        
        if (pos1 == null && pos2 != null)
            pos1 = pos2;
        else if (pos1 != null && pos2 != null) {
            if (!pos1.context.is(pos2.context))
                pos2 = pos2.convertTo(pos1.context);
            pos1.include(pos2);
        }
        
        if (pos1 != null)
            result.addBlocks(pos1);
        
        return result;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public SelectionComponent leftClick(LittleActionSource source, ItemStack stack, SelectionComponent config, ILittleLevelContext level, LittleGrid positionGrid,
            BlockHitResult hit, LittleTileContext context, boolean secondMode) {
        var nbt = config.getConfig();
        var pos = PlacementHelper.getPositionInside(level.asLevel(source.getActionLevel()), hit, positionGrid);
        nbt.put("pos1", new LittleBoxLevel(level, pos.box()).write());
        return config.withConfig(nbt);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public SelectionComponent rightClick(LittleActionSource source, ItemStack stack, SelectionComponent config, ILittleLevelContext level, LittleGrid positionGrid,
            BlockHitResult hit, LittleTileContext context, boolean secondMode) {
        var nbt = config.getConfig();
        var pos = PlacementHelper.getPositionInside(level.asLevel(source.getActionLevel()), hit, positionGrid);
        nbt.put("pos2", new LittleBoxLevel(level, pos.box()).write());
        return config.withConfig(nbt);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public SelectionComponent keyPressed(LittleActionSource source, ItemStack stack, SelectionComponent config, LittleGrid positionGrid, boolean secondMode, KeyMapping key) {
        var nbt = config.getConfig();
        if (key == LittleTilesClient.KEY_MARK) {
            int marked = nbt.getInt("marked");
            nbt.putInt("marked", (marked + 1) % 3);
            return config.withConfig(nbt);
        }
        int marked = nbt.getInt("marked");
        if (marked > 0) {
            var facing = LittleTilesClient.facingFromKeybind(source.asPlayer(), key);
            if (facing != null) {
                var box = LittleBoxLevel.tryOf(source.getActionLevel(), nbt.get("pos" + marked));
                if (box != null) {
                    LittleVecGrid vec = new LittleVecGrid(new LittleVec(facing), positionGrid);
                    vec.getVec().scale(Screen.hasControlDown() ? positionGrid.count : 1);
                    box.sameGrid(vec, () -> box.box.add(vec.getVec()));
                    nbt.put("pos" + marked, box.write());
                    return config.withConfig(nbt);
                }
            }
        }
        return super.keyPressed(source, stack, config, positionGrid, secondMode, key);
    }
    
    @Override
    public SelectionBuilder selectionBuilder(Level level, LittleActionSource source, ItemStack stack, SelectionComponent config) throws LittleActionException {
        var nbt = config.getConfig();
        LittleBoxLevel pos1 = LittleBoxLevel.tryOf(level, nbt.get("pos1"));
        LittleBoxLevel pos2 = LittleBoxLevel.tryOf(level, nbt.get("pos2"));
        
        if (pos1 == null && pos2 != null)
            pos1 = pos2;
        else if (pos1 != null && pos2 != null && !pos1.context.is(pos2.context))
            pos2 = pos2.convertTo(pos1.context);
        
        if (pos1 == null)
            return null;
        
        int minX = Math.min(pos1.getMinPos(Axis.X), pos2.getMinPos(Axis.X));
        int minY = Math.min(pos1.getMinPos(Axis.Y), pos2.getMinPos(Axis.Y));
        int minZ = Math.min(pos1.getMinPos(Axis.Z), pos2.getMinPos(Axis.Z));
        int maxX = Math.max(pos1.getMaxPos(Axis.X), pos2.getMaxPos(Axis.X));
        int maxY = Math.max(pos1.getMaxPos(Axis.Y), pos2.getMaxPos(Axis.Y));
        int maxZ = Math.max(pos1.getMaxPos(Axis.Z), pos2.getMaxPos(Axis.Z));
        
        if (pos1 != null && pos2 != null)
            pos1.include(pos2);
        
        level = pos1.context.asLevel(level);
        AABB bb;
        if (pos1.context instanceof SableContext)
            bb = pos1.toAABB();
        else
            bb = new AABB(pos1.context.toRealWorld(pos1.getMin().getVec3()), pos1.context.toRealWorld(pos1.getMax().getVec3()));
        
        if (source.isPlayer()) {
            LittlePermissionBuild perm = LittleTiles.CONFIG.build.get(source.asPlayer());
            if (perm.blueprintSizeLimit.isEnabled() && bb.getXsize() * bb.getYsize() * bb.getZsize() > perm.blueprintSizeLimit.value)
                throw new AreaTooLarge(perm);
        }
        
        SelectionBuilder search = new SelectionBuilder();
        search.scanLevel(level, minX, minY, minZ, maxX, maxY, maxZ, pos1);
        
        for (LittleEntity animation : LittleTiles.ANIMATION_HANDLERS.get(level).find(bb))
            if (pos1.context.isChildOf(animation.getSubLevel()))
                search.scanLevel(animation.getSubLevel(), minX, minY, minZ, maxX, maxY, maxZ, pos1);
            
        return search;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void buildRender(Level level, ItemStack stack, SelectionComponent config, SelectionRenderQueue queue) {
        var nbt = config.getConfig();
        int marked = nbt.getInt("marked");
        
        LittleBoxLevel pos1 = LittleBoxLevel.tryOf(level, nbt.get("pos1"));
        LittleBoxLevel pos2 = LittleBoxLevel.tryOf(level, nbt.get("pos2"));
        
        if (pos1 != null && pos2 != null && !pos1.context.is(pos2.context))
            pos2 = pos2.convertTo(pos1.context);
        
        if (pos1 != null) {
            queue.setContext(pos1.context);
            queue.setOrigin(pos1.pos);
            queue.addBox(pos1.toABB(), true, marked == 1 ? ColorUtils.ORANGE : -1);
        }
        
        if (pos2 != null) {
            if (pos1 == null) {
                queue.setContext(pos2.context);
                queue.setOrigin(pos2.pos);
            }
            queue.addBox(pos2.toABB(), true, marked == 2 ? ColorUtils.ORANGE : -1);
        }
        
        if (pos1 != null && pos2 != null) {
            pos1.include(pos2);
            var box = pos1.toABB();
            box.inflate(0.01);
            queue.addBox(box, false, -1, 100);
        }
    }
    
}
