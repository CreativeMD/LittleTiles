package team.creative.littletiles.common.packet.update;

import java.util.List;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.creativecore.common.network.CanBeNull;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.entity.LittleLevelEntity;
import team.creative.littletiles.common.level.LittleAnimationHandlers;

public class NeighborUpdate extends CreativePacket {
    
    @CanBeNull
    public UUID uuid;
    public List<BlockPos> positions;
    
    public NeighborUpdate(Level level, List<BlockPos> positions) {
        this.positions = positions;
        if (level instanceof ISubLevel subLevel)
            uuid = subLevel.getHolder().getUUID();
    }
    
    public NeighborUpdate() {}
    
    @Override
    public void executeClient(Player player) {
        Level level = player.level;
        
        if (uuid != null) {
            LittleLevelEntity entity = LittleAnimationHandlers.find(true, uuid);
            if (entity == null)
                return;
            
            level = entity.getFakeLevel();
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
