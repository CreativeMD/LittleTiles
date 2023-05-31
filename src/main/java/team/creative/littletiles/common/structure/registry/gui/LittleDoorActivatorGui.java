package team.creative.littletiles.common.structure.registry.gui;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.littletiles.common.gui.controls.animation.GuiChildEventPanel;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.animation.AnimationState;
import team.creative.littletiles.common.structure.animation.AnimationTimeline;
import team.creative.littletiles.common.structure.animation.AnimationTimeline.AnimationEventEntry;
import team.creative.littletiles.common.structure.animation.curve.ValueInterpolation;
import team.creative.littletiles.common.structure.animation.event.ChildDoorEvent;
import team.creative.littletiles.common.structure.relative.StructureRelative;
import team.creative.littletiles.common.structure.type.animation.LittleActivatorDoor;
import team.creative.littletiles.common.structure.type.animation.LittleDoor;

@OnlyIn(Dist.CLIENT)
public class LittleDoorActivatorGui extends LittleStructureGuiControl {
    
    public LittleDoorActivatorGui(LittleStructureGui gui, GuiTreeItemStructure item) {
        super(gui, item);
        flow = GuiFlow.STACK_Y;
        registerEventChanged(x -> updateTimeline());
    }
    
    @Override
    public void create(@Nullable LittleStructure structure) {
        boolean rightClick = true;
        int[] activated = new int[0];
        
        if (structure instanceof LittleDoor door) {
            rightClick = door.rightClick;
            if (structure instanceof LittleActivatorDoor act)
                activated = act.toActivate;
            else {
                List<AnimationEventEntry> events = GuiChildEventPanel.extract(door.getTransition("opening"), ChildDoorEvent.class);
                IntList list = new IntArrayList();
                for (AnimationEventEntry entry : events)
                    list.add(((ChildDoorEvent) entry.getEvent()).childId);
                activated = list.toIntArray();
            }
        }
        
        add(new GuiCheckBox("rightClick", rightClick).setTranslate("gui.rightclick").setTooltip("gui.door.rightclick.tooltip"));
        for (int i = 0; i < item.itemsCount(); i++)
            if (item.getChildStructure(i) instanceof LittleDoor)
                add(new GuiCheckBox("c" + i, ArrayUtils.contains(activated, i)).setTitle(Component.literal(((GuiTreeItemStructure) item.getItem(i)).getTitle())));
            
        updateTimeline();
    }
    
    public void updateTimeline() {
        AnimationTimeline timeline = generateTimeline(generateActivated());
        item.recipe.animation.setTimeline(item, timeline);
    }
    
    public int[] generateActivated() {
        IntList list = new IntArrayList();
        for (GuiChildControl child : controls)
            if (child.control instanceof GuiCheckBox box && box.value && box.name.startsWith("c"))
                list.add(Integer.parseInt(box.name.replace("c", "")));
        return list.toIntArray();
    }
    
    public AnimationTimeline generateTimeline(int[] activated) {
        int duration = 1;
        List<AnimationEventEntry> entries = new ArrayList<>();
        for (int i = 0; i < activated.length; i++) {
            if (item.getChildStructure(activated[i]) instanceof LittleDoor door) {
                duration = Math.max(duration, door.duration);
                entries.add(new AnimationEventEntry(0, new ChildDoorEvent(activated[i])));
            }
        }
        return new AnimationTimeline(duration, entries);
    }
    
    @Override
    public LittleStructure save(LittleStructure structure) {
        LittleActivatorDoor door = (LittleActivatorDoor) structure;
        
        door.center = new StructureRelative(new LittleBox(item.group.getMinVec()), item.group.getGrid());
        door.rightClick = get("rightClick", GuiCheckBox.class).value;
        door.toActivate = generateActivated();
        door.interpolation = ValueInterpolation.HERMITE;
        
        door.putState(new AnimationState("closed"));
        door.putState(new AnimationState("opened"));
        
        AnimationTimeline timeline = generateTimeline(door.toActivate);
        AnimationTimeline reversed = timeline.copy();
        reversed.reverse(item);
        door.putTransition("closed", "opened", "opening", timeline);
        door.putTransition("opened", "closed", "closing", reversed);
        
        return structure;
    }
    
}