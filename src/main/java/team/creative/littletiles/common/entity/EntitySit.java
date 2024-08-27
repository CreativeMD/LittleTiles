package team.creative.littletiles.common.entity;

import org.joml.Vector3d;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.connection.ILevelPositionProvider;
import team.creative.littletiles.common.structure.connection.children.StructureChildConnection;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.type.LittleChair;

public class EntitySit extends Entity implements ILevelPositionProvider, INoPushEntity {
    
    public static final EntityDataAccessor<CompoundTag> CONNECTION = SynchedEntityData.defineId(EntitySit.class, EntityDataSerializers.COMPOUND_TAG);
    public static final EntityDataAccessor<Float> CHAIRX = SynchedEntityData.defineId(EntitySit.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> CHAIRY = SynchedEntityData.defineId(EntitySit.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> CHAIRZ = SynchedEntityData.defineId(EntitySit.class, EntityDataSerializers.FLOAT);
    private StructureChildConnection temp;
    
    public EntitySit(LittleChair chair, Level level, double x, double y, double z) {
        super(LittleTilesRegistry.SIT_TYPE.get(), level);
        entityData.set(CHAIRX, (float) x);
        entityData.set(CHAIRY, (float) y);
        entityData.set(CHAIRZ, (float) z);
        this.temp = chair.children.generateConnection(this);
        entityData.set(CONNECTION, temp.save(new CompoundTag()));
        setPos(x, y, z);
    }
    
    public EntitySit(EntityType<? extends EntitySit> type, Level level) {
        super(type, level);
        noPhysics = true;
    }
    
    @Override
    public boolean isPushable() {
        return false;
    }
    
    @Override
    public void tick() {
        super.tick();
        StructureChildConnection connection = StructureChildConnection.load(this, entityData.get(CONNECTION), false);
        if (!level().isClientSide && !isVehicle()) {
            try {
                
                LittleStructure structure = connection.getStructure();
                if (structure instanceof LittleChair)
                    ((LittleChair) structure).setPlayer(null);
                kill();
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
            
        } else {
            try {
                LittleStructure structure = connection.getStructure();
                if (structure.getStructureLevel() instanceof IOrientatedLevel) {
                    Vector3d vec = new Vector3d(entityData.get(CHAIRX), entityData.get(CHAIRY), entityData.get(CHAIRZ));
                    ((IOrientatedLevel) structure.getStructureLevel()).getOrigin().transformPointToWorld(vec);
                    setPos(vec.x, vec.y, vec.z);
                }
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        }
    }
    
    @Override
    public Vec3 getPassengerRidingPosition(Entity entity) {
        return position();
    }
    
    @Override
    protected void defineSynchedData(Builder builder) {
        builder.define(CONNECTION, new CompoundTag());
        builder.define(CHAIRX, 0F);
        builder.define(CHAIRY, 0F);
        builder.define(CHAIRZ, 0F);
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.put("connection", entityData.get(CONNECTION));
        nbt.putFloat("chairX", entityData.get(CHAIRX));
        nbt.putFloat("chairY", entityData.get(CHAIRY));
        nbt.putFloat("chairZ", entityData.get(CHAIRZ));
    }
    
    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        entityData.set(CONNECTION, nbt.getCompound("connection"));
        entityData.set(CHAIRX, nbt.getFloat("chairX"));
        entityData.set(CHAIRY, nbt.getFloat("chairY"));
        entityData.set(CHAIRZ, nbt.getFloat("chairZ"));
    }
    
    @Override
    public Level getStructureLevel() {
        return level();
    }
    
    @Override
    public BlockPos getStructurePos() {
        return BlockPos.ZERO;
    }
    
    @Override
    public void structureDestroyed() {}
    
}
