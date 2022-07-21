package team.creative.littletiles.common.structure.type.bed;

import java.util.List;
import java.util.Optional;

import com.creativemd.creativecore.common.utils.math.RotationUtils;

import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.EnumFacing;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButton;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.utils.BooleanUtils;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.gui.controls.GuiDirectionIndicator;
import team.creative.littletiles.common.gui.controls.GuiTileViewer;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.packet.structure.BedUpdate;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.directional.StructureDirectional;
import team.creative.littletiles.common.structure.directional.StructureDirectionalField;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.registry.LittleStructureGuiParser;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.type.door.LittleSlidingDoor.LittleSlidingDoorParser;

public class LittleBed extends LittleStructure {
    
    public LittleBed(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    private Player sleepingPlayer = null;
    @OnlyIn(Dist.CLIENT)
    public Vec3d playerPostion;
    @StructureDirectional
    public Facing direction;
    
    @OnlyIn(Dist.CLIENT)
    public boolean hasBeenActivated;
    
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
    
    public Player getSleepingPlayer() {
        return sleepingPlayer;
    }
    
    public void setSleepingPlayer(Player player) {
        this.sleepingPlayer = player;
        if (!getLevel().isClientSide)
            getInput(0).updateState(BooleanUtils.asArray(player != null));
    }
    
    public Player.BedSleepingProblem trySleep(Player player, Vec3d highest) {
        Player.BedSleepingProblem ret = ForgeEventFactory.onPlayerSleepInBed(player, Optional.empty());
        if (ret != null)
            return ret;
        
        if (!player.isSleeping() && player.isAlive()) {
            if (!player.level.dimensionType().natural())
                return Player.BedSleepingProblem.NOT_POSSIBLE_HERE;
            
            if (player instanceof ServerPlayer sPlayer)
                sPlayer.setRespawnPosition(player.level.dimension(), player.blockPosition(), player.getYRot(), false, true);
            
            if (!ForgeEventFactory.fireSleepingTimeCheck(player, Optional.empty()))
                return Player.BedSleepingProblem.NOT_POSSIBLE_NOW;
            
            if (!player.isCreative()) {
                Vec3 vec3 = highest.toVanilla();
                List<Monster> list = player.level.getEntitiesOfClass(Monster.class, new AABB(vec3.x() - 8.0D, vec3.y() - 5.0D, vec3.z() - 8.0D, vec3.x() + 8.0D, vec3
                        .y() + 5.0D, vec3.z() + 8.0D), (p_9062_) -> {
                            return p_9062_.isPreventingPlayerRest(player);
                        });
                if (!list.isEmpty())
                    return Player.BedSleepingProblem.NOT_SAFE;
            }
            
            ((ILittleBedPlayerExtension) player).setSleepingCounter(0);
            
            if (player.isPassenger())
                player.stopRiding();
            
            setSleepingPlayer(player);
            ((ILittleBedPlayerExtension) player).setBed(this);
            
            player.setPose(Pose.SLEEPING);
            
            player.setPos(highest.x - 0.5, highest.y, highest.z - 0.5F);
            player.setSleepingPos(getPos());
            player.setDeltaMovement(Vec3.ZERO);
            player.hasImpulse = true;
            
            if (player instanceof ServerPlayer sPlayer) {
                player.awardStat(Stats.SLEEP_IN_BED);
                CriteriaTriggers.SLEPT_IN_BED.trigger(sPlayer);
                
                if (!sPlayer.getLevel().canSleepThroughNights())
                    player.displayClientMessage(new TranslatableComponent("sleep.not_possible"), true);
                
                sPlayer.getLevel().updateSleepingPlayerList();
            }
            return null;
        }
        
        return Player.BedSleepingProblem.OTHER_PROBLEM;
    }
    
    @Override
    public void onLittleTileDestroy() throws CorruptedConnectionException, NotYetConnectedException {
        super.onLittleTileDestroy();
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
            
            if (level.isClientSide) {
                hasBeenActivated = true;
                return InteractionResult.CONSUME;
            }
            if (level.dimensionType().bedWorks()) {
                
                Vec3d vec = getHighestCenterVec();
                if (this.sleepingPlayer != null) {
                    player.sendMessage(new TranslatableComponent("tile.bed.occupied", new Object[0]), Util.NIL_UUID);
                    return InteractionResult.SUCCESS;
                }
                
                BedSleepingProblem problem = trySleep(player, vec);
                
                if (problem != null)
                    player.displayClientMessage(problem.getMessage(), true);
                else {
                    LittleTiles.NETWORK.sendToClient(new BedUpdate(getStructureLocation()), (ServerPlayer) player);
                    LittleTiles.NETWORK.sendToClientTracking(new BedUpdate(getStructureLocation(), player), player);
                }
                return InteractionResult.SUCCESS;
            }
        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        
        return InteractionResult.SUCCESS;
    }
    
    public static class LittleBedParser extends LittleStructureGuiParser {
        
        public LittleBedParser(GuiParent parent, AnimationGuiHandler handler) {
            super(parent, handler);
            parent.registerEventClick(x -> {
                GuiTileViewer viewer = (GuiTileViewer) parent.get("tileviewer");
                GuiDirectionIndicator relativeDirection = (GuiDirectionIndicator) parent.get("relativeDirection");
                
                EnumFacing direction = EnumFacing.getHorizontal(((GuiStateButton) parent.get("direction")).getState());
                
                LittleSlidingDoorParser.updateDirection(viewer, direction.getOpposite(), relativeDirection);
            });
        }
        
        @Override
        public void createControls(LittleGroup previews, LittleStructure structure) {
            GuiTileViewer tile = new GuiTileViewer("tileviewer", previews.getGrid());
            tile.setViewDirection(Facing.UP);
            parent.add(tile);
            
            LittleVec size = previews.getSize();
            int index = EnumFacing.EAST.getHorizontalIndex();
            if (size.x < size.z)
                index = EnumFacing.SOUTH.getHorizontalIndex();
            if (structure instanceof LittleBed)
                index = ((LittleBed) structure).direction.getHorizontalIndex();
            if (index < 0)
                index = 0;
            parent.add(new GuiStateButton("direction", index, RotationUtils.getHorizontalFacingNames()));
            
            GuiDirectionIndicator relativeDirection = new GuiDirectionIndicator("relativeDirection", Facing.UP);
            parent.add(relativeDirection);
            LittleSlidingDoorParser.updateDirection(tile, Facing.getHorizontal(index).getOpposite(), relativeDirection);
        }
        
        @Override
        public LittleBed parseStructure(LittleGroup previews) {
            Facing direction = Facing.getHorizontal(((GuiStateButton) parent.get("direction")).getState());
            LittleBed bed = createStructure(LittleBed.class, null);
            bed.direction = direction;
            return bed;
        }
        
        @Override
        protected LittleStructureType getStructureType() {
            return LittleStructureRegistry.getStructureType(LittleBed.class);
        }
    }
    
}
