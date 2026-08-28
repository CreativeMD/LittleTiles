package team.creative.littletiles.common.item;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.mc.TooltipUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.api.common.tool.ILittleShaper;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.client.tool.LittleTool;
import team.creative.littletiles.client.tool.shaper.LittleToolShaper;
import team.creative.littletiles.client.tool.shaper.ShapeSelection;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActionPlace;
import team.creative.littletiles.common.action.LittleActionPlace.PlaceAction;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.gui.tool.GuiChisel;
import team.creative.littletiles.common.gui.tool.GuiConfigure;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.packet.action.ChangedElementPacket;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.PreviewMode;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeRegistry;

public class ItemLittleChisel extends Item implements ILittleShaper, IItemTooltip {
    
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
    public boolean hasShape(Player player, ItemStack stack) {
        return true;
    }
    
    @Override
    public LittleShape defaultShape() {
        return ShapeRegistry.DRAG_BOX;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        getShape(stack).appendInformation(stack, context, tooltip, flag);
        tooltip.add(Component.literal(TooltipUtils.printColor(LittleElement.getOrDefault(stack).color)));
    }
    
    @Override
    public void shapeFinished(Level level, Player player, ItemStack stack, ShapeSelection selection, LittleBoxes boxes) {
        if (LittleTilesClient.INTERACTION.start(true)) {
            LittleGroupAbsolute previews = new LittleGroupAbsolute(boxes.pos);
            previews.add(boxes.grid, LittleElement.getOrDefault(stack), boxes);
            LittleTilesClient.ACTION_HANDLER.execute(new LittleActionPlace(PlaceAction.ABSOLUTE, PlacementPreview.absolute(level, LittleTilesClient.ACTION_HANDLER.setting
                    .placementMode(), previews)));
        }
    }
    
    @Override
    public boolean selectLeftClick(Player player, ItemStack stack) {
        return false;
    }
    
    @Override
    public PreviewMode previewMode(Player player, ItemStack stack) {
        return LittleTilesClient.ACTION_HANDLER.setting.placementMode().placeInside || LittleTiles.CONFIG.rendering.previewLines ? PreviewMode.LINES : PreviewMode.PREVIEWS;
    }
    
    @Override
    public boolean previewInside(Player player, ItemStack stack) {
        return LittleTilesClient.ACTION_HANDLER.setting.placementMode().placeInside;
    }
    
    @Override
    public GuiConfigure getConfigure(Player player, ContainerSlotView view, boolean secondary) {
        return new GuiChisel(view);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { getShape(stack).translatable(), Minecraft.getInstance().options.keyPickItem.getTranslatedKeyMessage(), LittleTilesClient.KEY_MARK
                .getTranslatedKeyMessage(), LittleTilesClient.arrowKeysTooltip(), LittleTilesClient.KEY_CONFIGURE.getTranslatedKeyMessage(), LittleTilesClient.KEY_BUILDING_MODE
                        .getTranslatedKeyMessage() };
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public LittleTool tool(ItemStack stack) {
        return new LittleToolShaper(stack) {
            
            @Override
            public boolean onMouseWheelClickBlock(PreviewRenderer renderer, Level level, BlockHitResult result) {
                BlockState state = level.getBlockState(result.getBlockPos());
                if (LittleAction.isBlockValid(state)) {
                    LittleTiles.NETWORK.sendToServer(new ChangedElementPacket(new LittleElement(state, ColorUtils.WHITE)));
                    return true;
                } else if (state.getBlock() instanceof BlockTile) {
                    LittleTileContext context = renderer.selectFocused(result);
                    if (context.isComplete())
                        LittleTiles.NETWORK.sendToServer(new ChangedElementPacket(context.tile));
                    return true;
                }
                return false;
            }
        };
    }
}
