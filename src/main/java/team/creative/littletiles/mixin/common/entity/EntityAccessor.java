package team.creative.littletiles.mixin.common.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityInLevelCallback;

@Mixin(Entity.class)
public interface EntityAccessor {
    
    @Accessor
    public EntityInLevelCallback getLevelCallback();
    
    @Invoker
    public void callUnsetRemoved();
    
}
