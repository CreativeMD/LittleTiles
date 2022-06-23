package team.creative.littletiles.common.structure.registry.gui;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.controls.simple.GuiSteppedSlider;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.type.LittleLight;

@OnlyIn(Dist.CLIENT)
public class LittleLightGui extends LittleStructureGuiControl {
    
    public LittleLightGui(LittleStructureType type, AnimationGuiHandler handler) {
        super(type, handler);
    }
    
    @Override
    protected void createExtra(LittleGroup group, LittleStructure structure) {
        add(new GuiSteppedSlider("level", structure instanceof LittleLight ? ((LittleLight) structure).level : 15, 0, 15));
        add(new GuiCheckBox("rightclick", structure instanceof LittleLight ? !((LittleLight) structure).disableRightClick : true).setTranslate("gui.door.rightclick"));
    }
    
    @Override
    protected void saveExtra(LittleStructure structure, LittleGroup previews) {
        LittleLight light = (LittleLight) structure;
        light.level = (int) get("level", GuiSteppedSlider.class).value;
        light.disableRightClick = !get("rightclick", GuiCheckBox.class).value;
    }
    
}