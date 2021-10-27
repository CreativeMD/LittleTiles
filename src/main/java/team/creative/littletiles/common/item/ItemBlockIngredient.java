package team.creative.littletiles.common.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.model.ICreativeRendered;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.api.ingredient.ILittleIngredientInventory;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.ingredient.BlockIngredient;
import team.creative.littletiles.common.ingredient.BlockIngredientEntry;
import team.creative.littletiles.common.ingredient.IngredientUtils;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.ingredient.LittleInventory;

public class ItemBlockIngredient extends Item implements ICreativeRendered, ILittleIngredientInventory {
    
    public ItemBlockIngredient() {
        super(new Item.Properties().tab(LittleTiles.LITTLE_TAB).stacksTo(1));
    }
    
    public static BlockIngredientEntry loadIngredient(ItemStack stack) {
        if (stack.hasTag())
            return IngredientUtils.loadBlockIngredient(stack.getTag());
        return null;
    }
    
    public static void saveIngredient(ItemStack stack, BlockIngredientEntry entry) {
        entry.save(stack.getTag());
    }
    
    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        return false;
    }
    
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack me, ItemStack other, Slot p_150894_, ClickAction action, Player player, SlotAccess slot) {
        return false;
    }
    
    @Override
    public Component getName(ItemStack stack) {
        BlockIngredientEntry entry = loadIngredient(stack);
        if (entry != null) {
            return entry.getBlockStack().getDisplayName();
        } else
            return super.getName(stack);
    }
    
    @Override
    public boolean shouldOverrideMultiplayerNbt() {
        return true;
    }
    
    @Override
    public boolean isComplex() {
        return true;
    }
    
    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list) {}
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flagIn) {
        BlockIngredientEntry entry = loadIngredient(stack);
        if (entry != null)
            tooltip.add(BlockIngredient.printVolume(entry.value, false));
    }
    
    @Override
    public LittleIngredients getInventory(ItemStack stack) {
        BlockIngredientEntry entry = loadIngredient(stack);
        if (entry != null) {
            BlockIngredient ingredient = new BlockIngredient();
            ingredient.add(entry);
            return new LittleIngredients(ingredient.setLimits(1, 64)) {
                @Override
                protected boolean canAddNewIngredients() {
                    return false;
                }
                
                @Override
                protected boolean removeEmptyIngredients() {
                    return false;
                }
            };
        }
        return null;
    }
    
    @Override
    public void setInventory(ItemStack stack, LittleIngredients ingredients, LittleInventory inventory) {
        BlockIngredient blocks = ingredients.get(BlockIngredient.class);
        if (blocks != null && !blocks.isEmpty())
            for (BlockIngredientEntry entry : blocks)
                if (!entry.isEmpty() || entry.block instanceof AirBlock) {
                    if (inventory != null && entry.value > 1) {
                        ItemStack overflow = entry.getBlockStack();
                        overflow.setCount((int) entry.value);
                        entry.value -= overflow.getCount();
                        inventory.addStack(overflow);
                    }
                    
                    if (entry.value > 0) {
                        saveIngredient(stack, entry);
                        return;
                    }
                }
            
        stack.setTag(null);
        stack.setCount(0);
    }
    
    @Override
    public List<? extends RenderBox> getRenderingCubes(BlockState state, BlockEntity te, ItemStack stack) {
        List<RenderBox> cubes = new ArrayList<>();
        BlockIngredientEntry ingredient = loadIngredient(stack);
        if (ingredient == null)
            return null;
        
        double volume = Math.min(1, ingredient.value);
        LittleGrid context = LittleGrid.defaultGrid();
        int pixels = (int) (volume * context.count3d);
        if (pixels < context.count * context.count)
            cubes.add(new RenderBox(0.4F, 0.4F, 0.4F, 0.6F, 0.6F, 0.6F, ingredient.block.getState()));
        else {
            int remainingPixels = pixels;
            int planes = pixels / context.count2d;
            remainingPixels -= planes * context.count2d;
            int rows = remainingPixels / context.count;
            remainingPixels -= rows * context.count;
            
            float height = (float) (planes * context.pixelLength);
            
            if (planes > 0)
                cubes.add(new RenderBox(0.0F, 0.0F, 0.0F, 1.0F, height, 1.0F, ingredient.block.getState()));
            
            float width = (float) (rows * context.pixelLength);
            
            if (rows > 0)
                cubes.add(new RenderBox(0.0F, height, 0.0F, 1.0F, height + (float) context.pixelLength, width, ingredient.block.getState()));
            
            if (remainingPixels > 0)
                cubes.add(new RenderBox(0.0F, height, width, 1.0F, height + (float) context.pixelLength, width + (float) context.pixelLength, ingredient.block.getState()));
        }
        return cubes;
    }
    
    @Override
    public boolean shouldBeMerged() {
        return true;
    }
    
}
