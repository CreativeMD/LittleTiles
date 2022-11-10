package team.creative.littletiles.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;

@Mixin(PrimedTnt.class)
public interface PrimedTntAccessor {
    
    @Accessor
    public LivingEntity getOwner();
    
    @Accessor
    public void setOwner(LivingEntity entity);
    
}
