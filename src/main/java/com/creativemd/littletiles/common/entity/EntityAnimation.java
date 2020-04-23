package com.creativemd.littletiles.common.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.vecmath.Vector3d;

import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.math.box.OrientatedBoundingBox;
import com.creativemd.creativecore.common.utils.math.collision.CollidingPlane;
import com.creativemd.creativecore.common.utils.math.collision.CollidingPlane.PushCache;
import com.creativemd.creativecore.common.utils.math.collision.CollisionCoordinator;
import com.creativemd.creativecore.common.utils.math.vec.ChildVecOrigin;
import com.creativemd.creativecore.common.utils.math.vec.IVecOrigin;
import com.creativemd.creativecore.common.utils.math.vec.VecUtils;
import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.creativecore.common.world.FakeWorld;
import com.creativemd.creativecore.common.world.IOrientatedWorld;
import com.creativemd.creativecore.common.world.SubWorld;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.world.LittleRenderChunkSuppilier;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.block.BlockTile;
import com.creativemd.littletiles.common.item.ItemLittleWrench;
import com.creativemd.littletiles.common.structure.IAnimatedStructure;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.animation.AnimationState;
import com.creativemd.littletiles.common.structure.relative.StructureAbsolute;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.identifier.LittleIdentifierStructureAbsolute;
import com.creativemd.littletiles.common.tile.math.vec.LittleAbsoluteVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.place.Placement;
import com.creativemd.littletiles.common.util.place.PlacementHelper;
import com.creativemd.littletiles.common.util.place.PlacementMode;
import com.creativemd.littletiles.common.util.place.PlacementResult;
import com.creativemd.littletiles.common.util.vec.LittleRayTraceResult;
import com.creativemd.littletiles.common.util.vec.LittleTransformation;
import com.creativemd.littletiles.common.world.WorldAnimationHandler;
import com.google.common.base.Predicate;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityAnimation extends Entity {
	
	protected static final Predicate<Entity> noAnimation = (x) -> !(x instanceof EntityAnimation);
	
	// ================Constructors================
	
	public EntityAnimation(World worldIn) {
		super(worldIn);
	}
	
	public EntityAnimation(World world, CreativeWorld fakeWorld, EntityAnimationController controller, BlockPos absolutePreviewPos, UUID uuid, StructureAbsolute center, LittleIdentifierStructureAbsolute identifier) {
		this(world);
		
		this.structureIdentifier = identifier;
		try {
			if (identifier == null)
				this.structure = null;
			else
				this.structure = LittleAction.getTile(fakeWorld, identifier).connection.getStructureWithoutLoading();
		} catch (LittleActionException e) {
			throw new RuntimeException(e);
		}
		
		this.controller = controller;
		this.controller.setParent(this);
		
		this.absolutePreviewPos = absolutePreviewPos;
		setFakeWorld(fakeWorld);
		this.entityUniqueID = uuid;
		this.cachedUniqueIdString = this.entityUniqueID.toString();
		
		setCenter(center);
		
		updateWorldCollision();
		
		setPosition(center.baseOffset.getX(), center.baseOffset.getY(), center.baseOffset.getZ());
		
		addDoor();
		preventPush = true;
		updateTickState();
		updateBoundingBox();
		this.initalOffX = origin.offX();
		this.initalOffY = origin.offY();
		this.initalOffZ = origin.offZ();
		this.initalRotX = origin.rotX();
		this.initalRotY = origin.rotY();
		this.initalRotZ = origin.rotZ();
		preventPush = false;
		
		origin.tick();
	}
	
	public Entity getAbsoluteParent() {
		if (world instanceof SubWorld)
			return ((EntityAnimation) ((SubWorld) world).parent).getAbsoluteParent();
		return this;
	}
	
	public void setFakeWorld(CreativeWorld fakeWorld) {
		this.fakeWorld = fakeWorld;
		this.fakeWorld.parent = this;
		
		if (world.isRemote && this.fakeWorld.renderChunkSupplier == null)
			this.fakeWorld.renderChunkSupplier = new LittleRenderChunkSuppilier();
		
	}
	
	public boolean shouldAddDoor() {
		return !(world instanceof FakeWorld) && !(world instanceof SubWorld && ((SubWorld) world).getRealWorld() instanceof FakeWorld);
	}
	
	public World getRealWorld() {
		if (world instanceof SubWorld)
			return ((SubWorld) world).getRealWorld();
		return world;
	}
	
	public boolean isDoorAdded() {
		return addedDoor;
	}
	
	public void addDoor() {
		if (!shouldAddDoor())
			return;
		if (!addedDoor) {
			WorldAnimationHandler.getHandler(world).createDoor(this);
			addedDoor = true;
		}
	}
	
	public void markRemoved() {
		isDead = true;
		addedDoor = false;
		if (fakeWorld == null || fakeWorld.loadedEntityList == null)
			return;
		for (Entity entity : fakeWorld.loadedEntityList)
			if (entity instanceof EntityAnimation)
				((EntityAnimation) entity).markRemoved();
		if (world.isRemote)
			getRenderChunkSuppilier().unloadRenderCache();
	}
	
	@Override
	protected void entityInit() {
		addDoor();
	}
	
	// ================World Data================
	
	public double initalOffX;
	public double initalOffY;
	public double initalOffZ;
	public double initalRotX;
	public double initalRotY;
	public double initalRotZ;
	
	public CreativeWorld fakeWorld;
	public IVecOrigin origin;
	public EntityAnimationController controller;
	
	public boolean enteredAsChild = false;
	
	// ================Axis================
	
	public void setCenter(StructureAbsolute center) {
		this.center = center;
		this.fakeWorld.setOrigin(center.rotationCenter);
		this.origin = this.fakeWorld.getOrigin();
		if (fakeWorld.loadedEntityList.isEmpty())
			return;
		for (Entity entity : fakeWorld.loadedEntityList)
			if (entity instanceof EntityAnimation)
				((ChildVecOrigin) ((EntityAnimation) entity).origin).parent = this.origin;
	}
	
	public void setCenterVec(LittleAbsoluteVec axis, LittleVec additional) {
		setCenter(new StructureAbsolute(axis, additional));
	}
	
	public void setParentWorld(World world) {
		this.enteredAsChild = this.world instanceof CreativeWorld && !(world instanceof CreativeWorld);
		this.world = world;
		if (fakeWorld instanceof SubWorld)
			((SubWorld) fakeWorld).parentWorld = world;
		this.fakeWorld.setOrigin(center.rotationCenter);
		this.origin = this.fakeWorld.getOrigin();
		hasOriginChanged = true;
	}
	
	public LittleStructure structure;
	public LittleIdentifierStructureAbsolute structureIdentifier;
	public StructureAbsolute center;
	public BlockPos absolutePreviewPos;
	
	// ================Collision================
	
	public boolean preventPush = false;
	
	/** Is true when animation moves other entities */
	public boolean noCollision = false;
	
	public AABBCombiner collisionBoxWorker;
	
	/** Static not affected by direction or entity offset */
	public List<OrientatedBoundingBox> worldCollisionBoxes;
	
	/** Static not affected by direction or entity offset */
	public OrientatedBoundingBox worldBoundingBox;
	
	/** Should be called if the world of the animation will be modified (Currently
	 * not possible) */
	public void updateWorldCollision() {
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double minZ = Double.MAX_VALUE;
		double maxX = -Double.MAX_VALUE;
		double maxY = -Double.MAX_VALUE;
		double maxZ = -Double.MAX_VALUE;
		
		worldCollisionBoxes = new ArrayList<>();
		
		for (Iterator<TileEntity> iterator = fakeWorld.loadedTileEntityList.iterator(); iterator.hasNext();) {
			TileEntity tileEntity = iterator.next();
			
			if (tileEntity instanceof TileEntityLittleTiles) {
				TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
				if (te.isEmpty())
					continue;
				AxisAlignedBB bb = te.getSelectionBox();
				minX = Math.min(minX, bb.minX);
				minY = Math.min(minY, bb.minY);
				minZ = Math.min(minZ, bb.minZ);
				maxX = Math.max(maxX, bb.maxX);
				maxY = Math.max(maxY, bb.maxY);
				maxZ = Math.max(maxZ, bb.maxZ);
				
				ArrayList<AxisAlignedBB> boxes = new ArrayList<>();
				
				for (LittleTile tile : te) {
					List<LittleBox> tileBoxes = tile.getCollisionBoxes();
					for (LittleBox box : tileBoxes) {
						boxes.add(box.getBox(te.getContext(), te.getPos()));
					}
				}
				
				// BoxUtils.compressBoxes(boxes, 0.0F);
				
				for (AxisAlignedBB box : boxes) {
					worldCollisionBoxes.add(new OrientatedBoundingBox(origin, box));
				}
			}
		}
		
		fakeWorld.hasChanged = false;
		hasOriginChanged = true;
		
		collisionBoxWorker = new AABBCombiner(worldCollisionBoxes, 0);
		if (minX == Double.MAX_VALUE)
			worldBoundingBox = new OrientatedBoundingBox(origin, 0, 0, 0, 1, 1, 1);
		else
			worldBoundingBox = new OrientatedBoundingBox(origin, minX, minY, minZ, maxX, maxY, maxZ);
	}
	
	private static double minIgnore(double par1, double par2) {
		if (Math.abs(par2) < Math.abs(par1))
			return par2;
		return par1;
	}
	
	private static double maxIgnore(double par1, double par2) {
		if (Math.abs(par2) > Math.abs(par1))
			return par2;
		return par1;
	}
	
	public void moveAndRotateAnimation(double x, double y, double z, double rotX, double rotY, double rotZ) {
		if (x == 0 && y == 0 && z == 0 && rotX == 0 && rotY == 0 && rotZ == 0)
			return;
		
		CollisionCoordinator coordinator = new CollisionCoordinator(x, y, z, rotX, rotY, rotZ, origin, origin);
		if (LittleTiles.CONFIG.general.enableAnimationCollision)
			moveAndRotateAnimation(coordinator);
		coordinator.move();
	}
	
	public void moveAndRotateAnimation(CollisionCoordinator coordinator) {
		if (preventPush)
			return;
		
		noCollision = true;
		
		World world = getRealWorld();
		List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, coordinator.computeSurroundingBox(worldBoundingBox), EntityAnimation.noAnimation);
		if (!entities.isEmpty()) {
			
			// PHASE ONE
			// Gather all affected boxes
			List<AxisAlignedBB> surroundingBoxes = new ArrayList<>(worldCollisionBoxes.size());
			for (OrientatedBoundingBox box : worldCollisionBoxes) {
				if (box.cache == null)
					box.buildCache();
				box.cache.reset();
				
				surroundingBoxes.add(coordinator.computeSurroundingBox(box));
			}
			
			// PHASE TWO
			// Move entities by their center
			PushCache[] caches = new PushCache[entities.size()];
			for (int j = 0; j < entities.size(); j++) {
				Entity entity = entities.get(j);
				
				AxisAlignedBB entityBB = entity.getEntityBoundingBox();
				Vector3d center = new Vector3d(entityBB.minX + (entityBB.maxX - entityBB.minX) * 0.5D, entityBB.minY + (entityBB.maxY - entityBB.minY) * 0.5D, entityBB.minZ + (entityBB.maxZ - entityBB.minZ) * 0.5D);
				double radius = VecUtils.distanceToSquared(entityBB.minX, entityBB.minY, entityBB.minZ, center);
				
				Double t = null;
				OrientatedBoundingBox pushingBox = null;
				EnumFacing facing = null;
				
				checking_all_boxes:
				for (int i = 0; i < surroundingBoxes.size(); i++) {
					if (surroundingBoxes.get(i).intersects(entityBB)) {
						// Check for earliest hit
						OrientatedBoundingBox box = worldCollisionBoxes.get(i);
						
						if (!box.cache.isCached())
							box.cache.planes = CollidingPlane.getPlanes(box, box.cache, coordinator);
						
						// Binary search
						for (CollidingPlane plane : box.cache.planes) {
							Double tempT = plane.binarySearch(t, entityBB, radius, center, coordinator);
							if (tempT != null) {
								t = tempT;
								pushingBox = box;
								facing = plane.facing;
								if (t == 0)
									break checking_all_boxes;
							}
						}
					}
				}
				
				// Applying found t
				if (t != null) {
					PushCache cache = new PushCache();
					//cache.facing = facing;
					
					Vector3d newCenter = new Vector3d(center);
					coordinator.transform(newCenter, 1 - t);
					
					cache.pushBox = pushingBox;
					cache.entityBox = entityBB.offset(newCenter.x - center.x, newCenter.y - center.y, newCenter.z - center.z);
					caches[j] = cache;
				}
			}
			
			coordinator.move();
			
			// PHASE THREE
			for (int i = 0; i < entities.size(); i++) {
				Entity entity = entities.get(i);
				PushCache cache = caches[i];
				
				if (cache == null) {
					cache = new PushCache();
					cache.entityBox = entity.getEntityBoundingBox();
				}
				
				cache.entityBoxOrientated = coordinator.origin.getOrientatedBox(cache.entityBox);
				Vector3d center = new Vector3d(cache.entityBox.minX + (cache.entityBox.maxX - cache.entityBox.minX) * 0.5D, cache.entityBox.minY + (cache.entityBox.maxY - cache.entityBox.minY) * 0.5D, cache.entityBox.minZ + (cache.entityBox.maxZ - cache.entityBox.minZ) * 0.5D);
				coordinator.origin.transformPointToFakeWorld(center);
				
				Axis one = null;
				Axis two = null;
				
				boolean ignoreOne = false;
				Boolean positiveOne = null;
				boolean ignoreTwo = false;
				Boolean positiveTwo = null;
				
				double maxVolume = 0;
				
				List<OrientatedBoundingBox> intersecting = new ArrayList<>();
				List<EnumFacing> intersectingFacing = new ArrayList<>();
				
				for (OrientatedBoundingBox box : worldCollisionBoxes) {
					if (box == cache.pushBox || box.intersects(cache.entityBoxOrientated)) {
						//box.cache.planes = CollidingPlane.getPlanes(box, box.cache, coordinator);
						
						EnumFacing facing = CollidingPlane.getDirection(coordinator, box, center);
						if (facing == null || (!coordinator.hasRotation && (!coordinator.hasTranslation || RotationUtils.getOffset(RotationUtils.get(facing.getAxis(), coordinator.translation)) != facing.getAxisDirection())))
							continue;
						
						double intersectingVolume = box.getIntersectionVolume(cache.entityBoxOrientated);
						
						if (maxVolume == 0 || intersectingVolume > maxVolume) {
							maxVolume = intersectingVolume;
							cache.facing = facing;
						}
						
						intersecting.add(box);
						intersectingFacing.add(facing);
					}
				}
				
				if (intersecting.isEmpty())
					continue;
				
				one = RotationUtils.getDifferentAxisFirst(cache.facing.getAxis());
				two = RotationUtils.getDifferentAxisSecond(cache.facing.getAxis());
				
				positiveOne = null;
				positiveTwo = null;
				
				for (EnumFacing facing : intersectingFacing) {
					
					if (!ignoreOne && facing.getAxis() == one) {
						if (positiveOne == null)
							positiveOne = facing.getAxisDirection() == AxisDirection.POSITIVE;
						else if (facing.getAxisDirection() == AxisDirection.POSITIVE != positiveOne)
							ignoreOne = true;
					} else if (!ignoreTwo && facing.getAxis() == two) {
						if (positiveTwo == null)
							positiveTwo = facing.getAxisDirection() == AxisDirection.POSITIVE;
						else if (facing.getAxisDirection() == AxisDirection.POSITIVE != positiveTwo)
							ignoreTwo = true;
					}
					
					if (ignoreOne && ignoreTwo)
						break;
				}
				
				// Now things are ready. Go through all intersecting ones and push the box out
				Vector3d pushVec = new Vector3d();
				RotationUtils.setValue(pushVec, cache.facing.getAxisDirection().getOffset(), cache.facing.getAxis());
				if (!ignoreOne && positiveOne != null)
					RotationUtils.setValue(pushVec, positiveOne ? 1 : -1, one);
				if (!ignoreTwo && positiveTwo != null)
					RotationUtils.setValue(pushVec, positiveTwo ? 1 : -1, two);
				
				double scale = 0;
				
				for (int j = 0; j < intersecting.size(); j++) {
					EnumFacing facing = intersectingFacing.get(j);
					
					if ((ignoreOne && facing.getAxis() == one) || (ignoreTwo && facing.getAxis() == two))
						continue;
					
					scale = intersecting.get(j).getPushOutScale(scale, cache.entityBoxOrientated, pushVec);
				}
				
				boolean collidedHorizontally = entity.collidedHorizontally;
				boolean collidedVertically = entity.collidedVertically;
				boolean onGround = entity.onGround;
				
				AxisAlignedBB originalBox = entity.getEntityBoundingBox();
				
				Vector3d rotatedVec = new Vector3d(pushVec);
				coordinator.origin.rotation().transform(rotatedVec);
				
				double moveX = cache.entityBox.minX - originalBox.minX + rotatedVec.x * scale;
				double moveY = cache.entityBox.minY - originalBox.minY + rotatedVec.y * scale;
				double moveZ = cache.entityBox.minZ - originalBox.minZ + rotatedVec.z * scale;
				
				entity.move(MoverType.SELF, moveX, moveY, moveZ);
				
				if (entity instanceof EntityPlayerMP)
					WorldAnimationHandler.setPushedByDoor((EntityPlayerMP) entity);
				
				if (LittleTiles.CONFIG.general.enableCollisionMotion) {
					entity.motionX += moveX;
					entity.motionY += moveY;
					entity.motionZ += moveZ;
				}
				
				if (moveX != 0 || moveZ != 0)
					collidedHorizontally = true;
				if (moveY != 0) {
					collidedVertically = true;
					onGround = true;
				}
				
				entity.collidedHorizontally = collidedHorizontally;
				entity.collidedVertically = collidedVertically;
				entity.onGround = onGround;
				entity.collided = collidedHorizontally || collidedVertically;
				
			}
			
			for (OrientatedBoundingBox box : worldCollisionBoxes)
				box.cache.reset();
		}
		
		for (int i = 0; i < fakeWorld.loadedEntityList.size(); i++) {
			Entity entity = fakeWorld.loadedEntityList.get(i);
			if (entity instanceof EntityAnimation) {
				coordinator.reset(((EntityAnimation) entity).origin);
				((EntityAnimation) entity).moveAndRotateAnimation(coordinator);
			}
		}
		
		noCollision = false;
	}
	
	// ================Rendering================
	
	@SideOnly(Side.CLIENT)
	public boolean spawnedInWorld;
	
	@SideOnly(Side.CLIENT)
	public void createClient() {
		
	}
	
	public LittleRenderChunkSuppilier getRenderChunkSuppilier() {
		return (LittleRenderChunkSuppilier) fakeWorld.renderChunkSupplier;
	}
	
	// ================Ticking================
	
	protected void handleForces() {
		motionX = 0;
		motionY = 0;
		motionZ = 0;
	}
	
	protected boolean hasOriginChanged = false;
	
	protected void markOriginChange() {
		hasOriginChanged = true;
		if (fakeWorld.loadedEntityList.isEmpty())
			return;
		for (Entity entity : fakeWorld.loadedEntityList)
			if (entity instanceof EntityAnimation)
				((EntityAnimation) entity).markOriginChange();
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
		moveAndRotateAnimation(offset.x - origin.offX(), offset.y - origin.offY(), offset.z - origin.offZ(), rotation.x - origin.rotX(), rotation.y - origin.rotY(), rotation.z - origin.rotZ());
		origin.tick();
		hasOriginChanged = true;
	}
	
	public void onTick() {
		if (controller == null)
			return;
		AnimationState state = controller.tick();
		Vector3d offset = state.getOffset();
		Vector3d rotation = state.getRotation();
		moveAndRotateAnimation(offset.x - origin.offX(), offset.y - origin.offY(), offset.z - origin.offZ(), rotation.x - origin.rotX(), rotation.y - origin.rotY(), rotation.z - origin.rotZ());
	}
	
	private boolean addedDoor;
	
	@Override
	public void onUpdate() {
		
	}
	
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
	}
	
	// ================Overridden================
	
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
	public AxisAlignedBB getCollisionBox(Entity entityIn) {
		return null;
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox() {
		return null;
	}
	
	@Override
	public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
		
		return true;
	}
	
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
		if (player != null && player.getHeldItemMainhand().getItem() instanceof ItemLittleWrench) {
			ItemLittleWrench.rightClickAnimation(this, player);
			return true;
		}
		
		LittleRayTraceResult result = getRayTraceResult(pos, look);
		if (result == null)
			return false;
		
		TileEntity te = result.world.getTileEntity(result.getBlockPos());
		IBlockState state = result.world.getBlockState(result.getBlockPos());
		Vec3d hit = result.getHitVec();
		return state.getBlock().onBlockActivated(fakeWorld, result.getBlockPos(), state, player, EnumHand.MAIN_HAND, result.result.sideHit, (float) hit.x, (float) hit.y, (float) hit.z);
	}
	
	@Override
	public boolean isBurning() {
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderOnFire() {
		return false;
	}
	
	// ================Saving & Loading================
	
	public void transformWorld(LittleTransformation transformation) {
		if (!structure.load() || !structure.loadChildren() || !structure.loadParent())
			return;
		LittleAbsolutePreviews previews = structure.getAbsolutePreviewsSameWorldOnly(transformation.center);
		transformation.transform(previews);
		
		List<BlockPos> positions = new ArrayList<>();
		for (TileEntity te : fakeWorld.loadedTileEntityList) {
			if (te instanceof TileEntityLittleTiles) {
				((TileEntityLittleTiles) te).updateTilesSecretly((x) -> x.clear());
				positions.add(te.getPos());
			}
		}
		
		for (BlockPos pos : positions) {
			fakeWorld.setBlockToAir(pos);
			fakeWorld.removeTileEntity(pos);
		}
		
		if (world.isRemote)
			getRenderChunkSuppilier().unloadRenderCache();
		
		Placement placement = new Placement(null, PlacementHelper.getAbsolutePreviews(fakeWorld, previews, previews.pos, PlacementMode.all));
		PlacementResult result = placement.tryPlace();
		
		int childId = this.structure.parent.getChildID();
		LittleStructure parentStructure = this.structure.parent.getStructure(fakeWorld);
		this.structure = result.parentStructure;
		((IAnimatedStructure) this.structure).setAnimation(this);
		parentStructure.updateChildConnection(childId, this.structure);
		this.structure.updateParentConnection(childId, parentStructure);
		
		this.structure.transformAnimation(transformation);
		this.controller.transform(transformation);
		absolutePreviewPos = transformation.transform(absolutePreviewPos);
		
		updateWorldCollision();
		updateBoundingBox();
		updateTickState();
	}
	
	@Deprecated
	private LittleStructure searchForParent() {
		for (TileEntity te : fakeWorld.loadedTileEntityList) {
			if (te instanceof TileEntityLittleTiles) {
				for (LittleTile tile : (TileEntityLittleTiles) te) {
					if (!tile.connection.isLink()) {
						LittleStructure structure = tile.connection.getStructureWithoutLoading();
						if (structure.parent == null || structure.parent.isLinkToAnotherWorld())
							return structure;
					}
				}
			}
		}
		throw new RuntimeException("Could not find parent structure!");
	}
	
	@Override
	public void onRemovedFromWorld() {
		super.onRemovedFromWorld();
		markRemoved();
	}
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
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
			setCenterVec(new LittleAbsoluteVec("axis", compound), new LittleVec("additional", compound));
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
				te.updateCustomRenderer();
		}
		
		fakeWorld.loadedTileEntityList.removeIf(x -> x.isInvalid());
		
		int[] array = compound.getIntArray("previewPos");
		if (array.length == 3)
			absolutePreviewPos = new BlockPos(array[0], array[1], array[2]);
		else
			absolutePreviewPos = center.baseOffset;
		
		if (compound.hasKey("identifier")) {
			structureIdentifier = new LittleIdentifierStructureAbsolute(compound.getCompoundTag("identifier"));
			try {
				this.structure = LittleAction.getTile(fakeWorld, structureIdentifier).connection.getStructureWithoutLoading();
			} catch (LittleActionException e) {
				throw new RuntimeException(e);
			}
		} else {
			structure = searchForParent();
			structureIdentifier = structure.getAbsoluteIdentifier();
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
	protected void writeEntityToNBT(NBTTagCompound compound) {
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
		
		compound.setTag("identifier", structureIdentifier.writeToNBT(new NBTTagCompound()));
		
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
	
}
