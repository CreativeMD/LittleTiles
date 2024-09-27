package team.creative.littletiles.mixin.common.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.type.bed.ILittleBedPlayerExtension;
import team.creative.littletiles.common.structure.type.bed.LittleBed;

@Mixin(Player.class)
public abstract class PlayerBedMixin extends LivingEntity implements ILittleBedPlayerExtension {
    
    protected PlayerBedMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
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
    
    @Override
    public boolean setPositionToBed() {
        if (bed != null)
            try {
                Vec3d vec = bed.getHighestCenterVec();
                Vec3d offset = new Vec3d();
                offset.set(bed.direction.axis, bed.direction.offset() * 0.5);
                vec.add(offset);
                this.setPos(vec.x, vec.y, vec.z);
                return true;
            } catch (CorruptedConnectionException | NotYetConnectedException e) {
                LittleTiles.LOGGER.error("Could not sleep in bed", e);
            }
        return false;
    }
}
