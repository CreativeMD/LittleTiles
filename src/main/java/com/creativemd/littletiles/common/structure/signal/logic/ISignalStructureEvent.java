package com.creativemd.littletiles.common.structure.signal.logic;

import java.util.List;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.signal.component.ISignalComponent;
import com.creativemd.littletiles.common.structure.signal.logic.event.SignalEvent;

import net.minecraft.nbt.NBTTagList;

public interface ISignalStructureEvent {
	
	public List<SignalEvent> getSignalEvents();
	
	public void setSignalEvents(List<SignalEvent> events);
	
	public default NBTTagList writeSignalEvents() {
		NBTTagList list = new NBTTagList();
		for (SignalEvent event : getSignalEvents())
			list.appendTag(event.writeToNBT());
		return list;
	}
	
	public default void changed(ISignalComponent changed) {
		for (SignalEvent event : getSignalEvents())
			event.update((LittleStructure) this);
	}
}
