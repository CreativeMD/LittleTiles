package team.creative.littletiles.common.structure.registry.gui;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.controls.parent.GuiLabeledControl;
import team.creative.creativecore.common.gui.controls.parent.GuiTabs;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButtonMapped;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.controls.timeline.GuiTimeline;
import team.creative.creativecore.common.gui.controls.timeline.GuiTimelineChannel;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.controls.GuiDistanceControl;
import team.creative.littletiles.common.gui.controls.animation.GuiIsoAnimationPanel;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.animation.curve.ValueInterpolation;
import team.creative.littletiles.common.structure.type.animation.LittleDoor;

@OnlyIn(Dist.CLIENT)
public class LittleDoorAdvancedGui extends LittleStructureGuiControl {
    
    public LittleDoorAdvancedGui(LittleStructureGui gui, GuiTreeItemStructure item) {
        super(gui, item);
    }
    
    @Override
    public void create(LittleStructure structure) {
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
        
        GuiTabs tabs = new GuiTabs("tabs");
        
        add(tabs.setExpandableX());
        
        GuiParent single = tabs.createTab(Component.translatable("gui.door.same_transition"));
        single.flow = GuiFlow.STACK_Y;
        GuiTimeline time = new GuiTimeline();
        single.add(time.setExpandableX());
        
        time.setDim(-1, 100);
        time.addGuiTimelineChannel(Component.literal("rotX"), new GuiAdvancedTimelineChannel(time, false));
        time.addGuiTimelineChannel(Component.literal("rotY"), new GuiAdvancedTimelineChannel(time, false));
        time.addGuiTimelineChannel(Component.literal("rotZ"), new GuiAdvancedTimelineChannel(time, false));
        time.addGuiTimelineChannel(Component.literal("offX"), new GuiAdvancedTimelineChannel(time, true));
        time.addGuiTimelineChannel(Component.literal("offY"), new GuiAdvancedTimelineChannel(time, true));
        time.addGuiTimelineChannel(Component.literal("offZ"), new GuiAdvancedTimelineChannel(time, true));
        
        GuiParent editKey = new GuiParent(GuiFlow.FIT_X);
        single.add(editKey.setExpandableX());
        single.registerEvent(GuiTimeline.KeySelectedEvent.class, x -> {
            editKey.clear();
            if (x.control.channel instanceof GuiAdvancedTimelineChannel c)
                if (c.distance)
                    editKey.add(new GuiDistanceControl("distance", LittleGrid.min(), 0));
                else
                    editKey.add(new GuiTextfield("value").setFloatOnly());
            reflow();
        });
        single.registerEvent(GuiTimeline.NoKeySelectedEvent.class, x -> editKey.clear());
        
        GuiParent closing = tabs.createTab(Component.translatable("gui.door.different_transition"));
        tabs.getTabButton(1).setEnabled(false);
        
        tabs.select(0);
    }
    
    @Override
    public LittleStructure save(LittleStructure structure) {
        return structure;
    }
    
    public static class GuiAdvancedTimelineChannel extends GuiTimelineChannel {
        
        public final boolean distance;
        
        public GuiAdvancedTimelineChannel(GuiTimeline timeline, boolean distance) {
            super(timeline);
            this.distance = distance;
        }
        
    }
    
}