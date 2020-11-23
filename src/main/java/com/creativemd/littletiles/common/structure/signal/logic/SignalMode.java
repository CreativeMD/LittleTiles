package com.creativemd.littletiles.common.structure.signal.logic;

import java.util.List;

import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.signal.output.SignalOutputCondition;
import com.creativemd.littletiles.common.structure.signal.schedule.ISignalScheduleTicket;
import com.creativemd.littletiles.common.structure.signal.schedule.SignalTicker;

public enum SignalMode {
	
	EQUAL("signal.mode.equal", "=") {
		@Override
		public SignalOutputCondition create(LittleStructure structure, List<Integer> numbers, SignalTarget target) {
			SignalOutputCondition condition = new SignalOutputCondition(numbers.get(0), target) {
				
				@Override
				public SignalMode getMode() {
					return SignalMode.EQUAL;
				}
				
				@Override
				public void schedule(LittleStructure structure, boolean[] state) {
					SignalTicker.schedule(structure, this, state, delay);
				}
				
				@Override
				public int[] getExtraData() {
					List<ISignalScheduleTicket> tickets = SignalTicker.get(structure).findTickets(this);
					int[] extraData = new int[1 + tickets.size() * 2];
					extraData[0] = delay;
					for (int i = 0; i < tickets.size(); i++) {
						ISignalScheduleTicket ticket = tickets.get(i);
						extraData[1 + i * 2] = ticket.getDelay();
						extraData[2 + i * 2] = BooleanUtils.boolToInt(ticket.getState());
					}
					return extraData;
				}
				
			};
			if (structure != null)
				for (int i = 1; i < numbers.size(); i += 2) {
					if (i + 1 < numbers.size()) {
						int delay = numbers.get(i);
						boolean[] state = new boolean[target.getBandwidth(structure)];
						BooleanUtils.intToBool(numbers.get(i + 1), state);
						SignalTicker.schedule(structure, condition, state, delay);
					}
				}
			return condition;
		}
	},
	TOGGLE("signal.mode.toggle", "|=") {
		
		@Override
		public SignalOutputCondition create(LittleStructure structure, List<Integer> numbers, SignalTarget target) {
			int bandwidth = target.getBandwidth(structure);
			boolean[] before = new boolean[bandwidth];
			boolean[] result = new boolean[bandwidth];
			if (numbers.size() > 1)
				BooleanUtils.intToBool(numbers.get(1), before);
			if (numbers.size() > 2)
				BooleanUtils.intToBool(numbers.get(2), result);
			
			SignalOutputCondition condition = new SignalOutputCondition(numbers.get(0), target) {
				
				public boolean[] stateBefore = before;
				public boolean[] result;
				
				@Override
				public SignalMode getMode() {
					return SignalMode.TOGGLE;
				}
				
				@Override
				public void schedule(LittleStructure structure, boolean[] state) {
					boolean toggled = false;
					for (int i = 0; i < state.length; i++) {
						if (!stateBefore[i] && state[i]) {
							//Toggle
							result[i] = !result[i];
							toggled = true;
						}
						stateBefore[i] = state[i];
					}
					SignalTicker.schedule(structure, this, result, delay);
				}
				
				@Override
				public int[] getExtraData() {
					List<ISignalScheduleTicket> tickets = SignalTicker.get(structure).findTickets(this);
					int[] extraData = new int[3 + tickets.size() * 2];
					extraData[0] = delay;
					extraData[1] = BooleanUtils.boolToInt(stateBefore);
					extraData[2] = BooleanUtils.boolToInt(result);
					for (int i = 0; i < tickets.size(); i++) {
						ISignalScheduleTicket ticket = tickets.get(i);
						extraData[3 + i * 2] = ticket.getDelay();
						extraData[4 + i * 2] = BooleanUtils.boolToInt(ticket.getState());
					}
					return extraData;
				}
				
			};
			if (structure != null)
				for (int i = 1; i < numbers.size(); i += 2) {
					if (i + 1 < numbers.size()) {
						int delay = numbers.get(i);
						boolean[] state = new boolean[target.getBandwidth(structure)];
						BooleanUtils.intToBool(numbers.get(i + 1), state);
						SignalTicker.schedule(structure, condition, state, delay);
					}
				}
			return condition;
		}
	},
	PULSE("signal.mode.pulse", "~=") {
		
		@Override
		public SignalOutputCondition create(LittleStructure structure, List<Integer> numbers, SignalTarget target) {
			int bandwidth = target.getBandwidth(structure);
			int pulseLength = 10;
			if (numbers.size() > 1)
				pulseLength = numbers.get(1);
			boolean before = false;
			if (numbers.size() > 2)
				before = numbers.get(2) == 1;
			
			SignalOutputCondition condition = new SignalOutputConditionPulse(numbers.get(0), target, pulseLength, before);
			if (structure != null)
				if (numbers.size() == 4) {
					SignalTicker.schedule(structure, condition, BooleanUtils.asArray(false), numbers.get(3));
				} else if (numbers.size() == 5) {
					SignalTicker.schedule(structure, condition, BooleanUtils.asArray(true), numbers.get(3));
					SignalTicker.schedule(structure, condition, BooleanUtils.asArray(false), numbers.get(4));
				}
			return condition;
		}
	},
	THRESHOLD("signal.mode.threshold", "==") {
		
		@Override
		public SignalOutputCondition create(LittleStructure structure, List<Integer> numbers, SignalTarget target) {
			SignalOutputConditionStoreOne condition = new SignalOutputConditionStoreOne(numbers.get(0), target) {
				
				@Override
				public void schedule(LittleStructure structure, boolean[] state) {
					if (ticket != null)
						ticket.overwriteState(state);
					else
						ticket = SignalTicker.schedule(structure, this, state, delay);
				}
				
				@Override
				public void performStateChange(LittleStructure structure, boolean[] state) {
					ticket = null;
					super.performStateChange(structure, state);
				}
				
				@Override
				public SignalMode getMode() {
					return SignalMode.THRESHOLD;
				}
				
				@Override
				public int[] getExtraData() {
					if (ticket != null)
						return new int[] { delay, ticket.getDelay(), BooleanUtils.boolToInt(ticket.getState()) };
					return new int[] { delay };
				}
			};
			
			if (structure != null)
				if (numbers.size() >= 3) {
					int delay = numbers.get(1);
					boolean[] state = new boolean[target.getBandwidth(structure)];
					BooleanUtils.intToBool(numbers.get(2), state);
					condition.ticket = SignalTicker.schedule(structure, condition, state, delay);
				}
			return condition;
		}
	},
	STABILIZER("signal.mode.stabilizer", "~~") {
		
		@Override
		public SignalOutputCondition create(LittleStructure structure, List<Integer> numbers, SignalTarget target) {
			SignalOutputConditionStoreOne condition = new SignalOutputConditionStoreOne(numbers.get(0), target) {
				
				@Override
				public void schedule(LittleStructure structure, boolean[] state) {
					if (ticket != null)
						ticket.markObsolete();
					ticket = SignalTicker.schedule(structure, this, state, delay);
				}
				
				@Override
				public void performStateChange(LittleStructure structure, boolean[] state) {
					ticket = null;
					super.performStateChange(structure, state);
				}
				
				@Override
				public SignalMode getMode() {
					return SignalMode.THRESHOLD;
				}
				
				@Override
				public int[] getExtraData() {
					if (ticket != null)
						return new int[] { delay, ticket.getDelay(), BooleanUtils.boolToInt(ticket.getState()) };
					return new int[] { delay };
				}
			};
			
			if (structure != null)
				if (numbers.size() >= 3) {
					int delay = numbers.get(1);
					boolean[] state = new boolean[target.getBandwidth(structure)];
					BooleanUtils.intToBool(numbers.get(2), state);
					condition.ticket = SignalTicker.schedule(structure, condition, state, delay);
				}
			return condition;
		}
	};
	
	public final String translateKey;
	public final String splitter;
	
	private SignalMode(String translateKey, String splitter) {
		this.translateKey = translateKey;
		this.splitter = splitter;
	}
	
	public abstract SignalOutputCondition create(LittleStructure structure, List<Integer> numbers, SignalTarget target);
	
	public static abstract class SignalOutputConditionStoreOne extends SignalOutputCondition {
		
		ISignalScheduleTicket ticket;
		
		public SignalOutputConditionStoreOne(int delay, SignalTarget target) {
			super(delay, target);
		}
		
	}
	
	public static class SignalOutputConditionPulse extends SignalOutputCondition {
		
		public final int pulseLength;
		public boolean stateBefore;
		public ISignalScheduleTicket pulseStart;
		public ISignalScheduleTicket pulseEnd;
		
		public SignalOutputConditionPulse(int delay, SignalTarget target, int length, boolean stateBefore) {
			super(delay, target);
			this.pulseLength = length;
			this.stateBefore = stateBefore;
		}
		
		@Override
		public int getBandwidth(LittleStructure structure) {
			return 1;
		}
		
		@Override
		public SignalMode getMode() {
			return SignalMode.PULSE;
		}
		
		@Override
		public void performStateChange(LittleStructure structure, boolean[] state) {
			super.performStateChange(structure, state);
			if (BooleanUtils.any(state))
				pulseStart = null;
			else {
				pulseStart = null;
				pulseEnd = null;
			}
		}
		
		@Override
		public void schedule(LittleStructure structure, boolean[] state) {
			if (pulseEnd != null)
				return;
			boolean current = BooleanUtils.any(state);
			if (!stateBefore && current) {
				pulseStart = SignalTicker.schedule(structure, this, BooleanUtils.asArray(true), delay);
				pulseEnd = SignalTicker.schedule(structure, this, BooleanUtils.asArray(false), delay + pulseLength);
			}
			stateBefore = current;
		}
		
		@Override
		public int[] getExtraData() {
			int[] extraData = new int[3 + (pulseStart != null ? 2 : (pulseEnd != null ? 1 : 0))];
			extraData[0] = delay;
			extraData[1] = pulseLength;
			extraData[2] = stateBefore ? 1 : 0;
			if (pulseStart != null) {
				extraData[3] = pulseStart.getDelay();
				extraData[4] = pulseEnd.getDelay();
			} else if (pulseEnd != null) {
				extraData[3] = pulseEnd.getDelay();
			}
			
			return extraData;
		}
	}
	
}
