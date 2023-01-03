package team.creative.littletiles.common.structure.registry.gui;

import javax.annotation.Nullable;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.type.LittleNoClipStructure;

@OnlyIn(Dist.CLIENT)
public class LittleNoClipGui extends LittleStructureGuiControl {
    
    public LittleNoClipGui(LittleStructureGui gui, GuiTreeItemStructure item) {
        super(gui, item);
    }
    
    @Override
    public void create(@Nullable LittleStructure structure) {
        boolean slowness = true;
        if (structure instanceof LittleNoClipStructure)
            slowness = ((LittleNoClipStructure) structure).web;
        add(new GuiCheckBox("web", slowness).setTranslate("gui.noclip.slowness"));
    }
    
    @Override
    public LittleStructure save(LittleStructure structure) {
        ((LittleNoClipStructure) structure).web = get("web", GuiCheckBox.class).value;
        return structure;
    }
    
}