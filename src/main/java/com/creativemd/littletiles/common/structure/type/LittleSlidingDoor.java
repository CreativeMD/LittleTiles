package com.creativemd.littletiles.common.structure.type;

import java.util.UUID;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiIconButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiStateButton;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlClickEvent;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.littletiles.common.entity.DoorController;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.gui.controls.GuiDirectionIndicator;
import com.creativemd.littletiles.common.gui.controls.GuiLTDistance;
import com.creativemd.littletiles.common.gui.controls.GuiTileViewer;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.relative.StructureAbsolute;
import com.creativemd.littletiles.common.tiles.preview.LittleAbsolutePreviewsStructure;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVecContext;
import com.creativemd.littletiles.common.utils.animation.AnimationGuiHandler;
import com.creativemd.littletiles.common.utils.animation.AnimationKey;
import com.creativemd.littletiles.common.utils.animation.AnimationState;
import com.creativemd.littletiles.common.utils.animation.AnimationTimeline;
import com.creativemd.littletiles.common.utils.animation.ValueTimeline.LinearTimeline;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleSlidingDoor extends LittleDoorBase {
	
	public LittleSlidingDoor(LittleStructureType type) {
		super(type);
	}
	
	public EnumFacing moveDirection;
	public int moveDistance;
	public LittleGridContext moveContext;
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		super.loadFromNBTExtra(nbt);
		moveDistance = nbt.getInteger("distance");
		moveDirection = EnumFacing.getFront(nbt.getInteger("direction"));
		moveContext = LittleGridContext.get(nbt);
	}
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		super.writeToNBTExtra(nbt);
		nbt.setInteger("distance", moveDistance);
		nbt.setInteger("direction", moveDirection.getIndex());
		moveContext.set(nbt);
	}
	
	@Override
	public boolean tryToPlacePreviews(World world, EntityPlayer player, UUID uuid, StructureAbsolute absolute) {
		LittleTileVec offsetVec = new LittleTileVec(moveDirection);
		offsetVec.scale(moveDistance);
		LittleTileVecContext offset = new LittleTileVecContext(moveContext, offsetVec);
		
		LittleAbsolutePreviewsStructure previews = getAbsolutePreviews(stayAnimated ? getMainTile().te.getPos() : getMainTile().te.getPos().add(offset.vec.getBlockPos(moveContext)));
		LittleSlidingDoor structure = (LittleSlidingDoor) previews.getStructure();
		structure.duration = duration;
		structure.moveDirection = moveDirection.getOpposite();
		structure.moveDistance = moveDistance;
		structure.moveContext = moveContext;
		structure.setTiles(new HashMapList<>());
		
		if (offset.context.size > previews.context.size)
			previews.convertTo(offset.context);
		else if (offset.context.size < previews.context.size)
			offset.convertTo(previews.context);
		
		if (!stayAnimated)
			previews.movePreviews(world, player, null, previews.context, offset.vec);
		
		DoorController controller;
		if (stayAnimated)
			controller = new DoorController(new AnimationState(), new AnimationState().set(AnimationKey.getOffset(moveDirection.getAxis()), moveDirection.getAxisDirection().getOffset() * moveContext.toVanillaGrid(moveDistance)), null, duration);
		else
			controller = new DoorController(new AnimationState().set(AnimationKey.getOffset(moveDirection.getAxis()), -moveDirection.getAxisDirection().getOffset() * moveContext.toVanillaGrid(moveDistance)), new AnimationState(), true, duration);
		
		return place(world, player, previews, controller, uuid, absolute);
	}
	
	@Override
	public void onFlip(World world, EntityPlayer player, ItemStack stack, LittleGridContext context, Axis axis, LittleTileVec doubledCenter) {
		if (axis == this.moveDirection.getAxis())
			this.moveDirection = this.moveDirection.getOpposite();
	}
	
	@Override
	public void onRotate(World world, EntityPlayer player, ItemStack stack, LittleGridContext context, Rotation rotation, LittleTileVec doubledCenter) {
		moveDirection = RotationUtils.rotate(moveDirection, rotation);
	}
	
	@Override
	public StructureAbsolute getAbsoluteAxis() {
		return new StructureAbsolute(getMainTile().te.getPos(), getMainTile().box, getMainTile().getContext());
	}
	
	@Override
	public LittleGridContext getMinContext() {
		return LittleGridContext.max(super.getMinContext(), moveContext);
	}
	
	public static class LittleSlidingDoorParser extends LittleDoorBaseParser {
		
		public LittleSlidingDoorParser(GuiParent parent, AnimationGuiHandler handler) {
			super(parent, handler);
		}
		
		@SideOnly(Side.CLIENT)
		@CustomEventSubscribe
		public void buttonClicked(GuiControlClickEvent event) {
			if (event.source.is("direction")) {
				GuiTileViewer viewer = (GuiTileViewer) parent.get("tileviewer");
				EnumFacing direction = EnumFacing.getFront(((GuiStateButton) event.source).getState());
				GuiDirectionIndicator relativeDirection = (GuiDirectionIndicator) parent.get("relativeDirection");
				updateButtonDirection(viewer, direction, relativeDirection);
			}
		}
		
		@CustomEventSubscribe
		@SideOnly(Side.CLIENT)
		public void onChanged(GuiControlChangedEvent event) {
			if (event.source.is("distance"))
				updateTimeline();
		}
		
		@SideOnly(Side.CLIENT)
		public static void updateDirection(GuiTileViewer viewer, EnumFacing direction, GuiDirectionIndicator relativeDirection) {
			EnumFacing newDirection = EnumFacing.EAST;
			
			if (viewer.getXFacing().getAxis() == direction.getAxis())
				if (viewer.getXFacing().getAxisDirection() == direction.getAxisDirection())
					newDirection = EnumFacing.EAST;
				else
					newDirection = EnumFacing.WEST;
			else if (viewer.getYFacing().getAxis() == direction.getAxis())
				if (viewer.getYFacing().getAxisDirection() == direction.getAxisDirection())
					newDirection = EnumFacing.DOWN;
				else
					newDirection = EnumFacing.UP;
			else if (viewer.getZFacing().getAxis() == direction.getAxis())
				if (viewer.getZFacing().getAxisDirection() == direction.getAxisDirection())
					newDirection = EnumFacing.SOUTH;
				else
					newDirection = EnumFacing.NORTH;
			relativeDirection.setDirection(newDirection);
		}
		
		@SideOnly(Side.CLIENT)
		public void updateButtonDirection(GuiTileViewer viewer, EnumFacing direction, GuiDirectionIndicator relativeDirection) {
			updateDirection(viewer, direction, relativeDirection);
			updateTimeline();
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public void createControls(ItemStack stack, @Nullable LittleStructure structure) {
			LittleSlidingDoor door = null;
			if (structure instanceof LittleSlidingDoor)
				door = (LittleSlidingDoor) structure;
			
			LittleTileSize size = LittleTilePreview.getSize(stack);
			
			int index = EnumFacing.UP.ordinal();
			if (door != null)
				index = door.moveDirection.ordinal();
			EnumFacing direction = EnumFacing.getFront(index);
			
			LittleGridContext context = LittleGridContext.get(stack.getTagCompound());
			
			GuiTileViewer viewer = new GuiTileViewer("tileviewer", 0, 0, 100, 100, context);
			viewer.visibleAxis = false;
			parent.addControl(viewer);
			
			parent.addControl(new GuiStateButton("direction", index, 110, 0, 37, 12, RotationUtils.getFacingNames()));
			
			GuiDirectionIndicator relativeDirection = new GuiDirectionIndicator("relativeDirection", 155, 0, EnumFacing.UP);
			parent.addControl(relativeDirection);
			
			int distance = size.getSizeOfAxis(direction.getAxis());
			if (door != null) {
				distance = door.moveDistance;
				context = door.moveContext;
			}
			parent.addControl(new GuiLTDistance("distance", 110, 21, context, distance));
			
			parent.addControl(new GuiIconButton("reset view", 20, 107, 8) {
				
				@Override
				public void onClicked(int x, int y, int button) {
					viewer.offsetX.set(0);
					viewer.offsetY.set(0);
					viewer.scale.set(40);
				}
			}.setCustomTooltip("reset view"));
			parent.addControl(new GuiIconButton("change view", 40, 107, 7) {
				
				@Override
				public void onClicked(int x, int y, int button) {
					switch (viewer.getAxis()) {
					case X:
						viewer.setViewAxis(EnumFacing.Axis.Y);
						break;
					case Y:
						viewer.setViewAxis(EnumFacing.Axis.Z);
						break;
					case Z:
						viewer.setViewAxis(EnumFacing.Axis.X);
						break;
					default:
						break;
					}
					
					updateButtonDirection(viewer, direction, relativeDirection);
				}
			}.setCustomTooltip("change view"));
			parent.addControl(new GuiIconButton("flip view", 60, 107, 4) {
				
				@Override
				public void onClicked(int x, int y, int button) {
					viewer.setViewDirection(viewer.getViewDirection().getOpposite());
					updateDirection(viewer, direction, relativeDirection);
				}
			}.setCustomTooltip("flip view"));
			
			super.createControls(stack, structure);
			
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public LittleSlidingDoor parseStructure(int duration, boolean stayAnimated) {
			EnumFacing direction = EnumFacing.getFront(((GuiStateButton) parent.get("direction")).getState());
			
			GuiLTDistance distance = (GuiLTDistance) parent.get("distance");
			
			LittleSlidingDoor door = createStructure(LittleSlidingDoor.class);
			door.duration = duration;
			door.moveDirection = direction;
			door.moveDistance = distance.getDistance();
			door.moveContext = distance.getDistanceContext();
			door.stayAnimated = stayAnimated;
			return door;
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public void onLoaded(EntityAnimation animation, LittleTileBox entireBox, LittleGridContext context, AxisAlignedBB box, LittlePreviews previews) {
			super.onLoaded(animation, entireBox, context, box, previews);
			
			GuiTileViewer viewer = (GuiTileViewer) parent.get("tileviewer");
			GuiDirectionIndicator relativeDirection = (GuiDirectionIndicator) parent.get("relativeDirection");
			
			EnumFacing direction = EnumFacing.getFront(((GuiStateButton) parent.get("direction")).getState());
			
			updateButtonDirection(viewer, direction, relativeDirection);
		}
		
		@Override
		public void populateTimeline(AnimationTimeline timeline) {
			EnumFacing direction = EnumFacing.getFront(((GuiStateButton) parent.get("direction")).getState());
			GuiLTDistance distance = (GuiLTDistance) parent.get("distance");
			
			timeline.values.add(AnimationKey.getOffset(direction.getAxis()), new LinearTimeline().addPoint(0, 0D).addPoint(timeline.duration, direction.getAxisDirection().getOffset() * distance.getDistanceContext().toVanillaGrid(distance.getDistance())));
		}
	}
	
}
