package com.creativemd.littletiles.common.structure.signal.logic;

import java.util.Arrays;
import java.util.List;

import com.creativemd.littletiles.common.structure.signal.component.ISignalComponent;
import com.creativemd.littletiles.common.structure.signal.output.SignalOutputHandler;
import com.creativemd.littletiles.common.structure.signal.schedule.ISignalScheduleTicket;
import com.creativemd.littletiles.common.structure.signal.schedule.SignalTicker;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;

public enum SignalMode {
    
    EQUAL("signal.mode.equal") {
        @Override
        public SignalOutputHandler create(ISignalComponent component, int delay, NBTTagCompound nbt, boolean hasWorld) {
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
                public void write(boolean preview, NBTTagCompound nbt) {
                    if (preview)
                        return;
                    List<ISignalScheduleTicket> tickets = SignalTicker.findTickets(component, this);
                    NBTTagList list = new NBTTagList();
                    for (int i = 0; i < tickets.size(); i++) {
                        ISignalScheduleTicket ticket = tickets.get(i);
                        list.appendTag(new NBTTagIntArray(new int[] { ticket.getDelay(), BooleanUtils.boolToInt(ticket.getState()) }));
                    }
                    nbt.setTag("tickets", list);
                }
                
            };
            if (hasWorld) {
                NBTTagList list = nbt.getTagList("tickets", 11);
                for (int i = 0; i < list.tagCount(); i++) {
                    int[] array = list.getIntArrayAt(i);
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
        @SideOnly(Side.CLIENT)
        public GuiSignalModeConfiguration createConfiguration(NBTTagCompound nbt) {
            if (nbt == null)
                return new GuiSignalModeConfigurationEqual(1);
            return new GuiSignalModeConfigurationEqual(nbt);
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public void createControls(GuiParent parent, GuiSignalModeConfiguration configuration) {}
        
        @Override
        @SideOnly(Side.CLIENT)
        public GuiSignalModeConfiguration parseControls(GuiParent parent, int delay) {
            return new GuiSignalModeConfigurationEqual(delay);
        }
        
    },
    TOGGLE("signal.mode.toggle") {
        
        @Override
        public SignalOutputHandler create(ISignalComponent component, int delay, NBTTagCompound nbt, boolean hasWorld) {
            boolean[] before;
            boolean[] result;
            int bandwidth = nbt.getInteger("bandwidth");
            if (bandwidth > 0) {
                before = new boolean[bandwidth];
                result = new boolean[bandwidth];
                BooleanUtils.intToBool(nbt.getInteger("before"), before);
                BooleanUtils.intToBool(nbt.getInteger("result"), result);
            } else {
                before = null;
                result = null;
            }
            
            SignalOutputHandler handler = new SignalOutputHandlerToggle(component, delay, nbt, before, result);
            if (hasWorld) {
                NBTTagList list = nbt.getTagList("tickets", 11);
                for (int i = 0; i < list.tagCount(); i++) {
                    int[] array = list.getIntArrayAt(i);
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
        @SideOnly(Side.CLIENT)
        public GuiSignalModeConfiguration createConfiguration(NBTTagCompound nbt) {
            return new GuiSignalModeConfigurationToggle(nbt);
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public void createControls(GuiParent parent, GuiSignalModeConfiguration configuration) {}
        
        @Override
        @SideOnly(Side.CLIENT)
        public GuiSignalModeConfiguration parseControls(GuiParent parent, int delay) {
            return new GuiSignalModeConfigurationToggle(delay);
        }
        
    },
    PULSE("signal.mode.pulse") {
        
        @Override
        public SignalOutputHandler create(ISignalComponent component, int delay, NBTTagCompound nbt, boolean hasWorld) {
            SignalOutputHandler condition = new SignalOutputHandlerPulse(component, delay, nbt);
            if (hasWorld) {
                if (nbt.hasKey("start")) {
                    SignalTicker.schedule(condition, BooleanUtils.asArray(true), nbt.getInteger("start"));
                    SignalTicker.schedule(condition, BooleanUtils.asArray(false), nbt.getInteger("end"));
                } else if (nbt.hasKey("end"))
                    SignalTicker.schedule(condition, BooleanUtils.asArray(false), nbt.getInteger("end"));
            }
            return condition;
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public GuiSignalModeConfiguration createConfiguration(NBTTagCompound nbt) {
            return new GuiSignalModeConfigurationPulse(nbt);
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public void createControls(GuiParent parent, GuiSignalModeConfiguration configuration) {
            parent.addControl(new GuiLabel("length:", 0, 43));
            parent.addControl(new GuiTextfield("length", "" + (configuration instanceof GuiSignalModeConfigurationPulse ? ((GuiSignalModeConfigurationPulse) configuration).length : 10), 40, 41, 50, 12)
                    .setNumbersOnly());
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public GuiSignalModeConfiguration parseControls(GuiParent parent, int delay) {
            GuiTextfield length = (GuiTextfield) parent.get("length");
            return new GuiSignalModeConfigurationPulse(delay, Math.max(1, length.parseInteger()));
        }
    },
    THRESHOLD("signal.mode.threshold") {
        
        @Override
        public SignalOutputHandler create(ISignalComponent component, int delay, NBTTagCompound nbt, boolean hasWorld) {
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
                public void write(boolean preview, NBTTagCompound nbt) {
                    if (!preview && ticket != null)
                        nbt.setIntArray("ticket", new int[] { ticket.getDelay(), BooleanUtils.boolToInt(ticket.getState()) });
                }
            };
            
            if (hasWorld) {
                if (nbt.hasKey("ticket")) {
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
        @SideOnly(Side.CLIENT)
        public GuiSignalModeConfiguration createConfiguration(NBTTagCompound nbt) {
            return new GuiSignalModeConfigurationThreshold(nbt);
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public void createControls(GuiParent parent, GuiSignalModeConfiguration configuration) {}
        
        @Override
        @SideOnly(Side.CLIENT)
        public GuiSignalModeConfiguration parseControls(GuiParent parent, int delay) {
            return new GuiSignalModeConfigurationThreshold(delay);
        }
    },
    STABILIZER("signal.mode.stabilizer") {
        
        @Override
        public SignalOutputHandler create(ISignalComponent component, int delay, NBTTagCompound nbt, boolean hasWorld) {
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
                public void write(boolean preview, NBTTagCompound nbt) {
                    if (preview)
                        return;
                    if (ticket != null)
                        nbt.setIntArray("ticket", new int[] { ticket.getDelay(), BooleanUtils.boolToInt(ticket.getState()) });
                }
            };
            
            if (hasWorld) {
                if (nbt.hasKey("ticket")) {
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
        @SideOnly(Side.CLIENT)
        public GuiSignalModeConfiguration createConfiguration(NBTTagCompound nbt) {
            return new GuiSignalModeConfigurationStabilizer(nbt);
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public void createControls(GuiParent parent, GuiSignalModeConfiguration configuration) {}
        
        @Override
        @SideOnly(Side.CLIENT)
        public GuiSignalModeConfiguration parseControls(GuiParent parent, int delay) {
            return new GuiSignalModeConfigurationStabilizer(delay);
        }
    },
    EXTENDER("signal.mode.extender") {
        
        @Override
        public SignalOutputHandler create(ISignalComponent component, int delay, NBTTagCompound nbt, boolean hasWorld) {
            SignalOutputHandler condition = new SignalOutputHandlerExtender(component, delay, nbt);
            if (hasWorld) {
                if (nbt.hasKey("start")) {
                    SignalTicker.schedule(condition, BooleanUtils.asArray(true), nbt.getInteger("start"));
                    SignalTicker.schedule(condition, BooleanUtils.asArray(false), nbt.getInteger("end"));
                } else if (nbt.hasKey("end"))
                    SignalTicker.schedule(condition, BooleanUtils.asArray(false), nbt.getInteger("end"));
            }
            return condition;
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public GuiSignalModeConfiguration createConfiguration(NBTTagCompound nbt) {
            return new GuiSignalModeConfigurationExtender(nbt);
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public void createControls(GuiParent parent, GuiSignalModeConfiguration configuration) {
            parent.addControl(new GuiLabel("length:", 0, 43));
            parent.addControl(new GuiTextfield("length", "" + (configuration instanceof GuiSignalModeConfigurationExtender ? ((GuiSignalModeConfigurationExtender) configuration).length : 10), 40, 41, 50, 12)
                    .setNumbersOnly());
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public GuiSignalModeConfiguration parseControls(GuiParent parent, int delay) {
            GuiTextfield length = (GuiTextfield) parent.get("length");
            return new GuiSignalModeConfigurationExtender(delay, Math.max(1, length.parseInteger()));
        }
        
    };
    
    public final String translateKey;
    
    private SignalMode(String translateKey) {
        this.translateKey = translateKey;
    }
    
    public abstract SignalOutputHandler create(ISignalComponent component, int delay, NBTTagCompound nbt, boolean hasWorld);
    
    @SideOnly(Side.CLIENT)
    public abstract GuiSignalModeConfiguration createConfiguration(NBTTagCompound nbt);
    
    @SideOnly(Side.CLIENT)
    public abstract void createControls(GuiParent parent, GuiSignalModeConfiguration configuration);
    
    @SideOnly(Side.CLIENT)
    public abstract GuiSignalModeConfiguration parseControls(GuiParent parent, int delay);
    
    public static abstract class SignalOutputHandlerStoreOne extends SignalOutputHandler {
        
        ISignalScheduleTicket ticket;
        
        public SignalOutputHandlerStoreOne(ISignalComponent component, int delay, NBTTagCompound nbt) {
            super(component, delay, nbt);
        }
        
    }
    
    public static class SignalOutputHandlerToggle extends SignalOutputHandler {
        
        public boolean[] stateBefore;
        public boolean[] result;
        
        public SignalOutputHandlerToggle(ISignalComponent component, int delay, NBTTagCompound nbt, boolean[] stateBefore, boolean[] result) {
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
        public void write(boolean preview, NBTTagCompound nbt) {
            if (stateBefore != null) {
                nbt.setInteger("bandwidth", stateBefore.length);
                nbt.setInteger("before", BooleanUtils.boolToInt(stateBefore));
                nbt.setInteger("result", BooleanUtils.boolToInt(result));
            }
            if (preview)
                return;
            List<ISignalScheduleTicket> tickets = SignalTicker.findTickets(component, this);
            NBTTagList list = new NBTTagList();
            for (int i = 0; i < tickets.size(); i++) {
                ISignalScheduleTicket ticket = tickets.get(i);
                list.appendTag(new NBTTagIntArray(new int[] { ticket.getDelay(), BooleanUtils.boolToInt(ticket.getState()) }));
            }
            nbt.setTag("tickets", list);
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
        public void write(boolean preview, NBTTagCompound nbt) {
            nbt.setInteger("length", pulseLength);
            nbt.setBoolean("before", stateBefore);
            if (preview)
                return;
            if (pulseStart != null)
                nbt.setInteger("start", pulseStart.getDelay());
            if (pulseEnd != null)
                nbt.setInteger("end", pulseEnd.getDelay());
        }
        
    }
    
    public static class SignalOutputHandlerExtender extends SignalOutputHandler {
        
        public final int pulseLength;
        public boolean stateBefore;
        public ISignalScheduleTicket pulseStart;
        public ISignalScheduleTicket pulseEnd;
        
        public SignalOutputHandlerExtender(ISignalComponent component, int delay, NBTTagCompound nbt) {
            super(component, delay, nbt);
            this.pulseLength = nbt.hasKey("length") ? nbt.getInteger("length") : 10;
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
        public void write(boolean preview, NBTTagCompound nbt) {
            nbt.setInteger("length", pulseLength);
            nbt.setBoolean("before", stateBefore);
            if (preview)
                return;
            if (pulseStart != null)
                nbt.setInteger("start", pulseStart.getDelay());
            if (pulseEnd != null)
                nbt.setInteger("end", pulseEnd.getDelay());
        }
        
    }
    
    @SideOnly(Side.CLIENT)
    public static GuiSignalModeConfiguration getConfigDefault() {
        return EQUAL.createConfiguration(null);
    }
    
    @SideOnly(Side.CLIENT)
    public static GuiSignalModeConfiguration getConfig(NBTTagCompound nbt, SignalMode defaultMode) {
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
    
    @SideOnly(Side.CLIENT)
    public static abstract class GuiSignalModeConfiguration {
        
        public int delay;
        
        public GuiSignalModeConfiguration(NBTTagCompound nbt) {
            this(nbt.getInteger("delay"));
        }
        
        public GuiSignalModeConfiguration(int delay) {
            this.delay = delay;
        }
        
        public abstract SignalMode getMode();
        
        public abstract GuiSignalModeConfiguration copy();
        
        public abstract SignalOutputHandler getHandler(ISignalComponent component, LittleStructure structure);
        
    }
    
    @SideOnly(Side.CLIENT)
    private static class GuiSignalModeConfigurationEqual extends GuiSignalModeConfiguration {
        
        public GuiSignalModeConfigurationEqual(int delay) {
            super(delay);
        }
        
        public GuiSignalModeConfigurationEqual(NBTTagCompound nbt) {
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
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger("delay", delay);
            return getMode().create(component, delay, nbt, false);
        }
        
    }
    
    @SideOnly(Side.CLIENT)
    private static class GuiSignalModeConfigurationToggle extends GuiSignalModeConfiguration {
        
        public GuiSignalModeConfigurationToggle(int delay) {
            super(delay);
        }
        
        public GuiSignalModeConfigurationToggle(NBTTagCompound nbt) {
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
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger("delay", delay);
            return getMode().create(component, delay, nbt, false);
        }
        
    }
    
    @SideOnly(Side.CLIENT)
    private static class GuiSignalModeConfigurationPulse extends GuiSignalModeConfiguration {
        
        public int length;
        
        public GuiSignalModeConfigurationPulse(int delay, int length) {
            super(delay);
            this.length = length;
        }
        
        public GuiSignalModeConfigurationPulse(NBTTagCompound nbt) {
            super(nbt);
            this.length = nbt.hasKey("length") ? nbt.getInteger("length") : 10;
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
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger("delay", delay);
            nbt.setInteger("length", length);
            return getMode().create(component, delay, nbt, false);
        }
        
    }
    
    @SideOnly(Side.CLIENT)
    private static class GuiSignalModeConfigurationExtender extends GuiSignalModeConfiguration {
        
        public int length;
        
        public GuiSignalModeConfigurationExtender(int delay, int length) {
            super(delay);
            this.length = length;
        }
        
        public GuiSignalModeConfigurationExtender(NBTTagCompound nbt) {
            super(nbt);
            this.length = nbt.hasKey("length") ? nbt.getInteger("length") : 10;
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
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger("delay", delay);
            nbt.setInteger("length", length);
            return getMode().create(component, delay, nbt, false);
        }
        
    }
    
    @SideOnly(Side.CLIENT)
    private static class GuiSignalModeConfigurationThreshold extends GuiSignalModeConfiguration {
        
        public GuiSignalModeConfigurationThreshold(int delay) {
            super(delay);
        }
        
        public GuiSignalModeConfigurationThreshold(NBTTagCompound nbt) {
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
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger("delay", delay);
            return getMode().create(component, delay, nbt, false);
        }
        
    }
    
    @SideOnly(Side.CLIENT)
    private static class GuiSignalModeConfigurationStabilizer extends GuiSignalModeConfiguration {
        
        public GuiSignalModeConfigurationStabilizer(int delay) {
            super(delay);
        }
        
        public GuiSignalModeConfigurationStabilizer(NBTTagCompound nbt) {
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
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger("delay", delay);
            return getMode().create(component, delay, nbt, false);
        }
        
    }
}
