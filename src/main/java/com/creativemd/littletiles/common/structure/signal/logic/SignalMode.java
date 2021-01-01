package com.creativemd.littletiles.common.structure.signal.logic;

import java.util.List;

import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.signal.component.ISignalComponent;
import com.creativemd.littletiles.common.structure.signal.output.SignalOutputHandler;
import com.creativemd.littletiles.common.structure.signal.schedule.ISignalScheduleTicket;
import com.creativemd.littletiles.common.structure.signal.schedule.SignalTicker;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public enum SignalMode {
	
	EQUAL("signal.mode.equal", "=") {
		@Override
		public SignalOutputHandler create(ISignalComponent component, int delay, NBTTagCompound nbt) {
			SignalOutputHandler handler = new SignalOutputHandler(component, delay, nbt) {
				
				@Override
				public SignalMode getMode() {
					return SignalMode.EQUAL;
				}
				
				@Override
				public void schedule(boolean[] state) {
					SignalTicker.schedule(this, state, delay);
				}
				
				@Override
				public void write(NBTTagCompound nbt) {
					List<ISignalScheduleTicket> tickets = SignalTicker.get(component).findTickets(this);
					NBTTagList list = new NBTTagList();
					for (int i = 0; i < tickets.size(); i++) {
						ISignalScheduleTicket ticket = tickets.get(i);
						list.appendTag(new NBTTagIntArray(new int[] { ticket.getDelay(), BooleanUtils.boolToInt(ticket.getState()) }));
					}
					nbt.setTag("tickets", list);
				}
				
			};
			NBTTagList list = nbt.getTagList("tickets", 11);
			for (int i = 0; i < list.tagCount(); i++) {
				int[] array = list.getIntArrayAt(i);
				if (array.length == 2) {
					boolean[] state = new boolean[component.getBandwidth()];
					BooleanUtils.intToBool(array[1], state);
					SignalTicker.schedule(handler, state, array[0]);
				}
			}
			return handler;
		}
	},
	TOGGLE("signal.mode.toggle", "|=") {
		
		@Override
		public SignalOutputHandler create(ISignalComponent component, int delay, NBTTagCompound nbt) {
			int bandwidth = component.getBandwidth();
			boolean[] before = new boolean[bandwidth];
			boolean[] result = new boolean[bandwidth];
			BooleanUtils.intToBool(nbt.getInteger("before"), before);
			BooleanUtils.intToBool(nbt.getInteger("result"), result);
			
			SignalOutputHandler handler = new SignalOutputHandler(component, delay, nbt) {
				
				public boolean[] stateBefore = before;
				public boolean[] result;
				
				@Override
				public SignalMode getMode() {
					return SignalMode.TOGGLE;
				}
				
				@Override
				public void schedule(boolean[] state) {
					boolean toggled = false;
					for (int i = 0; i < state.length; i++) {
						if (!stateBefore[i] && state[i]) {
							//Toggle
							result[i] = !result[i];
							toggled = true;
						}
						stateBefore[i] = state[i];
					}
					SignalTicker.schedule(this, result, delay);
				}
				
				@Override
				public void write(NBTTagCompound nbt) {
					nbt.setInteger("before", BooleanUtils.boolToInt(stateBefore));
					nbt.setInteger("result", BooleanUtils.boolToInt(result));
					List<ISignalScheduleTicket> tickets = SignalTicker.get(component).findTickets(this);
					NBTTagList list = new NBTTagList();
					for (int i = 0; i < tickets.size(); i++) {
						ISignalScheduleTicket ticket = tickets.get(i);
						list.appendTag(new NBTTagIntArray(new int[] { ticket.getDelay(), BooleanUtils.boolToInt(ticket.getState()) }));
					}
					nbt.setTag("tickets", list);
				}
				
			};
			NBTTagList list = nbt.getTagList("tickets", 11);
			for (int i = 0; i < list.tagCount(); i++) {
				int[] array = list.getIntArrayAt(i);
				if (array.length == 2) {
					boolean[] state = new boolean[component.getBandwidth()];
					BooleanUtils.intToBool(array[1], state);
					SignalTicker.schedule(handler, state, array[0]);
				}
			}
			return handler;
		}
	},
	PULSE("signal.mode.pulse", "~=") {
		
		@Override
		public SignalOutputHandler create(ISignalComponent component, int delay, NBTTagCompound nbt) {
			SignalOutputHandler condition = new SignalOutputHandlerPulse(component, delay, nbt);
			if (nbt.hasKey("start")) {
				SignalTicker.schedule(condition, BooleanUtils.asArray(true), nbt.getInteger("start"));
				SignalTicker.schedule(condition, BooleanUtils.asArray(false), nbt.getInteger("end"));
			} else if (nbt.hasKey("end"))
				SignalTicker.schedule(condition, BooleanUtils.asArray(false), nbt.getInteger("end"));
			return condition;
		}
	},
	THRESHOLD("signal.mode.threshold", "==") {
		
		@Override
		public SignalOutputHandler create(ISignalComponent component, int delay, NBTTagCompound nbt) {
			SignalOutputHandlerStoreOne handler = new SignalOutputHandlerStoreOne(component, delay, nbt) {
				
				@Override
				public void schedule(boolean[] state) {
					if (ticket != null)
						ticket.overwriteState(state);
					else
						ticket = SignalTicker.schedule(this, state, delay);
				}
				
				@Override
				public void performStateChange(boolean[] state) {
					ticket = null;
					super.performStateChange(state);
				}
				
				@Override
				public SignalMode getMode() {
					return SignalMode.THRESHOLD;
				}
				
				@Override
				public void write(NBTTagCompound nbt) {
					if (ticket != null)
						nbt.setIntArray("ticket", new int[] { ticket.getDelay(), BooleanUtils.boolToInt(ticket.getState()) });
				}
			};
			
			if (nbt.hasKey("ticket")) {
				int[] array = nbt.getIntArray("ticket");
				if (array.length == 2) {
					boolean[] state = new boolean[component.getBandwidth()];
					BooleanUtils.intToBool(array[1], state);
					handler.ticket = SignalTicker.schedule(handler, state, array[0]);
				}
			}
			return handler;
		}
	},
	STABILIZER("signal.mode.stabilizer", "~~") {
		
		@Override
		public SignalOutputHandler create(ISignalComponent component, int delay, NBTTagCompound nbt) {
			SignalOutputHandlerStoreOne handler = new SignalOutputHandlerStoreOne(component, delay, nbt) {
				
				@Override
				public void schedule(boolean[] state) {
					if (ticket != null)
						ticket.markObsolete();
					ticket = SignalTicker.schedule(this, state, delay);
				}
				
				@Override
				public void performStateChange(boolean[] state) {
					ticket = null;
					super.performStateChange(state);
				}
				
				@Override
				public SignalMode getMode() {
					return SignalMode.THRESHOLD;
				}
				
				@Override
				public void write(NBTTagCompound nbt) {
					if (ticket != null)
						nbt.setIntArray("ticket", new int[] { ticket.getDelay(), BooleanUtils.boolToInt(ticket.getState()) });
				}
			};
			
			if (nbt.hasKey("ticket")) {
				int[] array = nbt.getIntArray("ticket");
				if (array.length == 2) {
					boolean[] state = new boolean[component.getBandwidth()];
					BooleanUtils.intToBool(array[1], state);
					handler.ticket = SignalTicker.schedule(handler, state, array[0]);
				}
			}
			return handler;
		}
	};
	
	public final String translateKey;
	public final String splitter;
	
	private SignalMode(String translateKey, String splitter) {
		this.translateKey = translateKey;
		this.splitter = splitter;
	}
	
	public abstract SignalOutputHandler create(ISignalComponent component, int delay, NBTTagCompound nbt);
	
	public static abstract class SignalOutputHandlerStoreOne extends SignalOutputHandler {
		
		ISignalScheduleTicket ticket;
		
		public SignalOutputHandlerStoreOne(ISignalComponent component, int delay, NBTTagCompound nbt) {
			super(component, delay, nbt);
		}
		
	}
	
	public static class SignalOutputHandlerPulse extends SignalOutputHandler {
		
		public final int pulseLength;
		public boolean stateBefore;
		public ISignalScheduleTicket pulseStart;
		public ISignalScheduleTicket pulseEnd;
		
		public SignalOutputHandlerPulse(ISignalComponent component, int delay, NBTTagCompound nbt) {
			super(component, delay, nbt);
			this.pulseLength = nbt.hasKey("length") ? nbt.getInteger("length") : 10;
			this.stateBefore = nbt.getBoolean("before");
		}
		
		@Override
		public int getBandwidth() {
			return 1;
		}
		
		@Override
		public SignalMode getMode() {
			return SignalMode.PULSE;
		}
		
		@Override
		public void performStateChange(boolean[] state) {
			super.performStateChange(state);
			if (BooleanUtils.any(state))
				pulseStart = null;
			else {
				pulseStart = null;
				pulseEnd = null;
			}
		}
		
		@Override
		public void schedule(boolean[] state) {
			if (pulseEnd != null)
				return;
			boolean current = BooleanUtils.any(state);
			if (!stateBefore && current) {
				pulseStart = SignalTicker.schedule(this, BooleanUtils.asArray(true), delay);
				pulseEnd = SignalTicker.schedule(this, BooleanUtils.asArray(false), delay + pulseLength);
			}
			stateBefore = current;
		}
		
		@Override
		public void write(NBTTagCompound nbt) {
			nbt.setInteger("length", pulseLength);
			nbt.setBoolean("before", stateBefore);
			if (pulseStart != null)
				nbt.setInteger("start", pulseStart.getDelay());
			if (pulseEnd != null)
				nbt.setInteger("end", pulseEnd.getDelay());
		}
	}
	
	public static GuiSignalModeConfiguration getConfigDefault() {
		
	}
	
	public static GuiSignalModeConfiguration getConfig(NBTTagCompound nbt) {
		
	}
	
	@SideOnly(Side.CLIENT)
	public static abstract class GuiSignalModeConfiguration {
		
		public abstract GuiSignalModeConfiguration copy();
		
		public abstract SignalOutputHandler getHandler(LittleStructure structure);
		
	}
}
