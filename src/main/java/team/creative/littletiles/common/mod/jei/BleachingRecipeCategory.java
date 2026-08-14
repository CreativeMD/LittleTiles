package team.creative.littletiles.common.mod.jei;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import team.creative.littletiles.common.item.ItemPremadeStructure;
import team.creative.littletiles.common.recipe.BlankOMaticRecipeRegistry.BleachRecipe;

public class BleachingRecipeCategory extends AbstractRecipeCategory<BleachRecipe> {
    
    public BleachingRecipeCategory(IGuiHelper helper) {
        super(LittleJEIPlugin.BLEACHING, Component.translatable("structure.blankomatic.jei"), helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, ItemPremadeStructure.of(
            "blankomatic")), 120, 60);
    }
    
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BleachRecipe recipe, IFocusGroup focuses) {
        List<ItemStack> inputs = new ArrayList<>();
        for (Block block : recipe.filter.getPossibleBlocks(Minecraft.getInstance().level.registryAccess()))
            inputs.add(new ItemStack(block, recipe.needed));
        
        builder.addInputSlot(0, 0).setStandardSlotBackground().addIngredients(VanillaTypes.ITEM_STACK, inputs);
        
        int cols = 5;
        for (int i = 0; i < recipe.results.length; i++)
            builder.addOutputSlot(30 + (i % cols) * 18, i / cols * 18).setStandardSlotBackground().addIngredient(VanillaTypes.ITEM_STACK, new ItemStack(recipe.results[i]));
    }
    
}
