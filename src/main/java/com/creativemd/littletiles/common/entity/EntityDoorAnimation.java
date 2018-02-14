package com.creativemd.littletiles.common.entity;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.littletiles.client.render.RenderingThread;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceRelative;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.structure.LittleDoorBase;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.rotation.DoorTransformation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityDoorAnimation extends EntityAnimation<EntityDoorAnimation> {
	
	private static final DataParameter<Integer> ENTITY_PROGRESS = EntityDataManager.<Integer>createKey(EntityDoorAnimation.class, DataSerializers.VARINT);
	
	public EntityPlayer activator;
	
	private int progress;
	
	public int duration;
	
	public boolean approved = true;
	
	public DoorTransformation transformation;
	
	public LittleTileVec additionalAxis;
	
	public void setTransformationStartOffset()
	{
		transformation.performTransformation(this, 0);
		prevWorldRotX = worldRotX;
		prevWorldRotY = worldRotY;
		prevWorldRotZ = worldRotZ;
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
	}

	public EntityDoorAnimation(World worldIn) {
		super(worldIn);
	}
	
	public EntityDoorAnimation(World world, BlockPos pos, LittleDoorBase structure, ArrayList<TileEntityLittleTiles> blocks, ArrayList<PlacePreviewTile> previews,
			LittleTileVec axis, DoorTransformation transformation, UUID uuid, EntityPlayer activator, LittleTileVec additionalAxis) {
		super(world, pos, blocks, previews, uuid, axis);
		
		this.activator = activator;
		this.structure = structure;
        
        this.transformation = transformation;
        this.duration = structure.duration;
        
        this.additionalAxis = additionalAxis.copy();
        
        setTransformationStartOffset();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void onScanningTE(TileEntityLittleTiles te)
	{
		if(world.isRemote)
		{
			te.rendering = new AtomicBoolean(false);
			te.setLoaded();
			RenderingThread.addCoordToUpdate(te, 0, false);
		}
		super.onScanningTE(te);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void createClient()
	{
		super.createClient();
		approved = false;
	}
	
	public void copyExtra(EntityDoorAnimation animation)
	{
		animation.progress = progress;
		animation.duration = duration;
		animation.approved = approved;
		animation.transformation = transformation;
		animation.lastSendProgress = lastSendProgress;
		animation.additionalAxis = additionalAxis.copy();
	}
	
	private int lastSendProgress = -1;
	
	public void setProgress(int progress)
	{
		this.progress = progress;
		if(!world.isRemote && (lastSendProgress == -1 || progress - lastSendProgress > 10 || progress == duration))
		{
			dataManager.set(ENTITY_PROGRESS, progress);
			lastSendProgress = progress;
		}		
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
        if (world.isRemote && ENTITY_PROGRESS.equals(key) && !isWaitingForRender())
        {
        	this.progress = this.dataManager.get(ENTITY_PROGRESS).intValue();
        }
    }
	
	@Override
	public void onTick()
	{
		if(transformation != null)
			transformation.performTransformation(this, progress/(double)duration);
	}
	
	@Override
	public void onPostTick()
	{
		if(transformation == null)
			return ;
		
		if(world.isRemote && isWaitingForRender())
		{
			ticksToWait--;
			if(waitingForRender.size() == 0 || ticksToWait < 0)
			{
				//System.out.println("KILL IT!");
				isDead = true;
			}else
				isDead = false;
		}else{
			if(progress >= duration)
			{
				//Try to place door, if not drop ItemStack
				LittleDoorBase structure = this.structure.copyToPlaceDoor();
				
				if(world.isRemote)
					structure.isWaitingForApprove = true;
				
				if(!world.isRemote || approved)
				{
					if(LittleActionPlaceRelative.placeTilesWithoutPlayer(world, previews, structure, false, baseOffset, null, null, false, EnumFacing.EAST) != null)
					{
						if(world.isRemote)
						{
							waitingForRender = new ArrayList<>();
							ArrayList<BlockPos> coordsToCheck = new ArrayList<>(LittleActionPlaceRelative.getSplittedTiles(previews, baseOffset).keySet());
							for (int i = 0; i < coordsToCheck.size(); i++) {
								TileEntity te = world.getTileEntity(coordsToCheck.get(i));
								if(te instanceof TileEntityLittleTiles)
								{
									((TileEntityLittleTiles) te).waitingAnimation = this;
									waitingForRender.add((TileEntityLittleTiles) te);
								}
							}
							ticksToWait = waitingForRender.size()*10;
							isDead = false;
							//System.out.println("Start waiting");
							return ;
						}
					}else if(!world.isRemote)
						WorldUtils.dropItem(world, this.structure.getStructureDrop(), baseOffset);
				}
				
				isDead = true;
				//setDead();
			}else
				setProgress(progress + 1);
		}
	}
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		duration = compound.getInteger("duration");
		setProgress(compound.getInteger("progress"));
		
		transformation = DoorTransformation.loadFromNBT(compound.getCompoundTag("transform"));
		
		additionalAxis = new LittleTileVec("additional", compound);
		
		setTransformationStartOffset();
		
	}
	
	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		
		compound.setInteger("duration", duration);
		compound.setInteger("progress", progress);
		
		compound.setTag("transform", transformation.writeToNBT(new NBTTagCompound()));
		
		additionalAxis.writeToNBT("additional", compound);
	}
	
}
