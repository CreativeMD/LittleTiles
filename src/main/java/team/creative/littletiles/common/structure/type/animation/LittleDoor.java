package team.creative.littletiles.common.structure.type.animation;

import net.minecraft.nbt.CompoundTag;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.animation.AnimationState;
import team.creative.littletiles.common.structure.animation.AnimationTimeline;
import team.creative.littletiles.common.structure.animation.curve.ValueInterpolation;

public abstract class LittleDoor extends LittleUndirectedStateStructure {
    
    public int duration;
    public ValueInterpolation interpolation;
    
    public boolean activateParent = false;
    public boolean rightClick = true;
    public boolean noClip = false;
    public boolean playPlaceSounds = true;
    
    public LittleDoor(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected AnimationTimeline generateTimeline(AnimationState start, AnimationState end) {
        return AnimationTimeline.generate(start, end, interpolation::create1d, null, duration, false, false);
    }
    
    @Override
    public boolean canRightClick() {
        return super.canRightClick() && rightClick;
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {
        super.loadExtra(nbt);
        duration = nbt.getInt("du");
        interpolation = ValueInterpolation.values()[nbt.getInt("in")];
        
        activateParent = nbt.getBoolean("actP");
        rightClick = nbt.getBoolean("hand");
        playPlaceSounds = nbt.getBoolean("sound");
        noClip = nbt.getBoolean("noClip");
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
        super.saveExtra(nbt);
        
        nbt.putInt("du", duration);
        nbt.putInt("in", interpolation.ordinal());
        
        if (activateParent)
            nbt.putBoolean("actP", activateParent);
        else
            nbt.remove("actP");
        
        if (rightClick)
            nbt.putBoolean("hand", rightClick);
        else
            nbt.remove("hand");
        
        if (noClip)
            nbt.putBoolean("noClip", noClip);
        else
            nbt.remove("noClip");
        
        if (playPlaceSounds)
            nbt.putBoolean("sound", playPlaceSounds);
        else
            nbt.remove("sound");
    }
    
}
