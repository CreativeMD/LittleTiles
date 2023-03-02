package team.creative.littletiles.common.packet.entity.animation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;
import team.creative.littletiles.common.packet.entity.LittleEntityPacket;
import team.creative.littletiles.common.structure.relative.StructureAbsolute;

public class LittleAnimationInitPacket extends LittleEntityPacket<LittleAnimationEntity> {
    
    public StructureAbsolute absolute;
    public CompoundTag extraData;
    
    public LittleAnimationInitPacket() {}
    
    public LittleAnimationInitPacket(LittleAnimationEntity entity) {
        super(entity);
        this.absolute = entity.getCenter();
        this.extraData = entity.saveExtraClientData();
        // TODO Add level information
    }
    
    @Override
    public void execute(Player player, LittleAnimationEntity entity) {
        requiresClient(player);
        
        entity.initSubLevelClient(absolute, extraData);
    }
    
}
