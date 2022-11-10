package team.creative.littletiles.mixin;

import java.util.List;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.logging.LogUtils;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import team.creative.littletiles.common.packet.mc.PacketSetInterface;

@Mixin(targets = "net/minecraft/network/ConnectionProtocol$PacketSet")
public abstract class PacketSetMixin implements PacketSetInterface {
    
    @Shadow
    @Final
    public Object2IntMap<Class<? extends Packet>> classToId;
    
    @Shadow
    @Final
    private List<Function<FriendlyByteBuf, ? extends Packet>> idToDeserializer;
    
    @Override
    public <P extends Packet> void register(Class<P> clazz, Function<FriendlyByteBuf, P> func) {
        int i = this.idToDeserializer.size();
        int j = this.classToId.put(clazz, i);
        if (j != -1) {
            String s = "Packet " + clazz + " is already registered to ID " + j;
            LogUtils.getLogger().error(LogUtils.FATAL_MARKER, s);
            throw new IllegalArgumentException(s);
        }
        
        this.idToDeserializer.add(func);
    }
    
}
