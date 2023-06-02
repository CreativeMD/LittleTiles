package team.creative.littletiles.common.structure.type.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.client.sound.EntitySound;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.util.math.vec.Vec1d;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.vec.LittleVecAbsolute;
import team.creative.littletiles.common.packet.structure.StructureStartAnimationPacket;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.animation.AnimationState;
import team.creative.littletiles.common.structure.animation.AnimationTimeline;
import team.creative.littletiles.common.structure.animation.AnimationTransition;
import team.creative.littletiles.common.structure.animation.PhysicalState;
import team.creative.littletiles.common.structure.animation.context.AnimationContext;
import team.creative.littletiles.common.structure.animation.curve.ValueCurve;
import team.creative.littletiles.common.structure.attribute.LittleAttributeBuilder;
import team.creative.littletiles.common.structure.directional.StructureDirectional;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.relative.StructureAbsolute;
import team.creative.littletiles.common.structure.relative.StructureRelative;
import team.creative.littletiles.common.structure.signal.logic.SignalMode;

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
    
    public LittleStateStructure(LittleStateStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    protected abstract AnimationTimeline generateTimeline(T start, T end);
    
    protected abstract ValueCurve<Vec1d> createEmptyCurve();
    
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
            LittleStateStructure structure = this;
            if (!states.get(end).isAligned() && !isAnimated())
                structure = (LittleStateStructure) changeToEntityForm().getStructure();
            
            structure.aimedState = end;
            structure.timeline = timeline;
            structure.timeline.start(states.get(start), states.get(end), this::createEmptyCurve);
            structure.physical = new PhysicalState();
            
            LittleTiles.NETWORK.sendToClient(new StructureStartAnimationPacket(structure.getStructureLocation(), structure.timeline), structure.getLevel(), structure.getPos());
            
            structure.queueForNextTick();
            return true;
        } catch (LittleActionException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean queuedTick() {
        if (timeline == null || mainBlock.isRemoved())
            return false;
        
        LittleAnimationEntity animation = getAnimationEntity();
        boolean done = timeline.tick(physical, this);
        if (animation != null)
            animation.physic.set(physical);
        
        if (isClient()) {
            if (done) {
                timeline = null;
                aimedState = -1;
                physical = null;
            }
            return timeline != null;
        }
        
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
    public StructureAbsolute createAnimationCenter(BlockPos pos, LittleGrid grid) {
        return new StructureAbsolute(new LittleVecAbsolute(pos, grid), center);
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
    
    @OnlyIn(Dist.CLIENT)
    public void setClientTimeline(AnimationTimeline timeline) {
        this.timeline = timeline;
        this.physical = new PhysicalState();
        queueForNextTick();
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {
        ListTag stateList = nbt.getList("s", Tag.TAG_COMPOUND);
        List<T> states = new ArrayList<>(stateList.size());
        for (int i = 0; i < stateList.size(); i++)
            states.add(createState(stateList.getCompound(i)));
        this.states = new ObjectImmutableList<>(states);
        
        currentState = nbt.getInt("cS");
        //if (currentState < 0 || currentState >= states.size()) 
        //throw new RuntimeException("Invalid state structure! State " + currentState + " not found. Only got " + states.size() + " states");
        
        aimedState = nbt.getInt("aS");
        if (aimedState >= states.size())
            throw new RuntimeException("Invalid state structure! Aimed State " + aimedState + " not found. Only got " + states.size() + " states");
        
        if (nbt.contains("timeline"))
            timeline = new AnimationTimeline(nbt.getCompound("timeline"));
        else
            timeline = null;
        
        if (nbt.contains("cP"))
            physical = new PhysicalState(nbt.getCompound("cP"));
        else
            physical = null;
        
        this.stayAnimated = nbt.getBoolean("stay");
        
        if (isChanging())
            queueForNextTick();
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
    
    public T getState(String name) {
        for (int i = 0; i < states.size(); i++)
            if (states.get(i).name.equals(name))
                return states.get(i);
        return null;
    }
    
    public int indexOfState(String name) {
        for (int i = 0; i < states.size(); i++)
            if (states.get(i).name.equals(name))
                return i;
        return -1;
    }
    
    public int putState(T state) {
        for (int i = 0; i < states.size(); i++)
            if (states.get(i).name.equals(state.name)) {
                states.set(i, state);
                return i;
            }
        states.add(state);
        return states.size() - 1;
    }
    
    @Override
    public boolean isClient() {
        return super.isClient();
    }
    
    @Override
    public boolean isGui() {
        return false;
    }
    
    @Override
    public AnimationContext getChild(int id) {
        try {
            LittleStructure structure = children.getChild(id).getStructure();
            if (structure instanceof AnimationContext context)
                return context;
        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        return null;
    }
    
    @Override
    public LittleStructure getChildStructure(int id) {
        try {
            return children.getChild(id).getStructure();
        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        return null;
    }
    
    @Override
    public void play(SoundEvent event, float volume, float pitch) {
        if (isClient())
            playClient(event, volume, pitch);
    }
    
    @OnlyIn(Dist.CLIENT)
    private void playClient(SoundEvent event, float volume, float pitch) {
        GuiControl.playSound(new EntitySound(event, getAnimationEntity(), volume, pitch, SoundSource.BLOCKS));
    }
    
    public static class LittleStateStructureType extends LittleStructureType {
        
        public <T extends LittleStateStructure> LittleStateStructureType(String id, Class<T> structureClass, BiFunction<? extends LittleStateStructureType, IStructureParentCollection, T> factory, LittleAttributeBuilder attribute, int bandwidth, SignalMode mode) {
            super(id, structureClass, factory, attribute);
            addOutput("state", bandwidth, mode);
        }
        
    }
    
}
