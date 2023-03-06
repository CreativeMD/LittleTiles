package team.creative.littletiles.common.packet.entity.level;

import java.util.Arrays;
import java.util.List;

import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;
import team.creative.littletiles.common.entity.level.LittleLevelEntity;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.common.level.little.LittlePlayerConnection;
import team.creative.littletiles.common.packet.entity.LittleEntityPacket;

public class LittleLevelPackets extends LittleEntityPacket<LittleLevelEntity> {
    
    public List<? extends Packet> packets;
    
    public LittleLevelPackets() {}
    
    public LittleLevelPackets(LittleLevel level, List<? extends Packet> packets) {
        super(level.key());
        this.packets = packets;
    }
    
    public LittleLevelPackets(LittleLevel level, Packet... packets) {
        super(level.key());
        this.packets = Arrays.asList(packets);
    }
    
    @Override
    public void execute(Player player, LittleLevelEntity entity) {
        LittlePlayerConnection.runInContext(entity.getSubLevel(), player, x -> {
            for (Packet packet : packets)
                packet.handle(x);
        });
    }
    
}
