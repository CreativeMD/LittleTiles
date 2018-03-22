package com.creativemd.littletiles.common.structure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.creativecore.common.utils.Rotation;
import com.creativemd.creativecore.common.utils.RotationUtils;
import com.creativemd.creativecore.common.world.WorldFake;
import com.creativemd.creativecore.gui.GuiControl;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiIDButton;
import com.creativemd.creativecore.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.gui.controls.gui.GuiStateButton;
import com.creativemd.creativecore.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.gui.event.gui.GuiControlClickEvent;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.gui.SubGuiStructure;
import com.creativemd.littletiles.common.gui.controls.GuiDirectionIndicator;
import com.creativemd.littletiles.common.gui.controls.GuiTileViewer;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.items.ItemRecipe;
import com.creativemd.littletiles.common.packet.LittleDoorInteractPacket;
import com.creativemd.littletiles.common.packet.LittleSlidingDoorPacket;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVecContext;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.rotation.OrdinaryDoorTransformation;
import com.creativemd.littletiles.common.utils.rotation.SlidingDoorTransformation;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleSlidingDoor extends LittleDoorBase {
	
	public EnumFacing moveDirection;
	public int moveDistance;
	public LittleGridContext moveContext;
	
	public LittleTilePos placedAxis;
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		super.loadFromNBTExtra(nbt);
		moveDistance = nbt.getInteger("distance");
		moveDirection = EnumFacing.getFront(nbt.getInteger("direction"));
		moveContext = LittleGridContext.get(nbt);
		if(nbt.hasKey("placedAxis"))
			placedAxis = new LittleTilePos("placedAxis", nbt);
	}

	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		super.writeToNBTExtra(nbt);
		nbt.setInteger("distance", moveDistance);
		nbt.setInteger("direction", moveDirection.getIndex());
		moveContext.set(nbt);
		if(placedAxis != null)
			placedAxis.writeToNBT("placedAxis", nbt);
	}
	
	@Override
	public boolean activate(World world, EntityPlayer player, Rotation rotation, BlockPos pos) {
		if(!isWaitingForApprove)
		{
			if(!hasLoaded())
			{
				player.sendStatusMessage(new TextComponentTranslation("Cannot interact with door! Not all tiles are loaded!"), true);
				return true;
			}
			
			UUID uuid = UUID.randomUUID();
			if(world.isRemote)
				PacketHandler.sendPacketToServer(new LittleSlidingDoorPacket(pos, player, uuid));
			return interactWithDoor(world, pos, player, uuid);
		}
		return false;
	}
	
	@Override
	public boolean onBlockActivated(World world, LittleTile tile, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if(world.isRemote && !isWaitingForApprove)
		{
			if(!hasLoaded())
			{
				player.sendStatusMessage(new TextComponentTranslation("Cannot interact with door! Not all tiles are loaded!"), true);
				return true;
			}
			
			
			UUID uuid = UUID.randomUUID();
			PacketHandler.sendPacketToServer(new LittleSlidingDoorPacket(pos, player, uuid));
			interactWithDoor(world, pos, player, uuid);
			
		}
		return true;
	}
	
	public boolean tryToPlacePreviews(World world, EntityPlayer player, BlockPos pos, UUID uuid)
	{
		placedAxis = new LittleTilePos(pos, moveContext);
		
		LittleAbsolutePreviews previews = new LittleAbsolutePreviews(pos, moveContext);
		for (Iterator<LittleTile> iterator = getTiles(); iterator.hasNext();) {
			LittleTile tile = iterator.next();
			previews.addTile(tile);
		}
		
		PlacePreviews defaultpreviews = new PlacePreviews(previews.context);
		
		LittleTileVec offset = new LittleTileVec(moveDirection);
		offset.scale(moveDistance);
		if(moveContext != previews.context)
			offset.convertTo(moveContext, previews.context);
		
		for (LittleTilePreview preview : previews) {
			defaultpreviews.add(preview.getPlaceableTile(preview.box, false, offset));
		}
		
		LittleSlidingDoor structure = new LittleSlidingDoor();
		structure.placedAxis = new LittleTilePos(pos, new LittleTileVecContext(moveContext, LittleTileVec.ZERO));
		structure.duration = duration;
		structure.moveDirection = moveDirection.getOpposite();
		structure.moveDistance = moveDistance;
		structure.moveContext = moveContext;
		structure.setTiles(new HashMapList<>());
		
		
		return place(world, structure, player, defaultpreviews, pos, new SlidingDoorTransformation(moveDirection, moveContext, moveDistance), uuid, getAbsoluteAxisVec(), getAdditionalAxisVec());
	}
	
	public boolean interactWithDoor(World world, BlockPos pos, EntityPlayer player, UUID uuid)
	{
		HashMapList<TileEntityLittleTiles, LittleTile> tempTiles = new HashMapList<>(tiles);
		
		for (Entry<TileEntityLittleTiles, ArrayList<LittleTile>> entry : tempTiles.entrySet()) {
			entry.getKey().preventUpdate = true;
			entry.getKey().removeTiles(entry.getValue());
			entry.getKey().preventUpdate = false;
		}
		
		if(tryToPlacePreviews(world, player, pos, uuid))
		{
			for (Entry<TileEntityLittleTiles, ArrayList<LittleTile>> entry : tempTiles.entrySet()) {
				entry.getKey().updateTiles();
			}
			return true;
		}
		
		for (Entry<TileEntityLittleTiles, ArrayList<LittleTile>> entry : tempTiles.entrySet()) {
			entry.getKey().addTiles(entry.getValue());
		}
		
		return false;
	}
	
	
	@Override
	public void onFlip(World world, EntityPlayer player, ItemStack stack, Axis axis, LittleTileVec doubledCenter)
	{
		if(axis == this.moveDirection.getAxis())
			this.moveDirection = this.moveDirection.getOpposite();
	}
	
	
	@Override
	public void onRotate(World world, EntityPlayer player, ItemStack stack, Rotation rotation, LittleTileVec doubledCenter) 
	{
		moveDirection = RotationUtils.rotateFacing(moveDirection, rotation);
	}

	@Override
	public LittleTilePos getAbsoluteAxisVec() {
		return placedAxis;
	}
	
	@Override
	public LittleTileVec getAdditionalAxisVec() {
		return LittleTileVec.ZERO;
	}
	
	@Override
	public LittleGridContext getMinContext()
	{
		return moveContext;
	}
	
	@SideOnly(Side.CLIENT)
	@CustomEventSubscribe
	public void buttonClicked(GuiControlClickEvent event)
	{
		if(event.source.is("direction"))
		{
			SubGuiStructure gui = ((SubGuiStructure) event.source.parent);
			EnumFacing direction = EnumFacing.getFront(((GuiStateButton) event.source).getState());
			GuiSteppedSlider slider = (GuiSteppedSlider) gui.get("distance");
			
			LittleTileSize size = LittleTilePreview.getSize(gui.stack);
			
			slider.minValue = 1;
			slider.maxValue = size.getSizeOfAxis(direction.getAxis())+1;
			if(gui.structure instanceof LittleSlidingDoor && ((LittleSlidingDoor) gui.structure).moveDirection == direction)
				slider.value = ((LittleSlidingDoor) gui.structure).moveDistance;
			else
				slider.value = slider.maxValue-1;
		}
		
		GuiTileViewer viewer = (GuiTileViewer) event.source.parent.get("tileviewer");
		if(event.source.is("change view"))
		{
			switch (viewer.axisDirection) {
			case X:
				viewer.axisDirection = EnumFacing.Axis.Y;
				break;
			case Y:
				viewer.axisDirection = EnumFacing.Axis.Z;
				break;
			case Z:
				viewer.axisDirection = EnumFacing.Axis.X;
				break;
			default:
				break;
			}
			viewer.updateViewDirection();
			
			viewer.updateNormalAxis();
		}else if(event.source.is("reset view"))
		{
			viewer.offsetX = 0;
			viewer.offsetY = 0;
			viewer.scale = 5;
		}else if(event.source.is("flip view"))
		{
			viewer.viewDirection = viewer.viewDirection.getOpposite();
			viewer.baked = null;
		}
		
		if(event.source.parent instanceof SubGuiStructure)
		{
			GuiDirectionIndicator relativeDirection = (GuiDirectionIndicator) event.source.parent.get("relativeDirection");
			
			EnumFacing direction = EnumFacing.getFront(((GuiStateButton) event.source.parent.get("direction")).getState());
			
			updateDirection(viewer, direction, relativeDirection);
		}
	}
	
	public static void updateDirection(GuiTileViewer viewer, EnumFacing direction, GuiDirectionIndicator relativeDirection)
	{
		EnumFacing newDirection = EnumFacing.EAST;
		
		if(viewer.getXFacing().getAxis() == direction.getAxis())
			if(viewer.getXFacing().getAxisDirection() == direction.getAxisDirection())
				newDirection = EnumFacing.EAST;
			else
				newDirection = EnumFacing.WEST;
		else if(viewer.getYFacing().getAxis() == direction.getAxis())
			if(viewer.getYFacing().getAxisDirection() == direction.getAxisDirection())
				newDirection = EnumFacing.DOWN;
			else
				newDirection = EnumFacing.UP;
		else if(viewer.getZFacing().getAxis() == direction.getAxis())
			if(viewer.getZFacing().getAxisDirection() == direction.getAxisDirection())
				newDirection = EnumFacing.SOUTH;
			else
				newDirection = EnumFacing.NORTH;
		relativeDirection.setDirection(newDirection);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void createControls(SubGui gui, LittleStructure structure) {
		super.createControls(gui, structure);
		LittleSlidingDoor door = null;
		if(structure instanceof LittleSlidingDoor)
			door = (LittleSlidingDoor) structure;
		
		LittleTileSize size = LittleTilePreview.getSize(((SubGuiStructure) gui).stack);
		
		int index = EnumFacing.UP.ordinal();
		if(door != null)
			index = door.moveDirection.ordinal();
		gui.addControl(new GuiStateButton("direction", index, 110, 30, 37, RotationUtils.getFacingNames()));
		
		GuiDirectionIndicator relativeDirection = new GuiDirectionIndicator("relativeDirection", 155, 30, EnumFacing.UP);
		gui.addControl(relativeDirection);
		int distance = size.getSizeOfAxis(EnumFacing.getFront(index).getAxis());
		if(door != null)
			distance = door.moveDistance;
		gui.addControl(new GuiSteppedSlider("distance", 110, 51, 60, 14, distance, 1, size.getSizeOfAxis(EnumFacing.getFront(index).getAxis())+1));
		
		gui.addControl(new GuiIDButton("reset view", 110, 75, 0));
		gui.addControl(new GuiIDButton("change view", 110, 95, 1));
		gui.addControl(new GuiIDButton("flip view", 110, 115, 1));
		
		GuiTileViewer tile = new GuiTileViewer("tileviewer", 0, 30, 100, 100, ((SubGuiStructure) gui).stack);
		tile.visibleAxis = false;
		tile.updateViewDirection();
		gui.addControl(tile);
		
		updateDirection(tile, EnumFacing.getFront(index), relativeDirection);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public LittleDoorBase parseStructure(SubGui gui, int duration) {
		EnumFacing direction = EnumFacing.getFront(((GuiStateButton) gui.get("direction")).getState());
		
		GuiSteppedSlider slider = (GuiSteppedSlider) gui.get("distance");
		
		LittleSlidingDoor door = new LittleSlidingDoor();
		door.duration = duration;
		door.moveDirection = direction;
		door.moveDistance = (int) slider.value;
		door.moveContext = ((GuiTileViewer) gui.get("tileviewer")).context;
		return door;
	}

	@Override
	public LittleDoorBase copyToPlaceDoor() {
		LittleSlidingDoor structure = new LittleSlidingDoor();
		structure.setTiles(new HashMapList<>());
		structure.moveDirection = moveDirection;
		structure.moveDistance = moveDistance;
		structure.moveContext = moveContext;
		structure.duration = this.duration;
		return structure;
	}

}
