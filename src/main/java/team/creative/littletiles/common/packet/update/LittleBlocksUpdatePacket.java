package team.creative.littletiles.common.packet.update;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.littletiles.common.world.WorldAnimationHandler;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.core.BlockPos;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.common.entity.EntityAnimation;

public class LittleBlocksUpdatePacket extends CreativePacket {
    
    public List<BlockPos> positions;
    public List<IBlockState> states;
    public List<SPacketUpdateTileEntity> packets;
    public UUID uuid;
    
    public LittleBlocksUpdatePacket(Level level, Iterable<? extends BlockEntity> blockEntities) {
        positions = new ArrayList<>();
        states = new ArrayList<>();
        packets = new ArrayList<>();
        
        for (BlockEntity be : blockEntities) {
            positions.add(te.getPos());
            states.add(world.getBlockState(te.getPos()));
            packets.add(te.getUpdatePacket());
        }
        
        if (world instanceof CreativeWorld)
            uuid = ((CreativeWorld) world).parent.getUniqueID();
    }
    
    public LittleBlocksUpdatePacket() {
        
    }
    
    @Override
    public void executeClient(Player player) {
        World world = player.world;
        
        if (uuid != null) {
            EntityAnimation animation = WorldAnimationHandler.findAnimation(true, uuid);
            if (animation == null)
                return;
            
            world = animation.fakeWorld;
        }
        
        for (int i = 0; i < positions.size(); i++) {
            if (world instanceof WorldClient) {
                ((WorldClient) world).invalidateRegionAndSetBlock(positions.get(i), states.get(i));
                if (packets.get(i) != null)
                    ((EntityPlayerSP) player).connection.handleUpdateTileEntity(packets.get(i));
            } else {
                world.setBlockState(positions.get(i), states.get(i), 3);
                TileEntity te = world.getTileEntity(positions.get(i));
                if (packets.get(i) != null)
                    te.onDataPacket(((EntityPlayerSP) player).connection.getNetworkManager(), packets.get(i));
            }
            
        }
    }
    
    @Override
    public void executeServer(ServerPlayer player) {
        
    }
}
