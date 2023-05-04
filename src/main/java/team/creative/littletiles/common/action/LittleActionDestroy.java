package team.creative.littletiles.common.action;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.mc.LevelUtils;
import team.creative.littletiles.common.action.LittleActionPlace.PlaceAction;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.block.little.tile.parent.StructureParentCollection;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.ingredient.LittleInventory;
import team.creative.littletiles.common.item.ItemLittleWrench;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.box.collection.LittleBoxesSimple;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

public class LittleActionDestroy extends LittleActionInteract<Boolean> {
    
    public transient LittleGroupAbsolute destroyedTiles;
    public transient StructurePreview structurePreview;
    
    public LittleActionDestroy(Level level, BlockPos blockPos, Player player) {
        super(level, blockPos, player);
    }
    
    public LittleActionDestroy() {}
    
    @Override
    public boolean canBeReverted() {
        return destroyedTiles != null || structurePreview != null;
    }
    
    @Override
    public LittleAction revert(Player player) {
        if (structurePreview != null)
            return structurePreview.getPlaceAction();
        destroyedTiles.convertToSmallest();
        return new LittleActionPlace(PlaceAction.ABSOLUTE, PlacementPreview.load(uuid, PlacementMode.normal, destroyedTiles, Facing.EAST));
    }
    
    @Override
    protected boolean requiresBreakEvent() {
        return true;
    }
    
    @Override
    protected Boolean action(Level level, BETiles be, LittleTileContext context, ItemStack stack, Player player, BlockHitResult hit, BlockPos pos, boolean secondMode) throws LittleActionException {
        if (context.parent.isStructure()) {
            try {
                LittleStructure structure = context.parent.getStructure();
                structure.checkConnections();
                structurePreview = new StructurePreview(structure);
                if (needIngredients(player) && !player.level.isClientSide)
                    LevelUtils.dropItem(level, structure.getStructureDrop(), pos);
                structure.tileDestroyed();
            } catch (CorruptedConnectionException | NotYetConnectedException e) {
                if (player.getMainHandItem().getItem() instanceof ItemLittleWrench) {
                    ((StructureParentCollection) context.parent).remove();
                    be.updateTiles();
                } else
                    throw new LittleActionException.StructureNotLoadedException();
            }
        } else {
            LittleInventory inventory = new LittleInventory(player);
            destroyedTiles = new LittleGroupAbsolute(pos);
            if (BlockTile.selectEntireBlock(player, secondMode)) {
                for (LittleTile toDestroy : context.parent)
                    destroyedTiles.add(context.parent, toDestroy);
                
                checkAndGive(player, inventory, getIngredients(destroyedTiles));
                be.updateTiles(x -> x.noneStructureTiles().clear());
            } else {
                destroyedTiles.add(context.parent, context.tile, context.box);
                
                checkAndGive(player, inventory, getIngredients(destroyedTiles));
                
                be.updateTiles((x) -> x.get(context.parent).remove(context.tile, context.box));
            }
        }
        
        level.playSound((Player) null, pos, context.tile.getSound()
                .getBreakSound(), SoundSource.BLOCKS, (context.tile.getSound().getVolume() + 1.0F) / 2.0F, context.tile.getSound().getPitch() * 0.8F);
        
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
            for (LittleBox destroyedBox : structurePreview.previews.group.allBoxes())
                boxes.add(destroyedBox);
        } else if (destroyedTiles != null) {
            destroyedTiles.convertToSmallest();
            boxes = new LittleBoxesSimple(destroyedTiles.pos, destroyedTiles.getGrid());
            for (LittleBox destroyedBox : destroyedTiles.group.allBoxes())
                boxes.add(destroyedBox);
        } else
            return null;
        
        boxes.mirror(axis, box);
        return new LittleActionDestroyBoxes(uuid, boxes);
    }
    
    @Override
    public Boolean failed() {
        return false;
    }
    
    @Override
    protected Boolean ignored() {
        return false;
    }
    
    @Override
    public boolean wasSuccessful(Boolean result) {
        return result;
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
            return new LittleActionPlace(requiresItemStack ? PlaceAction.PREMADE : PlaceAction.ABSOLUTE, PlacementPreview
                    .absolute(structure.getLevel(), PlacementMode.all, previews, Facing.EAST));
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
