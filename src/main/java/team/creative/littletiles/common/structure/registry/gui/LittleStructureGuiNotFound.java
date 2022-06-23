package team.creative.littletiles.common.structure.registry.gui;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;

@OnlyIn(Dist.CLIENT)
public class LittleStructureGuiNotFound extends LittleStructureGuiControl {
    
    public LittleStructureGuiNotFound(LittleStructureType type, AnimationGuiHandler handler) {
        super(type, handler);
    }
    
    @Override
    protected void createExtra(LittleGroup group, LittleStructure structure) {}
    
    @Override
    protected void saveExtra(LittleStructure structure, LittleGroup previews) {
        structure.load(previews.getStructureTag());
    }
    
}