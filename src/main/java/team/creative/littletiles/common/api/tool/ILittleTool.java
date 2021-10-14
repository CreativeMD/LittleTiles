package team.creative.littletiles.common.api.tool;

import com.creativemd.littletiles.client.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.common.container.SubContainerConfigure;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.placement.IMarkMode;
import team.creative.littletiles.common.placement.MarkMode;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.PlacementPreview;

public interface ILittleTool {
    
    @OnlyIn(Dist.CLIENT)
    public LittleGrid getPositionGrid(ItemStack stack);
    
    public void rotate(Player player, ItemStack stack, Rotation rotation, boolean client);
    
    public void flip(Player player, ItemStack stack, Axis axis, boolean client);
    
    @OnlyIn(Dist.CLIENT)
    public default SubGuiConfigure getConfigureGUI(Player player, ItemStack stack) {
        return null;
    }
    
    public default SubContainerConfigure getConfigureContainer(Player player, ItemStack stack) {
        return new SubContainerConfigure(player, stack);
    }
    
    @OnlyIn(Dist.CLIENT)
    public default SubGuiConfigure getConfigureGUIAdvanced(Player player, ItemStack stack) {
        return null;
    }
    
    public default SubContainerConfigure getConfigureContainerAdvanced(Player player, ItemStack stack) {
        return new SubContainerConfigure(player, stack);
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
    public default void render(Player player, ItemStack stack, double x, double y, double z) {}
    
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
