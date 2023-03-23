package team.creative.littletiles.common.structure.type.animation;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;
import team.creative.littletiles.common.math.vec.LittleVecAbsolute;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.animation.AnimationState;
import team.creative.littletiles.common.structure.animation.AnimationTimeline;
import team.creative.littletiles.common.structure.animation.AnimationTransition;
import team.creative.littletiles.common.structure.animation.PhysicalState;
import team.creative.littletiles.common.structure.animation.context.AnimationContext;
import team.creative.littletiles.common.structure.directional.StructureDirectional;
import team.creative.littletiles.common.structure.relative.StructureAbsolute;
import team.creative.littletiles.common.structure.relative.StructureRelative;

public abstract class LittleStateStructure<T extends AnimationState> extends LittleStructure implements AnimationContext {
    
    @StructureDirectional
    private List<T> states = new ArrayList<>();
    private int currentState;
    private int aimedState = -1;
    private AnimationTimeline timeline;
    private PhysicalState physical;
    
    @StructureDirectional(color = ColorUtils.RED)
    public StructureRelative center;
    
    public boolean stayAnimated = false;
    
    public LittleStateStructure(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    protected abstract AnimationTimeline generateTimeline(T start, T end);
    
    protected boolean startTransition(AnimationTransition transition) {
        return startTransition(transition.start, transition.end, transition.timeline);
    }
    
    protected boolean startTransition(int start, int end, @Nullable AnimationTimeline timeline) {
        if (isChanging())
            return false;
        
        if (start != currentState) {
            LittleTiles.LOGGER.error("Tried to trigger transition, but start does not match. Expected: {} got: {} ", start, currentState);
            return false;
        }
        
        if (end < 0 || end > states.size()) {
            LittleTiles.LOGGER.error("Tried to trigger transition, but end does not match. State {} does not exist. There are only {} states in total ", end, states.size());
            return false;
        }
        
        if (timeline == null)
            timeline = generateTimeline(states.get(start), states.get(end));
        
        if (timeline == null) {
            LittleTiles.LOGGER.error("Transition from {} to {} cannot be done, it does not exist.", start, end);
            return false;
        }
        
        try {
            if (!states.get(end).isAligned() && !isAnimated())
                changeToEntityForm();
            this.aimedState = end;
            this.timeline = timeline;
            this.timeline.start(states.get(start), states.get(end));
            this.physical = new PhysicalState();
            queueForNextTick();
            return true;
        } catch (LittleActionException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean queuedTick() {
        if (timeline == null)
            return false;
        
        LittleAnimationEntity animation = getAnimationEntity();
        boolean done = timeline.tick(physical, this);
        if (animation != null)
            animation.physic.set(physical);
        
        if (done)
            endTransition();
        
        return isChanging();
    }
    
    protected abstract boolean shouldStayAnimatedAfterTransitionEnd();
    
    protected void endTransition() {
        currentState = aimedState;
        aimedState = -1;
        timeline.end();
        timeline = null;
        physical = null;
        T state = current();
        if (!shouldStayAnimatedAfterTransitionEnd() && !stayAnimated && state.shouldGoBackToBlockform() && state.isAligned())
            try {
                changeToBlockForm();
            } catch (LittleActionException e) {
                e.printStackTrace();
            }
    }
    
    @Override
    public StructureAbsolute createAnimationCenter() {
        return new StructureAbsolute(new LittleVecAbsolute(mainBlock.getPos(), mainBlock.getGrid()), center);
    }
    
    public boolean canRightClick() {
        return !isChanging();
    }
    
    public boolean hasState(int index) {
        return index >= 0 && index < states.size();
    }
    
    public T current() {
        if (currentState >= 0 || currentState < states.size())
            return states.get(currentState);
        return getEmptyState();
    }
    
    public int currentIndex() {
        return currentState;
    }
    
    public boolean isChanging() {
        return aimedState != -1;
    }
    
    protected abstract T createState(CompoundTag nbt);
    
    protected abstract T getEmptyState();
    
    @Override
    protected void loadExtra(CompoundTag nbt) {
        ListTag stateList = nbt.getList("s", Tag.TAG_COMPOUND);
        List<T> states = new ArrayList<>(stateList.size());
        for (int i = 0; i < stateList.size(); i++)
            states.add(createState(nbt));
        this.states = new ObjectImmutableList<>(states);
        
        currentState = nbt.getInt("cS");
        //if (currentState < 0 || currentState >= states.size()) 
        //throw new RuntimeException("Invalid state structure! State " + currentState + " not found. Only got " + states.size() + " states");
        
        aimedState = nbt.getInt("aS");
        if (aimedState >= 0 || aimedState >= states.size())
            throw new RuntimeException("Invalid state structure! Aimed State " + aimedState + " not found. Only got " + states.size() + " states");
        
        if (nbt.contains("timeline"))
            timeline = AnimationTimeline.load(nbt.getCompound("timeline"));
        else
            timeline = null;
        
        if (nbt.contains("cP"))
            physical = new PhysicalState(nbt.getCompound("cP"));
        else
            physical = null;
        
        this.stayAnimated = nbt.getBoolean("stay");
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
        nbt.putInt("cS", currentState);
        nbt.putInt("aS", aimedState);
        
        if (timeline != null)
            nbt.put("timeline", timeline.save());
        
        if (physical != null)
            nbt.put("cP", physical.save());
        
        ListTag stateList = new ListTag();
        for (int i = 0; i < states.size(); i++)
            stateList.add(states.get(i).save());
        nbt.put("s", stateList);
        
        if (stayAnimated)
            nbt.putBoolean("stay", stayAnimated);
        else
            nbt.remove("stay");
    }
    
    public void putState(T state) {
        for (int i = 0; i < states.size(); i++)
            if (states.get(i).name.equals(state.name)) {
                states.set(i, state);
                return;
            }
        states.add(state);
    }
    
}
