package team.creative.littletiles.common.placement.selection;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.client.render.box.RenderBox;
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

public class AreaSelectionMode extends SelectionMode {
    
    @Override
    public SelectionScanResult scan(Level level, ItemStack stack, SelectionComponent config) {
        var nbt = config.getConfig();
        BlockPos pos = null;
        if (nbt.contains("pos1")) {
            int[] array = nbt.getIntArray("pos1");
            pos = new BlockPos(array[0], array[1], array[2]);
        }
        
        BlockPos pos2 = null;
        if (nbt.contains("pos2")) {
            int[] array = nbt.getIntArray("pos2");
            pos2 = new BlockPos(array[0], array[1], array[2]);
        }
        
        if (pos == null && pos2 == null)
            return null;
        
        if (pos == null)
            pos = pos2;
        else if (pos2 == null)
            pos2 = pos;
        
        SelectionScanResult result = new SelectionScanResult(level);
        result.addBlocks(pos, pos2);
        return result;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public SelectionComponent leftClick(LittleActionSource source, ItemStack stack, SelectionComponent config, LittleGrid positionGrid, BlockHitResult hit,
            @Nullable LittleTileContext context, boolean secondMode) {
        var nbt = config.getConfig();
        var pos = hit.getBlockPos();
        nbt.putIntArray("pos1", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        if (!source.isClient())
            source.sendText(Component.translatable("selection.mode.area.pos.first", pos.getX(), pos.getY(), pos.getZ()));
        return config.withConfig(nbt);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public SelectionComponent rightClick(LittleActionSource source, ItemStack stack, SelectionComponent config, LittleGrid positionGrid, BlockHitResult hit,
            @Nullable LittleTileContext context, boolean secondMode) {
        var nbt = config.getConfig();
        var pos = hit.getBlockPos();
        nbt.putIntArray("pos2", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        if (!source.isClient())
            source.sendText(Component.translatable("selection.mode.area.pos.second", pos.getX(), pos.getY(), pos.getZ()));
        return config.withConfig(nbt);
    }
    
    @Override
    public LittleGroup select(Level level, LittleActionSource source, SelectionParameters selection, ItemStack stack, SelectionComponent config) throws LittleActionException {
        var nbt = config.getConfig();
        BlockPos pos = null;
        if (nbt.contains("pos1")) {
            int[] array = nbt.getIntArray("pos1");
            pos = new BlockPos(array[0], array[1], array[2]);
        }
        
        BlockPos pos2 = null;
        if (nbt.contains("pos2")) {
            int[] array = nbt.getIntArray("pos2");
            pos2 = new BlockPos(array[0], array[1], array[2]);
        }
        
        if (pos == null && pos2 == null)
            return null;
        
        if (pos == null)
            pos = pos2;
        else if (pos2 == null)
            pos2 = pos;
        
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
        
        AABB bb = new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
        for (LittleEntity animation : LittleTiles.ANIMATION_HANDLERS.get(level).find(bb))
            search.scanLevel(animation.getSubLevel(), minX, minY, minZ, maxX, maxY, maxZ, null);
        
        return search.build();
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void buildRender(Level level, ItemStack stack, SelectionComponent config, SelectionRenderQueue queue) {
        var nbt = config.getConfig();
        BlockPos pos = null;
        if (nbt.contains("pos1")) {
            int[] array = nbt.getIntArray("pos1");
            pos = new BlockPos(array[0], array[1], array[2]);
        }
        
        BlockPos pos2 = null;
        if (nbt.contains("pos2")) {
            int[] array = nbt.getIntArray("pos2");
            pos2 = new BlockPos(array[0], array[1], array[2]);
        }
        
        if (pos != null)
            queue.addBox(new RenderBox(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, (BlockState) null), true);
        
        if (pos2 != null)
            queue.addBox(new RenderBox(pos2.getX(), pos2.getY(), pos2.getZ(), pos2.getX() + 1, pos2.getY() + 1, pos2.getZ() + 1, (BlockState) null), true);
        
        if (pos != null && pos2 != null) {
            int minX = Math.min(pos.getX(), pos2.getX());
            int minY = Math.min(pos.getY(), pos2.getY());
            int minZ = Math.min(pos.getZ(), pos2.getZ());
            int maxX = Math.max(pos.getX(), pos2.getX());
            int maxY = Math.max(pos.getY(), pos2.getY());
            int maxZ = Math.max(pos.getZ(), pos2.getZ());
            var box = new RenderBox(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1, (BlockState) null);
            box.grow(0.01F);
            queue.addBox(box, false, 100);
        }
    }
}
