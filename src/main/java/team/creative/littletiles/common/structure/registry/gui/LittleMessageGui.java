package team.creative.littletiles.common.structure.registry.gui;

import javax.annotation.Nullable;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.type.LittleStructureMessage;

@OnlyIn(Dist.CLIENT)
public class LittleMessageGui extends LittleStructureGuiControl {
    
    public LittleMessageGui(LittleStructureGui gui, GuiTreeItemStructure item) {
        super(gui, item);
    }
    
    @Override
    public void create(@Nullable LittleStructure structure) {
        add(new GuiTextfield("text", structure instanceof LittleStructureMessage ? ((LittleStructureMessage) structure).text : "Hello World!"));
        add(new GuiCheckBox("rightclick", structure instanceof LittleStructureMessage ? ((LittleStructureMessage) structure).allowRightClick : true)
                .setTranslate("gui.door.rightclick"));
    }
    
    @Override
    public LittleStructure save(LittleStructure structure) {
        LittleStructureMessage message = (LittleStructureMessage) structure;
        message.text = get("text", GuiTextfield.class).getText();
        message.allowRightClick = get("rightclick", GuiCheckBox.class).value;
        return structure;
    }
    
}