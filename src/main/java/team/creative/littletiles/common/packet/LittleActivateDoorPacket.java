package team.creative.littletiles.common.packet;

import java.util.UUID;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.world.SubWorld;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.math.location.StructureLocation;
import team.creative.littletiles.common.packet.update.StructureUpdate;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.type.door.LittleDoor;
import team.creative.littletiles.common.structure.type.door.LittleDoor.DoorActivator;
import team.creative.littletiles.common.structure.type.door.LittleDoor.StillInMotionException;

public class LittleActivateDoorPacket extends CreativeCorePacket {
    
    public StructureLocation location;
    public DoorActivator activator;
    public UUID uuid;
    
    public LittleActivateDoorPacket(DoorActivator activator, StructureLocation location, UUID uuid) {
        this.location = location;
        this.activator = activator;
        this.uuid = uuid;
    }
    
    public LittleActivateDoorPacket() {
        
    }
    
    @Override
    public void writeBytes(ByteBuf buf) {
        LittleAction.writeStructureLocation(location, buf);
        writeString(buf, uuid.toString());
        buf.writeInt(activator.ordinal());
    }
    
    @Override
    public void readBytes(ByteBuf buf) {
        location = LittleAction.readStructureLocation(buf);
        uuid = UUID.fromString(readString(buf));
        activator = DoorActivator.values()[buf.readInt()];
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void executeClient(EntityPlayer player) { // Note it does not take care of synchronization, just sends an activation to the client, only used for already animated doors		
        try {
            LittleStructure structure = location.find(player.world);
            if (structure instanceof LittleDoor) {
                LittleDoor door = (LittleDoor) structure;
                door.activate(activator, null, uuid, false);
            }
            
        } catch (StillInMotionException e) {
            PacketHandler.sendPacketToServer(new LittleEntityFixControllerPacket(uuid, new NBTTagCompound()));
        } catch (LittleActionException e) {
            PacketHandler.sendPacketToServer(new StructureUpdate(location, new NBTTagCompound()));
        }
        
    }
    
    @Override
    public void executeServer(EntityPlayer player) {
        LittleTile tile;
        EntityAnimation animation = null;
        
        try {
            LittleStructure structure = location.find(player.world);
            if (structure instanceof LittleDoor && !structure.mainBlock.isRemoved()) {
                LittleDoor door = (LittleDoor) structure;
                World world = door.getWorld();
                if (world instanceof SubWorld)
                    animation = (EntityAnimation) ((SubWorld) world).parent;
                
                door.activate(activator, player, uuid, true);
            }
        } catch (LittleActionException e) {
            if (animation != null)
                PacketHandler
                    .sendPacketToPlayer(new LittleEntityRequestPacket(animation.getUniqueID(), animation.writeToNBT(new NBTTagCompound()), false), (EntityPlayerMP) player);
            else
                PacketHandler.sendPacketToPlayer(new LittleResetAnimationPacket(uuid), (EntityPlayerMP) player);
            
        }
    }
    
}
