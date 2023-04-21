package team.creative.littletiles.common.structure.registry.gui;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.parent.GuiLabeledControl;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButtonMapped;
import team.creative.creativecore.common.gui.controls.simple.GuiSteppedSlider;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.controls.animation.GuiIsoAnimationPanel;
import team.creative.littletiles.common.gui.controls.animation.GuiIsoAnimationViewer;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.animation.AnimationState;
import team.creative.littletiles.common.structure.animation.AnimationTimeline;
import team.creative.littletiles.common.structure.animation.curve.ValueInterpolation;
import team.creative.littletiles.common.structure.relative.StructureRelative;
import team.creative.littletiles.common.structure.type.animation.LittleDoor;

@OnlyIn(Dist.CLIENT)
public abstract class LittleDoorBaseGui extends LittleStructureGuiControl {
    
    public LittleDoorBaseGui(LittleStructureGui gui, GuiTreeItemStructure item) {
        super(gui, item);
        registerEventChanged(x -> {
            if (x.control.is("duration_s", "children_activate", "interpolation"))
                updateTimeline();
        });
    }
    
    protected abstract boolean hasAxis();
    
    @Override
    public void create(@Nullable LittleStructure structure) {
        LittleGrid grid;
        LittleBox box;
        boolean even;
        ValueInterpolation inter;
        int duration;
        boolean stayAnimated;
        boolean rightClick;
        boolean noClip;
        boolean playPlaceSounds;
        if (structure instanceof LittleDoor door) {
            grid = door.center.getGrid();
            box = door.center.getBox();
            even = door.center.isEven();
            inter = door.interpolation;
            duration = door.duration;
            stayAnimated = door.stayAnimated;
            rightClick = door.rightClick;
            noClip = door.noClip;
            playPlaceSounds = door.playPlaceSounds;
        } else {
            grid = item.group.getGrid();
            box = new LittleBox(item.group.getMinVec());
            even = false;
            inter = ValueInterpolation.HERMITE;
            duration = 10;
            stayAnimated = false;
            rightClick = true;
            noClip = false;
            playPlaceSounds = true;
        }
        
        flow = GuiFlow.STACK_Y;
        align = Align.STRETCH;
        GuiParent settings = new GuiParent(GuiFlow.FIT_X);
        add(settings);
        
        settings.add(new GuiLabeledControl(Component.translatable("gui.interpolation")
                .append(":"), new GuiStateButtonMapped<ValueInterpolation>("inter", inter, new TextMapBuilder<ValueInterpolation>()
                        .addComponent(ValueInterpolation.values(), x -> x.translate()))));
        
        settings.add(new GuiLabeledControl(Component.translatable("gui.duration").append(":"), new GuiSteppedSlider("duration", duration, 1, 500)));
        
        settings.add(new GuiCheckBox("stayAnimated", stayAnimated).setTranslate("gui.stay_animated").setTooltip("gui.door.stay_animated.tooltip"));
        settings.add(new GuiCheckBox("rightClick", rightClick).setTranslate("gui.rightclick").setTooltip("gui.door.rightclick.tooltip"));
        settings.add(new GuiCheckBox("noClip", noClip).setTranslate("gui.no_clip").setTooltip("gui.door.no_clip.tooltip"));
        settings.add(new GuiCheckBox("playPlaceSounds", playPlaceSounds).setTranslate("gui.door.play_place_sound").setTooltip("gui.door.play_place_sound.tooltip"));
        
        add(new GuiIsoAnimationPanel(item, box, grid, even).setVisibleAxis(hasAxis()));
        
        createSpecific(structure instanceof LittleDoor ? (LittleDoor) structure : null);
        /*parent.controls.add(new GuiDoorEventsButton("children_activate", 93, 107, previews, structure instanceof LittleDoorBase ? (LittleDoorBase) structure : null));
        parent.controls
                .add(new GuiStateButton("interpolation", structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).interpolation : 0, 140, 107, 40, 7, ValueTimeline.interpolationTypes));
        
        updateTimeline();*/
    }
    
    @Override
    public LittleStructure save(LittleStructure structure) {
        LittleDoor door = (LittleDoor) structure;
        
        GuiIsoAnimationViewer viewer = get("viewer");
        door.center = new StructureRelative(viewer.getBox(), viewer.getGrid());
        GuiStateButtonMapped<ValueInterpolation> inter = get("inter");
        door.interpolation = inter.getSelected();
        
        /*GuiDoorSettingsButton settings = get("settings", GuiDoorSettingsButton.class);
        door.events = get("children_activate", GuiDoorEventsButton.class).events;*/
        
        door.duration = get("duration", GuiSteppedSlider.class).getValue();
        door.stayAnimated = get("stayAnimated", GuiCheckBox.class).value;
        door.rightClick = get("rightClick", GuiCheckBox.class).value;
        door.noClip = get("noClip", GuiCheckBox.class).value;
        door.playPlaceSounds = get("playPlaceSounds", GuiCheckBox.class).value;
        
        door.putState(new AnimationState("closed"));
        AnimationState state = new AnimationState("opened");
        save(state);
        door.putState(state);
        
        return structure;
    }
    
    protected abstract void createSpecific(@Nullable LittleDoor door);
    
    protected abstract void save(AnimationState state);
    
    protected abstract void populateTimeline(AnimationTimeline timeline, int interpolation);
    
    public void updateTimeline() {
        /*AnimationTimeline timeline = new AnimationTimeline((int) get("duration_s", GuiSteppedSlider.class).value);
        
        populateTimeline(timeline, get("interpolation", GuiStateButton.class).getState());
        handler.setTimeline(timeline, get("children_activate", GuiDoorEventsButton.class).events);*/
    }
    
}