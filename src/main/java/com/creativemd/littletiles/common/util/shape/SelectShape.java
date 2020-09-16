package com.creativemd.littletiles.common.util.shape;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.VectorUtils;
import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.littletiles.common.block.BlockTile;
import com.creativemd.littletiles.common.block.BlockTile.TEResult;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tile.math.vec.LittleAbsoluteVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class SelectShape {
	
	private static LinkedHashMap<String, SelectShape> shapes = new LinkedHashMap<>();
	
	public static void registerShape(SelectShape shape) {
		shapes.put(shape.key, shape);
	}
	
	public static Set<String> keys() {
		return shapes.keySet();
	}
	
	public static SelectShape tileShape = new SelectShape("tile") {
		
		@Override
		public void saveCustomSettings(GuiParent gui, NBTTagCompound nbt, LittleGridContext context) {
		
		}
		
		@Override
		public boolean rightClick(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			return false;
		}
		
		@Override
		public boolean leftClick(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			return true;
		}
		
		@Override
		public LittleBoxes getHighlightBoxes(World world, BlockPos pos, EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			return getBoxes(world, pos, player, nbt, result, context);
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public List<GuiControl> getCustomSettings(NBTTagCompound nbt, LittleGridContext context) {
			return new ArrayList<>();
		}
		
		@Override
		public LittleBoxes getBoxes(World world, BlockPos pos, EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			TEResult te = BlockTile.loadTeAndTile(world, pos, player);
			
			LittleBoxes boxes;
			if (te.isComplete()) {
				boxes = new LittleBoxes(te.te.getPos(), te.te.getContext());
				//if (te.tile.isChildOfStructure())
				//return boxes;
				boxes.add(te.tile.getBox().copy());
			} else {
				boxes = new LittleBoxes(pos, context);
				boxes.add(new LittleBox(0, 0, 0, context.size, context.size, context.size));
			}
			
			return boxes;
		}
		
		@Override
		public void deselect(EntityPlayer player, NBTTagCompound nbt, LittleGridContext context) {
			
		}
		
		@Override
		public void addExtraInformation(World world, NBTTagCompound nbt, List<String> list, LittleGridContext context) {
			
		}
	};
		
	public static final SelectShape TYPE = new SelectShape("type") {
		
		@Override
		public boolean rightClick(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			return false;
		}
		
		@Override
		public boolean leftClick(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			return true;
		}
		
		@Override
		public LittleBoxes getHighlightBoxes(World world, BlockPos pos, EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			return getBoxes(world, pos, player, nbt, result, context);
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public List<GuiControl> getCustomSettings(NBTTagCompound nbt, LittleGridContext context) {
			return new ArrayList<>();
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public void saveCustomSettings(GuiParent gui, NBTTagCompound nbt, LittleGridContext context) {
		
		}
		
		@Override
		public LittleBoxes getBoxes(World world, BlockPos pos, EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			TEResult te = BlockTile.loadTeAndTile(world, pos, player);
			
			LittleBoxes boxes;
			if (te.isComplete()) {
				LittleTile tile = te.tile;
				boxes = new LittleBoxes(te.te.getPos(), te.te.getContext());
				if (te.parent.isStructure())
					return boxes;
				boxes.add(te.tile.getBox().copy());
				
				for (Pair<IParentTileList, LittleTile> pair : te.te.allTiles()) {
					LittleTile toDestroy = pair.value;
					if (!pair.key.isStructure() && tile.canBeCombined(toDestroy) && toDestroy.canBeCombined(tile))
						boxes.addBox(pair.key, toDestroy);
				}
			} else {
				boxes = new LittleBoxes(pos, context);
				boxes.add(new LittleBox(0, 0, 0, context.size, context.size, context.size));
			}
			
			return boxes;
		}
		
		@Override
		public void deselect(EntityPlayer player, NBTTagCompound nbt, LittleGridContext context) {
			
		}
		
		@Override
		public void addExtraInformation(World world, NBTTagCompound nbt, List<String> list, LittleGridContext context) {
			
		}
	};
		
	public static final BasicSelectShape CUBE = new BasicSelectShape("cube") {
		
		@Override
		public LittleBox getBox(LittleVec vec, int thickness, EnumFacing side, LittleGridContext context) {
			LittleVec offset = new LittleVec(side);
			offset.scale((thickness - 1) / 2);
			vec.sub(offset);
			if ((thickness & 1) == 0 && side.getAxisDirection() == AxisDirection.NEGATIVE)
				vec.sub(side);
			
			LittleBox box = new LittleBox(vec, thickness, thickness, thickness);
			// box.makeItFitInsideBlock();
			return box;
		}
		
	};
	
	public static final BasicSelectShape BAR = new BasicSelectShape("bar") {
		
		@Override
		public LittleBox getBox(LittleVec vec, int thickness, EnumFacing side, LittleGridContext context) {
			LittleBox box = CUBE.getBox(vec, thickness, side, context);
			
			switch (side.getAxis()) {
			case X:
				box.minX = 0;
				box.maxX = context.size;
				break;
			case Y:
				box.minY = 0;
				box.maxY = context.size;
				break;
			case Z:
				box.minZ = 0;
				box.maxZ = context.size;
				break;
			}
			
			return box;
		}
		
	};
	
	public static final BasicSelectShape PLANE = new BasicSelectShape("plane") {
		
		@Override
		public LittleBox getBox(LittleVec vec, int thickness, EnumFacing side, LittleGridContext context) {
			LittleBox box = CUBE.getBox(vec, thickness, side, context);
			
			switch (side.getAxis()) {
			case X:
				box.minY = 0;
				box.maxY = context.size;
				box.minZ = 0;
				box.maxZ = context.size;
				break;
			case Y:
				box.minX = 0;
				box.maxX = context.size;
				box.minZ = 0;
				box.maxZ = context.size;
				break;
			case Z:
				box.minX = 0;
				box.maxX = context.size;
				box.minY = 0;
				box.maxY = context.size;
				break;
			}
			
			return box;
		}
		
	};
	
	public static final DragSelectShape DRAG_BOX = new DragSelectShape(DragShape.box);
	
	public static final SelectShape defaultShape = DRAG_BOX;
	
	public static SelectShape getShape(String name) {
		SelectShape shape = SelectShape.shapes.get(name);
		return shape == null ? SelectShape.defaultShape : shape;
	}
	
	public final String key;
	
	public SelectShape(String name) {
		this.key = name;
	}
	
	public abstract LittleBoxes getHighlightBoxes(World world, BlockPos pos, EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context);
	
	public abstract LittleBoxes getBoxes(World world, BlockPos pos, EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context);
	
	/** @return if the shape has been selected (information will then be send to the
	 *         server) */
	public abstract boolean rightClick(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context);
	
	/** @return if the shape has been selected (information will then be send to the
	 *         server) */
	public abstract boolean leftClick(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context);
	
	public abstract void deselect(EntityPlayer player, NBTTagCompound nbt, LittleGridContext context);
	
	public abstract void addExtraInformation(World world, NBTTagCompound nbt, List<String> list, LittleGridContext context);
	
	@SideOnly(Side.CLIENT)
	public abstract List<GuiControl> getCustomSettings(NBTTagCompound nbt, LittleGridContext context);
	
	@SideOnly(Side.CLIENT)
	public abstract void saveCustomSettings(GuiParent gui, NBTTagCompound nbt, LittleGridContext context);
	
	public void rotate(Rotation rotation, NBTTagCompound nbt) {
		
	}
	
	public void flip(Axis axis, NBTTagCompound nbt) {
		
	}
	
	public abstract static class BasicSelectShape extends SelectShape {
		
		public BasicSelectShape(String name) {
			super(name);
		}
		
		@Override
		public void deselect(EntityPlayer player, NBTTagCompound nbt, LittleGridContext context) {}
		
		@Override
		public boolean rightClick(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			return false;
		}
		
		@Override
		public boolean leftClick(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			return true;
		}
		
		@Override
		public LittleBoxes getHighlightBoxes(World world, BlockPos pos, EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			int thickness = nbt.getInteger("thick");
			if (thickness > context.size)
				nbt.setInteger("thick", thickness = context.size);
			LittleBoxes boxes = new LittleBoxes(result.getBlockPos(), context);
			LittleAbsoluteVec vec = new LittleAbsoluteVec(result, context);
			if (result.sideHit.getAxisDirection() == AxisDirection.POSITIVE && context.isAtEdge(VectorUtils.get(result.sideHit.getAxis(), result.hitVec)))
				vec.getVec().sub(result.sideHit);
			boxes.add(getBox(vec.getRelative(new LittleAbsoluteVec(result.getBlockPos(), context)).getVec(context), Math.max(1, nbt.getInteger("thick")), result.sideHit, context));
			return boxes;
		}
		
		@Override
		public LittleBoxes getBoxes(World world, BlockPos pos, EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			int thickness = nbt.getInteger("thick");
			if (thickness > context.size)
				nbt.setInteger("thick", thickness = context.size);
			LittleBoxes boxes = new LittleBoxes(result.getBlockPos(), context);
			LittleAbsoluteVec vec = new LittleAbsoluteVec(result, context);
			if (result.sideHit.getAxisDirection() == AxisDirection.POSITIVE && context.isAtEdge(VectorUtils.get(result.sideHit.getAxis(), result.hitVec)))
				vec.getVec().sub(result.sideHit);
			boxes.add(getBox(vec.getRelative(new LittleAbsoluteVec(result.getBlockPos(), context)).getVec(context), Math.max(1, nbt.getInteger("thick")), result.sideHit, context));
			return boxes;
		}
		
		@Override
		public void addExtraInformation(World world, NBTTagCompound nbt, List<String> list, LittleGridContext context) {
			list.add("thickness: " + Math.max(1, nbt.getInteger("thick")));
		}
		
		public abstract LittleBox getBox(LittleVec vec, int thickness, EnumFacing side, LittleGridContext context);
		
		@Override
		public List<GuiControl> getCustomSettings(NBTTagCompound nbt, LittleGridContext context) {
			List<GuiControl> controls = new ArrayList<>();
			controls.add(new GuiLabel("Size:", 0, 6));
			controls.add(new GuiSteppedSlider("thickness", 35, 5, 68, 10, Math.max(1, nbt.getInteger("thick")), 1, context.size));
			return controls;
		}
		
		@Override
		public void saveCustomSettings(GuiParent gui, NBTTagCompound nbt, LittleGridContext context) {
			GuiSteppedSlider thickness = (GuiSteppedSlider) gui.get("thickness");
			nbt.setInteger("thick", (int) thickness.value);
		}
		
	}
	
	public static class DragSelectShape extends SelectShape {
		
		private final DragShape shape;
		
		public DragSelectShape(DragShape shape) {
			super("drag" + shape.key);
			this.shape = shape;
		}
		
		public LittleAbsoluteVec first;
		
		public LittleBoxes getBoxes(EntityPlayer player, NBTTagCompound nbt, LittleAbsoluteVec min, LittleAbsoluteVec max, boolean preview, LittleGridContext context) {
			min.forceContext(max);
			LittleAbsoluteVec offset = new LittleAbsoluteVec(min.getPos(), min.getContext());
			LittleBox box = new LittleBox(new LittleBox(min.getRelative(offset).getVec(context)), new LittleBox(max.getRelative(offset).getVec(context)));
			return shape.getBoxes(new LittleBoxes(offset.getPos(), context), box.getMinVec(), box.getMaxVec(), player, nbt, preview, min, max);
		}
		
		@Override
		public LittleBoxes getHighlightBoxes(World world, BlockPos pos, EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			LittleAbsoluteVec vec = new LittleAbsoluteVec(result, context);
			if (result.sideHit.getAxisDirection() == AxisDirection.POSITIVE && context.isAtEdge(VectorUtils.get(result.sideHit.getAxis(), result.hitVec)))
				vec.getVec().sub(result.sideHit);
			if (first == null) {
				LittleBoxes boxes = new LittleBoxes(result.getBlockPos(), context);
				boxes.add(new LittleBox(vec.getRelative(new LittleAbsoluteVec(result.getBlockPos(), context)).getVec(context)));
				return boxes;
			}
			return getBoxes(player, nbt, first, vec, true, context);
		}
		
		@Override
		public boolean rightClick(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			return false;
		}
		
		@Override
		public boolean leftClick(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			if (first != null)
				return true;
			first = new LittleAbsoluteVec(result, context);
			if (result.sideHit.getAxisDirection() == AxisDirection.POSITIVE && context.isAtEdge(VectorUtils.get(result.sideHit.getAxis(), result.hitVec)))
				first.getVec().sub(result.sideHit);
			return false;
		}
		
		@Override
		public void deselect(EntityPlayer player, NBTTagCompound nbt, LittleGridContext context) {
			first = null;
		}
		
		@Override
		public LittleBoxes getBoxes(World world, BlockPos pos, EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
			LittleAbsoluteVec vec = new LittleAbsoluteVec(result, context);
			if (result.sideHit.getAxisDirection() == AxisDirection.POSITIVE && context.isAtEdge(VectorUtils.get(result.sideHit.getAxis(), result.hitVec)))
				vec.getVec().sub(result.sideHit);
			LittleBoxes boxes = getBoxes(player, nbt, first, vec, false, context);
			first = null;
			return boxes;
		}
		
		@Override
		public void addExtraInformation(World world, NBTTagCompound nbt, List<String> list, LittleGridContext context) {
			shape.addExtraInformation(nbt, list);
		}
		
		@Override
		public List<GuiControl> getCustomSettings(NBTTagCompound nbt, LittleGridContext context) {
			return shape.getCustomSettings(nbt, context);
		}
		
		@Override
		public void saveCustomSettings(GuiParent gui, NBTTagCompound nbt, LittleGridContext context) {
			shape.saveCustomSettings(gui, nbt, context);
		}
		
		@Override
		public void flip(Axis axis, NBTTagCompound nbt) {
			shape.flip(nbt, axis);
		}
		
		@Override
		public void rotate(Rotation rotation, NBTTagCompound nbt) {
			shape.rotate(nbt, rotation);
		}
		
	}
	
	static {
		for (DragShape shape : DragShape.shapes())
			if (shape == DragShape.box)
				registerShape(DRAG_BOX);
			else
				registerShape(new DragSelectShape(shape));
		registerShape(TYPE);
		registerShape(CUBE);
		registerShape(BAR);
		registerShape(PLANE);
	}
	
}
