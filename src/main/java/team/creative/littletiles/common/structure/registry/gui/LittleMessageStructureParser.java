package team.creative.littletiles.common.structure.registry.gui;

import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.type.LittleStructureMessage;

public class LittleMessageStructureParser extends LittleStructureGuiControl {
    
    public LittleMessageStructureParser(GuiParent parent, AnimationGuiHandler handler) {
        super(parent, handler);
    }
    
    @Override
    public void createControls(LittleGroup previews, LittleStructure structure) {
        parent.add(new GuiTextfield("text", structure instanceof LittleStructureMessage ? ((LittleStructureMessage) structure).text : "Hello World!"));
        parent.add(new GuiCheckBox("rightclick", structure instanceof LittleStructureMessage ? ((LittleStructureMessage) structure).allowRightClick : true)
                .setTranslate("gui.door.rightclick"));
    }
    
    @Override
    public LittleStructureMessage parseStructure(LittleGroup previews) {
        LittleStructureMessage structure = createStructure(LittleStructureMessage.class, null);
        GuiTextfield text = (GuiTextfield) parent.get("text");
        structure.text = text.getText();
        GuiCheckBox box = (GuiCheckBox) parent.get("rightclick");
        structure.allowRightClick = box.value;
        return structure;
    }
    
    @Override
    protected LittleStructureType getStructureType() {
        return LittleStructureRegistry.getStructureType(LittleStructureMessage.class);
    }
}