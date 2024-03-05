package team.creative.littletiles.common.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.ABB;
import team.creative.creativecore.common.util.math.box.BoxUtils;
import team.creative.creativecore.common.util.math.collision.CollidingPlane;
import team.creative.creativecore.common.util.math.collision.CollisionCoordinator;
import team.creative.creativecore.common.util.math.collision.PlaneCache;
import team.creative.creativecore.common.util.math.matrix.IVecOrigin;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.level.handler.LittleAnimationHandlers;
import team.creative.littletiles.common.level.little.LittleSubLevel;
import team.creative.littletiles.common.structure.animation.PhysicalState;

public abstract class LittleEntityPhysic<T extends LittleEntity<? extends LittleEntityPhysic>> {
    
    private static final Predicate<Entity> NO_ANIMATION = x -> !(x instanceof INoPushEntity);
    
    protected double minX;
    protected double minY;
    protected double minZ;
    protected double maxX;
    protected double maxY;
    protected double maxZ;
    protected boolean preventPush = false;
    protected boolean noCollision;
    private ABB bb;
    private Vec3 center;
    private boolean bbChanged = false;
    public final T parent;
    
    public LittleEntityPhysic(T parent) {
        this.parent = parent;
        this.bb = new ABB(parent.getBoundingBox());
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
    
    public void setBB(ABB bb) {
        if (bb.maxX >= Double.MAX_VALUE)
            return;
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
            parent.setBoundingBox(parent.getOrigin().getAABB(bb).toVanilla());
            if (originChanged)
                parent.resetOriginChange();
            center = parent.getBoundingBox().getCenter();
            bbChanged = false;
        }
    }
    
    public ABB getOBB() {
        return bb;
    }
    
    public Vec3 getCenter() {
        return center;
    }
    
    public void load(CompoundTag nbt) {
        preventPush = true; // No need to use ignoreCollision here, because it is internal
        set(nbt.getDouble("offX"), nbt.getDouble("offY"), nbt.getDouble("offZ"), nbt.getDouble("rotX"), nbt.getDouble("rotY"), nbt.getDouble("rotZ"));
        preventPush = false;
        
        minX = nbt.getDouble("x");
        minY = nbt.getDouble("y");
        minZ = nbt.getDouble("z");
        maxX = nbt.getDouble("x2");
        maxY = nbt.getDouble("y2");
        maxZ = nbt.getDouble("z2");
        setBB(new ABB(minX, minY, minZ, maxX, maxY, maxZ));
        loadExtra(nbt);
    }
    
    public abstract void loadExtra(CompoundTag nbt);
    
    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        IVecOrigin origin = getOrigin();
        nbt.putDouble("offX", origin.offX());
        nbt.putDouble("offY", origin.offY());
        nbt.putDouble("offZ", origin.offZ());
        nbt.putDouble("rotX", origin.rotX());
        nbt.putDouble("rotY", origin.rotY());
        nbt.putDouble("rotZ", origin.rotZ());
        
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
        
        CollisionCoordinator coordinator = new CollisionCoordinator(x, y, z, rotX, rotY, rotZ, getOrigin());
        if (LittleTiles.CONFIG.general.enableAnimationCollision)
            transform(coordinator);
        coordinator.finish();
    }
    
    public void transform(CollisionCoordinator coordinator) {
        if (preventPush)
            return;
        
        noCollision = true;
        
        List<Entity> entities = parent.getRealLevel().getEntities(parent, coordinator.computeSurroundingBox(bb).toVanilla(), NO_ANIMATION);
        if (!entities.isEmpty()) {
            for (int i = 0; i < entities.size(); i++) {
                Entity entity = entities.get(i);
                
                AABB entityBB = entity.getBoundingBox();
                AABB originalBox = entity.getBoundingBox();
                
                Vec3d center = new Vec3d(entityBB.getCenter());
                
                double radius = center.distanceSqr(entityBB.minX, entityBB.minY, entityBB.minZ);
                
                Double t = null;
                Facing facing = null;
                
                List<PlaneCache> cached = new ArrayList<>();
                
                // Calculate when or if they collide and collect all boxes that are important
                ABB inverseBB = coordinator.original().getOBB(originalBox);
                
                var shapes = parent.getSubLevel().getCollisions(entity, coordinator.computeInverseSurroundingBoxInternal(inverseBB).toVanilla());
                for (VoxelShape shape : shapes) { // Calculate all area the entity could collide, box is orientated to the sub level
                    for (AABB bb : shape.toAabbs()) {
                        PlaneCache cache = new PlaneCache(bb, coordinator);
                        cached.add(cache);
                        if (t != null && t == 0)
                            continue;
                        for (CollidingPlane plane : cache.planes) {
                            Double tempT = plane.binarySearch(t, entityBB, radius, center, coordinator);
                            if (tempT != null) {
                                t = tempT;
                                facing = plane.facing;
                                if (t == 0)
                                    break;
                            }
                        }
                    }
                    
                    // Applying found t
                    if (t != null) {
                        Vec3d newCenter = new Vec3d(center);
                        coordinator.transform(newCenter, 1 - t);
                        
                        entityBB = entityBB.move(newCenter.x - center.x, newCenter.y - center.y, newCenter.z - center.z);
                    }
                }
                
                Axis one = null;
                Axis two = null;
                
                boolean ignoreOne = false;
                Boolean positiveOne = null;
                boolean ignoreTwo = false;
                Boolean positiveTwo = null;
                
                double maxVolume = 0;
                
                List<PlaneCache> intersecting = new ArrayList<>();
                List<Facing> intersectingFacing = new ArrayList<>();
                ABB entityOBB = coordinator.moved().getOBB(entityBB);
                center.set(entityOBB.getCenter());
                
                for (PlaneCache cache : cached) {
                    if (!entityOBB.intersects(cache.bb))
                        continue;
                    Facing collideFacing = CollidingPlane.getDirection(coordinator, cache, center);
                    if (collideFacing == null || (!coordinator.hasRotation && (!coordinator.hasTranslation || coordinator.translation.get(
                        collideFacing.axis) > 0 != collideFacing.positive)))
                        continue;
                    
                    double intersectingVolume = BoxUtils.getIntersectionVolume(cache.bb, entityOBB);
                    
                    if (maxVolume == 0 || intersectingVolume > maxVolume) {
                        maxVolume = intersectingVolume;
                        facing = collideFacing;
                    }
                    
                    intersecting.add(cache);
                    intersectingFacing.add(collideFacing);
                }
                
                Vec3d pushVec = new Vec3d();
                double scale = 0;
                
                if (!intersecting.isEmpty()) {
                    one = facing.one();
                    two = facing.two();
                    
                    positiveOne = null;
                    positiveTwo = null;
                    
                    for (Facing collideFacing : intersectingFacing) {
                        
                        if (!ignoreOne && collideFacing.axis == one) {
                            if (positiveOne == null)
                                positiveOne = collideFacing.positive;
                            else if (collideFacing.positive != positiveOne)
                                ignoreOne = true;
                        } else if (!ignoreTwo && collideFacing.axis == two) {
                            if (positiveTwo == null)
                                positiveTwo = collideFacing.positive;
                            else if (collideFacing.positive != positiveTwo)
                                ignoreTwo = true;
                        }
                        
                        if (ignoreOne && ignoreTwo)
                            break;
                    }
                    
                    // Now things are ready. Go through all intersecting ones and push the box out
                    pushVec.set(facing.axis, facing.offset());
                    if (!ignoreOne && positiveOne != null)
                        pushVec.set(one, positiveOne ? 1 : -1);
                    if (!ignoreTwo && positiveTwo != null)
                        pushVec.set(two, positiveTwo ? 1 : -1);
                    
                    for (int j = 0; j < intersecting.size(); j++) {
                        Facing collideFacing = intersectingFacing.get(j);
                        
                        if ((ignoreOne && collideFacing.axis == one) || (ignoreTwo && collideFacing.axis == two))
                            continue;
                        
                        scale = intersecting.get(j).getPushOutScale(scale, entityOBB, pushVec);
                    }
                }
                
                boolean collidedHorizontally = entity.horizontalCollision;
                boolean collidedVertically = entity.verticalCollision;
                boolean onGround = entity.onGround();
                
                Vec3d rotatedVec = new Vec3d(pushVec);
                coordinator.moved().rotation().transform(rotatedVec);
                System.out.println(parent.level().isClientSide + " " + rotatedVec + " " + scale);
                
                double moveX = entityBB.minX - originalBox.minX + rotatedVec.x * scale;
                double moveY = entityBB.minY - originalBox.minY + rotatedVec.y * scale;
                double moveZ = entityBB.minZ - originalBox.minZ + rotatedVec.z * scale;
                
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
        
        for (OrientationAwareEntity child : parent.children())
            child.transform(coordinator);
        
        noCollision = false;
    }
    
}
