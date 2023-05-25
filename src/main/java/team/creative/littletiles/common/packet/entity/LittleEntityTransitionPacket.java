package team.creative.littletiles.common.packet.entity;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.network.CanBeNull;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.level.little.LittleLevelTransitionManager;
import team.creative.littletiles.common.level.little.LittleSubLevel;

public class LittleEntityTransitionPacket extends CreativePacket {
    
    public UUID uuid;
    @CanBeNull
    public UUID targetLevel;
    
    public LittleEntityTransitionPacket() {}
    
    public LittleEntityTransitionPacket(UUID uuid, Level level) {
        this.uuid = uuid;
        if (level instanceof LittleSubLevel subLevel)
            this.targetLevel = subLevel.key();
    }
    
    public LittleEntityTransitionPacket(Entity entity, Level level) {
        this(entity.getUUID(), level);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void executeClient(Player player) {
        LittleEntity entity = LittleTiles.ANIMATION_HANDLERS.find(true, targetLevel);
        Level level;
        if (entity == null)
            level = Minecraft.getInstance().level;
        else
            level = (Level) entity.getSubLevel();
        
        Entity target = LittleLevelTransitionManager.findEntity(uuid);
        if (target != null)
            LittleTilesClient.ANIMATION_HANDLER.queueEntityForTransition(target, level);
    }
    
    @Override
    public void executeServer(ServerPlayer player) {
        requiresClient(player);
    }
    
}
