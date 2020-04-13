package com.creativemd.littletiles.common.structure.signal;

import java.util.Iterator;

import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.util.EnumFacing;

public interface ISignalBase {
	
	public default boolean compatible(ISignalBase other) {
		return getBandwidth() == other.getBandwidth();
	}
	
	public int getBandwidth();
	
	public SignalNetwork getNetwork();
	
	public void setNetwork(SignalNetwork network);
	
	public Iterator<ISignalBase> connections();
	
	public boolean canConnect(EnumFacing facing);
	
	public void connect(EnumFacing facing, ISignalBase base, LittleGridContext context, int distance);
	
	public void disconnect(EnumFacing facing, ISignalBase base);
	
	public SignalType getType();
	
	public default boolean hasNetwork() {
		return getNetwork() != null;
	}
	
	public default SignalNetwork findNetwork() {
		if (hasNetwork())
			return getNetwork();
		
		Iterator<ISignalBase> connections = connections();
		while (connections.hasNext()) {
			ISignalBase connection = connections.next();
			if (connection.hasNetwork()) {
				connection.getNetwork().add(this);
				return getNetwork();
			}
		}
		
		SignalNetwork network = new SignalNetwork(getBandwidth());
		network.add(this);
		return getNetwork();
	}
	
}
