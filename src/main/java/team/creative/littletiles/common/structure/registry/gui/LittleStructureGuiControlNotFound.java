package team.creative.littletiles.common.structure.registry.gui;

import team.creative.creativecore.common.gui.GuiParent;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;

public class LittleStructureGuiControlNotFound extends LittleStructureGuiControl {
    
    public LittleStructureGuiControlNotFound(GuiParent parent, AnimationGuiHandler handler) {
        super(parent, handler);
    }
    
    @Override
    protected void createControls(LittleGroup previews, LittleStructure structure) {}
    
    @Override
    protected LittleStructure parseStructure(LittleGroup previews) {
        LittleStructure parsedStructure = structure.type.createStructure(null);
        parsedStructure.load(previews.getStructureTag());
        return parsedStructure;
    }
    
    @Override
    protected LittleStructureType getStructureType() {
        return structure.type;
    }
    
}