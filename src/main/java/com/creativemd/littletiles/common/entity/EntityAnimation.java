package com.creativemd.littletiles.common.entity;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.creativemd.creativecore.common.utils.BoxUtils;
import com.creativemd.creativecore.common.world.WorldFake;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.RenderingThread;
import com.creativemd.littletiles.client.render.entity.LittleRenderChunk;
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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
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
        this.fakeWorld.axis = rotationCenter;
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
	public List<EntityAABB> worldCollisionBoxes;
	
	/**
	 * Static not affected by direction or entity offset
	 */
	public AxisAlignedBB worldBoundingBox;
	
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
			ArrayList<AxisAlignedBB> boxes = new ArrayList<>();
			
			TileEntityLittleTiles te = iterator.next();
			
			onScanningTE(te);
			AxisAlignedBB bb = te.getSelectionBox();
			minX = Math.min(minX, bb.minX);
			minY = Math.min(minY, bb.minY);
			minZ = Math.min(minZ, bb.minZ);
			maxX = Math.max(maxX, bb.maxX);
			maxY = Math.max(maxY, bb.maxY);
			maxZ = Math.max(maxZ, bb.maxZ);
			
			for (Iterator<LittleTile> iterator2 = te.getTiles().iterator(); iterator2.hasNext();) {
				LittleTile tile = iterator2.next();
				List<LittleTileBox> tileBoxes = tile.getCollisionBoxes();
				for (LittleTileBox box : tileBoxes) {
					boxes.add(box.getBox(te.getContext(), te.getPos()));
				}
			}
			
			//BoxUtils.compressBoxes(boxes, 0.0F);
			
			for (AxisAlignedBB box : boxes) {
				worldCollisionBoxes.add(new EntityAABB(fakeWorld, box));
			}
		}
		
		collisionBoxWorker = new AABBCombiner(worldCollisionBoxes, 0);
		//BoxUtils.compressBoxes(worldCollisionBoxes, 0); //deviation might be increased txco save performance
		
		worldBoundingBox = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
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
		rotAnimation(x - worldRotX, 0, 0);
	}
	
	public void rotYTo(double y)
	{
		rotAnimation(0, y - worldRotY, 0);
	}
	
	public void rotZTo(double z)
	{
		rotAnimation(0, 0, z - worldRotZ);
	}
	
	protected void rotByPartially(List<Entity> entity, Axis axis, double angle)
	{
		if(!preventPush)
		{
			
			
			
		}
		
		switch(axis)
		{
		case X:
			worldRotX += angle;
			break;
		case Y:
			worldRotY += angle;
			break;
		case Z:
			worldRotZ += angle;
			break;
		}
	}
	
	public void rotBy(List<Entity> entity, Axis axis, double angle)
	{
		double rotAxis = getRot(axis);
		int before = (int) Math.floor(rotAxis / 90);
		int after = (int) Math.floor((rotAxis + angle) / 90);
		boolean positive = angle > 0;
		double rotated = 0;
		
		for (int i = before; positive ? i <= after : i >= after; i += positive ? 1 : -1) {
			double toRotate;
			if(i == before)
			{
				if(i == after)
					toRotate = angle;
				else
					if(positive)
						toRotate = (before + 1) * 90 - rotAxis;
					else
						toRotate = (before - 1) * 90 - rotAxis;
			}
			else
			{
				if(i == after)
					toRotate = angle - rotated;
				else
					toRotate = positive ? 90 : -90;
			}
			rotated += toRotate;
			if(toRotate != 0)
				rotByPartially(entity, axis, toRotate);
		}
		
	}
	
	public void rotAnimation(double x, double y, double z)
	{
		List<Entity> entities;
		if(preventPush)
			entities = null;
		else
		{
			entities = new ArrayList<>(); // Needs to be changed
		}
		
		if(x != 0)
			rotBy(entities, Axis.X, x);
		
		if(y != 0)
			rotBy(entities, Axis.Y, y);
		
		if(z != 0)
			rotBy(entities, Axis.Z, z);
		
		updateOrigin();
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
		if(!preventPush)
		{
			AxisAlignedBB bb = getEntityBoundingBox().expand(x, y, z);
			
			noCollision = true;
			
			List<EntityAABB> boxes = new ArrayList<>();
			for (Entity entity : world.getEntitiesWithinAABB(Entity.class, bb, EntityAnimation.NO_ANIMATION)) {
				
				AxisAlignedBB entityBox = entity.getEntityBoundingBox();
				
				for (EntityAABB box : worldCollisionBoxes) {
					if(box.intersects(entityBox, x, y, z))
						boxes.add(box);
				}
				
				boolean collidedHorizontally = entity.collidedHorizontally;
				boolean collidedVertically = entity.collidedVertically;
				boolean onGround = entity.onGround;
				
				if(y != 0)
				{
					double distance = y;
					for (EntityAABB box : boxes) {
						if(box.intersects(entityBox, 0, y, 0))
							distance = minIgnore(distance, box.getDistanceY(entityBox));
					}
					
					if(distance != y)
					{
						entity.move(MoverType.SELF, 0, y > 0 ? y + distance + 0.00000000000001D : y - distance - 0.00000000000001D, 0);
						collidedVertically = true;
						entityBox = entity.getEntityBoundingBox();
						
						if(y > 0)
							onGround = true;
					}
				}
				
				if(x != 0)
				{
					double distance = x;
					for (EntityAABB box : boxes) {
						if(box.intersects(entityBox, x, 0, 0))
							distance = minIgnore(distance, box.getDistanceX(entityBox));
					}
					
					if(distance != x)
					{
						entity.move(MoverType.SELF, x > 0 ? x + distance + 0.00000000000001D : x - distance - 0.00000000000001D, 0, 0);
						collidedHorizontally = true;
						entityBox = entity.getEntityBoundingBox();
					}
				}
				
				if(z != 0)
				{
					double distance = z;
					for (EntityAABB box : boxes) {
						if(box.intersects(entityBox, 0, 0, z))
							distance = minIgnore(distance, box.getDistanceZ(entityBox));
					}
					
					if(distance != z)
					{
						entity.move(MoverType.SELF, 0, 0, z > 0 ? z + distance + 0.00000000000001D : z - distance - 0.00000000000001D);
						collidedHorizontally = true;
					}
				}
				
				entity.collidedHorizontally = collidedHorizontally;
				entity.collidedVertically = collidedVertically;
				entity.onGround = onGround;
				entity.collided = collidedHorizontally || collidedVertically;
				boxes.clear();
			}
			
			noCollision = false;
		}
		
		posX += x;
		posY += y;
		posZ += z;
		
		updateOrigin();
	}
	
	public EntityAABB getFakeWorldOrientatedBox(AxisAlignedBB box)
	{
		Matrix3d inverted = new Matrix3d(fakeWorld.rotation());
		inverted.invert();
		return new EntityAABB(fakeWorld, BoxUtils.getRotated(box.offset(-fakeWorld.translation().x, -fakeWorld.translation().y, -fakeWorld.translation().z), rotationCenter, inverted, new Vector3d()));
	}
	
	//================Rendering================
	
	@SideOnly(Side.CLIENT)
	public LinkedHashMap<BlockPos, LittleRenderChunk> renderChunks;
	
	@SideOnly(Side.CLIENT)
	public ArrayList<TileEntityLittleTiles> renderQueue;
	
	@SideOnly(Side.CLIENT)
	protected ArrayList<TileEntityLittleTiles> waitingForRender;
	
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
	
	public void unloadRenderCache()
	{
		for (LittleRenderChunk chunk : renderChunks.values()) {
			chunk.unload();
		}
	}
	
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
        
        if(world.isRemote)
        	createClient();
        
        updateWorldCollision();
        
        setPosition(baseOffset.getX(), baseOffset.getY(), baseOffset.getZ());
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
	
	
	//================Events================
	
	public void onScanningTE(TileEntityLittleTiles te)
	{
		te.setLoaded();
		if(te.isClientSide())
		{
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
			setEntityBoundingBox(BoxUtils.getRotated(worldBoundingBox, rotationCenter, fakeWorld.rotation(), fakeWorld.translation()));
	}
	
	public void updateOrigin()
	{
		fakeWorld.off(posX - (getAxisPos().getX()), posY - (getAxisPos().getY()), posZ - (getAxisPos().getZ()));
		fakeWorld.rot(worldRotX, worldRotY, worldRotZ);
	}
	
	public void onTick()
	{
		
	}
	
	public void onPostTick()
	{
		
	}
	
	@Override
	public void onUpdate()
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
		
		for (int i = 0; i < blocks.size(); i++) {
			if(blocks.get(i).shouldTick()) //place enhance this since it's quite horrible for larger animations
				blocks.get(i).update();
		}
		
		
	}
	
	//================Overridden================
	
	@Override
	@SideOnly(Side.CLIENT)
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport)
    {
        
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
		animation.blocks = new ArrayList<>(blocks);
		
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
			fakeWorld.setBlockState(te.getPos(), LittleTiles.blockTile.getDefaultState());
			fakeWorld.setTileEntity(te.getPos(), te);
		}
		
		LittleTilePos absoluteAxis = getCenter(); // structure.getAbsoluteAxisVec();
		LittleAbsolutePreviews previews = new LittleAbsolutePreviews(baseOffset, absoluteAxis.getContext());
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
