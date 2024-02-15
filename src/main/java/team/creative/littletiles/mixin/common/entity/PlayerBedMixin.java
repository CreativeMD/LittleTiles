package team.creative.littletiles.mixin.common.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import team.creative.littletiles.common.structure.type.bed.ILittleBedPlayerExtension;
import team.creative.littletiles.common.structure.type.bed.LittleBed;

@Mixin(Player.class)
public abstract class PlayerBedMixin extends LivingEntity implements ILittleBedPlayerExtension {
    
    protected PlayerBedMixin(EntityType<? extends LivingEntity> p_20966_, Level p_20967_) {
        super(p_20966_, p_20967_);
    }
    
    @Unique
    public LittleBed bed;
    
    @Shadow
    private int sleepCounter;
    
    @Override
    public LittleBed getBed() {
        return bed;
    }
    
    @Override
    public void setBed(LittleBed bed) {
        this.bed = bed;
    }
    
    @Override
    public Direction getBedOrientation() {
        LittleBed bed = getBed();
        if (bed != null)
            return bed.getBedDirection();
        return super.getBedOrientation();
    }
    
    @Override
    @Unique
    public void setSleepingCounter(int counter) {
        this.sleepCounter = counter;
    }
    
}
