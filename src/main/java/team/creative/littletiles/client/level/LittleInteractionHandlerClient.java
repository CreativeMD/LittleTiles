package team.creative.littletiles.client.level;

import org.apache.commons.lang3.mutable.MutableObject;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import team.creative.littletiles.client.level.little.LittleClientLevel;
import team.creative.littletiles.common.level.handler.LevelHandler;
import team.creative.littletiles.mixin.client.MultiPlayerGameModeAccessor;
import team.creative.littletiles.mixin.client.level.ClientLevelAccessor;

public class LittleInteractionHandlerClient extends LevelHandler {
    
    private Minecraft mc = Minecraft.getInstance();
    private LittleClientLevel destroyLevel;
    private BlockPos destroyBlockPos = new BlockPos(-1, -1, -1);
    private ItemStack destroyingItem = ItemStack.EMPTY;
    private float destroyProgress;
    private float destroyTicks;
    private boolean isDestroying;
    
    public LittleInteractionHandlerClient(Level level) {
        super(level);
    }
    
    public Player getPlayer() {
        return mc.player;
    }
    
    public GameType getGameMode() {
        return mc.gameMode.getPlayerMode();
    }
    
    public ClientPacketListener getVanillaConnection() {
        return ((MultiPlayerGameModeAccessor) mc.gameMode).getConnection();
    }
    
    private void ensureHasSentCarriedItem() {
        ((MultiPlayerGameModeAccessor) mc.gameMode).callEnsureHasSentCarriedItem();
    }
    
    public boolean destroyBlock(LittleClientLevel level, BlockPos pos) {
        Player player = getPlayer();
        if (player.getMainHandItem().onBlockStartBreak(pos, player))
            return false;
        
        if (player.blockActionRestricted(level, pos, getGameMode()))
            return false;
        
        BlockState blockstate = level.getBlockState(pos);
        if (!player.getMainHandItem().getItem().canAttackBlock(blockstate, level, pos, player))
            return false;
        
        Block block = blockstate.getBlock();
        if (block instanceof GameMasterBlock && !player.canUseGameMasterBlocks())
            return false;
        if (blockstate.isAir())
            return false;
        
        FluidState fluidstate = level.getFluidState(pos);
        boolean flag = blockstate.onDestroyedByPlayer(level, pos, player, false, fluidstate);
        if (flag)
            block.destroy(level, pos, blockstate);
        
        return flag;
    }
    
    public boolean startDestroyBlock(LittleClientLevel level, BlockPos pos, Direction direction) {
        Player player = getPlayer();
        if (player.blockActionRestricted(level, pos, getGameMode()))
            return false;
        
        if (!level.getWorldBorder().isWithinBounds(pos))
            return false;
        
        if (getGameMode().isCreative()) {
            BlockState blockstate = level.getBlockState(pos);
            mc.getTutorial().onDestroyBlock(level, pos, blockstate, 1.0F);
            this.startPrediction(level, (sequence) -> {
                if (!ForgeHooks.onLeftClickBlock(player, pos, direction).isCanceled())
                    this.destroyBlock(level, pos);
                return new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, direction, sequence);
            });
            ((MultiPlayerGameModeAccessor) mc.gameMode).setDestroyDelay(5);
            return true;
        }
        
        if (!this.isDestroying || !this.sameDestroyTarget(level, pos)) {
            if (this.isDestroying)
                level.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.destroyBlockPos, direction));
            PlayerInteractEvent.LeftClickBlock event = ForgeHooks.onLeftClickBlock(player, pos, direction);
            
            BlockState blockstate1 = level.getBlockState(pos);
            mc.getTutorial().onDestroyBlock(level, pos, blockstate1, 0.0F);
            this.startPrediction(level, (sequence) -> {
                boolean flag = !blockstate1.isAir();
                if (flag && this.destroyProgress == 0.0F)
                    if (event.getUseBlock() != Event.Result.DENY)
                        blockstate1.attack(level, pos, player);
                    
                ServerboundPlayerActionPacket packet = new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, direction, sequence);
                if (event.getUseItem() == Event.Result.DENY)
                    return packet;
                
                if (flag && blockstate1.getDestroyProgress(player, level, pos) >= 1.0F)
                    this.destroyBlock(level, pos);
                else {
                    this.isDestroying = true;
                    this.destroyLevel = level;
                    this.destroyBlockPos = pos;
                    this.destroyingItem = player.getMainHandItem();
                    this.destroyProgress = 0.0F;
                    this.destroyTicks = 0.0F;
                    level.destroyBlockProgress(player.getId(), this.destroyBlockPos, (int) (this.destroyProgress * 10.0F) - 1);
                }
                
                return packet;
            });
        }
        
        return true;
    }
    
    public void stopDestroyBlock() {
        if (this.isDestroying) {
            Player player = getPlayer();
            BlockState blockstate = destroyLevel.getBlockState(this.destroyBlockPos);
            mc.getTutorial().onDestroyBlock(destroyLevel, this.destroyBlockPos, blockstate, -1.0F);
            destroyLevel.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.destroyBlockPos, Direction.DOWN));
            this.isDestroying = false;
            this.destroyLevel = null;
            this.destroyProgress = 0.0F;
            level.destroyBlockProgress(player.getId(), this.destroyBlockPos, -1);
            player.resetAttackStrengthTicker();
        }
        
    }
    
    public boolean continueDestroyBlock(LittleClientLevel level, BlockPos pos, Direction direction) {
        ensureHasSentCarriedItem();
        if (((MultiPlayerGameModeAccessor) mc.gameMode).getDestroyDelay() > 0) {
            ((MultiPlayerGameModeAccessor) mc.gameMode).setDestroyDelay(((MultiPlayerGameModeAccessor) mc.gameMode).getDestroyDelay() - 1);
            return true;
        }
        
        Player player = getPlayer();
        
        if (getGameMode().isCreative() && level.getWorldBorder().isWithinBounds(pos)) {
            ((MultiPlayerGameModeAccessor) mc.gameMode).setDestroyDelay(5);
            BlockState blockstate1 = level.getBlockState(pos);
            mc.getTutorial().onDestroyBlock(level, pos, blockstate1, 1.0F);
            this.startPrediction(level, (sequence) -> {
                if (!ForgeHooks.onLeftClickBlock(player, pos, direction).isCanceled())
                    this.destroyBlock(level, pos);
                return new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, direction, sequence);
            });
            return true;
        }
        
        if (this.sameDestroyTarget(level, pos)) {
            BlockState blockstate = level.getBlockState(pos);
            if (blockstate.isAir()) {
                this.isDestroying = false;
                this.destroyLevel = null;
                return false;
            }
            
            this.destroyProgress += blockstate.getDestroyProgress(player, level, pos);
            if (this.destroyTicks % 4.0F == 0.0F) {
                SoundType soundtype = blockstate.getSoundType(level, pos, player);
                mc.getSoundManager().play(new SimpleSoundInstance(soundtype
                        .getHitSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 8.0F, soundtype.getPitch() * 0.5F, SoundInstance.createUnseededRandom(), pos));
            }
            
            ++this.destroyTicks;
            mc.getTutorial().onDestroyBlock(level, pos, blockstate, Mth.clamp(this.destroyProgress, 0.0F, 1.0F));
            if (ForgeHooks.onLeftClickBlock(player, pos, direction).getUseItem() == net.minecraftforge.eventbus.api.Event.Result.DENY)
                return true;
            if (this.destroyProgress >= 1.0F) {
                this.isDestroying = false;
                this.destroyLevel = null;
                this.startPrediction(level, (sequence) -> {
                    this.destroyBlock(level, pos);
                    return new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, pos, direction, sequence);
                });
                this.destroyProgress = 0.0F;
                this.destroyTicks = 0.0F;
                ((MultiPlayerGameModeAccessor) mc.gameMode).setDestroyDelay(5);
            }
            
            level.destroyBlockProgress(player.getId(), this.destroyBlockPos, (int) (this.destroyProgress * 10.0F) - 1);
            return true;
        }
        return this.startDestroyBlock(level, pos, direction);
    }
    
    private void startPrediction(LittleClientLevel level, PredictiveAction p_233731_) {
        try (BlockStatePredictionHandler blockstatepredictionhandler = ((ClientLevelAccessor) level).callGetBlockStatePredictionHandler().startPredicting()) {
            int i = blockstatepredictionhandler.currentSequence();
            Packet<ServerGamePacketListener> packet = p_233731_.predict(i);
            level.connection.send(packet);
        }
        
    }
    
    public float getPickRange() {
        return (float) getPlayer().getReachDistance();
    }
    
    private boolean sameDestroyTarget(LittleClientLevel level, BlockPos pos) {
        if (this.level != destroyLevel)
            return false;
        ItemStack itemstack = getPlayer().getMainHandItem();
        boolean flag = this.destroyingItem.isEmpty() && itemstack.isEmpty();
        if (!this.destroyingItem.isEmpty() && !itemstack.isEmpty()) {
            flag = !this.destroyingItem.shouldCauseBlockBreakReset(itemstack);
        }
        
        return pos.equals(this.destroyBlockPos) && flag;
    }
    
    public InteractionResult useItemOn(LittleClientLevel level, LocalPlayer player, InteractionHand hand, BlockHitResult result) {
        ensureHasSentCarriedItem();
        if (!level.getWorldBorder().isWithinBounds(result.getBlockPos()))
            return InteractionResult.FAIL;
        MutableObject<InteractionResult> mutableobject = new MutableObject<>();
        this.startPrediction(level, (sequence) -> {
            mutableobject.setValue(this.performUseItemOn(level, player, hand, result));
            return new ServerboundUseItemOnPacket(hand, result, sequence);
        });
        return mutableobject.getValue();
    }
    
    private InteractionResult performUseItemOn(LittleClientLevel level, LocalPlayer player, InteractionHand hand, BlockHitResult hit) {
        BlockPos blockpos = hit.getBlockPos();
        ItemStack itemstack = player.getItemInHand(hand);
        PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(player, hand, blockpos, hit);
        if (event.isCanceled())
            return event.getCancellationResult();
        
        if (this.getGameMode() == GameType.SPECTATOR)
            return InteractionResult.SUCCESS;
        
        UseOnContext useoncontext = new UseOnContext(level, player, hand, itemstack, hit);
        if (event.getUseItem() != Event.Result.DENY) {
            InteractionResult result = itemstack.onItemUseFirst(useoncontext);
            if (result != InteractionResult.PASS)
                return result;
        }
        
        boolean flag = !player.getMainHandItem().doesSneakBypassUse(player.getLevel(), blockpos, player) || !player.getOffhandItem()
                .doesSneakBypassUse(player.getLevel(), blockpos, player);
        boolean flag1 = player.isSecondaryUseActive() && flag;
        BlockState blockstate = level.getBlockState(blockpos);
        if (!getVanillaConnection().isFeatureEnabled(blockstate.getBlock().requiredFeatures()))
            return InteractionResult.FAIL;
        
        if (event.getUseBlock() == Event.Result.ALLOW || (event.getUseBlock() != Event.Result.DENY && !flag1)) {
            InteractionResult interactionresult = blockstate.use(level, player, hand, hit);
            if (interactionresult.consumesAction())
                return interactionresult;
        }
        
        if (event.getUseItem() == Event.Result.DENY)
            return InteractionResult.PASS;
        
        if (event.getUseItem() == Event.Result.ALLOW || (!itemstack.isEmpty() && !player.getCooldowns().isOnCooldown(itemstack.getItem()))) {
            if (this.getGameMode().isCreative()) {
                int i = itemstack.getCount();
                InteractionResult interactionresult1 = itemstack.useOn(useoncontext);
                itemstack.setCount(i);
                return interactionresult1;
            }
            return itemstack.useOn(useoncontext);
            
        }
        return InteractionResult.PASS;
    }
    
    public InteractionResult useItem(LittleClientLevel level, Player player, InteractionHand hand) {
        if (this.getGameMode() == GameType.SPECTATOR)
            return InteractionResult.PASS;
        this.ensureHasSentCarriedItem();
        getVanillaConnection().send(new ServerboundMovePlayerPacket.PosRot(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), player.isOnGround()));
        MutableObject<InteractionResult> mutableobject = new MutableObject<>();
        this.startPrediction(level, (sequence) -> {
            ServerboundUseItemPacket serverbounduseitempacket = new ServerboundUseItemPacket(hand, sequence);
            ItemStack itemstack = player.getItemInHand(hand);
            if (player.getCooldowns().isOnCooldown(itemstack.getItem())) {
                mutableobject.setValue(InteractionResult.PASS);
                return serverbounduseitempacket;
            }
            
            InteractionResult cancelResult = ForgeHooks.onItemRightClick(player, hand);
            if (cancelResult != null) {
                mutableobject.setValue(cancelResult);
                return serverbounduseitempacket;
            }
            InteractionResultHolder<ItemStack> interactionresultholder = itemstack.use(level, player, hand);
            ItemStack itemstack1 = interactionresultholder.getObject();
            if (itemstack1 != itemstack) {
                player.setItemInHand(hand, itemstack1);
                if (itemstack1.isEmpty())
                    ForgeEventFactory.onPlayerDestroyItem(player, itemstack, hand);
            }
            
            mutableobject.setValue(interactionresultholder.getResult());
            return serverbounduseitempacket;
        });
        return mutableobject.getValue();
    }
    
    public void attack(LittleClientLevel level, Player player, Entity entity) {
        ensureHasSentCarriedItem();
        level.connection.send(ServerboundInteractPacket.createAttackPacket(entity, player.isShiftKeyDown()));
        if (this.getGameMode() != GameType.SPECTATOR) {
            player.attack(entity);
            player.resetAttackStrengthTicker();
        }
    }
    
    public InteractionResult interact(LittleClientLevel level, Player player, Entity entity, InteractionHand hand) {
        this.ensureHasSentCarriedItem();
        level.connection.send(ServerboundInteractPacket.createInteractionPacket(entity, player.isShiftKeyDown(), hand));
        return this.getGameMode() == GameType.SPECTATOR ? InteractionResult.PASS : player.interactOn(entity, hand);
    }
    
    public InteractionResult interactAt(LittleClientLevel level, Player player, Entity entity, EntityHitResult hit, InteractionHand hand) {
        this.ensureHasSentCarriedItem();
        Vec3 vec3 = hit.getLocation().subtract(entity.getX(), entity.getY(), entity.getZ());
        level.connection.send(ServerboundInteractPacket.createInteractionPacket(entity, player.isShiftKeyDown(), hand, vec3));
        if (this.getGameMode() == GameType.SPECTATOR)
            return InteractionResult.PASS; // don't fire for spectators to match non-specific EntityInteract
        InteractionResult cancelResult = ForgeHooks.onInteractEntityAt(player, entity, hit, hand);
        if (cancelResult != null)
            return cancelResult;
        return this.getGameMode() == GameType.SPECTATOR ? InteractionResult.PASS : entity.interactAt(player, vec3, hand);
    }
    
    public void releaseUsingItem(LittleClientLevel level, Player player) {
        this.ensureHasSentCarriedItem();
        level.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
        player.releaseUsingItem();
    }
    
    public boolean isDestroying() {
        return this.isDestroying;
    }
    
    public void handlePickItem(int slot) {
        getVanillaConnection().send(new ServerboundPickItemPacket(slot));
    }
}
