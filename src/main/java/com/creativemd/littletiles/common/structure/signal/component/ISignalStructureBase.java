package com.creativemd.littletiles.common.structure.signal.component;

import java.util.Iterator;

import com.creativemd.littletiles.common.structure.signal.network.SignalNetwork;

import net.minecraft.world.level.Level;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.common.grid.LittleGrid;

public interface ISignalStructureBase {
    
    public default boolean compatible(ISignalStructureBase other) {
        if (other.getType() != SignalComponentType.TRANSMITTER && other.getType() == getType())
            return false;
        return getBandwidth() == other.getBandwidth();
    }
    
    public Level getStructureLevel();
    
    public int getBandwidth();
    
    public SignalNetwork getNetwork();
    
    public void setNetwork(SignalNetwork network);
    
    public Iterator<ISignalStructureBase> connections();
    
    public boolean canConnect(Facing facing);
    
    public boolean connect(Facing facing, ISignalStructureBase base, LittleGrid context, int distance, boolean oneSidedRenderer);
    
    public void disconnect(Facing facing, ISignalStructureBase base);
    
    public default boolean hasNetwork() {
        return getNetwork() != null;
    }
    
    public default SignalNetwork findNetwork() {
        if (hasNetwork())
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
    
    public SignalComponentType getComponentType();
    
    public int getColor();
}
