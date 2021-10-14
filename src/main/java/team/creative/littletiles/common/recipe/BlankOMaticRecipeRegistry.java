package team.creative.littletiles.common.recipe;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.mc.BlockUtils;
import com.creativemd.creativecore.common.utils.sorting.BlockSelector.BlockSelectorMaterial;
import com.creativemd.littletiles.common.block.BlockLittleDyeable.LittleDyeableType;
import com.creativemd.littletiles.common.block.BlockLittleDyeable2.LittleDyeableType2;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.util.filter.Filter;
import team.creative.creativecore.common.util.ingredient.CreativeIngredient;
import team.creative.creativecore.common.util.ingredient.CreativeIngredientBlock;
import team.creative.creativecore.common.util.ingredient.CreativeIngredientItem;
import team.creative.creativecore.common.util.ingredient.CreativeIngredientItemStack;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.filter.BlockFilter;

public class BlankOMaticRecipeRegistry {
    
    public static int bleachTotalVolume = 1000;
    private static List<BleachRecipe> recipes = new ArrayList<>();
    private static List<BleachVolume> bleacher = new ArrayList<>();
    
    public static void registerBleachRecipe(BleachRecipe recipe) {
        recipes.add(recipe);
    }
    
    public static void registerBleacher(CreativeIngredient stack, int volume) {
        bleacher.add(new BleachVolume(stack, volume));
    }
    
    public static List<BleachRecipe> getRecipe(ItemStack stack) {
        BlockState state = BlockUtils.getState(stack);
        Block block = state.getBlock();
        List<BleachRecipe> results = new ArrayList<>();
        for (int i = 0; i < recipes.size(); i++)
            if (recipes.get(i).is(block))
                results.add(recipes.get(i));
        return results;
    }
    
    public static int getVolume(ItemStack stack) {
        for (int i = 0; i < bleacher.size(); i++)
            if (bleacher.get(i).stack.is(stack))
                return bleacher.get(i).volume;
        return 0;
    }
    
    static {
        registerBleacher(new CreativeIngredientItem(Items.WHITE_DYE), 4);
        registerBleacher(new CreativeIngredientItem(Items.LIGHT_GRAY_DYE), 2);
        registerBleacher(new CreativeIngredientItem(Items.GRAY_DYE), 1);
        registerBleacher(new CreativeIngredientBlock(Blocks.WHITE_WOOL), 8);
        registerBleacher(new CreativeIngredientBlock(Blocks.LIGHT_GRAY_WOOL), 4);
        registerBleacher(new CreativeIngredientBlock(Blocks.GRAY_WOOL), 2);
        registerBleacher(new CreativeIngredientItemStack(new ItemStack(Blocks.AZURE_BLUET), false), 4); // Azure Bluet
        registerBleacher(new CreativeIngredientItemStack(new ItemStack(Blocks.OXEYE_DAISY), false), 4); // Oxeye Daisy
        registerBleacher(new CreativeIngredientItemStack(new ItemStack(Blocks.WHITE_TULIP), false), 4);
        
        registerBleacher(new CreativeIngredientItem(Items.SUGAR), 1);
        
        registerBleachRecipe(new BleachRecipe(new BlockFilter(Blocks.COBBLESTONE), 1, LittleTiles.dyeableBlock.get(LittleDyeableType.GRAINY), LittleTiles.dyeableBlock
                .get(LittleDyeableType.GRAINY_BIG)));
        registerBleachRecipe(new BleachRecipe(new BlockFilter(Blocks.COBBLESTONE), 2, LittleTiles.dyeableBlock.get(LittleDyeableType.GRAINY_LOW)));
        
        registerBleachRecipe(new BleachRecipe(new BlockFilter(Blocks.STONE), 1, LittleTiles.dyeableBlock2.get(LittleDyeableType2.GRAVEL), LittleTiles.dyeableBlock2
                .get(LittleDyeableType2.SAND), LittleTiles.dyeableBlock2.get(LittleDyeableType2.STONE), LittleTiles.dyeableBlock.get(LittleDyeableType.CLAY)));
        registerBleachRecipe(new BleachRecipe(new BlockFilter(Blocks.STONE), 2, LittleTiles.dyeableBlock2.get(LittleDyeableType2.CORK)));
        
        BlocksFilter selector = new BlocksFilter(Blocks.STONE_BRICKS, Blocks.BRICKS);
        registerBleachRecipe(new BleachRecipe(selector, 1, LittleTiles.dyeableBlock.get(LittleDyeableType.BRICK), LittleTiles.dyeableBlock
                .get(LittleDyeableType.BRICK_BIG), LittleTiles.dyeableBlock.get(LittleDyeableType.BROKEN_BRICK_BIG), LittleTiles.dyeableBlock
                        .get(LittleDyeableType.CHISELED), LittleTiles.dyeableBlock.get(LittleDyeableType.STRIPS)));
        registerBleachRecipe(new BleachRecipe(selector, 2, LittleTiles.dyeableBlock.get(LittleDyeableType.BORDERED), LittleTiles.dyeableBlock.get(LittleDyeableType.FLOOR)));
        
        registerBleachRecipe(new BleachRecipe(new BlockSelectorMaterial(Material.ROCK), 4, LittleTiles.dyeableBlock.get(LittleDyeableType.CLEAN)));
    }
    
    public static class BleachVolume {
        
        public CreativeIngredient stack;
        public int volume;
        
        public BleachVolume(CreativeIngredient stack, int volume) {
            this.stack = stack;
            this.volume = volume;
        }
    }
    
    public static class BleachRecipe {
        
        public final Filter<Block> filter;
        public final BlockState[] results;
        public final int needed;
        
        public BleachRecipe(Filter<Block> filter, int needed, BlockState... results) {
            this.filter = filter;
            this.needed = needed;
            this.results = results;
        }
        
        public boolean is(Block block) {
            if (filter.is(block))
                return true;
            
            for (int i = 0; i < results.length; i++) {
                BlockState state = results[i];
                if (state.getBlock() == block && state.getBlock().getMetaFromState(state) == meta)
                    return true;
            }
            return false;
        }
        
        public boolean isResult(ItemStack stack) {
            BlockState otherState = BlockUtils.getState(stack);
            for (int i = 0; i < results.length; i++) {
                BlockState state = results[i];
                if (state.getBlock() == otherState.getBlock() && state.getBlock().getMetaFromState(state) == otherState.getBlock().getMetaFromState(otherState))
                    return true;
            }
            return false;
        }
    }
    
}
