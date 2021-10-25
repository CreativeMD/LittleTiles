package team.creative.littletiles.common.packet.update;

import java.util.List;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.level.CreativeLevel;
import team.creative.creativecore.common.network.CanBeNull;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.level.WorldAnimationHandler;

public class NeighborUpdate extends CreativePacket {
    
    @CanBeNull
    public UUID uuid;
    public List<BlockPos> positions;
    
    public NeighborUpdate(Level level, List<BlockPos> positions) {
        this.positions = positions;
        if (level instanceof CreativeLevel)
            uuid = ((CreativeLevel) level).parent.getUUID();
    }
    
    public NeighborUpdate() {}
    
    @Override
    public void executeClient(Player player) {
        Level level = player.level;
        
        if (uuid != null) {
            EntityAnimation animation = WorldAnimationHandler.findAnimation(true, uuid);
            if (animation == null)
                return;
            
            level = animation.fakeWorld;
        }
        
        for (int i = 0; i < positions.size(); i++) {
            BlockPos pos = positions.get(i);
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof BlockTile)
                state.neighborChanged(level, pos, state.getBlock(), pos, false);
        }
        
    }
    
    @Override
    public void executeServer(ServerPlayer player) {}
    
}
