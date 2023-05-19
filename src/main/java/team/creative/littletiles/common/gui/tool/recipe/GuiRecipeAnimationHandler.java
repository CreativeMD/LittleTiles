package team.creative.littletiles.common.gui.tool.recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import team.creative.creativecore.common.gui.controls.timeline.GuiAnimationHandler;
import team.creative.creativecore.common.gui.controls.tree.GuiTreeItem;
import team.creative.creativecore.common.util.type.itr.FilterIterator;
import team.creative.creativecore.common.util.type.itr.TreeIterator;
import team.creative.littletiles.common.structure.animation.AnimationTimeline;
import team.creative.littletiles.common.structure.animation.AnimationTimeline.AnimationEventEntry;
import team.creative.littletiles.common.structure.animation.PhysicalState;
import team.creative.littletiles.common.structure.animation.context.AnimationContext;
import team.creative.littletiles.common.structure.animation.event.ChildDoorEvent;
import team.creative.littletiles.common.structure.type.animation.LittleDoor;

public class GuiRecipeAnimationHandler implements GuiAnimationHandler {
    
    private int duration = 0;
    private int lastTick = -1;
    private boolean loop = true;
    private boolean playing = false;
    private int tick = 0;
    private AnimationTimeline timeline;
    private GuiTreeItemStructure current;
    private HashMap<GuiTreeItemStructure, ChildAnimationTimeline> childTimelines;
    
    public void setTimeline(GuiTreeItemStructure current, AnimationTimeline timeline) {
        reset();
        this.timeline = timeline;
        this.current = current;
        this.duration = timeline.duration;
        this.tick = Math.min(tick, duration);
        childTimelines = new HashMap<>();
        prepareChildEvents(current, timeline);
        updateState();
    }
    
    protected void prepareChildEvents(GuiTreeItemStructure parent, AnimationTimeline timeline) {
        prepareChildEvents(current, () -> new FilterIterator<AnimationEventEntry>(timeline.allEvents(), x -> x.getEvent().getClass() == ChildDoorEvent.class));
    }
    
    protected void prepareChildEvents(GuiTreeItemStructure parent, Iterable<AnimationEventEntry> events) {
        for (AnimationEventEntry entry : events) {
            GuiTreeItemStructure item = (GuiTreeItemStructure) parent.getItem(((ChildDoorEvent) entry.getEvent()).childId);
            if (item == null || !(item.structure instanceof LittleDoor))
                continue;
            
            ChildAnimationTimeline timeline = childTimelines.get(item);
            if (timeline == null)
                childTimelines.put(item, timeline = new ChildAnimationTimeline(item));
            timeline.add(item, entry);
        }
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
            childTimelines = null;
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
        timeline.setStateAtTick(tick, current.physicalState, current);
        for (Entry<GuiTreeItemStructure, ChildAnimationTimeline> entry : childTimelines.entrySet())
            entry.getValue().setTick(entry.getKey(), tick);
        lastTick = tick;
    }
    
    public GuiTreeItemStructure current() {
        return current;
    }
    
    public AnimationContext context() {
        return current;
    }
    
    public class ChildAnimationTimeline {
        
        private List<ChildAnimationTimelineEntry> entries = new ArrayList<>();
        private final PhysicalState closed;
        private final PhysicalState opened;
        
        public ChildAnimationTimeline(GuiTreeItemStructure item) {
            LittleDoor door = (LittleDoor) item.structure;
            this.closed = door.getState("closed");
            this.opened = door.getState("opened");
            entries.add(new ChildAnimationTimelineEntryStill(item, 0, false, closed));
            
        }
        
        public PhysicalState get(boolean opened) {
            return opened ? this.opened : this.closed;
        }
        
        public void add(GuiTreeItemStructure item, AnimationEventEntry entry) {
            ChildAnimationTimelineEntryStill last = (ChildAnimationTimelineEntryStill) entries.get(entries.size() - 1);
            if (last.start == entry.start)
                entries.remove(entries.size() - 1);
            else if (last.start > entry.start) // Animation cannot be started because previous is too late
                return;
            
            LittleDoor door = (LittleDoor) item.structure;
            AnimationTimeline timeline = door.getTransition(last.opened ? "closing" : "opening").copy();
            if (last.opened)
                timeline.start(opened, closed, door.interpolation::create1d);
            else
                timeline.start(closed, opened, door.interpolation::create1d);
            entries.add(new ChildAnimationTimelineEntryAnimation(item, entry.start, timeline));
            entries.add(new ChildAnimationTimelineEntryStill(item, entry.start + timeline.duration + 1, !last.opened, get(!last.opened)));
        }
        
        public void setTick(GuiTreeItemStructure item, int tick) {
            if (entries.isEmpty())
                return;
            
            ChildAnimationTimelineEntry last = null;
            for (ChildAnimationTimelineEntry entry : entries) {
                if (tick < entry.start)
                    break;
                last = entry;
            }
            last.set(item, tick - last.start);
        }
    }
    
    public static abstract class ChildAnimationTimelineEntry {
        
        public final int start;
        
        public ChildAnimationTimelineEntry(int start) {
            this.start = start;
        }
        
        public abstract void set(GuiTreeItemStructure item, int tick);
        
    }
    
    public static class ChildAnimationTimelineEntryStill extends ChildAnimationTimelineEntry {
        
        public final boolean opened;
        public final PhysicalState state;
        
        public ChildAnimationTimelineEntryStill(GuiTreeItemStructure item, int start, boolean opened, PhysicalState state) {
            super(start);
            this.opened = opened;
            this.state = state;
        }
        
        @Override
        public void set(GuiTreeItemStructure item, int tick) {
            item.physicalState.set(state);
        }
    }
    
    public class ChildAnimationTimelineEntryAnimation extends ChildAnimationTimelineEntry {
        
        public final AnimationTimeline timeline;
        
        public ChildAnimationTimelineEntryAnimation(GuiTreeItemStructure item, int start, AnimationTimeline timeline) {
            super(start);
            this.timeline = timeline;
            prepareChildEvents(item, timeline);
        }
        
        @Override
        public void set(GuiTreeItemStructure item, int tick) {
            timeline.setStateAtTick(tick, item.physicalState, item);
        }
        
    }
    
}
