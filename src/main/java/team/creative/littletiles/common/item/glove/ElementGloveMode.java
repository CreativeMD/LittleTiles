package team.creative.littletiles.common.item.glove;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.packet.action.BlockPacket;
import team.creative.littletiles.common.packet.action.BlockPacket.BlockPacketAction;
import team.creative.littletiles.common.packet.action.VanillaBlockPacket;
import team.creative.littletiles.common.packet.action.VanillaBlockPacket.VanillaBlockAction;

public abstract class ElementGloveMode extends GloveMode {
    
    public static LittleElement getElement(ItemStack stack) {
        var data = ILittleTool.getData(stack);
        if (data.contains("element"))
            return new LittleElement(data.getCompound("element"));
        LittleElement element = new LittleElement(Blocks.STONE.defaultBlockState(), ColorUtils.WHITE);
        setElement(stack, element);
        return element;
    }
    
    public static LittleElement getElement(CompoundTag nbt) {
        if (nbt.contains("element"))
            return new LittleElement(nbt.getCompound("element"));
        LittleElement element = new LittleElement(Blocks.STONE.defaultBlockState(), ColorUtils.WHITE);
        nbt.put("element", element.save(new CompoundTag()));
        return element;
    }
    
    public static void setElement(ItemStack stack, LittleElement element) {
        var data = ILittleTool.getData(stack);
        element.save(data.getCompound("element"));
        ILittleTool.setData(stack, data);
    }
    
    public static void setElement(CompoundTag nbt, LittleElement element) {
        nbt.put("element", element.save(new CompoundTag()));
    }
    
    public ElementGloveMode() {}
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean wheelClickBlock(Level level, Player player, ItemStack stack, BlockHitResult result) {
        BlockState state = level.getBlockState(result.getBlockPos());
        if (LittleAction.isBlockValid(state)) {
            LittleTiles.NETWORK.sendToServer(new VanillaBlockPacket(result.getBlockPos(), VanillaBlockAction.GLOVE));
            return true;
        } else if (state.getBlock() instanceof BlockTile) {
            CompoundTag nbt = new CompoundTag();
            nbt.putBoolean("secondMode", LittleActionHandlerClient.isUsingSecondMode());
            LittleTiles.NETWORK.sendToServer(new BlockPacket(level, result.getBlockPos(), player, BlockPacketAction.GLOVE, nbt));
            return true;
        }
        return false;
    }
    
    public LittleGrid getGrid(ItemStack stack) {
        return LittleGrid.get(stack);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasPreviewElement(ItemStack stack) {
        return true;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public LittleElement getPreviewElement(ItemStack stack) {
        return getElement(stack);
    }
    
    @Override
    public void vanillaBlockAction(Level level, ItemStack stack, BlockPos pos, BlockState state) {
        if (LittleAction.isBlockValid(state))
            setElement(stack, new LittleElement(state, ColorUtils.WHITE));
    }
    
    @Override
    public void littleBlockAction(Level level, BETiles be, LittleTileContext context, ItemStack stack, BlockPos pos, CompoundTag nbt) {
        if (LittleAction.isBlockValid(context.tile.getState()))
            setElement(stack, new LittleElement(context.tile.getState(), ColorUtils.WHITE));
    }
    
}