package com.creativemd.littletiles.common.utils.shape;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.gui.GuiControl;
import com.creativemd.creativecore.gui.container.GuiParent;
import com.creativemd.littletiles.common.tiles.vec.LittleBoxes;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DragShapeLine extends DragShape {

	public DragShapeLine() {
		super("line");
	}

	public void visitAll(double gx0, double gy0, double gz0, double gx1, double gy1, double gz1, List<LittleTileBox> boxes) {

		int gx0idx = (int) Math.floor(gx0);
		int gy0idx = (int) Math.floor(gy0);
		int gz0idx = (int) Math.floor(gz0);

		int gx1idx = (int) Math.floor(gx1);
		int gy1idx = (int) Math.floor(gy1);
		int gz1idx = (int) Math.floor(gz1);

		int sx = gx1idx > gx0idx ? 1 : gx1idx < gx0idx ? -1 : 0;
		int sy = gy1idx > gy0idx ? 1 : gy1idx < gy0idx ? -1 : 0;
		int sz = gz1idx > gz0idx ? 1 : gz1idx < gz0idx ? -1 : 0;

		int gx = gx0idx;
		int gy = gy0idx;
		int gz = gz0idx;

		// Planes for each axis that we will next cross
		int gxp = gx0idx + (gx1idx > gx0idx ? 1 : 0);
		int gyp = gy0idx + (gy1idx > gy0idx ? 1 : 0);
		int gzp = gz0idx + (gz1idx > gz0idx ? 1 : 0);

		// Only used for multiplying up the error margins
		double vx = gx1 == gx0 ? 1 : gx1 - gx0;
		double vy = gy1 == gy0 ? 1 : gy1 - gy0;
		double vz = gz1 == gz0 ? 1 : gz1 - gz0;

		// Error is normalized to vx * vy * vz so we only have to multiply up
		double vxvy = vx * vy;
		double vxvz = vx * vz;
		double vyvz = vy * vz;

		// Error from the next plane accumulators, scaled up by vx*vy*vz
		// gx0 + vx * rx === gxp
		// vx * rx === gxp - gx0
		// rx === (gxp - gx0) / vx
		double errx = (gxp - gx0) * vyvz;
		double erry = (gyp - gy0) * vxvz;
		double errz = (gzp - gz0) * vxvy;

		double derrx = sx * vyvz;
		double derry = sy * vxvz;
		double derrz = sz * vxvy;

		do {
			// visitor(gx, gy, gz);
			boxes.add(new LittleTileBox(gx, gy, gz, gx + 1, gy + 1, gz + 1));

			if (gx == gx1idx && gy == gy1idx && gz == gz1idx)
				break;

			// Which plane do we cross first?
			double xr = Math.abs(errx);
			double yr = Math.abs(erry);
			double zr = Math.abs(errz);

			if (sx != 0 && (sy == 0 || xr < yr) && (sz == 0 || xr < zr)) {
				gx += sx;
				errx += derrx;
			} else if (sy != 0 && (sz == 0 || yr < zr)) {
				gy += sy;
				erry += derry;
			} else if (sz != 0) {
				gz += sz;
				errz += derrz;
			}

		} while (true);
	}

	@Override
	public LittleBoxes getBoxes(LittleBoxes boxes, LittleTileVec min, LittleTileVec max, EntityPlayer player, NBTTagCompound nbt, boolean preview, LittleTilePos originalMin, LittleTilePos originalMax) {
		LittleTilePos absolute = new LittleTilePos(boxes.pos, boxes.context);

		LittleTileVec originalMinVec = originalMin.getRelative(absolute).getVec(boxes.context);
		LittleTileVec originalMaxVec = originalMax.getRelative(absolute).getVec(boxes.context);

		visitAll(originalMinVec.x + 0.5, originalMinVec.y + 0.5, originalMinVec.z + 0.5, originalMaxVec.x + 0.5, originalMaxVec.y + 0.5, originalMaxVec.z + 0.5, boxes);

		LittleTileBox.combineBoxesBlocks(boxes);

		return boxes;
	}

	@Override
	public void addExtraInformation(NBTTagCompound nbt, List<String> list) {

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
	public void rotate(NBTTagCompound nbt, Rotation rotation) {

	}

	@Override
	public void flip(NBTTagCompound nbt, Axis axis) {

	}

}
