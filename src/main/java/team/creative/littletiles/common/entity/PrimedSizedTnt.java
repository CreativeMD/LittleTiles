package team.creative.littletiles.common.entity;

import java.lang.reflect.Field;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.vec.LittleVec;

public class PrimedSizedTnt extends PrimedTnt {
    
    private static final Field ownerField = ObfuscationReflectionHelper.findField(PrimedTnt.class, "f_32072_");
    private static final EntityDataAccessor<String> TNTSIZE = SynchedEntityData.defineId(PrimedSizedTnt.class, EntityDataSerializers.STRING);
    
    public LittleGrid grid;
    public LittleVec size;
    
    public PrimedSizedTnt(EntityType<? extends PrimedSizedTnt> type, Level level) {
        super(type, level);
    }
    
    public PrimedSizedTnt(Level level, double x, double y, double z, @Nullable LivingEntity igniter, LittleGrid grid, LittleVec size) {
        super(LittleTiles.SIZED_TNT_TYPE, level);
        this.setPos(x, y, z);
        double d0 = level.random.nextDouble() * ((float) Math.PI * 2F);
        this.setDeltaMovement(-Math.sin(d0) * 0.02D, 0.2F, -Math.cos(d0) * 0.02D);
        this.setFuse(80);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        try {
            ownerField.set(this, igniter);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        setSize(grid, size);
        refreshDimensions();
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TNTSIZE, "1.1.1.1");
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        size.save("size", tag);
        grid.set(tag);
        super.addAdditionalSaveData(tag);
    }
    
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        size = new LittleVec("size", tag);
        grid = LittleGrid.get(tag);
        super.readAdditionalSaveData(tag);
    }
    
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (TNTSIZE.equals(key)) {
            String data = this.entityData.get(TNTSIZE);
            String[] coords = data.split("\\.");
            this.size = new LittleVec(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
            this.grid = LittleGrid.get(Integer.parseInt(coords[3]));
            
        }
        super.onSyncedDataUpdated(key);
    }
    
    public void setSize(LittleGrid grid, LittleVec size) {
        this.entityData.set(TNTSIZE, size.x + "." + size.y + "." + size.z + "." + grid.count);
        this.size = size;
        this.grid = grid;
    }
    
    @Override
    protected void explode() {
        this.level.explode(this, this.getX(), this.getY(0.0625D), this.getZ(), (float) (4.0D * size.getPercentVolume(grid)), Explosion.BlockInteraction.BREAK);
    }
    
}
