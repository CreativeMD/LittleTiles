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
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.creativemd.littletiles.common.utils.rotation.OrdinaryDoorTransformation;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileCoord;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;
import com.creativemd.littletiles.utils.PlacePreviewTile;
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

public class LittleDoor extends LittleStructure{

	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		if(nbt.hasKey("ax"))
		{
			axisVec = new LittleTileVec("a", nbt);
			if(getMainTile() != null)
				axisVec.subVec(getMainTile().cornerVec);
		}else{
			axisVec = new LittleTileVec("av", nbt);
		}
		axis = RotationUtils.getAxisFromIndex(nbt.getInteger("axis"));
		normalDirection = EnumFacing.getFront(nbt.getInteger("ndirection"));
		if(nbt.hasKey("duration"))
			duration = nbt.getInteger("duration");
		else
			duration = 50;
	}

	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		//axisPoint.writeToNBT("a", nbt);
		axisVec.writeToNBT("av", nbt);
		nbt.setInteger("axis", RotationUtils.getAxisIndex(axis));
		nbt.setInteger("ndirection", normalDirection.getIndex());
		nbt.setInteger("duration", duration);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void createControls(SubGui gui, LittleStructure structure) {
		LittleDoor door = null;
		if(structure instanceof LittleDoor)
			door = (LittleDoor) structure;
		GuiTileViewer tile = new GuiTileViewer("tileviewer", 0, 30, 100, 100, ((SubGuiStructure) gui).stack);
		int duration = 50;
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
			duration = door.duration;
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
		
		gui.controls.add(new GuiLabel("Duration:", 0, 141));
		gui.controls.add(new GuiSteppedSlider("duration_s", 50, 140, 50, 12, duration, 1, 500));
		//gui.controls.add(new GuiButton("->", 190, 90, 20));
		//gui.controls.add(new GuiStateButton("direction", 3, 130, 50, 50, 20, "NORTH", "SOUTH", "WEST", "EAST"));
	}
	
	public EnumFacing normalDirection;
	public EnumFacing.Axis axis;
	public LittleTileVec axisVec;
	public LittleTileVec lastMainTileVec = null;
	public int duration = 50;
	
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
		/*if(event.source.is("<--"))
		{
			viewer.viewDirection = viewer.viewDirection.getOpposite();
			//viewer.viewDirection = ForgeDirection.getOrientation(((GuiStateButton) event.source).getState()+2);
		}*/
		
		
		
	}
	
	@Override
	public ArrayList<PlacePreviewTile> getSpecialTiles()
	{
		ArrayList<PlacePreviewTile> boxes = new ArrayList<>();
		LittleTileBox box = new LittleTileBox(axisVec);
		
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
		this.axis = RotationUtils.rotateFacing(RotationUtils.getFacingFromAxis(axis), direction).getAxis();
		this.normalDirection = RotationUtils.rotateFacing(normalDirection, direction);
	}
	
	
	public boolean tryToPlacePreviews(World world, EntityPlayer player, BlockPos pos, Rotation direction, boolean inverse, UUID uuid)
	{
		ArrayList<LittleTile> tiles = getTiles();
		ArrayList<PlacePreviewTile> defaultpreviews = new ArrayList<>();
		LittleTileVec axisPoint = getAxisVec();
		
		LittleTileVec invaxis = axisPoint.copy();
		invaxis.invert();
		
		for (int i = 0; i < tiles.size(); i++) {
			LittleTile tileOfList = tiles.get(i);
			NBTTagCompound nbt = new NBTTagCompound();
			
			LittleTilePreview preview = tileOfList.getPreviewTile();
			preview.box.addOffset(new LittleTileVec(tileOfList.te.getPos()));
			preview.box.addOffset(invaxis);
			
			preview.rotatePreview(direction);
			
			defaultpreviews.add(preview.getPlaceableTile(preview.box, false, new LittleTileVec(0, 0, 0)));
		}
		
		defaultpreviews.add(new PreviewTileAxis(new LittleTileBox(0, 0, 0, 1, 1, 1), null, axis));
		
		LittleTileVec internalOffset = new LittleTileVec(axisPoint.x-pos.getX()*LittleTile.gridSize, axisPoint.y-pos.getY()*LittleTile.gridSize, axisPoint.z-pos.getZ()*LittleTile.gridSize);
		ArrayList<PlacePreviewTile> previews = new ArrayList<>();
		for (int i = 0; i < defaultpreviews.size(); i++) {
			PlacePreviewTile box = defaultpreviews.get(i); //.copy();
			//box.box.rotateBoxWithCenter(direction, new Vec3d(1/32D, 1/32D, 1/32D));
			box.box.addOffset(internalOffset);
			previews.add(box);
		}
		
		LittleDoor structure = new LittleDoor();
		structure.axisVec = new LittleTileVec(0, 0, 0);
		structure.setTiles(new ArrayList<LittleTile>());
		structure.axis = this.axis;
		
		EnumFacing rotationAxis = RotationUtils.getFacingFromAxis(this.axis);
		if(inverse)
			rotationAxis = rotationAxis.getOpposite();
		structure.normalDirection = RotationUtils.rotateFacing(normalDirection, rotationAxis);
		structure.duration = this.duration;
		
		HashMapList<BlockPos, PlacePreviewTile> splitted = ItemBlockTiles.getSplittedTiles(previews, pos);
		if(ItemBlockTiles.canPlaceTiles(world, splitted, new ArrayList<>(splitted.getKeys()), false))
		{
			ArrayList<TileEntityLittleTiles> blocks = new ArrayList<>();
			World fakeWorld = new WorldFake(world);
			ItemBlockTiles.placeTiles(fakeWorld, player, previews, structure, pos, null, null, false, EnumFacing.EAST);
			for (Iterator iterator = fakeWorld.loadedTileEntityList.iterator(); iterator.hasNext();) {
				TileEntity te = (TileEntity) iterator.next();
				if(te instanceof TileEntityLittleTiles)
					blocks.add((TileEntityLittleTiles) te);
			}
			
			EntityAnimation animation = new EntityAnimation(world, this, blocks, previews, structure.getAxisVec(), new OrdinaryDoorTransformation(direction), uuid);
			animation.setPosition(pos.getX(), pos.getY(), pos.getZ());
			world.spawnEntityInWorld(animation);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onBlockActivated(World world, LittleTile tile, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if(world.isRemote && axis != null)
		{
			if(!hasLoaded())
			{
				player.addChatComponentMessage(new TextComponentTranslation("Cannot interact with door! Not all tiles are loaded!"));
				return true;
			}
						
			//Calculate rotation
			Rotation rotation = Rotation.EAST;
			double playerRotation = MathHelper.wrapDegrees(player.rotationYaw);
			boolean rotX = playerRotation <= -90 || playerRotation >= 90;
			boolean rotY = player.rotationPitch > 0;
			boolean rotZ = playerRotation > 0 && playerRotation <= 180;
			boolean inverse = false;
			
			switch(axis)
			{
			case X:
				//System.out.println(normalDirection);
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
			
			//System.out.println("Sending packet");
			UUID uuid = UUID.randomUUID();
			PacketHandler.sendPacketToServer(new LittleDoorInteractPacket(pos, player, rotation, inverse, uuid));
			interactWithDoor(world, player, rotation, inverse, uuid);
		}
		return true;
	}
	
	public boolean interactWithDoor(World world, EntityPlayer player, Rotation rotation, boolean inverse, UUID uuid)
	{
		ArrayList<LittleTile> tiles = getTiles();
		
		LittleTileVec axisPoint = getAxisVec();
		int mainX = axisPoint.x/LittleTile.gridSize;
		int mainY = axisPoint.y/LittleTile.gridSize;
		int mainZ = axisPoint.z/LittleTile.gridSize;
		
		for (int i = 0; i < tiles.size(); i++) {
			tiles.get(i).te.removeTile(tiles.get(i));
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
		
		for (int i = 0; i < tiles.size(); i++) {
			tiles.get(i).te.addTile(tiles.get(i));
		}
		
		return false;
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
	public void writeToNBTPreview(NBTTagCompound nbt, BlockPos newCenter)
	{
		LittleTileVec axisPointBackup = axisVec.copy();
		axisVec.subVec(new LittleTileVec(newCenter));
		axisVec.addVec(new LittleTileVec(getMainTile().te.getPos()));
		axisVec.addVec(getMainTile().cornerVec);
		super.writeToNBTPreview(nbt, newCenter);
		axisVec = axisPointBackup;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public LittleStructure parseStructure(SubGui gui) {
		LittleDoor door = new LittleDoor();
		GuiTileViewer viewer = (GuiTileViewer) gui.get("tileviewer");
		door.axisVec = new LittleTileVec(viewer.axisX, viewer.axisY, viewer.axisZ);
		door.axis = viewer.axisDirection;
		door.normalDirection = RotationUtils.getFacingFromAxis(viewer.normalAxis);
		GuiSteppedSlider slider = (GuiSteppedSlider) gui.get("duration_s");
		door.duration = (int) slider.value;
		
		//door.updateNormalDirection();
		return door; 
	}

}
