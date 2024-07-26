package team.creative.littletiles.common.structure.type.bed;

import java.util.List;

import com.mojang.datafixers.util.Either;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.event.EventHooks;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.packet.structure.BedUpdate;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.directional.StructureDirectional;
import team.creative.littletiles.common.structure.directional.StructureDirectionalField;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.SignalState;

public class LittleBed extends LittleStructure {
    
    public LittleBed(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    private Player sleepingPlayer = null;
    @OnlyIn(Dist.CLIENT)
    public Vec3d playerPostion;
    @StructureDirectional
    public Facing direction;
    
    @Override
    protected void loadExtra(CompoundTag nbt) {}
    
    @Override
    protected void saveExtra(CompoundTag nbt) {}
    
    @Override
    protected Object failedLoadingRelative(CompoundTag nbt, StructureDirectionalField field) {
        if (field.key.equals("facing"))
            return Facing.get(nbt.getInt("direction"));
        return super.failedLoadingRelative(nbt, field);
    }
    
    @Override
    public boolean isBed(LivingEntity player) {
        return true;
    }
    
    @Override
    public Direction getBedDirection() {
        return direction.toVanilla();
    }
    
    public Player getSleepingPlayer() {
        return sleepingPlayer;
    }
    
    public void wakeUp() {
        setSleepingPlayer(null);
        broadcastPacket(new BedUpdate(getStructureLocation()));
    }
    
    @OnlyIn(Dist.CLIENT)
    public void setSleepingPlayerClient(Player player) {
        this.sleepingPlayer = player;
    }
    
    private void setSleepingPlayer(Player player) {
        this.sleepingPlayer = player;
        if (!isClient())
            getInput(0).updateState(SignalState.of(player != null));
    }
    
    public Player.BedSleepingProblem trySleep(ServerPlayer player, Vec3d highest) {
        BlockPos pos = getStructurePos();
        var vanillaResult = ((java.util.function.Supplier<Either<BedSleepingProblem, Unit>>) () -> {
            if (player.isSleeping() || !player.isAlive())
                return Either.left(Player.BedSleepingProblem.OTHER_PROBLEM);
            
            if (!player.level().dimensionType().natural())
                return Either.left(Player.BedSleepingProblem.NOT_POSSIBLE_HERE);
            player.setRespawnPosition(player.level().dimension(), pos, player.getYRot(), false, true);
            if (player.level().isDay())
                return Either.left(Player.BedSleepingProblem.NOT_POSSIBLE_NOW);
            if (!player.isCreative()) {
                Vec3 vec3 = highest.toVanilla();
                List<Monster> list = player.level().getEntitiesOfClass(Monster.class, new AABB(vec3.x() - 8.0, vec3.y() - 5.0, vec3.z() - 8.0, vec3.x() + 8.0, vec3.y() + 5.0, vec3
                        .z() + 8.0), monster -> monster.isPreventingPlayerRest(player));
                if (!list.isEmpty())
                    return Either.left(Player.BedSleepingProblem.NOT_SAFE);
            }
            return Either.right(Unit.INSTANCE);
        }).get();
        
        vanillaResult = EventHooks.canPlayerStartSleeping(player, pos, vanillaResult);
        if (vanillaResult.left().isPresent())
            return vanillaResult.left().get();
        
        ((ILittleBedPlayerExtension) player).setSleepingCounter(0);
        
        if (player.isPassenger())
            player.stopRiding();
        
        setSleepingPlayer(player);
        ((ILittleBedPlayerExtension) player).setBed(this);
        broadcastPacket(new BedUpdate(getStructureLocation(), player));
        
        player.setPose(Pose.SLEEPING);
        
        player.setPos(highest.x, highest.y, highest.z);
        player.setSleepingPos(getStructurePos());
        player.setDeltaMovement(Vec3.ZERO);
        player.hasImpulse = true;
        
        if (player instanceof ServerPlayer sPlayer) {
            player.awardStat(Stats.SLEEP_IN_BED);
            CriteriaTriggers.SLEPT_IN_BED.trigger(sPlayer);
            
            if (!sPlayer.serverLevel().canSleepThroughNights())
                player.displayClientMessage(Component.translatable("sleep.not_possible"), true);
            
            sPlayer.serverLevel().updateSleepingPlayerList();
        }
        return null;
    }
    
    @Override
    public void tileDestroyed() throws CorruptedConnectionException, NotYetConnectedException {
        super.tileDestroyed();
        if (sleepingPlayer != null)
            ((ILittleBedPlayerExtension) sleepingPlayer).setBed(null);
    }
    
    @Override
    public boolean canInteract() {
        return true;
    }
    
    @Override
    public InteractionResult use(Level level, LittleTileContext context, BlockPos pos, Player player, BlockHitResult result) {
        try {
            checkConnections();
            
            if (!LittleTiles.CONFIG.general.enableBed)
                return InteractionResult.PASS;
            
            if (level.isClientSide)
                return InteractionResult.CONSUME;
            
            if (level.dimensionType().bedWorks()) {
                
                Vec3d vec = getHighestCenterVec();
                if (this.sleepingPlayer != null) {
                    player.sendSystemMessage(Component.translatable("tile.bed.occupied", new Object[0]));
                    return InteractionResult.SUCCESS;
                }
                
                BedSleepingProblem problem = trySleep((ServerPlayer) player, vec);
                
                if (problem != null)
                    player.displayClientMessage(problem.getMessage(), true);
                return InteractionResult.SUCCESS;
            }
        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        
        return InteractionResult.SUCCESS;
    }
    
}
