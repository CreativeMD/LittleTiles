package team.creative.littletiles.common.structure.registry.gui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.animation.event.AnimationEvent;
import team.creative.littletiles.common.animation.event.ChildActivateEvent;
import team.creative.littletiles.common.animation.timeline.AnimationTimeline;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.type.door.LittleDoor;
import team.creative.littletiles.common.structure.type.door.LittleDoorActivator;

public class LittleDoorActivatorParser extends LittleStructureGuiControl {
    
    public LittleDoorActivatorParser(GuiParent parent, AnimationGuiHandler handler) {
        super(parent, handler);
    }
    
    public String getDisplayName(LittlePreviews previews, int childId) {
        String name = previews.getStructureName();
        if (name == null)
            if (previews.hasStructure())
                name = previews.getStructureId();
            else
                name = "none";
        return name + " " + childId;
    }
    
    public List<Integer> possibleChildren;
    
    @Override
    public void createControls(LittlePreviews previews, LittleStructure structure) {
        parent.controls.add(new GuiCheckBox("rightclick", CoreControl
                .translate("gui.door.rightclick"), 50, 123, structure instanceof LittleDoor ? !((LittleDoor) structure).disableRightClick : true));
        
        GuiScrollBox box = new GuiScrollBox("content", 0, 0, 100, 115);
        parent.controls.add(box);
        LittleDoorActivator activator = structure instanceof LittleDoorActivator ? (LittleDoorActivator) structure : null;
        possibleChildren = new ArrayList<>();
        int i = 0;
        int added = 0;
        for (LittlePreviews child : previews.getChildren()) {
            Class clazz = LittleStructureRegistry.getStructureClass(child.getStructureId());
            if (clazz != null && LittleDoor.class.isAssignableFrom(clazz)) {
                box.addControl(new GuiCheckBox("" + i, getDisplayName(child, i), 0, added * 20, activator != null && ArrayUtils.contains(activator.toActivate, i)));
                possibleChildren.add(i);
                added++;
            }
            i++;
        }
        
        updateTimeline();
    }
    
    @CustomEventSubscribe
    public void onChanged(GuiControlChangedEvent event) {
        if (event.source instanceof GuiCheckBox)
            updateTimeline();
    }
    
    public void updateTimeline() {
        AnimationTimeline timeline = new AnimationTimeline(0, new PairList<>());
        List<AnimationEvent> events = new ArrayList<>();
        for (Integer integer : possibleChildren) {
            GuiCheckBox box = (GuiCheckBox) parent.get("" + integer);
            if (box != null && box.value)
                events.add(new ChildActivateEvent(0, integer));
        }
        handler.setTimeline(timeline, events);
    }
    
    @Override
    public LittleStructure parseStructure(LittlePreviews previews) {
        LittleDoorActivator activator = createStructure(LittleDoorActivator.class, null);
        
        GuiCheckBox rightclick = (GuiCheckBox) parent.get("rightclick");
        activator.disableRightClick = !rightclick.value;
        
        GuiScrollBox box = (GuiScrollBox) parent.get("content");
        List<Integer> toActivate = new ArrayList<>();
        for (Integer integer : possibleChildren) {
            GuiCheckBox checkBox = (GuiCheckBox) box.get("" + integer);
            if (checkBox != null && checkBox.value)
                toActivate.add(integer);
        }
        activator.toActivate = new int[toActivate.size()];
        for (int i = 0; i < activator.toActivate.length; i++)
            activator.toActivate[i] = toActivate.get(i);
        
        return activator;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    protected LittleStructureType getStructureType() {
        return LittleStructureRegistry.getStructureType(LittleDoorActivator.class);
    }
    
}