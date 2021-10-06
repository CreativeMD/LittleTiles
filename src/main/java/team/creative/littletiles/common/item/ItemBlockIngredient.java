package team.creative.littletiles.common.item;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.core.NonNullList;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.model.ICreativeRendered;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.api.ingredient.ILittleIngredientInventory;
import team.creative.littletiles.common.ingredient.BlockIngredient;
import team.creative.littletiles.common.ingredient.BlockIngredientEntry;
import team.creative.littletiles.common.ingredient.IngredientUtils;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.ingredient.LittleInventory;

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
                    if (inventory != null && entry.value > 1) {
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
        int pixels = (int) (volume * context.maxTilesPerBlock);
        if (pixels < context.size * context.size)
            cubes.add(new RenderBox(0.4F, 0.4F, 0.4F, 0.6F, 0.6F, 0.6F, ingredient.block, ingredient.meta));
        else {
            int remainingPixels = pixels;
            int planes = pixels / context.maxTilesPerPlane;
            remainingPixels -= planes * context.maxTilesPerPlane;
            int rows = remainingPixels / context.size;
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
