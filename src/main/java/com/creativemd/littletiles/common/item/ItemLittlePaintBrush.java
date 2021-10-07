package com.creativemd.littletiles.common.item;

import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.gui.opener.GuiHandler;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.common.utils.tooltip.TooltipUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.LittleTilesClient;
import com.creativemd.littletiles.client.gui.SubGuiColorTube;
import com.creativemd.littletiles.client.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.client.gui.configure.SubGuiGridSelector;
import com.creativemd.littletiles.client.render.overlay.PreviewRenderer;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.block.LittleActionColorBoxes;
import com.creativemd.littletiles.common.action.block.LittleActionColorBoxes.LittleActionColorBoxesFiltered;
import com.creativemd.littletiles.common.api.ILittleEditor;
import com.creativemd.littletiles.common.container.SubContainerConfigure;
import com.creativemd.littletiles.common.packet.LittleBlockPacket;
import com.creativemd.littletiles.common.packet.LittleBlockPacket.BlockPacketAction;
import com.creativemd.littletiles.common.packet.LittleVanillaBlockPacket;
import com.creativemd.littletiles.common.packet.LittleVanillaBlockPacket.VanillaBlockAction;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.place.IMarkMode;
import com.creativemd.littletiles.common.util.place.PlacementPosition;
import com.creativemd.littletiles.common.util.place.PlacementPreview;
import com.creativemd.littletiles.common.util.selection.selector.TileSelector;
import com.creativemd.littletiles.common.util.shape.LittleShape;
import com.creativemd.littletiles.common.util.shape.ShapeRegistry;
import com.creativemd.littletiles.common.util.shape.ShapeSelection;
import com.creativemd.littletiles.common.util.tooltip.IItemTooltip;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemLittlePaintBrush extends Item implements ILittleEditor, IItemTooltip {
    
    public static ShapeSelection selection;
    
    public ItemLittlePaintBrush() {
        setCreativeTab(LittleTiles.littleTab);
        hasSubtypes = true;
        setMaxStackSize(1);
    }
    
    public static int getColor(ItemStack stack) {
        if (stack == null)
            return ColorUtils.WHITE;
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        if (!stack.getTagCompound().hasKey("color"))
            setColor(stack, ColorUtils.WHITE);
        return stack.getTagCompound().getInteger("color");
    }
    
    public static void setColor(ItemStack stack, int color) {
        if (stack == null)
            return;
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setInteger("color", color);
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
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add("click: dyes a tile");
        tooltip.add("shift+click: copies tile's color");
        LittleShape shape = getShape(stack);
        tooltip.add("shape: " + (shape == null ? "tile" : shape.getKey()));
        tooltip.add(TooltipUtils.printRGB(getColor(stack)));
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
    @SideOnly(Side.CLIENT)
    public SubGuiConfigure getConfigureGUI(EntityPlayer player, ItemStack stack) {
        return new SubGuiColorTube(stack);
    }
    
    @Override
    public SubContainerConfigure getConfigureContainer(EntityPlayer player, ItemStack stack) {
        return new SubContainerConfigure(player, stack);
    }
    
    public static LittleShape getShape(ItemStack stack) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        String shape = stack.getTagCompound().getString("shape");
        if (shape.equals("tile") || shape.equals(""))
            return ShapeRegistry.tileShape;
        return ShapeRegistry.getShape(shape);
    }
    
    @Override
    public void onDeselect(World world, ItemStack stack, EntityPlayer player) {
        selection = null;
    }
    
    @Override
    public boolean hasCustomBoxes(World world, ItemStack stack, EntityPlayer player, IBlockState state, PlacementPosition pos, RayTraceResult result) {
        return LittleAction.isBlockValid(state) || world.getTileEntity(result.getBlockPos()) instanceof TileEntityLittleTiles;
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
    public boolean onRightClick(World world, EntityPlayer player, ItemStack stack, PlacementPosition position, RayTraceResult result) {
        if (selection != null)
            selection.click(player);
        return true;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean onClickBlock(World world, EntityPlayer player, ItemStack stack, PlacementPosition position, RayTraceResult result) {
        if (LittleAction.isUsingSecondMode(player)) {
            selection = null;
            PreviewRenderer.marked = null;
        } else if (selection != null)
            if (selection.addAndCheckIfPlace(player, position, result)) {
                if (ItemLittleHammer.isFiltered())
                    new LittleActionColorBoxesFiltered(selection.getBoxes(false), getColor(stack), false, ItemLittleHammer.getFilter()).execute();
                else
                    new LittleActionColorBoxes(selection.getBoxes(false), getColor(stack), false).execute();
                selection = null;
                PreviewRenderer.marked = null;
            }
        return false;
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
    public LittleGridContext getPositionContext(ItemStack stack) {
        return ItemMultiTiles.currentContext;
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
    public boolean onMouseWheelClickBlock(World world, EntityPlayer player, ItemStack stack, PlacementPosition position, RayTraceResult result) {
        TileEntity tileEntity = world.getTileEntity(result.getBlockPos());
        if (tileEntity instanceof TileEntityLittleTiles)
            PacketHandler.sendPacketToServer(new LittleBlockPacket(world, result.getBlockPos(), player, BlockPacketAction.COLOR_TUBE, new NBTTagCompound()));
        else
            PacketHandler.sendPacketToServer(new LittleVanillaBlockPacket(result.getBlockPos(), VanillaBlockAction.COLOR_TUBE));
        return true;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiConfigure getConfigureGUIAdvanced(EntityPlayer player, ItemStack stack) {
        return new SubGuiGridSelector(stack, ItemMultiTiles.currentContext, ItemLittleHammer.isFiltered(), ItemLittleHammer.getFilter()) {
            
            @Override
            public void saveConfiguration(LittleGridContext context, boolean activeFilter, TileSelector selector) {
                ItemLittleHammer.setFilter(activeFilter, selector);
                if (selection != null)
                    selection.convertTo(context);
                ItemMultiTiles.currentContext = context;
            }
        };
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { getShape(stack).getLocalizedName(), Minecraft.getMinecraft().gameSettings.keyBindPickBlock.getDisplayName(), LittleTilesClient.mark.getDisplayName(),
                LittleTilesClient.configure.getDisplayName(),
                LittleTilesClient.configureAdvanced.getDisplayName() };
    }
}
