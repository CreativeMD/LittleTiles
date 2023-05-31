package team.creative.littletiles.common.gui.controls.animation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBoxMapped;
import team.creative.creativecore.common.gui.controls.parent.GuiLabeledControl;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiIconButton;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.controls.simple.GuiSlider;
import team.creative.creativecore.common.gui.controls.timeline.GuiTimeline;
import team.creative.creativecore.common.gui.controls.timeline.GuiTimelineChannel;
import team.creative.creativecore.common.gui.controls.timeline.GuiTimelineKey;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.GuiIcon;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.common.gui.tool.recipe.GuiRecipeAnimationHandler;
import team.creative.littletiles.common.structure.animation.AnimationTimeline;
import team.creative.littletiles.common.structure.animation.AnimationTimeline.AnimationEventEntry;
import team.creative.littletiles.common.structure.animation.event.PlaySoundEvent;

public class GuiSoundEventPanel extends GuiTimelinePanel {
    
    protected List<GuiSoundTimelineChannel> bothChannels;
    protected List<GuiSoundTimelineChannel> closingChannels;
    protected List<GuiSoundTimelineChannel> openingChannels;
    
    public GuiTimelineKey<PlaySoundEvent> edited;
    
    public GuiSoundEventPanel(GuiRecipeAnimationHandler handler, AnimationTimeline opening, AnimationTimeline closing, int duration) {
        super(handler, duration);
        List<AnimationEventEntry> openingEvents = extract(opening, PlaySoundEvent.class);
        List<AnimationEventEntry> closingEvents = extract(closing, PlaySoundEvent.class);
        List<AnimationEventEntry> bothEvents = new ArrayList<>();
        
        outer_loop: for (Iterator<AnimationEventEntry> openingItr = openingEvents.iterator(); openingItr.hasNext();) {
            AnimationEventEntry oEvent = openingItr.next();
            for (Iterator<AnimationEventEntry> closingItr = closingEvents.iterator(); closingItr.hasNext();) {
                AnimationEventEntry cEvent = closingItr.next();
                if (oEvent.start == cEvent.start) {
                    if (oEvent.getEvent().equals(cEvent.getEvent())) {
                        openingItr.remove();
                        closingItr.remove();
                        bothEvents.add(oEvent);
                        continue outer_loop;
                    }
                } else if (oEvent.start < cEvent.start)
                    break;
            }
        }
        
        createChannels(bothEvents, AnimationDirection.BOTH);
        createChannels(openingEvents, AnimationDirection.OPENING);
        createChannels(closingEvents, AnimationDirection.CLOSING);
        
        GuiParent channelControl = new GuiParent();
        add(channelControl);
        var soundDirection = new GuiComboBoxMapped<>("sound_direction", new TextMapBuilder<AnimationDirection>().addComponent(AnimationDirection.values(), x -> x.title()));
        channelControl.add(soundDirection);
        channelControl.add(new GuiButton("add", x -> createChannel(soundDirection.getSelected())).setTranslate("gui.add"));
        channelControl.add(new GuiButton("removed_unused", x -> {
            clearUnusedChannel(bothChannels);
            clearUnusedChannel(openingChannels);
            clearUnusedChannel(closingChannels);
            reflow();
        }).setTranslate("gui.door.clean.channel"));
        
        GuiParent editKey = new GuiParent(GuiFlow.STACK_Y);
        add(editKey.setExpandableX());
        registerEvent(GuiTimeline.KeySelectedEvent.class, x -> {
            editKey.clear();
            if (x.control.channel instanceof GuiSoundTimelineChannel c) {
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
                edited = x.control;
                reflow();
            }
        });
        registerEvent(GuiTimeline.NoKeySelectedEvent.class, x -> {
            editKey.clear();
            edited = null;
        });
        
        editKey.registerEventChanged(x -> {
            PlaySoundEvent value = edited.value;
            if (x.control.is("sound") && x.control instanceof GuiComboBoxMapped box)
                value.sound = PlaySoundEvent.get((ResourceLocation) box.getSelected());
            else if (x.control.is("volume") && x.control instanceof GuiSlider slider)
                value.volume = (float) slider.value;
            else if (x.control.is("pitch") && x.control instanceof GuiSlider slider)
                value.pitch = (float) slider.value;
            time.raiseEvent(new GuiControlChangedEvent(time));
        });
    }
    
    @Override
    protected void addBefore() {
        add(new GuiLabel("soundLabel").setTitle(translatable("gui.door.sound").append(":")));
    }
    
    public boolean isSoundEmpty() {
        for (GuiSoundTimelineChannel channel : bothChannels)
            if (!channel.isChannelEmpty())
                return false;
        for (GuiSoundTimelineChannel channel : openingChannels)
            if (!channel.isChannelEmpty())
                return false;
        for (GuiSoundTimelineChannel channel : closingChannels)
            if (!channel.isChannelEmpty())
                return false;
        return true;
    }
    
    protected void collectEvents(int duration, List<AnimationEventEntry> events, List<GuiSoundTimelineChannel> channels) {
        for (GuiSoundTimelineChannel channel : channels)
            for (GuiTimelineKey<PlaySoundEvent> key : channel.keys())
                if (key.tick <= duration)
                    events.add(new AnimationEventEntry(key.tick, key.value));
    }
    
    public void collectEvents(int duration, List<AnimationEventEntry> events, boolean opening) {
        collectEvents(duration, events, bothChannels);
        collectEvents(duration, events, opening ? openingChannels : closingChannels);
    }
    
    protected void createChannel(AnimationDirection direction) {
        GuiSoundTimelineChannel channel = new GuiSoundTimelineChannel(time);
        List<GuiSoundTimelineChannel> channels = switch (direction) {
            case BOTH -> bothChannels;
            case OPENING -> openingChannels;
            case CLOSING -> closingChannels;
        };
        time.addGuiTimelineChannel(direction.title().append("" + channels.size()), channel);
        channels.add(channel);
        reflow();
    }
    
    protected void createChannels(List<AnimationEventEntry> events, AnimationDirection direction) {
        List<GuiSoundTimelineChannel> channels = new ArrayList<>();
        
        for (int i = 0; i < events.size(); i++) {
            AnimationEventEntry entry = events.get(i);
            int id = 0;
            while (true) {
                while (id >= channels.size()) {
                    GuiSoundTimelineChannel channel = new GuiSoundTimelineChannel(time);
                    channels.add(channel);
                    time.addGuiTimelineChannel(direction.title().append("" + id), channel);
                }
                if (channels.get(id).isSpaceFor(null, entry.start)) {
                    channels.get(id).addKey(entry.start, (PlaySoundEvent) entry.getEvent());
                    break;
                }
                id++;
            }
        }
        
        switch (direction) {
            case BOTH -> bothChannels = channels;
            case OPENING -> openingChannels = channels;
            case CLOSING -> closingChannels = channels;
        };
    }
    
    public static class GuiSoundTimelineChannel extends GuiTimelineChannel<PlaySoundEvent> {
        
        public GuiSoundTimelineChannel(GuiTimeline timeline) {
            super(timeline);
        }
        
        @Override
        protected PlaySoundEvent getValueAt(int time) {
            return new PlaySoundEvent(SoundEvents.UI_BUTTON_CLICK.get(), 1, 1);
        }
        
    }
    
    public static enum AnimationDirection {
        
        BOTH,
        OPENING,
        CLOSING;
        
        public MutableComponent title() {
            return GuiControl.translatable("gui.door.direction." + name().toLowerCase());
        }
    }
    
}
