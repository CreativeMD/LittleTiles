package team.creative.littletiles.common.packet.update;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.level.CreativeLevel;
import team.creative.creativecore.common.network.CanBeNull;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.level.WorldAnimationHandler;

public class BlockUpdate extends CreativePacket {
    
    public UUID uuid;
    public BlockState state;
    public BlockPos pos;
    @CanBeNull
    public CompoundTag tag;
    
    public BlockUpdate(Level level, BlockPos pos, @Nullable BlockEntity be) {
        this.pos = pos;
        this.state = level.getBlockState(pos);
        if (be != null)
            tag = be.save(new CompoundTag());
        if (level instanceof CreativeLevel)
            uuid = ((CreativeLevel) level).parent.getUUID();
    }
    
    public BlockUpdate() {}
    
    @Override
    public void executeClient(Player player) {
        Level level = player.level;
        
        if (uuid != null) {
            EntityAnimation animation = WorldAnimationHandler.findAnimation(true, uuid);
            if (animation == null)
                return;
            
            level = animation.fakeWorld;
        }
        
        if (level instanceof ClientLevel)
            ((ClientLevel) level).setBlocksDirty(pos, state, state);
        else
            level.setBlock(pos, state, 3);
        if (tag != null)
            level.getBlockEntity(pos).load(tag);
    }
    
    @Override
    public void executeServer(ServerPlayer player) {}
}
