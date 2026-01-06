package team.creative.littletiles.mixin.common.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.phys.Vec3;

@Mixin(Entity.class)
public interface EntityAccessor {
    
    @Accessor
    public void setPushedByAnimationDelta(Vec3 vec);
    
    @Accessor
    public EntityInLevelCallback getLevelCallback();
    
    @Invoker
    public void callUnsetRemoved();
    
    @Invoker
    public void callSetLevel(Level level);
    
}
