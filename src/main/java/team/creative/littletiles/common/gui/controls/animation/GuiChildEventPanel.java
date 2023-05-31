package team.creative.littletiles.common.gui.controls.animation;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBoxMapped;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.controls.timeline.GuiTimeline;
import team.creative.creativecore.common.gui.controls.timeline.GuiTimelineChannel;
import team.creative.creativecore.common.gui.controls.timeline.GuiTimelineKey;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.common.gui.tool.recipe.GuiRecipeAnimationHandler;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.animation.AnimationTimeline;
import team.creative.littletiles.common.structure.animation.AnimationTimeline.AnimationEventEntry;
import team.creative.littletiles.common.structure.animation.event.ChildDoorEvent;
import team.creative.littletiles.common.structure.type.animation.LittleDoor;

public class GuiChildEventPanel extends GuiTimelinePanel {
    
    public final GuiTreeItemStructure item;
    public final List<GuiChildTimelineChannel> children = new ArrayList<>();
    
    public GuiChildEventPanel(GuiTreeItemStructure item, GuiRecipeAnimationHandler handler, AnimationTimeline timeline, int duration) {
        super(handler, duration);
        this.item = item;
        List<AnimationEventEntry> events = extract(timeline, ChildDoorEvent.class);
        for (AnimationEventEntry entry : events) {
            ChildDoorEvent event = (ChildDoorEvent) entry.getEvent();
            GuiChildTimelineChannel channel = getOrCreate(event.childId);
            if (channel.isSpaceFor(null, entry.start))
                channel.addKey(entry.start, event);
        }
        
        GuiParent channelControl = new GuiParent();
        add(channelControl);
        
        channelControl.add(new GuiComboBoxMapped<>("childBox", new TextMapBuilder<>()));
        channelControl.add(new GuiButton("childAdd", x -> {
            GuiComboBoxMapped<Integer> childBox = get("childBox");
            Integer selected = childBox.getSelected();
            if (selected != null)
                getOrCreate(selected);
            updateAddBox();
        }).setTranslate("gui.add"));
        channelControl.add(new GuiButton("removed_unused", x -> {
            clearUnusedChannel(children);
            updateAddBox();
        }).setTranslate("gui.door.clean.channel"));
        
        updateAddBox();
    }
    
    public void updateAddBox() {
        GuiComboBoxMapped<Integer> childBox = get("childBox");
        GuiButton button = get("childAdd");
        
        TextMapBuilder<Integer> map = new TextMapBuilder<>();
        for (int i = 0; i < item.itemsCount(); i++) {
            if (!hasChild(i) && item.getChildStructure(i) instanceof LittleDoor)
                map.addComponent(i, Component.literal(((GuiTreeItemStructure) item.getItem(i)).getTitle()));
        }
        
        childBox.setLines(map);
        childBox.setEnabled(map.size() > 0);
        button.setEnabled(map.size() > 0);
        
        if (getParent() != null)
            reflow();
    }
    
    protected GuiChildTimelineChannel getOrCreate(int id) {
        while (children.size() <= id)
            children.add(null);
        
        GuiChildTimelineChannel channel = children.get(id);
        if (channel == null) {
            children.set(id, channel = new GuiChildTimelineChannel(time, id));
            time.addGuiTimelineChannel(Component.literal("" + id), channel);
        }
        return channel;
    }
    
    protected boolean hasChild(int id) {
        return children.size() > id && children.get(id) != null;
    }
    
    public boolean isChildEmpty() {
        for (GuiChildTimelineChannel channel : children)
            if (channel != null && !channel.isChannelEmpty())
                return false;
        return true;
    }
    
    @Override
    protected void addBefore() {
        add(new GuiLabel("childLabel").setTitle(translatable("gui.door.child").append(":")));
    }
    
    public void collectEvents(int duration, List<AnimationEventEntry> events, boolean opening) {
        for (GuiChildTimelineChannel channel : children)
            for (GuiTimelineKey<ChildDoorEvent> key : channel.keys())
                if (key.tick <= duration)
                    events.add(new AnimationEventEntry(key.tick, key.value));
    }
    
    public static class GuiChildTimelineChannel extends GuiTimelineChannel<ChildDoorEvent> {
        
        public final int childId;
        
        public GuiChildTimelineChannel(GuiTimeline timeline, int childId) {
            super(timeline);
            this.childId = childId;
        }
        
        @Override
        protected ChildDoorEvent getValueAt(int time) {
            return new ChildDoorEvent(childId);
        }
        
    }
    
}
