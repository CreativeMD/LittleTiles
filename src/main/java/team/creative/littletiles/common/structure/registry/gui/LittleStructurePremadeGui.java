package team.creative.littletiles.common.structure.registry.gui;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructure;

@OnlyIn(Dist.CLIENT)
public class LittleStructurePremadeGui extends LittleStructureGuiControl {
    
    public LittleStructurePremadeGui(LittleStructureGui gui, GuiTreeItemStructure item) {
        super(gui, item);
    }
    
    @Override
    public void create(LittleStructure structure) {}
    
    @Override
    public LittleStructure save(LittleStructure structure) {
        structure.load(item.group.getStructureTag(), item.provider());
        return structure;
    }
    
    @Override
    public boolean canChangeType() {
        return false;
    }
    
}