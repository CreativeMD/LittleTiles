package team.creative.littletiles.common.animation.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.mojang.math.Vector3d;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.level.CreativeClientLevel;
import team.creative.creativecore.common.level.CreativeLevel;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.creativecore.common.util.math.matrix.ChildVecOrigin;
import team.creative.creativecore.common.util.math.matrix.IVecOrigin;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.client.render.level.LittleRenderChunkSuppilier;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.animation.AnimationState;
import team.creative.littletiles.common.animation.EntityAnimationController;
import team.creative.littletiles.common.animation.physic.LittleLevelEntityPhysic;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.item.ItemLittleWrench;
import team.creative.littletiles.common.math.location.LocalStructureLocation;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecAbsolute;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.connection.direct.StructureConnection;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.relative.StructureAbsolute;

public class LittleLevelEntity extends Entity implements OrientationAwareEntity {
    
    private CreativeLevel fakeLevel;
    private StructureAbsolute center;
    private IVecOrigin origin;
    protected boolean hasOriginChanged = false;
    private StructureConnection structure;
    
    public final LittleLevelEntityPhysic physic = new LittleLevelEntityPhysic(this);
    
    public double initalOffX;
    public double initalOffY;
    public double initalOffZ;
    public double initalRotX;
    public double initalRotY;
    public double initalRotZ;
    
    // ================Constructors================
    
    public LittleLevelEntity(EntityType<?> type, Level level) {
        super(type, level);
    }
    
    public LittleLevelEntity(EntityType<?> type, Level level, CreativeLevel fakeLevel, StructureAbsolute center, LocalStructureLocation location) {
        super(type, level);
        setFakeLevel(fakeLevel);
        setCenter(center);
        this.structure = new StructureConnection(fakeLevel, location);
        
        setPos(center.baseOffset.getX(), center.baseOffset.getY(), center.baseOffset.getZ());
        
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
    
    // ================Origin================
    
    @Override
    public void markOriginChange() {
        hasOriginChanged = true;
        for (OrientationAwareEntity child : children())
            child.markOriginChange();
    }
    
    @Override
    protected void defineSynchedData() {}
    
    public IVecOrigin getOrigin() {
        return origin;
    }
    
    public Level getRealLevel() {
        if (level instanceof ISubLevel)
            return ((ISubLevel) level).getRealLevel();
        return level;
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
    
    // ================Children================
    
    public Iterable<Entity> entities() {
        
    }
    
    public Iterable<OrientationAwareEntity> children() {
        
    }
    
    // ================Rendering================
    
    @OnlyIn(Dist.CLIENT)
    public LittleRenderChunkSuppilier getRenderChunkSuppilier() {
        return (LittleRenderChunkSuppilier) ((CreativeClientLevel) fakeLevel).renderChunkSupplier;
    }
    
    // ================Ticking================
    
    public void onUpdateForReal() {
        if (fakeWorld == null && !world.isRemote)
            isDead = true;
        
        if (fakeWorld == null)
            return;
        
        if (fakeWorld.hasChanged)
            updateWorldCollision();
        
        if (collisionBoxWorker != null) {
            collisionBoxWorker.work();
            
            if (collisionBoxWorker.hasFinished())
                collisionBoxWorker = null;
        }
        
        origin.tick();
        
        handleForces();
        
        if (world instanceof IOrientatedWorld) {
            if (!world.isRemote)
                setFlag(6, this.isGlowing());
            onEntityUpdate();
        } else
            super.onUpdate();
        
        for (int i = 0; i < fakeWorld.loadedEntityList.size(); i++) {
            Entity entity = fakeWorld.loadedEntityList.get(i);
            if (entity instanceof EntityAnimation)
                ((EntityAnimation) entity).onUpdateForReal();
        }
        fakeWorld.loadedEntityList.removeIf((x) -> {
            if (x.isDead) {
                if (x instanceof EntityAnimation)
                    ((EntityAnimation) x).markRemoved();
                return true;
            }
            return false;
        });
        
        onTick();
        
        updateBoundingBox();
        
        List<BlockPos> positions = new ArrayList<>();
        
        for (Iterator<TileEntity> iterator = fakeWorld.loadedTileEntityList.iterator(); iterator.hasNext();) {
            TileEntity te = iterator.next();
            
            if (((TileEntityLittleTiles) te).isTicking())
                ((TileEntityLittleTiles) te).tick();
            
        }
        
        prevPosX = center.baseOffset.getX() + origin.offXLast();
        prevPosY = center.baseOffset.getY() + origin.offYLast();
        prevPosZ = center.baseOffset.getZ() + origin.offZLast();
        posX = center.baseOffset.getX() + origin.offX();
        posY = center.baseOffset.getY() + origin.offY();
        posZ = center.baseOffset.getZ() + origin.offZ();
        
        doBlockCollisions();
    }
    
    protected void handleForces() {
        motionX = 0;
        motionY = 0;
        motionZ = 0;
    }
    
    public void updateBoundingBox() {
        if (worldBoundingBox == null || fakeWorld == null)
            return;
        
        if (origin.hasChanged() || hasOriginChanged) {
            markOriginChange();
            setEntityBoundingBox(origin.getAxisAlignedBox(worldBoundingBox));
            hasOriginChanged = false;
        }
    }
    
    public void updateTickState() {
        if (controller == null)
            return;
        AnimationState state = controller.getTickingState();
        Vector3d offset = state.getOffset();
        Vector3d rotation = state.getRotation();
        moveAndRotateAnimation(offset.x - origin.offX(), offset.y - origin.offY(), offset.z - origin.offZ(), rotation.x - origin.rotX(), rotation.y - origin
                .rotY(), rotation.z - origin.rotZ());
        origin.tick();
        hasOriginChanged = true;
    }
    
    public void onTick() {
        if (controller == null)
            return;
        AnimationState state = controller.tick();
        Vector3d offset = state.getOffset();
        Vector3d rotation = state.getRotation();
        moveAndRotateAnimation(offset.x - origin.offX(), offset.y - origin.offY(), offset.z - origin.offZ(), rotation.x - origin.rotX(), rotation.y - origin
                .rotY(), rotation.z - origin.rotZ());
    }
    
    // ================Save&Load================
    
    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        if (fakeWorld != null) {
            for (Entity entity : fakeWorld.loadedEntityList) {
                if (entity instanceof EntityAnimation)
                    ((EntityAnimation) entity).markRemoved();
            }
        }
        
        setFakeWorld(compound.getBoolean("subworld") ? SubWorld.createFakeWorld(world) : FakeWorld.createFakeWorld(getCachedUniqueIdString(), world.isRemote));
        
        this.initalOffX = compound.getDouble("initOffX");
        this.initalOffY = compound.getDouble("initOffY");
        this.initalOffZ = compound.getDouble("initOffZ");
        this.initalRotX = compound.getDouble("initRotX");
        this.initalRotY = compound.getDouble("initRotY");
        this.initalRotZ = compound.getDouble("initRotZ");
        
        fakeWorld.preventNeighborUpdate = true;
        
        if (compound.hasKey("axis"))
            setCenterVec(new LittleVecAbsolute("axis", compound), new LittleVec("additional", compound));
        else
            setCenter(new StructureAbsolute("center", compound));
        NBTTagList list = compound.getTagList("tileEntity", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound nbt = list.getCompoundTagAt(i);
            BlockPos pos = new BlockPos(nbt.getInteger("x"), nbt.getInteger("y"), nbt.getInteger("z"));
            fakeWorld.setBlockState(pos, BlockTile.getState(nbt.getInteger("stateId")));
            TileEntityLittleTiles te = (TileEntityLittleTiles) fakeWorld.getTileEntity(pos);
            te.readFromNBT(nbt);
            if (world.isRemote)
                te.render.tilesChanged();
        }
        
        fakeWorld.loadedTileEntityList.removeIf(x -> x.isInvalid());
        
        int[] array = compound.getIntArray("previewPos");
        if (array.length == 3)
            absolutePreviewPos = new BlockPos(array[0], array[1], array[2]);
        else
            absolutePreviewPos = center.baseOffset;
        
        if (compound.hasKey("identifier")) {
            try {
                LittleIdentifierAbsolute identifier = new LittleIdentifierAbsolute(compound.getCompoundTag("identifier"));
                int index = identifier.generateIndex();
                this.structureLocation = new LocalStructureLocation(identifier.pos, index);
                this.structure = structureLocation.find(fakeWorld);
            } catch (LittleActionException e) {
                throw new RuntimeException(e);
            }
        } else if (compound.hasKey("location")) {
            try {
                this.structureLocation = new LocalStructureLocation(compound.getCompoundTag("location"));
                this.structure = structureLocation.find(fakeWorld);
            } catch (LittleActionException e) {
                throw new RuntimeException(e);
            }
        } else {
            structure = searchForParent();
            structureLocation = new LocalStructureLocation(structure);
        }
        
        controller = EntityAnimationController.parseController(this, compound.getCompoundTag("controller"));
        
        fakeWorld.preventNeighborUpdate = false;
        
        if (compound.hasKey("subEntities")) {
            NBTTagList subEntities = compound.getTagList("subEntities", 10);
            for (int i = 0; i < subEntities.tagCount(); i++) {
                Entity entity = EntityList.createEntityFromNBT(subEntities.getCompoundTagAt(i), fakeWorld);
                if (entity != null)
                    fakeWorld.spawnEntity(entity);
            }
        }
        updateWorldCollision();
        updateBoundingBox();
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        center.writeToNBT("center", compound);
        
        compound.setDouble("initOffX", initalOffX);
        compound.setDouble("initOffY", initalOffY);
        compound.setDouble("initOffZ", initalOffZ);
        compound.setDouble("initRotX", initalRotX);
        compound.setDouble("initRotY", initalRotY);
        compound.setDouble("initRotZ", initalRotZ);
        
        compound.setBoolean("subworld", fakeWorld.hasParent());
        
        NBTTagList list = new NBTTagList();
        
        for (Iterator<TileEntity> iterator = fakeWorld.loadedTileEntityList.iterator(); iterator.hasNext();) {
            TileEntity te = iterator.next();
            if (te instanceof TileEntityLittleTiles) {
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setInteger("stateId", BlockTile.getStateId((TileEntityLittleTiles) te));
                list.appendTag(te.writeToNBT(nbt));
            }
        }
        
        compound.setTag("controller", controller.writeToNBT(new NBTTagCompound()));
        
        compound.setTag("tileEntity", list);
        
        compound.setIntArray("previewPos", new int[] { absolutePreviewPos.getX(), absolutePreviewPos.getY(), absolutePreviewPos.getZ() });
        
        compound.setTag("location", structureLocation.write());
        
        if (!fakeWorld.loadedEntityList.isEmpty()) {
            NBTTagList subEntities = new NBTTagList();
            for (Entity entity : fakeWorld.loadedEntityList) {
                NBTTagCompound nbt = new NBTTagCompound();
                entity.writeToNBTAtomically(nbt);
                subEntities.appendTag(nbt);
            }
            compound.setTag("subEntities", subEntities);
        }
    }
    
    // ================MC Hooks================
    
    @Override
    public boolean isBurning() {
        return false;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderOnFire() {
        return false;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        
    }
    
    @Override
    public void setPositionAndRotation(double x, double y, double z, float yaw, float pitch) {
        
    }
    
    @Override
    public void setPositionAndUpdate(double x, double y, double z) {
        
    }
    
    @Override
    public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {
        
    }
    
    public void setInitialPosition(double x, double y, double z) {
        setPosition(x, y, z);
    }
    
    @Override
    public void setPosition(double x, double y, double z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        updateBoundingBox();
    }
    
    @Override
    public void setDead() {
        if (!this.isDead && (!world.isRemote || controller == null)) {
            this.isDead = true;
        }
    }
    
    public void destroyAnimation() {
        this.isDead = true;
    }
    
    @Override
    public boolean canBeCollidedWith() {
        return false;
    }
    
    @Override
    public AABB getCollisionBox(Entity entityIn) {
        return null;
    }
    
    @Override
    public AABB getCollisionBoundingBox() {
        return null;
    }
    
    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        
        return true;
    }
    
    @Override
    public Packet<?> getAddEntityPacket() {
        // TODO Auto-generated method stub
        return null;
    }
    
    // ================Hit Result================
    
    public LittleRayTraceResult getRayTraceResult(Vec3d pos, Vec3d look) {
        return getTarget(fakeWorld, origin.transformPointToFakeWorld(pos), origin.transformPointToFakeWorld(look), pos, look);
    }
    
    private static LittleRayTraceResult getTarget(CreativeWorld world, Vec3d pos, Vec3d look, Vec3d originalPos, Vec3d originalLook) {
        LittleRayTraceResult result = null;
        double distance = 0;
        if (!world.loadedEntityList.isEmpty()) {
            for (Entity entity : world.loadedEntityList) {
                if (entity instanceof EntityAnimation) {
                    EntityAnimation animation = (EntityAnimation) entity;
                    
                    Vec3d newPos = animation.origin.transformPointToFakeWorld(originalPos);
                    Vec3d newLook = animation.origin.transformPointToFakeWorld(originalLook);
                    
                    if (animation.worldBoundingBox.intersects(new AxisAlignedBB(newPos, newLook))) {
                        LittleRayTraceResult tempResult = getTarget(animation.fakeWorld, newPos, newLook, originalPos, originalLook);
                        if (tempResult == null)
                            continue;
                        double tempDistance = newPos.distanceTo(tempResult.getHitVec());
                        if (result == null || tempDistance < distance) {
                            result = tempResult;
                            distance = tempDistance;
                        }
                    }
                }
            }
        }
        
        RayTraceResult tempResult = world.rayTraceBlocks(pos, look);
        if (tempResult == null || tempResult.typeOfHit != RayTraceResult.Type.BLOCK)
            return result;
        tempResult.hitInfo = world;
        if (result == null || pos.distanceTo(tempResult.hitVec) < distance)
            return new LittleRayTraceResult(tempResult, world);
        return result;
    }
    
    public boolean onRightClick(@Nullable EntityPlayer player, Vec3d pos, Vec3d look) {
        LittleRayTraceResult result = getRayTraceResult(pos, look);
        if (result == null)
            return false;
        
        Vec3d hit = result.getHitVec();
        if (player != null && player.getHeldItemMainhand().getItem() instanceof ItemLittleWrench) {
            ((ItemLittleWrench) player.getHeldItemMainhand().getItem())
                    .onItemUse(player, fakeWorld, result.getBlockPos(), EnumHand.MAIN_HAND, result.result.sideHit, (float) hit.x, (float) hit.y, (float) hit.z);
            return true;
        }
        
        TileEntity te = result.world.getTileEntity(result.getBlockPos());
        IBlockState state = result.world.getBlockState(result.getBlockPos());
        
        return state.getBlock()
                .onBlockActivated(fakeWorld, result.getBlockPos(), state, player, EnumHand.MAIN_HAND, result.result.sideHit, (float) hit.x, (float) hit.y, (float) hit.z);
    }
    
}
