package team.creative.littletiles.common.structure.registry.gui;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.type.LittleNoClipStructure;

@OnlyIn(Dist.CLIENT)
public class LittleNoClipGui extends LittleStructureGuiControl {
    
    public LittleNoClipGui(LittleStructureType type, AnimationGuiHandler handler) {
        super(type, handler);
    }
    
    @Override
    protected void createExtra(LittleGroup group, LittleStructure structure) {
        boolean slowness = true;
        if (structure instanceof LittleNoClipStructure)
            slowness = ((LittleNoClipStructure) structure).web;
        add(new GuiCheckBox("web", slowness).setTranslate("gui.noclip.slowness"));
    }
    
    @Override
    protected void saveExtra(LittleStructure structure, LittleGroup previews) {
        ((LittleNoClipStructure) structure).web = get("web", GuiCheckBox.class).value;
    }
    
}