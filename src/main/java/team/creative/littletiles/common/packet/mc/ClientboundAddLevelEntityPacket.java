package team.creative.littletiles.common.packet.mc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import team.creative.littletiles.common.entity.level.LittleLevelEntity;

public class ClientboundAddLevelEntityPacket extends ClientboundAddEntityPacket {
    
    private final CompoundTag nbt;
    
    public ClientboundAddLevelEntityPacket(LittleLevelEntity entity) {
        super(entity.getId(), entity.getUUID(), entity.getX(), entity.getY(), entity.getZ(), entity.getXRot(), entity.getYRot(), entity.getType(), 0, entity.getDeltaMovement(), 0);
        this.nbt = new CompoundTag();
        entity.addAdditionalSaveData(nbt);
    }
    
    public ClientboundAddLevelEntityPacket(FriendlyByteBuf buf) {
        super(buf);
        this.nbt = buf.readAnySizeNbt();
    }
    
    @Override
    public void write(FriendlyByteBuf buf) {
        super.write(buf);
        buf.writeNbt(nbt);
    }
    
    public CompoundTag getNbt() {
        return nbt;
    }
}