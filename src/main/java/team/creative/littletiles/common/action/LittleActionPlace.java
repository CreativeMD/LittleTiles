package team.creative.littletiles.common.action;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.littletiles.api.common.tool.ILittlePlacer;
import team.creative.littletiles.common.action.cancel.ActionCancelContext;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.action.source.LittleActionSource;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.ingredient.LittleIngredient;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.ingredient.LittleInventory;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.placement.Placement;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.PlacementResult;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.structure.registry.premade.LittlePremadePreview;
import team.creative.littletiles.common.structure.registry.premade.LittlePremadeRegistry;

public class LittleActionPlace extends LittleAction<Boolean> {
    
    public PlacementPreview preview;
    public PlaceAction action;
    
    public transient PlacementResult result;
    @OnlyIn(Dist.CLIENT)
    public transient LittleGroupAbsolute destroyed;
    public transient boolean toVanilla = true;
    
    public LittleActionPlace() {}
    
    public LittleActionPlace(PlaceAction action, PlacementPreview preview) {
        this.action = action;
        this.preview = preview;
    }
    
    @Override
    public boolean canBeReverted() {
        return true;
    }
    
    @Override
    public LittleAction revert(LittleActionSource source) throws LittleActionException {
        if (result == null)
            return null;
        result.placedBoxes.convertToSmallest();
        
        if (destroyed != null) {
            destroyed.convertToSmallest();
            return new LittleActions(new LittleActionDestroyBoxes(preview.levelUUID, result.placedBoxes.copy()), new LittleActionPlace(PlaceAction.ABSOLUTE, PlacementPreview.load(
                preview.levelUUID, PlacementMode.FILL, destroyed)));
        }
        return new LittleActionDestroyBoxes(preview.levelUUID, result.placedBoxes.copy());
    }
    
    @Override
    public boolean wasSuccessful(Boolean result) {
        return result;
    }
    
    @Override
    public Boolean failed() {
        return false;
    }
    
    @Override
    public void cancel(ActionCancelContext context) throws LittleActionException {
        if (result != null)
            context.markBE(result.blocks);
    }
    
    @Override
    public Boolean action(LittleActionSource source) throws LittleActionException {
        Level level = source.getActionLevel();
        
        if (!isAllowedToInteract(level, source, preview.position.getPos(), true, preview.position.facing)) {
            sendBlockResetToClient(level, source, preview);
            return false;
        }
        
        preview.validate();
        
        if (action == PlaceAction.PLACER) {
            ItemStack stack = source.getActionItem();
            if (!(stack.getItem() instanceof ILittlePlacer))
                return false;
            PlacementResult tiles = placeTile(source, stack, preview);
            
            if (!level.isClientSide)
                source.broadcastChanges();
            return tiles != null;
        }
        
        LittleInventory inventory = source.createInventory();
        if (canDrainIngredientsBeforePlacing(source, inventory)) {
            Placement placement = new Placement(source, preview);
            result = placement.place();
            
            if (result != null) {
                drainIngredientsAfterPlacing(source, inventory, result, preview.previews);
                
                if (!level.isClientSide) {
                    checkAndGive(source, inventory, getIngredients(source.getActionRegistry(), placement.unplaceableTiles));
                    checkAndGive(source, inventory, placement.overflow());
                }
                
                if (!placement.removedTiles.isEmpty() && level.isClientSide)
                    destroyed = placement.removedTiles.copy();
                
                if (toVanilla)
                    for (BETiles be : result.blocks)
                        be.convertBlockToVanilla();
                    
            }
            
            return result != null;
        }
        return false;
    }
    
    public PlacementResult placeTile(LittleActionSource source, ItemStack stack, PlacementPreview preview) throws LittleActionException {
        ILittlePlacer iTile = (ILittlePlacer) stack.getItem();
        ItemStack toPlace = stack.copy();
        
        LittleInventory inventory = source.createInventory();
        
        if (source.needsIngredients())
            if (!iTile.containsIngredients(stack))
                canTake(source, inventory, preview.getBeforePlaceIngredients(source.getActionRegistry()));
            
        isAllowedToUse(source, preview.previews);
        isAllowedToUse(source, preview.position);
        
        Placement placement = new Placement(source, preview).setStack(toPlace);
        result = placement.place();
        
        if (result != null) {
            if (source.needsIngredients()) {
                checkAndGive(source, inventory, placement.overflow());
                
                if (iTile.containsIngredients(stack)) {
                    stack.shrink(1);
                    checkAndGive(source, inventory, getIngredients(source.getActionRegistry(), placement.unplaceableTiles));
                } else {
                    LittleIngredients ingredients = LittleIngredient.extractStructureOnly(source.getActionRegistry(), preview.previews);
                    ingredients.add(result.ingredients.copy());
                    take(source, inventory, ingredients);
                }
            }
            
            if (!placement.removedTiles.isEmpty() && source.getActionLevel().isClientSide)
                destroyed = placement.removedTiles.copy();
        }
        return result;
    }
    
    protected boolean canDrainIngredientsBeforePlacing(LittleActionSource source, LittleInventory inventory) throws LittleActionException {
        if (action != PlaceAction.PREMADE)
            return canTake(source, inventory, preview.getBeforePlaceIngredients(source.getActionRegistry()));
        
        LittlePremadePreview entry = LittlePremadeRegistry.getPreview(preview.previews.getStructureId());
        
        try {
            inventory.startSimulation();
            return take(source, inventory, entry.stack) && entry.arePreviewsEqual(preview.previews);
        } finally {
            inventory.stopSimulation();
        }
    }
    
    protected void drainIngredientsAfterPlacing(LittleActionSource source, LittleInventory inventory, PlacementResult placedTiles,
            LittleGroup previews) throws LittleActionException {
        if (action == PlaceAction.PREMADE) {
            take(source, inventory, LittlePremadeRegistry.getPreview(previews.getStructureId()).stack);
            return;
        }
        LittleIngredients ingredients = LittleIngredient.extractStructureOnly(source.getActionRegistry(), previews);
        ingredients.add(getIngredients(source.getActionRegistry(), placedTiles.placedPreviews));
        take(source, inventory, ingredients);
    }
    
    @Override
    public void include(LittleBoxes boxes) {
        this.preview.include(boxes);
    }
    
    @Override
    public void exclude(LittleBoxes boxes) {
        this.preview.exclude(boxes);
    }
    
    @Override
    public LittleActionPlace mirror(Axis axis, LittleBoxAbsolute box) {
        PlacementPreview preview = this.preview.copy();
        preview.mirror(axis, box);
        return new LittleActionPlace(action, preview);
    }
    
    public static enum PlaceAction {
        
        PLACER,
        ABSOLUTE,
        PREMADE;
        
    }
    
}
