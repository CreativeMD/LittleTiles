package team.creative.littletiles.common.structure.registry.gui;

import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.controls.simple.GuiSteppedSlider;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.type.LittleLight;

public class LittleLightStructureParser extends LittleStructureGuiControl {
    
    public LittleLightStructureParser(GuiParent parent, AnimationGuiHandler handler) {
        super(parent, handler);
    }
    
    @Override
    public void createControls(LittleGroup previews, LittleStructure structure) {
        parent.add(new GuiSteppedSlider("level", structure instanceof LittleLight ? ((LittleLight) structure).level : 15, 0, 15));
        parent.add(new GuiCheckBox("rightclick", structure instanceof LittleLight ? !((LittleLight) structure).disableRightClick : true).setTranslate("gui.door.rightclick"));
    }
    
    @Override
    public LittleLight parseStructure(LittleGroup previews) {
        LittleLight structure = createStructure(LittleLight.class, null);
        GuiSteppedSlider slider = (GuiSteppedSlider) parent.get("level");
        GuiCheckBox rightclick = (GuiCheckBox) parent.get("rightclick");
        structure.level = (int) slider.value;
        structure.disableRightClick = !rightclick.value;
        return structure;
    }
    
    @Override
    protected LittleStructureType getStructureType() {
        return LittleStructureRegistry.getStructureType(LittleLight.class);
    }
}