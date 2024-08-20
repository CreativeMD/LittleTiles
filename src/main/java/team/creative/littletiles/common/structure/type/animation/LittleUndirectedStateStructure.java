package team.creative.littletiles.common.structure.type.animation;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.structure.animation.AnimationState;
import team.creative.littletiles.common.structure.animation.AnimationTimeline;
import team.creative.littletiles.common.structure.animation.AnimationTransition;
import team.creative.littletiles.common.structure.directional.StructureDirectional;
import team.creative.littletiles.common.structure.signal.output.InternalSignalOutput;

public abstract class LittleUndirectedStateStructure extends LittleStateStructure<AnimationState> {
    
    private static final AnimationState EMPTY = new AnimationState("");
    
    @StructureDirectional
    private List<AnimationTransition> transitions = new ArrayList<>();
    
    public LittleUndirectedStateStructure(LittleStateStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt, HolderLookup.Provider provider) {
        super.loadExtra(nbt, provider);
        ListTag transitionList = nbt.getList("t", Tag.TAG_COMPOUND);
        transitions = new ArrayList<>(transitionList.size());
        for (int i = 0; i < transitionList.size(); i++)
            transitions.add(new AnimationTransition(transitionList.getCompound(i)));
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt, HolderLookup.Provider provider) {
        super.saveExtra(nbt, provider);
        ListTag transitionList = new ListTag();
        for (int i = 0; i < transitions.size(); i++)
            transitionList.add(transitions.get(i).save());
        nbt.put("t", transitionList);
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
    
    public void putTransition(AnimationState start, AnimationState end, String name, AnimationTimeline timeline) {
        putTransition(start.name, end.name, name, timeline);
    }
    
    public void putTransition(String start, String end, String name, AnimationTimeline timeline) {
        putTransition(indexOfState(start), indexOfState(end), name, timeline);
    }
    
    public void putTransition(int start, int end, String name, AnimationTimeline timeline) {
        if (!hasState(start) || !hasState(end))
            return;
        transitions.add(new AnimationTransition(name, start, end, timeline));
    }
    
    public AnimationTimeline getTransition(String start, String end) {
        return getTransition(indexOfState(start), indexOfState(end));
    }
    
    public AnimationTimeline getTransition(String name) {
        for (AnimationTransition transition : transitions)
            if (transition.name.equals(name))
                return transition.timeline;
        return null;
    }
    
    public AnimationTimeline getTransition(int start, int end) {
        for (AnimationTransition transition : transitions)
            if (transition.start == start && transition.end == end)
                return transition.timeline;
        return null;
    }
    
    protected boolean startTransitionIfNecessary(InternalSignalOutput output) {
        return startTransitionIfNecessary(output.getState().number());
    }
    
    protected boolean startTransitionIfNecessary(int aimed) {
        if (!isChanging() && hasState(aimed) && aimed != currentIndex()) {
            startTransition(currentIndex(), aimed, getTransition(currentIndex(), aimed));
            return true;
        }
        return false;
    }
    
    @Override
    public void performInternalOutputChange(InternalSignalOutput output) {
        if (output.component.is("state"))
            startTransitionIfNecessary(output);
    }
    
    @Override
    public boolean canInteract() {
        return canRightClick();
    }
    
    @Override
    public InteractionResult use(Level level, LittleTileContext context, BlockPos pos, Player player, BlockHitResult result) {
        if (canRightClick()) {
            if (!isClient())
                getOutput(0).toggle();
            return InteractionResult.SUCCESS;
        }
        return super.use(level, context, pos, player, result);
    }
    
}
