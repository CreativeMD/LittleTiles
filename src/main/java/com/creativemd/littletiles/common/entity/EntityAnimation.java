package com.creativemd.littletiles.common.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.math.box.BoxPlane;
import com.creativemd.creativecore.common.utils.math.box.BoxUtils;
import com.creativemd.creativecore.common.utils.math.box.CollidingPlane;
import com.creativemd.creativecore.common.utils.math.box.CollidingPlane.PushCache;
import com.creativemd.creativecore.common.utils.math.box.OrientatedBoundingBox;
import com.creativemd.creativecore.common.utils.math.vec.ChildVecOrigin;
import com.creativemd.creativecore.common.utils.math.vec.IVecOrigin;
import com.creativemd.creativecore.common.utils.math.vec.MatrixUtils;
import com.creativemd.creativecore.common.utils.math.vec.MatrixUtils.MatrixLookupTable;
import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.creativecore.common.world.FakeWorld;
import com.creativemd.creativecore.common.world.SubWorld;
import com.creativemd.littletiles.client.render.LittleRenderChunkSuppilier;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceStack;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceStack.LittlePlaceResult;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.events.LittleDoorHandler;
import com.creativemd.littletiles.common.items.ItemLittleWrench;
import com.creativemd.littletiles.common.structure.IAnimatedStructure;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.relative.StructureAbsolute;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleAbsolutePreviewsStructure;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileIdentifierStructureAbsolute;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.animation.AnimationState;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;
import com.creativemd.littletiles.common.utils.vec.LittleRayTraceResult;
import com.creativemd.littletiles.common.utils.vec.LittleTransformation;
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
	
	protected static final Predicate<Entity> noAnimation = new Predicate<Entity>() {
		
		@Override
		public boolean apply(Entity input) {
			return !(input instanceof EntityAnimation);
		}
		
	};
	
	// ================Constructors================
	
	public EntityAnimation(World worldIn) {
		super(worldIn);
	}
	
	public EntityAnimation(World world, CreativeWorld fakeWorld, EntityAnimationController controller, BlockPos absolutePreviewPos, UUID uuid, StructureAbsolute center, LittleTileIdentifierStructureAbsolute identifier) {
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
		onUpdateForReal();
		this.initalOffX = origin.offX();
		this.initalOffY = origin.offY();
		this.initalOffZ = origin.offZ();
		this.initalRotX = origin.rotX();
		this.initalRotY = origin.rotY();
		this.initalRotZ = origin.rotZ();
		preventPush = false;
		
		origin.tick();
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
			return ((SubWorld) world).getParent();
		return world;
	}
	
	public boolean isDoorAdded() {
		return addedDoor;
	}
	
	public void addDoor() {
		if (!shouldAddDoor())
			return;
		if (!addedDoor) {
			LittleDoorHandler.getHandler(world).createDoor(this);
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
	
	public void setCenterVec(LittleTilePos axis, LittleTileVec additional) {
		setCenter(new StructureAbsolute(axis, additional));
	}
	
	public void setParentWorld(World world) {
		this.world = world;
		if (fakeWorld instanceof SubWorld)
			((SubWorld) fakeWorld).parentWorld = world;
		this.fakeWorld.setOrigin(center.rotationCenter);
		this.origin = this.fakeWorld.getOrigin();
	}
	
	public LittleStructure structure;
	public LittleTileIdentifierStructureAbsolute structureIdentifier;
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
				
				for (Iterator<LittleTile> iterator2 = te.getTiles().iterator(); iterator2.hasNext();) {
					LittleTile tile = iterator2.next();
					List<LittleTileBox> tileBoxes = tile.getCollisionBoxes();
					for (LittleTileBox box : tileBoxes) {
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
		World world = getRealWorld();
		
		boolean moved = false;
		if (!preventPush) {
			// Create rotation matrix to transform to caclulate surrounding box
			Matrix3d rotationX = rotX != 0 ? MatrixUtils.createRotationMatrixX(rotX) : null;
			Matrix3d rotationY = rotY != 0 ? MatrixUtils.createRotationMatrixY(rotY) : null;
			Matrix3d rotationZ = rotZ != 0 ? MatrixUtils.createRotationMatrixZ(rotZ) : null;
			Vector3d translation = x != 0 || y != 0 || z != 0 ? new Vector3d(x, y, z) : null;
			
			if (rotationX != null || rotationY != null || rotationZ != null || translation != null) {
				AxisAlignedBB moveBB = BoxUtils.getRotatedSurrounding(worldBoundingBox, origin, rotationX, rotX, rotationY, rotY, rotationZ, rotZ, translation);
				
				noCollision = true;
				
				List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, moveBB, EntityAnimation.noAnimation);
				if (!entities.isEmpty()) {
					// PHASE ONE
					List<AxisAlignedBB> surroundingBoxes = new ArrayList<>(worldCollisionBoxes.size());
					for (OrientatedBoundingBox box : worldCollisionBoxes) {
						
						if (box.cache == null)
							box.buildCache();
						box.cache.reset();
						
						surroundingBoxes.add(BoxUtils.getRotatedSurrounding(box, origin, rotationX, rotX, rotationY, rotY, rotationZ, rotZ, translation));
					}
					
					// PHASE TWO
					MatrixLookupTable table = new MatrixLookupTable(x, y, z, rotX, rotY, rotZ, center.rotationCenter, origin);
					
					PushCache[] caches = new PushCache[entities.size()];
					
					for (int j = 0; j < entities.size(); j++) {
						Entity entity = entities.get(j);
						AxisAlignedBB entityBox = entity.getEntityBoundingBox();
						Vector3d center = new Vector3d(entityBox.minX + (entityBox.maxX - entityBox.minX) * 0.5D, entityBox.minY + (entityBox.maxY - entityBox.minY) * 0.5D, entityBox.minZ + (entityBox.maxZ - entityBox.minZ) * 0.5D);
						
						Vector3d temp = new Vector3d(entityBox.minX, entityBox.minY, entityBox.minZ);
						temp.sub(center);
						double radius = temp.lengthSquared();
						
						origin.transformPointToFakeWorld(center);
						
						Double t = null;
						OrientatedBoundingBox pushingBox = null;
						EnumFacing facing = null;
						
						checking_all_boxes: for (int i = 0; i < surroundingBoxes.size(); i++) {
							if (surroundingBoxes.get(i).intersects(entityBox)) {
								// Check for earliest hit
								OrientatedBoundingBox box = worldCollisionBoxes.get(i);
								
								if (!box.cache.isCached())
									box.cache.planes = CollidingPlane.getPlanes(box, box.cache, table);
								
								// Binary search
								for (CollidingPlane plane : box.cache.planes) {
									Double tempT = plane.binarySearch(t, entityBox, radius, center, table);
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
							cache.facing = facing;
							
							Vector3d newCenter = new Vector3d(center);
							table.transform(newCenter, 1 - t);
							
							origin.transformPointToWorld(center);
							origin.transformPointToWorld(newCenter);
							
							cache.pushBox = pushingBox;
							cache.entityBox = entityBox.offset(newCenter.x - center.x, newCenter.y - center.y, newCenter.z - center.z);
							caches[j] = cache;
						}
					}
					
					origin.offX(origin.offX() + x);
					origin.offY(origin.offY() + y);
					origin.offZ(origin.offZ() + z);
					
					origin.rotX(origin.rotX() + rotX);
					origin.rotY(origin.rotY() + rotY);
					origin.rotZ(origin.rotZ() + rotZ);
					moved = true;
					
					// PHASE THREE
					for (int i = 0; i < entities.size(); i++) {
						Entity entity = entities.get(i);
						PushCache cache = caches[i];
						
						boolean cached = cache != null;
						if (!cached) {
							cache = new PushCache();
							cache.entityBox = entity.getEntityBoundingBox();
						}
						
						Vector3d[] corners = BoxUtils.getCorners(cache.entityBox);
						
						double minX = Double.MAX_VALUE;
						double minY = Double.MAX_VALUE;
						double minZ = Double.MAX_VALUE;
						double maxX = -Double.MAX_VALUE;
						double maxY = -Double.MAX_VALUE;
						double maxZ = -Double.MAX_VALUE;
						
						for (int h = 0; h < corners.length; h++) {
							Vector3d vec = corners[h];
							vec.sub(origin.translation());
							
							vec.sub(center.rotationCenter);
							origin.rotationInv().transform(vec);
							vec.add(center.rotationCenter);
							
							minX = Math.min(minX, vec.x);
							minY = Math.min(minY, vec.y);
							minZ = Math.min(minZ, vec.z);
							maxX = Math.max(maxX, vec.x);
							maxY = Math.max(maxY, vec.y);
							maxZ = Math.max(maxZ, vec.z);
						}
						
						OrientatedBoundingBox fakeBox = new OrientatedBoundingBox(origin, minX, minY, minZ, maxX, maxY, maxZ);
						Vector3d center = fakeBox.getCenter3d();
						
						Axis one = cached ? RotationUtils.getDifferentAxisFirst(cache.facing.getAxis()) : null;
						Axis two = cached ? RotationUtils.getDifferentAxisSecond(cache.facing.getAxis()) : null;
						
						boolean ignoreOne = false;
						Boolean positiveOne = null;
						boolean ignoreTwo = false;
						Boolean positiveTwo = null;
						
						double maxVolume = 0;
						
						List<OrientatedBoundingBox> intersecting = new ArrayList<>();
						List<EnumFacing> intersectingFacing = new ArrayList<>();
						
						if (cached) {
							intersecting.add(cache.pushBox);
							intersectingFacing.add(cache.facing);
						}
						
						for (OrientatedBoundingBox box : worldCollisionBoxes) {
							if ((!cached || box != cache.pushBox) && box.intersects(fakeBox)) {
								if (!box.cache.isCached())
									box.cache.planes = CollidingPlane.getPlanes(box, box.cache, table);
								
								boolean add = !cached;
								EnumFacing facing = CollidingPlane.getDirection(box, box.cache.planes, center);
								
								if (facing == null || (!table.hasOneRotation && (!table.hasTranslation || RotationUtils.get(facing.getAxis(), translation) == 0)))
									continue;
								
								if (cached) {
									if (facing == cache.facing)
										add = true;
									else if (!ignoreOne && facing.getAxis() == one) {
										add = true;
										if (positiveOne == null)
											positiveOne = facing.getAxisDirection() == AxisDirection.POSITIVE;
										else if (facing.getAxisDirection() == AxisDirection.POSITIVE != positiveOne) {
											ignoreOne = true;
											add = false;
										}
									} else if (!ignoreTwo && facing.getAxis() == two) {
										add = true;
										if (positiveTwo == null)
											positiveTwo = facing.getAxisDirection() == AxisDirection.POSITIVE;
										else if (facing.getAxisDirection() == AxisDirection.POSITIVE != positiveTwo) {
											ignoreTwo = true;
											add = false;
										}
									}
								}
								
								if (add) {
									double intersectingVolume = box.getIntersectionVolume(fakeBox);
									
									if (intersectingVolume > maxVolume) {
										cache.pushBox = box;
										maxVolume = intersectingVolume;
										cache.facing = facing;
									}
									
									intersecting.add(box);
									intersectingFacing.add(facing);
								}
							}
						}
						
						if (intersecting.isEmpty())
							continue;
						
						if (!cached) {
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
						}
						
						// Now things are ready. Go through all intersecting ones and push the box out
						Vector3d pushVec = new Vector3d();
						RotationUtils.setValue(pushVec, cache.facing.getAxisDirection().getOffset(), cache.facing.getAxis());
						if (!ignoreOne && positiveOne != null)
							RotationUtils.setValue(pushVec, positiveOne ? 1 : -1, one);
						if (!ignoreTwo && positiveTwo != null)
							RotationUtils.setValue(pushVec, positiveTwo ? 1 : -1, two);
						
						Vector3d pushInv = new Vector3d(-pushVec.x, -pushVec.y, -pushVec.z);
						
						Vector3d rotatedVec = new Vector3d(pushVec);
						origin.rotation().transform(rotatedVec);
						
						BoxPlane xPlane = BoxPlane.createOppositePlane(Axis.X, rotatedVec, corners);
						BoxPlane yPlane = BoxPlane.createOppositePlane(Axis.Y, rotatedVec, corners);
						BoxPlane zPlane = BoxPlane.createOppositePlane(Axis.Z, rotatedVec, corners);
						
						double scale = 0;
						
						for (int j = 0; j < intersecting.size(); j++) {
							
							EnumFacing facing = intersectingFacing.get(j);
							
							if ((ignoreOne && facing.getAxis() == one) || (ignoreTwo && facing.getAxis() == two))
								continue;
							
							scale = intersecting.get(j).getPushOutScale(scale, fakeBox, cache.entityBox, pushVec, pushInv, xPlane, yPlane, zPlane);
						}
						
						boolean collidedHorizontally = entity.collidedHorizontally;
						boolean collidedVertically = entity.collidedVertically;
						boolean onGround = entity.onGround;
						
						AxisAlignedBB originalBox = entity.getEntityBoundingBox();
						
						double moveX = cache.entityBox.minX - originalBox.minX + rotatedVec.x * scale;
						double moveY = cache.entityBox.minY - originalBox.minY + rotatedVec.y * scale;
						double moveZ = cache.entityBox.minZ - originalBox.minZ + rotatedVec.z * scale;
						
						entity.move(MoverType.PISTON, moveX, moveY, moveZ);
						
						if (entity instanceof EntityPlayerMP)
							LittleDoorHandler.setPushedByDoor((EntityPlayerMP) entity);
						
						/* entity.motionX += moveX; entity.motionY += moveY; entity.motionZ += moveZ; */
						
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
				}
				
				noCollision = false;
			}
		}
		
		if (!moved) {
			origin.offX(origin.offX() + x);
			origin.offY(origin.offY() + y);
			origin.offZ(origin.offZ() + z);
			
			origin.rotX(origin.rotX() + rotX);
			origin.rotY(origin.rotY() + rotY);
			origin.rotZ(origin.rotZ() + rotZ);
		}
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
		
		super.onUpdate();
		
		for (int i = 0; i < fakeWorld.loadedEntityList.size(); i++) {
			Entity entity = fakeWorld.loadedEntityList.get(i);
			if (entity instanceof EntityAnimation)
				((EntityAnimation) entity).onUpdateForReal();
		}
		fakeWorld.loadedEntityList.removeIf((Entity x) -> x.isDead);
		
		onTick();
		
		updateBoundingBox();
		
		for (TileEntity te : fakeWorld.loadedTileEntityList) {
			List<LittleTile> updateTiles = ((TileEntityLittleTiles) te).getUpdateTiles();
			if (updateTiles != null && !updateTiles.isEmpty())
				for (LittleTile tile : updateTiles)
					tile.updateEntity();
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
		if (!this.isDead && (!world.isRemote || controller == null || !controller.isWaitingForRender()))
			this.isDead = true;
	}
	
	public void destroyAnimation() {
		this.isDead = true;
	}
	
	@Override
	public boolean canBeCollidedWith() {
		return true;
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
		if (!structure.loadTiles() || !structure.loadChildren() || !structure.loadParent())
			return;
		LittleAbsolutePreviewsStructure previews = structure.getAbsolutePreviewsSameWorldOnly(transformation.center);
		transformation.transform(previews);
		
		List<BlockPos> positions = new ArrayList<>();
		for (TileEntity te : fakeWorld.loadedTileEntityList) {
			if (te instanceof TileEntityLittleTiles) {
				((TileEntityLittleTiles) te).getTiles().clear();
				positions.add(te.getPos());
			}
		}
		
		for (BlockPos pos : positions) {
			fakeWorld.setBlockToAir(pos);
			fakeWorld.removeTileEntity(pos);
		}
		
		if (world.isRemote)
			getRenderChunkSuppilier().unloadRenderCache();
		
		List<PlacePreviewTile> placePreviews = new ArrayList<>();
		previews.getPlacePreviews(placePreviews, null, true, LittleTileVec.ZERO);
		
		HashMap<BlockPos, PlacePreviews> splitted = LittleActionPlaceStack.getSplittedTiles(previews.context, placePreviews, previews.pos);
		
		int childId = this.structure.parent.getChildID();
		LittleStructure parentStructure = this.structure.parent.getStructure(fakeWorld);
		LittlePlaceResult result = LittleActionPlaceStack.placeTilesWithoutPlayer(fakeWorld, previews.context, splitted, previews.getStructure(), PlacementMode.all, previews.pos, null, null, null, null);
		this.structure = result.parentStructure;
		((IAnimatedStructure) this.structure).setAnimation(this);
		parentStructure.updateChildConnection(childId, this.structure);
		this.structure.updateParentConnection(childId, parentStructure);
		
		this.structure.transformAnimation(transformation);
		this.controller.transform(transformation);
		absolutePreviewPos = transformation.transform(absolutePreviewPos);
		
		updateWorldCollision();
		updateBoundingBox();
	}
	
	@Deprecated
	private LittleStructure searchForParent() {
		for (TileEntity te : fakeWorld.loadedTileEntityList) {
			if (te instanceof TileEntityLittleTiles) {
				for (Iterator<LittleTile> iterator = ((TileEntityLittleTiles) te).getTiles().iterator(); iterator.hasNext();) {
					LittleTile tile = iterator.next();
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
		setFakeWorld(compound.getBoolean("subworld") ? SubWorld.createFakeWorld(world) : FakeWorld.createFakeWorld(getCachedUniqueIdString(), world.isRemote));
		
		this.initalOffX = compound.getDouble("initOffX");
		this.initalOffY = compound.getDouble("initOffY");
		this.initalOffZ = compound.getDouble("initOffZ");
		this.initalRotX = compound.getDouble("initRotX");
		this.initalRotY = compound.getDouble("initRotY");
		this.initalRotZ = compound.getDouble("initRotZ");
		
		if (compound.hasKey("axis"))
			setCenterVec(new LittleTilePos("axis", compound), new LittleTileVec("additional", compound));
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
			structureIdentifier = new LittleTileIdentifierStructureAbsolute(compound.getCompoundTag("identifier"));
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
		
		compound.setIntArray("previewPos", new int[] { absolutePreviewPos.getX(), absolutePreviewPos.getY(),
		        absolutePreviewPos.getZ() });
		
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
