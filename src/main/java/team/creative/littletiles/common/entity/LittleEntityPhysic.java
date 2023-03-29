package team.creative.littletiles.common.entity;

import java.util.List;
import java.util.function.Predicate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.collision.CollisionCoordinator;
import team.creative.creativecore.common.util.math.matrix.IVecOrigin;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.level.handler.LittleAnimationHandlers;
import team.creative.littletiles.common.level.little.LittleSubLevel;
import team.creative.littletiles.common.structure.animation.PhysicalState;

public abstract class LittleEntityPhysic<T extends LittleEntity<? extends LittleEntityPhysic>> implements INoPushEntity {
    
    private static final Predicate<Entity> NO_ANIMATION = x -> !(x instanceof INoPushEntity);
    
    protected double minX;
    protected double minY;
    protected double minZ;
    protected double maxX;
    protected double maxY;
    protected double maxZ;
    protected boolean preventPush = false;
    protected boolean noCollision;
    private AABB bb;
    private Vec3 center;
    private boolean bbChanged = false;
    public final T parent;
    
    public LittleEntityPhysic(T parent) {
        this.parent = parent;
        this.bb = parent.getBoundingBox();
    }
    
    public double get(Facing facing) {
        return switch (facing) {
            case EAST -> maxX;
            case WEST -> minX;
            case UP -> maxY;
            case DOWN -> minY;
            case SOUTH -> maxZ;
            case NORTH -> minZ;
        };
    }
    
    public void set(Facing facing, double value) {
        switch (facing) {
            case EAST -> maxX = value;
            case WEST -> minX = value;
            case UP -> maxY = value;
            case DOWN -> minY = value;
            case SOUTH -> maxZ = value;
            case NORTH -> minZ = value;
        };
    }
    
    public IVecOrigin getOrigin() {
        return parent.getOrigin();
    }
    
    public void ignoreCollision(Runnable run) {
        preventPush = true;
        try {
            run.run();
        } finally {
            preventPush = false;
        }
    }
    
    public boolean shouldPush() {
        return !preventPush;
    }
    
    public abstract void setSubLevel(LittleSubLevel level);
    
    public abstract void tick();
    
    public void setBB(AABB bb) {
        this.bb = bb;
        this.bbChanged = true;
    }
    
    public void updateBoundingBox() {
        if (bb == null || parent.getSubLevel() == null)
            return;
        
        boolean originChanged = parent.getOrigin().hasChanged() || parent.getOrigin().hasChanged();
        if (bbChanged || originChanged) {
            if (originChanged)
                parent.markOriginChange();
            parent.setBoundingBox(parent.getOrigin().getAABB(bb));
            if (originChanged)
                parent.resetOriginChange();
            center = parent.getBoundingBox().getCenter();
            bbChanged = false;
        }
    }
    
    public AABB getOBB() {
        return bb;
    }
    
    public Vec3 getCenter() {
        return center;
    }
    
    public void load(CompoundTag nbt) {
        minX = nbt.getDouble("x");
        minY = nbt.getDouble("y");
        minZ = nbt.getDouble("z");
        maxX = nbt.getDouble("x2");
        maxY = nbt.getDouble("y2");
        maxZ = nbt.getDouble("z2");
        setBB(new AABB(minX, minY, minZ, maxX, maxY, maxZ));
        loadExtra(nbt);
    }
    
    public abstract void loadExtra(CompoundTag nbt);
    
    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putDouble("x", minX);
        nbt.putDouble("y", minY);
        nbt.putDouble("z", minZ);
        nbt.putDouble("x2", maxX);
        nbt.putDouble("y2", maxY);
        nbt.putDouble("z2", maxZ);
        saveExtra(nbt);
        return nbt;
    }
    
    protected abstract void saveExtra(CompoundTag nbt);
    
    public void set(PhysicalState state) {
        set(state.offX(), state.offY(), state.offZ(), state.rotX(), state.rotY(), state.rotZ());
    }
    
    public void set(double offX, double offY, double offZ, double rotX, double rotY, double rotZ) {
        IVecOrigin origin = getOrigin();
        moveAndRotateAnimation(offX - origin.offX(), offY - origin.offY(), offZ - origin.offZ(), rotX - origin.rotX(), rotY - origin.rotY(), rotZ - origin.rotZ());
    }
    
    public void moveAndRotateAnimation(double x, double y, double z, double rotX, double rotY, double rotZ) {
        if (x == 0 && y == 0 && z == 0 && rotX == 0 && rotY == 0 && rotZ == 0)
            return;
        
        CollisionCoordinator coordinator = new CollisionCoordinator(x, y, z, rotX, rotY, rotZ, getOrigin(), getOrigin());
        if (LittleTiles.CONFIG.general.enableAnimationCollision)
            transform(coordinator);
        coordinator.move();
    }
    
    public void transform(CollisionCoordinator coordinator) {
        if (preventPush)
            return;
        
        noCollision = true;
        
        List<Entity> entities = parent.getRealLevel().getEntities(parent, coordinator.computeSurroundingBox(bb), NO_ANIMATION);
        if (!entities.isEmpty()) {
            for (int j = 0; j < entities.size(); j++) {
                Entity entity = entities.get(j);
                AABB surroundingBB = coordinator.computeInverseSurroundingBox(entity.getBoundingBox()); // Calculate all area the entity could collide with box is orientated to the sub level
                
                double t = -1;
                for (VoxelShape shape : parent.getSubLevel().getCollisions(entity, surroundingBB)) {
                    // Calculate when or if they collide
                    
                }
                
                AABB originalBox = entity.getBoundingBox();
                Vec3d newCenter = new Vec3d(originalBox.getCenter());
                coordinator.transform(newCenter, 1 - t);
                
                boolean collidedHorizontally = entity.horizontalCollision;
                boolean collidedVertically = entity.verticalCollision;
                boolean onGround = entity.isOnGround();
                
                double moveX = newCenter.x - center.x;
                double moveY = newCenter.y - center.y;
                double moveZ = newCenter.z - center.z;
                
                entity.move(MoverType.SELF, new Vec3(moveX, moveY, moveZ));
                
                if (entity instanceof ServerPlayer)
                    LittleAnimationHandlers.setPushedByDoor((ServerPlayer) entity);
                
                if (LittleTiles.CONFIG.general.enableCollisionMotion)
                    entity.getDeltaMovement().add(moveX, moveY, moveZ);
                
                if (moveX != 0 || moveZ != 0)
                    collidedHorizontally = true;
                if (moveY != 0) {
                    collidedVertically = true;
                    onGround = true;
                }
                
                entity.horizontalCollision = collidedHorizontally;
                entity.verticalCollision = collidedVertically;
                entity.setOnGround(onGround);
            }
        }
        
        coordinator.move();
        
        for (OrientationAwareEntity child : parent.children()) {
            coordinator.reset(child.getOrigin());
            child.transform(coordinator);
        }
        
        noCollision = false;
    }
    
}
