package com.creativemd.littletiles.common.item;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.util.Color;

import com.creativemd.creativecore.client.rendering.RenderBox;
import com.creativemd.creativecore.client.rendering.model.CreativeBakedModel;
import com.creativemd.creativecore.client.rendering.model.ICreativeRendered;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.common.utils.tooltip.TooltipUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.LittleTilesClient;
import com.creativemd.littletiles.client.gui.SubGuiChisel;
import com.creativemd.littletiles.client.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.client.gui.configure.SubGuiModeSelector;
import com.creativemd.littletiles.client.render.overlay.PreviewRenderer;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.api.ILittlePlacer;
import com.creativemd.littletiles.common.block.BlockTile;
import com.creativemd.littletiles.common.container.SubContainerConfigure;
import com.creativemd.littletiles.common.packet.LittleBlockPacket;
import com.creativemd.littletiles.common.packet.LittleBlockPacket.BlockPacketAction;
import com.creativemd.littletiles.common.packet.LittleVanillaBlockPacket;
import com.creativemd.littletiles.common.packet.LittleVanillaBlockPacket.VanillaBlockAction;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.LittleTileColored;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tile.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tile.registry.LittleTileRegistry;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.place.IMarkMode;
import com.creativemd.littletiles.common.util.place.PlacementMode;
import com.creativemd.littletiles.common.util.place.PlacementPosition;
import com.creativemd.littletiles.common.util.place.PlacementPreview;
import com.creativemd.littletiles.common.util.shape.LittleShape;
import com.creativemd.littletiles.common.util.shape.ShapeRegistry;
import com.creativemd.littletiles.common.util.shape.ShapeSelection;
import com.creativemd.littletiles.common.util.tooltip.IItemTooltip;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemLittleChisel extends Item implements ICreativeRendered, ILittlePlacer, IItemTooltip {
    
    public static ShapeSelection selection;
    
    public ItemLittleChisel() {
        setCreativeTab(LittleTiles.littleTab);
        hasSubtypes = true;
        setMaxStackSize(1);
    }
    
    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        return 0F;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        LittleShape shape = getShape(stack);
        tooltip.add("shape: " + shape.getKey());
        shape.addExtraInformation(stack.getTagCompound(), tooltip);
        LittlePreview preview = ItemLittleGrabber.SimpleMode.getPreview(stack);
        tooltip.add(TooltipUtils.printRGB(preview.hasColor() ? preview.getColor() : ColorUtils.WHITE));
    }
    
    @Override
    public boolean canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, EntityPlayer player) {
        return false;
    }
    
    public static LittleShape getShape(ItemStack stack) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        
        return ShapeRegistry.getShape(stack.getTagCompound().getString("shape"));
    }
    
    public static void setShape(ItemStack stack, LittleShape shape) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        
        stack.getTagCompound().setString("shape", shape.getKey());
    }
    
    public static LittlePreview getPreview(ItemStack stack) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        
        if (stack.getTagCompound().hasKey("preview"))
            return LittleTileRegistry.loadPreview(stack.getTagCompound().getCompoundTag("preview"));
        
        IBlockState state = stack.getTagCompound().hasKey("state") ? Block.getStateById(stack.getTagCompound().getInteger("state")) : Blocks.STONE.getDefaultState();
        LittleTile tile = stack.getTagCompound().hasKey("color") ? new LittleTileColored(state.getBlock(), state.getBlock().getMetaFromState(state), stack.getTagCompound()
            .getInteger("color")) : new LittleTile(state.getBlock(), state.getBlock().getMetaFromState(state));
        
        LittleGridContext context = LittleGridContext.get();
        tile.setBox(new LittleBox(0, 0, 0, context.size, context.size, context.size));
        LittlePreview preview = tile.getPreviewTile();
        setPreview(stack, preview);
        return preview;
    }
    
    public static void setPreview(ItemStack stack, LittlePreview preview) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        
        NBTTagCompound nbt = new NBTTagCompound();
        preview.writeToNBT(nbt);
        stack.getTagCompound().setTag("preview", nbt);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public List<RenderBox> getRenderingCubes(IBlockState state, TileEntity te, ItemStack stack) {
        return Collections.emptyList();
    }
    
    @SideOnly(Side.CLIENT)
    public static IBakedModel model;
    
    @Override
    @SideOnly(Side.CLIENT)
    public void applyCustomOpenGLHackery(ItemStack stack, TransformType cameraTransformType) {
        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.pushMatrix();
        
        if (model == null)
            model = mc.getRenderItem().getItemModelMesher().getModelManager().getModel(new ModelResourceLocation(LittleTiles.modid + ":chisel_background", "inventory"));
        ForgeHooksClient
            .handleCameraTransforms(model, cameraTransformType, cameraTransformType == TransformType.FIRST_PERSON_LEFT_HAND || cameraTransformType == TransformType.THIRD_PERSON_LEFT_HAND);
        
        mc.getRenderItem().renderItem(new ItemStack(Items.PAPER), model);
        
        if (cameraTransformType == TransformType.GUI) {
            GlStateManager.translate(0.1, 0.1, 0);
            GlStateManager.scale(0.7, 0.7, 0.7);
            
            LittlePreview preview = getPreview(stack);
            ItemStack blockStack = new ItemStack(preview.getBlock(), 1, preview.getMeta());
            IBakedModel model = mc.getRenderItem().getItemModelWithOverrides(blockStack, mc.world, mc.player); // getItemModelMesher().getItemModel(blockStack);
            if (!(model instanceof CreativeBakedModel))
                ForgeHooksClient.handleCameraTransforms(model, cameraTransformType, false);
            
            GlStateManager.disableDepth();
            GlStateManager.pushMatrix();
            GlStateManager.translate(-0.5F, -0.5F, -0.5F);
            
            try {
                if (model.isBuiltInRenderer()) {
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.enableRescaleNormal();
                    TileEntityItemStackRenderer.instance.renderByItem(blockStack);
                } else {
                    Color color = preview.hasColor() ? ColorUtils.IntToRGBA(preview.getColor()) : ColorUtils.IntToRGBA(ColorUtils.WHITE);
                    color.setAlpha(255);
                    ReflectionHelper.findMethod(RenderItem.class, "renderModel", "func_191967_a", IBakedModel.class, int.class, ItemStack.class)
                        .invoke(mc.getRenderItem(), model, preview.hasColor() ? ColorUtils.RGBAToInt(color) : -1, blockStack);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
            
            GlStateManager.popMatrix();
            
            GlStateManager.enableDepth();
        }
        
        GlStateManager.popMatrix();
        
    }
    
    @Override
    public boolean hasLittlePreview(ItemStack stack) {
        return true;
    }
    
    @SideOnly(Side.CLIENT)
    private static EntityPlayer getPlayer() {
        return Minecraft.getMinecraft().player;
    }
    
    @Override
    public LittleAbsolutePreviews getLittlePreview(ItemStack stack) {
        return null;
    }
    
    @Override
    public LittleAbsolutePreviews getLittlePreview(ItemStack stack, boolean allowLowResolution) {
        if (selection != null) {
            LittleBoxes boxes = selection.getBoxes(allowLowResolution);
            
            LittleAbsolutePreviews previews = new LittleAbsolutePreviews(boxes.pos, boxes.context);
            
            LittlePreview preview = getPreview(stack);
            for (LittleBox box : boxes.all()) {
                LittlePreview newPreview = preview.copy();
                newPreview.box = box.copy();
                previews.addWithoutCheckingPreview(newPreview);
            }
            
            return previews;
        }
        return null;
    }
    
    @Override
    public void saveLittlePreview(ItemStack stack, LittlePreviews previews) {}
    
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
    public float getPreviewAlphaFactor() {
        return 0.4F;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void tick(EntityPlayer player, ItemStack stack, PlacementPosition position, RayTraceResult result) {
        if (selection == null)
            selection = new ShapeSelection(stack, false);
        selection.setLast(player, stack, getPosition(position, result, currentMode), result);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldCache() {
        return false;
    }
    
    @Override
    public void onDeselect(World world, ItemStack stack, EntityPlayer player) {
        selection = null;
    }
    
    protected static PlacementPosition getPosition(PlacementPosition position, RayTraceResult result, PlacementMode mode) {
        position = position.copy();
        
        EnumFacing facing = position.facing;
        if (mode.placeInside)
            facing = facing.getOpposite();
        if (facing.getAxisDirection() == AxisDirection.NEGATIVE)
            position.getVec().add(facing);
        
        return position;
    }
    
    @Override
    public void onClickAir(EntityPlayer player, ItemStack stack) {
        if (selection != null)
            selection.click(player);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean onClickBlock(World world, EntityPlayer player, ItemStack stack, PlacementPosition position, RayTraceResult result) {
        if (selection != null)
            selection.click(player);
        return false;
    }
    
    @Override
    public boolean onRightClick(World world, EntityPlayer player, ItemStack stack, PlacementPosition position, RayTraceResult result) {
        if (LittleAction.isUsingSecondMode(player)) {
            selection = null;
            PreviewRenderer.marked = null;
        } else if (selection != null)
            return selection.addAndCheckIfPlace(player, getPosition(position, result, currentMode), result);
        return false;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean onMouseWheelClickBlock(World world, EntityPlayer player, ItemStack stack, PlacementPosition position, RayTraceResult result) {
        IBlockState state = world.getBlockState(result.getBlockPos());
        if (LittleAction.isBlockValid(state)) {
            PacketHandler.sendPacketToServer(new LittleVanillaBlockPacket(result.getBlockPos(), VanillaBlockAction.CHISEL));
            return true;
        } else if (state.getBlock() instanceof BlockTile) {
            PacketHandler.sendPacketToServer(new LittleBlockPacket(world, result.getBlockPos(), player, BlockPacketAction.CHISEL, new NBTTagCompound()));
            return true;
        }
        return false;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiConfigure getConfigureGUI(EntityPlayer player, ItemStack stack) {
        return new SubGuiChisel(stack);
    }
    
    @Override
    public SubContainerConfigure getConfigureContainer(EntityPlayer player, ItemStack stack) {
        return new SubContainerConfigure(player, stack);
    }
    
    public static PlacementMode currentMode = PlacementMode.fill;
    
    @Override
    public PlacementMode getPlacementMode(ItemStack stack) {
        return currentMode;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiConfigure getConfigureGUIAdvanced(EntityPlayer player, ItemStack stack) {
        return new SubGuiModeSelector(stack, ItemMultiTiles.currentContext, currentMode) {
            
            @Override
            public void saveConfiguration(LittleGridContext context, PlacementMode mode) {
                currentMode = mode;
                if (selection != null)
                    selection.convertTo(context);
                ItemMultiTiles.currentContext = context;
            }
        };
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public IMarkMode onMark(EntityPlayer player, ItemStack stack, PlacementPosition position, RayTraceResult result, PlacementPreview previews) {
        if (selection != null)
            selection.toggleMark();
        return selection;
    }
    
    @Override
    public boolean containsIngredients(ItemStack stack) {
        return false;
    }
    
    @Override
    public LittleGridContext getPositionContext(ItemStack stack) {
        return ItemMultiTiles.currentContext;
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { getShape(stack).getLocalizedName(), Minecraft.getMinecraft().gameSettings.keyBindPickBlock.getDisplayName(),
                LittleTilesClient.mark.getDisplayName(), LittleTilesClient.configure.getDisplayName(),
                LittleTilesClient.configureAdvanced.getDisplayName() };
    }
}
