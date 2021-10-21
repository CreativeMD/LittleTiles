package team.creative.littletiles.common.structure.type;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.datafixers.util.Either;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Unit;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButton;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.utils.BooleanUtils;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.gui.controls.GuiDirectionIndicator;
import team.creative.littletiles.common.gui.controls.GuiTileViewer;
import team.creative.littletiles.common.math.box.LittleBox;
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
    
    @OnlyIn(Dist.CLIENT)
    public static float getBedOrientationInDegrees(Player player) {
        try {
            LittleStructure bed = (LittleStructure) littleBed.get(player);
            if (bed instanceof LittleBed)
                switch (((LittleBed) bed).direction) {
                case SOUTH:
                    return 90.0F;
                case WEST:
                    return 0.0F;
                case NORTH:
                    return 270.0F;
                case EAST:
                    return 180.0F;
                default:
                    return 0;
                }
            
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        
        BlockState state = player.bedLocation == null ? null : player.level.getBlockState(player.bedLocation);
        if (state != null && state.getBlock().isBed(state, player.level, player.bedLocation, player)) {
            Direction enumfacing = state.getBlock().getBedDirection(state, player.level, player.bedLocation);
            
            switch (enumfacing) {
            case SOUTH:
                return 90.0F;
            case WEST:
                return 0.0F;
            case NORTH:
                return 270.0F;
            case EAST:
                return 180.0F;
            }
        }
        
        return 0.0F;
    }
    
    public static Method setSize = ReflectionHelper.findMethod(Entity.class, "setSize", "func_70105_a", float.class, float.class);
    
    public static Field sleeping = ReflectionHelper.findField(EntityPlayer.class, new String[] { "sleeping", "field_71083_bS" });
    public static Field sleepTimer = ReflectionHelper.findField(EntityPlayer.class, new String[] { "sleepTimer", "field_71076_b" });
    
    public static Field littleBed = ReflectionHelper.findField(EntityPlayer.class, "littleBed");;
    
    public Either<Player.BedSleepingProblem, Unit> trySleep(Player player, Vec3d highest) {
        if (!player.world.isRemote) {
            if (player.isPlayerSleeping() || !player.isEntityAlive()) {
                return EntityPlayer.SleepResult.OTHER_PROBLEM;
            }
            
            if (!player.world.provider.isSurfaceWorld()) {
                return EntityPlayer.SleepResult.NOT_POSSIBLE_HERE;
            }
            
            if (player.world.isDaytime()) {
                return EntityPlayer.SleepResult.NOT_POSSIBLE_NOW;
            }
            
            double d0 = 8.0D;
            double d1 = 5.0D;
            List<EntityMob> list = player.world
                    .<EntityMob>getEntitiesWithinAABB(EntityMob.class, new AxisAlignedBB(highest.x - 8.0D, highest.y - 5.0D, highest.z - 8.0D, highest.x + 8.0D, highest.y + 5.0D, highest.z + 8.0D));
            
            if (!list.isEmpty()) {
                return EntityPlayer.SleepResult.NOT_SAFE;
            }
        }
        
        if (player.isRiding()) {
            player.dismountRidingEntity();
        }
        if (player.world.isRemote)
            playerPostion = highest;
        setSleepingPlayer(player);
        
        try {
            LittleBed.littleBed.set(player, this);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        
        try {
            setSize.invoke(player, 0.2F, 0.2F);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        
        float f1 = 0.5F + direction.getFrontOffsetX() * 0.8F;
        float f = 0.5F + direction.getFrontOffsetZ() * 0.8F;
        
        player.renderOffsetX = -1.8F * direction.getFrontOffsetX();
        player.renderOffsetZ = -1.8F * direction.getFrontOffsetZ();
        player.setPosition((float) highest.x - 0.5F + f1, ((float) highest.y), (float) highest.z - 0.5F + f);
        
        try {
            sleeping.setBoolean(player, true);
            sleepTimer.setInt(player, 0);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        
        player.bedLocation = mainBlock.getPos();
        player.motionX = 0.0D;
        player.motionY = 0.0D;
        player.motionZ = 0.0D;
        
        if (!player.world.isRemote) {
            player.world.updateAllPlayersSleepingFlag();
        }
        return Player.SleepResult.OK;
    }
    
    @Override
    public void onLittleTileDestroy() throws CorruptedConnectionException, NotYetConnectedException {
        super.onLittleTileDestroy();
        if (sleepingPlayer != null) {
            try {
                littleBed.set(sleepingPlayer, null);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void setBedDirection(Entity player) {
        if (player instanceof Player) {
            try {
                LittleStructure bed = (LittleStructure) littleBed.get(player);
                if (bed instanceof LittleBed) {
                    int i = ((LittleBed) bed).direction.getHorizontalIndex();
                    
                    GlStateManager.rotate(i * 90, 0.0F, 1.0F, 0.0F);
                    
                    if (player == Minecraft.getMinecraft().player) {
                        double height = 0.2;
                        double forward = 0;
                        
                        GlStateManager
                                .translate(((LittleBed) bed).direction.getDirectionVec().getX() * forward, height, ((LittleBed) bed).direction.getDirectionVec().getZ() * forward);
                    }
                    // GlStateManager.translate(0, ((LittleBed) bed).playerPostion.getPosY() -
                    // player.posY, 0);
                    
                    // Minecraft.getMinecraft().getRenderManager().playerViewY =
                    // (float)(((LittleBed) bed).direction.getHorizontalAngle() * 90 + 180);
                    // Minecraft.getMinecraft().getRenderManager().playerViewX = 0.0F;
                    
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public boolean canInteract() {
        return true;
    }
    
    @Override
    public InteractionResult use(Level level, LittleTile tile, LittleBox box, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        load();
        
        if (!LittleTiles.CONFIG.general.enableBed)
            return InteractionResult.PASS;
        
        if (world.isRemote) {
            hasBeenActivated = true;
            return InteractionResult.SUCCESS;
        }
        if (world.provider.canRespawnHere() && world.getBiome(pos) != Biomes.HELL) {
            Vec3d vec = getHighestCenterVec();
            if (this.sleepingPlayer != null) {
                player.sendStatusMessage(new TextComponentTranslation("tile.bed.occupied", new Object[0]), true);
                return InteractionResult.SUCCESS;
            }
            
            SleepResult enumstatus = trySleep(player, vec);
            if (enumstatus == SleepResult.OK) {
                player.addStat(StatList.SLEEP_IN_BED);
                PacketHandler.sendPacketToPlayer(new BedUpdate(getStructureLocation()), (EntityPlayerMP) player);
                PacketHandler.sendPacketToTrackingPlayers(new BedUpdate(getStructureLocation(), player), (EntityPlayerMP) player);
                return InteractionResult.SUCCESS;
            } else {
                if (enumstatus == SleepResult.NOT_POSSIBLE_NOW) {
                    player.sendStatusMessage(new TextComponentTranslation("tile.bed.noSleep"), true);
                } else if (enumstatus == SleepResult.NOT_SAFE) {
                    player.sendStatusMessage(new TextComponentTranslation("tile.bed.notSafe"), true);
                }
                
                return InteractionResult.SUCCESS;
            }
        }
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
            Facing direction = EnumFacing.getHorizontal(((GuiStateButton) parent.get("direction")).getState());
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
