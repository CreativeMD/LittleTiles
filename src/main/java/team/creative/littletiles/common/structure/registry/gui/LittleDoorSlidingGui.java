package team.creative.littletiles.common.structure.registry.gui;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.animation.timeline.AnimationTimeline;
import team.creative.littletiles.common.structure.LittleStructureType;

@OnlyIn(Dist.CLIENT)
public class LittleDoorSlidingGui extends LittleDoorBaseGui {
    
    public LittleDoorSlidingGui(LittleStructureType type, AnimationGuiHandler handler) {
        super(type, handler);
    }
    
    @Override
    public void populateTimeline(AnimationTimeline timeline, int interpolation) {}
    
}