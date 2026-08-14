package team.creative.littletiles.common.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import team.creative.creativecore.common.util.filter.BlockFilter;
import team.creative.creativecore.common.util.ingredient.CreativeIngredient;
import team.creative.creativecore.common.util.ingredient.CreativeIngredientBlock;
import team.creative.creativecore.common.util.ingredient.CreativeIngredientItem;
import team.creative.littletiles.LittleTilesRegistry;

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
    
    public static List<BleachRecipe> all() {
        return recipes;
    }
    
    static {
        registerBleacher(new CreativeIngredientItem(Items.BONE_MEAL), 4);
        registerBleacher(new CreativeIngredientItem(Items.WHITE_DYE), 4);
        registerBleacher(new CreativeIngredientItem(Items.LIGHT_GRAY_DYE), 2);
        registerBleacher(new CreativeIngredientItem(Items.GRAY_DYE), 1);
        registerBleacher(new CreativeIngredientBlock(Blocks.WHITE_WOOL), 8);
        registerBleacher(new CreativeIngredientBlock(Blocks.LIGHT_GRAY_WOOL), 4);
        registerBleacher(new CreativeIngredientBlock(Blocks.GRAY_WOOL), 2);
        registerBleacher(new CreativeIngredientItem(Items.AZURE_BLUET), 4); // Azure Bluet
        registerBleacher(new CreativeIngredientItem(Items.OXEYE_DAISY), 4); // Oxeye Daisy
        registerBleacher(new CreativeIngredientItem(Items.WHITE_TULIP), 4);
        
        registerBleacher(new CreativeIngredientItem(Items.SUGAR), 1);
        
        registerBleachRecipe(new BleachRecipe(BlockFilter.block(Blocks.COBBLESTONE), 1, LittleTilesRegistry.GRAINY.value(), LittleTilesRegistry.GRAINY_BIG.value()));
        registerBleachRecipe(new BleachRecipe(BlockFilter.block(Blocks.COBBLESTONE), 2, LittleTilesRegistry.GRAINY_LOW.value()));
        
        registerBleachRecipe(new BleachRecipe(BlockFilter.block(Blocks.STONE), 1, LittleTilesRegistry.GRAVEL.value(), LittleTilesRegistry.SAND.value(), LittleTilesRegistry.STONE
                .value(), LittleTilesRegistry.CLAY.value()));
        registerBleachRecipe(new BleachRecipe(BlockFilter.block(Blocks.STONE), 2, LittleTilesRegistry.CORK.value()));
        
        BlockFilter filter = BlockFilter.blocks(Blocks.STONE_BRICKS, Blocks.BRICKS);
        registerBleachRecipe(new BleachRecipe(filter, 1, LittleTilesRegistry.BRICK.value(), LittleTilesRegistry.BRICK_BIG.value(), LittleTilesRegistry.BROKEN_BRICK_BIG
                .value(), LittleTilesRegistry.CHISELED.value(), LittleTilesRegistry.STRIPS.value()));
        registerBleachRecipe(new BleachRecipe(filter, 2, LittleTilesRegistry.BORDERED.value(), LittleTilesRegistry.FLOOR.value()));
        
        registerBleachRecipe(new BleachRecipe(BlockFilter.tag(BlockTags.BASE_STONE_OVERWORLD), 4, LittleTilesRegistry.CLEAN.value()));
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
        
        public final BlockFilter filter;
        public final Block[] results;
        public final int needed;
        
        public BleachRecipe(BlockFilter filter, int needed, Block... results) {
            this.filter = filter;
            this.needed = needed;
            this.results = results;
        }
        
        public boolean is(Block block) {
            if (filter.is(block))
                return true;
            
            for (int i = 0; i < results.length; i++)
                if (results[i] == block)
                    return true;
            return false;
        }
        
        public boolean isResult(ItemStack stack) {
            Block other = Block.byItem(stack.getItem());
            for (int i = 0; i < results.length; i++)
                if (results[i] == other)
                    return true;
            return false;
        }
    }
    
}
