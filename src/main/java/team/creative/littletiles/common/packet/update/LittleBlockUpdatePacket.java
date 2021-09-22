package team.creative.littletiles.common.packet.update;

import java.util.UUID;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.world.WorldAnimationHandler;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.core.BlockPos;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.network.CreativePacket;

public class LittleBlockUpdatePacket extends CreativePacket {
    
    public UUID uuid;
    public BlockState state;
    public BlockPos pos;
    public SPacketUpdateTileEntity packet;
    
    public LittleBlockUpdatePacket(World world, BlockPos pos, @Nullable TileEntity te) {
        this.pos = pos;
        this.state = world.getBlockState(pos);
        if (te != null)
            packet = te.getUpdatePacket();
        if (world instanceof CreativeWorld)
            uuid = ((CreativeWorld) world).parent.getUniqueID();
    }
    
    public LittleBlockUpdatePacket() {
        
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
        
        if (world instanceof WorldClient)
            ((WorldClient) world).invalidateRegionAndSetBlock(pos, state);
        else
            world.setBlockState(pos, state, 3);
        if (packet != null)
            packet.processPacket(((EntityPlayerSP) player).connection);
    }
    
    @Override
    public void executeServer(ServerPlayer player) {
        
    }
}
