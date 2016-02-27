package com.creativemd.littletiles.common.structure;

import java.util.ArrayList;

import com.creativemd.creativecore.common.gui.SubGui;
import com.creativemd.creativecore.common.gui.controls.GuiButton;
import com.creativemd.creativecore.common.gui.event.ControlClickEvent;
import com.creativemd.creativecore.common.utils.Rotation;
import com.creativemd.creativecore.common.utils.RotationUtils;
import com.creativemd.creativecore.common.utils.RotationUtils.Axis;
import com.creativemd.littletiles.common.gui.SubGuiStructure;
import com.creativemd.littletiles.common.gui.controls.GuiTileViewer;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;
import com.creativemd.littletiles.utils.PreviewTile;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class LittleDoor extends LittleStructure{

	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		if(nbt.hasKey("ax"))
		{
			axisVec = new LittleTileVec("a", nbt);
			if(mainTile != null)
				axisVec.subVec(mainTile.cornerVec);
		}else{
			axisVec = new LittleTileVec("av", nbt);
		}
		axis = Axis.getAxis(nbt.getInteger("axis"));
		normalDirection = ForgeDirection.getOrientation(nbt.getInteger("ndirection"));
	}

	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		//axisPoint.writeToNBT("a", nbt);
		axisVec.writeToNBT("av", nbt);
		nbt.setInteger("axis", axis.toInt());
		nbt.setInteger("ndirection", RotationUtils.getIndex(normalDirection));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void createControls(SubGui gui, LittleStructure structure) {
		LittleDoor door = null;
		if(structure instanceof LittleDoor)
			door = (LittleDoor) structure;
		GuiTileViewer tile = new GuiTileViewer("tileviewer", 5, 30, 100, 100, ((SubGuiStructure) gui).stack);
		if(door != null)
		{
			tile.axisDirection = door.axis;
			/*tile.axisX = door.axisPoint.x;
			tile.axisY = door.axisPoint.y;
			tile.axisZ = door.axisPoint.z;*/
			tile.axisX = door.axisVec.x;
			tile.axisY = door.axisVec.y;
			tile.axisZ = door.axisVec.z;
			tile.normalAxis = Axis.getAxis(door.normalDirection);
		}
		tile.visibleAxis = true;
		tile.updateViewDirection();
		gui.controls.add(tile);
		gui.controls.add(new GuiButton("reset view", 105, 30, 70));
		//gui.controls.add(new GuiButton("y", 170, 50, 20));
		gui.controls.add(new GuiButton("flip view", 105, 50, 70));
		
		gui.controls.add(new GuiButton("swap axis", 105, 10, 70));
		gui.controls.add(new GuiButton("swap normal", 105, 70, 70));
		//gui.controls.add(new GuiButton("-->", 150, 50, 20));
		
		
		//gui.controls.add(new GuiButton("<-Z", 130, 70, 20));
		gui.controls.add(new GuiButton("up", "<-", 125, 90, 20).setRotation(90));
		gui.controls.add(new GuiButton("->", 145, 110, 20));
		gui.controls.add(new GuiButton("<-", 105, 110, 20));
		gui.controls.add(new GuiButton("down", "<-", 125, 110, 20).setRotation(-90));
		//gui.controls.add(new GuiButton("->", 190, 90, 20));
		//gui.controls.add(new GuiStateButton("direction", 3, 130, 50, 50, 20, "NORTH", "SOUTH", "WEST", "EAST"));
	}
	
	public ForgeDirection normalDirection;
	public Axis axis;
	public LittleTileVec axisVec;
	
	public LittleTileVec getAxisVec()
	{
		LittleTileVec newAxisVec = axisVec.copy();
		newAxisVec.addVec(mainTile.cornerVec);
		return newAxisVec;
	}
	
	@CustomEventSubscribe
	@SideOnly(Side.CLIENT)
	public void onButtonClicked(ControlClickEvent event)
	{
		GuiTileViewer viewer = (GuiTileViewer) event.source.parent.getControl("tileviewer");
		if(event.source.is("swap axis"))
		{
			switch (viewer.axisDirection) {
			case Xaxis:
				axis = Axis.Yaxis;
				break;
			case Yaxis:
				axis = Axis.Zaxis;
				break;
			case Zaxis:
				axis = Axis.Xaxis;
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
			viewer.scale = 1;
			//viewer.viewDirection = ForgeDirection.EAST;
			//((GuiStateButton) event.source.parent.getControl("direction")).setState(3);
		}else if(event.source.is("flip view"))
		{
			viewer.viewDirection = viewer.viewDirection.getOpposite();
			
			//viewer.viewDirection = ForgeDirection.getOrientation(((GuiStateButton) event.source).getState()+2);
		}else if(event.source instanceof GuiButton){			
			if(event.source.is("<-"))
			{
				if(viewer.axisDirection == Axis.Xaxis)
					viewer.axisZ++;
				else
					viewer.axisX--;
			}
			if(event.source.is("->"))
			{
				if(viewer.axisDirection == Axis.Xaxis)
					viewer.axisZ--;
				else
					viewer.axisX++;
			}
			if(event.source.is("up"))
			{
				if(viewer.axisDirection == Axis.Yaxis)
					viewer.axisZ--;
				else
					viewer.axisY++;
				
			}
			if(event.source.is("down"))
			{
				if(viewer.axisDirection == Axis.Yaxis)
					viewer.axisZ++;
				else
					viewer.axisY--;
			}else if(event.source.is("swap normal")){
				viewer.changeNormalAxis();
			}
		}
		/*if(event.source.is("<--"))
		{
			viewer.viewDirection = viewer.viewDirection.getOpposite();
			//viewer.viewDirection = ForgeDirection.getOrientation(((GuiStateButton) event.source).getState()+2);
		}*/
		
		
		
	}
	
	@Override
	public ArrayList<PreviewTile> getSpecialTiles()
	{
		ArrayList<PreviewTile> boxes = new ArrayList<>();
		LittleTileBox box = new LittleTileBox(axisVec.x, axisVec.y, axisVec.z, axisVec.x+1, axisVec.y+1, axisVec.z+1);
		
		boxes.add(new PreviewTileAxis(box, null, axis));
		return boxes;
	}
	
	@Override
	public void onFlip(World world, EntityPlayer player, ItemStack stack, ForgeDirection direction)
	{
		LittleTileBox box = new LittleTileBox(axisVec.x, axisVec.y, axisVec.z, axisVec.x+1, axisVec.y+1, axisVec.z+1);
		box.flipBoxWithCenter(direction, null);
		axisVec = box.getMinVec();
	}
	
	
	@Override
	public void onRotate(World world, EntityPlayer player, ItemStack stack, ForgeDirection direction) 
	{
		LittleTileBox box = new LittleTileBox(axisVec.x, axisVec.y, axisVec.z, axisVec.x+1, axisVec.y+1, axisVec.z+1);
		box.rotateBox(direction);
		axisVec = box.getMinVec();
		//ForgeDirection axisDirection = axis.getDirection();
		this.axis = Axis.getAxis(RotationUtils.rotateForgeDirection(axis.getDirection(), direction));
		//this.axis = Axis.getAxis(direction.getRotation(axis.getDirection()));
		this.normalDirection = RotationUtils.rotateForgeDirection(normalDirection, direction);
		//if(RotationUtils.isNegative(normalDirection))
			//normalDirection = normalDirection.getOpposite();
		/*Axis before = axis;
		if(axisDirection == Axis.Yaxis)
		{
			switch(axis)
			{
			case Xaxis:
				this.axis = Axis.Yaxis;
				break;
			case Yaxis:
				this.axis = Axis.Xaxis;
				break;
			default:
				break;
			}
		}else{
			switch(axis)
			{
			case Xaxis:
				this.axis = Axis.Zaxis;
				break;
			case Zaxis:
				this.axis = Axis.Xaxis;
				break;
			default:
				break;
			}
		}*/
		//if(before != axis)
		//	updateNormalDirection();
	}
	
	/*public static LittleTileVec getSize(ArrayList<PreviewTile> previews)
	{
		byte minX = LittleTile.maxPos;
		byte minY = LittleTile.maxPos;
		byte minZ = LittleTile.maxPos;
		byte maxX = LittleTile.minPos;
		byte maxY = LittleTile.minPos;
		byte maxZ = LittleTile.minPos;
		for (int i = 0; i < previews.size(); i++) {
			PreviewTile tile = previews.get(i);
			minX = (byte) Math.min(minX, tile.box.minX);
			minY = (byte) Math.min(minY, tile.box.minY);
			minZ = (byte) Math.min(minZ, tile.box.minZ);
			maxX = (byte) Math.max(maxX, tile.box.maxX);
			maxY = (byte) Math.max(maxY, tile.box.maxY);
			maxZ = (byte) Math.max(maxZ, tile.box.maxZ);
		}
		return new LittleTileVec(maxX-minX, maxY-minY, maxZ-minZ);
	}*/
	
	public boolean tryToPlacePreviews(World world, EntityPlayer player, int x, int y, int z, Rotation direction, ArrayList<PreviewTile> defaultpreviews, boolean inverse)
	{
		LittleTileVec axisPoint = getAxisVec();
		LittleTileVec internalOffset = new LittleTileVec(axisPoint.x-x*16, axisPoint.y-y*16, axisPoint.z-z*16);
		
		/*LittleTileVec missingOffset = new LittleTileVec(0, 0, 0);
		switch (direction) {
		case EAST:
			//internalOffset.x++;
			break;
		case WEST:
			//missingOffset.x++;
			break;
		case UP:
			//internalOffset.y++;
			break;
		case DOWN:
			//missingOffset.y++;
			break;
		case SOUTH:
			//internalOffset.z++;
			break;
		case NORTH:
			missingOffset.z--;
			break;
		default:
			break;
		}
		LittleTileVec missingInfOffset = missingOffset.copy();
		missingInfOffset.invert();*/
		ArrayList<PreviewTile> previews = new ArrayList<>();
		//LittleTileVec blockOffset = new LittleTileVec(8, 8, 8);
		//LittleTileVec blockInvOffset = blockOffset.copy();
		//blockInvOffset.invert();
		for (int i = 0; i < defaultpreviews.size(); i++) {
			PreviewTile box = defaultpreviews.get(i).copy();
			
			//box.box.addOffset(missingOffset);
			box.box.rotateBoxWithCenter(direction, Vec3.createVectorHelper(1/32D, 1/32D, 1/32D));
			//box.box.rotateBoxWithCenter(direction, Vec3.createVectorHelper(0, 0, 0));
			//box.box.addOffset(missingInfOffset);
			//box.box.addOffset(blockInvOffset);
			//box.box.addOffset(new LittleTileVec(0, 0, 1));
			box.box.addOffset(internalOffset);
			previews.add(box);
		}
		
		/*LittleTileVec size = getSize(previews);
		
		switch(direction)
		{
		case SOUTH:
			size.invert();
			size.x = 0;
			size.y = 0;
			//size.z = 0;
			break;
		default:
			break;
		}*/
			
		/*for (int i = 0; i < previews.size(); i++) {
			
			//PreviewTile box = previews.get(i);
			previews.get(i).box.addOffset(size);
		}*/
		
		LittleDoor structure = new LittleDoor();
		structure.dropStack = dropStack.copy();
		structure.axisVec = new LittleTileVec(0, 0, 0);
		structure.setTiles(new ArrayList<LittleTile>());
		structure.axis = this.axis;
		
		ForgeDirection rotationAxis = this.axis.getDirection();
		if(inverse)
			rotationAxis = rotationAxis.getOpposite();
		structure.normalDirection = this.normalDirection.getRotation(rotationAxis);
		
		/*Axis directionAxis = Axis.getAxis(this.axis);
		switch(directionAxis)
		{
		case Xaxis:
			if(directionAxis == Axis.Yaxis)
				structure.normalAxis = Axis.Zaxis;
			else
				structure.normalAxis = Axis.Yaxis;
			break;
		case Yaxis:
			if(directionAxis == Axis.Zaxis)
				structure.normalAxis = Axis.Xaxis;
			else
				structure.normalAxis = Axis.Zaxis;
			break;
		case Zaxis:
			if(directionAxis == Axis.Yaxis)
				structure.normalAxis = Axis.Xaxis;
			else
				structure.normalAxis = Axis.Yaxis;
			break;
		default:
			break;
		}*/
		
		if(ItemBlockTiles.placeTiles(world, player, previews, structure, x, y, z, null, null))
		{
			ArrayList<LittleTile> tiles = getTiles();
			for (int i = 0; i < tiles.size(); i++) {
				tiles.get(i).te.update();
			}
			tiles.clear();
			tiles = structure.getTiles();
			for (int i = 0; i < tiles.size(); i++) {
				tiles.get(i).te.combineTiles();
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onBlockActivated(World world, LittleTile tile, int x, int y, int z, EntityPlayer player, int side, float moveX, float moveY, float moveZ)
	{
		if(axis != null)
		{
			if(!hasLoaded())
			{
				player.addChatComponentMessage(new ChatComponentText("Cannot interact with door! Not all tiles are loaded!"));
				return true;
			}
			ArrayList<PreviewTile> previews = new ArrayList<>();
			
			LittleTileVec axisPoint = getAxisVec();
			
			int mainX = axisPoint.x/16;
			int mainY = axisPoint.y/16;
			int mainZ = axisPoint.z/16;
			
			
			//LittleTileVec internalOffset = new LittleTileVec(axis.x-mainX*16, axis.y-mainY*16, axis.z-mainZ*16);
			//internalOffset.invert();
			LittleTileVec invaxis = axisPoint.copy();
			//internalOffset.invert();
			//invaxis.addVec(internalOffset);
			invaxis.invert();
			
			//invaxis.addVec(internalOffset);
			ArrayList<LittleTile> tiles = getTiles();
			for (int i = 0; i < tiles.size(); i++) {
				LittleTile tileOfList = tiles.get(i);
				for (int j = 0; j < tileOfList.boundingBoxes.size(); j++) {
					NBTTagCompound nbt = new NBTTagCompound();
					tileOfList.saveTile(nbt);
					LittleTileBox box = tileOfList.boundingBoxes.get(j).copy();
					box.addOffset(new LittleTileVec(tileOfList.te.xCoord*16, tileOfList.te.yCoord*16, tileOfList.te.zCoord*16));
					box.addOffset(invaxis);
					//box.addOffset(internalOffset);
					//box.set(-box.minX, -box.minY, -box.minZ, -box.maxX, -box.maxY, -box.maxZ);
					//box.addOffset(internalOffset);
					PreviewTile preview = new PreviewTile(box, new LittleTilePreview(box, nbt));
					previews.add(preview);
					//tiles.get(i).boundingBoxes.get(j).rotateBox(direction);
				}
				
			}
			
			previews.add(new PreviewTileAxis(new LittleTileBox(0, 0, 0, 1, 1, 1), null, axis));
						
			//Calculate rotation
			Rotation rotation = Rotation.EAST;
			//Axis directionAxis = Axis.getAxis(axis);
			double playerRotation = MathHelper.wrapAngleTo180_float(player.rotationYaw);
			boolean rotX = playerRotation <= -90 || playerRotation >= 90;
			boolean rotY = player.rotationPitch > 0;
			boolean rotZ = playerRotation > 0 && playerRotation <= 180;
			boolean inverse = false;
			
			switch(axis)
			{
			case Xaxis:
				//System.out.println(player.rotationPitch);
				System.out.println(normalDirection);
				rotation = Rotation.UPX;
				switch(normalDirection)
				{
				case UP:
					if(!rotY)
						rotation = Rotation.DOWNX;
					break;
				case DOWN:
					if(rotY)
						rotation = Rotation.DOWNX;
					break;
				case SOUTH:
					if(!rotX)
						rotation = Rotation.DOWNX;
					break;
				case NORTH:
					if(rotX)
						rotation = Rotation.DOWNX;			
					break;
				default:
					break;
				}
				inverse = rotation == Rotation.UPX;
				break;
			case Yaxis:
				rotation = Rotation.SOUTH;
				switch(normalDirection)
				{
				case EAST:
					if(rotX)
						rotation = Rotation.NORTH;
					break;
				case WEST:
					if(!rotX)
						rotation = Rotation.NORTH;			
					break;
				case SOUTH:
					if(!rotZ)
						rotation = Rotation.NORTH;
					break;
				case NORTH:
					if(rotZ)
						rotation = Rotation.NORTH;
					break;
				default:
					break;
				}
				inverse = rotation == Rotation.SOUTH;
				break;
			case Zaxis:
				//System.out.println(player.rotationPitch);
				//System.out.println(normalDirection);
				rotation = Rotation.UP;				
				switch(normalDirection)
				{
				case EAST:
					if(rotZ)
						rotation = Rotation.DOWN;
					break;
				case WEST:
					if(!rotZ)
						rotation = Rotation.DOWN;			
					break;
				case UP:
					if(!rotY)
						rotation = Rotation.DOWN;
					break;
				case DOWN:
					if(rotY)
						rotation = Rotation.DOWN;
					break;
				default:
					break;
				}
				inverse = rotation == Rotation.UP;
				break;
			default:
				break;
			}
			
			/*for (int i = 0; i < previews.size(); i++) {
				previews.get(i).box.addOffset(internalOffset);
			}*/
			
			for (int i = 0; i < tiles.size(); i++) {
				tiles.get(i).te.tiles.remove(tiles.get(i));
			}
			
			if(tryToPlacePreviews(world, player, mainX, mainY, mainZ, rotation, previews, !inverse))
				return true;
			else if(tryToPlacePreviews(world, player, mainX, mainY, mainZ, rotation.getOpposite(), previews, inverse))
				return true;
			
			
			
			for (int i = 0; i < tiles.size(); i++) {
				tiles.get(i).te.tiles.add(tiles.get(i));
			}
			return true;
			
			//tiles.get(i).te.combineTiles();
		}
		return true;
	}
	
	public void updateNormalDirection()
	{
		switch(axis)
		{
		case Xaxis:
			normalDirection = Axis.Zaxis.getDirection();
			break;
		case Yaxis:
			normalDirection = Axis.Xaxis.getDirection();
			break;
		case Zaxis:
			normalDirection = Axis.Yaxis.getDirection();
			break;
		default:
			break;
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public LittleStructure parseStructure(SubGui gui) {
		LittleDoor door = new LittleDoor();
		GuiTileViewer viewer = (GuiTileViewer) gui.getControl("tileviewer");
		door.axisVec = new LittleTileVec(viewer.axisX, viewer.axisY, viewer.axisZ);
		door.axis = viewer.axisDirection;
		door.normalDirection = viewer.normalAxis.getDirection();
		//door.updateNormalDirection();
		return door; 
	}

}
