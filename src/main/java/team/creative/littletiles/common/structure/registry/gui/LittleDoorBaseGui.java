package team.creative.littletiles.common.structure.registry.gui;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.controls.parent.GuiLabeledControl;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButtonMapped;
import team.creative.creativecore.common.gui.controls.simple.GuiSteppedSlider;
import team.creative.creativecore.common.gui.controls.timeline.GuiTimeline;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.controls.animation.GuiChildEventPanel;
import team.creative.littletiles.common.gui.controls.animation.GuiIsoAnimationPanel;
import team.creative.littletiles.common.gui.controls.animation.GuiIsoAnimationViewer;
import team.creative.littletiles.common.gui.controls.animation.GuiSoundEventPanel;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.animation.AnimationState;
import team.creative.littletiles.common.structure.animation.AnimationTimeline;
import team.creative.littletiles.common.structure.animation.AnimationTimeline.AnimationEventEntry;
import team.creative.littletiles.common.structure.animation.PhysicalState;
import team.creative.littletiles.common.structure.animation.curve.ValueInterpolation;
import team.creative.littletiles.common.structure.relative.StructureRelative;
import team.creative.littletiles.common.structure.type.animation.LittleDoor;

@OnlyIn(Dist.CLIENT)
public abstract class LittleDoorBaseGui extends LittleStructureGuiControl {
    
    public GuiSoundEventPanel soundPanel;
    public GuiChildEventPanel childPanel;
    
    public LittleDoorBaseGui(LittleStructureGui gui, GuiTreeItemStructure item) {
        super(gui, item);
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
        AnimationTimeline opening;
        AnimationTimeline closing;
        if (structure instanceof LittleDoor door) {
            grid = door.center.getGrid();
            box = door.center.getBox();
            even = door.center.isEven();
            inter = door.interpolation;
            duration = Math.max(1, door.duration);
            stayAnimated = door.stayAnimated;
            rightClick = door.rightClick;
            noClip = door.noClip;
            playPlaceSounds = door.playPlaceSounds;
            opening = door.getTransition("opening");
            closing = door.getTransition("closing");
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
            opening = closing = null;
        }
        
        flow = GuiFlow.STACK_Y;
        align = Align.STRETCH;
        GuiParent settings = new GuiParent(GuiFlow.FIT_X).setVAlign(VAlign.CENTER);
        add(settings);
        
        settings.add(new GuiLabeledControl(Component.translatable("gui.interpolation")
                .append(":"), new GuiStateButtonMapped<ValueInterpolation>("inter", inter, new TextMapBuilder<ValueInterpolation>()
                        .addComponent(ValueInterpolation.values(), x -> x.translate()))));
        
        settings.add(new GuiLabeledControl(Component.translatable("gui.duration").append(":"), new GuiSteppedSlider("duration", duration, 1, 500)));
        
        settings.add(new GuiCheckBox("stayAnimated", stayAnimated).setTranslate("gui.stay_animated").setTooltip("gui.door.stay_animated.tooltip"));
        settings.add(new GuiCheckBox("rightClick", rightClick).setTranslate("gui.rightclick").setTooltip("gui.door.rightclick.tooltip"));
        settings.add(new GuiCheckBox("noClip", noClip).setTranslate("gui.no_clip").setTooltip("gui.door.no_clip.tooltip"));
        settings.add(new GuiCheckBox("playPlaceSounds", playPlaceSounds).setTranslate("gui.door.play_place_sound").setTooltip("gui.door.play_place_sound.tooltip"));
        
        add(new GuiIsoAnimationPanel(item, box, grid, even).setVisibleAxis(hasAxis()).setViewerDim(200, 200));
        
        createSpecific(structure instanceof LittleDoor ? (LittleDoor) structure : null);
        
        GuiParent extraSettings = new GuiParent();
        add(extraSettings);
        extraSettings.spacing = 4;
        
        soundPanel = new GuiSoundEventPanel(item.recipe.animation, opening, closing, duration);
        extraSettings.add(soundPanel);
        
        childPanel = new GuiChildEventPanel(item, item.recipe.animation, opening, duration);
        extraSettings.add(childPanel);
        
        updateTimeline();
        
        registerEventChanged(x -> {
            if (x.control.is("duration")) {
                updateTimeline();
                soundPanel.durationChanged(((GuiSteppedSlider) x.control).getValue());
                childPanel.durationChanged(((GuiSteppedSlider) x.control).getValue());
            } else if (x.control.is("inter"))
                updateTimeline();
            if (x.control instanceof GuiTimeline)
                updateTimeline();
        });
    }
    
    @Override
    public LittleStructure save(LittleStructure structure) {
        LittleDoor door = (LittleDoor) structure;
        
        GuiIsoAnimationViewer viewer = get("viewer");
        door.center = new StructureRelative(viewer.getBox(), viewer.getGrid());
        GuiStateButtonMapped<ValueInterpolation> inter = get("inter");
        door.interpolation = inter.getSelected();
        
        door.duration = get("duration", GuiSteppedSlider.class).getValue();
        door.stayAnimated = get("stayAnimated", GuiCheckBox.class).value;
        door.rightClick = get("rightClick", GuiCheckBox.class).value;
        door.noClip = get("noClip", GuiCheckBox.class).value;
        door.playPlaceSounds = get("playPlaceSounds", GuiCheckBox.class).value;
        
        door.putState(new AnimationState("closed"));
        AnimationState state = new AnimationState("opened");
        save(state);
        door.putState(state);
        
        if (soundPanel.isSoundEmpty() && childPanel.isChildEmpty())
            return structure;
        
        AnimationTimeline opening = saveEventTimeline(door.duration, true);
        if (opening != null)
            door.putTransition("closed", "opened", "opening", opening);
        
        AnimationTimeline closing = saveEventTimeline(door.duration, false);
        if (closing != null)
            door.putTransition("opened", "closed", "closing", closing);
        
        return structure;
    }
    
    protected AnimationTimeline saveEventTimeline(int duration, boolean opening) {
        List<AnimationEventEntry> events = new ArrayList<>();
        soundPanel.collectEvents(duration, events, opening);
        childPanel.collectEvents(duration, events, opening);
        
        if (events.isEmpty())
            return null;
        
        return new AnimationTimeline(duration, events);
    }
    
    protected abstract void createSpecific(@Nullable LittleDoor door);
    
    protected abstract void save(PhysicalState state);
    
    public void updateTimeline() {
        int duration = get("duration", GuiSteppedSlider.class).getValue();
        AnimationTimeline timeline = saveEventTimeline(duration, true);
        if (timeline == null)
            timeline = new AnimationTimeline(duration);
        GuiStateButtonMapped<ValueInterpolation> inter = get("inter");
        PhysicalState end = new PhysicalState();
        save(end);
        timeline.start(new PhysicalState(), end, inter.getSelected()::create1d);
        item.recipe.animation.setTimeline(item, timeline);
    }
    
}