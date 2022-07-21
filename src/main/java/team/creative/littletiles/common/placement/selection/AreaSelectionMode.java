package team.creative.littletiles.common.placement.selection;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.config.LittleTilesConfig.AreaTooLarge;
import team.creative.littletiles.common.entity.LittleLevelEntity;
import team.creative.littletiles.common.level.LittleAnimationHandlers;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.mod.chiselsandbits.ChiselsAndBitsManager;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

public class AreaSelectionMode extends SelectionMode {
    
    @Override
    public SelectionResult generateResult(Level level, ItemStack stack) {
        BlockPos pos = null;
        if (stack.getTag().contains("pos1")) {
            int[] array = stack.getTag().getIntArray("pos1");
            pos = new BlockPos(array[0], array[1], array[2]);
        }
        
        BlockPos pos2 = null;
        if (stack.getTag().contains("pos2")) {
            int[] array = stack.getTag().getIntArray("pos2");
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
    public void leftClick(Player player, ItemStack stack, BlockPos pos) {
        stack.getTag().putIntArray("pos1", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        if (!player.level.isClientSide)
            player.sendSystemMessage(Component.translatable("selection.mode.area.pos.first", pos.getX(), pos.getY(), pos.getZ()));
    }
    
    @Override
    public void rightClick(Player player, ItemStack stack, BlockPos pos) {
        stack.getTag().putIntArray("pos2", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        if (!player.level.isClientSide)
            player.sendSystemMessage(Component.translatable("selection.mode.area.pos.second", pos.getX(), pos.getY(), pos.getZ()));
    }
    
    @Override
    public void clear(ItemStack stack) {
        stack.getTag().remove("pos1");
        stack.getTag().remove("pos2");
    }
    
    public LittleGroup getGroup(Level level, Player player, BlockPos pos, BlockPos pos2, boolean includeVanilla, boolean includeCB, boolean includeLT, boolean rememberStructure) throws LittleActionException {
        int minX = Math.min(pos.getX(), pos2.getX());
        int minY = Math.min(pos.getY(), pos2.getY());
        int minZ = Math.min(pos.getZ(), pos2.getZ());
        int maxX = Math.max(pos.getX(), pos2.getX());
        int maxY = Math.max(pos.getY(), pos2.getY());
        int maxZ = Math.max(pos.getZ(), pos2.getZ());
        
        if (LittleTiles.CONFIG.build.get(player).limitRecipeSize && (maxX - minX) * (maxY - minY) * (maxZ - minZ) > LittleTiles.CONFIG.build.get(player).recipeBlocksLimit)
            throw new AreaTooLarge(player);
        
        LittleGroup previews = new LittleGroup();
        List<LittleGroup> children = new ArrayList<>();
        
        boolean includeBE = includeCB || includeLT;
        
        BlockPos center = new BlockPos(minX, minY, minZ);
        MutableBlockPos newPos = new MutableBlockPos();
        
        List<LittleStructure> structures = null;
        if (rememberStructure)
            structures = new ArrayList<>();
        
        for (int posX = minX; posX <= maxX; posX++) {
            for (int posY = minY; posY <= maxY; posY++) {
                for (int posZ = minZ; posZ <= maxZ; posZ++) {
                    newPos.set(posX, posY, posZ);
                    if (includeBE) {
                        BlockEntity blockEntity = level.getBlockEntity(newPos);
                        
                        if (includeLT) {
                            if (blockEntity instanceof BETiles be) {
                                for (IParentCollection parent : be.groups()) {
                                    if (rememberStructure && parent.isStructure()) {
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
                                            tile.move(new LittleVec((posX - minX) * parent.getGrid().count, (posY - minY) * parent.getGrid().count, (posZ - minZ) * parent
                                                    .getGrid().count));
                                            previews.add(parent.getGrid(), tile, tile);
                                        }
                                }
                            }
                        }
                        
                        if (includeCB) {
                            LittleGroup specialPreviews = ChiselsAndBitsManager.getGroup(blockEntity);
                            if (specialPreviews != null) {
                                specialPreviews.move(new LittleVecGrid(new LittleVec((posX - minX) * previews.getGrid().count, (posY - minY) * previews
                                        .getGrid().count, (posZ - minZ) * previews.getGrid().count), previews.getGrid()));
                                previews.add(specialPreviews);
                                continue;
                            }
                        }
                    }
                    
                    if (includeVanilla) {
                        BlockState state = level.getBlockState(newPos);
                        if (LittleAction.isBlockValid(state)) {
                            LittleBox box = previews.getGrid().box();
                            box.add(new LittleVec((posX - minX) * previews.getGrid().count, (posY - minY) * previews.getGrid().count, (posZ - minZ) * previews.getGrid().count));
                            previews.add(previews.getGrid(), new LittleElement(state, ColorUtils.WHITE), box);
                        }
                    }
                }
            }
        }
        if (children.isEmpty())
            return previews;
        return new LittleGroup(previews, children);
    }
    
    @Override
    public LittleGroup getGroup(Level level, Player player, ItemStack stack, boolean includeVanilla, boolean includeCB, boolean includeLT, boolean rememberStructure) throws LittleActionException {
        BlockPos pos = null;
        if (stack.getTag().contains("pos1")) {
            int[] array = stack.getTag().getIntArray("pos1");
            pos = new BlockPos(array[0], array[1], array[2]);
        }
        
        BlockPos pos2 = null;
        if (stack.getTag().contains("pos2")) {
            int[] array = stack.getTag().getIntArray("pos2");
            pos2 = new BlockPos(array[0], array[1], array[2]);
        }
        
        if (pos == null && pos2 == null)
            return null;
        
        if (pos == null)
            pos = pos2;
        else if (pos2 == null)
            pos2 = pos;
        
        List<LittleGroup> children = new ArrayList<>();
        LittleGroup previews = getGroup(level, player, pos, pos2, includeVanilla, includeCB, includeLT, rememberStructure);
        
        int minX = Math.min(pos.getX(), pos2.getX());
        int minY = Math.min(pos.getY(), pos2.getY());
        int minZ = Math.min(pos.getZ(), pos2.getZ());
        int maxX = Math.max(pos.getX(), pos2.getX());
        int maxY = Math.max(pos.getY(), pos2.getY());
        int maxZ = Math.max(pos.getZ(), pos2.getZ());
        BlockPos center = new BlockPos(minX, minY, minZ);
        
        List<LittleStructure> structures = null;
        if (rememberStructure)
            structures = new ArrayList<>();
        
        for (LittleLevelEntity animation : LittleAnimationHandlers.get(level).find(new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1))) {
            if (rememberStructure) {
                try {
                    LittleStructure structure = animation.getStructure();
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
                try {
                    for (Pair<IStructureParentCollection, LittleTile> pair : animation.getStructure().tiles()) {
                        LittleTile tile = pair.value.copy();
                        tile.move(new LittleVec((pair.key.getPos().getX() - minX) * pair.key.getGrid().count, (pair.key.getPos().getY() - minY) * pair.key
                                .getGrid().count, (pair.key.getPos().getZ() - minZ) * pair.key.getGrid().count));
                        previews.add(pair.key.getGrid(), tile, tile);
                    }
                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        }
        
        if (children.isEmpty())
            return previews;
        List<LittleGroup> newChildren = new ArrayList<>();
        for (LittleGroup child : previews.children.children())
            newChildren.add(child);
        newChildren.addAll(children);
        return new LittleGroup(previews, newChildren);
    }
    
}
