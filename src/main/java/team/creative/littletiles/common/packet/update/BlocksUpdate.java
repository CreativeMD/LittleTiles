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
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.creativecore.common.network.CanBeNull;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.entity.LittleEntity;

public class BlocksUpdate extends CreativePacket {
    
    public List<BlockPos> positions;
    public List<BlockState> states;
    public List<CompoundTag> tags;
    @CanBeNull
    public UUID uuid;
    
    public BlocksUpdate(LevelAccessor level, Set<BlockPos> positions) {
        this.positions = new ArrayList<>(positions.size());
        states = new ArrayList<>(positions.size());
        tags = new ArrayList<>(positions.size());
        
        for (BlockPos pos : positions) {
            this.positions.add(pos);
            states.add(level.getBlockState(pos));
            BlockEntity be = level.getBlockEntity(pos);
            if (be != null)
                tags.add(be.saveWithoutMetadata());
            else
                tags.add(null);
        }
        
        if (level instanceof ISubLevel subLevel)
            uuid = subLevel.getHolder().getUUID();
    }
    
    public BlocksUpdate(LevelAccessor level, Iterable<? extends BlockEntity> blockEntities) {
        positions = new ArrayList<>();
        states = new ArrayList<>();
        tags = new ArrayList<>();
        
        for (BlockEntity be : blockEntities) {
            positions.add(be.getBlockPos());
            states.add(level.getBlockState(be.getBlockPos()));
            tags.add(be.saveWithoutMetadata());
        }
        
        if (level instanceof ISubLevel subLevel)
            uuid = subLevel.getHolder().getUUID();
    }
    
    public BlocksUpdate() {}
    
    @Override
    public void executeClient(Player player) {
        LevelAccessor level = player.level();
        
        if (uuid != null) {
            LittleEntity entity = LittleTiles.ANIMATION_HANDLERS.find(true, uuid);
            if (entity == null)
                return;
            
            level = entity.getSubLevel();
        }
        
        for (int i = 0; i < positions.size(); i++) {
            if (level instanceof ClientLevel c) {
                c.setBlocksDirty(positions.get(i), states.get(i), states.get(i));
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
