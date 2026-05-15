package team.creative.littletiles.common.packet.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.common.tool.ILittleMeasure;
import team.creative.littletiles.common.item.component.MeasurementTypeComponent;

public class MeasurementTypePacket extends CreativePacket {
    
    public MeasurementTypeComponent component;
    
    public MeasurementTypePacket(MeasurementTypeComponent component) {
        this.component = component;
    }
    
    public MeasurementTypePacket() {}
    
    @Override
    public void executeClient(Player player) {}
    
    @Override
    public void executeServer(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof ILittleMeasure)
            stack.set(LittleTilesRegistry.MEASUREMENT_TYPE, component);
    }
    
}
