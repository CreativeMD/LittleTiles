package team.creative.littletiles.client.level.little;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientLevel.ClientLevelData;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import team.creative.littletiles.mixin.ClientPacketListenerAccessor;

public class LittleClientPacketListener extends ClientPacketListener {
    
    public LittleClientPacketListener(Minecraft mc, Connection con) {
        super(mc, null, con, null, null);
    }
    
    public void init(Minecraft mc, ClientLevel level, ClientLevelData data, Connection con) {
        ((ClientPacketListenerAccessor) this).setMinecraft(mc);
        ((ClientPacketListenerAccessor) this).setConnection(con);
        ((ClientPacketListenerAccessor) this).setLevel(level);
        ((ClientPacketListenerAccessor) this).setLevelData(data);
    }
    
}
