package team.creative.littletiles.common.item;

import java.util.List;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.creativemd.creativecore.common.gui.opener.GuiHandler;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.selection.selector.TileSelector;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.core.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActionDestroyBoxes;
import team.creative.littletiles.common.action.LittleActionDestroyBoxes.LittleActionDestroyBoxesFiltered;
import team.creative.littletiles.common.api.tool.ILittleEditor;
import team.creative.littletiles.common.gui.SubContainerConfigure;
import team.creative.littletiles.common.gui.SubGuiHammer;
import team.creative.littletiles.common.gui.configure.SubGuiConfigure;
import team.creative.littletiles.common.gui.configure.SubGuiGridSelector;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.mark.IMarkMode;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeRegistry;
import team.creative.littletiles.common.placement.shape.ShapeSelection;

public class ItemLittleHammer extends Item implements ILittleEditor, IItemTooltip {
    
    private static boolean activeFilter = false;
    private static TileSelector currentFilter = null;
    public static ShapeSelection selection;
    
    public static boolean isFiltered() {
        return activeFilter;
    }
    
    public static void setFilter(boolean active, TileSelector filter) {
        activeFilter = active;
        currentFilter = filter;
    }
    
    public static TileSelector getFilter() {
        return currentFilter;
    }
    
    public ItemLittleHammer() {
        setCreativeTab(LittleTiles.littleTab);
        hasSubtypes = true;
        setMaxStackSize(1);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add("can be used to chisel blocks");
        LittleShape shape = getShape(stack);
        tooltip.add("mode: " + shape.getKey());
        shape.addExtraInformation(stack.getTagCompound(), tooltip);
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (hand == EnumHand.OFF_HAND)
            return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand));
        if (!world.isRemote)
            GuiHandler.openGuiItem(player, world);
        return new ActionResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }
    
    @Override
    public LittleBoxes getBoxes(World world, ItemStack stack, EntityPlayer player, PlacementPosition pos, RayTraceResult result) {
        if (selection == null)
            selection = new ShapeSelection(stack, true);
        selection.setLast(player, stack, pos, result);
        return selection.getBoxes(true);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean onClickBlock(World world, EntityPlayer player, ItemStack stack, PlacementPosition position, RayTraceResult result) {
        if (LittleAction.isUsingSecondMode(player)) {
            selection = null;
            PreviewRenderer.marked = null;
        } else if (selection != null)
            if (selection.addAndCheckIfPlace(player, position, result)) {
                if (isFiltered())
                    new LittleActionDestroyBoxesFiltered(selection.getBoxes(false), getFilter()).execute();
                else
                    new LittleActionDestroyBoxes(selection.getBoxes(false)).execute();
                selection = null;
            }
        return false;
    }
    
    @Override
    public boolean canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, EntityPlayer player) {
        return false;
    }
    
    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        return 0F;
    }
    
    @Override
    public void onDeselect(World world, ItemStack stack, EntityPlayer player) {
        if (selection != null)
            selection = null;
    }
    
    @Override
    public boolean hasCustomBoxes(World world, ItemStack stack, EntityPlayer player, IBlockState state, PlacementPosition pos, RayTraceResult result) {
        return LittleAction.isBlockValid(state) || world.getTileEntity(result.getBlockPos()) instanceof TileEntityLittleTiles;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiConfigure getConfigureGUI(EntityPlayer player, ItemStack stack) {
        return new SubGuiHammer(stack);
    }
    
    @Override
    public SubContainerConfigure getConfigureContainer(EntityPlayer player, ItemStack stack) {
        return new SubContainerConfigure(player, stack);
    }
    
    @Override
    public void rotate(EntityPlayer player, ItemStack stack, Rotation rotation, boolean client) {
        if (client && selection != null)
            selection.rotate(player, stack, rotation);
        else
            new ShapeSelection(stack, false).rotate(player, stack, rotation);
    }
    
    @Override
    public void flip(EntityPlayer player, ItemStack stack, Axis axis, boolean client) {
        if (client && selection != null)
            selection.flip(player, stack, axis);
        else
            new ShapeSelection(stack, false).flip(player, stack, axis);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public IMarkMode onMark(EntityPlayer player, ItemStack stack, PlacementPosition position, RayTraceResult result, PlacementPreview previews) {
        if (selection != null)
            selection.toggleMark();
        return selection;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiConfigure getConfigureGUIAdvanced(EntityPlayer player, ItemStack stack) {
        return new SubGuiGridSelector(stack, ItemMultiTiles.currentContext, isFiltered(), getFilter()) {
            
            @Override
            public void saveConfiguration(LittleGridContext context, boolean activeFilter, TileSelector selector) {
                setFilter(activeFilter, selector);
                if (selection != null)
                    selection.convertTo(context);
                ItemMultiTiles.currentContext = context;
            }
        };
    }
    
    public static LittleShape getShape(ItemStack stack) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        
        return ShapeRegistry.getShape(stack.getTagCompound().getString("shape"));
    }
    
    @Override
    public LittleGridContext getPositionContext(ItemStack stack) {
        return ItemMultiTiles.currentContext;
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { getShape(stack).getLocalizedName(), LittleTilesClient.mark.getDisplayName(), LittleTilesClient.configure
                .getDisplayName(), LittleTilesClient.configureAdvanced.getDisplayName() };
    }
}
