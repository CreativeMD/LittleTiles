package team.creative.littletiles.common.api.tool;

import com.creativemd.littletiles.client.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.common.container.SubContainerConfigure;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.place.IMarkMode;
import com.creativemd.littletiles.common.util.place.MarkMode;
import com.creativemd.littletiles.common.util.place.PlacementPosition;
import com.creativemd.littletiles.common.util.place.PlacementPreview;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;

public interface ILittleTool {
    
    @OnlyIn(Dist.CLIENT)
    public default LittleGridContext getPositionContext(ItemStack stack) {
        return LittleGridContext.get();
    }
    
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
    public default IMarkMode onMark(EntityPlayer player, ItemStack stack, PlacementPosition position, RayTraceResult result, PlacementPreview previews) {
        if (previews != null)
            return new MarkMode(player, position, previews);
        return null;
    }
    
    @OnlyIn(Dist.CLIENT)
    public default void tick(EntityPlayer player, ItemStack stack, PlacementPosition position, RayTraceResult result) {}
    
    @OnlyIn(Dist.CLIENT)
    public default void render(EntityPlayer player, ItemStack stack, double x, double y, double z) {}
    
    @OnlyIn(Dist.CLIENT)
    public default void onDeselect(World world, ItemStack stack, EntityPlayer player) {}
    
    @OnlyIn(Dist.CLIENT)
    public default boolean onRightClick(World world, EntityPlayer player, ItemStack stack, PlacementPosition position, RayTraceResult result) {
        return true;
    }
    
    @OnlyIn(Dist.CLIENT)
    public default void onClickAir(EntityPlayer player, ItemStack stack) {}
    
    @OnlyIn(Dist.CLIENT)
    public default boolean onClickBlock(World world, EntityPlayer player, ItemStack stack, PlacementPosition position, RayTraceResult result) {
        return false;
    }
    
    @OnlyIn(Dist.CLIENT)
    public default boolean onMouseWheelClickBlock(World world, EntityPlayer player, ItemStack stack, PlacementPosition position, RayTraceResult result) {
        return false;
    }
}
