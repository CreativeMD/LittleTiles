package team.creative.littletiles.common.structure.registry.gui;

import javax.annotation.Nullable;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.control.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.control.simple.GuiTextfield;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.littletiles.LittleTiles;
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
        flow = GuiFlow.STACK_Y;
        add(new GuiTextfield("text", structure instanceof LittleStructureMessage m ? m.text : "Hello World!", LittleTiles.CONFIG.general.messageStructureLength).setExpandableX());
        add(new GuiCheckBox("rightclick", structure instanceof LittleStructureMessage m ? m.allowRightClick : true).setTranslate("gui.message.rightclick"));
        add(new GuiCheckBox("status", structure instanceof LittleStructureMessage m ? m.status : false).setTranslate("gui.message.status"));
    }
    
    @Override
    public LittleStructure save(LittleStructure structure) {
        LittleStructureMessage message = (LittleStructureMessage) structure;
        message.text = get("text", GuiTextfield.class).getText();
        message.allowRightClick = get("rightclick", GuiCheckBox.class).value;
        message.status = get("status", GuiCheckBox.class).value;
        return structure;
    }
    
}