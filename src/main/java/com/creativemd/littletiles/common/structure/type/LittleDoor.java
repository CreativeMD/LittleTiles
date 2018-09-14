package com.creativemd.littletiles.common.structure.type;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.creativecore.gui.container.GuiParent;
import com.creativemd.creativecore.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.gui.controls.gui.GuiIDButton;
import com.creativemd.creativecore.gui.event.gui.GuiControlClickEvent;
import com.creativemd.littletiles.client.tiles.LittleRenderingCube;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.gui.controls.GuiTileViewer;
import com.creativemd.littletiles.common.packet.LittleDoorInteractPacket;
import com.creativemd.littletiles.common.structure.LittleStructure;
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
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;
import com.creativemd.littletiles.common.utils.transformation.OrdinaryDoorTransformation;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleDoor extends LittleDoorBase {
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		super.loadFromNBTExtra(nbt);
		if (nbt.hasKey("ax")) {
			LittleTileVecContext vec = new LittleTileVecContext("a", nbt);
			if (getMainTile() != null)
				vec.sub(new LittleTileVecContext(getMainTile().getContext(), getMainTile().getMinVec()));
			doubledRelativeAxis = new LittleRelativeDoubledAxis(vec.context, vec.vec, new LittleTileVec(1, 1, 1));
			
		} else if (nbt.hasKey("av")) {
			LittleTileVecContext vec = new LittleTileVecContext("av", nbt);
			doubledRelativeAxis = new LittleRelativeDoubledAxis(vec.context, vec.vec, new LittleTileVec(1, 1, 1));
		} else {
			doubledRelativeAxis = new LittleRelativeDoubledAxis("avec", nbt);
		}
		axis = EnumFacing.Axis.values()[nbt.getInteger("axis")];
		normalDirection = EnumFacing.getFront(nbt.getInteger("ndirection"));
	}
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		super.writeToNBTExtra(nbt);
		doubledRelativeAxis.writeToNBT("avec", nbt);
		nbt.setInteger("axis", axis.ordinal());
		nbt.setInteger("ndirection", normalDirection.getIndex());
	}
	
	public EnumFacing normalDirection;
	public EnumFacing.Axis axis;
	public LittleRelativeDoubledAxis doubledRelativeAxis;
	public LittleTilePos lastMainTileVec = null;
	
	@Override
	public LittleTilePos getAbsoluteAxisVec() {
		LittleTilePos pos = getMainTile().getAbsolutePos();
		pos.add(doubledRelativeAxis.getNonDoubledVec());
		return pos;
	}
	
	@Override
	public LittleTileVec getAdditionalAxisVec() {
		return doubledRelativeAxis.additional.copy();
	}
	
	@Override
	public void moveStructure(EnumFacing facing) {
		doubledRelativeAxis.vec.add(facing);
	}
	
	@Override
	public void setMainTile(LittleTile tile) {
		LittleTilePos absolute = tile.getAbsolutePos();
		if (getMainTile() != null && (lastMainTileVec == null || !lastMainTileVec.equals(absolute)))
			doubledRelativeAxis.add(lastMainTileVec.getRelative(absolute));
		lastMainTileVec = absolute;
		super.setMainTile(tile);
	}
	
	@Override
	public LittleGridContext getMinContext() {
		doubledRelativeAxis.convertToSmallest();
		return doubledRelativeAxis.context;
	}
	
	@Override
	public ArrayList<PlacePreviewTile> getSpecialTiles(LittleGridContext context) {
		if (context.size < doubledRelativeAxis.context.size)
			throw new RuntimeException("Invalid context as it is smaller than the context of the axis");
		
		ArrayList<PlacePreviewTile> boxes = new ArrayList<>();
		doubledRelativeAxis.convertTo(context);
		boxes.add(new PlacePreviewTileAxis(new LittleTileBox(doubledRelativeAxis.vec.x, doubledRelativeAxis.vec.y, doubledRelativeAxis.vec.z, doubledRelativeAxis.vec.x + 1, doubledRelativeAxis.vec.y + 1, doubledRelativeAxis.vec.z + 1), null, axis, getAdditionalAxisVec()));
		doubledRelativeAxis.convertToSmallest();
		return boxes;
	}
	
	@Override
	public void onFlip(World world, EntityPlayer player, ItemStack stack, LittleGridContext context, Axis axis, LittleTileVec doubledCenter) {
		doubledCenter = doubledCenter.copy();
		if (context.size > doubledRelativeAxis.context.size)
			doubledRelativeAxis.convertTo(context);
		else {
			doubledCenter.convertTo(context, doubledRelativeAxis.context);
			context = doubledRelativeAxis.context;
		}
		LittleTileVec doubleddoubled = doubledRelativeAxis.vec.copy();
		doubleddoubled.scale(2);
		doubleddoubled.add(doubledRelativeAxis.additional);
		doubleddoubled.sub(doubledCenter);
		doubleddoubled.flip(axis);
		doubleddoubled.add(doubledCenter);
		doubledRelativeAxis = new LittleRelativeDoubledAxis(doubledRelativeAxis.context, new LittleTileVec(doubleddoubled.x / 2, doubleddoubled.y / 2, doubleddoubled.z / 2), new LittleTileVec(doubleddoubled.x % 2, doubleddoubled.y % 2, doubleddoubled.z % 2));
	}
	
	@Override
	public void onRotate(World world, EntityPlayer player, ItemStack stack, LittleGridContext context, Rotation rotation, LittleTileVec doubledCenter) {
		doubledCenter = doubledCenter.copy();
		if (context.size > doubledRelativeAxis.context.size)
			doubledRelativeAxis.convertTo(context);
		else {
			doubledCenter.convertTo(context, doubledRelativeAxis.context);
			context = doubledRelativeAxis.context;
		}
		LittleTileVec doubleddoubled = doubledRelativeAxis.vec.copy();
		doubleddoubled.scale(2);
		doubleddoubled.add(doubledRelativeAxis.additional);
		doubleddoubled.sub(doubledCenter);
		doubleddoubled.rotateVec(rotation);
		doubleddoubled.add(doubledCenter);
		doubledRelativeAxis = new LittleRelativeDoubledAxis(doubledRelativeAxis.context, new LittleTileVec(doubleddoubled.x / 2, doubleddoubled.y / 2, doubleddoubled.z / 2), new LittleTileVec(doubleddoubled.x % 2, doubleddoubled.y % 2, doubleddoubled.z % 2));
		
		this.axis = RotationUtils.rotateFacing(RotationUtils.getFacing(axis), rotation).getAxis();
		this.normalDirection = RotationUtils.rotateFacing(normalDirection, rotation);
	}
	
	public boolean tryToPlacePreviews(World world, EntityPlayer player, BlockPos pos, Rotation direction, boolean inverse, UUID uuid, LittleAbsolutePreviews originalPreviews, LittleTilePos absoluteAxis, LittleTileVec additional) {
		LittleAbsolutePreviews previews = new LittleAbsolutePreviews(originalPreviews.pos, originalPreviews.context);
		for (LittleTilePreview preview : originalPreviews) {
			previews.addWithoutCheckingPreview(preview.copy());
		}
		
		PlacePreviews defaultpreviews = new PlacePreviews(previews.context);
		absoluteAxis.convertTo(previews.context);
		
		for (LittleTilePreview preview : previews) {
			preview.box.subOffset(absoluteAxis.contextVec.vec);
			preview.rotatePreview(direction, additional);
			defaultpreviews.add(preview.getPlaceableTile(preview.box, false, absoluteAxis.contextVec.vec));
		}
		
		for (PlacePreviewTile placePreview : getAdditionalPreviews(defaultpreviews)) {
			placePreview.box.addOffset(absoluteAxis.contextVec.vec);
			defaultpreviews.add(placePreview);
		}
		
		LittleDoor structure = new LittleDoor();
		structure.doubledRelativeAxis = new LittleRelativeDoubledAxis(LittleGridContext.getMin(), LittleTileVec.ZERO, LittleTileVec.ZERO);
		structure.setTiles(new HashMapList<>());
		structure.axis = this.axis;
		structure.normalDirection = RotationUtils.rotateFacing(normalDirection, direction);
		structure.duration = this.duration;
		
		return place(world, structure, player, defaultpreviews, absoluteAxis.pos, new OrdinaryDoorTransformation(direction), uuid, absoluteAxis, additional);
	}
	
	@Override
	public Rotation getDefaultRotation() {
		return Rotation.getRotation(axis, true);
	}
	
	@Override
	public boolean activate(World world, @Nullable EntityPlayer player, @Nullable Rotation rotation, BlockPos pos) {
		if (axis != null && !isWaitingForApprove) {
			if (!hasLoaded()) {
				player.sendStatusMessage(new TextComponentTranslation("Cannot interact with door! Not all tiles are loaded!"), true);
				return false;
			}
			
			//Calculate rotation
			if (rotation == null) {
				double playerRotation = MathHelper.wrapDegrees(player.rotationYaw);
				boolean rotX = playerRotation <= -90 || playerRotation >= 90;
				boolean rotY = player.rotationPitch > 0;
				boolean rotZ = playerRotation > 0 && playerRotation <= 180;
				
				switch (axis) {
				case X:
					//System.out.println(normalDirection);
					rotation = Rotation.X_CLOCKWISE;
					switch (normalDirection) {
					case UP:
						if (!rotY)
							rotation = Rotation.X_COUNTER_CLOCKWISE;
						break;
					case DOWN:
						if (rotY)
							rotation = Rotation.X_COUNTER_CLOCKWISE;
						break;
					case SOUTH:
						if (!rotX)
							rotation = Rotation.X_COUNTER_CLOCKWISE;
						break;
					case NORTH:
						if (rotX)
							rotation = Rotation.X_COUNTER_CLOCKWISE;
						break;
					default:
						break;
					}
					break;
				case Y:
					rotation = Rotation.Y_CLOCKWISE;
					switch (normalDirection) {
					case EAST:
						if (rotX)
							rotation = Rotation.Y_COUNTER_CLOCKWISE;
						break;
					case WEST:
						if (!rotX)
							rotation = Rotation.Y_COUNTER_CLOCKWISE;
						break;
					case SOUTH:
						if (!rotZ)
							rotation = Rotation.Y_COUNTER_CLOCKWISE;
						break;
					case NORTH:
						if (rotZ)
							rotation = Rotation.Y_COUNTER_CLOCKWISE;
						break;
					default:
						break;
					}
					break;
				case Z:
					rotation = Rotation.Z_CLOCKWISE;
					switch (normalDirection) {
					case EAST:
						if (rotZ)
							rotation = Rotation.Z_COUNTER_CLOCKWISE;
						break;
					case WEST:
						if (!rotZ)
							rotation = Rotation.Z_COUNTER_CLOCKWISE;
						break;
					case UP:
						if (!rotY)
							rotation = Rotation.Z_COUNTER_CLOCKWISE;
						break;
					case DOWN:
						if (rotY)
							rotation = Rotation.Z_COUNTER_CLOCKWISE;
						break;
					default:
						break;
					}
					break;
				default:
					break;
				}
				
			}
			
			boolean inverse = false;
			switch (axis) {
			case X:
				inverse = rotation == Rotation.X_CLOCKWISE;
				break;
			case Y:
				inverse = rotation == Rotation.Y_CLOCKWISE;
				break;
			case Z:
				inverse = rotation == Rotation.Z_CLOCKWISE;
				break;
			default:
				break;
			}
			
			UUID uuid = UUID.randomUUID();
			if (world.isRemote)
				PacketHandler.sendPacketToServer(new LittleDoorInteractPacket(pos, player, rotation, inverse, uuid));
			interactWithDoor(world, player, rotation, inverse, uuid);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onBlockActivated(World world, LittleTile tile, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) {
		if (world.isRemote) {
			activate(world, player, null, pos);
			action.preventPacket = true;
		}
		return true;
	}
	
	public boolean interactWithDoor(World world, EntityPlayer player, Rotation rotation, boolean inverse, UUID uuid) {
		LoadList();
		
		LittleTilePos axisPoint = getAbsoluteAxisVec();
		LittleTileVec additional = getAdditionalAxisVec();
		
		axisPoint.removeInternalBlockOffset();
		
		BlockPos main = axisPoint.pos;
		
		HashMapList<TileEntityLittleTiles, LittleTile> tempTiles = new HashMapList<>(tiles);
		HashMap<TileEntityLittleTiles, LittleGridContext> tempContext = new HashMap<>();
		
		for (TileEntityLittleTiles te : tempTiles.keySet()) {
			tempContext.put(te, te.getContext());
		}
		
		for (Entry<TileEntityLittleTiles, ArrayList<LittleTile>> entry : tempTiles.entrySet()) {
			entry.getKey().preventUpdate = true;
			entry.getKey().removeTiles(entry.getValue());
			entry.getKey().preventUpdate = false;
		}
		
		LittleAbsolutePreviews previews = new LittleAbsolutePreviews(axisPoint.pos, getMinContext());
		for (Iterator<LittleTile> iterator = getTiles(); iterator.hasNext();) {
			LittleTile tile = iterator.next();
			previews.addTile(tile);
		}
		
		if (tryToPlacePreviews(world, player, main, rotation, !inverse, uuid, previews, axisPoint, additional) || tryToPlacePreviews(world, player, main, rotation.getOpposite(), inverse, uuid, previews, axisPoint, additional)) {
			for (Entry<TileEntityLittleTiles, ArrayList<LittleTile>> entry : tempTiles.entrySet()) {
				entry.getKey().updateTiles();
			}
			return true;
		}
		
		for (Entry<TileEntityLittleTiles, ArrayList<LittleTile>> entry : tempTiles.entrySet()) {
			entry.getKey().convertTo(tempContext.get(entry.getKey()));
			entry.getKey().addTiles(entry.getValue());
		}
		
		return false;
	}
	
	public void updateNormalDirection() {
		switch (axis) {
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
	public void writeToNBTPreview(NBTTagCompound nbt, BlockPos newCenter) {
		LittleRelativeDoubledAxis axisPointBackup = doubledRelativeAxis.copy();
		LittleTileVecContext vec = new LittleTilePos(getMainTile().te.getPos(), axisPointBackup.context).getRelative(new LittleTilePos(newCenter, axisPointBackup.context));
		doubledRelativeAxis.add(vec);
		vec = new LittleTileVecContext(getMainTile().getContext(), getMainTile().getMinVec());
		doubledRelativeAxis.add(vec);
		super.writeToNBTPreview(nbt, newCenter);
		doubledRelativeAxis = axisPointBackup;
	}
	
	@Override
	public LittleDoorBase copyToPlaceDoor() {
		LittleDoor structure = new LittleDoor();
		structure.doubledRelativeAxis = new LittleRelativeDoubledAxis(LittleGridContext.getMin(), LittleTileVec.ZERO, LittleTileVec.ZERO);
		structure.setTiles(new HashMapList<>());
		structure.axis = this.axis;
		structure.normalDirection = this.normalDirection;
		structure.duration = this.duration;
		return structure;
	}
	
	@Override
	public List<PlacePreviewTile> getAdditionalPreviews(PlacePreviews previews) {
		List<PlacePreviewTile> additional = new ArrayList<>();
		LittleTileBox box = new LittleTileBox(0, 0, 0, 1, 1, 1);
		//box.convertTo(doubledRelativeAxis.context, previews.context);
		additional.add(new PlacePreviewTileAxis(box, null, axis, getAdditionalAxisVec()));
		return additional;
	}
	
	public static class LittleRelativeDoubledAxis extends LittleTileVecContext {
		
		public LittleTileVec additional;
		
		public LittleRelativeDoubledAxis(LittleGridContext context, LittleTileVec vec, LittleTileVec additional) {
			super(context, vec);
			this.additional = additional;
		}
		
		public LittleRelativeDoubledAxis(String name, NBTTagCompound nbt) {
			super(name, nbt);
		}
		
		@Override
		protected void loadFromNBT(String name, NBTTagCompound nbt) {
			int[] array = nbt.getIntArray(name);
			if (array.length == 3) {
				this.vec = new LittleTileVec(array[0], array[1], array[2]);
				this.context = LittleGridContext.get();
				this.additional = new LittleTileVec(vec.x % 2, vec.y % 2, vec.z % 2);
				this.vec.x /= 2;
				this.vec.y /= 2;
				this.vec.z /= 2;
			} else if (array.length == 4) {
				this.vec = new LittleTileVec(array[0], array[1], array[2]);
				this.context = LittleGridContext.get(array[3]);
				this.additional = new LittleTileVec(vec.x % 2, vec.y % 2, vec.z % 2);
				this.vec.x /= 2;
				this.vec.y /= 2;
				this.vec.z /= 2;
			} else if (array.length == 7) {
				this.vec = new LittleTileVec(array[0], array[1], array[2]);
				this.context = LittleGridContext.get(array[3]);
				this.additional = new LittleTileVec(array[4], array[5], array[6]);
			} else
				throw new InvalidParameterException("No valid coords given " + nbt);
		}
		
		public LittleTileVecContext getNonDoubledVec() {
			return new LittleTileVecContext(context, vec);
		}
		
		public LittleTileVec getRotationVec() {
			LittleTileVec vec = this.vec.copy();
			vec.scale(2);
			vec.add(additional);
			return vec;
		}
		
		@Override
		public LittleRelativeDoubledAxis copy() {
			return new LittleRelativeDoubledAxis(context, vec.copy(), additional.copy());
		}
		
		@Override
		public void convertTo(LittleGridContext to) {
			LittleTileVec newVec = getRotationVec();
			newVec.convertTo(context, to);
			super.convertTo(to);
			additional = new LittleTileVec(newVec.x % 2, newVec.y % 2, newVec.z % 2);
			vec = newVec;
			vec.x /= 2;
			vec.y /= 2;
			vec.z /= 2;
		}
		
		@Override
		public void convertToSmallest() {
			if (isEven())
				super.convertToSmallest();
		}
		
		@Override
		public boolean equals(Object paramObject) {
			if (paramObject instanceof LittleRelativeDoubledAxis)
				return super.equals(paramObject) && additional.equals(((LittleRelativeDoubledAxis) paramObject).additional);
			return false;
		}
		
		public boolean isEven() {
			return additional.x % 2 == 0;
		}
		
		@Override
		public void writeToNBT(String name, NBTTagCompound nbt) {
			nbt.setIntArray(name, new int[] { vec.x, vec.y, vec.z, context.size, additional.x, additional.y, additional.z });
		}
		
		@Override
		public String toString() {
			return "[" + vec.x + "," + vec.y + "," + vec.z + ",grid:" + context.size + ",additional:" + additional + "]";
		}
	}
	
	public static class PlacePreviewTileAxis extends PlacePreviewTile {
		
		public static int red = ColorUtils.VecToInt(new Vec3d(1, 0, 0));
		public EnumFacing.Axis axis;
		public LittleTileVec additionalOffset;
		
		public PlacePreviewTileAxis(LittleTileBox box, LittleTilePreview preview, EnumFacing.Axis axis, LittleTileVec additionalOffset) {
			super(box, preview);
			this.axis = axis;
			this.additionalOffset = additionalOffset;
		}
		
		@Override
		public boolean needsCollisionTest() {
			return false;
		}
		
		@Override
		public PlacePreviewTile copy() {
			return new PlacePreviewTileAxis(box.copy(), null, axis, additionalOffset.copy());
		}
		
		@Override
		public List<LittleRenderingCube> getPreviews(LittleGridContext context) {
			ArrayList<LittleRenderingCube> cubes = new ArrayList<>();
			LittleTileBox preview = box.copy();
			int max = 40 * context.size;
			int min = -max;
			switch (axis) {
			case X:
				preview.minX = min;
				preview.maxX = max;
				break;
			case Y:
				preview.minY = min;
				preview.maxY = max;
				break;
			case Z:
				preview.minZ = min;
				preview.maxZ = max;
				break;
			default:
				break;
			}
			LittleRenderingCube cube = preview.getRenderingCube(context, null, 0);
			cube.sub(new Vec3d(context.gridMCLength / 2, context.gridMCLength / 2, context.gridMCLength / 2));
			cube.add(additionalOffset.getVec(context).scale(0.5));
			cube.color = red;
			cubes.add(cube);
			return cubes;
		}
		
		@Override
		public List<LittleTile> placeTile(EntityPlayer player, ItemStack stack, BlockPos pos, LittleGridContext context, TileEntityLittleTiles teLT, LittleStructure structure, List<LittleTile> unplaceableTiles, List<LittleTile> removedTiles, PlacementMode mode, EnumFacing facing, boolean requiresCollisionTest) {
			if (structure instanceof LittleDoor) {
				LittleDoor door = (LittleDoor) structure;
				LittleTilePos absolute = new LittleTilePos(pos, context, box.getMinVec());
				if (door.getMainTile() == null)
					door.selectMainTile();
				LittleTileVecContext vec = absolute.getRelative(door.getMainTile().getAbsolutePos());
				door.doubledRelativeAxis = new LittleRelativeDoubledAxis(vec.context, vec.vec, additionalOffset.copy());
			}
			return Collections.EMPTY_LIST;
		}
		
	}
	
	public static class LittleDoorParser extends LittleDoorBaseParser<LittleDoor> {
		
		public LittleDoorParser(String id, GuiParent parent) {
			super(id, parent);
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public LittleDoor parseStructure(int duration) {
			LittleDoor door = new LittleDoor();
			GuiTileViewer viewer = (GuiTileViewer) parent.get("tileviewer");
			door.doubledRelativeAxis = new LittleRelativeDoubledAxis(viewer.context, new LittleTileVec(viewer.axisX / 2, viewer.axisY / 2, viewer.axisZ / 2), new LittleTileVec(viewer.axisX % 2, viewer.axisY % 2, viewer.axisZ % 2));
			door.axis = viewer.axisDirection;
			door.normalDirection = RotationUtils.getFacing(viewer.normalAxis);
			door.duration = duration;
			return door;
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public void createControls(ItemStack stack, LittleStructure structure) {
			super.createControls(stack, structure);
			LittleDoor door = null;
			if (structure instanceof LittleDoor)
				door = (LittleDoor) structure;
			GuiTileViewer tile = new GuiTileViewer("tileviewer", 0, 30, 100, 100, stack);
			
			boolean even = false;
			if (door != null) {
				tile.axisDirection = door.axis;
				/* tile.axisX = door.axisPoint.x;
				 * tile.axisY = door.axisPoint.y;
				 * tile.axisZ = door.axisPoint.z; */
				tile.axisX = door.doubledRelativeAxis.vec.x * 2;
				tile.axisY = door.doubledRelativeAxis.vec.y * 2;
				tile.axisZ = door.doubledRelativeAxis.vec.z * 2;
				tile.normalAxis = door.normalDirection.getAxis();
				even = door.doubledRelativeAxis.isEven();
				tile.setEven(even);
			} else {
				tile.setEven(false);
			}
			tile.visibleAxis = true;
			tile.updateViewDirection();
			parent.controls.add(tile);
			parent.controls.add(new GuiIDButton("reset view", 109, 25, 0));
			//parent.controls.add(new GuiButton("y", 170, 50, 20));
			parent.controls.add(new GuiIDButton("flip view", 109, 45, 1));
			
			parent.controls.add(new GuiIDButton("swap axis", 109, 5, 2));
			parent.controls.add(new GuiIDButton("swap normal", 109, 65, 3));
			//parent.controls.add(new GuiButton("-->", 150, 50, 20));
			
			//parent.controls.add(new GuiButton("<-Z", 130, 70, 20));
			parent.controls.add(new GuiButton("up", "<-", 125, 86, 14) {
				@Override
				public void onClicked(int x, int y, int button) {
					
				}
				
			}.setRotation(90));
			parent.controls.add(new GuiIDButton("->", 146, 107, 4));
			parent.controls.add(new GuiIDButton("<-", 107, 107, 5));
			parent.controls.add(new GuiButton("down", "<-", 125, 107, 14) {
				@Override
				public void onClicked(int x, int y, int button) {
					
				}
				
			}.setRotation(-90));
			
			parent.controls.add(new GuiCheckBox("even", 107, 124, even));
			//parent.controls.add(new GuiButton("->", 190, 90, 20));
			//parent.controls.add(new GuiStateButton("direction", 3, 130, 50, 50, 20, "NORTH", "SOUTH", "WEST", "EAST"));
		}
		
		@CustomEventSubscribe
		@SideOnly(Side.CLIENT)
		public void onButtonClicked(GuiControlClickEvent event) {
			GuiTileViewer viewer = (GuiTileViewer) event.source.parent.get("tileviewer");
			if (event.source.is("swap axis")) {
				Axis axis = null;
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
			} else if (event.source.is("reset view")) {
				viewer.offsetX = 0;
				viewer.offsetY = 0;
				viewer.scale = 5;
				//viewer.viewDirection = ForgeDirection.EAST;
				//((GuiStateButton) event.source.parent.getControl("direction")).setState(3);
			} else if (event.source.is("flip view")) {
				viewer.viewDirection = viewer.viewDirection.getOpposite();
				viewer.baked = null;
				//viewer.viewDirection = ForgeDirection.getOrientation(((GuiStateButton) event.source).getState()+2);
			} else if (event.source instanceof GuiButton) {
				int amount = GuiScreen.isCtrlKeyDown() ? 2 * viewer.context.size : 2;
				if (event.source.is("<-")) {
					if (viewer.axisDirection == Axis.X)
						viewer.axisZ += amount;
					else
						viewer.axisX -= amount;
				}
				if (event.source.is("->")) {
					if (viewer.axisDirection == Axis.X)
						viewer.axisZ -= amount;
					else
						viewer.axisX += amount;
				}
				if (event.source.is("up")) {
					if (viewer.axisDirection == Axis.Y)
						viewer.axisZ -= amount;
					else
						viewer.axisY += amount;
				}
				if (event.source.is("down")) {
					if (viewer.axisDirection == Axis.Y)
						viewer.axisZ += amount;
					else
						viewer.axisY -= amount;
				} else if (event.source.is("swap normal")) {
					viewer.changeNormalAxis();
				}
			} else if (event.source.is("even")) {
				viewer.setEven(((GuiCheckBox) event.source).value);
			}
		}
		
	}
	
}
