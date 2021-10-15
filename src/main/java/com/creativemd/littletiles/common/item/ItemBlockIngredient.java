package com.creativemd.littletiles.common.item;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.client.rendering.RenderBox;
import com.creativemd.creativecore.client.rendering.model.ICreativeRendered;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.api.ILittleIngredientInventory;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.ingredient.BlockIngredient;
import com.creativemd.littletiles.common.util.ingredient.BlockIngredientEntry;
import com.creativemd.littletiles.common.util.ingredient.IngredientUtils;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredients;
import com.creativemd.littletiles.common.util.ingredient.LittleInventory;

import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockIngredient extends Item implements ICreativeRendered, ILittleIngredientInventory {
    
    public ItemBlockIngredient() {
        hasSubtypes = true;
        setCreativeTab(LittleTiles.littleTab);
        setMaxStackSize(1);
    }
    
    public static BlockIngredientEntry loadIngredient(ItemStack stack) {
        if (stack.hasTagCompound())
            return IngredientUtils.loadBlockIngredient(stack.getTagCompound());
        return null;
    }
    
    public static void saveIngredient(ItemStack stack, BlockIngredientEntry entry) {
        entry.writeToNBT(stack.getTagCompound());
    }
    
    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        BlockIngredientEntry entry = loadIngredient(stack);
        if (entry != null) {
            return entry.getItemStack().getDisplayName();
        } else
            return super.getItemStackDisplayName(stack);
        
    }
    
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
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
                if (!entry.isEmpty() || entry.block instanceof BlockAir) {
                    if (inventory != null && entry.value >= 1) {
                        ItemStack overflow = entry.getItemStack();
                        overflow.setCount((int) entry.value);
                        entry.value -= overflow.getCount();
                        inventory.addStack(overflow);
                    }
                    
                    if (entry.value > 0) {
                        saveIngredient(stack, entry);
                        return;
                    }
                }
            
        stack.setTagCompound(null);
        stack.setCount(0);
    }
    
    @Override
    public List<? extends RenderBox> getRenderingCubes(IBlockState state, TileEntity te, ItemStack stack) {
        List<RenderBox> cubes = new ArrayList<>();
        BlockIngredientEntry ingredient = loadIngredient(stack);
        if (ingredient == null)
            return null;
        
        double volume = Math.min(1, ingredient.value);
        LittleGridContext context = LittleGridContext.get();
        long pixels = (long) (volume * context.maxTilesPerBlock);
        if (pixels < context.size * context.size)
            cubes.add(new RenderBox(0.4F, 0.4F, 0.4F, 0.6F, 0.6F, 0.6F, ingredient.block, ingredient.meta));
        else {
            long remainingPixels = pixels;
            long planes = pixels / context.maxTilesPerPlane;
            remainingPixels -= planes * context.maxTilesPerPlane;
            long rows = remainingPixels / context.size;
            remainingPixels -= rows * context.size;
            
            float height = (float) (planes * context.pixelSize);
            
            if (planes > 0)
                cubes.add(new RenderBox(0.0F, 0.0F, 0.0F, 1.0F, height, 1.0F, ingredient.block, ingredient.meta));
            
            float width = (float) (rows * context.pixelSize);
            
            if (rows > 0)
                cubes.add(new RenderBox(0.0F, height, 0.0F, 1.0F, height + (float) context.pixelSize, width, ingredient.block, ingredient.meta));
            
            if (remainingPixels > 0)
                cubes.add(new RenderBox(0.0F, height, width, 1.0F, height + (float) context.pixelSize, width + (float) context.pixelSize, ingredient.block, ingredient.meta));
        }
        return cubes;
    }
    
    @Override
    public boolean shouldBeMerged() {
        return true;
    }
    
}
