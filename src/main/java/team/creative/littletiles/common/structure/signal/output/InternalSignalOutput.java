package team.creative.littletiles.common.structure.signal.output;

import java.text.ParseException;
import java.util.Arrays;

import com.creativemd.creativecore.common.packet.PacketHandler;

import net.minecraft.nbt.CompoundTag;
import team.creative.creativecore.common.util.math.utils.BooleanUtils;
import team.creative.littletiles.common.packet.LittleUpdateOutputPacket;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType.InternalComponentOutput;
import team.creative.littletiles.common.structure.signal.component.InternalSignal;
import team.creative.littletiles.common.structure.signal.component.SignalComponentType;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;
import team.creative.littletiles.common.structure.signal.logic.SignalMode;
import team.creative.littletiles.common.structure.signal.logic.SignalMode.SignalOutputHandlerToggle;

public class InternalSignalOutput extends InternalSignal<InternalComponentOutput> {
    
    public final SignalMode defaultMode;
    public final boolean syncToClient;
    public SignalInputCondition condition;
    public SignalOutputHandler handler;
    
    public InternalSignalOutput(LittleStructure parent, InternalComponentOutput component) {
        super(parent, component);
        this.defaultMode = component.defaultMode;
        this.syncToClient = component.syncToClient;
    }
    
    @Override
    public void changed() {
        parent.performInternalOutputChange(this);
        parent.schedule();
        if (syncToClient)
            PacketHandler.sendPacketToTrackingPlayers(new LittleUpdateOutputPacket(parent.getStructureLocation(), component.index, getState()), getStructureWorld(), parent
                    .getPos(), null);
    }
    
    @Override
    public SignalComponentType getComponentType() {
        return SignalComponentType.OUTPUT;
    }
    
    @Override
    public void load(CompoundTag nbt) {
        BooleanUtils.intToBool(nbt.getInt("state"), getState());
        try {
            if (nbt.contains("con"))
                condition = SignalInputCondition.parseInput(nbt.getString("con"));
            else
                condition = null;
        } catch (ParseException e) {
            condition = null;
        }
        SignalMode mode = defaultMode;
        if (nbt.contains("mode"))
            mode = SignalMode.valueOf(nbt.getString("mode"));
        int delay = nbt.getInt("delay");
        if (condition != null)
            delay = Math.max((int) Math.ceil(condition.calculateDelay()), nbt.getInt("delay"));
        handler = SignalOutputHandler.create(this, mode, delay, nbt, parent);
    }
    
    @Override
    public CompoundTag write(boolean preview, CompoundTag nbt) {
        nbt.putInt("state", BooleanUtils.boolToInt(getState()));
        if (condition != null)
            nbt.putString("con", condition.write());
        nbt.putString("mode", handler == null ? defaultMode.name() : handler.getMode().name());
        if (handler != null) {
            nbt.putInt("delay", handler.delay);
            handler.write(preview, nbt);
        }
        return nbt;
    }
    
    public void toggle() {
        if (handler instanceof SignalOutputHandlerToggle)
            ((SignalOutputHandlerToggle) handler).triggerToggle();
    }
    
    public void update() {
        if (condition == null)
            return;
        int bandwidth = getBandwidth();
        if (bandwidth > 0) {
            boolean[] outputState = new boolean[bandwidth];
            boolean[] result = condition.test(getStructure(), false);
            if (result.length == 1)
                Arrays.fill(outputState, result[0]);
            else
                for (int i = 0; i < result.length; i++)
                    if (i < outputState.length)
                        outputState[i] = result[i];
            handler.schedule(outputState);
        }
    }
    
}
