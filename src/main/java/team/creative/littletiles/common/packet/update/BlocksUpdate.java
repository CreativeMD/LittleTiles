package team.creative.littletiles.common.packet.update;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.level.CreativeLevel;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.level.WorldAnimationHandler;

public class BlocksUpdate extends CreativePacket {
    
    public List<BlockPos> positions;
    public List<BlockState> states;
    public List<CompoundTag> tags;
    public UUID uuid;
    
    public BlocksUpdate(Level level, Set<BlockPos> positions) {
        this.positions = new ArrayList<>(positions.size());
        states = new ArrayList<>(positions.size());
        tags = new ArrayList<>(positions.size());
        
        for (BlockPos pos : positions) {
            this.positions.add(pos);
            states.add(level.getBlockState(pos));
            BlockEntity be = level.getBlockEntity(pos);
            if (be != null)
                tags.add(be.save(new CompoundTag()));
            else
                tags.add(null);
        }
        
        if (level instanceof CreativeLevel)
            uuid = ((CreativeLevel) level).parent.getUUID();
    }
    
    public BlocksUpdate(Level level, Iterable<? extends BlockEntity> blockEntities) {
        positions = new ArrayList<>();
        states = new ArrayList<>();
        tags = new ArrayList<>();
        
        for (BlockEntity be : blockEntities) {
            positions.add(be.getBlockPos());
            states.add(level.getBlockState(be.getBlockPos()));
            tags.add(be.save(new CompoundTag()));
        }
        
        if (level instanceof CreativeLevel)
            uuid = ((CreativeLevel) level).parent.getUUID();
    }
    
    public BlocksUpdate() {}
    
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
            if (level instanceof ClientLevel) {
                ((ClientLevel) level).setBlocksDirty(positions.get(i), states.get(i), states.get(i));
                if (tags.get(i) != null)
                    level.getBlockEntity(positions.get(i)).load(tags.get(i));
            } else {
                level.setBlock(positions.get(i), states.get(i), 3);
                if (tags.get(i) != null)
                    level.getBlockEntity(positions.get(i)).load(tags.get(i));
            }
            
        }
    }
    
    @Override
    public void executeServer(ServerPlayer player) {}
}