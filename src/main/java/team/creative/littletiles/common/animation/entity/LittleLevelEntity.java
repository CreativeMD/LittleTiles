package team.creative.littletiles.common.animation.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.level.CreativeClientLevel;
import team.creative.creativecore.common.level.CreativeLevel;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.creativecore.common.util.math.matrix.ChildVecOrigin;
import team.creative.creativecore.common.util.math.matrix.IVecOrigin;
import team.creative.littletiles.client.render.level.LittleRenderChunkSuppilier;
import team.creative.littletiles.common.animation.physic.LittleLevelEntityPhysic;
import team.creative.littletiles.common.math.location.LocalStructureLocation;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.connection.direct.StructureConnection;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.relative.StructureAbsolute;

public class LittleLevelEntity extends Entity implements OrientationAwareEntity {
    
    private CreativeLevel fakeLevel;
    private StructureAbsolute center;
    private IVecOrigin origin;
    private StructureConnection structure;
    
    public final LittleLevelEntityPhysic physic = new LittleLevelEntityPhysic(this);
    
    public double initalOffX;
    public double initalOffY;
    public double initalOffZ;
    public double initalRotX;
    public double initalRotY;
    public double initalRotZ;
    
    public LittleLevelEntity(EntityType<?> type, Level level) {
        super(type, level);
    }
    
    public LittleLevelEntity(EntityType<?> type, Level level, CreativeLevel fakeLevel, StructureAbsolute center, LocalStructureLocation location) {
        super(type, level);
        setFakeLevel(fakeLevel);
        setCenter(center);
        this.structure = new StructureConnection(fakeLevel, location);
        
        setPosition(center.baseOffset.getX(), center.baseOffset.getY(), center.baseOffset.getZ());
        
        physic.ignoreCollision(() -> {
            updateTickState();
            this.initalOffX = origin.offX();
            this.initalOffY = origin.offY();
            this.initalOffZ = origin.offZ();
            this.initalRotX = origin.rotX();
            this.initalRotY = origin.rotY();
            this.initalRotZ = origin.rotZ();
        });
        
        origin.tick();
    }
    
    public IVecOrigin getOrigin() {
        return origin;
    }
    
    public LittleLevelEntity getTopLevelEntity() {
        if (level instanceof ISubLevel)
            return ((LittleLevelEntity) ((ISubLevel) level).getHolder()).getTopLevelEntity();
        return this;
    }
    
    protected void setFakeLevel(CreativeLevel fakeLevel) {
        this.fakeLevel = fakeLevel;
        this.fakeLevel.setHolder(this);
        this.fakeLevel.registerLevelBoundListener(physic);
    }
    
    public StructureAbsolute getCenter() {
        return center;
    }
    
    public void setCenter(StructureAbsolute center) {
        this.center = center;
        this.fakeLevel.setOrigin(center.rotationCenter);
        this.origin = this.fakeLevel.getOrigin();
        for (Entity entity : fakeLevel.loadedEntities())
            if (entity instanceof OrientationAwareEntity)
                ((OrientationAwareEntity) entity).parentVecOriginChange(origin);
    }
    
    @Override
    public void parentVecOriginChange(IVecOrigin origin) {
        ((ChildVecOrigin) origin).parent = origin;
    }
    
    public LittleStructure getStructure() throws CorruptedConnectionException, NotYetConnectedException {
        return structure.getStructure();
    }
    
    @OnlyIn(Dist.CLIENT)
    public LittleRenderChunkSuppilier getRenderChunkSuppilier() {
        return (LittleRenderChunkSuppilier) ((CreativeClientLevel) fakeLevel).renderChunkSupplier;
    }
    
    @Override
    protected void defineSynchedData() {}
    
    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public Packet<?> getAddEntityPacket() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
