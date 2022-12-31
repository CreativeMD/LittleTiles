package team.creative.littletiles.common.structure.registry.gui;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.type.LittleStructureMessage;

@OnlyIn(Dist.CLIENT)
public class LittleMessageGui extends LittleStructureGuiControl {
    
    public LittleMessageGui(LittleStructureType type, GuiTreeItemStructure item) {
        super(type, item);
    }
    
    @Override
    protected void createExtra(LittleGroup group, LittleStructure structure) {
        add(new GuiTextfield("text", structure instanceof LittleStructureMessage ? ((LittleStructureMessage) structure).text : "Hello World!"));
        add(new GuiCheckBox("rightclick", structure instanceof LittleStructureMessage ? ((LittleStructureMessage) structure).allowRightClick : true)
                .setTranslate("gui.door.rightclick"));
    }
    
    @Override
    protected void saveExtra(LittleStructure structure, LittleGroup previews) {
        LittleStructureMessage message = (LittleStructureMessage) structure;
        message.text = get("text", GuiTextfield.class).getText();
        message.allowRightClick = get("rightclick", GuiCheckBox.class).value;
    }
    
}