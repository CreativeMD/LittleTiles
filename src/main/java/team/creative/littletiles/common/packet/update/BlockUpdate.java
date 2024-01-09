package team.creative.littletiles.common.packet.update;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.creativecore.common.network.CanBeNull;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.entity.LittleEntity;

public class BlockUpdate extends CreativePacket {
    
    @CanBeNull
    public UUID uuid;
    public BlockState state;
    public BlockPos pos;
    @CanBeNull
    public CompoundTag tag;
    
    public BlockUpdate(LevelAccessor level, BlockPos pos, @Nullable BlockEntity be) {
        this.pos = pos;
        this.state = level.getBlockState(pos);
        if (be != null)
            tag = be.saveWithoutMetadata();
        if (level instanceof ISubLevel subLevel)
            uuid = subLevel.getHolder().getUUID();
    }
    
    public BlockUpdate() {}
    
    @Override
    public void executeClient(Player player) {
        LevelAccessor level = player.level();
        
        if (uuid != null) {
            LittleEntity entity = LittleTiles.ANIMATION_HANDLERS.find(true, uuid);
            if (entity == null)
                return;
            
            level = entity.getSubLevel();
        }
        
        if (level instanceof ClientLevel c)
            c.setBlocksDirty(pos, state, state);
        else
            level.setBlock(pos, state, 3);
        if (tag != null)
            level.getBlockEntity(pos).load(tag);
    }
    
    @Override
    public void executeServer(ServerPlayer player) {}
}
