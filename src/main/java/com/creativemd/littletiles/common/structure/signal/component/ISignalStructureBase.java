package com.creativemd.littletiles.common.structure.signal.component;

import java.util.Iterator;

import com.creativemd.littletiles.common.structure.signal.network.SignalNetwork;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public interface ISignalStructureBase {
    
    public default boolean compatible(ISignalStructureBase other) {
        if (other.getType() != SignalComponentType.TRANSMITTER && other.getType() == getType())
            return false;
        return getBandwidth() == other.getBandwidth();
    }
    
    public World getStructureWorld();
    
    public int getBandwidth();
    
    public SignalNetwork getNetwork();
    
    public void setNetwork(SignalNetwork network);
    
    public Iterator<ISignalStructureBase> connections();
    
    public boolean canConnect(EnumFacing facing);
    
    public boolean connect(EnumFacing facing, ISignalStructureBase base, LittleGridContext context, int distance, boolean oneSidedRenderer);
    
    public void disconnect(EnumFacing facing, ISignalStructureBase base);
    
    public void unload(EnumFacing facing, ISignalStructureBase base);
    
    public default boolean hasNetwork() {
        return getNetwork() != null;
    }
    
    public default SignalNetwork findNetwork() {
        if (hasNetwork())
            if (getNetwork().requiresResearch())
                getNetwork().deleteNetwork();
            else
                return getNetwork();
            
        Iterator<ISignalStructureBase> connections = connections();
        while (connections.hasNext()) {
            ISignalStructureBase connection = connections.next();
            if (connection.hasNetwork()) {
                connection.getNetwork().add(this);
                return getNetwork();
            }
        }
        
        SignalNetwork network = new SignalNetwork(getBandwidth());
        network.add(this);
        return getNetwork();
    }
    
    public SignalComponentType getType();
    
    public int getColor();
}
