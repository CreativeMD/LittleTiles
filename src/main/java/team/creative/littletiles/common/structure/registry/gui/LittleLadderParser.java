package team.creative.littletiles.common.structure.registry.gui;

import team.creative.creativecore.common.gui.GuiParent;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.type.LittleLadder;

public class LittleLadderParser extends LittleStructureGuiControl {
    
    public LittleLadderParser(GuiParent parent, AnimationGuiHandler handler) {
        super(parent, handler);
    }
    
    @Override
    public void createControls(LittleGroup previews, LittleStructure structure) {}
    
    @Override
    public LittleLadder parseStructure(LittleGroup previews) {
        return createStructure(LittleLadder.class, null);
    }
    
    @Override
    protected LittleStructureType getStructureType() {
        return LittleStructureRegistry.getStructureType(LittleLadder.class);
    }
}