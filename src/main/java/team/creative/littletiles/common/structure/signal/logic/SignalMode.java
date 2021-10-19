package team.creative.littletiles.common.structure.signal.logic;

import java.util.Arrays;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.util.math.utils.BooleanUtils;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.component.ISignalComponent;
import team.creative.littletiles.common.structure.signal.output.SignalOutputHandler;
import team.creative.littletiles.common.structure.signal.schedule.ISignalScheduleTicket;
import team.creative.littletiles.common.structure.signal.schedule.SignalTicker;

public enum SignalMode {
    
    EQUAL("signal.mode.equal") {
        @Override
        public SignalOutputHandler create(ISignalComponent component, int delay, CompoundTag nbt, boolean hasWorld) {
            SignalOutputHandler handler = new SignalOutputHandler(component, delay, nbt) {
                
                @Override
                public SignalMode getMode() {
                    return SignalMode.EQUAL;
                }
                
                @Override
                public void queue(boolean[] state) {
                    SignalTicker.schedule(this, state, delay);
                }
                
                @Override
                public void write(boolean preview, CompoundTag nbt) {
                    if (preview)
                        return;
                    List<ISignalScheduleTicket> tickets = SignalTicker.findTickets(component, this);
                    ListTag list = new ListTag();
                    for (int i = 0; i < tickets.size(); i++) {
                        ISignalScheduleTicket ticket = tickets.get(i);
                        list.add(new IntArrayTag(new int[] { ticket.getDelay(), BooleanUtils.boolToInt(ticket.getState()) }));
                    }
                    nbt.put("tickets", list);
                }
                
            };
            if (hasWorld) {
                ListTag list = nbt.getList("tickets", 11);
                for (int i = 0; i < list.size(); i++) {
                    int[] array = list.getIntArray(i);
                    if (array.length == 2) {
                        try {
                            boolean[] state = new boolean[component.getBandwidth()];
                            BooleanUtils.intToBool(array[1], state);
                            SignalTicker.schedule(handler, state, array[0]);
                        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                        
                    }
                }
            }
            return handler;
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public GuiSignalModeConfiguration createConfiguration(CompoundTag nbt) {
            if (nbt == null)
                return new GuiSignalModeConfigurationEqual(1);
            return new GuiSignalModeConfigurationEqual(nbt);
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public void createControls(GuiParent parent, GuiSignalModeConfiguration configuration) {}
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public GuiSignalModeConfiguration parseControls(GuiParent parent, int delay) {
            return new GuiSignalModeConfigurationEqual(delay);
        }
        
    },
    TOGGLE("signal.mode.toggle") {
        
        @Override
        public SignalOutputHandler create(ISignalComponent component, int delay, CompoundTag nbt, boolean hasWorld) {
            boolean[] before;
            boolean[] result;
            int bandwidth = nbt.getInt("bandwidth");
            if (bandwidth > 0) {
                before = new boolean[bandwidth];
                result = new boolean[bandwidth];
                BooleanUtils.intToBool(nbt.getInt("before"), before);
                BooleanUtils.intToBool(nbt.getInt("result"), result);
            } else {
                before = null;
                result = null;
            }
            
            SignalOutputHandler handler = new SignalOutputHandlerToggle(component, delay, nbt, before, result);
            if (hasWorld) {
                ListTag list = nbt.getList("tickets", 11);
                for (int i = 0; i < list.size(); i++) {
                    int[] array = list.getIntArray(i);
                    if (array.length == 2) {
                        try {
                            boolean[] state = new boolean[component.getBandwidth()];
                            BooleanUtils.intToBool(array[1], state);
                            SignalTicker.schedule(handler, state, array[0]);
                        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                    }
                }
            }
            return handler;
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public GuiSignalModeConfiguration createConfiguration(CompoundTag nbt) {
            return new GuiSignalModeConfigurationToggle(nbt);
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public void createControls(GuiParent parent, GuiSignalModeConfiguration configuration) {}
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public GuiSignalModeConfiguration parseControls(GuiParent parent, int delay) {
            return new GuiSignalModeConfigurationToggle(delay);
        }
        
    },
    PULSE("signal.mode.pulse") {
        
        @Override
        public SignalOutputHandler create(ISignalComponent component, int delay, CompoundTag nbt, boolean hasWorld) {
            SignalOutputHandler condition = new SignalOutputHandlerPulse(component, delay, nbt);
            if (hasWorld) {
                if (nbt.contains("start")) {
                    SignalTicker.schedule(condition, BooleanUtils.asArray(true), nbt.getInt("start"));
                    SignalTicker.schedule(condition, BooleanUtils.asArray(false), nbt.getInt("end"));
                } else if (nbt.contains("end"))
                    SignalTicker.schedule(condition, BooleanUtils.asArray(false), nbt.getInt("end"));
            }
            return condition;
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public GuiSignalModeConfiguration createConfiguration(CompoundTag nbt) {
            return new GuiSignalModeConfigurationPulse(nbt);
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public void createControls(GuiParent parent, GuiSignalModeConfiguration configuration) {
            parent.add(new GuiLabel("length:"));
            parent.add(new GuiTextfield("length", "" + (configuration instanceof GuiSignalModeConfigurationPulse ? ((GuiSignalModeConfigurationPulse) configuration).length : 10))
                    .setNumbersOnly());
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public GuiSignalModeConfiguration parseControls(GuiParent parent, int delay) {
            GuiTextfield length = (GuiTextfield) parent.get("length");
            return new GuiSignalModeConfigurationPulse(delay, Math.max(1, length.parseInteger()));
        }
    },
    THRESHOLD("signal.mode.threshold") {
        
        @Override
        public SignalOutputHandler create(ISignalComponent component, int delay, CompoundTag nbt, boolean hasWorld) {
            SignalOutputHandlerStoreOne handler = new SignalOutputHandlerStoreOne(component, delay, nbt) {
                
                @Override
                public void queue(boolean[] state) {
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
                public void write(boolean preview, CompoundTag nbt) {
                    if (!preview && ticket != null)
                        nbt.putIntArray("ticket", new int[] { ticket.getDelay(), BooleanUtils.boolToInt(ticket.getState()) });
                }
            };
            
            if (hasWorld) {
                if (nbt.contains("ticket")) {
                    int[] array = nbt.getIntArray("ticket");
                    if (array.length == 2) {
                        try {
                            boolean[] state = new boolean[component.getBandwidth()];
                            BooleanUtils.intToBool(array[1], state);
                            handler.ticket = SignalTicker.schedule(handler, state, array[0]);
                        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                    }
                }
            }
            return handler;
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public GuiSignalModeConfiguration createConfiguration(CompoundTag nbt) {
            return new GuiSignalModeConfigurationThreshold(nbt);
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public void createControls(GuiParent parent, GuiSignalModeConfiguration configuration) {}
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public GuiSignalModeConfiguration parseControls(GuiParent parent, int delay) {
            return new GuiSignalModeConfigurationThreshold(delay);
        }
    },
    STABILIZER("signal.mode.stabilizer") {
        
        @Override
        public SignalOutputHandler create(ISignalComponent component, int delay, CompoundTag nbt, boolean hasWorld) {
            SignalOutputHandlerStoreOne handler = new SignalOutputHandlerStoreOne(component, delay, nbt) {
                
                @Override
                public void queue(boolean[] state) {
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
                public void write(boolean preview, CompoundTag nbt) {
                    if (preview)
                        return;
                    if (ticket != null)
                        nbt.putIntArray("ticket", new int[] { ticket.getDelay(), BooleanUtils.boolToInt(ticket.getState()) });
                }
            };
            
            if (hasWorld) {
                if (nbt.contains("ticket")) {
                    int[] array = nbt.getIntArray("ticket");
                    if (array.length == 2) {
                        try {
                            boolean[] state = new boolean[component.getBandwidth()];
                            BooleanUtils.intToBool(array[1], state);
                            handler.ticket = SignalTicker.schedule(handler, state, array[0]);
                        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                    }
                }
            }
            return handler;
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public GuiSignalModeConfiguration createConfiguration(CompoundTag nbt) {
            return new GuiSignalModeConfigurationStabilizer(nbt);
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public void createControls(GuiParent parent, GuiSignalModeConfiguration configuration) {}
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public GuiSignalModeConfiguration parseControls(GuiParent parent, int delay) {
            return new GuiSignalModeConfigurationStabilizer(delay);
        }
    },
    EXTENDER("signal.mode.extender") {
        
        @Override
        public SignalOutputHandler create(ISignalComponent component, int delay, CompoundTag nbt, boolean hasWorld) {
            SignalOutputHandler condition = new SignalOutputHandlerExtender(component, delay, nbt);
            if (hasWorld) {
                if (nbt.contains("start")) {
                    SignalTicker.schedule(condition, BooleanUtils.asArray(true), nbt.getInt("start"));
                    SignalTicker.schedule(condition, BooleanUtils.asArray(false), nbt.getInt("end"));
                } else if (nbt.contains("end"))
                    SignalTicker.schedule(condition, BooleanUtils.asArray(false), nbt.getInt("end"));
            }
            return condition;
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public GuiSignalModeConfiguration createConfiguration(CompoundTag nbt) {
            return new GuiSignalModeConfigurationExtender(nbt);
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public void createControls(GuiParent parent, GuiSignalModeConfiguration configuration) {
            parent.add(new GuiLabel("length:"));
            parent.add(new GuiTextfield("length", "" + (configuration instanceof GuiSignalModeConfigurationExtender ? ((GuiSignalModeConfigurationExtender) configuration).length : 10))
                    .setNumbersOnly());
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public GuiSignalModeConfiguration parseControls(GuiParent parent, int delay) {
            GuiTextfield length = (GuiTextfield) parent.get("length");
            return new GuiSignalModeConfigurationExtender(delay, Math.max(1, length.parseInteger()));
        }
        
    };
    
    public final String translateKey;
    
    private SignalMode(String translateKey) {
        this.translateKey = translateKey;
    }
    
    public abstract SignalOutputHandler create(ISignalComponent component, int delay, CompoundTag nbt, boolean hasWorld);
    
    @OnlyIn(Dist.CLIENT)
    public abstract GuiSignalModeConfiguration createConfiguration(CompoundTag nbt);
    
    @OnlyIn(Dist.CLIENT)
    public abstract void createControls(GuiParent parent, GuiSignalModeConfiguration configuration);
    
    @OnlyIn(Dist.CLIENT)
    public abstract GuiSignalModeConfiguration parseControls(GuiParent parent, int delay);
    
    public static abstract class SignalOutputHandlerStoreOne extends SignalOutputHandler {
        
        ISignalScheduleTicket ticket;
        
        public SignalOutputHandlerStoreOne(ISignalComponent component, int delay, CompoundTag nbt) {
            super(component, delay, nbt);
        }
        
    }
    
    public static class SignalOutputHandlerToggle extends SignalOutputHandler {
        
        public boolean[] stateBefore;
        public boolean[] result;
        
        public SignalOutputHandlerToggle(ISignalComponent component, int delay, CompoundTag nbt, boolean[] stateBefore, boolean[] result) {
            super(component, delay, nbt);
            this.stateBefore = stateBefore;
            this.result = result;
        }
        
        @Override
        public SignalMode getMode() {
            return SignalMode.TOGGLE;
        }
        
        public void triggerToggle() {
            if (result == null) {
                try {
                    int bandwidth = component.getBandwidth();
                    result = new boolean[bandwidth];
                    BooleanUtils.set(result, component.getState());
                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
            }
            
            for (int i = 0; i < result.length; i++)
                result[i] = !result[i];
            performStateChange(result);
        }
        
        @Override
        public void queue(boolean[] state) {
            if (stateBefore == null || stateBefore.length != state.length) {
                stateBefore = new boolean[state.length];
                result = new boolean[state.length];
            }
            for (int i = 0; i < state.length; i++) {
                if (!stateBefore[i] && state[i])
                    result[i] = !result[i];
                stateBefore[i] = state[i];
            }
            SignalTicker.schedule(this, result, delay);
        }
        
        @Override
        public void write(boolean preview, CompoundTag nbt) {
            if (stateBefore != null) {
                nbt.putInt("bandwidth", stateBefore.length);
                nbt.putInt("before", BooleanUtils.boolToInt(stateBefore));
                nbt.putInt("result", BooleanUtils.boolToInt(result));
            }
            if (preview)
                return;
            List<ISignalScheduleTicket> tickets = SignalTicker.findTickets(component, this);
            ListTag list = new ListTag();
            for (int i = 0; i < tickets.size(); i++) {
                ISignalScheduleTicket ticket = tickets.get(i);
                list.add(new IntArrayTag(new int[] { ticket.getDelay(), BooleanUtils.boolToInt(ticket.getState()) }));
            }
            nbt.put("tickets", list);
        }
    }
    
    public static class SignalOutputHandlerPulse extends SignalOutputHandler {
        
        public final int pulseLength;
        public boolean stateBefore;
        public ISignalScheduleTicket pulseStart;
        public ISignalScheduleTicket pulseEnd;
        
        public SignalOutputHandlerPulse(ISignalComponent component, int delay, CompoundTag nbt) {
            super(component, delay, nbt);
            this.pulseLength = nbt.contains("length") ? nbt.getInt("length") : 10;
            this.stateBefore = nbt.getBoolean("before");
        }
        
        @Override
        public int getBandwidth() throws CorruptedConnectionException, NotYetConnectedException {
            return super.getBandwidth();
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
        public void queue(boolean[] state) {
            boolean current = BooleanUtils.any(state);
            if (pulseEnd == null && !stateBefore && current) {
                boolean[] startState = new boolean[state.length];
                Arrays.fill(startState, true);
                boolean[] endState = new boolean[state.length];
                pulseStart = SignalTicker.schedule(this, startState, delay);
                pulseEnd = SignalTicker.schedule(this, endState, delay + pulseLength);
            }
            stateBefore = current;
        }
        
        @Override
        public void write(boolean preview, CompoundTag nbt) {
            nbt.putInt("length", pulseLength);
            nbt.putBoolean("before", stateBefore);
            if (preview)
                return;
            if (pulseStart != null)
                nbt.putInt("start", pulseStart.getDelay());
            if (pulseEnd != null)
                nbt.putInt("end", pulseEnd.getDelay());
        }
        
    }
    
    public static class SignalOutputHandlerExtender extends SignalOutputHandler {
        
        public final int pulseLength;
        public boolean stateBefore;
        public ISignalScheduleTicket pulseStart;
        public ISignalScheduleTicket pulseEnd;
        
        public SignalOutputHandlerExtender(ISignalComponent component, int delay, CompoundTag nbt) {
            super(component, delay, nbt);
            this.pulseLength = nbt.contains("length") ? nbt.getInt("length") : 10;
            this.stateBefore = nbt.getBoolean("before");
        }
        
        @Override
        public int getBandwidth() throws CorruptedConnectionException, NotYetConnectedException {
            return super.getBandwidth();
        }
        
        @Override
        public SignalMode getMode() {
            return SignalMode.EXTENDER;
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
        public void queue(boolean[] state) {
            boolean current = BooleanUtils.any(state);
            if (!stateBefore && current) { // switch from off to on
                if (pulseEnd != null) {
                    pulseEnd.markObsolete();
                    pulseEnd = null;
                } else if (pulseStart == null) {
                    boolean[] startState = new boolean[state.length];
                    Arrays.fill(startState, true);
                    pulseStart = SignalTicker.schedule(this, startState, delay);
                }
            } else if (stateBefore && !current) { // switch from on to off
                if (pulseEnd != null) {
                    pulseEnd.markObsolete();
                    pulseEnd = null;
                }
                
                pulseEnd = SignalTicker.schedule(this, new boolean[state.length], delay + pulseLength);
            }
            stateBefore = current;
        }
        
        @Override
        public void write(boolean preview, CompoundTag nbt) {
            nbt.putInt("length", pulseLength);
            nbt.putBoolean("before", stateBefore);
            if (preview)
                return;
            if (pulseStart != null)
                nbt.putInt("start", pulseStart.getDelay());
            if (pulseEnd != null)
                nbt.putInt("end", pulseEnd.getDelay());
        }
        
    }
    
    @OnlyIn(Dist.CLIENT)
    public static GuiSignalModeConfiguration getConfigDefault() {
        return EQUAL.createConfiguration(null);
    }
    
    @OnlyIn(Dist.CLIENT)
    public static GuiSignalModeConfiguration getConfig(CompoundTag nbt, SignalMode defaultMode) {
        return get(nbt.getString("mode"), defaultMode).createConfiguration(nbt);
    }
    
    public static SignalMode get(String test) {
        try {
            return SignalMode.valueOf(test);
        } catch (IllegalArgumentException e) {
            return EQUAL;
        }
    }
    
    public static SignalMode get(String test, SignalMode defaultMode) {
        try {
            return SignalMode.valueOf(test);
        } catch (IllegalArgumentException e) {
            return defaultMode;
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    public static abstract class GuiSignalModeConfiguration {
        
        public int delay;
        
        public GuiSignalModeConfiguration(CompoundTag nbt) {
            this(nbt.getInt("delay"));
        }
        
        public GuiSignalModeConfiguration(int delay) {
            this.delay = delay;
        }
        
        public abstract SignalMode getMode();
        
        public abstract GuiSignalModeConfiguration copy();
        
        public abstract SignalOutputHandler getHandler(ISignalComponent component, LittleStructure structure);
        
    }
    
    @OnlyIn(Dist.CLIENT)
    private static class GuiSignalModeConfigurationEqual extends GuiSignalModeConfiguration {
        
        public GuiSignalModeConfigurationEqual(int delay) {
            super(delay);
        }
        
        public GuiSignalModeConfigurationEqual(CompoundTag nbt) {
            super(nbt);
        }
        
        @Override
        public SignalMode getMode() {
            return EQUAL;
        }
        
        @Override
        public GuiSignalModeConfiguration copy() {
            return new GuiSignalModeConfigurationEqual(delay);
        }
        
        @Override
        public SignalOutputHandler getHandler(ISignalComponent component, LittleStructure structure) {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("delay", delay);
            return getMode().create(component, delay, nbt, false);
        }
        
    }
    
    @OnlyIn(Dist.CLIENT)
    private static class GuiSignalModeConfigurationToggle extends GuiSignalModeConfiguration {
        
        public GuiSignalModeConfigurationToggle(int delay) {
            super(delay);
        }
        
        public GuiSignalModeConfigurationToggle(CompoundTag nbt) {
            super(nbt);
        }
        
        @Override
        public SignalMode getMode() {
            return TOGGLE;
        }
        
        @Override
        public GuiSignalModeConfiguration copy() {
            return new GuiSignalModeConfigurationToggle(delay);
        }
        
        @Override
        public SignalOutputHandler getHandler(ISignalComponent component, LittleStructure structure) {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("delay", delay);
            return getMode().create(component, delay, nbt, false);
        }
        
    }
    
    @OnlyIn(Dist.CLIENT)
    private static class GuiSignalModeConfigurationPulse extends GuiSignalModeConfiguration {
        
        public int length;
        
        public GuiSignalModeConfigurationPulse(int delay, int length) {
            super(delay);
            this.length = length;
        }
        
        public GuiSignalModeConfigurationPulse(CompoundTag nbt) {
            super(nbt);
            this.length = nbt.contains("length") ? nbt.getInt("length") : 10;
        }
        
        @Override
        public SignalMode getMode() {
            return PULSE;
        }
        
        @Override
        public GuiSignalModeConfiguration copy() {
            return new GuiSignalModeConfigurationPulse(delay, length);
        }
        
        @Override
        public SignalOutputHandler getHandler(ISignalComponent component, LittleStructure structure) {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("delay", delay);
            nbt.putInt("length", length);
            return getMode().create(component, delay, nbt, false);
        }
        
    }
    
    @OnlyIn(Dist.CLIENT)
    private static class GuiSignalModeConfigurationExtender extends GuiSignalModeConfiguration {
        
        public int length;
        
        public GuiSignalModeConfigurationExtender(int delay, int length) {
            super(delay);
            this.length = length;
        }
        
        public GuiSignalModeConfigurationExtender(CompoundTag nbt) {
            super(nbt);
            this.length = nbt.contains("length") ? nbt.getInt("length") : 10;
        }
        
        @Override
        public SignalMode getMode() {
            return EXTENDER;
        }
        
        @Override
        public GuiSignalModeConfiguration copy() {
            return new GuiSignalModeConfigurationExtender(delay, length);
        }
        
        @Override
        public SignalOutputHandler getHandler(ISignalComponent component, LittleStructure structure) {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("delay", delay);
            nbt.putInt("length", length);
            return getMode().create(component, delay, nbt, false);
        }
        
    }
    
    @OnlyIn(Dist.CLIENT)
    private static class GuiSignalModeConfigurationThreshold extends GuiSignalModeConfiguration {
        
        public GuiSignalModeConfigurationThreshold(int delay) {
            super(delay);
        }
        
        public GuiSignalModeConfigurationThreshold(CompoundTag nbt) {
            super(nbt);
        }
        
        @Override
        public SignalMode getMode() {
            return THRESHOLD;
        }
        
        @Override
        public GuiSignalModeConfiguration copy() {
            return new GuiSignalModeConfigurationThreshold(delay);
        }
        
        @Override
        public SignalOutputHandler getHandler(ISignalComponent component, LittleStructure structure) {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("delay", delay);
            return getMode().create(component, delay, nbt, false);
        }
        
    }
    
    @OnlyIn(Dist.CLIENT)
    private static class GuiSignalModeConfigurationStabilizer extends GuiSignalModeConfiguration {
        
        public GuiSignalModeConfigurationStabilizer(int delay) {
            super(delay);
        }
        
        public GuiSignalModeConfigurationStabilizer(CompoundTag nbt) {
            super(nbt);
        }
        
        @Override
        public SignalMode getMode() {
            return STABILIZER;
        }
        
        @Override
        public GuiSignalModeConfiguration copy() {
            return new GuiSignalModeConfigurationStabilizer(delay);
        }
        
        @Override
        public SignalOutputHandler getHandler(ISignalComponent component, LittleStructure structure) {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("delay", delay);
            return getMode().create(component, delay, nbt, false);
        }
        
    }
}
