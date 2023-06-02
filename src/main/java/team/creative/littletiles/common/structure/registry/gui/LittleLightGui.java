package team.creative.littletiles.common.structure.registry.gui;

import javax.annotation.Nullable;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.controls.simple.GuiSteppedSlider;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.type.LittleLight;

@OnlyIn(Dist.CLIENT)
public class LittleLightGui extends LittleStructureGuiControl {
    
    public LittleLightGui(LittleStructureGui gui, GuiTreeItemStructure item) {
        super(gui, item);
    }
    
    @Override
    public void create(@Nullable LittleStructure structure) {
        add(new GuiSteppedSlider("level", structure instanceof LittleLight ? ((LittleLight) structure).level : 15, 0, 15));
        add(new GuiCheckBox("rightclick", structure instanceof LittleLight ? ((LittleLight) structure).allowRightClick : true).setTranslate("gui.door.rightclick"));
    }
    
    @Override
    public LittleStructure save(LittleStructure structure) {
        LittleLight light = (LittleLight) structure;
        light.level = (int) get("level", GuiSteppedSlider.class).value;
        light.allowRightClick = get("rightclick", GuiCheckBox.class).value;
        return structure;
    }
    
}