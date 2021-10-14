package team.creative.littletiles.common.action;

import com.creativemd.littletiles.common.action.block.LittleActionDestroyBoxes;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceAbsolute;
import com.creativemd.littletiles.common.action.block.LittlePreviews;
import com.creativemd.littletiles.common.action.block.SPacketSetSlot;
import com.creativemd.littletiles.common.action.block.TileEntityLittleTiles;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.littletiles.common.api.tool.ILittlePlacer;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.ingredient.LittleIngredient;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.ingredient.LittleInventory;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.box.collection.LittleBoxesSimple;
import team.creative.littletiles.common.placement.Placement;
import team.creative.littletiles.common.placement.PlacementHelper;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.PlacementResult;
import team.creative.littletiles.common.placement.mode.PlacementMode;

public class LittleActionPlace extends LittleAction {
    
    public PlacementPreview preview;
    public PlaceAction action;
    
    @OnlyIn(Dist.CLIENT)
    public transient PlacementResult result;
    @OnlyIn(Dist.CLIENT)
    public transient LittleGroupAbsolute destroyed;
    
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
            return new LittleActions(new LittleActionDestroyBoxes(boxes), new LittleActionPlaceAbsolute(destroyed, PlacementMode.normal, true));
        }
        return new LittleActionDestroyBoxes(boxes);
    }
    
    @Override
    public boolean action(Player player) throws LittleActionException {
        Level level = player.level;
        
        if (!isAllowedToInteract(level, player, preview.position.getPos(), true, preview.position.facing)) {
            sendBlockResetToClient(level, player, preview);
            return false;
        }
        
        if (action == PlaceAction.CURRENT_ITEM) {
            ItemStack stack = player.getMainHandItem();
            if (PlacementHelper.getLittleInterface(stack) != null) {
                PlacementResult tiles = placeTile(player, stack, preview);
                
                if (!player.level.isClientSide) {
                    ServerPlayer playerMP = (ServerPlayer) player;
                    Slot slot = playerMP.openContainer.getSlotFromInventory(playerMP.inventory, playerMP.inventory.currentItem);
                    playerMP.connection.sendPacket(new SPacketSetSlot(playerMP.openContainer.windowId, slot.slotNumber, playerMP.inventory.getCurrentItem()));
                }
                return tiles != null;
            }
            return false;
        }
        
        LittleInventory inventory = new LittleInventory(player);
        if (canDrainIngredientsBeforePlacing(player, inventory)) {
            Placement placement = new Placement(player, preview);
            PlacementResult placedTiles = placement.place();
            
            if (placedTiles != null) {
                drainIngredientsAfterPlacing(player, inventory, placedTiles, previews);
                
                if (!player.level.isClientSide) {
                    checkAndGive(player, inventory, getIngredients(placement.unplaceableTiles));
                    checkAndGive(player, inventory, getIngredients(placement.removedTiles));
                }
                
                if (!placement.removedTiles.isEmpty())
                    destroyed = placement.removedTiles.copy();
                
                if (toVanilla) {
                    for (TileEntityLittleTiles te : placedTiles.tileEntities) {
                        te.convertBlockToVanilla();
                    }
                }
            } else
                boxes = new LittleBoxesSimple(previews.pos, LittleGridContext.get());
            
            return placedTiles != null;
        }
        return false;
    }
    
    public PlacementResult placeTile(Player player, ItemStack stack, PlacementPreview preview) throws LittleActionException {
        ILittlePlacer iTile = PlacementHelper.getLittleInterface(stack);
        if (result == null)
            return null;
        
        ItemStack toPlace = stack.copy();
        
        LittleInventory inventory = new LittleInventory(player);
        
        if (needIngredients(player))
            if (!iTile.containsIngredients(stack))
                canTake(player, inventory, getIngredients(preview.previews));
            
        Placement placement = new Placement(player, preview).setStack(toPlace);
        result = placement.place();
        
        if (result != null) {
            if (needIngredients(player)) {
                checkAndGive(player, inventory, getIngredients(placement.removedTiles));
                
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
        return canTake(player, inventory, getIngredients(previews));
    }
    
    protected void drainIngredientsAfterPlacing(Player player, LittleInventory inventory, PlacementResult placedTiles, LittlePreviews previews) throws LittleActionException {
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
        ABSOLUTE;
        
    }
    
}
