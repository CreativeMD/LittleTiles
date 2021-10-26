package team.creative.littletiles.common.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import team.creative.creativecore.common.util.filter.Filter;
import team.creative.creativecore.common.util.filter.block.BlockFilters;
import team.creative.creativecore.common.util.ingredient.CreativeIngredient;
import team.creative.creativecore.common.util.ingredient.CreativeIngredientBlock;
import team.creative.creativecore.common.util.ingredient.CreativeIngredientItem;
import team.creative.creativecore.common.util.ingredient.CreativeIngredientItemStack;
import team.creative.littletiles.LittleTiles;

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
        Block block = Block.byItem(stack.getItem());
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
        
        registerBleachRecipe(new BleachRecipe(BlockFilters.block(Blocks.COBBLESTONE), 1, LittleTiles.GRAINY.defaultBlockState(), LittleTiles.GRAINY_BIG.defaultBlockState()));
        registerBleachRecipe(new BleachRecipe(BlockFilters.block(Blocks.COBBLESTONE), 2, LittleTiles.GRAINY_LOW.defaultBlockState()));
        
        registerBleachRecipe(new BleachRecipe(BlockFilters.block(Blocks.STONE), 1, LittleTiles.GRAVEL.defaultBlockState(), LittleTiles.SAND.defaultBlockState(), LittleTiles.STONE
                .defaultBlockState(), LittleTiles.CLAY.defaultBlockState()));
        registerBleachRecipe(new BleachRecipe(BlockFilters.block(Blocks.STONE), 2, LittleTiles.CORK.defaultBlockState()));
        
        Filter<Block> filter = BlockFilters.blocks(Blocks.STONE_BRICKS, Blocks.BRICKS);
        registerBleachRecipe(new BleachRecipe(filter, 1, LittleTiles.BRICK.defaultBlockState(), LittleTiles.BRICK_BIG.defaultBlockState(), LittleTiles.BROKEN_BRICK_BIG
                .defaultBlockState(), LittleTiles.CHISELED.defaultBlockState(), LittleTiles.STRIPS.defaultBlockState()));
        registerBleachRecipe(new BleachRecipe(filter, 2, LittleTiles.BORDERED.defaultBlockState(), LittleTiles.FLOOR.defaultBlockState()));
        
        registerBleachRecipe(new BleachRecipe(BlockFilters.material(Material.STONE), 4, LittleTiles.CLEAN.defaultBlockState()));
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
                if (state.getBlock() == block)
                    return true;
            }
            return false;
        }
        
        public boolean isResult(ItemStack stack) {
            Block other = Block.byItem(stack.getItem());
            for (int i = 0; i < results.length; i++) {
                BlockState state = results[i];
                if (state.getBlock() == other)
                    return true;
            }
            return false;
        }
    }
    
}
