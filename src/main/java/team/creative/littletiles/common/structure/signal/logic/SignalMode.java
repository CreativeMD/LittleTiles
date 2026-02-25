package team.creative.littletiles.common.structure.signal.logic;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.control.simple.GuiLabel;
import team.creative.creativecore.common.gui.control.simple.GuiTextfield;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.gui.signal.mode.GuiSignalModeConfiguration;
import team.creative.littletiles.common.gui.signal.mode.GuiSignalModeConfigurationEqual;
import team.creative.littletiles.common.gui.signal.mode.GuiSignalModeConfigurationExtender;
import team.creative.littletiles.common.gui.signal.mode.GuiSignalModeConfigurationPulse;
import team.creative.littletiles.common.gui.signal.mode.GuiSignalModeConfigurationStabilizer;
import team.creative.littletiles.common.gui.signal.mode.GuiSignalModeConfigurationThreshold;
import team.creative.littletiles.common.gui.signal.mode.GuiSignalModeConfigurationToggle;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.SignalState;
import team.creative.littletiles.common.structure.signal.component.ISignalComponent;
import team.creative.littletiles.common.structure.signal.output.SignalOutputHandler;
import team.creative.littletiles.common.structure.signal.output.mode.SignalOutputHandlerExtender;
import team.creative.littletiles.common.structure.signal.output.mode.SignalOutputHandlerPulse;
import team.creative.littletiles.common.structure.signal.output.mode.SignalOutputHandlerStoreOne;
import team.creative.littletiles.common.structure.signal.output.mode.SignalOutputHandlerToggle;
import team.creative.littletiles.common.structure.signal.schedule.SignalScheduleTicket;

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
                public void queue(SignalState state) {
                    LittleTiles.TICKERS.schedule(this, state, delay);
                }
                
                @Override
                public void write(boolean preview, CompoundTag nbt) {
                    if (preview)
                        return;
                    List<SignalScheduleTicket> tickets = LittleTiles.TICKERS.findTickets(component, this);
                    ListTag list = new ListTag();
                    for (int i = 0; i < tickets.size(); i++) {
                        SignalScheduleTicket ticket = tickets.get(i);
                        list.add(new IntArrayTag(new int[] { ticket.getDelay(), ticket.getState().number() }));
                    }
                    if (!list.isEmpty())
                        nbt.put("tickets", list);
                }
                
            };
            if (hasWorld) {
                ListTag list = nbt.getList("tickets", 11);
                for (int i = 0; i < list.size(); i++) {
                    int[] array = list.getIntArray(i);
                    if (array.length == 2) {
                        try {
                            SignalState state = SignalState.create(component.getBandwidth()).load(array[1]);
                            LittleTiles.TICKERS.schedule(handler, state, array[0]);
                        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                        
                    }
                }
            }
            return handler;
        }
        
        @Override
        public GuiSignalModeConfiguration createConfiguration(SignalOutputHandler handler) {
            return new GuiSignalModeConfigurationEqual(handler);
        }
        
        @Override
        public void createControls(GuiParent parent, GuiSignalModeConfiguration configuration) {}
        
        @Override
        public GuiSignalModeConfiguration parseControls(GuiParent parent, int delay) {
            return new GuiSignalModeConfigurationEqual(delay);
        }
        
    },
    TOGGLE("signal.mode.toggle") {
        
        @Override
        public SignalOutputHandler create(ISignalComponent component, int delay, CompoundTag nbt, boolean hasWorld) {
            SignalState before;
            SignalState result;
            int bandwidth = nbt.getInt("bandwidth");
            if (bandwidth > 0) {
                before = SignalState.create(bandwidth);
                result = SignalState.create(bandwidth);
                before = before.load(nbt.get("before"));
                result = result.load(nbt.get("result"));
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
                            SignalState state = SignalState.create(component.getBandwidth()).load(array[1]);
                            LittleTiles.TICKERS.schedule(handler, state, array[0]);
                        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                    }
                }
            }
            return handler;
        }
        
        @Override
        public GuiSignalModeConfiguration createConfiguration(SignalOutputHandler handler) {
            return new GuiSignalModeConfigurationToggle(handler);
        }
        
        @Override
        public void createControls(GuiParent parent, GuiSignalModeConfiguration configuration) {}
        
        @Override
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
                    LittleTiles.TICKERS.schedule(condition, SignalState.TRUE, nbt.getInt("start"));
                    LittleTiles.TICKERS.schedule(condition, SignalState.FALSE, nbt.getInt("end"));
                } else if (nbt.contains("end"))
                    LittleTiles.TICKERS.schedule(condition, SignalState.FALSE, nbt.getInt("end"));
            }
            return condition;
        }
        
        @Override
        public GuiSignalModeConfiguration createConfiguration(SignalOutputHandler handler) {
            return new GuiSignalModeConfigurationPulse(handler);
        }
        
        @Override
        public void createControls(GuiParent parent, GuiSignalModeConfiguration configuration) {
            parent.add(new GuiLabel("length:").setTitle(Component.translatable("gui.signal.length").append(":")));
            parent.add(new GuiTextfield("length", "" + (configuration instanceof GuiSignalModeConfigurationPulse ? ((GuiSignalModeConfigurationPulse) configuration).length : 10))
                    .setNumbersOnly());
        }
        
        @Override
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
                public void queue(SignalState state) {
                    if (ticket != null)
                        ticket.overwriteState(state);
                    else
                        ticket = LittleTiles.TICKERS.schedule(this, state, delay);
                }
                
                @Override
                public void performStateChange(SignalState state) {
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
                        nbt.putIntArray("ticket", new int[] { ticket.getDelay(), ticket.getState().number() });
                }
            };
            
            if (hasWorld) {
                if (nbt.contains("ticket")) {
                    int[] array = nbt.getIntArray("ticket");
                    if (array.length == 2) {
                        try {
                            SignalState state = SignalState.create(component.getBandwidth()).load(array[1]);
                            handler.ticket = LittleTiles.TICKERS.schedule(handler, state, array[0]);
                        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                    }
                }
            }
            return handler;
        }
        
        @Override
        public GuiSignalModeConfiguration createConfiguration(SignalOutputHandler handler) {
            return new GuiSignalModeConfigurationThreshold(handler);
        }
        
        @Override
        public void createControls(GuiParent parent, GuiSignalModeConfiguration configuration) {}
        
        @Override
        public GuiSignalModeConfiguration parseControls(GuiParent parent, int delay) {
            return new GuiSignalModeConfigurationThreshold(delay);
        }
    },
    STABILIZER("signal.mode.stabilizer") {
        
        @Override
        public SignalOutputHandler create(ISignalComponent component, int delay, CompoundTag nbt, boolean hasWorld) {
            SignalOutputHandlerStoreOne handler = new SignalOutputHandlerStoreOne(component, delay, nbt) {
                
                @Override
                public void queue(SignalState state) {
                    if (ticket != null)
                        ticket.markObsolete();
                    ticket = LittleTiles.TICKERS.schedule(this, state, delay);
                }
                
                @Override
                public void performStateChange(SignalState state) {
                    ticket = null;
                    super.performStateChange(state);
                }
                
                @Override
                public SignalMode getMode() {
                    return SignalMode.STABILIZER;
                }
                
                @Override
                public void write(boolean preview, CompoundTag nbt) {
                    if (preview)
                        return;
                    if (ticket != null)
                        nbt.putIntArray("ticket", new int[] { ticket.getDelay(), ticket.getState().number() });
                }
            };
            
            if (hasWorld) {
                if (nbt.contains("ticket")) {
                    int[] array = nbt.getIntArray("ticket");
                    if (array.length == 2) {
                        try {
                            SignalState state = SignalState.create(component.getBandwidth()).load(array[1]);
                            handler.ticket = LittleTiles.TICKERS.schedule(handler, state, array[0]);
                        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                    }
                }
            }
            return handler;
        }
        
        @Override
        public GuiSignalModeConfiguration createConfiguration(SignalOutputHandler handler) {
            return new GuiSignalModeConfigurationStabilizer(handler);
        }
        
        @Override
        public void createControls(GuiParent parent, GuiSignalModeConfiguration configuration) {}
        
        @Override
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
                    LittleTiles.TICKERS.schedule(condition, SignalState.TRUE, nbt.getInt("start"));
                    LittleTiles.TICKERS.schedule(condition, SignalState.FALSE, nbt.getInt("end"));
                } else if (nbt.contains("end"))
                    LittleTiles.TICKERS.schedule(condition, SignalState.FALSE, nbt.getInt("end"));
            }
            return condition;
        }
        
        @Override
        public GuiSignalModeConfiguration createConfiguration(SignalOutputHandler handler) {
            return new GuiSignalModeConfigurationExtender(handler);
        }
        
        @Override
        public void createControls(GuiParent parent, GuiSignalModeConfiguration configuration) {
            parent.add(new GuiLabel("length:").setTitle(Component.translatable("gui.signal.length").append(":")));
            parent.add(
                new GuiTextfield("length", "" + (configuration instanceof GuiSignalModeConfigurationExtender ? ((GuiSignalModeConfigurationExtender) configuration).length : 10))
                        .setNumbersOnly());
        }
        
        @Override
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
    
    public abstract GuiSignalModeConfiguration createConfiguration(SignalOutputHandler handler);
    
    public abstract void createControls(GuiParent parent, GuiSignalModeConfiguration configuration);
    
    public abstract GuiSignalModeConfiguration parseControls(GuiParent parent, int delay);
    
    public static GuiSignalModeConfiguration getConfigDefault() {
        return EQUAL.createConfiguration(null);
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
}
