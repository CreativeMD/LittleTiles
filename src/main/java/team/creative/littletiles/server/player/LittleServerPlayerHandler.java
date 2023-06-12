package team.creative.littletiles.server.player;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Logger;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundChatAckPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundJigsawGeneratePacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.common.packet.entity.LittleVanillaPacket;
import team.creative.littletiles.mixin.server.network.ServerGamePacketListenerImplAccessor;

public class LittleServerPlayerHandler implements ServerPlayerConnection, TickablePacketListener, ServerGamePacketListener {
    
    private static final Logger LOGGER = LittleTiles.LOGGER;
    private final MinecraftServer server;
    public final ServerPlayer player;
    
    public Level level;
    
    private boolean isDestroyingBlock;
    private int destroyProgressStart;
    private BlockPos destroyPos = BlockPos.ZERO;
    private Level destroyLevel;
    private int gameTicks;
    private boolean hasDelayedDestroy;
    private BlockPos delayedDestroyPos = BlockPos.ZERO;
    private int delayedTickStart;
    private int lastSentState = -1;
    private Object2IntMap<Level> ackBlockChanges = new Object2IntArrayMap<>();
    
    LittleServerPlayerHandler(ServerPlayer player) {
        this.server = player.getServer();
        this.player = player;
    }
    
    public void ensureRunningOnSameThread(Packet packet) throws RunningOnDifferentThreadException {
        if (!server.isSameThread()) {
            server.executeIfPossible(() -> LittleServerPlayerConnection.runInContext((LittleLevel) level, player, x -> packet.handle(x)));
            throw RunningOnDifferentThreadException.RUNNING_ON_DIFFERENT_THREAD;
        }
    }
    
    public ServerLevel requiresServerLevel() {
        if (level instanceof ServerLevel s)
            return s;
        throw new RuntimeException("Cannot run this packet on this level " + level);
    }
    
    @Override
    public ServerPlayer getPlayer() {
        return player;
    }
    
    public ServerGamePacketListenerImpl getVanilla() {
        return player.connection;
    }
    
    @Override
    public void handlePlayerInput(ServerboundPlayerInputPacket packet) {
        getVanilla().handlePlayerInput(packet);
    }
    
    @Override
    public void handleMoveVehicle(ServerboundMoveVehiclePacket packet) {
        getVanilla().handleMoveVehicle(packet);
    }
    
    @Override
    public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket packet) {
        getVanilla().handleAcceptTeleportPacket(packet);
    }
    
    @Override
    public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket packet) {
        getVanilla().handleRecipeBookSeenRecipePacket(packet);
    }
    
    @Override
    public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket packet) {
        getVanilla().handleRecipeBookChangeSettingsPacket(packet);
    }
    
    @Override
    public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket packet) {
        getVanilla().handleSeenAdvancements(packet);
    }
    
    @Override
    public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket packet) {
        getVanilla().handleCustomCommandSuggestions(packet);
    }
    
    @Override
    public void handleSetCommandBlock(ServerboundSetCommandBlockPacket packet) {
        ensureRunningOnSameThread(packet);
        if (!this.server.isCommandBlockEnabled())
            this.player.sendSystemMessage(Component.translatable("advMode.notEnabled"));
        else if (!this.player.canUseGameMasterBlocks())
            this.player.sendSystemMessage(Component.translatable("advMode.notAllowed"));
        else {
            BaseCommandBlock basecommandblock = null;
            CommandBlockEntity commandblockentity = null;
            BlockPos blockpos = packet.getPos();
            BlockEntity blockentity = level.getBlockEntity(blockpos);
            if (blockentity instanceof CommandBlockEntity) {
                commandblockentity = (CommandBlockEntity) blockentity;
                basecommandblock = commandblockentity.getCommandBlock();
            }
            
            String s = packet.getCommand();
            boolean flag = packet.isTrackOutput();
            if (basecommandblock != null) {
                CommandBlockEntity.Mode commandblockentity$mode = commandblockentity.getMode();
                BlockState blockstate = level.getBlockState(blockpos);
                Direction direction = blockstate.getValue(CommandBlock.FACING);
                BlockState blockstate1;
                blockstate1 = switch (packet.getMode()) {
                    case SEQUENCE -> Blocks.CHAIN_COMMAND_BLOCK.defaultBlockState();
                    case AUTO -> Blocks.REPEATING_COMMAND_BLOCK.defaultBlockState();
                    default -> Blocks.COMMAND_BLOCK.defaultBlockState();
                };
                
                BlockState blockstate2 = blockstate1.setValue(CommandBlock.FACING, direction).setValue(CommandBlock.CONDITIONAL, Boolean.valueOf(packet.isConditional()));
                if (blockstate2 != blockstate) {
                    level.setBlock(blockpos, blockstate2, 2);
                    blockentity.setBlockState(blockstate2);
                    level.getChunkAt(blockpos).setBlockEntity(blockentity);
                }
                
                basecommandblock.setCommand(s);
                basecommandblock.setTrackOutput(flag);
                if (!flag)
                    basecommandblock.setLastOutput((Component) null);
                
                commandblockentity.setAutomatic(packet.isAutomatic());
                if (commandblockentity$mode != packet.getMode())
                    commandblockentity.onModeSwitch();
                
                basecommandblock.onUpdated();
                if (!StringUtil.isNullOrEmpty(s))
                    this.player.sendSystemMessage(Component.translatable("advMode.setCommand.success", s));
            }
            
        }
    }
    
    @Override
    public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket packet) {
        ensureRunningOnSameThread(packet);
        if (!this.server.isCommandBlockEnabled())
            this.player.sendSystemMessage(Component.translatable("advMode.notEnabled"));
        else if (!this.player.canUseGameMasterBlocks())
            this.player.sendSystemMessage(Component.translatable("advMode.notAllowed"));
        else {
            BaseCommandBlock basecommandblock = packet.getCommandBlock(level);
            if (basecommandblock != null) {
                basecommandblock.setCommand(packet.getCommand());
                basecommandblock.setTrackOutput(packet.isTrackOutput());
                if (!packet.isTrackOutput())
                    basecommandblock.setLastOutput((Component) null);
                
                basecommandblock.onUpdated();
                this.player.sendSystemMessage(Component.translatable("advMode.setCommand.success", packet.getCommand()));
            }
            
        }
    }
    
    @Override
    public void handlePickItem(ServerboundPickItemPacket packet) {
        getVanilla().handlePickItem(packet);
    }
    
    @Override
    public void handleRenameItem(ServerboundRenameItemPacket packet) {
        getVanilla().handleRenameItem(packet);
    }
    
    @Override
    public void handleSetBeaconPacket(ServerboundSetBeaconPacket packet) {
        getVanilla().handleSetBeaconPacket(packet);
    }
    
    @Override
    public void handleSetStructureBlock(ServerboundSetStructureBlockPacket packet) {
        ensureRunningOnSameThread(packet);
        ServerLevel level = requiresServerLevel();
        if (this.player.canUseGameMasterBlocks()) {
            BlockPos blockpos = packet.getPos();
            BlockState blockstate = level.getBlockState(blockpos);
            BlockEntity blockentity = level.getBlockEntity(blockpos);
            if (blockentity instanceof StructureBlockEntity structure) {
                structure.setMode(packet.getMode());
                structure.setStructureName(packet.getName());
                structure.setStructurePos(packet.getOffset());
                structure.setStructureSize(packet.getSize());
                structure.setMirror(packet.getMirror());
                structure.setRotation(packet.getRotation());
                structure.setMetaData(packet.getData());
                structure.setIgnoreEntities(packet.isIgnoreEntities());
                structure.setShowAir(packet.isShowAir());
                structure.setShowBoundingBox(packet.isShowBoundingBox());
                structure.setIntegrity(packet.getIntegrity());
                structure.setSeed(packet.getSeed());
                if (structure.hasStructureName()) {
                    String s = structure.getStructureName();
                    if (packet.getUpdateType() == StructureBlockEntity.UpdateType.SAVE_AREA)
                        if (structure.saveStructure())
                            this.player.displayClientMessage(Component.translatable("structure_block.save_success", s), false);
                        else
                            this.player.displayClientMessage(Component.translatable("structure_block.save_failure", s), false);
                    else if (packet.getUpdateType() == StructureBlockEntity.UpdateType.LOAD_AREA)
                        if (!structure.isStructureLoadable())
                            this.player.displayClientMessage(Component.translatable("structure_block.load_not_found", s), false);
                        else if (structure.loadStructure(level))
                            this.player.displayClientMessage(Component.translatable("structure_block.load_success", s), false);
                        else
                            this.player.displayClientMessage(Component.translatable("structure_block.load_prepare", s), false);
                    else if (packet.getUpdateType() == StructureBlockEntity.UpdateType.SCAN_AREA)
                        if (structure.detectSize())
                            this.player.displayClientMessage(Component.translatable("structure_block.size_success", s), false);
                        else
                            this.player.displayClientMessage(Component.translatable("structure_block.size_failure"), false);
                } else
                    this.player.displayClientMessage(Component.translatable("structure_block.invalid_structure_name", packet.getName()), false);
                
                structure.setChanged();
                level.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
            }
            
        }
    }
    
    @Override
    public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket packet) {
        ensureRunningOnSameThread(packet);
        if (this.player.canUseGameMasterBlocks()) {
            BlockPos blockpos = packet.getPos();
            BlockState blockstate = level.getBlockState(blockpos);
            BlockEntity blockentity = level.getBlockEntity(blockpos);
            if (blockentity instanceof JigsawBlockEntity jigsaw) {
                jigsaw.setName(packet.getName());
                jigsaw.setTarget(packet.getTarget());
                jigsaw.setPool(ResourceKey.create(Registries.TEMPLATE_POOL, packet.getPool()));
                jigsaw.setFinalState(packet.getFinalState());
                jigsaw.setJoint(packet.getJoint());
                jigsaw.setChanged();
                level.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
            }
            
        }
    }
    
    @Override
    public void handleJigsawGenerate(ServerboundJigsawGeneratePacket packet) {
        ensureRunningOnSameThread(packet);
        ServerLevel level = requiresServerLevel();
        if (this.player.canUseGameMasterBlocks()) {
            BlockEntity blockentity = level.getBlockEntity(packet.getPos());
            if (blockentity instanceof JigsawBlockEntity jigsawblockentity)
                jigsawblockentity.generate(level, packet.levels(), packet.keepJigsaws());
        }
    }
    
    @Override
    public void handleSelectTrade(ServerboundSelectTradePacket packet) {
        getVanilla().handleSelectTrade(packet);
    }
    
    @Override
    public void handleEditBook(ServerboundEditBookPacket packet) {
        getVanilla().handleEditBook(packet);
    }
    
    @Override
    public void handleEntityTagQuery(ServerboundEntityTagQuery packet) {
        ensureRunningOnSameThread(packet);
        if (this.player.hasPermissions(2)) {
            Entity entity = level.getEntity(packet.getEntityId());
            if (entity != null)
                send(new ClientboundTagQueryPacket(packet.getTransactionId(), entity.saveWithoutId(new CompoundTag())));
        }
    }
    
    @Override
    public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQuery packet) {
        ensureRunningOnSameThread(packet);
        if (this.player.hasPermissions(2)) {
            BlockEntity blockentity = level.getBlockEntity(packet.getPos());
            CompoundTag compoundtag = blockentity != null ? blockentity.saveWithoutMetadata() : null;
            send(new ClientboundTagQueryPacket(packet.getTransactionId(), compoundtag));
        }
    }
    
    @Override
    public void handleMovePlayer(ServerboundMovePlayerPacket packet) {
        getVanilla().handleMovePlayer(packet);
    }
    
    @Override
    public void handlePlayerAction(ServerboundPlayerActionPacket packet) {
        ensureRunningOnSameThread(packet);
        BlockPos blockpos = packet.getPos();
        this.player.resetLastActionTime();
        switch (packet.getAction()) {
            case SWAP_ITEM_WITH_OFFHAND:
            case DROP_ITEM:
            case DROP_ALL_ITEMS:
            case RELEASE_USE_ITEM:
                getVanilla().handlePlayerAction(packet);
                return;
            case START_DESTROY_BLOCK:
            case ABORT_DESTROY_BLOCK:
            case STOP_DESTROY_BLOCK:
                handleBlockBreakAction(blockpos, packet.getAction(), packet.getDirection(), level.getMaxBuildHeight(), packet.getSequence());
                ackBlockChangesUpTo(level, packet.getSequence());
                return;
            default:
                throw new IllegalArgumentException("Invalid player action");
        }
    }
    
    private static boolean wasBlockPlacementAttempt(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty())
            return false;
        Item item = stack.getItem();
        return (item instanceof BlockItem || item instanceof BucketItem) && !player.getCooldowns().isOnCooldown(item);
    }
    
    @Override
    public void handleUseItemOn(ServerboundUseItemOnPacket packet) {
        ensureRunningOnSameThread(packet);
        ackBlockChangesUpTo(level, packet.getSequence());
        this.player.connection.ackBlockChangesUpTo(packet.getSequence());
        InteractionHand interactionhand = packet.getHand();
        ItemStack itemstack = this.player.getItemInHand(interactionhand);
        if (itemstack.isItemEnabled(level.enabledFeatures())) {
            BlockHitResult blockhitresult = packet.getHitResult();
            Vec3 vec3 = blockhitresult.getLocation();
            BlockPos blockpos = blockhitresult.getBlockPos();
            Vec3 vec31 = Vec3.atCenterOf(blockpos);
            if (this.player.canReach(blockpos, 1.5)) { // Vanilla uses eye-to-center distance < 6, which implies a padding of 1.5
                Vec3 vec32 = vec3.subtract(vec31);
                if (Math.abs(vec32.x()) < 1.0000001D && Math.abs(vec32.y()) < 1.0000001D && Math.abs(vec32.z()) < 1.0000001D) {
                    Direction direction = blockhitresult.getDirection();
                    this.player.resetLastActionTime();
                    int i = level.getMaxBuildHeight();
                    if (blockpos.getY() < i) {
                        if (((ServerGamePacketListenerImplAccessor) getVanilla()).getAwaitingPositionFromClient() == null && level.mayInteract(this.player, blockpos)) {
                            InteractionResult interactionresult = this.player.gameMode.useItemOn(this.player, level, itemstack, interactionhand, blockhitresult);
                            if (direction == Direction.UP && !interactionresult.consumesAction() && blockpos.getY() >= i - 1 && wasBlockPlacementAttempt(this.player, itemstack)) {
                                Component component = Component.translatable("build.tooHigh", i - 1).withStyle(ChatFormatting.RED);
                                this.player.sendSystemMessage(component, true);
                            } else if (interactionresult.shouldSwing()) {
                                this.player.swing(interactionhand, true);
                            }
                        }
                    } else {
                        Component component1 = Component.translatable("build.tooHigh", i - 1).withStyle(ChatFormatting.RED);
                        this.player.sendSystemMessage(component1, true);
                    }
                    
                    this.player.connection.send(new ClientboundBlockUpdatePacket(level, blockpos));
                    this.player.connection.send(new ClientboundBlockUpdatePacket(level, blockpos.relative(direction)));
                } else {
                    LOGGER.warn("Rejecting UseItemOnPacket from {}: Location {} too far away from hit block {}.", this.player.getGameProfile().getName(), vec3, blockpos);
                }
            }
        }
    }
    
    @Override
    public void handleUseItem(ServerboundUseItemPacket packet) {
        ensureRunningOnSameThread(packet);
        this.ackBlockChangesUpTo(level, packet.getSequence());
        InteractionHand interactionhand = packet.getHand();
        ItemStack itemstack = this.player.getItemInHand(interactionhand);
        this.player.resetLastActionTime();
        if (!itemstack.isEmpty() && itemstack.isItemEnabled(level.enabledFeatures()))
            if (useItem(level, itemstack, interactionhand).shouldSwing())
                this.player.swing(interactionhand, true);
    }
    
    @Override
    public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket packet) {
        getVanilla().handleTeleportToEntityPacket(packet);
    }
    
    @Override
    public void handleResourcePackResponse(ServerboundResourcePackPacket packet) {
        getVanilla().handleResourcePackResponse(packet);
    }
    
    @Override
    public void handlePaddleBoat(ServerboundPaddleBoatPacket packet) {
        getVanilla().handlePaddleBoat(packet);
    }
    
    @Override
    public void handlePong(ServerboundPongPacket packet) {
        getVanilla().handlePong(packet);
    }
    
    @Override
    public void onDisconnect(Component component) {
        getVanilla().onDisconnect(component);
    }
    
    public void ackBlockChangesUpTo(Level level, int sequence) {
        if (sequence < 0)
            throw new IllegalArgumentException("Expected packet sequence nr >= 0");
        ackBlockChanges.put(level, Math.max(sequence, ackBlockChanges.getOrDefault(level, -1)));
    }
    
    @Override
    public void send(Packet<?> packet) {
        send(level, packet);
    }
    
    public void send(Level level, Packet<?> packet) {
        send(level, packet, null);
    }
    
    public void send(Level level, Packet<?> packet, @Nullable PacketSendListener listener) {
        try {
            LittleTiles.NETWORK.sendToClient(new LittleVanillaPacket((LittleLevel) level, packet), player);
            if (listener != null)
                listener.onSuccess();
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Sending packet");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Packet being sent");
            crashreportcategory.setDetail("Packet class", () -> packet.getClass().getCanonicalName());
            throw new ReportedException(crashreport);
        }
    }
    
    @Override
    public void handleSetCarriedItem(ServerboundSetCarriedItemPacket packet) {
        getVanilla().handleSetCarriedItem(packet);
    }
    
    @Override
    public void handleChat(ServerboundChatPacket packet) {
        getVanilla().handleChat(packet);
    }
    
    @Override
    public void handleChatCommand(ServerboundChatCommandPacket packet) {
        getVanilla().handleChatCommand(packet);
    }
    
    @Override
    public void handleChatAck(ServerboundChatAckPacket packet) {
        getVanilla().handleChatAck(packet);
    }
    
    @Override
    public void handleAnimate(ServerboundSwingPacket packet) {
        getVanilla().handleAnimate(packet);
    }
    
    @Override
    public void handlePlayerCommand(ServerboundPlayerCommandPacket packet) {
        getVanilla().handlePlayerCommand(packet);
    }
    
    @Override
    public void handleInteract(ServerboundInteractPacket packet) {
        getVanilla().handleInteract(packet);
    }
    
    @Override
    public void handleClientCommand(ServerboundClientCommandPacket packet) {
        getVanilla().handleClientCommand(packet);
    }
    
    @Override
    public void handleContainerClose(ServerboundContainerClosePacket packet) {
        getVanilla().handleContainerClose(packet);
    }
    
    @Override
    public void handleContainerClick(ServerboundContainerClickPacket packet) {
        getVanilla().handleContainerClick(packet);
    }
    
    @Override
    public void handlePlaceRecipe(ServerboundPlaceRecipePacket packet) {
        getVanilla().handlePlaceRecipe(packet);
    }
    
    @Override
    public void handleContainerButtonClick(ServerboundContainerButtonClickPacket packet) {
        getVanilla().handleContainerButtonClick(packet);
    }
    
    @Override
    public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket packet) {
        getVanilla().handleSetCreativeModeSlot(packet);
    }
    
    @Override
    public void handleSignUpdate(ServerboundSignUpdatePacket packet) {
        List<String> list = Stream.of(packet.getLines()).map(ChatFormatting::stripFormatting).collect(Collectors.toList());
        ((ServerGamePacketListenerImplAccessor) getVanilla()).callFilterTextPacket(list).thenAcceptAsync((lines) -> this.updateSignText(packet, lines), this.server);
    }
    
    private void updateSignText(ServerboundSignUpdatePacket packet, List<FilteredText> lines) {
        this.player.resetLastActionTime();
        BlockPos blockpos = packet.getPos();
        if (level.hasChunkAt(blockpos)) {
            BlockEntity blockentity = level.getBlockEntity(blockpos);
            if (!(blockentity instanceof SignBlockEntity))
                return;
            
            SignBlockEntity signblockentity = (SignBlockEntity) blockentity;
            signblockentity.updateSignText(this.player, packet.isFrontText(), lines);
        }
        
    }
    
    @Override
    public void handleKeepAlive(ServerboundKeepAlivePacket packet) {
        getVanilla().handleKeepAlive(packet);
    }
    
    @Override
    public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket packet) {
        getVanilla().handlePlayerAbilities(packet);
    }
    
    @Override
    public void handleClientInformation(ServerboundClientInformationPacket packet) {
        getVanilla().handleClientInformation(packet);
    }
    
    @Override
    public void handleCustomPayload(ServerboundCustomPayloadPacket packet) {
        getVanilla().handleCustomPayload(packet); // not sure if it makes sense, but for now there is nothing else to do here
    }
    
    @Override
    public void handleChangeDifficulty(ServerboundChangeDifficultyPacket packet) {
        getVanilla().handleChangeDifficulty(packet);
    }
    
    @Override
    public void handleLockDifficulty(ServerboundLockDifficultyPacket packet) {
        getVanilla().handleLockDifficulty(packet);
    }
    
    @Override
    public void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket packet) {
        getVanilla().handleChatSessionUpdate(packet);
    }
    
    @Override
    public void tick() {
        if (!ackBlockChanges.isEmpty()) {
            for (Entry<Level> entry : ackBlockChanges.object2IntEntrySet())
                this.send(entry.getKey(), new ClientboundBlockChangedAckPacket(entry.getIntValue()));
            ackBlockChanges.clear();
        }
        
        ++this.gameTicks;
        if (this.hasDelayedDestroy) {
            BlockState blockstate = destroyLevel.getBlockState(this.delayedDestroyPos);
            if (blockstate.isAir())
                this.hasDelayedDestroy = false;
            else {
                float f = this.incrementDestroyProgress(destroyLevel, blockstate, this.delayedDestroyPos, this.delayedTickStart);
                if (f >= 1.0F) {
                    this.hasDelayedDestroy = false;
                    this.destroyBlock(destroyLevel, this.delayedDestroyPos);
                    destroyLevel = null;
                }
            }
        } else if (this.isDestroyingBlock) {
            BlockState blockstate1 = destroyLevel.getBlockState(this.destroyPos);
            if (blockstate1.isAir()) {
                destroyLevel.destroyBlockProgress(this.player.getId(), this.destroyPos, -1);
                this.lastSentState = -1;
                this.isDestroyingBlock = false;
                destroyLevel = null;
            } else
                this.incrementDestroyProgress(destroyLevel, blockstate1, this.destroyPos, this.destroyProgressStart);
        }
        
    }
    
    private float incrementDestroyProgress(Level level, BlockState state, BlockPos pos, int ticks) {
        int i = this.gameTicks - ticks;
        float f = state.getDestroyProgress(this.player, level, pos) * (i + 1);
        int j = (int) (f * 10.0F);
        if (j != this.lastSentState) {
            level.destroyBlockProgress(this.player.getId(), pos, j);
            this.lastSentState = j;
        }
        
        return f;
    }
    
    private void debugLogging(BlockPos pos, boolean p_215127_, int sequence, String message) {}
    
    public boolean isCreative() {
        return player.isCreative();
    }
    
    public GameType getGameMode() {
        return this.player.gameMode.getGameModeForPlayer();
    }
    
    public void handleBlockBreakAction(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction direction, int buildHeight, int sequence) {
        PlayerInteractEvent.LeftClickBlock event = ForgeHooks.onLeftClickBlock(player, pos, direction);
        if (event.isCanceled() || (!this.isCreative() && event.getResult() == Event.Result.DENY))
            return;
        
        if (!this.player.canReach(pos, 1.5))
            this.debugLogging(pos, false, sequence, "too far");
        else if (pos.getY() >= buildHeight) {
            send(new ClientboundBlockUpdatePacket(pos, level.getBlockState(pos)));
            this.debugLogging(pos, false, sequence, "too high");
        } else {
            if (action == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
                if (!level.mayInteract(this.player, pos)) {
                    send(new ClientboundBlockUpdatePacket(pos, level.getBlockState(pos)));
                    this.debugLogging(pos, false, sequence, "may not interact");
                    return;
                }
                
                if (this.isCreative()) {
                    this.destroyAndAck(level, pos, sequence, "creative destroy");
                    return;
                }
                
                if (this.player.blockActionRestricted(level, pos, getGameMode())) {
                    send(new ClientboundBlockUpdatePacket(pos, level.getBlockState(pos)));
                    this.debugLogging(pos, false, sequence, "block action restricted");
                    return;
                }
                
                this.destroyProgressStart = this.gameTicks;
                float f = 1.0F;
                BlockState blockstate = level.getBlockState(pos);
                if (!blockstate.isAir()) {
                    if (event.getUseBlock() != Event.Result.DENY)
                        blockstate.attack(level, pos, this.player);
                    f = blockstate.getDestroyProgress(this.player, level, pos);
                }
                
                if (!blockstate.isAir() && f >= 1.0F) {
                    this.destroyAndAck(level, pos, sequence, "insta mine");
                } else {
                    if (this.isDestroyingBlock) {
                        send(new ClientboundBlockUpdatePacket(this.destroyPos, level.getBlockState(this.destroyPos)));
                        this.debugLogging(pos, false, sequence, "abort destroying since another started (client insta mine, server disagreed)");
                    }
                    
                    this.isDestroyingBlock = true;
                    this.destroyPos = pos.immutable();
                    this.destroyLevel = level;
                    int i = (int) (f * 10.0F);
                    level.destroyBlockProgress(this.player.getId(), pos, i);
                    this.debugLogging(pos, true, sequence, "actual start of destroying");
                    this.lastSentState = i;
                }
            } else if (action == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
                if (pos.equals(this.destroyPos) && destroyLevel == level) {
                    int j = this.gameTicks - this.destroyProgressStart;
                    BlockState blockstate1 = level.getBlockState(pos);
                    if (!blockstate1.isAir()) {
                        float f1 = blockstate1.getDestroyProgress(this.player, level, pos) * (j + 1);
                        if (f1 >= 0.7F) {
                            this.isDestroyingBlock = false;
                            level.destroyBlockProgress(this.player.getId(), pos, -1);
                            this.destroyAndAck(level, pos, sequence, "destroyed");
                            this.destroyLevel = null;
                            return;
                        }
                        
                        if (!this.hasDelayedDestroy) {
                            this.isDestroyingBlock = false;
                            this.hasDelayedDestroy = true;
                            this.delayedDestroyPos = pos;
                            this.delayedTickStart = this.destroyProgressStart;
                            this.destroyLevel = null;
                        }
                    }
                }
                
                this.debugLogging(pos, true, sequence, "stopped destroying");
            } else if (action == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK) {
                this.isDestroyingBlock = false;
                if (!Objects.equals(this.destroyPos, pos)) {
                    LOGGER.warn("Mismatch in destroy block pos: {} {}", this.destroyPos, pos);
                    destroyLevel.destroyBlockProgress(this.player.getId(), this.destroyPos, -1);
                    this.debugLogging(pos, true, sequence, "aborted mismatched destroying");
                }
                
                if (!Objects.equals(this.destroyLevel, level)) {
                    LOGGER.warn("Mismatch in destroy level: {} {}", this.destroyLevel, level);
                    destroyLevel.destroyBlockProgress(this.player.getId(), this.destroyPos, -1);
                    this.debugLogging(pos, true, sequence, "aborted mismatched destroying");
                }
                
                destroyLevel.destroyBlockProgress(this.player.getId(), pos, -1);
                destroyLevel = null;
                this.debugLogging(pos, true, sequence, "aborted destroying");
            }
            
        }
    }
    
    public void destroyAndAck(Level level, BlockPos pos, int sequence, String message) {
        if (this.destroyBlock(level, pos))
            this.debugLogging(pos, true, sequence, message);
        else {
            send(level, new ClientboundBlockUpdatePacket(pos, level.getBlockState(pos)));
            this.debugLogging(pos, false, sequence, message);
        }
        
    }
    
    public boolean destroyBlock(Level level, BlockPos pos) {
        BlockState blockstate = level.getBlockState(pos);
        int exp = onBlockBreakEvent(level, getGameMode(), pos);
        if (exp == -1)
            return false;
        
        BlockEntity blockentity = level.getBlockEntity(pos);
        Block block = blockstate.getBlock();
        if (block instanceof GameMasterBlock && !this.player.canUseGameMasterBlocks()) {
            level.sendBlockUpdated(pos, blockstate, blockstate, 3);
            return false;
        }
        
        if (player.getMainHandItem().onBlockStartBreak(pos, player))
            return false;
        
        if (this.player.blockActionRestricted(level, pos, getGameMode()))
            return false;
        
        if (this.isCreative()) {
            removeBlock(level, pos, false);
            return true;
        }
        
        ItemStack itemstack = this.player.getMainHandItem();
        ItemStack itemstack1 = itemstack.copy();
        boolean flag1 = blockstate.canHarvestBlock(level, pos, this.player); // previously player.hasCorrectToolForDrops(blockstate)
        itemstack.mineBlock(level, blockstate, pos, this.player);
        if (itemstack.isEmpty() && !itemstack1.isEmpty())
            ForgeEventFactory.onPlayerDestroyItem(this.player, itemstack1, InteractionHand.MAIN_HAND);
        boolean flag = removeBlock(level, pos, flag1);
        
        if (flag && flag1)
            block.playerDestroy(level, this.player, pos, blockstate, blockentity, itemstack1);
        
        if (flag && exp > 0 && level instanceof ServerLevel s)
            blockstate.getBlock().popExperience(s, pos, exp);
        
        return true;
    }
    
    private boolean removeBlock(Level level, BlockPos pos, boolean canHarvest) {
        BlockState state = level.getBlockState(pos);
        boolean removed = state.onDestroyedByPlayer(level, pos, this.player, canHarvest, level.getFluidState(pos));
        if (removed)
            state.getBlock().destroy(level, pos, state);
        return removed;
    }
    
    public int onBlockBreakEvent(Level level, GameType gameType, BlockPos pos) {
        boolean preCancelEvent = false;
        ItemStack itemstack = player.getMainHandItem();
        if (!itemstack.isEmpty() && !itemstack.getItem().canAttackBlock(level.getBlockState(pos), level, pos, player))
            preCancelEvent = true;
        
        if (gameType.isBlockPlacingRestricted()) {
            if (gameType == GameType.SPECTATOR)
                preCancelEvent = true;
            
            if (!player.mayBuild() && itemstack.isEmpty() || !itemstack
                    .hasAdventureModeBreakTagForBlock(level.registryAccess().registryOrThrow(Registries.BLOCK), new BlockInWorld(level, pos, false)))
                preCancelEvent = true;
        }
        
        // Tell client the block is gone immediately then process events
        if (level.getBlockEntity(pos) == null)
            send(level, new ClientboundBlockUpdatePacket(pos, level.getFluidState(pos).createLegacyBlock()));
        
        // Post the block break event
        BlockState state = level.getBlockState(pos);
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, state, player);
        event.setCanceled(preCancelEvent);
        MinecraftForge.EVENT_BUS.post(event);
        
        // Handle if the event is canceled
        if (event.isCanceled()) {
            // Let the client know the block still exists
            send(level, new ClientboundBlockUpdatePacket(level, pos));
            
            // Update any tile entity data for this block
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                Packet<?> pkt = blockEntity.getUpdatePacket();
                if (pkt != null)
                    send(level, pkt);
            }
        }
        return event.isCanceled() ? -1 : event.getExpToDrop();
    }
    
    public InteractionResult useItem(Level level, ItemStack stack, InteractionHand hand) {
        if (this.getGameMode() == GameType.SPECTATOR)
            return InteractionResult.PASS;
        if (player.getCooldowns().isOnCooldown(stack.getItem()))
            return InteractionResult.PASS;
        
        InteractionResult cancelResult = ForgeHooks.onItemRightClick(player, hand);
        if (cancelResult != null)
            return cancelResult;
        
        int i = stack.getCount();
        int j = stack.getDamageValue();
        InteractionResultHolder<ItemStack> result = stack.use(level, player, hand);
        ItemStack itemstack = result.getObject();
        if (itemstack == stack && itemstack.getCount() == i && itemstack.getUseDuration() <= 0 && itemstack.getDamageValue() == j)
            return result.getResult();
        
        if (result.getResult() == InteractionResult.FAIL && itemstack.getUseDuration() > 0 && !player.isUsingItem())
            return result.getResult();
        
        if (stack != itemstack)
            player.setItemInHand(hand, itemstack);
        
        if (this.isCreative()) {
            itemstack.setCount(i);
            if (itemstack.isDamageableItem() && itemstack.getDamageValue() != j)
                itemstack.setDamageValue(j);
        }
        
        if (itemstack.isEmpty())
            player.setItemInHand(hand, ItemStack.EMPTY);
        
        if (!player.isUsingItem())
            player.inventoryMenu.sendAllDataToRemote();
        
        return result.getResult();
    }
    
    public InteractionResult useItemOn(Level level, ItemStack stack, InteractionHand hand, BlockHitResult hit) {
        BlockPos blockpos = hit.getBlockPos();
        BlockState blockstate = level.getBlockState(blockpos);
        if (!blockstate.getBlock().isEnabled(level.enabledFeatures()))
            return InteractionResult.FAIL;
        
        PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(player, hand, blockpos, hit);
        if (event.isCanceled())
            return event.getCancellationResult();
        
        if (this.getGameMode() == GameType.SPECTATOR) {
            MenuProvider menuprovider = blockstate.getMenuProvider(level, blockpos);
            if (menuprovider != null) {
                player.openMenu(menuprovider);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        
        UseOnContext useoncontext = new UseOnContext(level, player, hand, stack, hit);
        if (event.getUseItem() != Event.Result.DENY) {
            InteractionResult result = stack.onItemUseFirst(useoncontext);
            if (result != InteractionResult.PASS)
                return result;
        }
        
        boolean flag = !player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty();
        boolean flag1 = (player.isSecondaryUseActive() && flag) && !(player.getMainHandItem().doesSneakBypassUse(level, blockpos, player) && player.getOffhandItem()
                .doesSneakBypassUse(level, blockpos, player));
        ItemStack itemstack = stack.copy();
        if (event.getUseBlock() == Event.Result.ALLOW || (event.getUseBlock() != Event.Result.DENY && !flag1)) {
            InteractionResult interactionresult = blockstate.use(level, player, hand, hit);
            if (interactionresult.consumesAction()) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(player, blockpos, itemstack);
                return interactionresult;
            }
        }
        
        if (event.getUseItem() == Event.Result.ALLOW || (!stack.isEmpty() && !player.getCooldowns().isOnCooldown(stack.getItem()))) {
            if (event.getUseItem() == Event.Result.DENY)
                return InteractionResult.PASS;
            InteractionResult interactionresult1;
            if (this.isCreative()) {
                int i = stack.getCount();
                interactionresult1 = stack.useOn(useoncontext);
                stack.setCount(i);
            } else
                interactionresult1 = stack.useOn(useoncontext);
            
            if (interactionresult1.consumesAction())
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(player, blockpos, itemstack);
            
            return interactionresult1;
        }
        return InteractionResult.PASS;
    }
    
    @Override
    public boolean isAcceptingMessages() {
        return getVanilla().isAcceptingMessages();
    }
    
    @FunctionalInterface
    interface EntityInteraction {
        InteractionResult run(ServerPlayer player, Entity entity, InteractionHand hand);
    }
    
}
