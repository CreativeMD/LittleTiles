package team.creative.littletiles.common.item.glove;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.tool.GuiGlove;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.packet.action.BlockPacket;
import team.creative.littletiles.common.packet.action.BlockPacket.BlockPacketAction;
import team.creative.littletiles.common.packet.action.VanillaBlockPacket;
import team.creative.littletiles.common.packet.action.VanillaBlockPacket.VanillaBlockAction;

public class BlueprintGloveMode extends GloveMode {
    
    public static final String CONTENT = "c";
    
    public static LittleGroup getPreviews(ItemStack stack) {
        if (stack.getOrCreateTag().contains(CONTENT))
            return LittleGroup.load(stack.getOrCreateTagElement(CONTENT));
        
        LittleGroup group = new LittleGroup();
        group.add(LittleGrid.MIN, new LittleElement(Blocks.STONE.defaultBlockState(), ColorUtils.WHITE), new LittleBox(0, 0, 0, 1, 1, 1));
        stack.getOrCreateTag().put(CONTENT, LittleGroup.save(group));
        return group;
    }
    
    public static BlockPos getOrigin(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        return new BlockPos(nbt.getInt("ox"), nbt.getInt("oy"), nbt.getInt("oz"));
    }
    
    public static void setOrigin(ItemStack stack, BlockPos pos) {
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.putInt("ox", pos.getX());
        nbt.putInt("oy", pos.getY());
        nbt.putInt("oz", pos.getZ());
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean wheelClickBlock(Level level, Player player, ItemStack stack, BlockHitResult result) {
        BlockState state = level.getBlockState(result.getBlockPos());
        if (LittleAction.isBlockValid(state)) {
            LittleTiles.NETWORK.sendToServer(new VanillaBlockPacket(result.getBlockPos(), VanillaBlockAction.GLOVE));
            return true;
        }
        if (state.getBlock() instanceof BlockTile) {
            CompoundTag nbt = new CompoundTag();
            nbt.putBoolean("secondMode", LittleActionHandlerClient.isUsingSecondMode());
            LittleTiles.NETWORK.sendToServer(new BlockPacket(level, result.getBlockPos(), player, BlockPacketAction.GLOVE, nbt));
            return true;
        }
        return false;
    }
    
    @Override
    public void littleBlockAction(Level level, BETiles be, LittleTileContext context, ItemStack stack, BlockPos pos, CompoundTag nbt) {
        LittleGroup previews = new LittleGroup();
        if (nbt.getBoolean("secondMode")) {
            for (Pair<IParentCollection, LittleTile> pair : be.allTiles())
                previews.add(pair.key.getGrid(), pair.value, pair.value);
        } else
            previews.add(context.parent.getGrid(), context.tile, context.box);
        setTiles(previews, stack);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasPreviewElement(ItemStack stack) {
        return false;
    }
    
    @Override
    public LittleElement getPreviewElement(ItemStack stack) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void loadGui(GuiGlove gui) {}
    
    @Override
    public void saveGui(GuiGlove gui, CompoundTag nbt) {
        nbt.put(CONTENT, LittleGroup.save(getTiles(gui.tool.get())));
    }
    
    @Override
    public LittleGroup getTiles(ItemStack stack) {
        return getPreviews(stack);
    }
    
    @Override
    public void setTiles(LittleGroup previews, ItemStack stack) {
        stack.getOrCreateTag().put(CONTENT, LittleGroup.save(previews));
    }
    
    @Override
    public void vanillaBlockAction(Level level, ItemStack stack, BlockPos pos, BlockState state) {
        if (LittleAction.isBlockValid(state)) {
            LittleGroup group = new LittleGroup();
            group.add(LittleGrid.MIN, new LittleElement(state, ColorUtils.WHITE), new LittleBox(0, 0, 0, 1, 1, 1));
            setTiles(group, stack);
        }
    }
    
}