package com.creativemd.littletiles.common.entity;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import com.creativemd.creativecore.common.utils.BoxUtils;
import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.creativecore.common.world.WorldFake;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.RenderingThread;
import com.creativemd.littletiles.client.render.entity.LittleRenderChunk;
import com.creativemd.littletiles.client.render.entity.TERenderData;
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

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class EntityAnimation<T extends EntityAnimation> extends Entity {
	
	//================World Data================
	
	public static int intFloorDiv(int p_76137_0_, int p_76137_1_)
    {
        return p_76137_0_ < 0 ? -((-p_76137_0_ - 1) / p_76137_1_) - 1 : p_76137_0_ / p_76137_1_;
    }
	
	public void setCenterVec(LittleTilePos axis)
	{
		axis.removeInternalBlockOffset();
		
		this.center = axis;
        this.baseOffset = axis.pos;
        
        this.inBlockCenter = axis.contextVec;
        this.chunkOffset = getRenderChunkPos(baseOffset);
        
        
        int chunkX = intFloorDiv(baseOffset.getX(), 16);
		int chunkY = intFloorDiv(baseOffset.getY(), 16);
		int chunkZ = intFloorDiv(baseOffset.getZ(), 16);
        
        inChunkOffset = new BlockPos(baseOffset.getX() - (chunkX*16), baseOffset.getY() - (chunkY*16), baseOffset.getZ() - (chunkZ*16));
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
	
	/**
	 * Rotated and moved collision boxes
	 */
	public ArrayList<AxisAlignedBB> collisionBoxes;
	
	/**
	 * Static not affected by direction or entity offset
	 */
	public ArrayList<AxisAlignedBB> worldCollisionBoxes;
	
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
			
			BoxUtils.compressBoxes(boxes, 1.0F);
			
			worldCollisionBoxes.addAll(boxes);
		}
		
		BoxUtils.compressBoxes(worldCollisionBoxes, 0); //deviation might be increased to save performance
		
		worldBoundingBox = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
	}
	
	//================Rendering================
	
	@SideOnly(Side.CLIENT)
	public HashMap<BlockPos, LittleRenderChunk> chunk;
	
	@SideOnly(Side.CLIENT)
	public HashMapList<BlockRenderLayer, TERenderData> renderData;
	
	@SideOnly(Side.CLIENT)
	public ArrayList<TileEntityLittleTiles> renderQueue;
	
	//@SideOnly(Side.CLIENT)
	//public AxisAlignedBB renderBoundingBox;
	
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
		renderData.removeValue(new TERenderData(null, null, te.getPos()));
	}
	
	//================Constructors================

	public EntityAnimation(World worldIn) {
		super(worldIn);
	}
	
	public EntityAnimation(World world, ArrayList<TileEntityLittleTiles> blocks, PlacePreviews previews, UUID uuid, LittleTilePos center) {
		this(world);
		
		this.blocks = blocks;
		this.previews = previews;
		
		this.entityUniqueID = uuid;
        this.cachedUniqueIdString = this.entityUniqueID.toString();
        
        setCenterVec(center);
        
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
			this.renderData = new HashMapList<>();
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
		if(worldBoundingBox == null)
			return ;
		
		boolean rotated = prevWorldRotX != worldRotX || prevWorldRotY != worldRotY || prevPosZ != worldRotZ;
		boolean moved = prevPosX != posX || prevPosY != posY || prevPosZ != posZ;
		
		if(rotated || moved)
		{
			Matrix3d rotationX = new Matrix3d();
			rotationX.rotX(Math.toRadians(worldRotX));
			Matrix3d rotationY = new Matrix3d();
			rotationY.rotY(Math.toRadians(worldRotY));
			Matrix3d rotationZ = new Matrix3d();
			rotationZ.rotZ(Math.toRadians(worldRotZ));
			
			ArrayList<Vector3d> boxPoints = new ArrayList<>();
			boxPoints.add(new Vector3d(worldBoundingBox.minX, worldBoundingBox.minY, worldBoundingBox.minZ));
			
			boxPoints.add(new Vector3d(worldBoundingBox.maxX, worldBoundingBox.minY, worldBoundingBox.minZ));
			boxPoints.add(new Vector3d(worldBoundingBox.minX, worldBoundingBox.maxY, worldBoundingBox.minZ));
			boxPoints.add(new Vector3d(worldBoundingBox.minX, worldBoundingBox.minY, worldBoundingBox.maxZ));
			
			boxPoints.add(new Vector3d(worldBoundingBox.maxX, worldBoundingBox.maxY, worldBoundingBox.minZ));
			boxPoints.add(new Vector3d(worldBoundingBox.maxX, worldBoundingBox.minY, worldBoundingBox.maxZ));
			boxPoints.add(new Vector3d(worldBoundingBox.minX, worldBoundingBox.maxY, worldBoundingBox.maxZ));
			
			boxPoints.add(new Vector3d(worldBoundingBox.maxX, worldBoundingBox.maxY, worldBoundingBox.maxZ));
			
			double minX = Double.MAX_VALUE;
			double minY = Double.MAX_VALUE;
			double minZ = Double.MAX_VALUE;
			double maxX = -Double.MAX_VALUE;
			double maxY = -Double.MAX_VALUE;
			double maxZ = -Double.MAX_VALUE;
			
			Vector3d origin = new Vector3d(center.getPosX()+center.getContext().gridMCLength/2, center.getPosY()+center.getContext().gridMCLength/2, center.getPosZ()+center.getContext().gridMCLength/2);
			
			for (int i = 0; i < boxPoints.size(); i++) {
				Vector3d vec = boxPoints.get(i);
				vec.sub(origin);
				rotationX.transform(vec);
				rotationY.transform(vec);
				rotationZ.transform(vec);
				vec.add(origin);
				
				minX = Math.min(minX, vec.x);
				minY = Math.min(minY, vec.y);
				minZ = Math.min(minZ, vec.z);
				maxX = Math.max(maxX, vec.x);
				maxY = Math.max(maxY, vec.y);
				maxZ = Math.max(maxZ, vec.z);
			}
			//BlockPos realStart = baseOffset.add(startOffset);
			double offsetX = posX - baseOffset.getX();
			double offsetY = posY - baseOffset.getY();
			double offsetZ = posZ - baseOffset.getZ();
			setEntityBoundingBox(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).offset(offsetX, offsetY, offsetZ));
		}
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
		
		prevWorldRotX = worldRotX;
		prevWorldRotY = worldRotY;
		prevWorldRotZ = worldRotZ;
		
		handleForces();
		
		super.onUpdate();
		
		onTick();
		
		onPostTick();
		
		updateBoundingBox();
		
		for (int i = 0; i < blocks.size(); i++) {
			if(i == 0)
			{
				WorldFake fakeWorld = (WorldFake) blocks.get(i).getWorld();
				fakeWorld.offsetX = posX - (getAxisPos().getX());
				fakeWorld.offsetY = posY - (getAxisPos().getY());
				fakeWorld.offsetZ = posZ - (getAxisPos().getZ());
				if(fakeWorld.axis == null)
				{
					Vec3d vec = getCenter().getVec();
					fakeWorld.axis = new Vector3d(vec.x, vec.y, vec.z);
				}
				fakeWorld.rotX = worldRotX;
				fakeWorld.rotY = worldRotY;
				fakeWorld.rotZ = worldRotZ;
			}
			
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
		animation.setCenterVec(center.copy());
		animation.structure = structure;
		animation.previews = previews.copy();
		animation.blocks = new ArrayList<>(blocks);
		
		animation.worldBoundingBox = worldBoundingBox;
		animation.worldCollisionBoxes = new ArrayList<>(worldCollisionBoxes);
		if(collisionBoxes != null)
			animation.collisionBoxes = new ArrayList<>(collisionBoxes);
		
		if(world.isRemote)
		{
			animation.renderData = renderData;
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
		
		//startOffset = new BlockPos(compound.getInteger("strOffX"), compound.getInteger("strOffY"), compound.getInteger("strOffZ"));
		
		setCenterVec(new LittleTilePos("axis", compound));
		
		World worldFake = WorldFake.createFakeWorld(world);
		NBTTagList list = compound.getTagList("tileEntity", compound.getId());
		blocks = new ArrayList<>();
		ArrayList<LittleTile> tiles = new ArrayList<>();
		structure = null; 
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound nbt = list.getCompoundTagAt(i);
			TileEntityLittleTiles te = (TileEntityLittleTiles) TileEntity.create(worldFake, nbt);
			te.setWorld(worldFake);
			blocks.add(te);
			for (Iterator<LittleTile> iterator = te.getTiles().iterator(); iterator.hasNext();) {
				LittleTile tile = iterator.next();
				if(tile.isMainBlock)
					this.structure = (LittleDoorBase) tile.structure;
				tiles.add(tile);
			}
			worldFake.setBlockState(te.getPos(), LittleTiles.blockTile.getDefaultState());
			worldFake.setTileEntity(te.getPos(), te);
		}
		
		LittleTilePos absoluteAxis = getCenter(); // structure.getAbsoluteAxisVec();
		LittleAbsolutePreviews previews = new LittleAbsolutePreviews(absoluteAxis.pos, absoluteAxis.getContext());
		for (LittleTile tile : tiles) {
			previews.addTile(tile);
		}
		
		previews.ensureContext(structure.getMinContext());		
		PlacePreviews defaultpreviews = new PlacePreviews(previews.context);
		absoluteAxis.convertTo(previews.context);
		
		for (LittleTilePreview preview : previews) {
			defaultpreviews.add(preview.getPlaceableTile(preview.box, false, absoluteAxis.contextVec.vec));
		}
		
		for (PlacePreviewTile placePreview : structure.getAdditionalPreviews(defaultpreviews)) {
			placePreview.box.addOffset(absoluteAxis.contextVec.vec);
			defaultpreviews.add(placePreview);
		}
		
		updateWorldCollision();
		updateBoundingBox();
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		
		/*compound.setInteger("strOffX", startOffset.getX());
		compound.setInteger("strOffY", startOffset.getY());
		compound.setInteger("strOffZ", startOffset.getZ());*/
		center.writeToNBT("axis", compound);
		
		NBTTagList list = new NBTTagList();
		
		for (Iterator<TileEntityLittleTiles> iterator = blocks.iterator(); iterator.hasNext();) {
			TileEntityLittleTiles te = iterator.next();
			list.appendTag(te.writeToNBT(new NBTTagCompound()));
		}
		
		compound.setTag("tileEntity", list);
		
	}

}
