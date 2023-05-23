package team.creative.littletiles.mixin.server.level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;

@Mixin(ServerEntity.class)
public interface ServerEntityAccessor {
    
    @Accessor
    public Entity getEntity();
    
}
