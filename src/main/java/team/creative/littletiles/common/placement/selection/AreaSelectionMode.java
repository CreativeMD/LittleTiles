package team.creative.littletiles.common.placement.selection;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.box.ABB;
import team.creative.littletiles.LittleTiles;
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
import team.creative.littletiles.common.level.context.ILittleLevelContext;
import team.creative.littletiles.common.math.vec.BlockPosLevel;
import team.creative.littletiles.common.mod.sable.SableContext;

public class AreaSelectionMode extends SelectionMode {
    
    @Override
    public SelectionScanResult scan(Level level, ItemStack stack, SelectionComponent config) {
        var nbt = config.getConfig();
        BlockPosLevel pos = BlockPosLevel.tryOf(level, nbt.get("pos1"));
        BlockPosLevel pos2 = BlockPosLevel.tryOf(level, nbt.get("pos2"));
        
        if (pos == null && pos2 == null)
            return null;
        
        if (pos == null)
            pos = pos2;
        else if (pos2 == null)
            pos2 = pos;
        
        level = pos.context.asLevel(level);
        if (!pos.context.is(pos2.context))
            pos2 = pos2.convertTo(pos.context);
        
        SelectionScanResult result = new SelectionScanResult(level);
        result.addBlocks(pos.pos, pos2.pos);
        return result;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public SelectionComponent leftClick(LittleActionSource source, ItemStack stack, SelectionComponent config, ILittleLevelContext level, LittleGrid positionGrid,
            BlockHitResult hit, @Nullable LittleTileContext context, boolean secondMode) {
        var nbt = config.getConfig();
        var pos = new BlockPosLevel(level, hit.getBlockPos());
        nbt.put("pos1", pos.write());
        if (!source.isClient())
            source.sendText(Component.translatable("selection.mode.area.pos.first", pos.getX(), pos.getY(), pos.getZ()));
        return config.withConfig(nbt);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public SelectionComponent rightClick(LittleActionSource source, ItemStack stack, SelectionComponent config, ILittleLevelContext level, LittleGrid positionGrid,
            BlockHitResult hit, @Nullable LittleTileContext context, boolean secondMode) {
        var nbt = config.getConfig();
        var pos = new BlockPosLevel(level, hit.getBlockPos());
        nbt.put("pos2", pos.write());
        if (!source.isClient())
            source.sendText(Component.translatable("selection.mode.area.pos.second", pos.getX(), pos.getY(), pos.getZ()));
        return config.withConfig(nbt);
    }
    
    @Override
    public LittleGroup select(Level level, LittleActionSource source, SelectionParameters selection, ItemStack stack, SelectionComponent config) throws LittleActionException {
        var nbt = config.getConfig();
        BlockPosLevel pos = BlockPosLevel.tryOf(level, nbt.get("pos1"));
        BlockPosLevel pos2 = BlockPosLevel.tryOf(level, nbt.get("pos2"));
        
        if (pos == null && pos2 == null)
            return null;
        
        if (pos == null)
            pos = pos2;
        else if (pos2 == null)
            pos2 = pos;
        
        level = pos.context.asLevel(level);
        if (!pos.context.is(pos2.context))
            pos2 = pos2.convertTo(pos.context);
        
        int minX = Math.min(pos.getX(), pos2.getX());
        int minY = Math.min(pos.getY(), pos2.getY());
        int minZ = Math.min(pos.getZ(), pos2.getZ());
        int maxX = Math.max(pos.getX(), pos2.getX());
        int maxY = Math.max(pos.getY(), pos2.getY());
        int maxZ = Math.max(pos.getZ(), pos2.getZ());
        
        if (source.isPlayer()) {
            LittlePermissionBuild perm = LittleTiles.CONFIG.build.get(source.asPlayer());
            if (perm.blueprintSizeLimit.isEnabled() && (maxX - minX) * (maxY - minY) * (maxZ - minZ) > perm.blueprintSizeLimit.value)
                throw new AreaTooLarge(perm);
        }
        
        SelectionBuilder search = new SelectionBuilder(selection);
        search.scanLevel(level, minX, minY, minZ, maxX, maxY, maxZ, null);
        
        BlockPos realMin = BlockPos.containing(pos.context.toRealWorld(pos.pos.getCenter()));
        BlockPos realMax = BlockPos.containing(pos2.context.toRealWorld(pos2.pos.getCenter()));
        AABB bb = new AABB(realMin.getX(), realMin.getY(), realMin.getZ(), realMax.getX() + 1, realMax.getY() + 1, realMax.getZ() + 1);
        if (!(pos.context instanceof SableContext)) {
            minX = Math.min(realMin.getX(), realMax.getX());
            minY = Math.min(realMin.getY(), realMax.getY());
            minZ = Math.min(realMin.getZ(), realMax.getZ());
            maxX = Math.max(realMin.getX(), realMax.getX());
            maxY = Math.max(realMin.getY(), realMax.getY());
            maxZ = Math.max(realMin.getZ(), realMax.getZ());
        }
        
        for (LittleEntity animation : LittleTiles.ANIMATION_HANDLERS.get(level).find(bb))
            if (pos.context.isChildOf(animation.getSubLevel()))
                search.scanLevel(animation.getSubLevel(), minX, minY, minZ, maxX, maxY, maxZ, null);
            
        return search.build();
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void buildRender(Level level, ItemStack stack, SelectionComponent config, SelectionRenderQueue queue) {
        var nbt = config.getConfig();
        
        BlockPosLevel pos = BlockPosLevel.tryOf(level, nbt.get("pos1"));
        BlockPosLevel pos2 = BlockPosLevel.tryOf(level, nbt.get("pos2"));
        
        if (pos != null || pos2 != null) {
            if (pos != null) {
                queue.setOrigin(pos.pos);
                queue.setContext(pos.context);
            } else {
                queue.setOrigin(pos2.pos);
                queue.setContext(pos2.context);
            }
            
            if (pos != null && pos2 != null && !pos.context.is(pos2.context))
                pos2 = pos2.convertTo(pos.context);
        }
        
        if (pos != null)
            queue.addBox(new ABB(pos.pos), true);
        
        if (pos2 != null)
            queue.addBox(new ABB(pos2.pos), true);
        
        if (pos != null && pos2 != null) {
            var box = new ABB(pos.pos, pos2.pos);
            box.inflate(0.01);
            queue.addBox(box, false, -1, 100);
        }
    }
}
