package team.creative.littletiles.api.common.tool;

import java.util.function.Consumer;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.client.tool.LittleTool;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.tool.GuiConfigure;
import team.creative.littletiles.common.placement.setting.PlacementPlayerSetting;

public interface ILittleTool {
    
    public static boolean hasData(ItemStack stack) {
        var data = stack.get(LittleTilesRegistry.DATA.value());
        if (data != null)
            return !data.getUnsafe().isEmpty();
        return stack.get(DataComponents.CUSTOM_DATA) != null;
    }
    
    public static CompoundTag getData(ItemStack stack) {
        var data = stack.get(LittleTilesRegistry.DATA.value());
        if (data != null)
            return data.copyTag();
        else if (stack.has(DataComponents.CUSTOM_DATA)) {
            var oldData = stack.get(DataComponents.CUSTOM_DATA).copyTag();
            stack.remove(DataComponents.CUSTOM_DATA);
            setData(stack, oldData);
            return oldData;
        }
        return new CompoundTag();
    }
    
    @Deprecated
    public static void consumeUnsafeData(ItemStack stack, Consumer<CompoundTag> consumer) {
        var data = stack.get(LittleTilesRegistry.DATA.value());
        if (data != null)
            consumer.accept(data.getUnsafe());
        else if (stack.has(DataComponents.CUSTOM_DATA)) {
            var oldData = stack.get(DataComponents.CUSTOM_DATA).copyTag();
            stack.remove(DataComponents.CUSTOM_DATA);
            setData(stack, oldData);
            consumer.accept(oldData);
        }
    }
    
    public static void setData(ItemStack stack, CompoundTag nbt) {
        CustomData.set(LittleTilesRegistry.DATA.value(), stack, nbt);
    }
    
    public default LittleGrid getPositionGrid(Player player, ItemStack stack) {
        return PlacementPlayerSetting.grid(player);
    }
    
    public default GuiConfigure getConfigure(Player player, ContainerSlotView view, boolean secondary) {
        return null;
    }
    
    @OnlyIn(Dist.CLIENT)
    public default boolean isCorrectTool(ItemStack stack, LittleTool tool) {
        return true;
    }
    
    @OnlyIn(Dist.CLIENT)
    public LittleTool tool(ItemStack stack);
    
}
