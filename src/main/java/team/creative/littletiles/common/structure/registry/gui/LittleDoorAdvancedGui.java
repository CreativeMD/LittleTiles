package team.creative.littletiles.common.structure.registry.gui;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButton;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.util.type.list.PairList;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.animation.property.AnimationProperty;
import team.creative.littletiles.common.animation.timeline.AnimationTimeline;
import team.creative.littletiles.common.gui.controls.GuiLTDistance;
import team.creative.littletiles.common.gui.controls.GuiTileViewer;
import team.creative.littletiles.common.gui.dialogs.SubGuiDialogAxis.GuiAxisButton;
import team.creative.littletiles.common.gui.dialogs.SubGuiDoorEvents.GuiDoorEventsButton;
import team.creative.littletiles.common.gui.dialogs.SubGuiDoorSettings.GuiDoorSettingsButton;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.relative.StructureRelative;
import team.creative.littletiles.common.structure.type.door.LittleAdvancedDoor;
import team.creative.littletiles.common.structure.type.door.LittleDoor;
import team.creative.littletiles.common.structure.type.door.LittleDoorBase;

@OnlyIn(Dist.CLIENT)
public class LittleDoorAdvancedGui extends LittleStructureGuiControl {
    
    public LittleGridContext context;
    
    public LittleDoorAdvancedGui(LittleStructureType type, AnimationGuiHandler handler) {
        super(type, handler);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void createControls(LittlePreviews previews, LittleStructure structure) {
        LittleAdvancedDoor door = structure instanceof LittleAdvancedDoor ? (LittleAdvancedDoor) structure : null;
        List<TimelineChannel> channels = new ArrayList<>();
        channels.add(new TimelineChannelDouble("rot X").addKeys(door != null && door.rotX != null ? door.rotX.getPointsCopy() : null));
        channels.add(new TimelineChannelDouble("rot Y").addKeys(door != null && door.rotY != null ? door.rotY.getPointsCopy() : null));
        channels.add(new TimelineChannelDouble("rot Z").addKeys(door != null && door.rotZ != null ? door.rotZ.getPointsCopy() : null));
        channels.add(new TimelineChannelInteger("off X").addKeys(door != null && door.offX != null ? door.offX.getRoundedPointsCopy() : null));
        channels.add(new TimelineChannelInteger("off Y").addKeys(door != null && door.offY != null ? door.offY.getRoundedPointsCopy() : null));
        channels.add(new TimelineChannelInteger("off Z").addKeys(door != null && door.offZ != null ? door.offZ.getRoundedPointsCopy() : null));
        parent.controls.add(new GuiTimeline("timeline", 0, 0, 190, 67, door != null ? door.duration : 10, channels, handler).setSidebarWidth(30));
        parent.controls.add(new GuiLabel("tick", "0", 150, 75));
        
        context = door != null ? (door.offGrid != null ? door.offGrid : LittleGridContext.get()) : LittleGridContext.get();
        parent.controls.add(new GuiTextfield("keyValue", "", 0, 75, 40, 10).setFloatOnly().setEnabled(false));
        parent.controls.add(new GuiLTDistance("keyDistance", 0, 75, context, 0).setVisible(false));
        
        parent.controls.add(new GuiLabel("Position:", 90, 90));
        parent.controls.add(new GuiTextfield("keyPosition", "", 149, 90, 40, 10).setNumbersOnly().setEnabled(false));
        
        parent.controls.add(new GuiAxisButton("axis", "open axis", 0, 93, 50, 10, previews
                .getContext(), structure instanceof LittleAdvancedDoor ? (LittleAdvancedDoor) structure : null, handler));
        
        boolean stayAnimated = structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).stayAnimated : false;
        boolean disableRightClick = structure instanceof LittleDoor ? !((LittleDoor) structure).disableRightClick : true;
        boolean noClip = structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).noClip : false;
        boolean playPlaceSounds = structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).playPlaceSounds : true;
        parent.controls.add(new GuiDoorSettingsButton("settings", 0, 110, stayAnimated, disableRightClick, noClip, playPlaceSounds));
        parent.controls.add(new GuiLabel(CoreControl.translate("gui.door.duration") + ":", 90, 122));
        parent.controls.add(new GuiTextfield("duration_s", structure instanceof LittleAdvancedDoor ? "" + ((LittleDoorBase) structure).duration : "" + 10, 149, 121, 40, 8)
                .setNumbersOnly());
        parent.controls
                .add(new GuiStateButton("interpolation", structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).interpolation : 0, 140, 107, 40, 7, ValueTimeline.interpolationTypes));
        parent.controls.add(new GuiDoorEventsButton("children_activate", 93, 107, previews, structure instanceof LittleDoorBase ? (LittleDoorBase) structure : null));
        updateTimeline();
    }
    
    public void updateTimeline() {
        GuiTimeline timeline = (GuiTimeline) parent.get("timeline");
        GuiDoorEventsButton children = (GuiDoorEventsButton) parent.get("children_activate");
        AnimationTimeline animation = new AnimationTimeline(timeline.getDuration(), new PairList<>());
        GuiStateButton interpolationButton = (GuiStateButton) parent.get("interpolation");
        int interpolation = interpolationButton.getState();
        
        ValueTimeline rotX = ValueTimeline.create(interpolation, timeline.channels.get(0).getPairs());
        if (rotX != null)
            animation.values.add(AnimationProperty.rotX, rotX);
        
        ValueTimeline rotY = ValueTimeline.create(interpolation, timeline.channels.get(1).getPairs());
        if (rotY != null)
            animation.values.add(AnimationProperty.rotY, rotY);
        
        ValueTimeline rotZ = ValueTimeline.create(interpolation, timeline.channels.get(2).getPairs());
        if (rotZ != null)
            animation.values.add(AnimationProperty.rotZ, rotZ);
        
        ValueTimeline offX = ValueTimeline.create(interpolation, timeline.channels.get(3).getPairs());
        if (offX != null)
            animation.values.add(AnimationProperty.offX, offX.factor(context.pixelSize));
        
        ValueTimeline offY = ValueTimeline.create(interpolation, timeline.channels.get(4).getPairs());
        if (offY != null)
            animation.values.add(AnimationProperty.offY, offY.factor(context.pixelSize));
        
        ValueTimeline offZ = ValueTimeline.create(interpolation, timeline.channels.get(5).getPairs());
        if (offZ != null)
            animation.values.add(AnimationProperty.offZ, offZ.factor(context.pixelSize));
        
        handler.setTimeline(animation, children.events);
        
        GuiDoorSettingsButton settings = (GuiDoorSettingsButton) parent.get("settings");
        settings.stayAnimatedPossible = animation.isFirstAligned();
    }
    
    @SideOnly(Side.CLIENT)
    private KeyControl selected;
    
    @CustomEventSubscribe
    @SideOnly(Side.CLIENT)
    public void onKeySelected(KeySelectedEvent event) {
        GuiTextfield textfield = (GuiTextfield) parent.get("keyValue");
        GuiLTDistance distance = (GuiLTDistance) parent.get("keyDistance");
        
        selected = (KeyControl) event.source;
        
        if (((KeyControl) event.source).value instanceof Double) {
            distance.setVisible(false);
            textfield.setEnabled(true);
            textfield.setVisible(true);
            textfield.text = "" + selected.value;
        } else {
            distance.setEnabled(true);
            distance.setVisible(true);
            textfield.setVisible(false);
            
            distance.setDistance(context, (int) selected.value);
        }
        
        GuiTextfield position = (GuiTextfield) parent.get("keyPosition");
        position.setEnabled(true);
        position.text = "" + selected.tick;
    }
    
    @CustomEventSubscribe
    @SideOnly(Side.CLIENT)
    public void onChange(GuiControlChangedEvent event) {
        if (event.source.is("keyDistance")) {
            
            if (!selected.modifiable)
                return;
            
            GuiLTDistance distance = (GuiLTDistance) event.source;
            LittleGridContext newContext = distance.getDistanceContext();
            if (newContext.size > context.size) {
                int scale = newContext.size / context.size;
                GuiTimeline timeline = (GuiTimeline) parent.get("timeline");
                for (TimelineChannel channel : timeline.channels) {
                    if (channel instanceof TimelineChannelInteger) {
                        for (Object control : channel.controls) {
                            ((KeyControl<Integer>) control).value *= scale;
                        }
                    }
                }
            }
            
            context = newContext;
            selected.value = distance.getDistance();
        } else if (event.source.is("keyValue")) {
            if (!selected.modifiable)
                return;
            
            try {
                selected.value = Double.parseDouble(((GuiTextfield) event.source).text);
            } catch (NumberFormatException e) {
                
            }
        } else if (event.source.is("keyPosition")) {
            if (!selected.modifiable)
                return;
            
            try {
                GuiTimeline timeline = (GuiTimeline) parent.get("timeline");
                
                int tick = selected.tick;
                int newTick = Integer.parseInt(((GuiTextfield) event.source).text);
                if (selected.channel.isSpaceFor(selected, newTick)) {
                    selected.tick = newTick;
                    selected.channel.movedKey(selected);
                    if (tick != selected.tick)
                        timeline.adjustKeysPositionX();
                }
            } catch (NumberFormatException e) {
                
            }
        } else if (event.source.is("duration_s")) {
            try {
                GuiTimeline timeline = (GuiTimeline) parent.get("timeline");
                timeline.setDuration(Integer.parseInt(((GuiTextfield) event.source).text));
            } catch (NumberFormatException e) {
                
            }
        } else if (event.source.is("timeline") || event.source.is("children_activate") || event.source.is("interpolation"))
            updateTimeline();
    }
    
    @CustomEventSubscribe
    @SideOnly(Side.CLIENT)
    public void onKeyDeselected(KeyDeselectedEvent event) {
        selected = null;
        GuiTextfield textfield = (GuiTextfield) parent.get("keyValue");
        textfield.setEnabled(false);
        textfield.text = "";
        textfield.setCursorPositionZero();
        
        textfield = (GuiTextfield) parent.get("keyPosition");
        textfield.setEnabled(false);
        textfield.text = "";
        textfield.setCursorPositionZero();
        
        GuiLTDistance distance = (GuiLTDistance) parent.get("keyDistance");
        distance.setEnabled(false);
        distance.resetTextfield();
        
        updateTimeline();
    }
    
    @CustomEventSubscribe
    @SideOnly(Side.CLIENT)
    public void toolTip(GuiToolTipEvent event) {
        if (event.source.is("timeline")) {
            ((GuiLabel) parent.get("tick")).setCaption(event.tooltip.get(0));
            event.CancelEvent();
        }
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public LittleStructure parseStructure(LittlePreviews previews) {
        LittleAdvancedDoor door = createStructure(LittleAdvancedDoor.class, null);
        GuiTileViewer viewer = ((GuiAxisButton) parent.get("axis")).viewer;
        GuiDoorEventsButton button = (GuiDoorEventsButton) parent.get("children_activate");
        door.axisCenter = new StructureRelative(viewer.getBox(), viewer.getAxisContext());
        GuiTimeline timeline = (GuiTimeline) parent.get("timeline");
        door.duration = timeline.getDuration();
        
        GuiDoorSettingsButton settings = (GuiDoorSettingsButton) parent.get("settings");
        GuiStateButton interpolationButton = (GuiStateButton) parent.get("interpolation");
        door.events = button.events;
        door.disableRightClick = !settings.disableRightClick;
        door.interpolation = interpolationButton.getState();
        
        door.rotX = ValueTimeline.create(door.interpolation, timeline.channels.get(0).getPairs());
        door.rotY = ValueTimeline.create(door.interpolation, timeline.channels.get(1).getPairs());
        door.rotZ = ValueTimeline.create(door.interpolation, timeline.channels.get(2).getPairs());
        door.offX = ValueTimeline.create(door.interpolation, timeline.channels.get(3).getPairs());
        door.offY = ValueTimeline.create(door.interpolation, timeline.channels.get(4).getPairs());
        door.offZ = ValueTimeline.create(door.interpolation, timeline.channels.get(5).getPairs());
        
        door.noClip = settings.noClip;
        if (!isAligned(AnimationProperty.offX, door.offX) || !isAligned(AnimationProperty.offY, door.offY) || !isAligned(AnimationProperty.offZ, door.offZ) || !isAligned(AnimationProperty.rotX, door.rotX) || !isAligned(AnimationProperty.rotY, door.rotY) || !isAligned(AnimationProperty.rotZ, door.rotZ))
            door.stayAnimated = true;
        else
            door.stayAnimated = settings.stayAnimated;
        door.playPlaceSounds = settings.playPlaceSounds;
        door.offGrid = context;
        return door;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    protected LittleStructureType getStructureType() {
        return LittleStructureRegistry.getStructureType(LittleAdvancedDoor.class);
    }
    
}