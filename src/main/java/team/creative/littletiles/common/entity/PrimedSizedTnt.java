package team.creative.littletiles.common.entity;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.mixin.common.entity.PrimedTntAccessor;

public class PrimedSizedTnt extends PrimedTnt {
    
    private static final EntityDataAccessor<String> TNTSIZE = SynchedEntityData.defineId(PrimedSizedTnt.class, EntityDataSerializers.STRING);
    
    public LittleGrid grid;
    public LittleVec size;
    
    public PrimedSizedTnt(EntityType<? extends PrimedSizedTnt> type, Level level) {
        super(type, level);
    }
    
    public PrimedSizedTnt(Level level, double x, double y, double z, @Nullable LivingEntity igniter, LittleGrid grid, LittleVec size) {
        super(LittleTilesRegistry.SIZED_TNT_TYPE.get(), level);
        this.setPos(x, y, z);
        double d0 = level.random.nextDouble() * ((float) Math.PI * 2F);
        this.setDeltaMovement(-Math.sin(d0) * 0.02D, 0.2F, -Math.cos(d0) * 0.02D);
        this.setFuse(80);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        ((PrimedTntAccessor) this).setOwner(igniter);
        setSize(grid, size);
        refreshDimensions();
    }
    
    @Override
    protected void defineSynchedData(Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TNTSIZE, "1.1.1.1");
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
        this.level().explode(this, this.getX(), this.getY(0.0625D), this.getZ(), (float) (4.0D * size.getPercentVolume(grid)), Level.ExplosionInteraction.TNT);
    }
    
}
