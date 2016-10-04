package com.creativemd.littletiles.common.structure;

import java.util.ArrayList;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.Rotation;
import com.creativemd.creativecore.common.utils.RotationUtils;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.gui.controls.gui.GuiIDButton;
import com.creativemd.creativecore.gui.event.gui.GuiControlClickEvent;
import com.creativemd.littletiles.common.gui.SubGuiStructure;
import com.creativemd.littletiles.common.gui.controls.GuiTileViewer;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileCoord;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;
import com.creativemd.littletiles.utils.PreviewTile;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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

public class LittleDoor extends LittleStructure{

	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		if(nbt.hasKey("ax")) //TODO Add compability for non relative axis
		{
			axisVec = new LittleTileVec("a", nbt);
			if(getMainTile() != null)
				axisVec.subVec(getMainTile().cornerVec);
		}else{
			axisVec = new LittleTileVec("av", nbt);
		}
		axis = RotationUtils.getAxisFromIndex(nbt.getInteger("axis"));
		normalDirection = EnumFacing.getFront(nbt.getInteger("ndirection"));
	}

	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		//axisPoint.writeToNBT("a", nbt);
		axisVec.writeToNBT("av", nbt);
		nbt.setInteger("axis", RotationUtils.getAxisIndex(axis));
		nbt.setInteger("ndirection", normalDirection.getIndex());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void createControls(SubGui gui, LittleStructure structure) {
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
	
	public LittleTileVec getAxisVec()
	{
		LittleTileVec newAxisVec = axisVec.copy();
		newAxisVec.addVec(getMainTile().getAbsoluteCoordinates());
		return newAxisVec;
	}
	
	@Override
	public void moveStructure(EnumFacing facing)
	{
		axisVec.addVec(new LittleTileVec(facing));
	}
	
	@Override
	public void setMainTile(LittleTile tile)
	{
		if(getMainTile() != null)
		{
			LittleTileVec oldVec = lastMainTileVec;
			oldVec.subVec(tile.getAbsoluteCoordinates());
			axisVec.addVec(oldVec);
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
	public void onFlip(World world, EntityPlayer player, ItemStack stack, EnumFacing direction)
	{
		LittleTileBox box = new LittleTileBox(axisVec.x, axisVec.y, axisVec.z, axisVec.x+1, axisVec.y+1, axisVec.z+1);
		box.flipBoxWithCenter(direction, null);
		axisVec = box.getMinVec();
	}
	
	
	@Override
	public void onRotate(World world, EntityPlayer player, ItemStack stack, EnumFacing direction) 
	{
		LittleTileBox box = new LittleTileBox(axisVec.x, axisVec.y, axisVec.z, axisVec.x+1, axisVec.y+1, axisVec.z+1);
		box.rotateBox(direction);
		axisVec = box.getMinVec();
		//ForgeDirection axisDirection = axis.getDirection();
		this.axis = RotationUtils.rotateFacing(RotationUtils.getFacingFromAxis(axis), direction).getAxis();
		//this.axis = Axis.getAxis(direction.getRotation(axis.getDirection()));
		this.normalDirection = RotationUtils.rotateFacing(normalDirection, direction);
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
	
	
	public boolean tryToPlacePreviews(World world, EntityPlayer player, BlockPos pos, Rotation direction, ArrayList<PreviewTile> defaultpreviews, boolean inverse)
	{
		LittleTileVec axisPoint = getAxisVec();
		LittleTileVec internalOffset = new LittleTileVec(axisPoint.x-pos.getX()*LittleTile.gridSize, axisPoint.y-pos.getY()*LittleTile.gridSize, axisPoint.z-pos.getZ()*LittleTile.gridSize);
		
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
		//LittleTileVec blockOffset = new LittleTileVec(LittleTile.halfGridSize, LittleTile.halfGridSize, LittleTile.halfGridSize);
		//LittleTileVec blockInvOffset = blockOffset.copy();
		//blockInvOffset.invert();
		for (int i = 0; i < defaultpreviews.size(); i++) {
			PreviewTile box = defaultpreviews.get(i).copy();
			
			//box.box.addOffset(missingOffset);
			box.box.rotateBoxWithCenter(direction, new Vec3d(1/32D, 1/32D, 1/32D));
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
		
		EnumFacing rotationAxis = RotationUtils.getFacingFromAxis(this.axis);
		if(inverse)
			rotationAxis = rotationAxis.getOpposite();
		structure.normalDirection = RotationUtils.rotateFacing(normalDirection, rotationAxis);
		
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
		
		if(ItemBlockTiles.placeTiles(world, player, previews, structure, pos, null, null, false))
		{
			/*ArrayList<LittleTile> tiles = getTiles();
			for (int i = 0; i < tiles.size(); i++) {
				tiles.get(i).te.updateBlock();
			}
			tiles.clear();
			tiles = structure.getTiles();
			for (int i = 0; i < tiles.size(); i++) {
				tiles.get(i).te.combineTiles();
			}*/
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onBlockActivated(World world, LittleTile tile, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if(axis != null)
		{
			if(!hasLoaded())
			{
				player.addChatComponentMessage(new TextComponentTranslation("Cannot interact with door! Not all tiles are loaded!"));
				return true;
			}
			ArrayList<PreviewTile> previews = new ArrayList<>();
			
			LittleTileVec axisPoint = getAxisVec();
			
			int mainX = axisPoint.x/LittleTile.gridSize;
			int mainY = axisPoint.y/LittleTile.gridSize;
			int mainZ = axisPoint.z/LittleTile.gridSize;
			
			
			//LittleTileVec internalOffset = new LittleTileVec(axis.x-mainX*LittleTile.gridSize, axis.y-mainY*LittleTile.gridSize, axis.z-mainZ*LittleTile.gridSize);
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
					box.addOffset(new LittleTileVec(tileOfList.te.getPos()));
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
			double playerRotation = MathHelper.wrapDegrees(player.rotationYaw);
			boolean rotX = playerRotation <= -90 || playerRotation >= 90;
			boolean rotY = player.rotationPitch > 0;
			boolean rotZ = playerRotation > 0 && playerRotation <= 180;
			boolean inverse = false;
			
			switch(axis)
			{
			case X:
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
			case Y:
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
			case Z:
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
				tiles.get(i).te.removeTile(tiles.get(i));
			}
			
			if(tryToPlacePreviews(world, player, new BlockPos(mainX, mainY, mainZ), rotation, previews, !inverse))
				return true;
			else if(tryToPlacePreviews(world, player, new BlockPos(mainX, mainY, mainZ), rotation.getOpposite(), previews, inverse))
				return true;
			
			for (int i = 0; i < tiles.size(); i++) {
				tiles.get(i).te.addTile(tiles.get(i));
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
		case X:
			normalDirection = RotationUtils.getFacingFromAxis(Axis.Z);
			break;
		case Y:
			normalDirection = RotationUtils.getFacingFromAxis(Axis.X);
			break;
		case Z:
			normalDirection = RotationUtils.getFacingFromAxis(Axis.Y);
			break;
		default:
			break;
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public LittleStructure parseStructure(SubGui gui) {
		LittleDoor door = new LittleDoor();
		GuiTileViewer viewer = (GuiTileViewer) gui.get("tileviewer");
		door.axisVec = new LittleTileVec(viewer.axisX, viewer.axisY, viewer.axisZ);
		door.axis = viewer.axisDirection;
		door.normalDirection = RotationUtils.getFacingFromAxis(viewer.normalAxis);
		//door.updateNormalDirection();
		return door; 
	}

}
