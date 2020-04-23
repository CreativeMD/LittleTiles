package com.creativemd.littletiles.common.util.place;

import java.util.List;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.client.render.tile.LittleRenderingCube;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.tile.math.vec.LittleAbsoluteVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVecContext;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PlacementPosition extends LittleAbsoluteVec {
	
	public EnumFacing facing;
	
	@SideOnly(Side.CLIENT)
	public List<LittleRenderingCube> positingCubes;
	
	public PlacementPosition(BlockPos pos, LittleVecContext vec, EnumFacing facing) {
		super(pos, vec);
		this.facing = facing;
	}
	
	public PlacementPosition(BlockPos pos, LittleGridContext context, LittleVec vec, EnumFacing facing) {
		super(pos, context, vec);
		this.facing = facing;
	}
	
	public static PlacementPosition readFromBytes(ByteBuf buf) {
		return new PlacementPosition(LittleAction.readPos(buf), LittleAction.readLittleVecContext(buf), CreativeCorePacket.readFacing(buf));
	}
	
	public void assign(LittleAbsoluteVec pos) {
		this.pos = pos.getPos();
		this.contextVec = pos.getVecContext();
	}
	
	public AxisAlignedBB getBox() {
		double x = getPosX();
		double y = getPosY();
		double z = getPosZ();
		return new AxisAlignedBB(x, y, z, x + getContext().pixelSize, y + getContext().pixelSize, z + getContext().pixelSize);
	}
	
	public void subVec(LittleVec vec) {
		getVec().add(vec);
		removeInternalBlockOffset();
	}
	
	public void addVec(LittleVec vec) {
		getVec().sub(vec);
		removeInternalBlockOffset();
	}
	
	public void writeToBytes(ByteBuf buf) {
		LittleAction.writePos(buf, pos);
		LittleAction.writeLittleVecContext(contextVec, buf);
		CreativeCorePacket.writeFacing(buf, facing);
	}
	
	@Override
	public PlacementPosition copy() {
		return new PlacementPosition(pos, contextVec.copy(), facing);
	}
}
