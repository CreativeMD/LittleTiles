package team.creative.littletiles.common.structure.type.animation;

import java.util.function.BiFunction;

import net.minecraft.nbt.CompoundTag;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.placement.box.LittlePlaceBoxRelative;
import team.creative.littletiles.common.structure.animation.AnimationState;
import team.creative.littletiles.common.structure.animation.AnimationTimeline;
import team.creative.littletiles.common.structure.animation.curve.ValueInterpolation;
import team.creative.littletiles.common.structure.attribute.LittleAttributeBuilder;
import team.creative.littletiles.common.structure.directional.StructureDirectionalField;
import team.creative.littletiles.common.structure.signal.logic.SignalMode;

public abstract class LittleDoor extends LittleUndirectedStateStructure {
    
    public int duration;
    public ValueInterpolation interpolation;
    
    public boolean activateParent = false;
    public boolean rightClick = true;
    public boolean noClip = false;
    public boolean playPlaceSounds = true;
    
    public LittleDoor(LittleStateStructureType type, IStructureParentCollection mainBlock) {
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
    
    public static class LittleDoorType extends LittleStateStructureType {
        
        public <T extends LittleDoor> LittleDoorType(String id, Class<T> structureClass, BiFunction<? extends LittleStateStructureType, IStructureParentCollection, T> factory, LittleAttributeBuilder attribute) {
            super(id, structureClass, factory, attribute, 1, SignalMode.TOGGLE);
        }
        
    }
    
    public static class LittleDoorTypeInvisibleCenter extends LittleDoorType {
        
        public <T extends LittleDoor> LittleDoorTypeInvisibleCenter(String id, Class<T> structureClass, BiFunction<? extends LittleStateStructureType, IStructureParentCollection, T> factory, LittleAttributeBuilder attribute) {
            super(id, structureClass, factory, attribute);
        }
        
        @Override
        protected LittlePlaceBoxRelative getPlaceBox(Object value, StructureDirectionalField type, LittleGroup previews) {
            if (type.key.equals("center"))
                return null;
            return super.getPlaceBox(value, type, previews);
        }
        
    }
    
}
