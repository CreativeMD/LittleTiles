package team.creative.littletiles.common.packet.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import team.creative.littletiles.common.entity.level.LittleEntity;

public class LittleEntityPhysicPacket extends LittleEntityPacket<LittleEntity> {
    
    public CompoundTag extraData;
    
    public LittleEntityPhysicPacket() {}
    
    public LittleEntityPhysicPacket(LittleEntity entity) {
        super(entity);
        this.extraData = entity.physic.save();
    }
    
    @Override
    public void execute(Player player, LittleEntity entity) {
        requiresClient(player);
        entity.physic.load(extraData);
    }
    
}
