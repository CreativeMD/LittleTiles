package team.creative.littletiles.common.gui.controls.animation;

import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.timeline.GuiTimeline;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.flow.GuiSizeRule.GuiSizeRules;
import team.creative.littletiles.common.gui.tool.recipe.GuiRecipeAnimationHandler;

public class GuiTimelinePanel extends GuiParent {
    
    public final GuiTimeline time;
    public final GuiRecipeAnimationHandler handler;
    
    public GuiTimelinePanel(GuiRecipeAnimationHandler handler, int duration) {
        flow = GuiFlow.STACK_Y;
        this.handler = handler;
        time = new GuiTimeline(handler);
        time.setDim(new GuiSizeRules().minHeight(10));
        time.setDuration(duration);
        add(time.setExpandableX());
    }
    
    public void durationChanged(int duration) {
        time.setDuration(Math.max(1, duration));
    }
}
