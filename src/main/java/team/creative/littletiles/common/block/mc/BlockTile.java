package team.creative.littletiles.common.block.mc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

import com.creativemd.littletiles.client.render.cache.LayeredRenderBoxCache;
import com.creativemd.littletiles.client.render.tile.LittleRenderBox;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.World;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements.Type;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext.Builder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.util.ForgeSoundType;
import team.chisel.ctm.api.IFacade;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.face.CachedFaceRenderType;
import team.creative.creativecore.client.render.face.FaceRenderType;
import team.creative.creativecore.client.render.model.ICreativeRendered;
import team.creative.creativecore.common.level.CreativeLevel;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.type.Pair;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActionActivated;
import team.creative.littletiles.common.action.LittleActionDestroy;
import team.creative.littletiles.common.api.block.LittlePhysicBlock;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.block.little.tile.parent.ParentCollection;
import team.creative.littletiles.common.block.little.tile.parent.StructureParentCollection;
import team.creative.littletiles.common.item.ItemBlockTiles;
import team.creative.littletiles.common.item.ItemLittlePaintBrush;
import team.creative.littletiles.common.item.ItemLittleSaw;
import team.creative.littletiles.common.item.ItemLittleWrench;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.face.LittleFace;
import team.creative.littletiles.common.mod.ctm.CTMManager;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureAttribute;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.type.LittleBed;
import team.creative.littletiles.server.LittleTilesServer;

public class BlockTile extends BaseEntityBlock implements IFacade, LittlePhysicBlock {
    
    private static boolean loadingBlockEntityFromWorld = false;
    public static Minecraft mc = Minecraft.getInstance(); // Note that doesn't work
    
    public static BETiles loadBE(BlockGetter level, BlockPos pos) {
        if (level == null)
            return null;
        loadingBlockEntityFromWorld = true;
        BlockEntity be = null;
        try {
            be = level.getBlockEntity(pos);
        } catch (Exception e) {
            return null;
        }
        loadingBlockEntityFromWorld = false;
        if (be instanceof BETiles && ((BETiles) be).hasLoaded())
            return (BETiles) be;
        return null;
    }
    
    public static boolean selectEntireBlock(Player player, boolean secondMode) {
        return secondMode && !(player.getMainHandItem().getItem() instanceof ItemLittleSaw) && !(player.getMainHandItem().getItem() instanceof ItemLittlePaintBrush);
    }
    
    public static final SoundType SILENT = new ForgeSoundType(-1.0F, 1.0F, () -> SoundEvents.STONE_BREAK, () -> SoundEvents.STONE_STEP, () -> SoundEvents.STONE_PLACE, () -> SoundEvents.STONE_HIT, () -> SoundEvents.STONE_FALL);
    
    public final boolean ticking;
    public final boolean rendered;
    
    public BlockTile(Material material, boolean ticking, boolean rendered) {
        super(BlockBehaviour.Properties.of(material).explosionResistance(3.0F).sound(SILENT));
        this.ticking = ticking;
        this.rendered = rendered;
    }
    
    public static BlockState getStateByAttribute(int attribute) {
        return getState(LittleStructureAttribute.ticking(attribute), LittleStructureAttribute.tickRendering(attribute));
    }
    
    public static BlockState getState(boolean ticking, boolean rendered) {
        return rendered ? (ticking ? LittleTiles.BLOCK_TILES_TICKING_RENDERED.defaultBlockState() : LittleTiles.BLOCK_TILES_RENDERED
                .defaultBlockState()) : (ticking ? LittleTiles.BLOCK_TILES_TICKING.defaultBlockState() : LittleTiles.BLOCK_TILES.defaultBlockState());
    }
    
    public static BlockState getState(BETiles te) {
        return getState(te.isTicking(), te.isRendered());
    }
    
    public static BlockState getState(List<StructureParentCollection> structures) {
        boolean ticking = false;
        boolean rendered = false;
        for (StructureParentCollection structure : structures) {
            if (LittleStructureAttribute.ticking(structure.getAttribute()))
                ticking = true;
            if (LittleStructureAttribute.tickRendering(structure.getAttribute()))
                rendered = true;
            
            if (ticking && rendered)
                break;
        }
        return getState(ticking, rendered);
    }
    
    @Override
    public RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.MODEL;
    }
    
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BETiles be = loadBE(level, pos);
        VoxelShape shape = Shapes.empty();
        
        if (be != null) {
            for (IParentCollection list : be.groups()) {
                if (list.isStructure() && LittleStructureAttribute.noCollision(list.getAttribute()))
                    continue;
                if (list.isStructure() && LittleStructureAttribute.extraCollision(list.getAttribute()))
                    try {
                        shape = Shapes.or(shape, list.getStructure().getExtraShape(state, level, pos, context));
                    } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                
                for (LittleTile tile : list)
                    if (!tile.getBlock().noCollision())
                        shape = Shapes.or(shape, tile.getShapes(list));
            }
        }
        return shape;
    }
    
    @Override
    @Deprecated
    public float getShadeBrightness(BlockState p_60472_, BlockGetter p_60473_, BlockPos p_60474_) {
        // TODO Not sure if this should be used or not
        return p_60472_.isCollisionShapeFullBlock(p_60473_, p_60474_) ? 0.2F : 1.0F;
    }
    
    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        // TODO expensive but accurate, maybe it can be set to false by an option
        return true;
    }
    
    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        // TODO Interesting thing to add (note this runs on sever side)
        return !isShapeFullBlock(state.getShape(level, pos)) && state.getFluidState().isEmpty();
    }
    
    @Override
    public boolean skipRendering(BlockState state, BlockState state2, Direction direction) {
        return false;
    }
    
    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        BETiles be = loadBE(level, pos);
        if (be != null)
            return be.sideCache.isCollisionFullBlock();
        return false;
    }
    
    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        //Shape for things that block the view of the camera, should exclude water for example
        return this.getCollisionShape(state, level, pos, context);
    }
    
    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        // its cached state wise ... which is a big problem, maybe try to keep the cache empty????
        VoxelShape shape = Shapes.empty();
        BETiles be = loadBE(level, pos);
        if (be != null) {
            for (Pair<IParentCollection, LittleTile> pair : be.allTiles()) {
                if (pair.key.isStructure() && LittleStructureAttribute.lightEmitter(pair.key.getAttribute()))
                    continue;
                if (pair.value.getBlock().isTranslucent())
                    continue;
                shape = Shapes.or(shape, pair.value.getShapes(pair.key));
            }
        }
        
        return shape;
    }
    
    @Override
    @Deprecated
    public VoxelShape getBlockSupportShape(BlockState p_60581_, BlockGetter p_60582_, BlockPos p_60583_) {
        //TODO Shape that other stuff can be placed on, same as collision shape but maybe should exclude materials like glass not sure yet.
        return this.getCollisionShape(p_60581_, p_60582_, p_60583_, CollisionContext.empty());
    }
    
    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        // All tiles that can interact (be right clicked)
        VoxelShape shape = Shapes.empty();
        BETiles be = loadBE(level, pos);
        if (be != null) {
            for (IParentCollection parent : be.groups()) {
                try {
                    boolean canInteract = parent.isStructure() && parent.getStructure().canInteract();
                    for (LittleTile tile : parent)
                        if (canInteract || tile.canInteract())
                            shape = Shapes.or(shape, tile.getShapes(parent));
                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                
            }
        }
        
        return shape;
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // get Selection shape and it's also used for some other stuff I don't know (only works on client side) TODO CHECK if that is the case
        LittleTileContext tileContext = LittleTileContext.selectFocused(level, pos, mc.player);
        if (tileContext.isComplete()) {
            if (selectEntireBlock(mc.player, LittleActionHandlerClient.isUsingSecondMode()))
                return tileContext.parent.getBE().getBlockShape();
            if (LittleTiles.CONFIG.rendering.highlightStructureBox && tileContext.parent.isStructure())
                try {
                    return tileContext.parent.getStructure().getSurroundingBox().getShape();
                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
            return tileContext.tile.getShapes(tileContext.parent);
        }
        return Shapes.empty();
    }
    
    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        //TODO requires some more work
        return false;
    }
    
    @Override
    @Nullable
    public BlockPathTypes getAiPathNodeType(BlockState state, BlockGetter world, BlockPos pos, @Nullable Mob entity) {
        return state.getBlock() == Blocks.LAVA ? BlockPathTypes.LAVA : state.isBurning(world, pos) ? BlockPathTypes.DAMAGE_FIRE : null;
    }
    
    @Override
    public void setBedOccupied(BlockState state, Level world, BlockPos pos, LivingEntity sleeper, boolean occupied) {}
    
    @Override
    public boolean isBed(BlockState state, BlockGetter level, BlockPos pos, @Nullable Entity player) {
        BETiles be = loadBE(level, pos);
        if (be != null) {
            LittleStructure bed = null;
            if (player != null) {
                try {
                    bed = (LittleStructure) LittleBed.littleBed.get(player);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            
            for (LittleStructure structure : be.loadedStructures())
                if (structure == bed || structure.isBed((LivingEntity) player))
                    return true;
                
        }
        return false;
    }
    
    @Override
    public Direction getBedDirection(BlockState state, LevelReader world, BlockPos pos) {
        return Direction.SOUTH;
    }
    
    /*@Override
    public MaterialColor getMapColor(BlockGetter level, BlockPos pos) {
        // TODO Needs custom state? Not sure if this is even possible
        BETiles te = loadBE(level, pos);
        if (te != null) {
            double biggest = 0;
            LittleTile tile = null;
            for (Pair<IParentCollection, LittleTile> pair : te.allTiles()) {
                double tempVolume = pair.value.getVolume();
                if (tempVolume > biggest) {
                    biggest = tempVolume;
                    tile = pair.value;
                }
            }
            
            if (tile != null)
                return tile.getBlock().getState().getMapColor(level, pos);
        }
        return super.defaultMaterialColor();
    }*/
    
    @Override
    public Optional<Vec3> getRespawnPosition(BlockState state, EntityType<?> type, LevelReader level, BlockPos pos, float orientation, @Nullable LivingEntity entity) {
        if (isBed(state, level, pos, entity) && level instanceof Level && level.dimensionType().bedWorks())
            return BedBlock.findStandUpPosition(type, level, pos, orientation);
        
        return Optional.empty();
    }
    
    @Override
    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        BETiles be = loadBE(level, pos);
        if (be != null && entity != null && entity.getBoundingBox() != null) {
            AABB bb = entity.getBoundingBox().inflate(0.001);
            for (IStructureParentCollection structure : be.structures())
                if (LittleStructureAttribute.ladder(structure.getAttribute()))
                    for (LittleTile tile : structure)
                        if (tile.intersectsWith(bb, structure))
                            return true;
        }
        return false;
    }
    
    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        LittleTileContext context = LittleTileContext.selectFocused(level, pos, player);
        THIS THING IS ALSO ON SERVER SIDE, NO IDEA HOW TO DEAL WITH IT
        if (result.isComplete()) {
            
            state = result.tile.getBlockState();
            
            float hardness = state.getBlockHardness(world, pos);
            if (hardness < 0.0F)
                return 0.0F;
            
            if (!canHarvestBlock(player, state)) {
                return player.getDigSpeed(state, pos) / hardness / 40F;
            } else {
                return player.getDigSpeed(state, pos) / hardness / 20F;
            }
        } else
            return super.getPlayerRelativeBlockHardness(state, player, world, pos);
        float f = p_60466_.getDestroySpeed(p_60468_, p_60469_);
        if (f == -1.0F) {
            return 0.0F;
        } else {
            int i = net.minecraftforge.common.ForgeHooks.canHarvestBlock(p_60466_, p_60467_, p_60468_, p_60469_) ? 30 : 100;
            return p_60467_.getDigSpeed(p_60466_, p_60469_) / f / i;
        }
    }
    
    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float height) {
        entity.causeFallDamage(height, 1.0F, DamageSource.FALL);
        //TODO Implement bed and slime block
    }
    
    @Override
    public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
        //TODO Implement bed and slime block
    }
    
    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list) {}
    
    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {}
    
    @Override
    protected void spawnDestroyParticles(Level p_152422_, Player p_152423_, BlockPos p_152424_, BlockState p_152425_) {
        p_152422_.levelEvent(p_152423_, 2001, p_152424_, getId(p_152425_));
    }
    
    @Override
    public void playerWillDestroy(Level p_49852_, BlockPos p_49853_, BlockState p_49854_, Player p_49855_) {
        this.spawnDestroyParticles(p_49852_, p_49855_, p_49853_, p_49854_);
        if (p_49854_.is(BlockTags.GUARDED_BY_PIGLINS)) {
            PiglinAi.angerNearbyPiglins(p_49855_, false);
        }
        
        p_49852_.gameEvent(p_49855_, GameEvent.BLOCK_DESTROY, p_49853_);
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState removed, boolean p_50941_) {
        BETiles te = loadBE(level, pos); // TODO CHECK which method to use maybe playerWillDestroy is better
        if (te != null && te.isEmpty())
            super.onRemove(state, level, pos, removed, p_50941_);
    }
    
    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, net.minecraftforge.common.IPlantable plantable) {
        // TODO Could be added support for
        return false;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, Random rand) {
        if (LittleTiles.CONFIG.rendering.enableRandomDisplayTick) {
            BETiles be = loadBE(level, pos);
            if (be != null)
                for (Pair<IParentCollection, LittleTile> pair : be.allTiles())
                    pair.value.randomDisplayTick(pair.key, rand);
        }
    }
    
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (hand == InteractionHand.OFF_HAND)
            return InteractionResult.PASS;
        if (level.isClientSide)
            return useClient(state, level, pos, player, hand, result);
        return InteractionResult.PASS;
    }
    
    @OnlyIn(Dist.CLIENT)
    public InteractionResult useClient(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        LittleTileContext context = LittleTileContext.selectFocused(level, pos, player);
        if (context.isComplete() && !(player.getItemInHand(hand).getItem() instanceof ItemLittleWrench))
            return new LittleActionActivated(level, pos, player).execute(player);
        return InteractionResult.PASS;
    }
    
    @Override
    public float getFriction(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        float slipperiness = 1;
        boolean found = false;
        BETiles be = loadBE(level, pos);
        if (be != null && entity != null && entity.getBoundingBox() != null) {
            AABB bb = entity.getBoundingBox().move(0, -0.001, 0);
            for (Pair<IParentCollection, LittleTile> pair : be.allTiles()) {
                if (pair.value.intersectsWith(bb, pair.key)) {
                    slipperiness = Math.min(slipperiness, pair.value.getFriction(pair.key, entity));
                    found = true;
                }
            }
        }
        if (found)
            return slipperiness;
        return super.getFriction(state, level, pos, entity);
    }
    
    private boolean lightLoopPreventer = true;
    
    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        int light = 0;
        if (!lightLoopPreventer)
            return 0;
        BETiles te = loadBE(level, pos);
        if (te != null) {
            for (IParentCollection list : te.groups()) {
                if (list.isStructure() && LittleStructureAttribute.lightEmitter(list.getAttribute()))
                    try {
                        light = Math.max(light, list.getStructure().getLightValue(pos));
                        continue;
                    } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                
                for (LittleTile tile : list) {
                    lightLoopPreventer = false;
                    int tempLight = (int) Math.ceil(tile.getLightValue() * tile.getPercentVolume(te.getGrid()));
                    lightLoopPreventer = true;
                    if (tempLight > light)
                        light = tempLight;
                }
            }
        }
        return light;
    }
    
    @Override
    public boolean canCreatureSpawn(BlockState state, BlockGetter world, BlockPos pos, Type type, EntityType<?> entityType) {
        return false;
    }
    
    @Override
    public boolean removedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (level.isClientSide)
            return removedByPlayerClient(state, level, pos, player, willHarvest, fluid);
        return true;
    }
    
    @OnlyIn(Dist.CLIENT)
    public boolean removedByPlayerClient(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        LittleTileContext result = LittleTileContext.selectFocused(level, pos, player, 1.0F);
        if (result.isComplete())
            return new LittleActionDestroy(level, pos, player).execute(player);
        return false;
    }
    
    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        BETiles be = loadBE(context.getLevel(), context.getClickedPos());
        if (be != null)
            return be.isEmpty();
        return true;
    }
    
    @Override
    public BlockState getStateAtViewpoint(BlockState state, BlockGetter level, BlockPos pos, Vec3 viewpoint) {
        BETiles be = loadBE(level, pos);
        if (be != null) {
            int x = be.getGrid().toGrid(viewpoint.x);
            int y = be.getGrid().toGrid(viewpoint.y);
            int z = be.getGrid().toGrid(viewpoint.z);
            LittleBox box = new LittleBox(x, y, z, x + 1, y + 1, z + 1);
            for (Pair<IParentCollection, LittleTile> pair : be.allTiles())
                if (pair.value.intersectsWith(box))
                    return pair.value.getBlock().getState();
        }
        return state;
    }
    
    @Override
    public List<ItemStack> getDrops(BlockState state, Builder builder) {
        return Collections.emptyList();
    }
    
    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        LittleTileContext result = LittleTileContext.selectFocused(level, pos, player);
        if (result.isComplete()) {
            if (selectEntireBlock(mc.player, LittleActionHandlerClient.isUsingSecondMode())) {
                ItemStack drop = new ItemStack(LittleTiles.ITEM_TILES);
                LittleGroup group = new LittleGroup(result.parent.getGrid());
                for (LittleTile tile : result.parent)
                    group.add(result.parent.getGrid(), tile, tile);
                drop.setTag(LittleGroup.save(group));
                return drop;
            }
            if (result.parent.isStructure())
                try {
                    return result.parent.getStructure().getStructureDrop();
                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
            return result.tile.getDrop(result.parent.getGrid());
        }
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        BETiles te = loadBE(level, pos);
        if (te != null) {
            int heighest = 0;
            LittleTile heighestTile = null;
            for (IParentCollection list : te.groups()) {
                if (list.isStructure() && LittleStructureAttribute.noCollision(list.getAttribute()))
                    continue;
                
                for (LittleTile tile : list)
                    for (LittleBox box : tile)
                        if (box != null && box.maxY > heighest) {
                            heighest = box.maxY;
                            heighestTile = tile;
                        }
            }
            
            if (heighestTile != null)
                world.spawnParticle(EnumParticleTypes.BLOCK_DUST, entity.posX, entity.posY, entity.posZ, numberOfParticles, 0.0D, 0.0D, 0.0D, 0.15000000596046448D, new int[] { Block
                        .getStateId(heighestTile.getBlockState()) });
        }
        return true;
    }
    
    @Override
    public boolean addRunningEffects(BlockState state, Level level, BlockPos pos, Entity entity) {
        BETiles te = loadBE(level, pos);
        if (te != null) {
            int heighest = 0;
            LittleTile heighestTile = null;
            for (IParentCollection list : te.groups()) {
                if (list.isStructure() && LittleStructureAttribute.noCollision(list.getAttribute()))
                    continue;
                
                for (LittleTile tile : list)
                    for (LittleBox box : tile)
                        if (box != null && box.maxY > heighest) {
                            heighest = box.maxY;
                            heighestTile = tile;
                        }
            }
            
            Random random = new Random();
            if (heighestTile != null)
                world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, entity.posX + (random.nextFloat() - 0.5D) * entity.width, entity
                        .getEntityBoundingBox().minY + 0.1D, entity.posZ + (random.nextFloat() - 0.5D) * entity.width, -entity.motionX * 4.0D, 1.5D, -entity.motionZ * 4.0D, Block
                                .getStateId(heighestTile.getBlockState()));
            return true;
        }
        return false;
    }
    
    @OnlyIn(Dist.CLIENT)
    public SoundType getSoundTypeClient(BlockState state, LevelReader level, BlockPos pos) {
        LittleTileContext result = LittleTileContext.selectFocused(level, pos, mc.player);
        if (result.isComplete())
            return result.tile.getSound();
        return null;
    }
    
    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, Entity entity) {
        if (entity == null)
            return SILENT;
        SoundType sound = null;
        if (entity instanceof Player && level.isClientSide())
            sound = getSoundTypeClient(state, level, pos);
        
        if (sound == null) {
            // GET HEIGHEST TILE POSSIBLE
            BETiles be = loadBE(level, pos);
            if (be != null) {
                int heighest = 0;
                LittleTile heighestTile = null;
                for (IParentCollection list : be.groups()) {
                    if (list.isStructure() && LittleStructureAttribute.noCollision(list.getAttribute()))
                        continue;
                    
                    for (LittleTile tile : list)
                        for (LittleBox box : tile)
                            if (box != null && box.maxY > heighest) {
                                heighest = box.maxY;
                                heighestTile = tile;
                            }
                        
                }
                
                if (heighestTile != null)
                    return heighestTile.getSound();
            }
        }
        
        if (sound == null)
            sound = SoundType.STONE;
        return sound;
    }
    
    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        float bonus = 0F;
        BETiles be = loadBE(level, pos);
        if (be != null)
            for (Pair<IParentCollection, LittleTile> pair : be.allTiles())
                bonus += pair.value.getEnchantPowerBonus(pair.key);
        return bonus;
    }
    
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos origin, boolean p_60514_) {
        BETiles te = loadBE(level, pos);
        if (te != null) {
            te.onNeighbourChanged();
            if (!level.isClientSide)
                LittleTilesServer.NEIGHBOR.add(level, pos);
        }
    }
    
    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        BETiles te = loadBE(level, pos);
        if (te != null)
            te.onNeighbourChanged();
    }
    
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        BETiles te = loadBE(level, pos);
        if (te != null && te.shouldCheckForCollision()) {
            for (IStructureParentCollection list : te.structures())
                if (LittleStructureAttribute.collisionListener(list.getAttribute()))
                    try {
                        list.getStructure().onEntityCollidedWithBlock(level, list, pos, entity);
                    } catch (CorruptedConnectionException | NotYetConnectedException e) {}
            for (Pair<IParentCollection, LittleTile> pair : te.allTiles()) {
                if (pair.value.checkEntityCollision())
                    pair.value.entityCollided(pair.key, entity);
            }
        }
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (rendered)
            return new BETiles(LittleTiles.BE_TILES_TYPE_RENDERED, pos, state);
        return new BETiles(LittleTiles.BE_TILES_TYPE, pos, state);
    }
    
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (ticking)
            return level.isClientSide ? null : createTickerHelper(type, rendered ? LittleTiles.BE_TILES_TYPE_RENDERED : LittleTiles.BE_TILES_TYPE, BETiles::serverTick);
        return null;
    }
    
    @OnlyIn(Dist.CLIENT)
    private static BETiles checkforTileEntity(Level level, Direction facing, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos.relative(facing));
        if (be instanceof BETiles)
            return (BETiles) be;
        return null;
    }
    
    @OnlyIn(Dist.CLIENT)
    private static boolean checkforNeighbor(Level level, Direction facing, BlockPos pos) {
        BlockPos newPos = pos.relative(facing);
        BlockState state = level.getBlockState(newPos);
        return state.skipRendering(state, facing);
    }
    
    @OnlyIn(Dist.CLIENT)
    private static void updateRenderer(BETiles tileEntity, Facing facing, HashMap<Facing, Boolean> neighbors, HashMap<Facing, BETiles> neighborsTiles, RenderBox cube, LittleFace face) {
        if (face == null) {
            cube.setType(facing, FaceRenderType.INSIDE_RENDERED);
            return;
        }
        Boolean shouldRender = neighbors.get(facing);
        if (shouldRender == null) {
            shouldRender = checkforNeighbor(tileEntity.getWorld(), facing, tileEntity.getPos());
            neighbors.put(facing, shouldRender);
        }
        
        if (shouldRender) {
            TileEntityLittleTiles otherTile = null;
            if (!neighborsTiles.containsKey(facing)) {
                otherTile = checkforTileEntity(tileEntity.getWorld(), facing, tileEntity.getPos());
                neighborsTiles.put(facing, otherTile);
            } else
                otherTile = neighborsTiles.get(facing);
            if (otherTile != null) {
                face.move(facing);
                // face.face = facing.getOpposite();
                shouldRender = otherTile.shouldSideBeRendered(facing.getOpposite(), face, (LittleTile) cube.customData);
            }
        }
        if (shouldRender)
            if (((LittleTile) cube.customData).isTranslucent() && face.isPartiallyFilled())
                cube.setType(facing, new CachedFaceRenderType(face.generateFans(), (float) face.context.pixelSize, true, true));
            else
                cube.setType(facing, FaceRenderType.OUTSIDE_RENDERED);
        else
            cube.setType(facing, FaceRenderType.OUTSIDE_NOT_RENDERD);
    }
    
    @OnlyIn(Dist.CLIENT)
    public static List<LittleRenderBox> getRenderingCubes(IBlockState state, TileEntity te, ItemStack stack, BlockRenderLayer layer) {
        ArrayList<LittleRenderBox> cubes = new ArrayList<>();
        if (te instanceof TileEntityLittleTiles) {
            HashMap<EnumFacing, Boolean> neighbors = new HashMap<>();
            HashMap<EnumFacing, TileEntityLittleTiles> neighborsTiles = new HashMap<>();
            
            TileEntityLittleTiles tileEntity = (TileEntityLittleTiles) te;
            
            LayeredRenderBoxCache cache = tileEntity.render.getBoxCache();
            List<LittleRenderBox> cachedCubes = cache.get(layer);
            if (cachedCubes != null) {
                if (tileEntity.render.hasNeighbourChanged) {
                    for (BlockRenderLayer tempLayer : BlockRenderLayer.values()) {
                        List<LittleRenderBox> renderCubes = cache.get(tempLayer);
                        if (renderCubes == null)
                            continue;
                        for (int i = 0; i < renderCubes.size(); i++) {
                            LittleRenderBox cube = renderCubes.get(i);
                            for (int k = 0; k < EnumFacing.VALUES.length; k++) {
                                EnumFacing facing = EnumFacing.VALUES[k];
                                if (cube.getType(facing).isOutside()) {
                                    LittleBoxFace face = cube.box.generateFace(tileEntity.getContext(), facing);
                                    
                                    boolean shouldRenderBefore = cube.renderSide(facing);
                                    // face.move(facing);
                                    updateRenderer(tileEntity, facing, neighbors, neighborsTiles, cube, face);
                                    if (cube.renderSide(facing)) {
                                        if (!shouldRenderBefore)
                                            cube.doesNeedQuadUpdate = true;
                                    } else
                                        cube.setQuad(facing, null);
                                }
                            }
                        }
                    }
                }
                tileEntity.render.hasNeighbourChanged = false;
                return cachedCubes;
            }
            
            for (Pair<IParentCollection, LittleTile> pair : tileEntity.allTiles()) {
                LittleTile tile = pair.value;
                if (tile.shouldBeRenderedInLayer(layer)) {
                    // Check for sides which does not need to be rendered
                    LittleRenderBox cube = pair.key.getTileRenderingCube(tile, ((BETiles) te).getContext(), layer);
                    if (cube == null)
                        continue;
                    for (int k = 0; k < EnumFacing.VALUES.length; k++) {
                        EnumFacing facing = EnumFacing.VALUES[k];
                        LittleBoxFace face = cube.box.generateFace(tileEntity.getContext(), facing);
                        
                        cube.customData = tile;
                        
                        if (face == null)
                            cube.setType(facing, FaceRenderType.INSIDE_RENDERED);
                        else {
                            if (face.isFaceInsideBlock()) {
                                if (((TileEntityLittleTiles) te).shouldSideBeRendered(facing, face, tile))
                                    if (tile.isTranslucent() && face.isPartiallyFilled())
                                        cube.setType(facing, new CachedFaceRenderType(face.generateFans(), (float) face.context.pixelSize, true, false));
                                    else
                                        cube.setType(facing, FaceRenderType.INSIDE_RENDERED);
                                else
                                    cube.setType(facing, FaceRenderType.INSIDE_NOT_RENDERED);
                            } else
                                updateRenderer(tileEntity, facing, neighbors, neighborsTiles, cube, face);
                        }
                    }
                    cubes.add(cube);
                }
                
            }
            
            for (LittleStructure structure : tileEntity.loadedStructures(LittleStructureAttribute.EXTRA_RENDERING)) {
                try {
                    structure.checkConnections();
                    structure.getRenderingCubes(tileEntity.getPos(), layer, cubes);
                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                
            }
            
            cache.set(cubes, layer);
            
        } else if (stack != null)
            return ItemBlockTiles.getItemRenderingCubes(stack);
        return cubes;
    }
    
    @Override
    public boolean dropFromExplosion(Explosion p_49826_) {
        return false;
    }
    
    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        BETiles be = loadBE(level, pos);
        float calculatedResistance = 0;
        float structureResistance = 0;
        if (be != null)
            for (IParentCollection list : be.groups())
                try {
                    if (list.isStructure() && list.getStructure().getExplosionResistance() > 0)
                        structureResistance = Math.max(structureResistance, list.getStructure().getExplosionResistance());
                    else
                        for (LittleTile tile : list)
                            calculatedResistance += tile.getExplosionResistance();
                } catch (CorruptedConnectionException | NotYetConnectedException e) {
                    
                }
            
        if (calculatedResistance > structureResistance)
            return calculatedResistance;
        return structureResistance;
    }
    
    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        BETiles be = loadBE(level, pos);
        if (be != null) {
            be.updateTiles((x) -> {
                ParentCollection parent = x.noneStructureTiles();
                for (LittleTile tile : parent)
                    tile.onTileExplodes(parent, explosion);
                parent.clear();
                
                for (StructureParentCollection list : x.structures())
                    try {
                        list.getStructure().onLittleTileDestroy();
                    } catch (CorruptedConnectionException | NotYetConnectedException e) {
                        e.printStackTrace();
                    }
            });
        }
        super.onBlockExploded(state, level, pos, explosion);
    }
    
    @Override
    public BlockState getFacade(LevelAccessor level, BlockPos pos, Direction side) {
        return defaultBlockState();
    }
    
    @Override
    public BlockState getFacade(LevelAccessor level, BlockPos pos, Direction side, BlockPos connection) {
        BETiles te = loadBE(level, pos);
        if (te != null) {
            BlockState lookingFor = CTMManager.isInstalled() ? CTMManager.getCorrectStateOrigin(level, connection) : level.getBlockState(connection);
            for (Pair<IParentCollection, LittleTile> pair : te.allTiles())
                if (pair.value.getState() == lookingFor)
                    return lookingFor;
        }
        return this.defaultBlockState();
    }
    
    @Override
    public double bound(CreativeLevel level, BlockPos pos, Facing facing) {
        BETiles te = loadBE(level, pos);
        if (te != null) {
            int value = facing.positive ? Integer.MIN_VALUE : Integer.MAX_VALUE;
            for (Pair<IParentCollection, LittleTile> pair : te.allTiles()) {
                for (LittleBox box : pair.value)
                    value = facing.positive ? Math.max(value, box.get(facing)) : Math.min(value, box.get(facing));
                
                if (facing.positive ? te.getGrid().count == value : value == 0)
                    break;
            }
            return pos.get(facing.axis.toVanilla()) + te.getGrid().toVanillaGrid(value);
        }
        return (facing.positive ? 0 : 1) + pos.get(facing.axis.toVanilla()); // most inward value possible
    }
    
}