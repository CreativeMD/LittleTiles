package com.creativemd.littletiles.common.entity.old;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import javax.vecmath.Vector3d;

import com.creativemd.creativecore.common.utils.math.box.OrientatedBoundingBox;
import com.creativemd.creativecore.common.utils.math.vec.IVecOrigin;
import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.creativecore.common.world.FakeWorld;
import com.creativemd.creativecore.common.world.SubWorld;
import com.creativemd.littletiles.client.render.entity.LittleRenderChunk;
import com.creativemd.littletiles.common.block.BlockTile;
import com.creativemd.littletiles.common.entity.AABBCombiner;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleAbsoluteVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVecContext;
import com.creativemd.littletiles.common.tile.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Deprecated
public abstract class EntityOldAnimation extends Entity {
	
	protected static Predicate<Entity> NO_ANIMATION = new Predicate<Entity>() {
		
		@Override
		public boolean apply(Entity input) {
			return !(input instanceof EntityOldAnimation);
		}
		
	};
	
	// ================World Data================
	
	public static int intFloorDiv(int p_76137_0_, int p_76137_1_) {
		return p_76137_0_ < 0 ? -((-p_76137_0_ - 1) / p_76137_1_) - 1 : p_76137_0_ / p_76137_1_;
	}
	
	public void setCenterVec(LittleAbsoluteVec axis, LittleVec additional) {
		axis.removeInternalBlockOffset();
		
		this.center = axis;
		this.baseOffset = axis.getPos();
		
		this.inBlockCenter = axis.getVecContext();
		this.chunkOffset = getRenderChunkPos(baseOffset);
		
		int chunkX = intFloorDiv(baseOffset.getX(), 16);
		int chunkY = intFloorDiv(baseOffset.getY(), 16);
		int chunkZ = intFloorDiv(baseOffset.getZ(), 16);
		
		this.inChunkOffset = new BlockPos(baseOffset.getX() - (chunkX * 16), baseOffset.getY() - (chunkY * 16), baseOffset.getZ() - (chunkZ * 16));
		this.additionalAxis = additional;
		
		this.rotationCenter = new Vector3d(axis.getPosX() + additionalAxis.getPosX(axis.getContext()) / 2, axis.getPosY() + additionalAxis.getPosY(axis.getContext()) / 2, axis.getPosZ() + additionalAxis.getPosZ(axis.getContext()) / 2);
		this.rotationCenterInsideBlock = new Vector3d(inBlockCenter.getPosX() + additionalAxis.getPosX(inBlockCenter.getContext()) / 2, inBlockCenter.getPosY() + additionalAxis.getPosY(inBlockCenter.getContext()) / 2, inBlockCenter.getPosZ() + additionalAxis.getPosZ(inBlockCenter.getContext()) / 2);
		
		this.fakeWorld.setOrigin(rotationCenter);
		this.origin = this.fakeWorld.getOrigin();
	}
	
	public static BlockPos getRenderChunkPos(BlockPos blockPos) {
		return new BlockPos(blockPos.getX() >> 4, blockPos.getY() >> 4, blockPos.getZ() >> 4);
	}
	
	protected LittleAbsoluteVec center;
	protected LittleVecContext inBlockCenter;
	protected BlockPos baseOffset;
	protected BlockPos chunkOffset;
	protected BlockPos inChunkOffset;
	protected LittleVec additionalAxis;
	public Vector3d rotationCenter;
	public Vector3d rotationCenterInsideBlock;
	
	public LittleAbsoluteVec getCenter() {
		return center;
	}
	
	public LittleVecContext getInsideBlockCenter() {
		return inBlockCenter;
	}
	
	public BlockPos getAxisPos() {
		return baseOffset;
	}
	
	public BlockPos getAxisChunkPos() {
		return chunkOffset;
	}
	
	public BlockPos getInsideChunkPos() {
		return inChunkOffset;
	}
	
	// ================World Data================
	
	public CreativeWorld fakeWorld;
	public IVecOrigin origin;
	public LittleAbsolutePreviews previews;
	public ArrayList<TileEntityLittleTiles> blocks;
	
	public double prevWorldRotX = 0;
	public double prevWorldRotY = 0;
	public double prevWorldRotZ = 0;
	
	public double worldRotX = 0;
	public double worldRotY = 0;
	public double worldRotZ = 0;
	
	public Vec3d getRotVector(float partialTicks) {
		return new Vec3d(this.prevWorldRotX + (this.worldRotX - this.prevWorldRotX) * partialTicks, this.prevWorldRotY + (this.worldRotY - this.prevWorldRotY) * partialTicks, this.prevWorldRotZ + (this.worldRotZ - this.prevWorldRotZ) * partialTicks);
	}
	
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
	
	public void rotXTo(double x) {
		rotateAnimation(x - worldRotX, 0, 0);
	}
	
	public void rotYTo(double y) {
		rotateAnimation(0, y - worldRotY, 0);
	}
	
	public void rotZTo(double z) {
		rotateAnimation(0, 0, z - worldRotZ);
	}
	
	public void rotateAnimation(double rotX, double rotY, double rotZ) {
		moveAndRotateAnimation(0, 0, 0, rotX, rotY, rotZ);
	}
	
	public void moveAndRotateAnimation(double x, double y, double z, double rotX, double rotY, double rotZ) {
		posX += x;
		posY += y;
		posZ += z;
		
		worldRotX += rotX;
		worldRotY += rotY;
		worldRotZ += rotZ;
		
		updateOrigin();
	}
	
	public void moveXTo(double x) {
		moveAnimation(x - posX, 0, 0);
	}
	
	public void moveYTo(double y) {
		moveAnimation(0, y - posY, 0);
	}
	
	public void moveZTo(double z) {
		moveAnimation(0, 0, z - posZ);
	}
	
	public void moveAnimation(double x, double y, double z) {
		moveAndRotateAnimation(x, y, z, 0, 0, 0);
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
	
	// ================Constructors================
	
	public EntityOldAnimation(World worldIn) {
		super(worldIn);
	}
	
	public EntityOldAnimation(World world, CreativeWorld fakeWorld, ArrayList<TileEntityLittleTiles> blocks, LittleAbsolutePreviews previews, UUID uuid, LittleAbsoluteVec center, LittleVec additional) {
		this(world);
		
		this.blocks = blocks;
		this.previews = previews;
		
		this.entityUniqueID = uuid;
		this.cachedUniqueIdString = this.entityUniqueID.toString();
		
		this.fakeWorld = fakeWorld;
		this.fakeWorld.parent = this;
		
		setCenterVec(center, additional);
		
		updateWorldCollision();
		
		setPosition(baseOffset.getX(), baseOffset.getY(), baseOffset.getZ());
	}
	
	@SideOnly(Side.CLIENT)
	public void createClient() {
		if (blocks != null) {
			this.renderChunks = new LinkedHashMap<>();
			this.renderQueue = new ArrayList<>(blocks);
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
		boolean moved = prevPosX != posX || prevPosY != posY || prevPosZ != posZ;
		
		if (rotated || moved)
			setEntityBoundingBox(origin.getAxisAlignedBox(worldBoundingBox));
	}
	
	public void updateOrigin() {
		origin.off(posX - getAxisPos().getX(), posY - getAxisPos().getY(), posZ - getAxisPos().getZ());
		origin.rot(worldRotX, worldRotY, worldRotZ);
	}
	
	public void onTick() {
		
	}
	
	public void onPostTick() {
		
	}
	
	public boolean addedDoor = false;
	
	@Override
	public void onUpdate() {
		onUpdateForReal();
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
		if (!world.isRemote)
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
	
	@Override
	public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
		return EnumActionResult.SUCCESS;
	}
	
	// ================Copy================
	
	protected abstract void copyExtra(EntityOldAnimation animation);
	
	public EntityOldAnimation copy() {
		EntityOldAnimation animation = null;
		try {
			animation = this.getClass().getConstructor(World.class).newInstance(this.world);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		animation.setUniqueId(getUniqueID());
		animation.fakeWorld = fakeWorld;
		animation.setCenterVec(center.copy(), additionalAxis.copy());
		animation.previews = previews.copy();
		animation.blocks = blocks;
		
		animation.worldBoundingBox = worldBoundingBox;
		animation.worldCollisionBoxes = new ArrayList<>(worldCollisionBoxes);
		// if(collisionBoxes != null)
		// animation.collisionBoxes = new ArrayList<>(collisionBoxes);
		
		if (world.isRemote) {
			animation.renderChunks = renderChunks;
			animation.renderQueue = renderQueue;
		}
		
		animation.prevWorldRotX = prevWorldRotX;
		animation.prevWorldRotY = prevWorldRotY;
		animation.prevWorldRotZ = prevWorldRotZ;
		
		animation.worldRotX = worldRotX;
		animation.worldRotY = worldRotY;
		animation.worldRotZ = worldRotZ;
		
		// animation.startOffset = startOffset;
		
		copyExtra(animation);
		
		return animation;
	}
	
	// ================Saving & Loading================
	
	protected void reloadPreviews(LittleStructure parent, BlockPos pos) {
		previews = parent.getAbsolutePreviews(pos);
	}
	
	public LittleStructure getParentStructure() {
		for (TileEntityLittleTiles te : blocks) {
			for (LittleTile tile : te) {
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
		setCenterVec(new LittleAbsoluteVec("axis", compound), new LittleVec("additional", compound));
		NBTTagList list = compound.getTagList("tileEntity", compound.getId());
		blocks = new ArrayList<>();
		LittleStructure parent = null;
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound nbt = list.getCompoundTagAt(i);
			TileEntityLittleTiles te = (TileEntityLittleTiles) TileEntity.create(fakeWorld, nbt);
			te.setWorld(fakeWorld);
			blocks.add(te);
			for (LittleTile tile : te) {
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
		BlockPos pos;
		if (array.length == 3)
			pos = new BlockPos(array[0], array[1], array[2]);
		else
			pos = baseOffset;
		
		reloadPreviews(parent, pos);
		
		updateWorldCollision();
		updateBoundingBox();
	}
	
	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		center.writeToNBT("axis", compound);
		additionalAxis.writeToNBT("additional", compound);
		
		compound.setBoolean("subworld", fakeWorld.hasParent());
		
		NBTTagList list = new NBTTagList();
		
		for (Iterator<TileEntityLittleTiles> iterator = blocks.iterator(); iterator.hasNext();) {
			TileEntityLittleTiles te = iterator.next();
			list.appendTag(te.writeToNBT(new NBTTagCompound()));
		}
		
		compound.setTag("tileEntity", list);
		
		compound.setIntArray("previewPos", new int[] { previews.pos.getX(), previews.pos.getY(), previews.pos.getZ() });
		
	}
	
}
