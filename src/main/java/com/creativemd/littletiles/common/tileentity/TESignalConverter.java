package com.creativemd.littletiles.common.tileentity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.tileentity.TileEntityCreative;
import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.littletiles.common.block.BlockSignalConverter;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.signal.component.ISignalStructureBase;
import com.creativemd.littletiles.common.structure.signal.component.ISignalStructureComponent;
import com.creativemd.littletiles.common.structure.signal.component.SignalComponentType;
import com.creativemd.littletiles.common.structure.signal.network.SignalNetwork;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class TESignalConverter extends TileEntityCreative implements ISignalStructureComponent {
    
    private SignalNetwork network;
    private boolean[] inputSignalState = new boolean[4];
    private boolean[] inputRedstoneState = new boolean[4];
    private int redstoneState;
    
    private List<SignalConnection> connections = new ArrayList<>();
    
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
        return state.canProvidePower() && side != null;
    }
    
    @Override
    public World getStructureWorld() {
        return getWorld();
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        return super.writeToNBT(compound);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
    }
    
    @Override
    public void handleUpdate(NBTTagCompound nbt, boolean chunkUpdate) {
        readFromNBT(nbt);
    }
    
    @Override
    public int getBandwidth() {
        return 4;
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
    public Iterator<ISignalStructureBase> connections() {
        return new Iterator<ISignalStructureBase>() {
            
            Iterator<SignalConnection> iterator = connections.iterator();
            
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }
            
            @Override
            public ISignalStructureBase next() {
                return iterator.next().base;
            }
        };
    }
    
    @Override
    public boolean canConnect(EnumFacing facing) {
        return true;
    }
    
    public int indexOf(ISignalStructureBase base) {
        for (int i = 0; i < connections.size(); i++)
            if (connections.get(i).base == base)
                return i;
        return -1;
    }
    
    @Override
    public boolean connect(EnumFacing facing, ISignalStructureBase base, LittleGridContext context, int distance, boolean oneSidedRenderer) {
        int index = indexOf(base);
        if (index == -1)
            connections.add(new SignalConnection(facing, base));
        return true;
    }
    
    @Override
    public void disconnect(EnumFacing facing, ISignalStructureBase base) {
        int index = indexOf(base);
        if (index != -1)
            connections.remove(index);
    }
    
    @Override
    public SignalComponentType getType() {
        return SignalComponentType.IOSPECIAL;
    }
    
    @Override
    public void updateState(boolean[] state) {
        if (!BooleanUtils.equals(state, inputSignalState)) {
            BooleanUtils.set(inputSignalState, state);
            changed();
        }
    }
    
    @Override
    public void changed() {
        IBlockState state = world.getBlockState(pos);
        EnumFacing facing = state.getValue(BlockSignalConverter.FACING);
        world.neighborChanged(pos.offset(facing), state.getBlock(), pos);
    }
    
    public int getPower() {
        boolean[] toReturn = new boolean[4];
        BooleanUtils.or(toReturn, inputRedstoneState);
        BooleanUtils.or(toReturn, inputSignalState);
        return BooleanUtils.toNumber(toReturn);
    }
    
    @Override
    public boolean[] getState() {
        return inputRedstoneState;
    }
    
    @Override
    public LittleStructure getStructure() {
        return null;
    }
    
    @Override
    public int getId() {
        return 0;
    }
    
    @Override
    public int getColor() {
        return -1;
    }
    
    public void setPower(int level) {
        boolean[] newLevel = BooleanUtils.toBits(level, 4);
        if (!BooleanUtils.equals(inputRedstoneState, newLevel)) {
            BooleanUtils.set(inputRedstoneState, newLevel);
            changed();
            findNetwork().update();
        }
    }
    
    @Override
    public void unload(EnumFacing facing, ISignalStructureBase base) {
        int index = indexOf(base);
        if (index != -1)
            connections.remove(index);
    }
    
    public class SignalConnection {
        
        public final EnumFacing facing;
        public final ISignalStructureBase base;
        
        public SignalConnection(EnumFacing facing, ISignalStructureBase base) {
            this.facing = facing;
            this.base = base;
        }
        
    }
    
}
