package team.creative.littletiles.common.packet.structure;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.littletiles.client.render.level.RenderUploader;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;
import team.creative.littletiles.common.packet.entity.LittleEntityPacket;

public class StructureEntityToBlockPacket extends LittleEntityPacket<LittleAnimationEntity> {
    
    public StructureEntityToBlockPacket() {}
    
    public StructureEntityToBlockPacket(LittleAnimationEntity entity) {
        super(entity);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void execute(Player player, LittleAnimationEntity entity) {
        requiresClient(player);
        RenderUploader.queue(entity.level, entity);
    }
    
}
