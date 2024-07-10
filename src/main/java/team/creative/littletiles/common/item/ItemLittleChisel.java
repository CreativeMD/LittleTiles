package team.creative.littletiles.common.item;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.mc.TooltipUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.api.common.tool.ILittlePlacer;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.gui.tool.GuiChisel;
import team.creative.littletiles.common.gui.tool.GuiConfigure;
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
import team.creative.littletiles.common.placement.setting.PlacementPlayerSetting;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeRegistry;
import team.creative.littletiles.common.placement.shape.ShapeSelection;

public class ItemLittleChisel extends Item implements ILittlePlacer, IItemTooltip {
    
    public static ShapeSelection selection;
    
    public ItemLittleChisel() {
        super(new Item.Properties().stacksTo(1));
    }
    
    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return false;
    }
    
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 0F;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        LittleShape shape = getShape(stack);
        tooltip.add(Component.translatable("gui.shape").append(": ").append(Component.translatable(shape.getTranslatableName())));
        shape.addExtraInformation(stack.getTag(), tooltip);
        tooltip.add(Component.literal(TooltipUtils.printColor(getElement(stack).color)));
    }
    
    public static LittleShape getShape(ItemStack stack) {
        return getShape(stack.getOrCreateTag());
    }
    
    public static LittleShape getShape(CompoundTag nbt) {
        return ShapeRegistry.REGISTRY.get(nbt.getString("shape"));
    }
    
    public static void setShape(ItemStack stack, LittleShape shape) {
        setShape(stack.getOrCreateTag(), shape);
    }
    
    public static void setShape(CompoundTag nbt, LittleShape shape) {
        nbt.putString("shape", shape.getKey());
    }
    
    public static LittleElement getElement(ItemStack stack) {
        if (stack.getOrCreateTag().contains("element"))
            return new LittleElement(stack.getOrCreateTagElement("element"));
        
        LittleElement element = new LittleElement(Blocks.STONE.defaultBlockState(), ColorUtils.WHITE);
        setElement(stack, element);
        return element;
    }
    
    public static LittleElement getElement(CompoundTag nbt) {
        if (nbt.contains("element"))
            return new LittleElement(nbt.getCompound("element"));
        
        return new LittleElement(Blocks.STONE.defaultBlockState(), ColorUtils.WHITE);
    }
    
    public static void setElement(ItemStack stack, LittleElement element) {
        element.save(stack.getOrCreateTagElement("element"));
    }
    
    public static void setElement(CompoundTag nbt, LittleElement element) {
        CompoundTag tag = new CompoundTag();
        element.save(tag);
        nbt.put("element", tag);
    }
    
    @Override
    public boolean hasTiles(ItemStack stack) {
        return true;
    }
    
    @Override
    public LittleGroup getTiles(ItemStack stack) {
        return null;
    }
    
    @Override
    public LittleGroup getLow(ItemStack stack) {
        return null;
    }
    
    @Override
    public PlacementPreview getPlacement(Player player, Level level, ItemStack stack, PlacementPosition position, boolean allowLowResolution) {
        if (selection != null) {
            LittleBoxes boxes = selection.getBoxes(allowLowResolution, getPositionGrid(player, stack));
            LittleGroupAbsolute previews = new LittleGroupAbsolute(boxes.pos);
            previews.add(boxes.grid, getElement(stack), boxes);
            return PlacementPreview.absolute(level, stack, previews, selection.getFirst().pos.facing);
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
        selection.setLast(player, stack, getPosition(position, result, PlacementPlayerSetting.placementMode(player)), result);
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
    @OnlyIn(Dist.CLIENT)
    public boolean onClickBlock(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        if (selection != null)
            selection.click(player);
        return false;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onRightClick(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        if (LittleActionHandlerClient.isUsingSecondMode()) {
            selection = null;
            LittleTilesClient.PREVIEW_RENDERER.removeMarked();
        } else if (selection != null)
            return selection.addAndCheckIfPlace(player, getPosition(position, result, PlacementPlayerSetting.placementMode(player)), result);
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
    public GuiConfigure getConfigure(Player player, ContainerSlotView view) {
        return new GuiChisel(view);
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
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { getShape(stack).getTranslatable(), Minecraft.getInstance().options.keyPickItem.getTranslatedKeyMessage(), LittleTilesClient.mark
                .getTranslatedKeyMessage(), LittleTilesClient.arrowKeysTooltip(), LittleTilesClient.configure.getTranslatedKeyMessage() };
    }
}
