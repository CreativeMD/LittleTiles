package team.creative.littletiles.common.gui.tool.blueprint.test;

import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.littletiles.common.gui.tool.blueprint.GuiBlueprint;
import team.creative.littletiles.common.gui.tool.blueprint.GuiTreeItemStructure;

public abstract class BlueprintTestError implements Iterable<GuiTreeItemStructure> {
    
    public abstract Component header();
    
    public abstract Component description();
    
    public abstract Component tooltip(GuiTreeItemStructure structure);
    
    public abstract void create(GuiBlueprint blueprint, GuiParent parent, Runnable refresh);
    
}
