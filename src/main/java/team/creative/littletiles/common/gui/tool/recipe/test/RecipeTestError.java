package team.creative.littletiles.common.gui.tool.recipe.test;

import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.littletiles.common.gui.tool.recipe.GuiRecipe;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;

public abstract class RecipeTestError implements Iterable<GuiTreeItemStructure> {
    
    public abstract Component header();
    
    public abstract Component description();
    
    public abstract Component tooltip(GuiTreeItemStructure structure);
    
    public abstract void create(GuiRecipe recipe, GuiParent parent);
    
}
