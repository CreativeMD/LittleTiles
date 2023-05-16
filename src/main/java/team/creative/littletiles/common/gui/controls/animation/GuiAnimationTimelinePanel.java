package team.creative.littletiles.common.gui.controls.animation;

import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.parent.GuiLabeledControl;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.controls.timeline.GuiTimeline;
import team.creative.creativecore.common.gui.controls.timeline.GuiTimelineKey;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.math.vec.Vec1d;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.controls.GuiDistanceControl;
import team.creative.littletiles.common.gui.tool.recipe.GuiRecipeAnimationHandler;
import team.creative.littletiles.common.structure.animation.AnimationTimeline;
import team.creative.littletiles.common.structure.animation.PhysicalPart;
import team.creative.littletiles.common.structure.animation.curve.ValueCurve;
import team.creative.littletiles.common.structure.animation.curve.ValueCurveInterpolation;
import team.creative.littletiles.common.structure.animation.curve.ValueInterpolation;
import team.creative.littletiles.common.structure.registry.gui.LittleDoorAdvancedGui.GuiAdvancedTimelineChannel;

public class GuiAnimationTimelinePanel extends GuiParent {
    
    public final GuiTimeline time;
    public final GuiRecipeAnimationHandler handler;
    private GuiAdvancedTimelineChannel rotX;
    private GuiAdvancedTimelineChannel rotY;
    private GuiAdvancedTimelineChannel rotZ;
    private GuiAdvancedTimelineChannel offX;
    private GuiAdvancedTimelineChannel offY;
    private GuiAdvancedTimelineChannel offZ;
    
    public GuiTimelineKey edited;
    
    public GuiAnimationTimelinePanel(GuiRecipeAnimationHandler handler, AnimationTimeline timeline) {
        flow = GuiFlow.STACK_Y;
        this.handler = handler;
        add(new GuiLabeledControl(Component.translatable("gui.duration").append(":"), new GuiTextfield("duration", "" + timeline.duration).setNumbersOnly()));
        time = new GuiTimeline(handler);
        time.setDuration(timeline.duration);
        add(time.setExpandableX());
        
        time.setDim(-1, 100);
        
        for (PhysicalPart part : PhysicalPart.values()) {
            GuiAdvancedTimelineChannel channel = new GuiAdvancedTimelineChannel(time, part.offset);
            if (timeline.get(part) instanceof ValueCurveInterpolation<Vec1d> curve) {
                for (Pair<Integer, Vec1d> pair : curve)
                    channel.addKey(pair.key, pair.value.x);
            }
            time.addGuiTimelineChannel(part.title(), channel);
            set(part, channel);
        }
        
        GuiParent editKey = new GuiParent(GuiFlow.FIT_X);
        add(editKey.setExpandableX());
        registerEvent(GuiTimeline.KeySelectedEvent.class, x -> {
            editKey.clear();
            if (x.control.channel instanceof GuiAdvancedTimelineChannel c)
                if (c.distance) {
                    GuiDistanceControl distance = new GuiDistanceControl("distance", LittleGrid.min(), 0);
                    distance.setVanillaDistance(x.control.value);
                    editKey.add(distance);
                } else
                    editKey.add(new GuiTextfield("value", "" + x.control.value).setFloatOnly());
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
        });
        registerEventChanged(x -> {
            if (x.control instanceof GuiTextfield text && text.is("duration"))
                time.setDuration(Math.max(1, text.parseInteger()));
        });
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
    
    protected ValueCurve<Vec1d> parse(Iterable<GuiTimelineKey> keys, ValueInterpolation interpolation, int duration) {
        ValueCurveInterpolation<Vec1d> curve = interpolation.create1d();
        for (GuiTimelineKey key : keys)
            if (key.tick != 0 && key.tick < duration)
                curve.add(key.tick, new Vec1d(key.value));
        return curve;
    }
    
    public AnimationTimeline generateTimeline(ValueInterpolation interpolation) {
        GuiTextfield durationT = get("duration");
        int duration = durationT.parseInteger();
        
        AnimationTimeline timeline = new AnimationTimeline(duration);
        for (PhysicalPart part : PhysicalPart.values()) {
            GuiAdvancedTimelineChannel channel = get(part);
            timeline.set(part, channel.isChannelEmpty() ? ValueCurve.ONE_EMPTY : parse(channel.keys(), interpolation, duration));
        }
        return timeline;
    }
    
}
