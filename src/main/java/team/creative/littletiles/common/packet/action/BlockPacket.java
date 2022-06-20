package team.creative.littletiles.common.packet.action;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.creativecore.common.network.CanBeNull;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.mc.PlayerUtils;
import team.creative.creativecore.common.util.mc.TickUtils;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.entity.LittleLevelEntity;
import team.creative.littletiles.common.item.ItemLittleChisel;
import team.creative.littletiles.common.item.ItemLittleGlove;
import team.creative.littletiles.common.item.ItemLittlePaintBrush;
import team.creative.littletiles.common.level.LittleAnimationHandlers;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

public class BlockPacket extends CreativePacket {
    
    public static enum BlockPacketAction {
        
        COLOR_TUBE(true) {
            @Override
            public void action(Level level, BETiles be, LittleTileContext context, ItemStack stack, Player player, BlockHitResult moving, BlockPos pos, CompoundTag nbt) {
                if (context.parent.isStructure()) {
                    try {
                        LittleStructure structure = context.parent.getStructure();
                        if (structure.hasStructureColor()) {
                            ItemLittlePaintBrush.setColor(player.getMainHandItem(), structure.getStructureColor());
                            return;
                        }
                    } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                    
                }
                ItemLittlePaintBrush.setColor(player.getMainHandItem(), context.tile.color);
                
            }
        },
        CHISEL(false) {
            
            @Override
            public void action(Level level, BETiles be, LittleTileContext context, ItemStack stack, Player player, BlockHitResult moving, BlockPos pos, CompoundTag nbt) {
                if (LittleAction.isBlockValid(context.tile.getState()))
                    ItemLittleChisel.setElement(stack, new LittleElement(context.tile.getState(), ColorUtils.WHITE));
            }
        },
        GRABBER(false) {
            
            @Override
            public void action(Level level, BETiles be, LittleTileContext context, ItemStack stack, Player player, BlockHitResult moving, BlockPos pos, CompoundTag nbt) {
                ItemLittleGlove.getMode(stack).littleBlockAction(level, be, context, stack, pos, nbt);
            }
        },
        WRENCH(true) {
            
            @Override
            public void action(Level level, BETiles be, LittleTileContext context, ItemStack stack, Player player, BlockHitResult moving, BlockPos pos, CompoundTag nbt) {
                player.sendSystemMessage(Component.literal("grid:" + be.getGrid()));
                be.combineTiles();
                be.convertBlockToVanilla();
                be.updateTiles();
            }
            
        },
        WRENCH_INFO(true) {
            
            @Override
            public void action(Level level, BETiles be, LittleTileContext context, ItemStack stack, Player player, BlockHitResult moving, BlockPos pos, CompoundTag nbt) {
                if (context.parent.isStructure())
                    try {
                        String info = context.parent.getStructure().info();
                        if (!info.isEmpty())
                            player.sendSystemMessage(Component.literal(info));
                    } catch (CorruptedConnectionException | NotYetConnectedException e) {}
            }
            
        },
        RECIPE(false) {
            
            @Override
            public void action(Level level, BETiles be, LittleTileContext context, ItemStack stack, Player player, BlockHitResult moving, BlockPos pos, CompoundTag nbt) {
                LittleGroup previews;
                if (context.parent.isStructure()) {
                    if (nbt.getBoolean("secondMode"))
                        try {
                            previews = context.parent.getStructure().getPreviews(pos);
                        } catch (CorruptedConnectionException | NotYetConnectedException e) {
                            return;
                        }
                    else
                        try {
                            previews = context.parent.getStructure().findTopStructure().getPreviews(pos);
                        } catch (CorruptedConnectionException | NotYetConnectedException e) {
                            return;
                        }
                } else {
                    previews = new LittleGroup(null, be.getGrid(), null);
                    if (nbt.getBoolean("secondMode"))
                        for (Pair<IParentCollection, LittleTile> pair : be.allTiles())
                            previews.addDirectly(pair.getValue().copy());
                    else
                        previews.addDirectly(context.tile.copy());
                }
                
                stack.setTag(LittleGroup.save(previews));
            }
        };
        
        public final boolean rightClick;
        
        private BlockPacketAction(boolean rightClick) {
            this.rightClick = rightClick;
        }
        
        public abstract void action(Level level, BETiles be, LittleTileContext context, ItemStack stack, Player player, BlockHitResult moving, BlockPos pos, CompoundTag nbt);
    }
    
    public BlockPos blockPos;
    public Vec3 pos;
    public Vec3 look;
    public BlockPacketAction action;
    public CompoundTag nbt;
    @CanBeNull
    public UUID uuid;
    
    public BlockPacket() {}
    
    public BlockPacket(Level level, BlockPos blockPos, Player player, BlockPacketAction action) {
        this(level, blockPos, player, action, new CompoundTag());
    }
    
    public BlockPacket(Level level, BlockPos blockPos, Player player, BlockPacketAction action, CompoundTag nbt) {
        this.blockPos = blockPos;
        this.action = action;
        float partialTickTime = TickUtils.getDeltaFrameTime(level);
        this.pos = player.getPosition(partialTickTime);
        double distance = PlayerUtils.getReach(player);
        Vec3 view = player.getViewVector(partialTickTime);
        this.look = pos.add(view.x * distance, view.y * distance, view.z * distance);
        this.nbt = nbt;
        if (level instanceof ISubLevel subLevel)
            uuid = subLevel.getHolder().getUUID();
    }
    
    @Override
    public void executeClient(Player player) {}
    
    @Override
    public void executeServer(ServerPlayer player) {
        Level level = player.level;
        
        if (uuid != null) {
            LittleLevelEntity entity = LittleAnimationHandlers.find(false, uuid);
            if (entity == null)
                return;
            
            if (!LittleAction.isAllowedToInteract(player, entity, action.rightClick))
                return;
            
            level = entity.getFakeLevel();
            pos = entity.getOrigin().transformPointToFakeWorld(pos);
            look = entity.getOrigin().transformPointToFakeWorld(look);
        }
        
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof BETiles) {
            BETiles be = (BETiles) blockEntity;
            LittleTileContext context = be.getFocusedTile(pos, look);
            
            if (!LittleAction.isAllowedToInteract(level, player, blockPos, action.rightClick, Facing.EAST)) {
                LittleAction.sendBlockResetToClient(level, player, be);
                return;
            }
            
            if (context.isComplete()) {
                ItemStack stack = player.getMainHandItem();
                action.action(level, be, context, stack, player, be.rayTrace(pos, look), blockPos, nbt);
                player.inventoryMenu.broadcastChanges();
                
            }
        }
    }
    
}
