package team.creative.littletiles.common.structure.registry.gui;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.controls.timeline.GuiTimeline;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructure;

@OnlyIn(Dist.CLIENT)
public class LittleDoorAdvancedGui extends LittleStructureGuiControl {
    
    public LittleDoorAdvancedGui(LittleStructureGui gui, GuiTreeItemStructure item) {
        super(gui, item);
    }
    
    @Override
    public void create(LittleStructure structure) {
        GuiTimeline timeline = new GuiTimeline();
        add(timeline.setExpandableX());
        timeline.addGuiTimelineChannel(Component.literal("test")).addKeyFixed(spacing, spacing);
        
    }
    
    @Override
    public LittleStructure save(LittleStructure structure) {
        return structure;
    }
    
}