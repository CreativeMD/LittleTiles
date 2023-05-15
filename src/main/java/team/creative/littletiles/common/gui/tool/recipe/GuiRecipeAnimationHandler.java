package team.creative.littletiles.common.gui.tool.recipe;

import team.creative.creativecore.common.gui.controls.timeline.GuiAnimationHandler;
import team.creative.creativecore.common.gui.controls.tree.GuiTreeItem;
import team.creative.creativecore.common.util.type.itr.TreeIterator;
import team.creative.littletiles.common.structure.animation.AnimationTimeline;

public class GuiRecipeAnimationHandler implements GuiAnimationHandler {
    
    private int duration = 0;
    private int lastTick = -1;
    private boolean loop = true;
    private boolean playing = false;
    private int tick = 0;
    private AnimationTimeline timeline;
    private GuiTreeItemStructure current;
    
    public void setTimeline(GuiTreeItemStructure current, AnimationTimeline timeline) {
        reset();
        this.timeline = timeline;
        this.current = current;
        this.duration = timeline.duration;
        this.tick = Math.min(tick, duration);
        updateState();
    }
    
    @Override
    public void loop(boolean loop) {
        this.loop = loop;
    }
    
    @Override
    public void play() {
        playing = true;
    }
    
    @Override
    public void pause() {
        playing = false;
    }
    
    @Override
    public void stop() {
        playing = false;
        set(0);
    }
    
    @Override
    public void set(int tick) {
        this.tick = tick;
    }
    
    @Override
    public int get() {
        return tick;
    }
    
    public void reset() {
        if (timeline != null) {
            if (current != null)
                for (GuiTreeItem item : (Iterable<GuiTreeItem>) () -> new TreeIterator<GuiTreeItem>(current, x -> x.items().iterator()))
                    ((GuiTreeItemStructure) item).physicalState.setZero();
            timeline = null;
            current = null;
        }
    }
    
    public void tick() {
        if (timeline == null)
            return;
        
        if (playing) {
            if (tick > timeline.duration) {
                if (loop)
                    tick = 0;
            } else
                tick++;
        }
        
        if (tick != lastTick)
            updateState();
    }
    
    protected void updateState() {
        timeline.executeState(tick, current.physicalState, null);
        lastTick = tick;
    }
    
}
