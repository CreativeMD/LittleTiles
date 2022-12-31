package team.creative.littletiles.common.structure.registry.gui;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;

@OnlyIn(Dist.CLIENT)
public class LittleStructureGuiDefault extends LittleStructureGuiControl {
    
    public LittleStructureGuiDefault(LittleStructureType type, GuiTreeItemStructure item) {
        super(type, item);
    }
    
    @Override
    protected void createExtra(LittleGroup group, LittleStructure structure) {}
    
    @Override
    protected void saveExtra(LittleStructure structure, LittleGroup previews) {}
    
}