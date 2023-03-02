package team.creative.littletiles.common.entity.animation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.client.render.entity.LittleAnimationRenderManager;
import team.creative.littletiles.client.render.entity.LittleEntityRenderManager;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.entity.OrientationAwareEntity;
import team.creative.littletiles.common.level.little.LittleSubLevel;
import team.creative.littletiles.common.math.location.LocalStructureLocation;
import team.creative.littletiles.common.packet.entity.animation.LittleAnimationInitPacket;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.connection.direct.StructureConnection;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.relative.StructureAbsolute;
import team.creative.littletiles.server.level.little.SubServerLevel;

public class LittleAnimationEntity extends LittleEntity<LittleAnimationEntityPhysic> {
    
    private StructureAbsolute center;
    private StructureConnection structure;
    
    public LittleAnimationEntity(EntityType<?> type, Level level) {
        super(type, level);
    }
    
    public LittleAnimationEntity(Level level, LittleSubLevel subLevel, StructureAbsolute center, LocalStructureLocation location) {
        super(LittleTilesRegistry.ENTITY_ANIMATION.get(), level, subLevel, center.baseOffset);
        setCenter(center);
        this.structure = new StructureConnection((Level) subLevel, location);
    }
    
    @Override
    protected LittleSubLevel createLevel() {
        return new LittleAnimationLevel(level);
    }
    
    public void setCenter(StructureAbsolute center) {
        this.center = center;
        this.subLevel.setOrigin(center.rotationCenter);
        for (OrientationAwareEntity entity : children())
            entity.parentVecOriginChange(origin);
    }
    
    public StructureAbsolute getCenter() {
        return center;
    }
    
    public LittleStructure getStructure() throws CorruptedConnectionException, NotYetConnectedException {
        return structure.getStructure();
    }
    
    @Override
    public void loadEntity(CompoundTag nbt) {
        setCenter(new StructureAbsolute("center", nbt));
        this.structure = new StructureConnection((Level) subLevel, nbt.getCompound("structure"));
        try {
            this.structure.getStructure();
        } catch (CorruptedConnectionException | NotYetConnectedException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void saveEntity(CompoundTag nbt) {
        nbt.put("structure", structure.write());
        center.save("center", nbt);
    }
    
    @Override
    public CreativePacket initClientPacket() {
        return new LittleAnimationInitPacket(this);
    }
    
    public CompoundTag saveExtraClientData() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("physic", physic.save());
        return nbt;
    }
    
    public void initSubLevelClient(StructureAbsolute absolute, CompoundTag extraData) {
        setSubLevel(SubServerLevel.createSubLevel(level));
        setCenter(absolute);
        physic.load(extraData.getCompound("physic"));
        ((LittleAnimationLevel) subLevel).renderManager = new LittleAnimationRenderManager(this);
    }
    
    @Override
    public void internalTick() {}
    
    @Override
    public void initialTick() {}
    
    @Override
    protected LittleAnimationEntityPhysic createPhysic() {
        return new LittleAnimationEntityPhysic(this);
    }
    
    @Override
    public LittleEntityRenderManager getRenderManager() {
        return ((LittleAnimationLevel) subLevel).renderManager;
    }
}
