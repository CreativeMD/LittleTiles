package team.creative.littletiles.mixin.common.level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.LevelEntityGetter;

@Mixin(Level.class)
public interface LevelAccessor {
    
    @Invoker
    public LevelEntityGetter<Entity> callGetEntities();
}
