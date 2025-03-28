package team.creative.littletiles.common.placement.selection;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import team.creative.creativecore.common.util.math.box.ABB;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.config.LittleBuildingConfig;
import team.creative.littletiles.common.config.LittleTilesConfig.AreaTooLarge;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.item.component.SelectionComponent;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.mod.chiselsandbits.ChiselsAndBitsManager;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

public class AreaSelectionMode extends SelectionMode {
    
    @Override
    public SelectionResult scan(Level level, SelectionComponent component) {
        var nbt = component.getConfig();
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
        
        SelectionResult result = new SelectionResult(level);
        result.addBlocks(pos, pos2);
        return result;
    }
    
    @Override
    public SelectionComponent leftClick(Player player, SelectionComponent component, BlockPos pos) {
        var nbt = component.getConfig();
        nbt.putIntArray("pos1", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        if (!player.level().isClientSide)
            player.sendSystemMessage(Component.translatable("selection.mode.area.pos.first", pos.getX(), pos.getY(), pos.getZ()));
        return component.withConfig(nbt);
    }
    
    @Override
    public SelectionComponent rightClick(Player player, SelectionComponent component, BlockPos pos) {
        var nbt = component.getConfig();
        nbt.putIntArray("pos2", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        if (!player.level().isClientSide)
            player.sendSystemMessage(Component.translatable("selection.mode.area.pos.second", pos.getX(), pos.getY(), pos.getZ()));
        return component.withConfig(nbt);
    }
    
    @Override
    public LittleGroup select(SelectionParameters selection, SelectionComponent component) throws LittleActionException {
        var nbt = component.getConfig();
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
        
        LittleBuildingConfig config = LittleTiles.CONFIG.build.get(selection.player());
        
        if (config.blueprintSizeLimit.isEnabled() && (maxX - minX) * (maxY - minY) * (maxZ - minZ) > config.blueprintSizeLimit.value)
            throw new AreaTooLarge(selection.player(), config);
        
        AreaSelectionSearch search = new AreaSelectionSearch(selection);
        search.scanLevel(selection.level(), minX, minY, minZ, maxX, maxY, maxZ);
        
        AABB bb = new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
        for (LittleEntity animation : LittleTiles.ANIMATION_HANDLERS.get(selection.level()).find(bb))
            search.scanLevel(animation.getSubLevel(), animation.getOrigin().getOBB(bb));
        
        return search.build();
    }
    
    public static class AreaSelectionSearch {
        
        public final SelectionParameters selection;
        private final List<LittleStructure> structures;
        private final List<LittleGroup> children = new ArrayList<>();
        private final LittleGroup previews = new LittleGroup();
        private final MutableBlockPos temp = new MutableBlockPos();
        
        public AreaSelectionSearch(SelectionParameters selection) throws AreaTooLarge {
            this.selection = selection;
            this.structures = selection.rememberStructure() ? new ArrayList<>() : null;
        }
        
        public void scanLevel(LevelAccessor level, ABB bb) throws LittleActionException {
            scanLevel(level, Mth.floor(bb.minX), Mth.floor(bb.minY), Mth.floor(bb.minZ), Mth.ceil(bb.maxX), Mth.ceil(bb.maxY), Mth.ceil(bb.maxZ));
        }
        
        public void scanLevel(LevelAccessor level, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) throws LittleActionException {
            BlockPos center = new BlockPos(minX, minY, minZ);
            for (int posX = minX; posX <= maxX; posX++) {
                for (int posY = minY; posY <= maxY; posY++) {
                    for (int posZ = minZ; posZ <= maxZ; posZ++) {
                        temp.set(posX, posY, posZ);
                        if (selection.includeBE()) {
                            BlockEntity blockEntity = level.getBlockEntity(temp);
                            
                            if (selection.includeLT() && blockEntity instanceof BETiles be)
                                for (IParentCollection parent : be.groups()) {
                                    if (selection.rememberStructure() && parent.isStructure()) {
                                        try {
                                            LittleStructure structure = parent.getStructure();
                                            while (structure.getParent() != null)
                                                structure = structure.getParent().getStructure();
                                            structure.checkConnections();
                                            if (!structures.contains(structure)) {
                                                children.add(structure.getPreviews(center));
                                                structures.add(structure);
                                            }
                                        } catch (CorruptedConnectionException | NotYetConnectedException e) {
                                            continue;
                                        }
                                        
                                    } else
                                        for (LittleTile tile : parent) {
                                            tile = tile.copy();
                                            tile.move(new LittleVec(parent.getGrid().toGrid(posX - minX), parent.getGrid().toGrid(posY - minY), parent.getGrid().toGrid(
                                                posZ - minZ)));
                                            previews.add(parent.getGrid(), tile, tile);
                                        }
                                }
                            
                            if (selection.includeCB()) {
                                LittleGroup specialPreviews = ChiselsAndBitsManager.getGroup(blockEntity);
                                if (specialPreviews != null) {
                                    specialPreviews.move(new LittleVecGrid(new LittleVec(previews.getGrid().toGrid(posX - minX), previews.getGrid().toGrid(posY - minY), previews
                                            .getGrid().toGrid(posZ - minZ)), previews.getGrid()));
                                    previews.add(specialPreviews);
                                    continue;
                                }
                            }
                        }
                        
                        if (selection.includeVanilla()) {
                            BlockState state = level.getBlockState(temp);
                            if (LittleAction.isBlockValid(state)) {
                                LittleBox box = previews.getGrid().box();
                                box.add(new LittleVec((posX - minX) * previews.getGrid().count, (posY - minY) * previews.getGrid().count, (posZ - minZ) * previews
                                        .getGrid().count));
                                previews.add(previews.getGrid(), new LittleElement(state, ColorUtils.WHITE), box);
                            }
                        }
                    }
                }
            }
        }
        
        public LittleGroup build() {
            if (children.isEmpty())
                return previews;
            List<LittleGroup> newChildren = new ArrayList<>();
            for (LittleGroup child : previews.children.children())
                newChildren.add(child);
            newChildren.addAll(children);
            return new LittleGroup(previews, newChildren);
        }
    }
    
}
