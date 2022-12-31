package team.creative.littletiles.common.structure.registry.gui;

import javax.annotation.Nullable;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;

@OnlyIn(Dist.CLIENT)
public class LittleStructureGuiNone extends LittleStructureGuiControl {
    
    public LittleStructureGuiNone(LittleStructureType type, AnimationGuiHandler handler) {
        super(type, handler);
    }
    
    @Override
    public void create(LittleGroup group, @Nullable LittleStructure structure) {}
    
    @Override
    public LittleStructure save(LittleGroup group) {
        return null;
    }
    
    @Override
    protected void createExtra(LittleGroup group, LittleStructure structure) {}
    
    @Override
    protected void saveExtra(LittleStructure structure, LittleGroup previews) {}
    
}