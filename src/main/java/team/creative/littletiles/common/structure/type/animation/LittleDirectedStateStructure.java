package team.creative.littletiles.common.structure.type.animation;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.structure.animation.AnimationStateDirected;
import team.creative.littletiles.common.structure.animation.AnimationTimeline;
import team.creative.littletiles.common.structure.animation.AnimationTransition;
import team.creative.littletiles.common.structure.directional.StructureDirectional;
import team.creative.littletiles.common.structure.signal.SignalState;

public class LittleDirectedStateStructure extends LittleStateStructure<AnimationStateDirected> {
    
    private static final AnimationStateDirected EMPTY = new AnimationStateDirected("");
    
    @StructureDirectional
    private List<AnimationTransition> transitions = new ArrayList<>();
    private int currentTransition = -1;
    
    public LittleDirectedStateStructure(LittleStateStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected AnimationStateDirected createState(CompoundTag nbt) {
        return new AnimationStateDirected(nbt);
    }
    
    @Override
    protected AnimationStateDirected getEmptyState() {
        return EMPTY;
    }
    
    @Override
    protected AnimationTimeline generateTimeline(AnimationStateDirected start, AnimationStateDirected end) {
        return null;
    }
    
    public void startTransition(int index) {
        if (isChanging())
            return;
        
        if (index < 0 || index >= transitions.size()) {
            LittleTiles.LOGGER.error("Tried to trigger transition {}, but there are only ", index, transitions.size());
            return;
        }
        
        AnimationTransition transition = transitions.get(index);
        if (startTransition(transition))
            currentTransition = index;
        startTransition(transition.start, transition.end, transition.timeline);
    }
    
    @Override
    protected boolean shouldStayAnimatedAfterTransitionEnd() {
        getOutput(0).updateState(SignalState.of(currentIndex()));
        currentTransition = -1;
        
        int transition = current().signalChanged(this);
        if (transition >= 0 && transition < transitions.size()) {
            startTransition(transition);
            return true;
        }
        return false;
    }
    
    @Nullable
    public AnimationTransition transition() {
        if (isChanging())
            return transitions.get(currentTransition);
        return null;
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {
        super.loadExtra(nbt);
        
        ListTag transList = nbt.getList("t", Tag.TAG_COMPOUND);
        AnimationTransition[] transitions = new AnimationTransition[transList.size()];
        for (int i = 0; i < transList.size(); i++)
            transitions[i] = new AnimationTransition(transList.getCompound(i));
        this.transitions = new ObjectImmutableList<>(transitions);
        
        currentTransition = nbt.getInt("cT");
        if (currentTransition > 0 && currentTransition >= transitions.length)
            throw new RuntimeException("Invalid state structure! Transition " + currentTransition + " not found. Only got " + transitions.length + " transitions");
    }
    
    @Override
    public boolean canRightClick() {
        return super.canRightClick() && current().hasRightClickTransition();
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
        super.saveExtra(nbt);
        
        nbt.putInt("cT", currentTransition);
        
        ListTag transList = new ListTag();
        for (int i = 0; i < transitions.size(); i++)
            transList.add(transitions.get(i).save());
        nbt.put("t", transList);
    }
    
    @Override
    protected void processSignalChangesInternal() {
        if (!isChanging()) {
            int transition = current().signalChanged(this);
            if (transition >= 0 && transition < transitions.size())
                startTransition(transition);
        }
    }
    
}
