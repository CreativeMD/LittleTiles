package com.creativemd.littletiles.common.util.place;

import java.util.List;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.client.render.tile.LittleRenderBox;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.tile.math.vec.LittleAbsoluteVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVecContext;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PlacementPosition extends LittleAbsoluteVec {
    
    public EnumFacing facing;
    
    @SideOnly(Side.CLIENT)
    public List<LittleRenderBox> positingCubes;
    
    public PlacementPosition(BlockPos pos, LittleVecContext vec, EnumFacing facing) {
        super(pos, vec);
        this.facing = facing;
    }
    
    public PlacementPosition(BlockPos pos, LittleGridContext context, LittleVec vec, EnumFacing facing) {
        super(pos, context, vec);
        this.facing = facing;
    }
    
    public PlacementPosition(RayTraceResult result, LittleGridContext context) {
        super(result, context);
        this.facing = result.sideHit;
    }
    
    public static PlacementPosition readFromBytes(ByteBuf buf) {
        return new PlacementPosition(CreativeCorePacket.readPos(buf), LittleAction.readLittleVecContext(buf), CreativeCorePacket.readFacing(buf));
    }
    
    public void assign(LittleAbsoluteVec pos) {
        this.pos = pos.getPos();
        this.contextVec = pos.getVecContext();
    }
    
    public AxisAlignedBB getBox(LittleGridContext context) {
        double x = getPosX();
        double y = getPosY();
        double z = getPosZ();
        return new AxisAlignedBB(x, y, z, x + context.pixelSize, y + context.pixelSize, z + context.pixelSize);
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
        CreativeCorePacket.writePos(buf, pos);
        LittleAction.writeLittleVecContext(contextVec, buf);
        CreativeCorePacket.writeFacing(buf, facing);
    }
    
    @Override
    public PlacementPosition copy() {
        return new PlacementPosition(pos, contextVec.copy(), facing);
    }
}
