package team.creative.littletiles.common.structure.registry.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.control.collection.GuiListBoxBase;
import team.creative.creativecore.common.gui.control.simple.GuiButton;
import team.creative.creativecore.common.gui.control.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.control.simple.GuiTextfield;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.flow.GuiSizeRule;
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
        add(new GuiCheckBox("rightclick", structure instanceof LittleStructureMessage m ? m.allowRightClick : true).setTranslate("gui.message.rightclick"));
        add(new GuiCheckBox("status", structure instanceof LittleStructureMessage m ? m.status : false).setTranslate("gui.message.status"));
        
        GuiListBoxBase<GuiTextfield> list = new GuiListBoxBase<>("lines", true, new ArrayList<>());
        add(list.setDim(new GuiSizeRule.GuiSizeRules().prefHeight(100)));
        List<String> lines;
        if (structure instanceof LittleStructureMessage m && m.text != null && m.text.size() > 0)
            lines = m.text;
        else
            lines = Arrays.asList("Hello World!");
        
        for (int i = 0; i < lines.size(); i++)
            list.addItem((GuiTextfield) new GuiTextfield("" + i, lines.get(i), LittleTiles.CONFIG.general.messageStructureLength).setExpandableX());
        
        add(new GuiButton("add", x -> {
            list.addItem((GuiTextfield) new GuiTextfield("added", "", LittleTiles.CONFIG.general.messageStructureLength).setExpandableX());
        }).setTranslate("gui.add"));
    }
    
    @Override
    public LittleStructure save(LittleStructure structure) {
        LittleStructureMessage message = (LittleStructureMessage) structure;
        
        GuiListBoxBase<GuiTextfield> list = get("lines");
        message.text = new ArrayList<>();
        for (GuiTextfield item : list.items())
            message.text.add(item.getText());
        message.allowRightClick = get("rightclick", GuiCheckBox.class).value;
        message.status = get("status", GuiCheckBox.class).value;
        return structure;
    }
    
}