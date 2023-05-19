package team.creative.littletiles.common.gui.controls.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.timeline.GuiTimeline;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.flow.GuiSizeRule.GuiSizeRules;
import team.creative.littletiles.common.gui.tool.recipe.GuiRecipeAnimationHandler;
import team.creative.littletiles.common.structure.animation.AnimationTimeline;
import team.creative.littletiles.common.structure.animation.AnimationTimeline.AnimationEventEntry;
import team.creative.littletiles.common.structure.animation.event.AnimationEvent;

public class GuiTimelinePanel extends GuiParent {
    
    public static List<AnimationEventEntry> extract(AnimationTimeline timeline, Class<? extends AnimationEvent> clazz) {
        if (timeline == null)
            return Collections.EMPTY_LIST;
        
        List<AnimationEventEntry> events = null;
        for (AnimationEventEntry entry : timeline.allEvents()) {
            if (entry.getEvent().getClass() == clazz) {
                if (events == null)
                    events = new ArrayList<>();
                events.add(entry);
            }
        }
        if (events == null)
            return Collections.EMPTY_LIST;
        return events;
    }
    
    public final GuiTimeline time;
    public final GuiRecipeAnimationHandler handler;
    
    public GuiTimelinePanel(GuiRecipeAnimationHandler handler, int duration) {
        setDim(200, -1);
        flow = GuiFlow.STACK_Y;
        this.handler = handler;
        setExpandableX();
        addBefore();
        
        time = new GuiTimeline(handler);
        time.setDim(new GuiSizeRules().minHeight(10));
        time.setDuration(duration);
        add(time.setExpandableX());
    }
    
    protected void addBefore() {}
    
    public void durationChanged(int duration) {
        time.setDuration(Math.max(1, duration));
    }
}
