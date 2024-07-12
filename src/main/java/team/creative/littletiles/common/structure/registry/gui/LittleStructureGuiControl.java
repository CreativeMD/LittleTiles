package team.creative.littletiles.common.structure.registry.gui;

import javax.annotation.Nullable;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructure;

@OnlyIn(Dist.CLIENT)
public abstract class LittleStructureGuiControl extends GuiParent {
    
    public final LittleStructureGui gui;
    public final GuiTreeItemStructure item;
    
    public LittleStructureGuiControl(LittleStructureGui gui, GuiTreeItemStructure item) {
        this.gui = gui;
        this.item = item;
    }
    
    @Override
    public GuiParent getParent() {
        return (GuiParent) super.getParent();
    }
    
    public abstract void create(@Nullable LittleStructure structure);
    
    public abstract LittleStructure save(LittleStructure structure);
    
    public boolean canChangeType() {
        return true;
    }
    
}
