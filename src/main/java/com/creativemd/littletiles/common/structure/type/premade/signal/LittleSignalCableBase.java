package com.creativemd.littletiles.common.structure.type.premade.signal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.math.box.CubeObject;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.tile.LittleRenderingCube;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.signal.ISignalBase;
import com.creativemd.littletiles.common.structure.signal.SignalNetwork;
import com.creativemd.littletiles.common.structure.type.premade.LittleStructurePremade;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.LittleTileColored;
import com.creativemd.littletiles.common.tile.math.box.LittleAbsoluteBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.vec.SurroundingBox;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class LittleSignalCableBase extends LittleStructurePremade implements ISignalBase {
	
	private SignalNetwork network;
	
	protected LittleConnectionFace[] faces;
	
	public LittleSignalCableBase(LittleStructureType type) {
		super(type);
		this.faces = new LittleConnectionFace[getNumberOfConnections()];
	}
	
	@Override
	public SignalNetwork getNetwork() {
		return network;
	}
	
	@Override
	public void setNetwork(SignalNetwork network) {
		this.network = network;
	}
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		int[] result = nbt.getIntArray("faces");
		if (result != null && result.length == getNumberOfConnections() * 2)
			for (int i = 0; i < faces.length; i++) {
				int distance = result[i * 2];
				if (distance < 0)
					faces[i] = null;
				else {
					faces[i] = new LittleConnectionFace();
					faces[i].distance = distance;
					faces[i].context = LittleGridContext.get(result[i * 2 + 1]);
				}
			}
	}
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		int[] result = new int[getNumberOfConnections() * 2];
		for (int i = 0; i < faces.length; i++) {
			if (faces[i] != null) {
				result[i * 2] = faces[i].distance;
				result[i * 2 + 1] = faces[i].context.size;
			} else {
				result[i * 2] = -1;
				result[i * 2 + 1] = 0;
			}
		}
		nbt.setIntArray("faces", result);
	}
	
	public abstract EnumFacing getFacing(int index);
	
	public abstract int getIndex(EnumFacing facing);
	
	@Override
	public int getBandwidth() {
		return ((LittleStructureTypeNetwork) type).bandwidth;
	}
	
	public int getNumberOfConnections() {
		return ((LittleStructureTypeNetwork) type).numberOfConnections;
	}
	
	@Override
	public void connect(EnumFacing facing, ISignalBase base, LittleGridContext context, int distance) {
		int index = getIndex(facing);
		if (faces[index] != null) {
			if (faces[index].getConnection() == base)
				return;
			faces[index].disconnect(facing);
		} else
			faces[index] = new LittleConnectionFace();
		faces[index].connect(base, context, distance);
		
	}
	
	@Override
	public void disconnect(EnumFacing facing, ISignalBase base) {
		int index = getIndex(facing);
		if (faces[index] != null) {
			faces[index] = null;
			updateStructure();
		}
	}
	
	@Override
	public void neighbourChanged() {
		if (getWorld().isRemote || !load())
			return;
		
		LittleAbsoluteBox box = new SurroundingBox(false).add(tiles.entrySet()).getAbsoluteBox();
		
		for (int i = 0; i < faces.length; i++) {
			EnumFacing facing = getFacing(i);
			
			LittleConnectResult result = checkConnection(facing, box);
			if (result != null) {
				this.connect(facing, result.base, result.context, result.distance);
				result.base.connect(facing.getOpposite(), this, result.context, result.distance);
			} else {
				if (faces[i] != null)
					faces[i].disconnect(facing);
				faces[i] = null;
			}
		}
	}
	
	@Override
	public Iterator<ISignalBase> connections() {
		if (load())
			return new Iterator<ISignalBase>() {
				
				int index = searchForNextIndex(0);
				LittleAbsoluteBox box = new SurroundingBox(false).add(tiles.entrySet()).getAbsoluteBox();
				
				int searchForNextIndex(int index) {
					while (index < faces.length && (faces[index] == null || !faces[index].verifyConnect(getFacing(index), box))) {
						faces[index] = null;
						index++;
					}
					return index;
				}
				
				@Override
				public boolean hasNext() {
					return index < faces.length && faces[index] != null;
				}
				
				@Override
				public ISignalBase next() {
					ISignalBase next = faces[index].getConnection();
					this.index = searchForNextIndex(index++);
					return next;
				}
			};
		return new Iterator<ISignalBase>() {
			
			@Override
			public boolean hasNext() {
				return false;
			}
			
			@Override
			public ISignalBase next() {
				return null;
			}
			
		};
	}
	
	protected LittleConnectResult checkConnection(World world, LittleAbsoluteBox box, EnumFacing facing, BlockPos pos) throws ConnectionException {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof TileEntityLittleTiles) {
			TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
			
			LittleTile closest = null;
			int minDistance = 0;
			
			for (LittleTile tile : te)
				if (!tile.isChildOfStructure(this)) {
					int distance = box.getDistanceIfEqualFromOneSide(facing, tile.box, tile.te.getPos(), tile.getContext());
					if (distance < 0)
						continue;
					
					if (closest == null || minDistance > distance) {
						closest = tile;
						minDistance = distance;
					}
				}
			
			if (closest != null && closest.isChildOfStructure() && closest.connection.getStructure(world) instanceof ISignalBase && ((ISignalBase) closest.connection.getStructureWithoutLoading()).compatible(this)) {
				box = box.createBoxFromFace(facing, minDistance);
				
				HashMapList<BlockPos, LittleBox> boxes = box.splitted();
				for (Entry<BlockPos, ArrayList<LittleBox>> entry : boxes.entrySet()) {
					TileEntity toSearchIn = world.getTileEntity(entry.getKey());
					if (toSearchIn instanceof TileEntityLittleTiles) {
						TileEntityLittleTiles parsedSearch = (TileEntityLittleTiles) toSearchIn;
						LittleBox toCheck = entry.getValue().get(0);
						try {
							parsedSearch.convertToAtMinimum(box.getContext());
							if (parsedSearch.getContext().size > box.getContext().size)
								toCheck.convertTo(box.getContext(), parsedSearch.getContext());
							
							if (!parsedSearch.isSpaceForLittleTile(toCheck))
								throw new ConnectionException("No space");
						} finally {
							parsedSearch.convertToSmallest();
						}
					} else if (!world.getBlockState(entry.getKey()).getMaterial().isReplaceable())
						throw new ConnectionException("Block in the way");
				}
				
				ISignalBase base = (ISignalBase) closest.connection.getStructureWithoutLoading();
				if (base.canConnect(facing.getOpposite()))
					return new LittleConnectResult(base, box.getContext(), minDistance);
				throw new ConnectionException("Side is invalid");
			} else if (closest != null)
				throw new ConnectionException("Tile in the way");
			
		}
		
		return null;
	}
	
	public LittleConnectResult checkConnection(EnumFacing facing, LittleAbsoluteBox box) {
		if (!canConnect(facing))
			return null;
		
		BlockPos pos = box.getMinPos();
		Axis axis = facing.getAxis();
		boolean positive = facing.getAxisDirection() == AxisDirection.POSITIVE;
		if (positive)
			pos = RotationUtils.setValue(pos, box.getMaxPos(axis), axis);
		
		World world = getWorld();
		
		try {
			if (positive ? box.getMaxGridFrom(axis, pos) < box.getContext().size : box.getMinGridFrom(axis, pos) > 0) {
				LittleConnectResult result = checkConnection(world, box, facing, pos);
				if (result != null)
					return result;
			}
			
			return checkConnection(world, box, facing, pos.offset(facing));
		} catch (ConnectionException e) {
			return null;
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getRenderingCubes(BlockPos pos, BlockRenderLayer layer, List<LittleRenderingCube> cubes) {
		if (layer != BlockRenderLayer.SOLID)
			return;
		int color = getMainTile() instanceof LittleTileColored ? ((LittleTileColored) getMainTile()).color : ColorUtils.WHITE;
		SurroundingBox box = new SurroundingBox(false).add(tiles.entrySet());
		
		LittleVec min = box.getMinPosOffset();
		LittleVec max = box.getSize();
		max.add(min);
		LittleBox overallBox = new LittleBox(min, max);
		BlockPos difference = pos.subtract(box.getMinPos());
		overallBox.sub(box.getContext().toGrid(difference.getX()), box.getContext().toGrid(difference.getY()), box.getContext().toGrid(difference.getZ()));
		
		render(box, overallBox, cubes, color);
	}
	
	@SideOnly(Side.CLIENT)
	public void renderFace(EnumFacing facing, LittleGridContext context, LittleBox renderBox, int distance, Axis axis, Axis one, Axis two, boolean positive, int color, List<LittleRenderingCube> cubes) {
		if (positive) {
			renderBox.setMin(axis, renderBox.getMax(axis));
			renderBox.setMax(axis, renderBox.getMax(axis) + distance);
		} else {
			renderBox.setMax(axis, renderBox.getMin(axis));
			renderBox.setMin(axis, renderBox.getMin(axis) - distance);
		}
		
		LittleRenderingCube cube = renderBox.getRenderingCube(context, LittleTiles.singleCable, axis.ordinal());
		if (positive)
			cube.setMax(axis, cube.getMin(axis) + cube.getSize(axis) / 2);
		else
			cube.setMin(axis, cube.getMax(axis) - cube.getSize(axis) / 2);
		
		cube.color = color;
		cube.keepVU = true;
		cube.allowOverlap = true;
		float shrink = 0.18F;
		float shrinkOne = cube.getSize(one) * shrink;
		float shrinkTwo = cube.getSize(two) * shrink;
		cube.setMin(one, cube.getMin(one) + shrinkOne);
		cube.setMax(one, cube.getMax(one) - shrinkOne);
		cube.setMin(two, cube.getMin(two) + shrinkTwo);
		cube.setMax(two, cube.getMax(two) - shrinkTwo);
		cubes.add(cube);
	}
	
	@SideOnly(Side.CLIENT)
	public void render(SurroundingBox box, LittleBox overallBox, List<LittleRenderingCube> cubes, int color) {
		for (int i = 0; i < faces.length; i++) {
			if (faces[i] == null)
				continue;
			
			int distance = faces[i].distance;
			EnumFacing facing = getFacing(i);
			
			Axis axis = facing.getAxis();
			Axis one = RotationUtils.getDifferentAxisFirst(axis);
			Axis two = RotationUtils.getDifferentAxisSecond(axis);
			boolean positive = facing.getAxisDirection() == AxisDirection.POSITIVE;
			LittleGridContext context = faces[i].context;
			
			LittleBox renderBox = overallBox.copy();
			
			if (box.getContext().size > context.size) {
				distance *= box.getContext().size / context.size;
				context = box.getContext();
			} else if (context.size > box.getContext().size)
				renderBox.convertTo(box.getContext(), context);
			
			renderFace(facing, context, renderBox, distance, axis, one, two, positive, color, cubes);
		}
	}
	
	public class LittleConnectionFace {
		
		public ISignalBase connection;
		public int distance;
		public LittleGridContext context;
		
		public LittleConnectionFace() {
			
		}
		
		public void disconnect(EnumFacing facing) {
			if (connection != null)
				connection.disconnect(facing.getOpposite(), LittleSignalCableBase.this);
			if (hasNetwork())
				getNetwork().remove(connection);
			connection = null;
			updateStructure();
		}
		
		public void connect(ISignalBase connection, LittleGridContext context, int distance) {
			if (this.connection != null)
				throw new RuntimeException("Cannot connect until old connection is closed");
			
			if (hasNetwork())
				getNetwork().add(connection);
			this.connection = connection;
			this.context = context;
			this.distance = distance;
			updateStructure();
		}
		
		public boolean verifyConnect(EnumFacing facing, LittleAbsoluteBox box) {
			if (connection != null)
				return true;
			
			LittleConnectResult result = checkConnection(facing, box);
			if (result != null) {
				this.connection = result.base;
				this.context = result.context;
				this.distance = result.distance;
				return true;
			}
			return false;
		}
		
		public ISignalBase getConnection() {
			return connection;
		}
	}
	
	public static class LittleConnectResult {
		
		public final ISignalBase base;
		public final LittleGridContext context;
		public final int distance;
		
		public LittleConnectResult(ISignalBase base, LittleGridContext context, int distance) {
			this.base = base;
			this.context = context;
			this.distance = distance;
		}
	}
	
	public static class ConnectionException extends Exception {
		
		public ConnectionException(String msg) {
			super(msg);
		}
		
	}
	
	public static abstract class LittleStructureTypeNetwork extends LittleStructureTypePremade {
		
		public final int bandwidth;
		public final int numberOfConnections;
		
		public LittleStructureTypeNetwork(String id, String category, Class<? extends LittleStructure> structureClass, int attribute, String modid, int bandwidth, int numberOfConnections) {
			super(id, category, structureClass, attribute | LittleStructureAttribute.NEIGHBOR_LISTENER, modid);
			this.bandwidth = bandwidth;
			this.numberOfConnections = numberOfConnections;
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public List<LittleRenderingCube> getPositingCubes(World world, BlockPos pos, ItemStack stack) {
			List<LittleRenderingCube> cubes = new ArrayList<>();
			for (int i = 0; i < 6; i++) {
				EnumFacing facing = EnumFacing.VALUES[i];
				Axis axis = facing.getAxis();
				TileEntity tileEntity = world.getTileEntity(pos.offset(facing));
				if (tileEntity instanceof TileEntityLittleTiles) {
					for (LittleStructure structure : ((TileEntityLittleTiles) tileEntity).allStructures()) {
						if (structure instanceof ISignalBase && ((ISignalBase) structure).getBandwidth() == bandwidth && ((ISignalBase) structure).canConnect(facing.getOpposite())) {
							LittleRenderingCube cube = new LittleRenderingCube(new CubeObject(structure.getSurroundingBox().offset(-tileEntity.getPos().getX(), -tileEntity.getPos().getY(), -tileEntity.getPos().getZ())), null, Blocks.AIR, 0);
							cube.setMin(axis, 0);
							cube.setMax(axis, 1);
							cubes.add(cube);
						}
					}
					
				}
			}
			
			TileEntity tileEntity = world.getTileEntity(pos);
			if (tileEntity instanceof TileEntityLittleTiles) {
				for (LittleStructure structure : ((TileEntityLittleTiles) tileEntity).allStructures()) {
					if (structure instanceof ISignalBase && ((ISignalBase) structure).getBandwidth() == bandwidth) {
						AxisAlignedBB box = structure.getSurroundingBox().offset(-tileEntity.getPos().getX(), -tileEntity.getPos().getY(), -tileEntity.getPos().getZ());
						LittleRenderingCube cube;
						
						if (((ISignalBase) structure).canConnect(EnumFacing.WEST) || ((ISignalBase) structure).canConnect(EnumFacing.EAST)) {
							cube = new LittleRenderingCube(new CubeObject(box), null, Blocks.AIR, 0);
							if (((ISignalBase) structure).canConnect(EnumFacing.WEST))
								cube.setMin(Axis.X, 0);
							if (((ISignalBase) structure).canConnect(EnumFacing.EAST))
								cube.setMax(Axis.X, 1);
							cubes.add(cube);
						}
						
						if (((ISignalBase) structure).canConnect(EnumFacing.DOWN) || ((ISignalBase) structure).canConnect(EnumFacing.UP)) {
							cube = new LittleRenderingCube(new CubeObject(box), null, Blocks.AIR, 0);
							if (((ISignalBase) structure).canConnect(EnumFacing.DOWN))
								cube.setMin(Axis.Y, 0);
							if (((ISignalBase) structure).canConnect(EnumFacing.UP))
								cube.setMax(Axis.Y, 1);
							cubes.add(cube);
						}
						
						if (((ISignalBase) structure).canConnect(EnumFacing.NORTH) || ((ISignalBase) structure).canConnect(EnumFacing.SOUTH)) {
							cube = new LittleRenderingCube(new CubeObject(box), null, Blocks.AIR, 0);
							if (((ISignalBase) structure).canConnect(EnumFacing.NORTH))
								cube.setMin(Axis.Z, 0);
							if (((ISignalBase) structure).canConnect(EnumFacing.SOUTH))
								cube.setMax(Axis.Z, 1);
							cubes.add(cube);
						}
					}
				}
				
			}
			if (cubes.isEmpty())
				return null;
			return cubes;
		}
	}
	
}
