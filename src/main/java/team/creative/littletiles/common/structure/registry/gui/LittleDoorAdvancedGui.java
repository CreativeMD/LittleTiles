package team.creative.littletiles.common.structure.registry.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.controls.parent.GuiLabeledControl;
import team.creative.creativecore.common.gui.controls.parent.GuiTabs;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButtonMapped;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.controls.timeline.GuiTimeline;
import team.creative.creativecore.common.gui.controls.timeline.GuiTimelineChannelDouble;
import team.creative.creativecore.common.gui.controls.timeline.GuiTimelineKey;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.math.vec.Vec1d;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.controls.GuiGridConfig;
import team.creative.littletiles.common.gui.controls.GuiPhysicalStateControl;
import team.creative.littletiles.common.gui.controls.animation.GuiAnimationTimelinePanel;
import team.creative.littletiles.common.gui.controls.animation.GuiIsoAnimationPanel;
import team.creative.littletiles.common.gui.controls.animation.GuiIsoAnimationViewer;
import team.creative.littletiles.common.gui.controls.animation.GuiIsoAnimationViewer.GuiAnimationAxisChangedEvent;
import team.creative.littletiles.common.gui.tool.recipe.GuiRecipeAnimationHandler;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.animation.AnimationState;
import team.creative.littletiles.common.structure.animation.AnimationTimeline;
import team.creative.littletiles.common.structure.animation.AnimationTimeline.AnimationEventEntry;
import team.creative.littletiles.common.structure.animation.PhysicalPart;
import team.creative.littletiles.common.structure.animation.PhysicalState;
import team.creative.littletiles.common.structure.animation.curve.ValueCurveInterpolation;
import team.creative.littletiles.common.structure.animation.curve.ValueInterpolation;
import team.creative.littletiles.common.structure.relative.StructureAbsolute;
import team.creative.littletiles.common.structure.relative.StructureRelative;
import team.creative.littletiles.common.structure.type.animation.LittleAdvancedDoor;

@OnlyIn(Dist.CLIENT)
public class LittleDoorAdvancedGui extends LittleStructureGuiControl {
    
    public GuiTimelineConfig same;
    public GuiTimelineConfig different;
    
    public LittleDoorAdvancedGui(LittleStructureGui gui, GuiTreeItemStructure item) {
        super(gui, item);
    }
    
    @Override
    public void create(LittleStructure structure) {
        LittleGrid grid;
        LittleBox box;
        boolean even;
        ValueInterpolation inter;
        boolean stayAnimated;
        boolean rightClick;
        boolean noClip;
        boolean playPlaceSounds;
        boolean sameTransition;
        int duration;
        PhysicalState closed = new PhysicalState();
        PhysicalState opened = new PhysicalState();
        AnimationTimeline opening;
        AnimationTimeline closing;
        if (structure instanceof LittleAdvancedDoor door) {
            grid = door.center.getGrid();
            box = door.center.getBox();
            even = door.center.isEven();
            inter = door.interpolation;
            stayAnimated = door.stayAnimated;
            rightClick = door.rightClick;
            noClip = door.noClip;
            playPlaceSounds = door.playPlaceSounds;
            sameTransition = !door.differentTransition;
            duration = Math.max(1, door.duration);
            closed.set(door.getState("closed"));
            opened.set(door.getState("opened"));
            opening = door.getTransition("opening");
            if (opening == null)
                opening = new AnimationTimeline(duration);
            closing = door.getTransition("closing");
            if (sameTransition || closing == null) {
                closing = opening.copy();
                closing.reverse(item);
            }
        } else {
            grid = item.group.getGrid();
            box = new LittleBox(item.group.getMinVec());
            even = false;
            inter = ValueInterpolation.HERMITE;
            stayAnimated = false;
            rightClick = true;
            noClip = false;
            playPlaceSounds = true;
            sameTransition = true;
            duration = 10;
            opening = new AnimationTimeline(duration);
            closing = new AnimationTimeline(duration);
            
        }
        
        flow = GuiFlow.STACK_Y;
        align = Align.STRETCH;
        
        GuiParent upper = new GuiParent();
        add(upper);
        
        upper.add(new GuiIsoAnimationPanel(item, box, grid, even).setViewerDim(100, 100).setVisibleAxis(true));
        GuiParent settings = new GuiParent(GuiFlow.FIT_X).setVAlign(VAlign.CENTER);
        settings.spacing = 5;
        upper.add(settings);
        
        settings.add(new GuiLabeledControl(Component.translatable("gui.interpolation")
                .append(":"), new GuiStateButtonMapped<ValueInterpolation>("inter", inter, new TextMapBuilder<ValueInterpolation>()
                        .addComponent(ValueInterpolation.values(), x -> x.translate()))));
        
        settings.add(new GuiCheckBox("stayAnimated", stayAnimated).setTranslate("gui.stay_animated").setTooltip("gui.door.stay_animated.tooltip"));
        settings.add(new GuiCheckBox("rightClick", rightClick).setTranslate("gui.rightclick").setTooltip("gui.door.rightclick.tooltip"));
        settings.add(new GuiCheckBox("noClip", noClip).setTranslate("gui.no_clip").setTooltip("gui.door.no_clip.tooltip"));
        settings.add(new GuiCheckBox("playPlaceSounds", playPlaceSounds).setTranslate("gui.door.play_place_sound").setTooltip("gui.door.play_place_sound.tooltip"));
        
        settings.add(new GuiLabeledControl(Component.translatable("gui.duration").append(":"), new GuiTextfield("duration", "" + duration).setNumbersOnly()));
        
        GuiIsoAnimationViewer viewer = upper.get("viewer");
        settings.add(new GuiCheckBox("even", viewer.isEven()).setTranslate("gui.door.axis.even"));
        
        settings.add(new GuiGridConfig("grid", viewer.getGrid(), x -> {
            LittleBox viewerBox = viewer.getBox();
            viewerBox.convertTo(viewer.getGrid(), x);
            
            if (viewer.isEven())
                viewerBox.maxX = viewerBox.minX + 2;
            else
                viewerBox.maxX = viewerBox.minX + 1;
            
            if (viewer.isEven())
                viewerBox.maxY = viewerBox.minY + 2;
            else
                viewerBox.maxY = viewerBox.minY + 1;
            
            if (viewer.isEven())
                viewerBox.maxZ = viewerBox.minZ + 2;
            else
                viewerBox.maxZ = viewerBox.minZ + 1;
            
            viewer.setAxis(viewerBox, x);
        }));
        
        settings.registerEventChanged(x -> {
            if (x.control.is("even"))
                get("viewer", GuiIsoAnimationViewer.class).setEven(((GuiCheckBox) x.control).value);
            if (x.control.is("inter"))
                updateTimeline();
        });
        
        GuiTabs tabs = new GuiTabs("tabs");
        
        add(tabs.setExpandableX());
        
        tabs.createTab(Component.translatable("gui.door.same_transition"))
                .add((GuiControl) (same = new GuiTimelineConfigSame(item, item.recipe.animation, closed, opened, duration, opening)));
        
        tabs.createTab(Component.translatable("gui.door.different_transition"))
                .add((GuiControl) (this.different = new GuiTimelineConfigDifferent(item, item.recipe.animation, closed, opened, duration, opening, closing)));
        
        tabs.select(sameTransition ? 0 : 1);
        
        registerEvent(GuiAnimationAxisChangedEvent.class, x -> item.setNewCenter(new StructureAbsolute(new BlockPos(0, 0, 0), viewer.getBox().copy(), viewer.getGrid())));
        raiseEvent(new GuiAnimationAxisChangedEvent(viewer));
        
        tabs.registerEventChanged(x -> {
            if (x.control instanceof GuiTimeline || x.control instanceof GuiTabs || x.control instanceof GuiPhysicalStateControl)
                updateTimeline();
        });
        
        settings.registerEventChanged(x -> {
            if (x.control instanceof GuiTextfield text && text.is("duration")) {
                int newDuration = text.parseInteger();
                same.durationChanged(newDuration);
                different.durationChanged(newDuration);
            }
        });
        
        updateTimeline();
    }
    
    public void updateTimeline() {
        GuiTimelineConfig config = get("tabs", GuiTabs.class).index() != 0 ? different : same;
        GuiStateButtonMapped<ValueInterpolation> inter = get("inter");
        GuiTextfield durationT = get("duration");
        int duration = durationT.parseInteger();
        PhysicalState closed = config.closedState();
        PhysicalState opened = config.openedState();
        boolean opening = config.openingAnimation();
        AnimationTimeline timeline = config.generateTimeline(duration, inter.getSelected(), opening);
        if (opening)
            timeline.start(closed, opened, inter.getSelected()::create1d);
        else
            timeline.start(opened, closed, inter.getSelected()::create1d);
        item.recipe.animation.setTimeline(item, timeline);
    }
    
    @Override
    public LittleStructure save(LittleStructure structure) {
        LittleAdvancedDoor door = (LittleAdvancedDoor) structure;
        
        GuiIsoAnimationViewer viewer = get("viewer");
        door.center = new StructureRelative(viewer.getBox(), viewer.getGrid());
        GuiStateButtonMapped<ValueInterpolation> inter = get("inter");
        door.interpolation = inter.getSelected();
        
        door.differentTransition = get("tabs", GuiTabs.class).index() != 0;
        door.stayAnimated = get("stayAnimated", GuiCheckBox.class).value;
        door.rightClick = get("rightClick", GuiCheckBox.class).value;
        door.noClip = get("noClip", GuiCheckBox.class).value;
        door.playPlaceSounds = get("playPlaceSounds", GuiCheckBox.class).value;
        door.duration = get("duration", GuiTextfield.class).parseInteger();
        GuiTimelineConfig config = door.differentTransition ? different : same;
        
        door.putState(new AnimationState("closed", config.closedState(), !door.stayAnimated));
        door.putState(new AnimationState("opened", config.openedState(), !door.stayAnimated));
        door.putTransition("closed", "opened", "opening", config.generateTimeline(door.duration, door.interpolation, true));
        door.putTransition("opened", "closed", "closing", config.generateTimeline(door.duration, door.interpolation, false));
        
        return structure;
    }
    
    public static interface GuiTimelineConfig {
        
        public PhysicalState closedState();
        
        public PhysicalState openedState();
        
        public AnimationTimeline generateTimeline(int duration, ValueInterpolation interpolation, boolean opening);
        
        public boolean openingAnimation();
        
        public void durationChanged(int duration);
    }
    
    public static class GuiTimelineConfigSame extends GuiAnimationTimelinePanel implements GuiTimelineConfig {
        
        public static AnimationTimeline setup(PhysicalState closed, PhysicalState opened, int duration, AnimationTimeline original) {
            List<AnimationEventEntry> events = new ArrayList<>();
            for (AnimationEventEntry entry : original.allEvents())
                events.add(entry.copy());
            AnimationTimeline timeline = new AnimationTimeline(original.duration, events);
            for (PhysicalPart part : PhysicalPart.values()) {
                ValueCurveInterpolation<Vec1d> curve = original.get(part)
                        .isEmpty() ? new ValueCurveInterpolation.LinearCurve<>() : (ValueCurveInterpolation<Vec1d>) original.get(part).copy();
                timeline.set(part, curve);
                
                if (opened.get(part) != 0 || closed.get(part) != 0 || !curve.isEmpty()) {
                    if (curve.isEmpty() || closed.get(part) != curve.getFirst().x)
                        curve.add(0, new Vec1d(closed.get(part)));
                    if (curve.isEmpty() || opened.get(part) != curve.getLast().x)
                        curve.add(timeline.duration, new Vec1d(opened.get(part)));
                }
            }
            return timeline;
        }
        
        public GuiTimelineConfigSame(GuiTreeItemStructure item, GuiRecipeAnimationHandler handler, PhysicalState closed, PhysicalState opened, int duration, AnimationTimeline timeline) {
            super(item, handler, duration, setup(closed, opened, duration, timeline), false);
        }
        
        @Override
        public PhysicalState openedState() {
            PhysicalState state = new PhysicalState();
            for (PhysicalPart part : PhysicalPart.values()) {
                GuiAdvancedTimelineChannel channel = get(part);
                if (!channel.isChannelEmpty())
                    state.set(part, channel.getLast().value);
            }
            return state;
        }
        
        @Override
        public PhysicalState closedState() {
            PhysicalState state = new PhysicalState();
            for (PhysicalPart part : PhysicalPart.values()) {
                GuiAdvancedTimelineChannel channel = get(part);
                if (!channel.isChannelEmpty())
                    state.set(part, channel.getFirst().value);
            }
            return state;
        }
        
        @Override
        public AnimationTimeline generateTimeline(int duration, ValueInterpolation interpolation, boolean opening) {
            AnimationTimeline timeline = generateTimeline(duration, interpolation);
            if (!opening)
                timeline.reverse(handler.context());
            return timeline;
        }
        
        @Override
        public boolean openingAnimation() {
            return true;
        }
    }
    
    public static class GuiTimelineConfigDifferent extends GuiParent implements GuiTimelineConfig {
        
        public GuiPhysicalStateControl closed;
        public GuiPhysicalStateControl opened;
        
        public GuiAnimationTimelinePanel opening;
        public GuiAnimationTimelinePanel closing;
        
        public GuiTimelineConfigDifferent(GuiTreeItemStructure item, GuiRecipeAnimationHandler handler, PhysicalState closedState, PhysicalState openedState, int duration, AnimationTimeline opening, AnimationTimeline closing) {
            flow = GuiFlow.STACK_Y;
            
            GuiParent stateConfig = new GuiParent();
            add(stateConfig);
            GuiParent closedParent = new GuiParent(GuiFlow.STACK_Y).setAlign(Align.CENTER);
            stateConfig.add(closedParent);
            closedParent.add(new GuiLabel("closedLabel").setTranslate("gui.door.closed"));
            closedParent.add(closed = new GuiPhysicalStateControl("closed", closedState));
            
            GuiParent openedParent = new GuiParent(GuiFlow.STACK_Y).setAlign(Align.CENTER);
            stateConfig.add(openedParent);
            openedParent.add(new GuiLabel("openedLabel").setTranslate("gui.door.opened"));
            openedParent.add(opened = new GuiPhysicalStateControl("opened", openedState));
            
            GuiTabs tabs = new GuiTabs("timelineTabs");
            add(tabs);
            
            tabs.createTab(translatable("gui.door.opening")).add(this.opening = new GuiAnimationTimelinePanel(item, handler, duration, opening, true));
            tabs.createTab(translatable("gui.door.closing")).add(this.closing = new GuiAnimationTimelinePanel(item, handler, duration, closing, true));
            
            tabs.select(0);
        }
        
        @Override
        public PhysicalState openedState() {
            return opened.create();
        }
        
        @Override
        public PhysicalState closedState() {
            return closed.create();
        }
        
        @Override
        public AnimationTimeline generateTimeline(int duration, ValueInterpolation interpolation, boolean opening) {
            return opening ? this.opening.generateTimeline(duration, interpolation) : this.closing.generateTimeline(duration, interpolation);
        }
        
        @Override
        public boolean openingAnimation() {
            return get("timelineTabs", GuiTabs.class).index() == 0;
        }
        
        @Override
        public void durationChanged(int duration) {
            opening.durationChanged(duration);
            closing.durationChanged(duration);
        }
    }
    
    public static class GuiAdvancedTimelineChannel extends GuiTimelineChannelDouble {
        
        public final boolean distance;
        public final boolean limited;
        
        public GuiAdvancedTimelineChannel(GuiTimeline timeline, boolean distance, boolean limited) {
            super(timeline);
            this.distance = distance;
            this.limited = limited;
        }
        
        @Override
        public boolean isSpaceFor(GuiTimelineKey<Double> key, int tick) {
            if (limited && (tick == 0 || tick == timeline.getDuration()))
                return false;
            return super.isSpaceFor(key, tick);
        }
        
    }
    
}