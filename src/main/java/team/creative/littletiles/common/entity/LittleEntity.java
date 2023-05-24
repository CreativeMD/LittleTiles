package team.creative.littletiles.common.entity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.creativecore.common.util.math.collision.CollisionCoordinator;
import team.creative.creativecore.common.util.math.matrix.ChildVecOrigin;
import team.creative.creativecore.common.util.math.matrix.IVecOrigin;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.type.itr.FilterIterator;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.render.entity.LittleEntityRenderManager;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.common.level.little.LittleSubLevel;
import team.creative.littletiles.common.math.vec.LittleHitResult;

public abstract class LittleEntity<T extends LittleEntityPhysic> extends Entity implements OrientationAwareEntity, INoPushEntity {
    
    private Iterable<OrientationAwareEntity> childrenItr = () -> new FilterIterator<OrientationAwareEntity>(entities(), OrientationAwareEntity.class);
    
    protected LittleSubLevel subLevel;
    protected IVecOrigin origin;
    protected boolean hasOriginChanged = false;
    public final T physic = createPhysic();
    private List<Entity> entitiesToAdd;
    
    // ================Constructors================
    
    public LittleEntity(EntityType<?> type, Level level) {
        super(type, level);
    }
    
    public LittleEntity(EntityType<?> type, Level level, Vec3d center) {
        super(type, level);
        setSubLevel(createLevel(), center);
        setPos(center.x, center.y, center.z);
        origin.tick();
        physic.ignoreCollision(() -> initialTick());
    }
    
    public LittleEntity(EntityType<?> type, Level level, LittleSubLevel subLevel, Vec3d center) {
        super(type, level);
        setSubLevel(subLevel, center);
        setPos(center.x, center.y, center.z);
        origin.tick();
        physic.ignoreCollision(() -> initialTick());
    }
    
    protected abstract LittleSubLevel createLevel();
    
    protected abstract T createPhysic();
    
    public boolean isReal() {
        if (level instanceof ISubLevel sub)
            level = sub.getRealLevel();
        return !(level instanceof IOrientatedLevel);
    }
    
    // ================Origin================
    
    @Override
    public void markOriginChange() {
        hasOriginChanged = true;
        for (OrientationAwareEntity child : children())
            child.markOriginChange();
    }
    
    public void resetOriginChange() {
        hasOriginChanged = false;
    }
    
    @Override
    protected void defineSynchedData() {}
    
    @Override
    public IVecOrigin getOrigin() {
        return origin;
    }
    
    public LittleSubLevel getSubLevel() {
        return subLevel;
    }
    
    public Level getRealLevel() {
        if (level instanceof ISubLevel sub)
            return sub.getRealLevel();
        return level;
    }
    
    public LittleEntity getTopLevelEntity() {
        if (level instanceof ISubLevel sub)
            return ((LittleEntity) sub.getHolder()).getTopLevelEntity();
        return this;
    }
    
    public abstract CreativePacket initClientPacket();
    
    public abstract void startTracking(ServerPlayer player);
    
    public abstract void stopTracking(ServerPlayer player);
    
    protected void setSubLevel(LittleSubLevel subLevel, Vec3d center) {
        this.subLevel = subLevel;
        this.subLevel.setHolder(this);
        this.subLevel.setOrigin(center);
        this.origin = subLevel.getOrigin();
        physic.setSubLevel(subLevel);
    }
    
    @Override
    public void parentVecOriginChange(IVecOrigin origin) {
        ((ChildVecOrigin) origin).parent = origin;
    }
    
    @Override
    public void transform(CollisionCoordinator coordinator) {
        physic.transform(coordinator);
    }
    
    public AABB getRealBB() {
        if (level instanceof ISubLevel or)
            return or.getOrigin().getAABB(getBoundingBox());
        return getBoundingBox();
    }
    
    public Vec3 getRealCenter() {
        if (level instanceof ISubLevel or)
            return or.getOrigin().transformPointToWorld(position());
        return position();
    }
    
    // ================Children================
    
    public Iterable<Entity> entities() {
        return ((LittleLevel) subLevel).entities();
    }
    
    public Iterable<OrientationAwareEntity> children() {
        return childrenItr;
    }
    
    // ================Rendering================
    
    @OnlyIn(Dist.CLIENT)
    public abstract LittleEntityRenderManager getRenderManager();
    
    // ================Ticking================
    
    public abstract void initialTick();
    
    public abstract void internalTick();
    
    @Override
    public void performTick() {
        if (entitiesToAdd != null) {
            for (Entity entity : entitiesToAdd)
                subLevel.addFreshEntity(entity);
            entitiesToAdd = null;
        }
        if (level instanceof ISubLevel) {
            if (!level.isClientSide)
                this.setSharedFlag(6, this.isCurrentlyGlowing());
            super.baseTick();
        } else
            super.tick();
        
        children().forEach(x -> x.performTick());
        internalTick();
        subLevel.tick();
        
        physic.tick();
        physic.updateBoundingBox();
        
        syncMovement();
        
        origin.tick();
        
        Vec3 center = physic.getCenter();
        setPosRaw(center.x, center.y, center.z);
    }
    
    public abstract void syncMovement();
    
    // ================Save&Load================
    
    protected abstract Vec3d loadCenter(CompoundTag nbt);
    
    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        setSubLevel(createLevel(), loadCenter(nbt));
        
        physic.load(nbt.getCompound("physic"));
        
        loadEntity(nbt);
        
        physic.updateBoundingBox();
        if (nbt.contains("entities")) {
            entitiesToAdd = new ArrayList<>();
            ListTag list = nbt.getList("entities", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++)
                EntityType.create(list.getCompound(i), (Level) subLevel).ifPresent(entitiesToAdd::add);
        } else
            entitiesToAdd = null;
    }
    
    public abstract void loadEntity(CompoundTag nbt);
    
    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        nbt.put("physic", physic.save());
        
        saveEntity(nbt);
        
        ListTag list = new ListTag();
        for (Entity entity : entities()) {
            CompoundTag entityNBT = new CompoundTag();
            entity.save(entityNBT);
            list.add(entityNBT);
        }
        if (!list.isEmpty())
            nbt.put("entities", list);
    }
    
    public abstract void saveEntity(CompoundTag nbt);
    
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
    
    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        LittleTiles.ANIMATION_HANDLERS.get(level).add(this);
    }
    
    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        LittleTiles.ANIMATION_HANDLERS.get(level).remove(this);
    }
    
    // ================MC Hooks================
    
    @Override
    public void setPos(double x, double y, double z) {
        super.setPosRaw(x, y, z); // Fixes bounding box
    }
    
    @Override
    protected AABB makeBoundingBox() {
        return origin.getAABB(physic.getOBB());
    }
    
    @Override
    public boolean isOnFire() {
        return false;
    }
    
    @Override
    public boolean fireImmune() {
        return true;
    }
    
    @Override
    public boolean displayFireAnimation() {
        return false;
    }
    
    public void destroyAnimation() {
        this.kill();
    }
    
    @Override
    public void kill() {
        subLevel.unload();
        super.kill();
    }
    
    @Override
    public boolean canBeCollidedWith() {
        return false;
    }
    
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }
    
    @Override
    public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
        return InteractionResult.PASS;
    }
    
    // ================Hit Result================
    
    public LittleHitResult rayTrace(Vec3 pos, Vec3 look) {
        LittleHitResult result = null;
        double distance = 0;
        for (Entity entity : entities()) {
            if (entity instanceof LittleEntity levelEntity) {
                Vec3 newPos = levelEntity.origin.transformPointToFakeWorld(pos);
                Vec3 newLook = levelEntity.origin.transformPointToFakeWorld(look);
                
                if (levelEntity.physic.getOBB().intersects(newPos, newLook)) {
                    LittleHitResult tempResult = levelEntity.rayTrace(pos, look);
                    if (tempResult == null)
                        continue;
                    double tempDistance = newPos.distanceTo(tempResult.hit.getLocation());
                    if (result == null || tempDistance < distance) {
                        result = tempResult;
                        distance = tempDistance;
                    }
                }
            } else {
                Vec3 newPos = origin.transformPointToFakeWorld(pos);
                Vec3 newLook = origin.transformPointToFakeWorld(look);
                if (entity.getBoundingBox().intersects(newPos, newLook)) {
                    LittleHitResult tempResult = new LittleHitResult(this, new EntityHitResult(entity, entity.getBoundingBox().clip(newPos, newLook).get()), subLevel);
                    double tempDistance = newPos.distanceTo(tempResult.hit.getLocation());
                    if (result == null || tempDistance < distance) {
                        result = tempResult;
                        distance = tempDistance;
                    }
                }
            }
        }
        
        Vec3 newPos = origin.transformPointToFakeWorld(pos);
        Vec3 newLook = origin.transformPointToFakeWorld(look);
        HitResult tempResult = subLevel.clip(new ClipContext(newPos, newLook, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, null));
        if (tempResult == null || tempResult.getType() != Type.BLOCK || !(tempResult instanceof BlockHitResult))
            return result;
        if (result == null || pos.distanceTo(tempResult.getLocation()) < distance)
            return new LittleHitResult(this, tempResult, subLevel);
        return result;
    }
    
    // ================CLIENT================
    
    @Override
    public boolean shouldRender(double x, double y, double z) {
        Vec3 center = getRealCenter();
        double d0 = center.x - x;
        double d1 = center.y - y;
        double d2 = center.z - z;
        return this.shouldRenderAtSqrDistance(d0 * d0 + d1 * d1 + d2 * d2);
    }
    
    public boolean hasLoaded() {
        return subLevel != null;
    }
}
