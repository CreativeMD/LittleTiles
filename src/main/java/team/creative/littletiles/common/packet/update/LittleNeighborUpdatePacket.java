package team.creative.littletiles.common.packet.update;

import java.util.List;
import java.util.UUID;

import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.world.WorldAnimationHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.level.CreativeLevel;
import team.creative.creativecore.common.network.CanBeNull;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.common.block.BlockTile;

public class LittleNeighborUpdatePacket extends CreativePacket {
    
    @CanBeNull
    public UUID uuid;
    public List<BlockPos> positions;
    
    public LittleNeighborUpdatePacket(Level level, List<BlockPos> positions) {
        this.positions = positions;
        if (level instanceof CreativeLevel)
            uuid = ((CreativeLevel) level).parent.getUUID();
    }
    
    public LittleNeighborUpdatePacket() {
        
    }
    
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
    public void executeServer(ServerPlayer player) {
        
    }
    
}
