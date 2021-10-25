package team.creative.littletiles.common.packet.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.common.api.tool.ILittleTool;

public class ConfigurePacket extends CreativePacket {
    
    public CompoundTag nbt;
    public Item item;
    
    public ConfigurePacket(ItemStack stack, CompoundTag nbt) {
        this.item = stack.getItem();
        this.nbt = nbt;
    }
    
    public ConfigurePacket() {}
    
    @Override
    public void executeClient(Player player) {}
    
    @Override
    public void executeServer(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() == item && item instanceof ILittleTool)
            ((ILittleTool) stack.getItem()).configured(stack, nbt);
    }
    
}
