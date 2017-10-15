package com.creativemd.littletiles.common.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.creativecore.common.utils.Rotation;
import com.creativemd.creativecore.common.utils.RotationUtils;
import com.creativemd.creativecore.common.world.WorldFake;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.gui.controls.gui.GuiIDButton;
import com.creativemd.creativecore.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.gui.event.gui.GuiControlClickEvent;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.gui.SubGuiStructure;
import com.creativemd.littletiles.common.gui.controls.GuiTileViewer;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.packet.LittleDoorInteractPacket;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileCoord;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.rotation.OrdinaryDoorTransformation;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleDoor extends LittleDoorBase{

	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		super.loadFromNBTExtra(nbt);
		if(nbt.hasKey("ax"))
		{
			axisVec = new LittleTileVec("a", nbt);
			if(getMainTile() != null)
				axisVec.sub(getMainTile().getCornerVec());
		}else{
			axisVec = new LittleTileVec("av", nbt);
		}
		axis = EnumFacing.Axis.values()[nbt.getInteger("axis")];
		normalDirection = EnumFacing.getFront(nbt.getInteger("ndirection"));
	}

	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		super.writeToNBTExtra(nbt);
		axisVec.writeToNBT("av", nbt);
		nbt.setInteger("axis", axis.ordinal());
		nbt.setInteger("ndirection", normalDirection.getIndex());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void createControls(SubGui gui, LittleStructure structure) {
		super.createControls(gui, structure);
		LittleDoor door = null;
		if(structure instanceof LittleDoor)
			door = (LittleDoor) structure;
		GuiTileViewer tile = new GuiTileViewer("tileviewer", 0, 30, 100, 100, ((SubGuiStructure) gui).stack);
		if(door != null)
		{
			tile.axisDirection = door.axis;
			/*tile.axisX = door.axisPoint.x;
			tile.axisY = door.axisPoint.y;
			tile.axisZ = door.axisPoint.z;*/
			tile.axisX = door.axisVec.x;
			tile.axisY = door.axisVec.y;
			tile.axisZ = door.axisVec.z;
			tile.normalAxis = door.normalDirection.getAxis();
		}
		tile.visibleAxis = true;
		tile.updateViewDirection();
		gui.controls.add(tile);
		gui.controls.add(new GuiIDButton("reset view", 109, 30, 0));
		//gui.controls.add(new GuiButton("y", 170, 50, 20));
		gui.controls.add(new GuiIDButton("flip view", 109, 50, 1));
		
		gui.controls.add(new GuiIDButton("swap axis", 109, 10, 2));
		gui.controls.add(new GuiIDButton("swap normal", 109, 70, 3));
		//gui.controls.add(new GuiButton("-->", 150, 50, 20));
		
		
		//gui.controls.add(new GuiButton("<-Z", 130, 70, 20));
		gui.controls.add(new GuiButton("up", "<-", 125, 91, 14){
			@Override
			public void onClicked(int x, int y, int button)
			{
				
			}
			
		}.setRotation(90));
		gui.controls.add(new GuiIDButton("->", 146, 112, 4));
		gui.controls.add(new GuiIDButton("<-", 107, 112, 5));
		gui.controls.add(new GuiButton("down", "<-", 125, 112, 14){
			@Override
			public void onClicked(int x, int y, int button)
			{
				
			}
			
		}.setRotation(-90));
		//gui.controls.add(new GuiButton("->", 190, 90, 20));
		//gui.controls.add(new GuiStateButton("direction", 3, 130, 50, 50, 20, "NORTH", "SOUTH", "WEST", "EAST"));
	}
	
	public EnumFacing normalDirection;
	public EnumFacing.Axis axis;
	public LittleTileVec axisVec;
	public LittleTileVec lastMainTileVec = null;
	
	@Override
	public LittleTileVec getAxisVec()
	{
		LittleTileVec newAxisVec = axisVec.copy();
		newAxisVec.add(getMainTile().getAbsoluteCoordinates());
		return newAxisVec;
	}
	
	@Override
	public void moveStructure(EnumFacing facing)
	{
		axisVec.add(new LittleTileVec(facing));
	}
	
	@Override
	public void setMainTile(LittleTile tile)
	{
		if(getMainTile() != null)
		{
			LittleTileVec oldVec = lastMainTileVec;
			oldVec.sub(tile.getAbsoluteCoordinates());
			axisVec.add(oldVec);
		}
		lastMainTileVec = tile.getAbsoluteCoordinates().copy();
		super.setMainTile(tile);
	}
	
	@CustomEventSubscribe
	@SideOnly(Side.CLIENT)
	public void onButtonClicked(GuiControlClickEvent event)
	{
		GuiTileViewer viewer = (GuiTileViewer) event.source.parent.get("tileviewer");
		if(event.source.is("swap axis"))
		{
			switch (viewer.axisDirection) {
			case X:
				axis = EnumFacing.Axis.Y;
				break;
			case Y:
				axis = EnumFacing.Axis.Z;
				break;
			case Z:
				axis = EnumFacing.Axis.X;
				break;
			default:
				break;
			}
			viewer.axisDirection = axis;
			viewer.updateViewDirection();
			
			viewer.updateNormalAxis();
			//viewer.axisDirection
		}else if(event.source.is("reset view"))
		{
			viewer.offsetX = 0;
			viewer.offsetY = 0;
			viewer.scale = 5;
			//viewer.viewDirection = ForgeDirection.EAST;
			//((GuiStateButton) event.source.parent.getControl("direction")).setState(3);
		}else if(event.source.is("flip view"))
		{
			viewer.viewDirection = viewer.viewDirection.getOpposite();
			viewer.baked = null;
			//viewer.viewDirection = ForgeDirection.getOrientation(((GuiStateButton) event.source).getState()+2);
		}else if(event.source instanceof GuiButton){			
			if(event.source.is("<-"))
			{
				if(viewer.axisDirection == Axis.X)
					viewer.axisZ++;
				else
					viewer.axisX--;
			}
			if(event.source.is("->"))
			{
				if(viewer.axisDirection == Axis.X)
					viewer.axisZ--;
				else
					viewer.axisX++;
			}
			if(event.source.is("up"))
			{
				if(viewer.axisDirection == Axis.Y)
					viewer.axisZ--;
				else
					viewer.axisY++;
				
			}
			if(event.source.is("down"))
			{
				if(viewer.axisDirection == Axis.Y)
					viewer.axisZ++;
				else
					viewer.axisY--;
			}else if(event.source.is("swap normal")){
				viewer.changeNormalAxis();
			}
		}		
	}
	
	@Override
	public ArrayList<PlacePreviewTile> getSpecialTiles()
	{
		ArrayList<PlacePreviewTile> boxes = new ArrayList<>();
		LittleTileBox box = new LittleTileBox(axisVec);
		
		boxes.add(new PlacePreviewTileAxis(box, null, axis));
		return boxes;
	}
	
	@Override
	public void onFlip(World world, EntityPlayer player, ItemStack stack, Axis axis)
	{
		LittleTileBox box = new LittleTileBox(axisVec.x, axisVec.y, axisVec.z, axisVec.x+1, axisVec.y+1, axisVec.z+1);
		box.flipBox(axis);
		axisVec = box.getMinVec();
	}
	
	
	@Override
	public void onRotate(World world, EntityPlayer player, ItemStack stack, Rotation rotation) 
	{
		LittleTileBox box = new LittleTileBox(axisVec.x, axisVec.y, axisVec.z, axisVec.x+1, axisVec.y+1, axisVec.z+1);
		box.rotateBox(rotation);
		axisVec = box.getMinVec();
		this.axis = RotationUtils.rotateFacing(RotationUtils.getFacing(axis), rotation).getAxis();
		this.normalDirection = RotationUtils.rotateFacing(normalDirection, rotation);
	}
	
	
	public boolean tryToPlacePreviews(World world, EntityPlayer player, BlockPos pos, Rotation direction, boolean inverse, UUID uuid)
	{
		ArrayList<PlacePreviewTile> defaultpreviews = new ArrayList<>();
		LittleTileVec axisPoint = getAxisVec();
		
		LittleTileVec invaxis = axisPoint.copy();
		invaxis.invert();
		
		for (Iterator iterator = getTiles(); iterator.hasNext();) {
			LittleTile tileOfList = (LittleTile) iterator.next();
			NBTTagCompound nbt = new NBTTagCompound();
			
			LittleTilePreview preview = tileOfList.getPreviewTile();
			preview.box.addOffset(tileOfList.te.getPos());
			preview.box.addOffset(invaxis);
			
			preview.rotatePreview(direction);
			
			defaultpreviews.add(preview.getPlaceableTile(preview.box, false, new LittleTileVec(0, 0, 0)));
		}
		
		defaultpreviews.add(new PlacePreviewTileAxis(new LittleTileBox(0, 0, 0, 1, 1, 1), null, axis));
		
		LittleTileVec internalOffset = new LittleTileVec(axisPoint.x-pos.getX()*LittleTile.gridSize, axisPoint.y-pos.getY()*LittleTile.gridSize, axisPoint.z-pos.getZ()*LittleTile.gridSize);
		ArrayList<PlacePreviewTile> previews = new ArrayList<>();
		for (int i = 0; i < defaultpreviews.size(); i++) {
			PlacePreviewTile box = defaultpreviews.get(i);
			box.box.addOffset(internalOffset);
			previews.add(box);
		}
		
		LittleDoor structure = new LittleDoor();
		structure.axisVec = new LittleTileVec(0, 0, 0);
		structure.setTiles(new HashMapList<>());
		structure.axis = this.axis;
		
		EnumFacing rotationAxis = RotationUtils.getFacing(this.axis);
		if(inverse)
			rotationAxis = rotationAxis.getOpposite();
		structure.normalDirection = RotationUtils.rotateFacing(normalDirection, direction);
		structure.duration = this.duration;
		
		return place(world, structure, player, previews, pos, new OrdinaryDoorTransformation(direction), uuid);
	}
	
	@Override
	public boolean onBlockActivated(World world, LittleTile tile, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if(world.isRemote && axis != null && !isWaitingForApprove)
		{
			if(!hasLoaded())
			{
				player.sendStatusMessage(new TextComponentTranslation("Cannot interact with door! Not all tiles are loaded!"), true);
				return true;
			}
						
			//Calculate rotation
			Rotation rotation = null;
			double playerRotation = MathHelper.wrapDegrees(player.rotationYaw);
			boolean rotX = playerRotation <= -90 || playerRotation >= 90;
			boolean rotY = player.rotationPitch > 0;
			boolean rotZ = playerRotation > 0 && playerRotation <= 180;
			boolean inverse = false;
			
			switch(axis)
			{
			case X:
				//System.out.println(normalDirection);
				rotation = Rotation.X_CLOCKWISE;
				switch(normalDirection)
				{
				case UP:
					if(!rotY)
						rotation = Rotation.X_COUNTER_CLOCKWISE;
					break;
				case DOWN:
					if(rotY)
						rotation = Rotation.X_COUNTER_CLOCKWISE;
					break;
				case SOUTH:
					if(!rotX)
						rotation = Rotation.X_COUNTER_CLOCKWISE;
					break;
				case NORTH:
					if(rotX)
						rotation = Rotation.X_COUNTER_CLOCKWISE;			
					break;
				default:
					break;
				}
				inverse = rotation == Rotation.X_CLOCKWISE;
				break;
			case Y:
				rotation = Rotation.Y_CLOCKWISE;
				switch(normalDirection)
				{
				case EAST:
					if(rotX)
						rotation = Rotation.Y_COUNTER_CLOCKWISE;
					break;
				case WEST:
					if(!rotX)
						rotation = Rotation.Y_COUNTER_CLOCKWISE;			
					break;
				case SOUTH:
					if(!rotZ)
						rotation = Rotation.Y_COUNTER_CLOCKWISE;
					break;
				case NORTH:
					if(rotZ)
						rotation = Rotation.Y_COUNTER_CLOCKWISE;
					break;
				default:
					break;
				}
				inverse = rotation == Rotation.Y_CLOCKWISE;
				break;
			case Z:
				rotation = Rotation.Z_CLOCKWISE;				
				switch(normalDirection)
				{
				case EAST:
					if(rotZ)
						rotation = Rotation.Z_COUNTER_CLOCKWISE;
					break;
				case WEST:
					if(!rotZ)
						rotation = Rotation.Z_COUNTER_CLOCKWISE;			
					break;
				case UP:
					if(!rotY)
						rotation = Rotation.Z_COUNTER_CLOCKWISE;
					break;
				case DOWN:
					if(rotY)
						rotation = Rotation.Z_COUNTER_CLOCKWISE;
					break;
				default:
					break;
				}
				inverse = rotation == Rotation.Z_CLOCKWISE;
				break;
			default:
				break;
			}
			
			UUID uuid = UUID.randomUUID();
			PacketHandler.sendPacketToServer(new LittleDoorInteractPacket(pos, player, rotation, inverse, uuid));
			interactWithDoor(world, player, rotation, inverse, uuid);
		}
		return true;
	}
	
	public boolean interactWithDoor(World world, EntityPlayer player, Rotation rotation, boolean inverse, UUID uuid)
	{		
		LoadList();
		
		LittleTileVec axisPoint = getAxisVec();
		int mainX = axisPoint.x/LittleTile.gridSize;
		int mainY = axisPoint.y/LittleTile.gridSize;
		int mainZ = axisPoint.z/LittleTile.gridSize;
		
		for (Iterator<LittleTile> iterator = getTiles(); iterator.hasNext();) {
			LittleTile tile = iterator.next();
			tile.te.removeTile(tile);
		}
		
		if(tryToPlacePreviews(world, player, new BlockPos(mainX, mainY, mainZ), rotation, !inverse, uuid))
		{
			//System.out.println("Placing first! inverse:" + inverse + ",client:" + world.isRemote + ",rotation:" + rotation);
			return true;
		}
		else if(tryToPlacePreviews(world, player, new BlockPos(mainX, mainY, mainZ), rotation.getOpposite(), inverse, uuid))
		{
			//System.out.println("Placing second! inverse:" + inverse + ",client:" + world.isRemote + ",rotation:" + rotation.getOpposite());
			return true;
		}
		
		for (Iterator<LittleTile> iterator = getTiles(); iterator.hasNext();) {
			LittleTile tile = iterator.next();
			tile.te.addTile(tile);
		}
		
		return false;
	}
	
	public void updateNormalDirection()
	{
		switch(axis)
		{
		case X:
			normalDirection = RotationUtils.getFacing(Axis.Z);
			break;
		case Y:
			normalDirection = RotationUtils.getFacing(Axis.X);
			break;
		case Z:
			normalDirection = RotationUtils.getFacing(Axis.Y);
			break;
		default:
			break;
		}
	}
	
	@Override
	public void writeToNBTPreview(NBTTagCompound nbt, BlockPos newCenter)
	{
		LittleTileVec axisPointBackup = axisVec.copy();
		axisVec.sub(new LittleTileVec(newCenter));
		axisVec.add(new LittleTileVec(getMainTile().te.getPos()));
		axisVec.add(getMainTile().getCornerVec());
		super.writeToNBTPreview(nbt, newCenter);
		axisVec = axisPointBackup;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public LittleDoorBase parseStructure(SubGui gui, int duration) {
		LittleDoor door = new LittleDoor();
		GuiTileViewer viewer = (GuiTileViewer) gui.get("tileviewer");
		door.axisVec = new LittleTileVec(viewer.axisX, viewer.axisY, viewer.axisZ);
		door.axis = viewer.axisDirection;
		door.normalDirection = RotationUtils.getFacing(viewer.normalAxis);
		door.duration = duration;
		return door; 
	}

	@Override
	public LittleDoorBase copyToPlaceDoor() {
		LittleDoor structure = new LittleDoor();
		structure.axisVec = new LittleTileVec(0, 0, 0);
		structure.setTiles(new HashMapList<>());
		structure.axis = this.axis;
		structure.normalDirection = this.normalDirection;
		structure.duration = this.duration;
		return structure;
	}

	@Override
	public ArrayList<PlacePreviewTile> getAdditionalPreviews() {
		ArrayList<PlacePreviewTile> previews = new ArrayList<>();
		previews.add(new PlacePreviewTileAxis(new LittleTileBox(0, 0, 0, 1, 1, 1), null, axis));
		return previews;
	}

}
