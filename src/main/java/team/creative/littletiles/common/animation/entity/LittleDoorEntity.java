package team.creative.littletiles.common.animation.entity;

import com.mojang.math.Vector3d;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.level.CreativeLevel;
import team.creative.littletiles.common.animation.AnimationState;
import team.creative.littletiles.common.animation.EntityAnimationController;
import team.creative.littletiles.common.math.location.LocalStructureLocation;
import team.creative.littletiles.common.structure.relative.StructureAbsolute;

public class LittleDoorEntity extends LittleLevelEntity {
    
    public EntityAnimationController controller;
    
    public LittleDoorEntity(EntityType<?> type, Level level) {
        super(type, level);
    }
    
    public LittleDoorEntity(EntityType<?> type, Level level, CreativeLevel fakeLevel, StructureAbsolute center, LocalStructureLocation location) {
        super(type, level, fakeLevel, center, location);
    }
    
    @Override
    public void initialTick() {
        if (controller == null)
            return;
        AnimationState state = controller.getTickingState();
        Vector3d offset = state.getOffset();
        Vector3d rotation = state.getRotation();
        moveAndRotateAnimation(offset.x - origin.offX(), offset.y - origin.offY(), offset.z - origin.offZ(), rotation.x - origin.rotX(), rotation.y - origin
                .rotY(), rotation.z - origin.rotZ());
        origin.tick();
        hasOriginChanged = true;
    }
    
    @Override
    public void onTick() {
        if (controller == null)
            return;
        AnimationState state = controller.tick();
        Vector3d offset = state.getOffset();
        Vector3d rotation = state.getRotation();
        moveAndRotateAnimation(offset.x - origin.offX(), offset.y - origin.offY(), offset.z - origin.offZ(), rotation.x - origin.rotX(), rotation.y - origin
                .rotY(), rotation.z - origin.rotZ());
    }
    
    @Override
    public void loadLevelEntity(CompoundTag nbt) {
        controller = EntityAnimationController.parseController(this, nbt.getCompound("controller"));
    }
    
    @Override
    public void saveLevelEntity(CompoundTag nbt) {
        nbt.setTag("controller", controller.writeToNBT(new NBTTagCompound()));
    }
    
}
