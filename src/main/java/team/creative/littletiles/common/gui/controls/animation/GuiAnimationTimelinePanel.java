package team.creative.littletiles.common.gui.controls.animation;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBoxMapped;
import team.creative.creativecore.common.gui.controls.parent.GuiLabeledControl;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiIconButton;
import team.creative.creativecore.common.gui.controls.simple.GuiSlider;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.controls.timeline.GuiTimeline;
import team.creative.creativecore.common.gui.controls.timeline.GuiTimelineKey;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.GuiIcon;
import team.creative.creativecore.common.util.math.vec.Vec1d;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.controls.GuiDistanceControl;
import team.creative.littletiles.common.gui.controls.animation.GuiChildEventPanel.GuiChildTimelineChannel;
import team.creative.littletiles.common.gui.controls.animation.GuiSoundEventPanel.GuiSoundTimelineChannel;
import team.creative.littletiles.common.gui.tool.recipe.GuiRecipeAnimationHandler;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.animation.AnimationTimeline;
import team.creative.littletiles.common.structure.animation.AnimationTimeline.AnimationEventEntry;
import team.creative.littletiles.common.structure.animation.PhysicalPart;
import team.creative.littletiles.common.structure.animation.curve.ValueCurve;
import team.creative.littletiles.common.structure.animation.curve.ValueCurveInterpolation;
import team.creative.littletiles.common.structure.animation.curve.ValueInterpolation;
import team.creative.littletiles.common.structure.animation.event.ChildDoorEvent;
import team.creative.littletiles.common.structure.animation.event.PlaySoundEvent;
import team.creative.littletiles.common.structure.registry.gui.LittleDoorAdvancedGui.GuiAdvancedTimelineChannel;
import team.creative.littletiles.common.structure.type.animation.LittleDoor;

public class GuiAnimationTimelinePanel extends GuiTimelinePanel {
    
    private GuiAdvancedTimelineChannel rotX;
    private GuiAdvancedTimelineChannel rotY;
    private GuiAdvancedTimelineChannel rotZ;
    private GuiAdvancedTimelineChannel offX;
    private GuiAdvancedTimelineChannel offY;
    private GuiAdvancedTimelineChannel offZ;
    
    protected final List<GuiSoundTimelineChannel> soundChannels = new ArrayList<>();
    protected final List<GuiChildTimelineChannel> childChannels = new ArrayList<>();
    
    public final GuiTreeItemStructure item;
    public GuiTimelineKey edited;
    
    public GuiAnimationTimelinePanel(GuiTreeItemStructure item, GuiRecipeAnimationHandler handler, int duration, AnimationTimeline timeline, boolean limited) {
        super(handler, duration);
        this.item = item;
        
        for (PhysicalPart part : PhysicalPart.values()) {
            GuiAdvancedTimelineChannel channel = new GuiAdvancedTimelineChannel(time, part.offset, limited);
            if (timeline.get(part) instanceof ValueCurveInterpolation<Vec1d> curve) {
                for (Pair<Integer, Vec1d> pair : curve)
                    channel.addKey(pair.key, pair.value.x);
            }
            time.addGuiTimelineChannel(part.title(), channel);
            set(part, channel);
        }
        
        List<AnimationEventEntry> soundEvents = extract(timeline, PlaySoundEvent.class);
        for (int i = 0; i < soundEvents.size(); i++) {
            AnimationEventEntry entry = soundEvents.get(i);
            int id = 0;
            while (true) {
                while (id >= soundChannels.size()) {
                    GuiSoundTimelineChannel channel = new GuiSoundTimelineChannel(time);
                    soundChannels.add(channel);
                    time.addGuiTimelineChannel(translatable("gui.door.sound").append("" + id), channel);
                }
                if (soundChannels.get(id).isSpaceFor(null, entry.start)) {
                    soundChannels.get(id).addKey(entry.start, (PlaySoundEvent) entry.getEvent());
                    break;
                }
                id++;
            }
        }
        
        List<AnimationEventEntry> childEvents = extract(timeline, ChildDoorEvent.class);
        for (AnimationEventEntry entry : childEvents) {
            ChildDoorEvent event = (ChildDoorEvent) entry.getEvent();
            GuiChildTimelineChannel channel = getOrCreateChild(event.childId);
            if (channel.isSpaceFor(null, entry.start))
                channel.addKey(entry.start, event);
        }
        
        GuiParent addPanel = new GuiParent(GuiFlow.FIT_X);
        add(addPanel.setExpandableX());
        addPanel.add(new GuiButton("add_sound", x -> {
            GuiSoundTimelineChannel channel = new GuiSoundTimelineChannel(time);
            time.addGuiTimelineChannel(translatable("gui.door.sound").append("" + soundChannels.size()), channel);
            soundChannels.add(channel);
            reflow();
        }).setTranslate("gui.door.sound.add"));
        
        addPanel.add(new GuiComboBoxMapped<>("childBox", new TextMapBuilder<>()));
        addPanel.add(new GuiButton("childAdd", x -> {
            GuiComboBoxMapped<Integer> childBox = get("childBox");
            Integer selected = childBox.getSelected();
            if (selected != null)
                getOrCreateChild(selected);
            updateAddBox();
        }).setTranslate("gui.add"));
        
        addPanel.add(new GuiButton("removed_unused", x -> {
            GuiTimelinePanel.clearUnusedChannel(soundChannels);
            GuiTimelinePanel.clearUnusedChannel(childChannels);
            
            updateAddBox();
        }).setTranslate("gui.door.clean.channel"));
        
        GuiParent editKey = new GuiParent(GuiFlow.FIT_X);
        add(editKey.setExpandableX());
        registerEvent(GuiTimeline.KeySelectedEvent.class, x -> {
            editKey.clear();
            if (x.control.channel instanceof GuiAdvancedTimelineChannel c)
                if (c.distance) {
                    GuiDistanceControl distance = new GuiDistanceControl("distance", LittleGrid.min(), 0);
                    distance.setVanillaDistance((double) x.control.value);
                    editKey.add(distance);
                } else
                    editKey.add(new GuiTextfield("value", "" + x.control.value).setFloatOnly());
            else if (x.control.channel instanceof GuiChildTimelineChannel c) {
                edited = null;
                reflow();
            } else if (x.control.channel instanceof GuiSoundTimelineChannel c) {
                PlaySoundEvent value = (PlaySoundEvent) x.control.value;
                var box = new GuiComboBoxMapped<ResourceLocation>("sound", new TextMapBuilder<ResourceLocation>().addComponent(BuiltInRegistries.SOUND_EVENT.keySet(), y -> {
                    if (y.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE))
                        return Component.literal(y.getPath());
                    return Component.literal(y.toString());
                })).setSearchbar(true);
                box.select(value.sound.getLocation());
                editKey.add(box);
                GuiParent other = new GuiParent().setVAlign(VAlign.CENTER);
                other.add(new GuiLabeledControl(GuiControl.translatable("gui.volume").append(":"), new GuiSlider("volume", value.volume, 0, 1).setDim(40, 10)));
                other.add(new GuiLabeledControl(GuiControl.translatable("gui.pitch").append(":"), new GuiSlider("pitch", value.pitch, 0.5, 2).setDim(40, 10)));
                other.add(new GuiIconButton("play", GuiIcon.PLAY, y -> {
                    GuiSlider volume = other.get("volume");
                    GuiSlider pitch = other.get("pitch");
                    GuiControl.playSound(PlaySoundEvent.get(box.getSelected()), (float) volume.value, (float) pitch.value);
                }));
                editKey.add(other);
            }
            edited = x.control;
            reflow();
        });
        registerEvent(GuiTimeline.NoKeySelectedEvent.class, x -> {
            editKey.clear();
            edited = null;
        });
        
        editKey.registerEventChanged(x -> {
            if (x.control instanceof GuiDistanceControl distance) {
                edited.value = distance.getVanillaDistance();
                time.raiseEvent(new GuiControlChangedEvent(time));
            } else if (x.control instanceof GuiTextfield text) {
                edited.value = text.parseDouble();
                time.raiseEvent(new GuiControlChangedEvent(time));
            }
            
            if (edited != null && edited.value instanceof PlaySoundEvent value) {
                if (x.control.is("sound") && x.control instanceof GuiComboBoxMapped box)
                    value.sound = PlaySoundEvent.get((ResourceLocation) box.getSelected());
                else if (x.control.is("volume") && x.control instanceof GuiSlider slider)
                    value.volume = (float) slider.value;
                else if (x.control.is("pitch") && x.control instanceof GuiSlider slider)
                    value.pitch = (float) slider.value;
                time.raiseEvent(new GuiControlChangedEvent(time));
            }
        });
        
        updateAddBox();
    }
    
    protected GuiChildTimelineChannel getOrCreateChild(int id) {
        while (childChannels.size() <= id)
            childChannels.add(null);
        
        GuiChildTimelineChannel channel = childChannels.get(id);
        if (channel == null) {
            childChannels.set(id, channel = new GuiChildTimelineChannel(time, id));
            time.addGuiTimelineChannel(Component.literal("" + id), channel);
        }
        return channel;
    }
    
    protected boolean hasChild(int id) {
        return childChannels.size() > id && childChannels.get(id) != null;
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
    
    public GuiAdvancedTimelineChannel get(PhysicalPart part) {
        return switch (part) {
            case OFFX -> offX;
            case OFFY -> offY;
            case OFFZ -> offZ;
            case ROTX -> rotX;
            case ROTY -> rotY;
            case ROTZ -> rotZ;
        };
    }
    
    public void set(PhysicalPart part, GuiAdvancedTimelineChannel value) {
        switch (part) {
            case OFFX -> offX = value;
            case OFFY -> offY = value;
            case OFFZ -> offZ = value;
            case ROTX -> rotX = value;
            case ROTY -> rotY = value;
            case ROTZ -> rotZ = value;
        }
    }
    
    protected ValueCurve<Vec1d> parse(Iterable<GuiTimelineKey<Double>> keys, ValueInterpolation interpolation, int duration) {
        ValueCurveInterpolation<Vec1d> curve = interpolation.create1d();
        for (GuiTimelineKey<Double> key : keys)
            if (key.tick != 0 && key.tick < duration)
                curve.add(key.tick, new Vec1d(key.value));
        return curve;
    }
    
    public AnimationTimeline generateTimeline(int duration, ValueInterpolation interpolation) {
        List<AnimationEventEntry> events = new ArrayList<>();
        for (GuiChildTimelineChannel channel : childChannels)
            for (GuiTimelineKey<ChildDoorEvent> key : channel.keys())
                if (key.tick <= duration)
                    events.add(new AnimationEventEntry(key.tick, key.value));
                
        for (GuiSoundTimelineChannel channel : soundChannels)
            for (GuiTimelineKey<PlaySoundEvent> key : channel.keys())
                if (key.tick <= duration)
                    events.add(new AnimationEventEntry(key.tick, key.value));
                
        AnimationTimeline timeline = new AnimationTimeline(duration, events);
        for (PhysicalPart part : PhysicalPart.values()) {
            GuiAdvancedTimelineChannel channel = get(part);
            timeline.set(part, channel.isChannelEmpty() ? ValueCurve.ONE_EMPTY : parse(channel.keys(), interpolation, duration));
        }
        
        return timeline;
    }
    
}
