package com.creativemd.littletiles.common.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.math.box.BoxPlane;
import com.creativemd.creativecore.common.utils.math.box.BoxUtils;
import com.creativemd.creativecore.common.utils.math.box.CollidingPlane;
import com.creativemd.creativecore.common.utils.math.box.CollidingPlane.PushCache;
import com.creativemd.creativecore.common.utils.math.box.OrientatedBoundingBox;
import com.creativemd.creativecore.common.utils.math.vec.MatrixUtils;
import com.creativemd.creativecore.common.utils.math.vec.MatrixUtils.MatrixLookupTable;
import com.creativemd.creativecore.common.utils.math.vec.VecOrigin;
import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.creativecore.common.world.FakeWorld;
import com.creativemd.creativecore.common.world.SubWorld;
import com.creativemd.littletiles.client.render.RenderingThread;
import com.creativemd.littletiles.client.render.entity.LittleRenderChunk;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.events.LittleDoorHandler;
import com.creativemd.littletiles.common.items.ItemLittleWrench;
import com.creativemd.littletiles.common.packet.LittleEntityInteractPacket;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.relative.StructureAbsolute;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.preview.LittleAbsolutePreviewsStructure;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.animation.AnimationState;
import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
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
	
	public EntityAnimation(World world, CreativeWorld fakeWorld, EntityAnimationController controller, BlockPos absolutePreviewPos, UUID uuid, StructureAbsolute center) {
		this(world);
		
		this.controller = controller;
		this.controller.parent = this;
		
		this.absolutePreviewPos = absolutePreviewPos;
		
		this.entityUniqueID = uuid;
		this.cachedUniqueIdString = this.entityUniqueID.toString();
		
		this.fakeWorld = fakeWorld;
		this.fakeWorld.parent = this;
		
		setCenter(center);
		
		reloadWorldBlocks();
		updateWorldCollision();
		
		setPosition(center.baseOffset.getX(), center.baseOffset.getY(), center.baseOffset.getZ());
		
		addDoor();
		preventPush = true;
		onUpdateForReal();
		preventPush = false;
		
		prevWorldOffsetX = worldOffsetX;
		prevWorldOffsetY = worldOffsetY;
		prevWorldOffsetZ = worldOffsetZ;
		
		prevWorldRotX = worldRotX;
		prevWorldRotY = worldRotY;
		prevWorldRotZ = worldRotZ;
	}
	
	public boolean shouldAddDoor() {
		return true;
	}
	
	public void addDoor() {
		if (!shouldAddDoor())
			return;
		if (!addedDoor) {
			LittleDoorHandler.getHandler(world).createDoor(this);
			addedDoor = true;
		}
	}
	
	public void reloadWorldBlocks() {
		blocks = new ArrayList<>();
		for (TileEntity te : fakeWorld.loadedTileEntityList)
			if (te instanceof TileEntityLittleTiles)
				blocks.add((TileEntityLittleTiles) te);
	}
	
	@Override
	protected void entityInit() {
		addDoor();
	}
	
	// ================World Data================
	
	public CreativeWorld fakeWorld;
	public VecOrigin origin;
	public EntityAnimationController controller;
	public List<TileEntityLittleTiles> blocks;
	
	public double prevWorldRotX;
	public double prevWorldRotY;
	public double prevWorldRotZ;
	
	public double worldRotX;
	public double worldRotY;
	public double worldRotZ;
	
	public double prevWorldOffsetX;
	public double prevWorldOffsetY;
	public double prevWorldOffsetZ;
	
	public double worldOffsetX;
	public double worldOffsetY;
	public double worldOffsetZ;
	
	public Vec3d getRotationVector(float partialTicks) {
		return new Vec3d(this.prevWorldRotX + (this.worldRotX - this.prevWorldRotX) * (double) partialTicks, this.prevWorldRotY + (this.worldRotY - this.prevWorldRotY) * (double) partialTicks, this.prevWorldRotZ + (this.worldRotZ - this.prevWorldRotZ) * (double) partialTicks);
	}
	
	public Vec3d getOffsetVector(float partialTicks) {
		return new Vec3d(this.prevWorldOffsetX + (this.worldOffsetX - this.prevWorldOffsetX) * (double) partialTicks, this.prevWorldOffsetY + (this.worldOffsetY - this.prevWorldOffsetY) * (double) partialTicks, this.prevWorldOffsetZ + (this.worldOffsetZ - this.prevWorldOffsetZ) * (double) partialTicks);
	}
	
	// ================Axis================
	
	public void setCenter(StructureAbsolute center) {
		this.center = center;
		this.origin = new VecOrigin(center.rotationCenter);
		this.fakeWorld.setOrigin(origin);
	}
	
	public void setCenterVec(LittleTilePos axis, LittleTileVec additional) {
		setCenter(new StructureAbsolute(axis, additional));
	}
	
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
		double minX = Integer.MAX_VALUE;
		double minY = Integer.MAX_VALUE;
		double minZ = Integer.MAX_VALUE;
		double maxX = Integer.MIN_VALUE;
		double maxY = Integer.MIN_VALUE;
		double maxZ = Integer.MIN_VALUE;
		
		worldCollisionBoxes = new ArrayList<>();
		
		for (Iterator<TileEntityLittleTiles> iterator = blocks.iterator(); iterator.hasNext();) {
			TileEntityLittleTiles te = iterator.next();
			
			onScanningTE(te);
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
		
		collisionBoxWorker = new AABBCombiner(worldCollisionBoxes, 0);
		// BoxUtils.compressBoxes(worldCollisionBoxes, 0); //deviation might be
		// increased txco save performance
		
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
	
	public double getRot(Axis axis) {
		switch (axis) {
		case X:
			return worldRotX;
		case Y:
			return worldRotY;
		case Z:
			return worldRotZ;
		default:
			return 0;
		}
	}
	
	public void moveAndRotateAnimation(double x, double y, double z, double rotX, double rotY, double rotZ) {
		boolean moved = false;
		if (!preventPush) {
			// Create rotation matrix to transform to caclulate surrounding box
			Matrix3d rotationX = rotX != 0 ? MatrixUtils.createRotationMatrixX(rotX) : null;
			Matrix3d rotationY = rotY != 0 ? MatrixUtils.createRotationMatrixY(rotY) : null;
			Matrix3d rotationZ = rotZ != 0 ? MatrixUtils.createRotationMatrixZ(rotZ) : null;
			Vector3d translation = x != 0 || y != 0 || z != 0 ? new Vector3d(x, y, z) : null;
			
			if (rotationX != null || rotationY != null || rotationZ != null || translation != null) {
				AxisAlignedBB moveBB = BoxUtils.getRotatedSurrounding(worldBoundingBox, center.rotationCenter, origin.rotation(), origin.translation(), rotationX, rotX, rotationY, rotY, rotationZ, rotZ, translation);
				
				noCollision = true;
				
				List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, moveBB, EntityAnimation.noAnimation);
				if (!entities.isEmpty()) {
					// PHASE ONE
					List<AxisAlignedBB> surroundingBoxes = new ArrayList<>(worldCollisionBoxes.size());
					for (OrientatedBoundingBox box : worldCollisionBoxes) {
						
						if (box.cache == null)
							box.buildCache();
						box.cache.reset();
						
						surroundingBoxes.add(BoxUtils.getRotatedSurrounding(box, center.rotationCenter, origin.rotation(), origin.translation(), rotationX, rotX, rotationY, rotY, rotationZ, rotZ, translation));
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
					
					worldOffsetX += x;
					worldOffsetY += y;
					worldOffsetZ += z;
					
					worldRotX += rotX;
					worldRotY += rotY;
					worldRotZ += rotZ;
					
					updateOrigin();
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
			worldOffsetX += x;
			worldOffsetY += y;
			worldOffsetZ += z;
			
			worldRotX += rotX;
			worldRotY += rotY;
			worldRotZ += rotZ;
			
			updateOrigin();
		}
	}
	
	// ================Rendering================
	
	@SideOnly(Side.CLIENT)
	public LinkedHashMap<BlockPos, LittleRenderChunk> renderChunks;
	
	@SideOnly(Side.CLIENT)
	public ArrayList<TileEntityLittleTiles> renderQueue;
	
	@SideOnly(Side.CLIENT)
	public void unloadRenderCache() {
		if (renderChunks == null)
			return;
		for (LittleRenderChunk chunk : renderChunks.values()) {
			chunk.unload();
		}
	}
	
	@SideOnly(Side.CLIENT)
	public boolean spawnedInWorld;
	
	@SideOnly(Side.CLIENT)
	public void createClient() {
		if (blocks != null) {
			this.renderChunks = new LinkedHashMap<>();
			this.renderQueue = new ArrayList<>(blocks);
		}
	}
	
	// ================Events================
	
	public void onScanningTE(TileEntityLittleTiles te) {
		te.setLoaded();
		if (world.isRemote) {
			if (te.rendering == null)
				te.rendering = new AtomicBoolean(false);
			
			RenderingThread.addCoordToUpdate(te, 0, false);
		}
	}
	
	// ================Ticking================
	
	protected void handleForces() {
		motionX = 0;
		motionY = 0;
		motionZ = 0;
	}
	
	public void updateBoundingBox() {
		if (worldBoundingBox == null || fakeWorld == null)
			return;
		
		boolean rotated = prevWorldRotX != worldRotX || prevWorldRotY != worldRotY || prevPosZ != worldRotZ;
		boolean moved = prevWorldOffsetX != worldOffsetX || prevWorldOffsetY != worldOffsetY || prevWorldOffsetZ != worldOffsetZ;
		
		if (rotated || moved)
			setEntityBoundingBox(origin.getAxisAlignedBox(worldBoundingBox));
	}
	
	public void updateOrigin() {
		origin.off(worldOffsetX, worldOffsetY, worldOffsetZ);
		origin.rot(worldRotX, worldRotY, worldRotZ);
	}
	
	public void onTick() {
		AnimationState state = controller.tick();
		double moveX;
		double moveY;
		double moveZ;
		if (state.offset != null) {
			moveX = state.offset.x;
			moveY = state.offset.y;
			moveZ = state.offset.z;
		} else {
			moveX = 0;
			moveY = 0;
			moveZ = 0;
		}
		
		double rotateX;
		double rotateY;
		double rotateZ;
		if (state.rotation != null) {
			rotateX = state.rotation.x;
			rotateY = state.rotation.y;
			rotateZ = state.rotation.z;
		} else {
			rotateX = 0;
			rotateY = 0;
			rotateZ = 0;
		}
		moveAndRotateAnimation(moveX - worldOffsetX, moveY - worldOffsetY, moveZ - worldOffsetZ, rotateX - worldRotX, rotateY - worldRotY, rotateZ - worldRotZ);
	}
	
	public void onPostTick() {
		
	}
	
	public boolean addedDoor;
	
	@Override
	public void onUpdate() {
		
	}
	
	public void onUpdateForReal() {
		if (blocks == null && !world.isRemote)
			isDead = true;
		
		if (blocks == null)
			return;
		
		if (collisionBoxWorker != null) {
			collisionBoxWorker.work();
			
			if (collisionBoxWorker.hasFinished())
				collisionBoxWorker = null;
		}
		
		prevWorldRotX = worldRotX;
		prevWorldRotY = worldRotY;
		prevWorldRotZ = worldRotZ;
		
		prevWorldOffsetX = worldOffsetX;
		prevWorldOffsetY = worldOffsetY;
		prevWorldOffsetZ = worldOffsetZ;
		
		handleForces();
		
		super.onUpdate();
		
		onTick();
		
		onPostTick();
		
		updateBoundingBox();
		
		for (TileEntity te : fakeWorld.tickableTileEntities) {
			((ITickable) te).update();
		}
		
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
		if (!world.isRemote || !controller.isWaitingForRender())
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
	
	public boolean onRightClick(EntityPlayer player) {
		if (player.getHeldItemMainhand().getItem() instanceof ItemLittleWrench) {
			ItemLittleWrench.rightClickAnimation(this, player);
			return false;
		}
		return controller.onRightClick();
	}
	
	@Override
	public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
		if (world.isRemote)
			PacketHandler.sendPacketToServer(new LittleEntityInteractPacket(entityUniqueID));
		return EnumActionResult.SUCCESS;
	}
	
	// ================Saving & Loading================
	
	public LittleAbsolutePreviewsStructure getAbsolutePreviews(LittleStructure parent) {
		return parent.getAbsolutePreviews(absolutePreviewPos);
	}
	
	public LittleStructure getParentStructure() {
		for (TileEntityLittleTiles te : blocks) {
			for (Iterator<LittleTile> iterator = te.getTiles().iterator(); iterator.hasNext();) {
				LittleTile tile = iterator.next();
				if (!tile.connection.isLink()) {
					LittleStructure structure = tile.connection.getStructureWithoutLoading();
					if (structure.parent == null || structure.parent.isLinkToAnotherWorld())
						return structure;
				}
			}
		}
		return null;
	}
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		this.fakeWorld = compound.getBoolean("subworld") ? SubWorld.createFakeWorld(world) : FakeWorld.createFakeWorld(getCachedUniqueIdString(), world.isRemote);
		if (compound.hasKey("axis"))
			setCenterVec(new LittleTilePos("axis", compound), new LittleTileVec("additional", compound));
		else
			setCenter(new StructureAbsolute("center", compound));
		NBTTagList list = compound.getTagList("tileEntity", compound.getId());
		blocks = new ArrayList<>();
		LittleStructure parent = null;
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound nbt = list.getCompoundTagAt(i);
			TileEntityLittleTiles te = (TileEntityLittleTiles) TileEntity.create(fakeWorld, nbt);
			te.setWorld(fakeWorld);
			blocks.add(te);
			for (Iterator<LittleTile> iterator = te.getTiles().iterator(); iterator.hasNext();) {
				LittleTile tile = iterator.next();
				if (!tile.connection.isLink()) {
					LittleStructure structure = tile.connection.getStructureWithoutLoading();
					if (structure.parent == null || structure.parent.isLinkToAnotherWorld())
						parent = structure;
				}
			}
			fakeWorld.setBlockState(te.getPos(), BlockTile.getState(te.isTicking(), te.isRendered()));
			fakeWorld.setTileEntity(te.getPos(), te);
		}
		
		int[] array = compound.getIntArray("previewPos");
		if (array.length == 3)
			absolutePreviewPos = new BlockPos(array[0], array[1], array[2]);
		else
			absolutePreviewPos = center.baseOffset;
		
		controller = EntityAnimationController.parseController(this, compound.getCompoundTag("controller"));
		
		updateWorldCollision();
		updateBoundingBox();
	}
	
	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		center.writeToNBT("center", compound);
		
		compound.setBoolean("subworld", fakeWorld.hasParent());
		
		NBTTagList list = new NBTTagList();
		
		for (Iterator<TileEntityLittleTiles> iterator = blocks.iterator(); iterator.hasNext();) {
			TileEntityLittleTiles te = iterator.next();
			list.appendTag(te.writeToNBT(new NBTTagCompound()));
		}
		
		compound.setTag("controller", controller.writeToNBT(new NBTTagCompound()));
		
		compound.setTag("tileEntity", list);
		
		compound.setIntArray("previewPos", new int[] { absolutePreviewPos.getX(), absolutePreviewPos.getY(), absolutePreviewPos.getZ() });
		
	}
	
}
