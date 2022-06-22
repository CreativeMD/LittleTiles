package team.creative.littletiles.common.structure.registry.gui;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButton;
import team.creative.creativecore.common.gui.controls.simple.GuiSteppedSlider;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.util.type.list.PairList;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.animation.timeline.AnimationTimeline;
import team.creative.littletiles.common.gui.dialogs.SubGuiDoorEvents.GuiDoorEventsButton;
import team.creative.littletiles.common.gui.dialogs.SubGuiDoorSettings.GuiDoorSettingsButton;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.type.door.LittleDoor;
import team.creative.littletiles.common.structure.type.door.LittleDoorBase;

public abstract class LittleDoorBaseParser extends LittleStructureGuiControl {
    
    public LittleDoorBaseParser(GuiParent parent, AnimationGuiHandler handler) {
        super(parent, handler);
    }
    
    @SideOnly(Side.CLIENT)
    @CustomEventSubscribe
    public void onChanged(GuiControlChangedEvent event) {
        if (event.source.is("duration_s") || event.source.is("children_activate") || event.source.is("interpolation"))
            updateTimeline();
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void createControls(LittlePreviews previews, LittleStructure structure) {
        boolean stayAnimated = structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).stayAnimated : false;
        boolean disableRightClick = structure instanceof LittleDoor ? !((LittleDoor) structure).disableRightClick : true;
        boolean noClip = structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).noClip : false;
        boolean playPlaceSounds = structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).playPlaceSounds : true;
        parent.controls.add(new GuiDoorSettingsButton("settings", 108, 93, stayAnimated, disableRightClick, noClip, playPlaceSounds));
        parent.controls.add(new GuiLabel(CoreControl.translate("gui.door.duration") + ":", 90, 122));
        parent.controls.add(new GuiSteppedSlider("duration_s", 140, 122, 50, 6, structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).duration : 10, 1, 500));
        parent.controls.add(new GuiDoorEventsButton("children_activate", 93, 107, previews, structure instanceof LittleDoorBase ? (LittleDoorBase) structure : null));
        parent.controls
                .add(new GuiStateButton("interpolation", structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).interpolation : 0, 140, 107, 40, 7, ValueTimeline.interpolationTypes));
        
        updateTimeline();
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public LittleDoorBase parseStructure(LittlePreviews previews) {
        GuiSteppedSlider slider = (GuiSteppedSlider) parent.get("duration_s");
        GuiDoorSettingsButton settings = (GuiDoorSettingsButton) parent.get("settings");
        GuiDoorEventsButton button = (GuiDoorEventsButton) parent.get("children_activate");
        GuiStateButton interpolationButton = (GuiStateButton) parent.get("interpolation");
        
        int duration = (int) slider.value;
        LittleDoorBase door = parseStructure();
        door.duration = duration;
        door.stayAnimated = settings.stayAnimated;
        door.disableRightClick = !settings.disableRightClick;
        door.noClip = settings.noClip;
        door.playPlaceSounds = settings.playPlaceSounds;
        door.events = button.events;
        door.interpolation = interpolationButton.getState();
        
        return door;
    }
    
    @SideOnly(Side.CLIENT)
    public abstract LittleDoorBase parseStructure();
    
    @SideOnly(Side.CLIENT)
    public abstract void populateTimeline(AnimationTimeline timeline, int interpolation);
    
    public void updateTimeline() {
        GuiSteppedSlider slider = (GuiSteppedSlider) parent.get("duration_s");
        AnimationTimeline timeline = new AnimationTimeline((int) slider.value, new PairList<>());
        GuiDoorEventsButton children = (GuiDoorEventsButton) parent.get("children_activate");
        GuiStateButton interpolationButton = (GuiStateButton) parent.get("interpolation");
        
        populateTimeline(timeline, interpolationButton.getState());
        handler.setTimeline(timeline, children.events);
    }
    
}