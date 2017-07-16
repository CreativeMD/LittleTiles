package com.creativemd.littletiles.common.items.geo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.creativemd.creativecore.gui.GuiControl;
import com.creativemd.creativecore.gui.container.GuiParent;
import com.creativemd.creativecore.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.littletiles.common.items.ItemUtilityKnife;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class SelectShape {
	
	public static LinkedHashMap<String, SelectShape> shapes = new LinkedHashMap<>();	
	
	public static BasicSelectShape CUBE = new BasicSelectShape("cube"){
		
		@Override
		public LittleTileBox getBox(LittleTileVec vec, int thickness, EnumFacing side) {
			LittleTileVec offset = new LittleTileVec(side);
			offset.scale((int) (thickness-1)/2);
			vec.subVec(offset);
			if(((thickness & 1) == 0 && side.getAxisDirection() == AxisDirection.NEGATIVE) || side.getAxisDirection() == AxisDirection.POSITIVE)
				vec.subVec(new LittleTileVec(side));
			
			LittleTileBox box = new LittleTileBox(vec, new LittleTileSize(thickness, thickness, thickness));
			//box.makeItFitInsideBlock();
			return box;
		}
		
	};
	
	public static BasicSelectShape BAR = new BasicSelectShape("bar"){
		
		@Override
		public LittleTileBox getBox(LittleTileVec vec, int thickness, EnumFacing side) {
			LittleTileBox box = CUBE.getBox(vec, thickness, side);
			
			switch(side.getAxis())
			{
			case X:
				box.minX = 0;
				box.maxX = LittleTile.gridSize;
				break;
			case Y:
				box.minY = 0;
				box.maxY = LittleTile.gridSize;
				break;
			case Z:
				box.minZ = 0;
				box.maxZ = LittleTile.gridSize;
				break;	
			}
			
			return box;
		}
		
	};
	
	public static BasicSelectShape PLANE = new BasicSelectShape("plane"){
		
		@Override
		public LittleTileBox getBox(LittleTileVec vec, int thickness, EnumFacing side) {
			LittleTileBox box = CUBE.getBox(vec, thickness, side);
			
			switch(side.getAxis())
			{
			case X:
				box.minY = 0;
				box.maxY = LittleTile.gridSize;
				box.minZ = 0;
				box.maxZ = LittleTile.gridSize;
				break;
			case Y:
				box.minX = 0;
				box.maxX = LittleTile.gridSize;
				box.minZ = 0;
				box.maxZ = LittleTile.gridSize;
				break;
			case Z:
				box.minX = 0;
				box.maxX = LittleTile.gridSize;
				box.minY = 0;
				box.maxY = LittleTile.gridSize;
				break;	
			}
			
			return box;
		}
		
	};
	
	public static SelectShape defaultShape = CUBE;
	
	public static SelectShape getShape(String name)
	{
		SelectShape shape = SelectShape.shapes.get(name);
		return shape == null ? SelectShape.defaultShape : shape;
	}
	
	public final String key;
	
	public SelectShape(String name) {
		shapes.put(name, this);
		this.key = name;
	}
	
	public abstract List<LittleTileBox> getHighlightBoxes(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result);
	
	/**
	 * @return if the shape has been selected (information will then be send to the server)
	 */
	public abstract boolean rightClick(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result);
	
	/**
	 * @return if the shape has been selected (information will then be send to the server)
	 */
	public abstract boolean leftClick(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result);
	
	public abstract void deselect(EntityPlayer player, NBTTagCompound nbt);
	
	public abstract List<LittleTileBox> getBoxes(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result);
	
	public abstract void addExtraInformation(EntityPlayer player, NBTTagCompound nbt, List<String> list);
	
	@SideOnly(Side.CLIENT)
	public abstract List<GuiControl> getCustomSettings(NBTTagCompound nbt);
	
	@SideOnly(Side.CLIENT)
	public abstract void saveCustomSettings(GuiParent gui, NBTTagCompound nbt);
	
	public abstract static class BasicSelectShape extends SelectShape {
		
		public BasicSelectShape(String name) {
			super(name);
		}
		
		@Override
		public void deselect(EntityPlayer player, NBTTagCompound nbt) {}
		
		@Override
		public boolean rightClick(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result) {
			return false;
		}
		
		@Override
		public boolean leftClick(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result) {
			return true;
		}
		
		@Override
		public List<LittleTileBox> getHighlightBoxes(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result) {
			List<LittleTileBox> boxes = new ArrayList<>();
			LittleTileBox box = getBox(new LittleTileVec(result.hitVec).getRelativeVec(result.getBlockPos()), Math.max(1, nbt.getInteger("thick")), result.sideHit);
			box.addOffset(new LittleTileVec(result.getBlockPos()));
			boxes.add(box);
			return boxes;
		}
		
		@Override
		public List<LittleTileBox> getBoxes(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result) {
			List<LittleTileBox> boxes = new ArrayList<>();
			LittleTileBox box = getBox(new LittleTileVec(result.hitVec).getRelativeVec(result.getBlockPos()), Math.max(1, nbt.getInteger("thick")), result.sideHit);
			box.addOffset(new LittleTileVec(result.getBlockPos()));
			boxes.add(box);
			return boxes;
		}
		
		@Override
		public void addExtraInformation(EntityPlayer player, NBTTagCompound nbt, List<String> list) {
			list.add("thickness: " + Math.max(1, nbt.getInteger("thick")));
		}

		public abstract LittleTileBox getBox(LittleTileVec vec, int thickness, EnumFacing side);
		
		@Override
		public List<GuiControl> getCustomSettings(NBTTagCompound nbt) {
			List<GuiControl> controls = new ArrayList<>();
			controls.add(new GuiLabel("Size:", 0, 6));
			controls.add(new GuiSteppedSlider("thickness", 35, 5, 68, 10, Math.max(1, nbt.getInteger("thick")), 1, LittleTile.gridSize));
			return controls;
		}
		
		@Override
		public void saveCustomSettings(GuiParent gui, NBTTagCompound nbt) {
			GuiSteppedSlider thickness = (GuiSteppedSlider) gui.get("thickness");
			nbt.setInteger("thick", (int) thickness.value);
		}
		
	}
	
	public static class ChiselSelectShape extends SelectShape {
		
		private final ChiselShape shape;
		
		public ChiselSelectShape(ChiselShape shape) {
			super("drag" + shape.key);
			this.shape = shape;
		}
		
		public LittleTileVec first;
		
		public List<LittleTileBox> getBoxes(EntityPlayer player, NBTTagCompound nbt, LittleTileVec min, LittleTileVec max, boolean preview)
		{
			LittleTileBox box = new LittleTileBox(new LittleTileBox(min), new LittleTileBox(max));
			return shape.getBoxes(box.getMinVec(), box.getMaxVec(), player, nbt, preview, min, max);
		}

		@Override
		public List<LittleTileBox> getHighlightBoxes(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result) {
			if(first == null)
			{
				ArrayList<LittleTileBox> boxes = new ArrayList<>();
				LittleTileVec vec = new LittleTileVec(result.hitVec);
				if(result.sideHit.getAxisDirection() == AxisDirection.POSITIVE)
					vec.subVec(new LittleTileVec(result.sideHit));
				boxes.add(new LittleTileBox(vec));
				return boxes;
			}
			LittleTileVec vec = new LittleTileVec(result.hitVec);
			if(result.sideHit.getAxisDirection() == AxisDirection.POSITIVE)
				vec.subVec(new LittleTileVec(result.sideHit));
			return getBoxes(player, nbt, first, vec, true);
		}

		@Override
		public boolean rightClick(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result) {
			return false;
		}

		@Override
		public boolean leftClick(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result) {
			if(first != null)
				return true;
			first = new LittleTileVec(result.hitVec);
			if(result.sideHit.getAxisDirection() == AxisDirection.POSITIVE)
				first.subVec(new LittleTileVec(result.sideHit));
			return false;
		}

		@Override
		public void deselect(EntityPlayer player, NBTTagCompound nbt) {
			first = null;
		}

		@Override
		public List<LittleTileBox> getBoxes(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result) {
			LittleTileVec vec = new LittleTileVec(result.hitVec);
			if(result.sideHit.getAxisDirection() == AxisDirection.POSITIVE)
				vec.subVec(new LittleTileVec(result.sideHit));
			List<LittleTileBox> boxes = getBoxes(player, nbt, first, vec, false);
			first = null;
			return boxes;
		}

		@Override
		public void addExtraInformation(EntityPlayer player, NBTTagCompound nbt, List<String> list) {
			shape.addExtraInformation(nbt, list);
		}

		@Override
		public List<GuiControl> getCustomSettings(NBTTagCompound nbt) {
			return shape.getCustomSettings(nbt);
		}

		@Override
		public void saveCustomSettings(GuiParent gui, NBTTagCompound nbt) {
			shape.saveCustomSettings(gui, nbt);
		}
		
		
		
	}
	
}
