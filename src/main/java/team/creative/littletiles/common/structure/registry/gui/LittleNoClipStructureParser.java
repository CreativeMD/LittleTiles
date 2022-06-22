package team.creative.littletiles.common.structure.registry.gui;

import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.type.LittleNoClipStructure;

public class LittleNoClipStructureParser extends LittleStructureGuiControl {
    
    public LittleNoClipStructureParser(GuiParent parent, AnimationGuiHandler handler) {
        super(parent, handler);
    }
    
    @Override
    public void createControls(LittleGroup previews, LittleStructure structure) {
        boolean slowness = true;
        if (structure instanceof LittleNoClipStructure)
            slowness = ((LittleNoClipStructure) structure).web;
        parent.add(new GuiCheckBox("web", slowness).setTranslate("gui.noclip.slowness"));
    }
    
    @Override
    public LittleNoClipStructure parseStructure(LittleGroup previews) {
        LittleNoClipStructure structure = createStructure(LittleNoClipStructure.class, null);
        structure.web = ((GuiCheckBox) parent.get("web")).value;
        return structure;
    }
    
    @Override
    protected LittleStructureType getStructureType() {
        return LittleStructureRegistry.getStructureType(LittleNoClipStructure.class);
    }
}