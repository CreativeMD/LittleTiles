package team.creative.littletiles.common.structure.type.animation;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.animation.AnimationState;
import team.creative.littletiles.common.structure.animation.AnimationTimeline;
import team.creative.littletiles.common.structure.animation.AnimationTransition;
import team.creative.littletiles.common.structure.directional.StructureDirectional;
import team.creative.littletiles.common.structure.signal.output.InternalSignalOutput;

public abstract class LittleUndirectedStateStructure extends LittleStateStructure<AnimationState> {
    
    private static final AnimationState EMPTY = new AnimationState("");
    
    @StructureDirectional
    private List<AnimationTransition> transitions = new ArrayList<>();
    
    public LittleUndirectedStateStructure(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected AnimationState createState(CompoundTag nbt) {
        return new AnimationState(nbt);
    }
    
    @Override
    protected AnimationState getEmptyState() {
        return EMPTY;
    }
    
    @Override
    protected boolean shouldStayAnimatedAfterTransitionEnd() {
        if (startTransitionIfNecessary(getOutput(0))) // Check if the state has changed already
            return true;
        return false;
    }
    
    protected AnimationTimeline getTransition(int start, int end) {
        for (AnimationTransition transition : transitions)
            if (transition.start == start && transition.end == end)
                return transition.timeline;
        return null;
    }
    
    protected boolean startTransitionIfNecessary(InternalSignalOutput output) {
        int state = output.getState().number();
        if (isChanging() && hasState(state)) {
            startTransition(currentIndex(), state, getTransition(currentIndex(), state));
            return true;
        }
        return false;
    }
    
    @Override
    public void performInternalOutputChange(InternalSignalOutput output) {
        if (output.component.is("state"))
            startTransitionIfNecessary(output);
    }
    
}
