package team.creative.littletiles.common.placement.selection;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.level.LittleAnimationHandlers;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
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
            player.sendMessage(new TranslatableComponent("selection.mode.area.pos.first", pos.getX(), pos.getY(), pos.getZ()), Util.NIL_UUID);
    }
    
    @Override
    public void rightClick(Player player, ItemStack stack, BlockPos pos) {
        stack.getTag().putIntArray("pos2", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        if (!player.level.isClientSide)
            player.sendMessage(new TranslatableComponent("selection.mode.area.pos.second", pos.getX(), pos.getY(), pos.getZ()), Util.NIL_UUID);
    }
    
    @Override
    public void clear(ItemStack stack) {
        stack.getTag().remove("pos1");
        stack.getTag().remove("pos2");
    }
    
    public LittleGroup getGroup(Level world, BlockPos pos, BlockPos pos2, boolean includeVanilla, boolean includeCB, boolean includeLT, boolean rememberStructure) {
        int minX = Math.min(pos.getX(), pos2.getX());
        int minY = Math.min(pos.getY(), pos2.getY());
        int minZ = Math.min(pos.getZ(), pos2.getZ());
        int maxX = Math.max(pos.getX(), pos2.getX());
        int maxY = Math.max(pos.getY(), pos2.getY());
        int maxZ = Math.max(pos.getZ(), pos2.getZ());
        
        LittleGroup previews = new LittleGroup();
        
        boolean includeTE = includeCB || includeLT;
        
        BlockPos center = new BlockPos(minX, minY, minZ);
        MutableBlockPos newPos = new MutableBlockPos();
        
        List<LittleStructure> structures = null;
        if (rememberStructure)
            structures = new ArrayList<>();
        
        for (int posX = minX; posX <= maxX; posX++) {
            for (int posY = minY; posY <= maxY; posY++) {
                for (int posZ = minZ; posZ <= maxZ; posZ++) {
                    newPos.set(posX, posY, posZ);
                    if (includeTE) {
                        BlockEntity tileEntity = world.getBlockEntity(newPos);
                        
                        if (includeLT) {
                            if (tileEntity instanceof BETiles) {
                                BETiles te = (BETiles) tileEntity;
                                for (IParentCollection parent : te.groups()) {
                                    if (rememberStructure && parent.isStructure()) {
                                        try {
                                            LittleStructure structure = parent.getStructure();
                                            while (structure.getParent() != null)
                                                structure = structure.getParent().getStructure();
                                            structure.checkConnections();
                                            if (!structures.contains(structure)) {
                                                previews.addChild(structure.getPreviews(center), false);
                                                structures.add(structure);
                                            }
                                        } catch (CorruptedConnectionException | NotYetConnectedException e) {
                                            continue;
                                        }
                                        
                                    } else
                                        for (LittleTile tile : parent) {
                                            LittlePreview preview = previews.addPreview(null, tile.getPreviewTile(), te.getGrid());
                                            preview.box.add(new LittleVec((posX - minX) * previews.getGrid().count, (posY - minY) * previews
                                                    .getGrid().count, (posZ - minZ) * previews.getGrid().count));
                                        }
                                }
                            }
                        }
                        
                        if (includeCB) {
                            LittlePreviews specialPreviews = ChiselsAndBitsManager.getPreviews(tileEntity);
                            if (specialPreviews != null) {
                                for (int i = 0; i < specialPreviews.size(); i++) {
                                    LittlePreview preview = previews.addPreview(null, specialPreviews.get(i), LittleGrid.get(ChiselsAndBitsManager.convertingFrom));
                                    preview.box.add(new LittleVec((posX - minX) * previews.getGrid().count, (posY - minY) * previews.getGrid().count, (posZ - minZ) * previews
                                            .getGrid().count));
                                }
                                continue;
                            }
                        }
                    }
                    
                    if (includeVanilla) {
                        BlockState state = world.getBlockState(newPos);
                        if (LittleAction.isBlockValid(state)) {
                            LittleTile tile = new LittleTile(state.getBlock(), state.getBlock().getMetaFromState(state));
                            int minSize = LittleGrid.min().count;
                            tile.setBox(new LittleBox(0, 0, 0, minSize, minSize, minSize));
                            LittlePreview preview = previews.addPreview(null, tile.getPreviewTile(), LittleGrid.min());
                            preview.box.add(new LittleVec((posX - minX) * previews.getGrid().count, (posY - minY) * previews.getGrid().count, (posZ - minZ) * previews
                                    .getGrid().count));
                        }
                    }
                }
            }
        }
        return previews;
    }
    
    @Override
    public LittleGroup getGroup(Level level, ItemStack stack, boolean includeVanilla, boolean includeCB, boolean includeLT, boolean rememberStructure) {
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
        
        LittleGroup previews = getGroup(level, pos, pos2, includeVanilla, includeCB, includeLT, rememberStructure);
        
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
        
        for (EntityAnimation animation : LittleAnimationHandlers.getHandler(level).findAnimations(new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1))) {
            if (rememberStructure) {
                try {
                    LittleStructure structure = animation.structure;
                    while (structure.getParent() != null)
                        structure = structure.getParent().getStructure();
                    structure.checkConnections();
                    if (!structures.contains(structure)) {
                        previews.addChild(structure.getPreviews(center), false);
                        structures.add(structure);
                    }
                } catch (CorruptedConnectionException | NotYetConnectedException e) {
                    continue;
                }
                
            } else
                try {
                    for (Pair<IStructureParentCollection, LittleTile> pair : animation.structure.tiles()) {
                        LittlePreview preview = previews.addPreview(null, pair.value.getPreviewTile(), pair.getKey().getGrid());
                        preview.box.add(new LittleVec((pair.key.getPos().getX() - minX) * previews.getGrid().count, (pair.key.getPos().getY() - minY) * previews
                                .getGrid().count, (pair.key.getPos().getZ() - minZ) * previews.getGrid().count));
                    }
                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        }
        
        return previews;
    }
    
}
