package team.creative.littletiles.common.packet;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tile.math.location.StructureLocation;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import team.creative.littletiles.common.action.LittleActionException;

public class LittleUpdateStructurePacket extends CreativeCorePacket {
    
    public StructureLocation location;
    public NBTTagCompound structureNBT;
    
    public LittleUpdateStructurePacket() {
        
    }
    
    public LittleUpdateStructurePacket(StructureLocation location, NBTTagCompound structureNBT) {
        this.location = location;
        this.structureNBT = structureNBT;
    }
    
    @Override
    public void writeBytes(ByteBuf buf) {
        LittleAction.writeStructureLocation(location, buf);
        writeNBT(buf, structureNBT);
    }
    
    @Override
    public void readBytes(ByteBuf buf) {
        location = LittleAction.readStructureLocation(buf);
        structureNBT = readNBT(buf);
    }
    
    @Override
    public void executeClient(EntityPlayer player) {
        try {
            LittleStructure structure = location.find(player.world);
            structure.mainBlock.getTe().updateTiles(x -> x.get(structure.mainBlock).setStructureNBT(structureNBT));
        } catch (LittleActionException e) {}
    }
    
    @Override
    public void executeServer(EntityPlayer player) {
        try {
            LittleStructure structure = location.find(player.world);
            NBTTagCompound nbt = new NBTTagCompound();
            structure.writeToNBT(nbt);
            PacketHandler.sendPacketToPlayer(new LittleUpdateStructurePacket(location, nbt), (EntityPlayerMP) player);
        } catch (LittleActionException e) {}
        
    }
    
}
