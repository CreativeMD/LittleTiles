package team.creative.littletiles.common.placement.selection;

import net.minecraft.client.KeyMapping;
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
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.config.LittlePermissionBuild;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.item.component.SelectionComponent;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.placement.PlacementHelper;

public class ExactAreaSelectionMode extends SelectionMode {
    
    @Override
    public SelectionScanResult scan(Level level, ItemStack stack, SelectionComponent config) {
        SelectionScanResult result = new SelectionScanResult(level);
        
        var nbt = config.getConfig();
        LittleBoxAbsolute pos1 = null;
        if (nbt.contains("pos1"))
            pos1 = LittleBoxAbsolute.of(nbt.getIntArray("pos1"));
        
        LittleBoxAbsolute pos2 = null;
        if (nbt.contains("pos2"))
            pos2 = LittleBoxAbsolute.of(nbt.getIntArray("pos2"));
        
        if (pos1 == null && pos2 != null)
            pos1 = pos2;
        else if (pos1 != null && pos2 != null)
            pos1.include(pos2);
        
        if (pos1 != null)
            result.addBlocks(pos1);
        
        return result;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public SelectionComponent leftClick(LittleActionSource source, ItemStack stack, SelectionComponent config, LittleGrid positionGrid, BlockHitResult hit,
            LittleTileContext context, boolean secondMode) {
        var nbt = config.getConfig();
        var pos = PlacementHelper.getPositionInside(source.getActionLevel(), hit, positionGrid);
        nbt.putIntArray("pos1", pos.toAbsoluteBox().toArray());
        return config.withConfig(nbt);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public SelectionComponent rightClick(LittleActionSource source, ItemStack stack, SelectionComponent config, LittleGrid positionGrid, BlockHitResult hit,
            LittleTileContext context, boolean secondMode) {
        var nbt = config.getConfig();
        var pos = PlacementHelper.getPositionInside(source.getActionLevel(), hit, positionGrid);
        nbt.putIntArray("pos2", pos.toAbsoluteBox().toArray());
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
                var box = LittleBoxAbsolute.of(nbt.getIntArray("pos" + marked));
                LittleVecGrid vec = new LittleVecGrid(new LittleVec(facing), positionGrid);
                box.sameGrid(vec, () -> box.box.add(vec.getVec()));
                nbt.putIntArray("pos" + marked, box.toArray());
                return config.withConfig(nbt);
            }
        }
        return super.keyPressed(source, stack, config, positionGrid, secondMode, key);
    }
    
    @Override
    public LittleGroup select(Level level, LittleActionSource source, SelectionParameters selection, ItemStack stack, SelectionComponent config) throws LittleActionException {
        var nbt = config.getConfig();
        LittleBoxAbsolute pos1 = null;
        if (nbt.contains("pos1"))
            pos1 = LittleBoxAbsolute.of(nbt.getIntArray("pos1"));
        
        LittleBoxAbsolute pos2 = null;
        if (nbt.contains("pos2"))
            pos2 = LittleBoxAbsolute.of(nbt.getIntArray("pos2"));
        
        if (pos1 == null && pos2 != null)
            pos1 = pos2;
        
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
        
        AABB bb = pos1.toAABB();
        
        if (source.isPlayer()) {
            LittlePermissionBuild perm = LittleTiles.CONFIG.build.get(source.asPlayer());
            if (perm.blueprintSizeLimit.isEnabled() && bb.getXsize() * bb.getYsize() * bb.getZsize() > perm.blueprintSizeLimit.value)
                throw new AreaTooLarge(perm);
        }
        
        SelectionBuilder search = new SelectionBuilder(selection);
        search.scanLevel(level, minX, minY, minZ, maxX, maxY, maxZ, pos1);
        
        for (LittleEntity animation : LittleTiles.ANIMATION_HANDLERS.get(level).find(bb))
            search.scanLevel(animation.getSubLevel(), minX, minY, minZ, maxX, maxY, maxZ, pos1);
        
        return search.build();
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void buildRender(Level level, ItemStack stack, SelectionComponent config, SelectionRenderQueue queue) {
        var nbt = config.getConfig();
        int marked = nbt.getInt("marked");
        LittleBoxAbsolute pos1 = null;
        if (nbt.contains("pos1"))
            pos1 = LittleBoxAbsolute.of(nbt.getIntArray("pos1"));
        
        LittleBoxAbsolute pos2 = null;
        if (nbt.contains("pos2"))
            pos2 = LittleBoxAbsolute.of(nbt.getIntArray("pos2"));
        
        if (pos1 != null) {
            var box = pos1.getRenderingBox();
            if (marked == 1)
                box.color = ColorUtils.ORANGE;
            queue.addBox(box, true);
        }
        
        if (pos2 != null) {
            var box = pos2.getRenderingBox();
            if (marked == 2)
                box.color = ColorUtils.ORANGE;
            queue.addBox(box, true);
        }
        
        if (pos1 != null && pos2 != null) {
            pos1.include(pos2);
            var box = pos1.getRenderingBox();
            box.grow(0.01F);
            queue.addBox(box, false, 100);
        }
    }
    
}
