package com.creativemd.littletiles.common.structure.signal.component;

import java.util.Iterator;

import com.creativemd.littletiles.common.structure.signal.network.SignalNetwork;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class InvalidSignalComponent implements ISignalStructureBase {
    
    public static final InvalidSignalComponent INSTANCE = new InvalidSignalComponent();
    
    private InvalidSignalComponent() {}
    
    @Override
    public World getStructureWorld() {
        return null;
    }
    
    @Override
    public int getBandwidth() {
        return 0;
    }
    
    @Override
    public SignalNetwork getNetwork() {
        return null;
    }
    
    @Override
    public void setNetwork(SignalNetwork network) {}
    
    @Override
    public Iterator<ISignalStructureBase> connections() {
        return null;
    }
    
    @Override
    public boolean canConnect(EnumFacing facing) {
        return false;
    }
    
    @Override
    public boolean connect(EnumFacing facing, ISignalStructureBase base, LittleGridContext context, int distance, boolean oneSidedRenderer) {
        return false;
    }
    
    @Override
    public void disconnect(EnumFacing facing, ISignalStructureBase base) {}
    
    @Override
    public SignalComponentType getType() {
        return SignalComponentType.INVALID;
    }
    
    @Override
    public int getColor() {
        return 0;
    }
    
    @Override
    public void unload(EnumFacing facing, ISignalStructureBase base) {}
    
}
