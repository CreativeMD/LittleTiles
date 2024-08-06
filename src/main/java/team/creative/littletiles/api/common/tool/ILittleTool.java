package team.creative.littletiles.api.common.tool;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.tool.GuiConfigure;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.mark.IMarkMode;
import team.creative.littletiles.common.placement.mark.MarkMode;
import team.creative.littletiles.common.placement.setting.PlacementPlayerSetting;

public interface ILittleTool {
    
    public static CompoundTag getData(ItemStack stack) {
        var data = stack.get(LittleTilesRegistry.DATA.value());
        if (data != null)
            return data.getUnsafe();
        return new CompoundTag();
    }
    
    public static void setData(ItemStack stack, CompoundTag nbt) {
        CustomData.set(LittleTilesRegistry.DATA.value(), stack, nbt);
    }
    
    public default LittleGrid getPositionGrid(Player player, ItemStack stack) {
        return PlacementPlayerSetting.grid(player);
    }
    
    public void rotate(Player player, ItemStack stack, Rotation rotation, boolean client);
    
    public void mirror(Player player, ItemStack stack, Axis axis, boolean client);
    
    public default void configured(ItemStack stack, CompoundTag nbt) {
        setData(stack, nbt);
    }
    
    public default GuiConfigure getConfigure(Player player, ContainerSlotView view) {
        return null;
    }
    
    public default boolean sendTransformationUpdate() {
        return true;
    }
    
    @OnlyIn(Dist.CLIENT)
    public default IMarkMode onMark(Player player, ItemStack stack, PlacementPosition position, BlockHitResult result, PlacementPreview previews) {
        if (previews != null)
            return new MarkMode(player, position, previews);
        return null;
    }
    
    @OnlyIn(Dist.CLIENT)
    public default void tick(Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {}
    
    @OnlyIn(Dist.CLIENT)
    public default void render(Player player, ItemStack stack, PoseStack pose) {}
    
    @OnlyIn(Dist.CLIENT)
    public default void onDeselect(Level level, ItemStack stack, Player player) {}
    
    @OnlyIn(Dist.CLIENT)
    public default boolean onRightClick(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        return true;
    }
    
    @OnlyIn(Dist.CLIENT)
    public default void onClickAir(Player player, ItemStack stack) {}
    
    @OnlyIn(Dist.CLIENT)
    public default boolean onClickBlock(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        return false;
    }
    
    @OnlyIn(Dist.CLIENT)
    public default boolean onMouseWheelClickBlock(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        return false;
    }
}
