package com.creativemd.littletiles.common.structure.premade.signal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.math.box.CubeObject;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.tile.LittleRenderingCube;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.structure.premade.LittleStructurePremade;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.signal.ISignalBase;
import com.creativemd.littletiles.common.structure.signal.ISignalTransmitter;
import com.creativemd.littletiles.common.structure.signal.SignalNetwork;
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

public class LittleSignalCable extends LittleStructurePremade implements ISignalTransmitter {
	
	public LittleCableFace[] faces = new LittleCableFace[6];
	private SignalNetwork network;
	
	public LittleSignalCable(LittleStructureType type) {
		super(type);
	}
	
	@Override
	public int getBandwidth() {
		return ((LittleStructureTypeCable) type).bandwidth;
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
	public Iterator<ISignalBase> connections() {
		if (load())
			return new Iterator<ISignalBase>() {
				
				int index = searchForNextIndex(0);
				LittleAbsoluteBox box = new SurroundingBox(false).add(tiles.entrySet()).getAbsoluteBox();
				
				int searchForNextIndex(int index) {
					while (index < 6 && (faces[index] == null || !faces[index].verifyConnect(EnumFacing.VALUES[index], box))) {
						faces[index] = null;
						index++;
					}
					return index;
				}
				
				@Override
				public boolean hasNext() {
					return index < 6 && faces[index] != null;
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
	public boolean canConnect(EnumFacing facing) {
		return true;
	}
	
	@Override
	public void connect(EnumFacing facing, ISignalBase base, LittleGridContext context, int distance) {
		int index = facing.ordinal();
		if (faces[index] != null) {
			if (faces[index].getConnection() == base)
				return;
			faces[index].disconnect(facing);
		} else
			faces[index] = new LittleCableFace();
		faces[index].connect(base, context, distance);
		
	}
	
	@Override
	public void disconnect(EnumFacing facing, ISignalBase base) {
		int index = facing.ordinal();
		if (faces[index] != null)
			faces[index] = null;
	}
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		int[] result = nbt.getIntArray("faces");
		if (result != null && result.length == 12)
			for (int i = 0; i < faces.length; i++) {
				int distance = result[i * 2];
				if (distance < 0)
					faces[i] = null;
				else {
					faces[i] = new LittleCableFace();
					faces[i].distance = distance;
					faces[i].context = LittleGridContext.get(result[i * 2 + 1]);
				}
			}
	}
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		int[] result = new int[12];
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
	
	@Override
	public void neighbourChanged() {
		if (getWorld().isRemote || !load())
			return;
		
		LittleAbsoluteBox box = new SurroundingBox(false).add(tiles.entrySet()).getAbsoluteBox();
		
		for (int i = 0; i < faces.length; i++) {
			EnumFacing facing = EnumFacing.VALUES[i];
			
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
	@SideOnly(Side.CLIENT)
	public void getRenderingCubes(BlockPos pos, BlockRenderLayer layer, List<LittleRenderingCube> cubes) {
		int color = getMainTile() instanceof LittleTileColored ? ((LittleTileColored) getMainTile()).color : ColorUtils.WHITE;
		
		for (int i = 0; i < faces.length; i++) {
			if (faces[i] == null)
				continue;
			
			int distance = faces[i].distance;
			EnumFacing facing = EnumFacing.VALUES[i];
			SurroundingBox box = new SurroundingBox(false).add(tiles.entrySet());
			LittleGridContext context = faces[i].context;
			
			if (box.getContext().size > context.size) {
				distance *= box.getContext().size / context.size;
				context = box.getContext();
			} else if (context.size > box.getContext().size)
				box.convertTo(context);
			
			Axis axis = facing.getAxis();
			Axis one = RotationUtils.getDifferentAxisFirst(axis);
			Axis two = RotationUtils.getDifferentAxisSecond(axis);
			boolean positive = facing.getAxisDirection() == AxisDirection.POSITIVE;
			
			LittleVec min = box.getMinPosOffset();
			LittleVec size = box.getSize();
			LittleVec max = size.copy();
			max.add(min);
			LittleBox renderBox = new LittleBox(min, max);
			BlockPos difference = pos.subtract(box.getMinPos());
			renderBox.sub(context.toGrid(difference.getX()), context.toGrid(difference.getY()), context.toGrid(difference.getZ()));
			
			int internalAxisOffset = positive ? (int) (max.get(axis) - RotationUtils.get(axis, difference) * context.size) : min.get(axis) - RotationUtils.get(axis, difference) * context.size;
			if (positive) {
				renderBox.setMin(axis, internalAxisOffset);
				renderBox.setMax(axis, internalAxisOffset + distance);
			} else {
				renderBox.setMin(axis, internalAxisOffset - distance);
				renderBox.setMax(axis, internalAxisOffset);
			}
			
			LittleRenderingCube cube = renderBox.getRenderingCube(context, LittleTiles.singleCable, axis.ordinal());
			cube.color = color;
			cube.keepVU = true;
			cube.allowOverlap = true;
			float shrink = (float) context.toVanillaGrid(size.get(axis)) * 0.18F;
			cube.setMin(one, cube.getMin(one) + shrink);
			cube.setMax(one, cube.getMax(one) - shrink);
			cube.setMin(two, cube.getMin(two) + shrink);
			cube.setMax(two, cube.getMax(two) - shrink);
			cubes.add(cube);
		}
	}
	
	public class LittleCableFace {
		
		private ISignalBase connection;
		private int distance;
		private LittleGridContext context;
		
		public LittleCableFace() {
			
		}
		
		public void disconnect(EnumFacing facing) {
			if (connection != null)
				connection.disconnect(facing.getOpposite(), LittleSignalCable.this);
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
	
	public static class LittleStructureTypeCable extends LittleStructureTypePremade {
		
		public final int bandwidth;
		
		@SideOnly(Side.CLIENT)
		public List<RenderCubeObject> cubes;
		
		public LittleStructureTypeCable(String id, String category, Class<? extends LittleStructure> structureClass, int attribute, String modid, int bandwidth) {
			super(id, category, structureClass, attribute | LittleStructureAttribute.NEIGHBOR_LISTENER, modid);
			this.bandwidth = bandwidth;
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public List<RenderCubeObject> getRenderingCubes() {
			if (cubes == null) {
				float size = (float) ((Math.sqrt(bandwidth) * 1F / 32F) * 1.4);
				cubes = new ArrayList<>();
				cubes.add(new RenderCubeObject(0, 0.5F - size, 0.5F - size, size * 2, 0.5F + size, 0.5F + size, LittleTiles.coloredBlock).setColor(-13619152));
				cubes.add(new RenderCubeObject(0 + size * 2, 0.5F - size * 0.8F, 0.5F - size * 0.8F, 1 - size * 2, 0.5F + size * 0.8F, 0.5F + size * 0.8F, LittleTiles.singleCable).setColor(-13619152).setKeepUV(true));
				cubes.add(new RenderCubeObject(1 - size * 2, 0.5F - size, 0.5F - size, 1, 0.5F + size, 0.5F + size, LittleTiles.coloredBlock).setColor(-13619152));
			}
			return cubes;
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
						if (structure instanceof LittleSignalCable && ((LittleSignalCable) structure).getBandwidth() == bandwidth) {
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
					if (structure instanceof LittleSignalCable && ((LittleSignalCable) structure).getBandwidth() == bandwidth) {
						AxisAlignedBB box = structure.getSurroundingBox().offset(-tileEntity.getPos().getX(), -tileEntity.getPos().getY(), -tileEntity.getPos().getZ());
						LittleRenderingCube cube = new LittleRenderingCube(new CubeObject(box), null, Blocks.AIR, 0);
						cube.setMin(Axis.X, 0);
						cube.setMax(Axis.X, 1);
						cubes.add(cube);
						
						cube = new LittleRenderingCube(new CubeObject(box), null, Blocks.AIR, 0);
						cube.setMin(Axis.Y, 0);
						cube.setMax(Axis.Y, 1);
						cubes.add(cube);
						
						cube = new LittleRenderingCube(new CubeObject(box), null, Blocks.AIR, 0);
						cube.setMin(Axis.Z, 0);
						cube.setMax(Axis.Z, 1);
						cubes.add(cube);
					}
				}
				
			}
			if (cubes.isEmpty())
				return null;
			return cubes;
		}
		
	}
	
}
