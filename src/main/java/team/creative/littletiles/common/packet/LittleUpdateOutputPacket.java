package team.creative.littletiles.common.packet;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.creativecore.common.util.math.utils.BooleanUtils;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.math.location.StructureLocation;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.signal.output.InternalSignalOutput;

public class LittleUpdateOutputPacket extends CreativePacket {
    
    public StructureLocation location;
    public int index;
    public boolean[] state;
    
    public LittleUpdateOutputPacket() {
        
    }
    
    public LittleUpdateOutputPacket(StructureLocation location, int index, boolean[] state) {
        this.location = location;
        this.index = index;
        this.state = state;
    }
    
    @Override
    public void executeClient(Player player) {
        try {
            LittleStructure structure = location.find(player.level);
            InternalSignalOutput output = structure.getOutput(index);
            BooleanUtils.set(output.getState(), state);
            structure.receiveInternalOutputChange(output);
        } catch (LittleActionException e) {}
    }
    
    @Override
    public void executeServer(ServerPlayer player) {
        
    }
    
}
