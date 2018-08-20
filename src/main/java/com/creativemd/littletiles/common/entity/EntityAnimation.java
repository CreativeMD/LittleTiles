package com.creativemd.littletiles.common.entity;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.box.BoxPlane;
import com.creativemd.creativecore.common.utils.math.box.BoxUtils;
import com.creativemd.creativecore.common.utils.math.box.CollidingPlane;
import com.creativemd.creativecore.common.utils.math.box.OrientatedBoundingBox;
import com.creativemd.creativecore.common.utils.math.box.BoxUtils.BoxFace;
import com.creativemd.creativecore.common.utils.math.box.CollidingPlane.PushCache;
import com.creativemd.creativecore.common.utils.math.vec.MatrixUtils;
import com.creativemd.creativecore.common.utils.math.vec.MatrixUtils.MatrixLookupTable;
import com.creativemd.creativecore.common.utils.math.vec.VecOrigin;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.world.IFakeWorld;
import com.creativemd.creativecore.common.world.WorldFake;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.RenderingThread;
import com.creativemd.littletiles.client.render.entity.LittleRenderChunk;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.events.LittleDoorHandler;
import com.creativemd.littletiles.common.structure.LittleDoorBase;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVecContext;
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

public abstract class EntityAnimation<T extends EntityAnimation> extends Entity {
	
	protected static Predicate<Entity> NO_ANIMATION = new Predicate<Entity>() {

		@Override
		public boolean apply(Entity input) {
			return !(input instanceof EntityAnimation);
		}
		
	};
	
	//================World Data================
	
	public static int intFloorDiv(int p_76137_0_, int p_76137_1_)
    {
        return p_76137_0_ < 0 ? -((-p_76137_0_ - 1) / p_76137_1_) - 1 : p_76137_0_ / p_76137_1_;
    }
	
	public void setCenterVec(LittleTilePos axis, LittleTileVec additional)
	{
		axis.removeInternalBlockOffset();
		
		this.center = axis;
        this.baseOffset = axis.pos;
        
        this.inBlockCenter = axis.contextVec;
        this.chunkOffset = getRenderChunkPos(baseOffset);
        
        
        int chunkX = intFloorDiv(baseOffset.getX(), 16);
		int chunkY = intFloorDiv(baseOffset.getY(), 16);
		int chunkZ = intFloorDiv(baseOffset.getZ(), 16);
        
        this.inChunkOffset = new BlockPos(baseOffset.getX() - (chunkX*16), baseOffset.getY() - (chunkY*16), baseOffset.getZ() - (chunkZ*16));
        this.additionalAxis = additional;
        
        this.rotationCenter = new Vector3d(axis.getPosX()+additionalAxis.getPosX(axis.getContext())/2, axis.getPosY()+additionalAxis.getPosY(axis.getContext())/2, axis.getPosZ()+additionalAxis.getPosZ(axis.getContext())/2);
        this.rotationCenterInsideBlock = new Vector3d(inBlockCenter.getPosX()+additionalAxis.getPosX(inBlockCenter.context)/2, inBlockCenter.getPosY()+additionalAxis.getPosY(inBlockCenter.context)/2, inBlockCenter.getPosZ()+additionalAxis.getPosZ(inBlockCenter.context)/2);
        
        this.origin = new VecOrigin(rotationCenter);
        ((IFakeWorld) this.fakeWorld).setOrigin(origin);
	}
	
	public static BlockPos getRenderChunkPos(BlockPos blockPos)
	{
		return new BlockPos(blockPos.getX() >> 4, blockPos.getY() >> 4, blockPos.getZ() >> 4);
	}
	
	protected LittleTilePos center;
	protected LittleTileVecContext inBlockCenter;
	protected BlockPos baseOffset;
	protected BlockPos chunkOffset;
	protected BlockPos inChunkOffset;
	protected LittleTileVec additionalAxis;
	public Vector3d rotationCenter;
	public Vector3d rotationCenterInsideBlock;
	
	public LittleTilePos getCenter()
	{
		return center;
	}
	
	public LittleTileVecContext getInsideBlockCenter()
	{
		return inBlockCenter;
	}
	
	public BlockPos getAxisPos()
	{
		return baseOffset;
	}
	
	public BlockPos getAxisChunkPos()
	{
		return chunkOffset;
	}
	
	public BlockPos getInsideChunkPos()
	{
		return inChunkOffset;
	}
	
	//================World Data================
	
	public WorldFake fakeWorld;
	public VecOrigin origin;
	public LittleDoorBase structure;
	public PlacePreviews previews;
	public ArrayList<TileEntityLittleTiles> blocks;
	
	public double prevWorldRotX = 0;
	public double prevWorldRotY = 0;
	public double prevWorldRotZ = 0;
	
	public double worldRotX = 0;
	public double worldRotY = 0;
	public double worldRotZ = 0;
	
	public Vec3d getRotVector(float partialTicks)
	{
		return new Vec3d(this.prevWorldRotX + (this.worldRotX - this.prevWorldRotX) * (double)partialTicks,
				this.prevWorldRotY + (this.worldRotY - this.prevWorldRotY) * (double)partialTicks,
				this.prevWorldRotZ + (this.worldRotZ - this.prevWorldRotZ) * (double)partialTicks);
	}
	
	//================Collision================
	
	public boolean preventPush = false;
	
	/**
	 * Is true when animation moves other entities
	 */
	public boolean noCollision = false;
	
	public AABBCombiner collisionBoxWorker;
	
	/**
	 * Static not affected by direction or entity offset
	 */
	public List<OrientatedBoundingBox> worldCollisionBoxes;
	
	/**
	 * Static not affected by direction or entity offset
	 */
	public OrientatedBoundingBox worldBoundingBox;
	
	/**
	 * Should be called if the world of the animation will be modified (Currently not possible)
	 */
	public void updateWorldCollision()
	{
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
			
			//BoxUtils.compressBoxes(boxes, 0.0F);
			
			for (AxisAlignedBB box : boxes) {
				worldCollisionBoxes.add(new OrientatedBoundingBox(origin, box));
			}
		}
		
		collisionBoxWorker = new AABBCombiner(worldCollisionBoxes, 0);
		//BoxUtils.compressBoxes(worldCollisionBoxes, 0); //deviation might be increased txco save performance
		
		worldBoundingBox = new OrientatedBoundingBox(origin, minX, minY, minZ, maxX, maxY, maxZ);
	}
	
	private static double minIgnore(double par1, double par2)
	{
		if(Math.abs(par2) < Math.abs(par1))
			return par2;
		return par1;
	}
	
	private static double maxIgnore(double par1, double par2)
	{
		if(Math.abs(par2) > Math.abs(par1))
			return par2;
		return par1;
	}
	
	public double getRot(Axis axis)
	{
		switch(axis)
		{
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
	
	public void rotXTo(double x)
	{
		rotateAnimation(x - worldRotX, 0, 0);
	}
	
	public void rotYTo(double y)
	{
		rotateAnimation(0, y - worldRotY, 0);
	}
	
	public void rotZTo(double z)
	{
		rotateAnimation(0, 0, z - worldRotZ);
	}
	
	public void rotateAnimation(double rotX, double rotY, double rotZ)
	{
		moveAndRotateAnimation(0, 0, 0, rotX, rotY, rotZ);
	}
	
	public void moveAndRotateAnimation(double x, double y, double z, double rotX, double rotY, double rotZ)
	{
		boolean moved = false;
		if(!preventPush)
		{
			//Create rotation matrix to transform to caclulate surrounding box			
			Matrix3d rotationX = rotX != 0 ? MatrixUtils.createRotationMatrixX(rotX) : null;
			Matrix3d rotationY = rotY != 0 ? MatrixUtils.createRotationMatrixY(rotY) : null;
			Matrix3d rotationZ = rotZ != 0 ? MatrixUtils.createRotationMatrixZ(rotZ) : null;
			Vector3d translation = x != 0 || y != 0 || z != 0 ? new Vector3d(x, y, z) : null;
			
			if(rotationX != null || rotationY != null || rotationZ != null || translation != null)
			{
				AxisAlignedBB moveBB = BoxUtils.getRotatedSurrounding(worldBoundingBox, rotationCenter, origin.rotation(), origin.translation(), rotationX, rotX, rotationY, rotY, rotationZ, rotZ, translation);
				
				noCollision = true;
				
				List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, moveBB, EntityAnimation.NO_ANIMATION);
				if(!entities.isEmpty())
				{
					// PHASE ONE
					List<AxisAlignedBB> surroundingBoxes = new ArrayList<>(worldCollisionBoxes.size());
					for (OrientatedBoundingBox box : worldCollisionBoxes) {
						
						if(box.cache == null)
							box.buildCache();
						box.cache.reset();
						
						surroundingBoxes.add(BoxUtils.getRotatedSurrounding(box, rotationCenter, origin.rotation(), origin.translation(), rotationX, rotX, rotationY, rotY, rotationZ, rotZ, translation));
					}
					
					// PHASE TWO
					MatrixLookupTable table = new MatrixLookupTable(x, y, z, rotX, rotY, rotZ, rotationCenter, origin);
					
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
						
						checking_all_boxes:
						for (int i = 0; i < surroundingBoxes.size(); i++) {
							if(surroundingBoxes.get(i).intersects(entityBox))
							{
								//Check for earliest hit
								OrientatedBoundingBox box = worldCollisionBoxes.get(i);
								
								if(!box.cache.isCached())
									box.cache.planes = CollidingPlane.getPlanes(box, box.cache, table);
								
								//Binary search
								for(CollidingPlane plane : box.cache.planes) {
									Double tempT = plane.binarySearch(t, entityBox, radius, center, table);
									if(tempT != null)
									{
										t = tempT;
										pushingBox = box;
										facing = plane.facing;
										if(t == 0)
											break checking_all_boxes;
									}
								}
							}
						}
						
						// Applying found t
						if(t != null)
						{
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
					
					posX += x;
					posY += y;
					posZ += z;
					
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
						if(!cached)
						{
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
							
							vec.sub(rotationCenter);
							origin.rotationInv().transform(vec);
							vec.add(rotationCenter);
							
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
						
						if(cached)
						{
							intersecting.add(cache.pushBox);
							intersectingFacing.add(cache.facing);
						}
						
						for (OrientatedBoundingBox box : worldCollisionBoxes) {
							if((!cached || box != cache.pushBox) && box.intersects(fakeBox))
							{
								if(!box.cache.isCached())
									box.cache.planes = CollidingPlane.getPlanes(box, box.cache, table);
								
								boolean add = !cached;
								EnumFacing facing = CollidingPlane.getDirection(box, box.cache.planes, center);
								
								if(facing == null || (!table.hasOneRotation && RotationUtils.get(facing.getAxis(), translation) == 0))
									continue;
								
								if(cached)
								{
									if(facing == cache.facing)
										add = true;
									else if(!ignoreOne && facing.getAxis() == one)
									{
										add = true;
										if(positiveOne == null)
											positiveOne = facing.getAxisDirection() == AxisDirection.POSITIVE;
										else if(facing.getAxisDirection() == AxisDirection.POSITIVE != positiveOne)
										{
											ignoreOne = true;
											add = false;
										}
									}
									else if(!ignoreTwo && facing.getAxis() == two)
									{
										add = true;
										if(positiveTwo == null)
											positiveTwo = facing.getAxisDirection() == AxisDirection.POSITIVE;
										else if(facing.getAxisDirection() == AxisDirection.POSITIVE != positiveTwo)
										{
											ignoreTwo = true;
											add = false;
										}
									}
								}
								
								if(add)
								{
									double intersectingVolume = box.getIntersectionVolume(fakeBox);
									
									if(intersectingVolume > maxVolume)
									{
										cache.pushBox = box;
										maxVolume = intersectingVolume;
										cache.facing = facing;
									}
								
									intersecting.add(box);
									intersectingFacing.add(facing);
								}
							}
						}
						
						if(intersecting.isEmpty())
							continue;
						
						if(!cached)
						{
							one = RotationUtils.getDifferentAxisFirst(cache.facing.getAxis());
							two = RotationUtils.getDifferentAxisSecond(cache.facing.getAxis());
							
							positiveOne = null;
							positiveTwo = null;
							
							for (EnumFacing facing : intersectingFacing) {
								
								if(!ignoreOne && facing.getAxis() == one)
								{
									if(positiveOne == null)
										positiveOne = facing.getAxisDirection() == AxisDirection.POSITIVE;
									else if(facing.getAxisDirection() == AxisDirection.POSITIVE != positiveOne)
										ignoreOne = true;
								}
								else if(!ignoreTwo && facing.getAxis() == two)
								{
									if(positiveTwo == null)
										positiveTwo = facing.getAxisDirection() == AxisDirection.POSITIVE;
									else if(facing.getAxisDirection() == AxisDirection.POSITIVE != positiveTwo)
										ignoreTwo = true;
								}
								
								if(ignoreOne && ignoreTwo)
									break;
							}
						}
						
						// Now things are ready. Go through all intersecting ones and push the box out
						Vector3d pushVec = new Vector3d();
						RotationUtils.setValue(pushVec, cache.facing.getAxisDirection().getOffset(), cache.facing.getAxis());
						if(!ignoreOne && positiveOne != null)
							RotationUtils.setValue(pushVec, positiveOne ? 1 : -1, one);
						if(!ignoreTwo && positiveTwo != null)
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
							
							if((ignoreOne && facing.getAxis() == one) || (ignoreTwo && facing.getAxis() == two))
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
						
						entity.move(MoverType.SELF, moveX, moveY, moveZ);
						
						if(entity instanceof EntityPlayerMP)
							LittleDoorHandler.setPushedByDoor((EntityPlayerMP) entity);
						
						/*entity.motionX += moveX;
						entity.motionY += moveY;
						entity.motionZ += moveZ;*/
						
						if(moveX != 0 || moveZ != 0)
							collidedHorizontally = true;
						if(moveY != 0)
						{
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
		
		if(!moved)
		{
			posX += x;
			posY += y;
			posZ += z;
			
			worldRotX += rotX;
			worldRotY += rotY;
			worldRotZ += rotZ;
			
			updateOrigin();
		}
	}
	
	public void moveXTo(double x)
	{
		moveAnimation(x - posX, 0, 0);
	}
	
	public void moveYTo(double y)
	{
		moveAnimation(0, y - posY, 0);
	}
	
	public void moveZTo(double z)
	{
		moveAnimation(0, 0, z - posZ);
	}
	
	public void moveAnimation(double x, double y, double z)
	{
		moveAndRotateAnimation(x, y, z, 0, 0, 0);
	}
	
	//================Rendering================
	
	@SideOnly(Side.CLIENT)
	public LinkedHashMap<BlockPos, LittleRenderChunk> renderChunks;
	
	@SideOnly(Side.CLIENT)
	public ArrayList<TileEntityLittleTiles> renderQueue;
	
	@SideOnly(Side.CLIENT)
	protected CopyOnWriteArrayList<TileEntityLittleTiles> waitingForRender;
	
	@SideOnly(Side.CLIENT)
	protected int ticksToWait;
	
	@SideOnly(Side.CLIENT)
	public boolean isWaitingForRender()
	{
		return waitingForRender != null;
	}
	
	@SideOnly(Side.CLIENT)
	public void removeWaitingTe(TileEntityLittleTiles te)
	{
		waitingForRender.remove(te);
	}
	
	@SideOnly(Side.CLIENT)
	public void unloadRenderCache()
	{
		if(renderChunks == null)
			return ;
		for (LittleRenderChunk chunk : renderChunks.values()) {
			chunk.unload();
		}
	}
	
	@SideOnly(Side.CLIENT)
	public boolean spawnedInWorld;
	
	//================Constructors================

	public EntityAnimation(World worldIn) {
		super(worldIn);
	}
	
	public EntityAnimation(World world, WorldFake fakeWorld, ArrayList<TileEntityLittleTiles> blocks, PlacePreviews previews, UUID uuid, LittleTilePos center, LittleTileVec additional) {
		this(world);
		
		this.blocks = blocks;
		this.previews = previews;
		
		this.entityUniqueID = uuid;
        this.cachedUniqueIdString = this.entityUniqueID.toString();
        
        this.fakeWorld = fakeWorld;
        
        setCenterVec(center, additional);
        
        updateWorldCollision();
        
        setPosition(baseOffset.getX(), baseOffset.getY(), baseOffset.getZ());
        
        addDoor();
	}
	
	@SideOnly(Side.CLIENT)
	public void createClient()
	{
		if(blocks != null)
		{
			this.renderChunks = new LinkedHashMap<>();
			this.renderQueue = new ArrayList<>(blocks);
		}
	}
	
	public void addDoor()
	{
		if(!addedDoor)
		{
			LittleDoorHandler.getHandler(world).createDoor(this);
			addedDoor = true;
		}
	}
	
	
	//================Events================
	
	public void onScanningTE(TileEntityLittleTiles te)
	{
		te.setLoaded();
		if(world.isRemote)
		{
			if(te.rendering == null)
				te.rendering = new AtomicBoolean(false);
			
			RenderingThread.addCoordToUpdate(te, 0, false);
		}
	}
	
	//================Ticking================
	
	protected void handleForces()
	{
		motionX = 0;
		motionY = 0;
		motionZ = 0;
	}
	
	public void updateBoundingBox()
	{
		if(worldBoundingBox == null || fakeWorld == null)
			return ;
		
		boolean rotated = prevWorldRotX != worldRotX || prevWorldRotY != worldRotY || prevPosZ != worldRotZ;
		boolean moved = prevPosX != posX || prevPosY != posY || prevPosZ != posZ;
		
		if(rotated || moved)
			setEntityBoundingBox(origin.getAxisAlignedBox(worldBoundingBox));
	}
	
	public void updateOrigin()
	{
		origin.off(posX - getAxisPos().getX(), posY - getAxisPos().getY(), posZ - getAxisPos().getZ());
		origin.rot(worldRotX, worldRotY, worldRotZ);
	}
	
	public void onTick()
	{
		
	}
	
	public void onPostTick()
	{
		
	}
	
	public boolean addedDoor = false;
	
	@Override
	public void onUpdate()
	{
		addDoor();
	}
	
	public void onUpdateForReal()
	{
		if(blocks == null && !world.isRemote)
			isDead = true;
		
		if(blocks == null)
			return ;
		
		if(collisionBoxWorker != null)
		{
			collisionBoxWorker.work();
			
			if(collisionBoxWorker.hasFinished())
				collisionBoxWorker = null;
		}
		
		prevWorldRotX = worldRotX;
		prevWorldRotY = worldRotY;
		prevWorldRotZ = worldRotZ;
		
		handleForces();
		
		super.onUpdate();
		
		onTick();
		
		onPostTick();
		
		updateBoundingBox();
		
		for (TileEntity te : fakeWorld.tickableTileEntities) {
			((ITickable) te).update();
		}
		
		
	}
	
	//================Overridden================
	
	@Override
	@SideOnly(Side.CLIENT)
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport)
    {
        
    }
	
	@Override	
	public void setPositionAndRotation(double x, double y, double z, float yaw, float pitch)
    {
		
    }
	
	@Override
	public void setPositionAndUpdate(double x, double y, double z)
    {
		
    }
	
	@Override
	public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch)
    {
		
    }
	
	public void setInitialPosition(double x, double y, double z)
	{
		setPosition(x, y, z);
	}
	
	@Override
	public void setPosition(double x, double y, double z)
    {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        updateBoundingBox();
    }
	
	@Override
	public void setDead()
    {
		if(!world.isRemote)
			this.isDead = true;
    }
	
	@Override
	public boolean canBeCollidedWith()
    {
        return true;
    }
	
	@Override
	public AxisAlignedBB getCollisionBox(Entity entityIn)
    {
        return null;
    }
	
	@Override
    public AxisAlignedBB getCollisionBoundingBox()
    {
        return null;
    }
	
	@Override
	public boolean processInitialInteract(EntityPlayer player, EnumHand hand)
    {
        return true;
    }
	
	@Override
	public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand)
    {
        return EnumActionResult.SUCCESS;
    }
	
	//================Copy================
	
	protected abstract void copyExtra(T animation);
	
	public T copy()
	{
		T animation = null;
		try {
			animation = (T) this.getClass().getConstructor(World.class).newInstance(this.world);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		animation.setUniqueId(getUniqueID());
		animation.fakeWorld = fakeWorld;
		animation.setCenterVec(center.copy(), additionalAxis.copy());
		animation.structure = structure;
		animation.previews = previews.copy();
		animation.blocks = blocks;
		
		animation.worldBoundingBox = worldBoundingBox;
		animation.worldCollisionBoxes = new ArrayList<>(worldCollisionBoxes);
		//if(collisionBoxes != null)
			//animation.collisionBoxes = new ArrayList<>(collisionBoxes);
		
		if(world.isRemote)
		{
			animation.renderChunks = renderChunks;
			animation.renderQueue = renderQueue;
		}
		
		animation.prevWorldRotX = prevWorldRotX;
		animation.prevWorldRotY = prevWorldRotY;
		animation.prevWorldRotZ = prevWorldRotZ;
		
		animation.worldRotX = worldRotX;
		animation.worldRotY = worldRotY;
		animation.worldRotZ = worldRotZ;
		
		//animation.startOffset = startOffset;
		
		copyExtra(animation);
		
		return animation;
	}
	
	//================Saving & Loading================
	
	protected BlockPos getPreviewOffset()
	{
		return baseOffset;
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		this.fakeWorld = WorldFake.createFakeWorld(world);
		setCenterVec(new LittleTilePos("axis", compound), new LittleTileVec("additional", compound));
		NBTTagList list = compound.getTagList("tileEntity", compound.getId());
		blocks = new ArrayList<>();
		ArrayList<LittleTile> tiles = new ArrayList<>();
		structure = null; 
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound nbt = list.getCompoundTagAt(i);
			TileEntityLittleTiles te = (TileEntityLittleTiles) TileEntity.create(fakeWorld, nbt);
			te.setWorld(fakeWorld);
			blocks.add(te);
			for (Iterator<LittleTile> iterator = te.getTiles().iterator(); iterator.hasNext();) {
				LittleTile tile = iterator.next();
				if(tile.isMainBlock)
					this.structure = (LittleDoorBase) tile.structure;
				tiles.add(tile);
			}
			fakeWorld.setBlockState(te.getPos(), BlockTile.getState(te.isTicking(), te.isRendered()));
			fakeWorld.setTileEntity(te.getPos(), te);
		}
		
		LittleTilePos absoluteAxis = getCenter(); // structure.getAbsoluteAxisVec();
		LittleAbsolutePreviews previews = new LittleAbsolutePreviews(getPreviewOffset(), absoluteAxis.getContext());
		for (LittleTile tile : tiles) {
			previews.addTile(tile);
		}
		
		previews.ensureContext(structure.getMinContext());		
		this.previews = new PlacePreviews(previews.context);
		absoluteAxis.convertTo(previews.context);
		
		for (LittleTilePreview preview : previews) {
			this.previews.add(preview.getPlaceableTile(preview.box, false, LittleTileVec.ZERO));
		}
		
		for (PlacePreviewTile placePreview : structure.getAdditionalPreviews(this.previews)) {
			placePreview.box.addOffset(absoluteAxis.contextVec.vec);
			this.previews.add(placePreview);
		}
		
		updateWorldCollision();
		updateBoundingBox();
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		center.writeToNBT("axis", compound);
		additionalAxis.writeToNBT("additional", compound);
		
		NBTTagList list = new NBTTagList();
		
		for (Iterator<TileEntityLittleTiles> iterator = blocks.iterator(); iterator.hasNext();) {
			TileEntityLittleTiles te = iterator.next();
			list.appendTag(te.writeToNBT(new NBTTagCompound()));
		}
		
		compound.setTag("tileEntity", list);
		
	}

}
