package team.creative.littletiles.common.action;

import com.creativemd.littletiles.common.tile.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;

import net.minecraft.core.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.mc.WorldUtils;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.block.little.tile.parent.StructureParentCollection;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.ingredient.LittleInventory;
import team.creative.littletiles.common.item.ItemLittleWrench;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.box.collection.LittleBoxesSimple;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

public class LittleActionDestroy extends LittleActionInteract {
    
    public LittleActionDestroy(Level level, BlockPos blockPos, Player player) {
        super(level, blockPos, player);
    }
    
    public LittleActionDestroy() {
        
    }
    
    @Override
    public boolean canBeReverted() {
        return destroyedTiles != null || structurePreview != null;
    }
    
    public LittleAbsolutePreviews destroyedTiles;
    public StructurePreview structurePreview;
    
    @Override
    public LittleAction revert(Player player) {
        if (structurePreview != null)
            return structurePreview.getPlaceAction();
        destroyedTiles.convertToSmallest();
        return new LittleActionPlaceAbsolute(destroyedTiles, PlacementMode.normal);
    }
    
    @Override
    protected boolean requiresBreakEvent() {
        return true;
    }
    
    @Override
    protected boolean action(Level level, BETiles be, LittleTileContext context, ItemStack stack, Player player, BlockHitResult hit, BlockPos pos, boolean secondMode) throws LittleActionException {
        if (parent.isStructure()) {
            try {
                LittleStructure structure = parent.getStructure();
                structure.load();
                structurePreview = new StructurePreview(structure);
                if (needIngredients(player) && !player.world.isRemote)
                    WorldUtils.dropItem(world, structure.getStructureDrop(), pos);
                structure.onLittleTileDestroy();
            } catch (CorruptedConnectionException | NotYetConnectedException e) {
                if (player.getHeldItemMainhand().getItem() instanceof ItemLittleWrench) {
                    ((StructureParentCollection) parent).remove();
                    te.updateTiles();
                } else
                    throw new LittleActionException.StructureNotLoadedException();
            }
        } else {
            LittleInventory inventory = new LittleInventory(player);
            destroyedTiles = new LittleAbsolutePreviews(pos, te.getContext());
            if (BlockTile.selectEntireBlock(player, secondMode)) {
                for (LittleTile toDestroy : parent)
                    destroyedTiles.addTile(parent, toDestroy); // No need to use addPreivew as all previews are inside one block
                    
                checkAndGive(player, inventory, getIngredients(destroyedTiles));
                te.updateTiles(x -> x.noneStructureTiles().clear());
            } else {
                destroyedTiles.addTile(parent, tile); // No need to use addPreivew as all previews are inside one block
                
                checkAndGive(player, inventory, getIngredients(destroyedTiles));
                
                te.updateTiles((x) -> x.get(parent).remove(tile));
            }
        }
        
        world.playSound((EntityPlayer) null, pos, tile.getSound()
                .getBreakSound(), SoundCategory.BLOCKS, (tile.getSound().getVolume() + 1.0F) / 2.0F, tile.getSound().getPitch() * 0.8F);
        
        return true;
    }
    
    @Override
    protected boolean isRightClick() {
        return false;
    }
    
    @Override
    public LittleAction mirror(Axis axis, LittleBoxAbsolute box) {
        LittleBoxes boxes;
        if (structurePreview != null) {
            boxes = new LittleBoxesSimple(structurePreview.previews.pos, structurePreview.previews.getGrid());
            boxes.add(structurePreview.previews.get(0).box);
        } else if (destroyedTiles != null) {
            destroyedTiles.convertToSmallest();
            boxes = new LittleBoxesSimple(blockPos, destroyedTiles.getContext());
            for (LittlePreview preview : destroyedTiles)
                boxes.add(preview.box);
        } else
            return null;
        
        boxes.flip(axis, box);
        return new LittleActionDestroyBoxes(boxes);
    }
    
    public static class StructurePreview {
        
        public LittleGroupAbsolute previews;
        public boolean requiresItemStack;
        public LittleStructure structure;
        
        public StructurePreview(LittleStructure structure) throws CorruptedConnectionException, NotYetConnectedException {
            structure = structure.findTopStructure();
            structure.checkConnections();
            previews = structure.getAbsolutePreviews(structure.getPos());
            requiresItemStack = previews.getStructureType().canOnlyBePlacedByItemStack();
            this.structure = structure;
        }
        
        public LittleAction getPlaceAction() {
            if (requiresItemStack)
                return new LittleActionPlaceAbsolute.LittleActionPlaceAbsolutePremade(previews, PlacementMode.all, false);
            return new LittleActionPlaceAbsolute(previews, PlacementMode.all, false);
        }
        
        @Override
        public int hashCode() {
            return previews.getStructureTag().hashCode();
        }
        
        @Override
        public boolean equals(Object paramObject) {
            if (paramObject instanceof StructurePreview)
                return structure == ((StructurePreview) paramObject).structure;
            if (paramObject instanceof LittleStructure)
                return structure == paramObject;
            return false;
        }
    }
    
}
