package team.creative.littletiles.common.structure.registry.gui;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.littletiles.common.animation.timeline.AnimationTimeline;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;

@OnlyIn(Dist.CLIENT)
public abstract class LittleDoorBaseGui extends LittleStructureGuiControl {
    
    public LittleDoorBaseGui(LittleStructureType type, GuiTreeItemStructure item) {
        super(type, item);
        registerEventChanged(x -> {
            if (x.control.is("duration_s", "children_activate", "interpolation"))
                updateTimeline();
        });
    }
    
    @Override
    protected void createExtra(LittleGroup group, LittleStructure structure) {
        /*boolean stayAnimated = structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).stayAnimated : false;
        boolean disableRightClick = structure instanceof LittleDoor ? !((LittleDoor) structure).disableRightClick : true;
        boolean noClip = structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).noClip : false;
        boolean playPlaceSounds = structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).playPlaceSounds : true;
        parent.controls.add(new GuiDoorSettingsButton("settings", 108, 93, stayAnimated, disableRightClick, noClip, playPlaceSounds));
        parent.controls.add(new GuiLabel(CoreControl.translate("gui.door.duration") + ":", 90, 122));
        parent.controls.add(new GuiSteppedSlider("duration_s", 140, 122, 50, 6, structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).duration : 10, 1, 500));
        parent.controls.add(new GuiDoorEventsButton("children_activate", 93, 107, previews, structure instanceof LittleDoorBase ? (LittleDoorBase) structure : null));
        parent.controls
                .add(new GuiStateButton("interpolation", structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).interpolation : 0, 140, 107, 40, 7, ValueTimeline.interpolationTypes));
        
        updateTimeline();*/
    }
    
    @Override
    protected void saveExtra(LittleStructure structure, LittleGroup previews) {
        /*LittleDoorBase door = (LittleDoorBase) structure;
        
        GuiDoorSettingsButton settings = get("settings", GuiDoorSettingsButton.class);
        
        door.duration = (int) get("duration_s", GuiSteppedSlider.class).value;
        door.stayAnimated = settings.stayAnimated;
        door.disableRightClick = !settings.disableRightClick;
        door.noClip = settings.noClip;
        door.playPlaceSounds = settings.playPlaceSounds;
        door.events = get("children_activate", GuiDoorEventsButton.class).events;
        door.interpolation = get("interpolation", GuiStateButton.class).getState();*/
    }
    
    public abstract void populateTimeline(AnimationTimeline timeline, int interpolation);
    
    public void updateTimeline() {
        /*AnimationTimeline timeline = new AnimationTimeline((int) get("duration_s", GuiSteppedSlider.class).value);
        
        populateTimeline(timeline, get("interpolation", GuiStateButton.class).getState());
        handler.setTimeline(timeline, get("children_activate", GuiDoorEventsButton.class).events);*/
    }
    
}