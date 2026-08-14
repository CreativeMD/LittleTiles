package team.creative.littletiles.common.mod.jei;

import org.jetbrains.annotations.Nullable;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.common.item.ItemPremadeStructure;
import team.creative.littletiles.common.recipe.BlankOMaticRecipeRegistry;
import team.creative.littletiles.common.recipe.BlankOMaticRecipeRegistry.BleachRecipe;

@JeiPlugin
public class LittleJEIPlugin implements IModPlugin {
    
    public static final RecipeType<BleachRecipe> BLEACHING = RecipeType.create(LittleTiles.MODID, "bleaching", BleachRecipe.class);
    
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(LittleTiles.MODID, "jei_plugin");
    }
    
    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(LittleTilesRegistry.PREMADE.value(), new ISubtypeInterpreter<ItemStack>() {
            
            @Override
            public @Nullable Object getSubtypeData(ItemStack ingredient, UidContext context) {
                return ItemPremadeStructure.getPremadeId(ingredient);
            }
            
            @Override
            public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
                return "";
            }
        });
        registration.registerSubtypeInterpreter(LittleTilesRegistry.ITEM_TILES.value(), new ISubtypeInterpreter<ItemStack>() {
            
            @Override
            public @Nullable Object getSubtypeData(ItemStack ingredient, UidContext context) {
                return ILittleTool.getData(ingredient);
            }
            
            @Override
            public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
                return "";
            }
        });
    }
    
    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new BleachingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }
    
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(BLEACHING, BlankOMaticRecipeRegistry.all());
    }
    
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalysts(BLEACHING, ItemPremadeStructure.of("blankomatic"));
    }
    
}
