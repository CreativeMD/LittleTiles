package team.creative.littletiles.common.item;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import com.creativemd.creativecore.client.rendering.model.CreativeBakedModel;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.model.ICreativeRendered;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.mc.TooltipUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.api.tool.ILittlePlacer;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.SubGuiChisel;
import team.creative.littletiles.common.gui.configure.GuiConfigure;
import team.creative.littletiles.common.gui.configure.SubGuiModeSelector;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.packet.action.BlockPacket;
import team.creative.littletiles.common.packet.action.BlockPacket.BlockPacketAction;
import team.creative.littletiles.common.packet.action.VanillaBlockPacket;
import team.creative.littletiles.common.packet.action.VanillaBlockPacket.VanillaBlockAction;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.mark.IMarkMode;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeRegistry;
import team.creative.littletiles.common.placement.shape.ShapeSelection;

public class ItemLittleChisel extends Item implements ICreativeRendered, ILittlePlacer, IItemTooltip {
    
    public static ShapeSelection selection;
    
    public ItemLittleChisel() {
        super(new Item.Properties().tab(LittleTiles.LITTLE_TAB).stacksTo(1));
    }
    
    @Override
    public boolean isComplex() {
        return true;
    }
    
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 0F;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        LittleShape shape = getShape(stack);
        tooltip.add(new TranslatableComponent("gui.shape").append(": ").append(new TranslatableComponent(shape.getKey())));
        shape.addExtraInformation(stack.getTag(), tooltip);
        tooltip.add(new TextComponent(TooltipUtils.printColor(getPreview(stack).color)));
    }
    
    @Override
    public boolean canDestroyBlockInCreative(Level level, BlockPos pos, ItemStack stack, Player player) {
        return false;
    }
    
    public static LittleShape getShape(ItemStack stack) {
        return ShapeRegistry.getShape(stack.getOrCreateTag().getString("shape"));
    }
    
    public static void setShape(ItemStack stack, LittleShape shape) {
        stack.getOrCreateTag().putString("shape", shape.getKey());
    }
    
    public static LittleElement getPreview(ItemStack stack) {
        if (stack.getOrCreateTag().contains("preview"))
            return new LittleElement(stack.getOrCreateTagElement("element"));
        
        LittleElement element = new LittleElement(Blocks.STONE.defaultBlockState(), ColorUtils.WHITE);
        setPreview(stack, element);
        return element;
    }
    
    public static void setPreview(ItemStack stack, LittleElement element) {
        element.save(stack.getOrCreateTagElement("element"));
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public List<RenderBox> getRenderingBoxes(BlockState state, BlockEntity te, ItemStack stack) {
        return Collections.emptyList();
    }
    
    @OnlyIn(Dist.CLIENT)
    public static BakedModel model;
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyCustomOpenGLHackery(ItemStack stack, TransformType cameraTransformType) {
        Minecraft mc = Minecraft.getInstance();
        GlStateManager.pushMatrix();
        
        if (model == null)
            model = mc.getRenderItem().getItemModelMesher().getModelManager().getModel(new ModelResourceLocation(LittleTiles.MODID + ":chisel_background", "inventory"));
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
    public boolean hasTiles(ItemStack stack) {
        return true;
    }
    
    @OnlyIn(Dist.CLIENT)
    private static Player getPlayer() {
        return Minecraft.getInstance().player;
    }
    
    @Override
    public LittleGroup getTiles(ItemStack stack) {
        return null;
    }
    
    @Override
    public PlacementPreview getPlacement(Level level, ItemStack stack, PlacementPosition position, boolean allowLowResolution) {
        if (selection != null) {
            LittleBoxes boxes = selection.getBoxes(allowLowResolution);
            LittleGroupAbsolute previews = new LittleGroupAbsolute(boxes.pos, boxes.grid);
            previews.add(getPreview(stack), boxes);
            return new PlacementPreview(level, previews, getPlacementMode(stack), selection.getFirst().pos.facing);
        }
        return null;
    }
    
    @Override
    public void saveTiles(ItemStack stack, LittleGroup group) {}
    
    @Override
    public void rotate(Player player, ItemStack stack, Rotation rotation, boolean client) {
        if (client && selection != null)
            selection.rotate(player, stack, rotation);
        else
            new ShapeSelection(stack, false).rotate(player, stack, rotation);
    }
    
    @Override
    public void mirror(Player player, ItemStack stack, Axis axis, boolean client) {
        if (client && selection != null)
            selection.mirror(player, stack, axis);
        else
            new ShapeSelection(stack, false).mirror(player, stack, axis);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public float getPreviewAlphaFactor() {
        return 0.4F;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void tick(Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        if (selection == null)
            selection = new ShapeSelection(stack, false);
        selection.setLast(player, stack, getPosition(position, result, currentMode), result);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean shouldCache() {
        return false;
    }
    
    @Override
    public void onDeselect(Level level, ItemStack stack, Player player) {
        selection = null;
    }
    
    protected static PlacementPosition getPosition(PlacementPosition position, BlockHitResult result, PlacementMode mode) {
        position = position.copy();
        
        Facing facing = position.facing;
        if (mode.placeInside)
            facing = facing.opposite();
        if (!facing.positive)
            position.getVec().add(facing);
        
        return position;
    }
    
    @Override
    public void onClickAir(Player player, ItemStack stack) {
        if (selection != null)
            selection.click(player);
    }
    
    @Override
    public boolean onClickBlock(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        if (selection != null)
            selection.click(player);
        return false;
    }
    
    @Override
    public boolean onRightClick(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        if (LittleActionHandlerClient.isUsingSecondMode(player)) {
            selection = null;
            PreviewRenderer.marked = null;
        } else if (selection != null)
            return selection.addAndCheckIfPlace(player, getPosition(position, result, currentMode), result);
        return false;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onMouseWheelClickBlock(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        BlockState state = level.getBlockState(result.getBlockPos());
        if (LittleAction.isBlockValid(state)) {
            LittleTiles.NETWORK.sendToServer(new VanillaBlockPacket(result.getBlockPos(), VanillaBlockAction.CHISEL));
            return true;
        } else if (state.getBlock() instanceof BlockTile) {
            LittleTiles.NETWORK.sendToServer(new BlockPacket(level, result.getBlockPos(), player, BlockPacketAction.CHISEL, new CompoundTag()));
            return true;
        }
        return false;
    }
    
    @Override
    public GuiConfigure getConfigure(Player player, ItemStack stack) {
        return new SubGuiChisel(stack);
    }
    
    public static PlacementMode currentMode = PlacementMode.fill;
    
    @Override
    public PlacementMode getPlacementMode(ItemStack stack) {
        return currentMode;
    }
    
    @Override
    public GuiConfigure getConfigureAdvanced(Player player, ItemStack stack) {
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
    @OnlyIn(Dist.CLIENT)
    public IMarkMode onMark(Player player, ItemStack stack, PlacementPosition position, BlockHitResult result, PlacementPreview previews) {
        if (selection != null)
            selection.toggleMark();
        return selection;
    }
    
    @Override
    public boolean containsIngredients(ItemStack stack) {
        return false;
    }
    
    @Override
    public LittleGrid getPositionGrid(ItemStack stack) {
        return ItemMultiTiles.currentContext;
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { getShape(stack).getLocalizedName(), Minecraft.getInstance().options.keyPickItem.getTranslatedKeyMessage(), LittleTilesClient.mark
                .getTranslatedKeyMessage(), LittleTilesClient.configure.getTranslatedKeyMessage(), LittleTilesClient.configureAdvanced.getTranslatedKeyMessage() };
    }
}
