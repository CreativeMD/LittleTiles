package team.creative.littletiles.common.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

import com.creativemd.littletiles.client.render.cache.LayeredRenderBoxCache;
import com.creativemd.littletiles.client.render.tile.LittleRenderBox;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.action.block.LittleActionDestroy;
import com.creativemd.littletiles.common.mod.ctm.CTMManager;
import com.creativemd.littletiles.common.structure.type.LittleBed;
import com.creativemd.littletiles.common.tile.math.box.face.LittleBoxFace;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.util.ForgeSoundType;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.chisel.ctm.api.IFacade;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.face.CachedFaceRenderType;
import team.creative.creativecore.client.render.face.FaceRenderType;
import team.creative.creativecore.client.render.model.ICreativeRendered;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.type.Pair;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.block.BlockTile.TEResult;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.entity.BETilesRendered;
import team.creative.littletiles.common.block.entity.BETilesTicking;
import team.creative.littletiles.common.block.entity.BETilesTickingRendered;
import team.creative.littletiles.common.block.entity.TileEntityLittleTilesRendered;
import team.creative.littletiles.common.block.entity.TileEntityLittleTilesTicking;
import team.creative.littletiles.common.block.entity.TileEntityLittleTilesTickingRendered;
import team.creative.littletiles.common.item.ItemBlockTiles;
import team.creative.littletiles.common.item.ItemLittlePaintBrush;
import team.creative.littletiles.common.item.ItemLittleSaw;
import team.creative.littletiles.common.item.ItemLittleWrench;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureAttribute;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.LittleTileContext;
import team.creative.littletiles.common.tile.parent.IParentCollection;
import team.creative.littletiles.common.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.tile.parent.ParentCollection;
import team.creative.littletiles.common.tile.parent.ParentTileList;
import team.creative.littletiles.common.tile.parent.StructureParentCollection;
import team.creative.littletiles.server.LittleTilesServer;

@Interface(iface = "team.chisel.ctm.api.IFacade", modid = "ctm")
public class BlockTile extends BaseEntityBlock implements ICreativeRendered, IFacade {
    
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
    
    public static int getStateId(BETiles te) {
        return getStateId(te.isTicking(), te.isRendered());
    }
    
    public static int getStateId(boolean ticking, boolean rendered) {
        return rendered ? (ticking ? 3 : 2) : (ticking ? 1 : 0);
    }
    
    public static BlockState getState(int id) {
        switch (id) {
        case 0:
            return LittleTiles.blockTile.defaultBlockState();
        case 1:
            return LittleTiles.blockTileTicking.defaultBlockState();
        case 2:
            return LittleTiles.blockTileRendered.defaultBlockState();
        case 3:
            return LittleTiles.blockTileTickingRendered.defaultBlockState();
        }
        return null;
    }
    
    public static BlockState getStateByAttribute(int attribute) {
        return getState(LittleStructureAttribute.ticking(attribute), LittleStructureAttribute.tickRendering(attribute));
    }
    
    public static BlockState getState(boolean ticking, boolean rendered) {
        return rendered ? (ticking ? LittleTiles.blockTileTickingRendered.defaultBlockState() : LittleTiles.blockTileRendered
                .defaultBlockState()) : (ticking ? LittleTiles.blockTileTicking.defaultBlockState() : LittleTiles.blockTile.defaultBlockState());
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
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return true;
    }
    
    /** Used to determine ambient occlusion and culling when rebuilding chunks for
     * render */
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }
    
    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
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
    public boolean skipRendering(BlockState state, BlockState state2, Direction direction) {
        // TODO might be interesting for rendering, will use true for now
        return true;
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
                boolean canInteract = parent.isStructure() && parent.getStructure().canInteract(level, pos);
                for (LittleTile tile : parent)
                    if (canInteract || tile.canInteract())
                        shape = Shapes.or(shape, tile.getShapes(parent));
            }
        }
        
        return shape;
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // get Selection shape and it's also used for some other stuff I don't know (only works on client side) TODO CHECK if that is the case
        LittleTileContext tileContext = LittleTileContext.selectFocused(level, pos, mc.player);
        if (tileContext.isComplete()) {
            if (selectEntireBlock(mc.player, LittleAction.isUsingSecondMode(mc.player)))
                return tileContext.parent.getBE().getBlockShape();
            if (LittleTiles.CONFIG.rendering.highlightStructureBox && tileContext.parent.isStructure())
                try {
                    return tileContext.parent.getStructure().getSurroundingBox().getShape();
                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
            return tileContext.tile.getShapes(tileContext.parent);
        }
        return shape;
    }
    
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState removed, boolean p_50941_) {
        super.onRemove(state, level, pos, removed, p_50941_);
        adads
    }
    
    @Override
    public boolean isPathfindable(BlockState p_50921_, BlockGetter p_50922_, BlockPos p_50923_, PathComputationType p_50924_) {
        asds
        return false;
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
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasCustomBreakingProgress(IBlockState state) {
        return false;
    }
    
    @Override
    public MaterialColor getMapColor(BlockGetter level, BlockPos pos) {
        // TODO Needs custom state? Not sure if this is even possible
        TileEntityLittleTiles te = loadTe(worldIn, pos);
        if (te != null) {
            double biggest = 0;
            LittleTile tile = null;
            for (Pair<IParentTileList, LittleTile> pair : te.allTiles()) {
                double tempVolume = pair.value.getVolume();
                if (tempVolume > biggest) {
                    biggest = tempVolume;
                    tile = pair.value;
                }
            }
            
            if (tile != null)
                return tile.getBlockState().getMapColor(worldIn, pos);
        }
        return this.blockMapColor;
    }
    
    @Override
    public Optional<Vec3> getRespawnPosition(BlockState state, EntityType<?> type, LevelReader level, BlockPos pos, float orientation, @Nullable LivingEntity entity) {
        // TODO new code vs old code
        if (isBed(state, level, pos, entity) && level instanceof Level && BedBlock.canSetSpawn(level)) {
            return BedBlock.findStandUpPosition(type, level, pos, orientation);
        }
        return Optional.empty();
        int tries = 0;
        EnumFacing enumfacing = EnumFacing.EAST; // (EnumFacing)worldIn.getBlockState(pos).getValue(FACING);
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        
        for (int l = 0; l <= 1; ++l) {
            int i1 = i - enumfacing.getFrontOffsetX() * l - 1;
            int j1 = k - enumfacing.getFrontOffsetZ() * l - 1;
            int k1 = i1 + 2;
            int l1 = j1 + 2;
            
            for (int i2 = i1; i2 <= k1; ++i2) {
                for (int j2 = j1; j2 <= l1; ++j2) {
                    BlockPos blockpos = new BlockPos(i2, j, j2);
                    
                    if (hasRoomForPlayer(world, blockpos)) {
                        if (tries <= 0) {
                            return blockpos;
                        }
                        
                        --tries;
                    }
                }
            }
        }
        
        return null;
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
    
    public static boolean canHarvestBlock(EntityPlayer player, IBlockState state) {
        if (state.getMaterial().isToolNotRequired()) {
            return true;
        }
        
        ItemStack stack = player.getHeldItemMainhand();
        String tool = state.getBlock().getHarvestTool(state);
        if (stack.isEmpty() || tool == null) {
            return player.canHarvestBlock(state);
        }
        
        int toolLevel = stack.getItem().getHarvestLevel(stack, tool, player, state);
        if (toolLevel < 0) {
            return player.canHarvestBlock(state);
        }
        
        return toolLevel >= state.getBlock().getHarvestLevel(state);
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
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return true;
    }
    
    @Override
    public void destroy(LevelAccessor p_49860_, BlockPos p_49861_, BlockState p_49862_) {
        adasd
    }
    
    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntityLittleTiles te = loadTe(world, pos);
        if (te != null && te.isEmpty())
            super.breakBlock(world, pos, state);
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
            return new LittleActionActivated(worldIn, pos, playerIn).execute();
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
                    slipperiness = Math.min(slipperiness, pair.value.getFriction(level, pos, entity));
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
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        if (isSideSolid(state, worldIn, pos, face))
            return BlockFaceShape.SOLID;
        return BlockFaceShape.UNDEFINED;
    }
    
    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        TileEntityLittleTiles te = loadTe(world, pos);
        if (te != null)
            return te.sideCache.get(face).doesBlockLight();
        return false;
    }
    
    @Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileEntityLittleTiles te = loadTe(world, pos);
        if (te != null)
            return te.sideCache.get(side).isFilled();
        return false;
    }
    
    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, net.minecraft.entity.EntityLiving.SpawnPlacementType type) {
        return false;
    }
    
    @Override
    public boolean removedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (level.isClientSide)
            return removedByPlayerClient(state, level, pos, player, willHarvest);
        return true;
    }
    
    @OnlyIn(Dist.CLIENT)
    public boolean removedByPlayerClient(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        LittleTileContext result = LittleTileContext.selectFocused(level, pos, player, 1.0F);
        if (result.isComplete())
            return new LittleActionDestroy(level, pos, player).execute();
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
    public IBlockState getStateAtViewpoint(IBlockState state, IBlockAccess world, BlockPos pos, Vec3d viewpoint) {
        TileEntityLittleTiles te = loadTe(world, pos);
        if (te != null) {
            int x = te.getContext().toGrid(viewpoint.x);
            int y = te.getContext().toGrid(viewpoint.y);
            int z = te.getContext().toGrid(viewpoint.z);
            LittleBox box = new LittleBox(x, y, z, x + 1, y + 1, z + 1);
            for (Pair<IParentTileList, LittleTile> pair : te.allTiles())
                if (pair.value.intersectsWith(box))
                    return pair.value.getBlockState();
        }
        return state;
    }
    
    @Override
    public ArrayList<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        return new ArrayList<ItemStack>(); // Removed
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        TEResult result = loadTeAndTile(world, pos, mc.player);
        if (result.isComplete()) {
            if (selectEntireBlock(mc.player, LittleAction.isUsingSecondMode(player))) {
                ItemStack drop = new ItemStack(LittleTiles.multiTiles);
                LittlePreview.saveTiles(world, result.te.getContext(), result.te, drop);
                return drop;
            }
            if (result.parent.isStructure())
                try {
                    return result.parent.getStructure().getStructureDrop();
                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
            return result.tile.getDrop(result.te.getContext());
        }
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean addLandingEffects(IBlockState state, net.minecraft.world.WorldServer world, BlockPos pos, IBlockState iblockstate, EntityLivingBase entity, int numberOfParticles) {
        TileEntityLittleTiles te = loadTe(world, pos);
        if (te != null) {
            int heighest = 0;
            LittleTile heighestTile = null;
            for (IParentTileList list : te.groups()) {
                if (list.isStructure() && LittleStructureAttribute.noCollision(list.getAttribute()))
                    continue;
                
                for (LittleTile tile : list) {
                    LittleBox box = tile.getCollisionBox();
                    if (box != null && box.maxY > heighest) {
                        heighest = box.maxY;
                        heighestTile = tile;
                    }
                }
            }
            
            if (heighestTile != null)
                world.spawnParticle(EnumParticleTypes.BLOCK_DUST, entity.posX, entity.posY, entity.posZ, numberOfParticles, 0.0D, 0.0D, 0.0D, 0.15000000596046448D, new int[] { Block
                        .getStateId(heighestTile.getBlockState()) });
        }
        return true;
    }
    
    @Override
    public boolean addRunningEffects(IBlockState state, World world, BlockPos pos, Entity entity) {
        TileEntityLittleTiles te = loadTe(world, pos);
        if (te != null) {
            int heighest = 0;
            LittleTile heighestTile = null;
            for (IParentTileList list : te.groups()) {
                if (list.isStructure() && LittleStructureAttribute.noCollision(list.getAttribute()))
                    continue;
                
                for (LittleTile tile : list) {
                    LittleBox box = tile.getCollisionBox();
                    if (box != null && box.maxY > heighest) {
                        heighest = box.maxY;
                        heighestTile = tile;
                    }
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
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(IBlockState oldstate, World worldObj, RayTraceResult target, net.minecraft.client.particle.ParticleManager manager) {
        TEResult result = loadTeAndTile(worldObj, target.getBlockPos(), mc.player);
        if (result.isComplete()) {
            IBlockState state = result.tile.getBlockState();
            BlockPos pos = target.getBlockPos();
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            float f = 0.1F;
            AxisAlignedBB axisalignedbb = result.tile.getSelectedBox(BlockPos.ORIGIN, result.te.getContext());
            double d0 = i + worldObj.rand.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minX;
            double d1 = j + worldObj.rand.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minY;
            double d2 = k + worldObj.rand.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minZ;
            EnumFacing side = target.sideHit;
            if (side == EnumFacing.DOWN)
                d1 = j + axisalignedbb.minY - 0.10000000149011612D;
            
            if (side == EnumFacing.UP)
                d1 = j + axisalignedbb.maxY + 0.10000000149011612D;
            
            if (side == EnumFacing.NORTH)
                d2 = k + axisalignedbb.minZ - 0.10000000149011612D;
            
            if (side == EnumFacing.SOUTH)
                d2 = k + axisalignedbb.maxZ + 0.10000000149011612D;
            
            if (side == EnumFacing.WEST)
                d0 = i + axisalignedbb.minX - 0.10000000149011612D;
            
            if (side == EnumFacing.EAST)
                d0 = i + axisalignedbb.maxX + 0.10000000149011612D;
            
            ((ParticleDigging) manager.spawnEffectParticle(EnumParticleTypes.BLOCK_CRACK.getParticleID(), d0, d1, d2, 0.0D, 0.0D, 0.0D, Block.getStateId(state))).setBlockPos(pos)
                    .multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F);
        }
        return true;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, BlockPos pos, net.minecraft.client.particle.ParticleManager manager) {
        TEResult result = loadTeAndTile(world, pos, mc.player);
        if (result.isComplete()) {
            IBlockState state = result.tile.getBlockState();
            int i = 4;
            
            for (int j = 0; j < 1; ++j) {
                for (int k = 0; k < 1; ++k) {
                    for (int l = 0; l < 1; ++l) {
                        double d0 = pos.getX() + (j + 0.5D) / 4.0D;
                        double d1 = pos.getY() + (k + 0.5D) / 4.0D;
                        double d2 = pos.getZ() + (l + 0.5D) / 4.0D;
                        manager.spawnEffectParticle(EnumParticleTypes.BLOCK_CRACK
                                .getParticleID(), d0, d1, d2, d0 - pos.getX() - 0.5D, d1 - pos.getY() - 0.5D, d2 - pos.getZ() - 0.5D, Block.getStateId(state));
                    }
                }
            }
        }
        return true;
    }
    
    @SideOnly(Side.CLIENT)
    public SoundType getSoundTypeClient(IBlockState state, World world, BlockPos pos) {
        TEResult result = loadTeAndTile(world, pos, mc.player);
        if (result != null && result.tile != null)
            return result.tile.getSound();
        return null;
    }
    
    @Override
    public SoundType getSoundType(IBlockState state, World world, BlockPos pos, @Nullable Entity entity) {
        if (entity == null)
            return SILENT;
        SoundType sound = null;
        if (entity instanceof EntityPlayer && world.isRemote)
            sound = getSoundTypeClient(state, world, pos);
        
        if (sound == null) {
            // GET HEIGHEST TILE POSSIBLE
            TileEntityLittleTiles te = loadTe(world, pos);
            if (te != null) {
                int heighest = 0;
                LittleTile heighestTile = null;
                for (IParentTileList list : te.groups()) {
                    if (list.isStructure() && LittleStructureAttribute.noCollision(list.getAttribute()))
                        continue;
                    
                    for (LittleTile tile : list) {
                        LittleBox box = tile.getCollisionBox();
                        if (box != null && box.maxY > heighest) {
                            heighest = box.maxY;
                            heighestTile = tile;
                        }
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
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false; // removed because if is normal cube player will be pushed out of the block (bad for no-clip structure or water)
    }
    
    @Override
    public float getEnchantPowerBonus(World world, BlockPos pos) {
        float bonus = 0F;
        TileEntityLittleTiles te = loadTe(world, pos);
        if (te != null)
            for (Pair<IParentTileList, LittleTile> pair : te.allTiles())
                bonus += pair.value.getEnchantPowerBonus(world, pos) * pair.value.getPercentVolume(te.getContext());
        return bonus;
    }
    
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        TileEntityLittleTiles te = loadTe(worldIn, pos);
        if (te != null) {
            te.onNeighbourChanged();
            if (!worldIn.isRemote)
                LittleTilesServer.NEIGHBOR.add(worldIn, pos);
        }
    }
    
    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
        if (loadingTileEntityFromWorld)
            return;
        TileEntityLittleTiles te = loadTe(world, pos);
        if (te != null)
            te.onNeighbourChanged();
    }
    
    @Override
    @Nullable
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
        TileEntityLittleTiles te = loadTe(worldIn, pos);
        if (te != null) {
            RayTraceResult moving = te.rayTrace(start, end);
            if (moving != null)
                return new RayTraceResult(moving.hitVec, moving.sideHit, pos);
        }
        return null;
    }
    
    @Override
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        TileEntityLittleTiles te = loadTe(worldIn, pos);
        if (te != null && te.shouldCheckForCollision()) {
            for (IStructureTileList list : te.structures())
                if (LittleStructureAttribute.collisionListener(list.getAttribute()))
                    try {
                        list.getStructure().onEntityCollidedWithBlock(worldIn, list, pos, entityIn);
                    } catch (CorruptedConnectionException | NotYetConnectedException e) {}
            for (Pair<IParentTileList, LittleTile> pair : te.allTiles()) {
                if (pair.value.shouldCheckForCollision())
                    pair.value.onEntityCollidedWithBlock(pair.key, entityIn);
            }
        }
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (rendered)
            if (ticking)
                return new BETilesTickingRendered(pos, state);
            else
                return new BETilesRendered(pos, state);
            
        if (ticking)
            return new BETilesTicking(pos, state);
        return new BETiles(LittleTiles.BE_TILES_TYPE, pos, state);
    }
    
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_152694_, BlockState p_152695_, BlockEntityType<T> p_152696_) {
        adasds
        return p_152694_.isClientSide ? null : createTickerHelper(p_152696_, BlockEntityType.BREWING_STAND, BrewingStandBlockEntity::serverTick);
    }
    
    @SideOnly(Side.CLIENT)
    private static TileEntityLittleTiles checkforTileEntity(World world, EnumFacing facing, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos.offset(facing));
        if (tileEntity instanceof TileEntityLittleTiles)
            return (TileEntityLittleTiles) tileEntity;
        return null;
    }
    
    @SideOnly(Side.CLIENT)
    private static boolean checkforNeighbor(World world, EnumFacing facing, BlockPos pos) {
        BlockPos newPos = pos.offset(facing);
        IBlockState state = world.getBlockState(newPos);
        return !state.doesSideBlockRendering(world, newPos, facing.getOpposite());
    }
    
    @SideOnly(Side.CLIENT)
    private static void updateRenderer(TileEntityLittleTiles tileEntity, EnumFacing facing, HashMap<EnumFacing, Boolean> neighbors, HashMap<EnumFacing, TileEntityLittleTiles> neighborsTiles, RenderBox cube, LittleBoxFace face) {
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
    
    @Override
    @SideOnly(Side.CLIENT)
    public List<? extends RenderBox> getRenderingCubes(IBlockState state, TileEntity te, ItemStack stack) {
        if (te instanceof TileEntityLittleTiles)
            return Collections.emptyList();
        return getRenderingCubes(state, te, stack, MinecraftForgeClient.getRenderLayer());
    }
    
    @SideOnly(Side.CLIENT)
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
            
            for (Pair<IParentTileList, LittleTile> pair : tileEntity.allTiles()) {
                LittleTile tile = pair.value;
                if (tile.shouldBeRenderedInLayer(layer)) {
                    // Check for sides which does not need to be rendered
                    LittleRenderBox cube = pair.key.getTileRenderingCube(tile, ((TileEntityLittleTiles) te).getContext(), layer);
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
                    structure.load();
                    structure.getRenderingCubes(tileEntity.getPos(), layer, cubes);
                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                
            }
            
            cache.set(cubes, layer);
            
        } else if (stack != null)
            return ItemBlockTiles.getItemRenderingCubes(stack);
        return cubes;
    }
    
    @Override
    public boolean canDropFromExplosion(Explosion explosionIn) {
        return false;
    }
    
    @Override
    public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        TileEntityLittleTiles te = loadTe(world, pos);
        float calculatedResistance = 0;
        float structureResistance = 0;
        if (te != null)
            for (IParentTileList list : te.groups())
                try {
                    if (list.isStructure() && list.getStructure().getExplosionResistance() > 0)
                        structureResistance = Math.max(structureResistance, list.getStructure().getExplosionResistance());
                    else
                        for (LittleTile tile : list)
                            calculatedResistance += tile.getExplosionResistance() * tile.getPercentVolume(te.getContext());
                } catch (CorruptedConnectionException | NotYetConnectedException e) {
                    
                }
            
        if (calculatedResistance > structureResistance)
            return calculatedResistance;
        return structureResistance;
    }
    
    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
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
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    
    public Vec3d getFogColor(World world, BlockPos pos, IBlockState state, Entity entity, Vec3d originalColor, float partialTicks) {
        TileEntityLittleTiles te = loadTe(world, pos);
        if (te != null)
            for (Pair<IParentTileList, LittleTile> pair : te.allTiles())
                if (pair.value.getBox().getBox(te.getContext(), pos).intersects(entity.getEntityBoundingBox()))
                    return pair.value.getFogColor(pair.key, entity, originalColor, partialTicks);
                
        return super.getFogColor(world, pos, state, entity, originalColor, partialTicks);
    }
    
    @Override
    public Vec3d modifyAcceleration(World world, BlockPos pos, Entity entityIn, Vec3d motion) {
        AxisAlignedBB boundingBox = entityIn.getEntityBoundingBox();
        TileEntityLittleTiles te = loadTe(world, pos);
        if (te != null) {
            Vec3d vec = Vec3d.ZERO;
            for (Pair<IParentTileList, LittleTile> pair : te.allTiles()) {
                if (pair.value.getBox().getBox(te.getContext(), pos).intersects(boundingBox)) {
                    Vec3d tileMotion = pair.value.modifyAcceleration(pair.key, entityIn, motion);
                    if (tileMotion != null)
                        vec = vec.add(tileMotion);
                }
            }
            return motion.add(vec);
        }
        return motion;
    }
    
    @Override
    @Nullable
    public Boolean isEntityInsideMaterial(IBlockAccess world, BlockPos blockpos, IBlockState iblockstate, Entity entity, double yToTest, Material materialIn, boolean testingHead) {
        return isAABBInsideMaterial(entity.world, blockpos, entity.getEntityBoundingBox(), materialIn);
    }
    
    @Override
    @Nullable
    public Boolean isAABBInsideMaterial(World world, BlockPos pos, AxisAlignedBB boundingBox, Material materialIn) {
        TileEntityLittleTiles te = loadTe(world, pos);
        if (te != null)
            for (Pair<IParentTileList, LittleTile> pair : te.allTiles())
                if (pair.value.isMaterial(materialIn) && pair.value.getBox().getBox(te.getContext(), pos).intersects(boundingBox))
                    return true;
        return false;
    }
    
    @Override
    public Boolean isAABBInsideLiquid(World world, BlockPos pos, AxisAlignedBB boundingBox) {
        TileEntityLittleTiles te = loadTe(world, pos);
        if (te != null)
            for (Pair<IParentTileList, LittleTile> pair : te.allTiles())
                if (pair.value.isLiquid() && pair.value.getBox().getBox(te.getContext(), pos).intersects(boundingBox))
                    return true;
        return false;
    }
    
    @Override
    public float getBlockLiquidHeight(World world, BlockPos pos, IBlockState state, Material material) {
        float height = 0;
        TileEntityLittleTiles te = loadTe(world, pos);
        if (te != null)
            for (Pair<IParentTileList, LittleTile> pair : te.allTiles())
                if (pair.value.isMaterial(material))
                    height = Math.max(height, (float) te.getContext().toVanillaGrid(pair.value.getMaxY()));
        return height;
    }
    
    @Override
    @Method(modid = CTMManager.ctmID)
    public IBlockState getFacade(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return world.getBlockState(pos);
    }
    
    @Override
    @Method(modid = CTMManager.ctmID)
    public IBlockState getFacade(IBlockAccess world, BlockPos pos, EnumFacing side, BlockPos connection) {
        TileEntityLittleTiles te = loadTe(world, pos);
        if (te != null) {
            IBlockState lookingFor = CTMManager.isInstalled() ? CTMManager.getCorrectStateOrigin(world, connection) : world.getBlockState(connection);
            for (Pair<IParentTileList, LittleTile> pair : te.allTiles())
                if (pair.value.getBlock() == lookingFor.getBlock() && pair.value.getMeta() == lookingFor.getBlock().getMetaFromState(lookingFor))
                    return lookingFor;
        }
        return this.getDefaultState();
    }
    
}
