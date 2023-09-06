package team.creative.littletiles.common.action;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.littletiles.api.common.tool.ILittlePlacer;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.ingredient.LittleIngredient;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.ingredient.LittleInventory;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.placement.Placement;
import team.creative.littletiles.common.placement.PlacementHelper;
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
    public LittleAction revert(Player player) throws LittleActionException {
        if (result == null)
            return null;
        result.placedBoxes.convertToSmallest();
        
        if (destroyed != null) {
            destroyed.convertToSmallest();
            return new LittleActions(new LittleActionDestroyBoxes(preview.levelUUID, result.placedBoxes.copy()), new LittleActionPlace(PlaceAction.ABSOLUTE, PlacementPreview.load(
                preview.levelUUID, PlacementMode.NORMAL, destroyed, preview.position.facing)));
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
    public Boolean action(Player player) throws LittleActionException {
        Level level = player.level();
        
        if (!isAllowedToInteract(level, player, preview.position.getPos(), true, preview.position.facing)) {
            sendBlockResetToClient(level, player, preview);
            return false;
        }
        
        if (action == PlaceAction.CURRENT_ITEM) {
            ItemStack stack = player.getMainHandItem();
            if (PlacementHelper.getLittleInterface(stack) != null) {
                PlacementResult tiles = placeTile(player, stack, preview);
                
                if (!level.isClientSide)
                    player.inventoryMenu.broadcastChanges();
                return tiles != null;
            }
            return false;
        }
        
        LittleInventory inventory = new LittleInventory(player);
        if (canDrainIngredientsBeforePlacing(player, inventory)) {
            Placement placement = new Placement(player, preview);
            result = placement.place();
            
            if (result != null) {
                drainIngredientsAfterPlacing(player, inventory, result, preview.previews);
                
                if (!level.isClientSide) {
                    checkAndGive(player, inventory, getIngredients(placement.unplaceableTiles));
                    checkAndGive(player, inventory, placement.overflow());
                }
                
                if (!placement.removedTiles.isEmpty())
                    destroyed = placement.removedTiles.copy();
                
                if (toVanilla)
                    for (BETiles be : result.blocks)
                        be.convertBlockToVanilla();
                    
            }
            
            return result != null;
        }
        return false;
    }
    
    public PlacementResult placeTile(Player player, ItemStack stack, PlacementPreview preview) throws LittleActionException {
        ILittlePlacer iTile = PlacementHelper.getLittleInterface(stack);
        ItemStack toPlace = stack.copy();
        
        LittleInventory inventory = new LittleInventory(player);
        
        if (needIngredients(player))
            if (!iTile.containsIngredients(stack))
                canTake(player, inventory, preview.getBeforePlaceIngredients());
            
        Placement placement = new Placement(player, preview).setStack(toPlace);
        result = placement.place();
        
        if (result != null) {
            if (needIngredients(player)) {
                checkAndGive(player, inventory, placement.overflow());
                
                if (iTile.containsIngredients(stack)) {
                    stack.shrink(1);
                    checkAndGive(player, inventory, getIngredients(placement.unplaceableTiles));
                } else {
                    LittleIngredients ingredients = LittleIngredient.extractStructureOnly(preview.previews);
                    ingredients.add(getIngredients(result.placedPreviews));
                    take(player, inventory, ingredients);
                }
            }
            
            if (!placement.removedTiles.isEmpty())
                destroyed = placement.removedTiles.copy();
        }
        return result;
    }
    
    protected boolean canDrainIngredientsBeforePlacing(Player player, LittleInventory inventory) throws LittleActionException {
        if (action != PlaceAction.PREMADE)
            return canTake(player, inventory, preview.getBeforePlaceIngredients());
        
        LittlePremadePreview entry = LittlePremadeRegistry.getPreview(preview.previews.getStructureId());
        
        try {
            inventory.startSimulation();
            return take(player, inventory, entry.stack) && entry.arePreviewsEqual(preview.previews);
        } finally {
            inventory.stopSimulation();
        }
    }
    
    protected void drainIngredientsAfterPlacing(Player player, LittleInventory inventory, PlacementResult placedTiles, LittleGroup previews) throws LittleActionException {
        if (action == PlaceAction.PREMADE) {
            take(player, inventory, LittlePremadeRegistry.getPreview(previews.getStructureId()).stack);
            return;
        }
        LittleIngredients ingredients = LittleIngredient.extractStructureOnly(previews);
        ingredients.add(getIngredients(placedTiles.placedPreviews));
        take(player, inventory, ingredients);
    }
    
    @Override
    public LittleActionPlace mirror(Axis axis, LittleBoxAbsolute box) {
        PlacementPreview preview = this.preview.copy();
        preview.mirror(axis, box);
        return new LittleActionPlace(action, preview);
    }
    
    public static enum PlaceAction {
        
        CURRENT_ITEM,
        ABSOLUTE,
        PREMADE;
        
    }
    
}
