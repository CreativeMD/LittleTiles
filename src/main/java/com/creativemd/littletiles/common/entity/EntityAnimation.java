package com.creativemd.littletiles.common.entity;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.creativecore.common.utils.RotationUtils;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.creativecore.common.world.WorldFake;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.RenderingThread;
import com.creativemd.littletiles.client.render.entity.TERenderData;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.structure.LittleDoor;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.PreviewTileAxis;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.creativemd.littletiles.common.utils.rotation.DoorTransformation;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;
import com.creativemd.littletiles.utils.PlacePreviewTile;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityAnimation extends Entity {
	
	private static final DataParameter<Integer> ENTITY_PROGRESS = EntityDataManager.<Integer>createKey(EntityAnimation.class, DataSerializers.VARINT);
	
	public LittleDoor structure;
	public ArrayList<PlacePreviewTile> previews;
	public ArrayList<TileEntityLittleTiles> blocks;
	
	@SideOnly(Side.CLIENT)
	public HashMapList<BlockRenderLayer, TERenderData> renderData;
	
	@SideOnly(Side.CLIENT)
	public ArrayList<TileEntityLittleTiles> renderQueue;
	
	public void setAxisVec(LittleTileVec axis)
	{
		this.axis = axis;
        this.baseOffset = axis.getBlockPos();
        this.inBlockAxis = axis.copy();
        this.inBlockAxis.subVec(new LittleTileVec(baseOffset));
        this.chunkOffset = getRenderChunkPos(baseOffset);
        
        
        int chunkX = MathHelper.bucketInt(baseOffset.getX(), 16);
		int chunkY = MathHelper.bucketInt(baseOffset.getY(), 16);
		int chunkZ = MathHelper.bucketInt(baseOffset.getZ(), 16);
        
        inChunkOffset = new BlockPos(baseOffset.getX() - (chunkX*16), baseOffset.getY() - (chunkY*16), baseOffset.getZ() - (chunkZ*16));
	}
	
	private LittleTileVec axis;
	private LittleTileVec inBlockAxis;
	private BlockPos baseOffset;
	private BlockPos chunkOffset;
	private BlockPos inChunkOffset;
	
	public LittleTileVec getAxis()
	{
		return axis;
	}
	
	public LittleTileVec getInsideBlockAxis()
	{
		return inBlockAxis;
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
	
	private int progress;
	
	public int duration;
	
	public boolean approved = true;

	public EntityAnimation(World worldIn) {
		super(worldIn);
	}
	
	public static BlockPos getRenderChunkPos(BlockPos blockPos)
	{
		return new BlockPos(blockPos.getX() >> 4, blockPos.getY() >> 4, blockPos.getZ() >> 4);
	}
	
	public DoorTransformation transformation;
	
	public EntityAnimation(World world, LittleDoor structure, ArrayList<TileEntityLittleTiles> blocks, ArrayList<PlacePreviewTile> previews, LittleTileVec axis, DoorTransformation transformation, UUID uuid) {
		this(world);
		
		this.structure = structure;
		this.blocks = blocks;
		this.previews = previews;
		
		this.entityUniqueID = uuid;
        this.cachedUniqueIdString = this.entityUniqueID.toString();
        
        setAxisVec(axis);
        this.transformation = transformation;
        
        this.duration = structure.duration;
        
        //TODO TAKE CARE OF BOUNDING BOX!!!! and position
        
        if(world.isRemote)
        	createClient();
	}
	
	@SideOnly(Side.CLIENT)
	public void createClient()
	{
		this.renderData = new HashMapList<>();
		this.renderQueue = new ArrayList<>(blocks);
		for (Iterator<TileEntityLittleTiles> iterator = blocks.iterator(); iterator.hasNext();) {
			TileEntityLittleTiles te = iterator.next();
			te.rendering = new AtomicBoolean(false);
			RenderingThread.addCoordToUpdate(te, 0, false);
		}
		approved = false;
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		if(blocks == null)
			setDead();
		
		prevWorldRotX = worldRotX;
		prevWorldRotY = worldRotY;
		prevWorldRotZ = worldRotZ;
		
		if(transformation != null) //Client side
			transformation.performTransformation(this, progress/(double)duration);
		else
			return ;
		
		if(progress >= duration)
		{
			//Try to place door, if not drop ItemStack
			LittleDoor structure = new LittleDoor();
			structure.axisVec = new LittleTileVec(0, 0, 0);
			structure.setTiles(new ArrayList<LittleTile>());
			structure.axis = this.structure.axis;
			structure.normalDirection = this.structure.normalDirection;
			structure.duration = this.structure.duration;
			
			if(!world.isRemote || approved)
			{
				if(ItemBlockTiles.placeTiles(world, null, previews, structure, baseOffset, null, null, false, EnumFacing.EAST))
				{
					
				}else if(!world.isRemote)
					WorldUtils.dropItem(world, this.structure.getStructureDrop(), baseOffset);
			}
			
			setDead();
		}else
			setProgress(progress + 1);
	}
	
	public void setProgress(int progress)
	{
		this.progress = progress;
		dataManager.set(ENTITY_PROGRESS, progress);
	}
	
	public int getProgress()
	{
		return progress;
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(ENTITY_PROGRESS, 0);
	}
	
	 @Override
	    public void notifyDataManagerChange(DataParameter<?> key)
	    {
	        if (ENTITY_PROGRESS.equals(key))
	        	this.progress = this.dataManager.get(ENTITY_PROGRESS).intValue();
	    }

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		
		duration = compound.getInteger("duration");
		setProgress(compound.getInteger("progress"));
		
		transformation = DoorTransformation.loadFromNBT(compound.getCompoundTag("transform"));
		
		setAxisVec(new LittleTileVec("axis", compound));
		
		World worldFake = new WorldFake(worldObj);
		NBTTagList list = compound.getTagList("tileEntity", compound.getId());
		blocks = new ArrayList<>();
		ArrayList<LittleTile> tiles = new ArrayList<>();
		structure = null; 
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound nbt = list.getCompoundTagAt(i);
			TileEntityLittleTiles te = (TileEntityLittleTiles) TileEntity.func_190200_a(worldFake, nbt);
			te.setWorldObj(worldFake);
			blocks.add(te);
			for (Iterator<LittleTile> iterator = te.getTiles().iterator(); iterator.hasNext();) {
				LittleTile tile = iterator.next();
				if(tile.isMainBlock)
					this.structure = (LittleDoor) tile.structure;
				tiles.add(tile);
			}
			worldFake.setBlockState(te.getPos(), LittleTiles.blockTile.getDefaultState());
			worldFake.setTileEntity(te.getPos(), te);
		}
		
		ArrayList<PlacePreviewTile> defaultpreviews = new ArrayList<>();
		LittleTileVec axisPoint = structure.getAxisVec();
		
		LittleTileVec invaxis = axisPoint.copy();
		invaxis.invert();
		
		for (int i = 0; i < tiles.size(); i++) {
			LittleTile tileOfList = tiles.get(i);
			NBTTagCompound nbt = new NBTTagCompound();
			
			LittleTilePreview preview = tileOfList.getPreviewTile();
			preview.box.addOffset(new LittleTileVec(tileOfList.te.getPos()));
			preview.box.addOffset(invaxis);
			
			defaultpreviews.add(preview.getPlaceableTile(preview.box, false, new LittleTileVec(0, 0, 0)));
		}
		
		defaultpreviews.add(new PreviewTileAxis(new LittleTileBox(0, 0, 0, 1, 1, 1), null, structure.axis));
		
		LittleTileVec internalOffset = new LittleTileVec(axisPoint.x-baseOffset.getX()*LittleTile.gridSize, axisPoint.y-baseOffset.getY()*LittleTile.gridSize, axisPoint.z-baseOffset.getZ()*LittleTile.gridSize);
		previews = new ArrayList<>();
		for (int i = 0; i < defaultpreviews.size(); i++) {
			PlacePreviewTile box = defaultpreviews.get(i); //.copy();
			//box.box.rotateBoxWithCenter(direction, new Vec3d(1/32D, 1/32D, 1/32D));
			box.box.addOffset(internalOffset);
			previews.add(box);
		}
	}
	
	

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		compound.setInteger("duration", duration);
		compound.setInteger("progress", progress);
		
		compound.setTag("transform", transformation.writeToNBT(new NBTTagCompound()));
		
		axis.writeToNBT("axis", compound);
		
		NBTTagList list = new NBTTagList();
		
		for (Iterator<TileEntityLittleTiles> iterator = blocks.iterator(); iterator.hasNext();) {
			TileEntityLittleTiles te = iterator.next();
			list.appendTag(te.writeToNBT(new NBTTagCompound()));
		}
		
		compound.setTag("tileEntity", list);
	}
	
	

}
