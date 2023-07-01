package team.creative.littletiles.common.block.little.registry;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.filter.premade.BlockFilters;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.entity.PrimedSizedTnt;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;

public class LittleBlocks {
    
    public static void init() {
        LittleBlockRegistry.register(BlockFilters.block(Blocks.BARRIER), x -> new LittleMCBlock(x) {
            
            @Override
            @OnlyIn(Dist.CLIENT)
            public void randomDisplayTick(IParentCollection parent, LittleTile tile, RandomSource rand) {
                Minecraft mc = Minecraft.getInstance();
                ItemStack itemstack = mc.player.getMainHandItem();
                if (mc.player.isCreative() && itemstack.is(Blocks.BARRIER.asItem()))
                    mc.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK_MARKER, tile.getState()), parent.getPos().getX() + 0.5D, parent.getPos().getY() + 0.5D, parent
                            .getPos().getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
            }
        });
        
        LittleBlockRegistry.register(BlockFilters.instance(TntBlock.class), x -> new LittleMCBlock(x) {
            
            @Override
            public boolean canInteract() {
                return true;
            }
            
            @Override
            public InteractionResult use(IParentCollection parent, LittleTile tile, LittleBox box, Player player, BlockHitResult result) {
                ItemStack heldItem = player.getMainHandItem();
                if (heldItem.is(Items.FLINT_AND_STEEL) || heldItem.is(Items.FIRE_CHARGE)) {
                    if (!parent.isClient())
                        explodeBox(parent, box, player, false);
                    parent.getBE().updateTiles(x -> tile.remove(x.get(parent), box));
                    
                    if (heldItem.getItem() == Items.FLINT_AND_STEEL)
                        heldItem.hurtAndBreak(1, player, (x) -> x.broadcastBreakEvent(InteractionHand.MAIN_HAND));
                    else if (!player.isCreative())
                        heldItem.shrink(1);
                    
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.PASS;
            }
            
            @Override
            public void exploded(IParentCollection parent, LittleTile tile, Explosion explosion) {
                for (LittleBox box : tile)
                    explodeBox(parent, box, explosion.getExploder(), true);
            }
            
            public void explodeBox(IParentCollection parent, LittleBox box, Entity entity, boolean randomFuse) {
                BlockPos pos = parent.getPos();
                LittleVec size = box.getSize();
                LittleVec min = box.getMinVec();
                LittleGrid grid = parent.getGrid();
                PrimedSizedTnt entitytntprimed = new PrimedSizedTnt(parent
                        .getLevel(), pos.getX() + min.getPosX(grid) + size.getPosX(grid) / 2, pos.getY() + min.getPosY(grid) + size.getPosY(grid) / 2, pos.getZ() + min
                                .getPosZ(grid) + size.getPosZ(grid) / 2, entity instanceof LivingEntity ? (LivingEntity) entity : null, grid, size);
                if (randomFuse)
                    entitytntprimed.setFuse((short) (parent.getLevel().random.nextInt(entitytntprimed.getFuse() / 4) + entitytntprimed.getFuse() / 8));
                parent.getLevel().addFreshEntity(entitytntprimed);
                parent.getLevel()
                        .playSound((Player) null, entitytntprimed.getX(), entitytntprimed.getY(), entitytntprimed.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
                parent.getLevel().gameEvent(entity, GameEvent.PRIME_FUSE, parent.getPos());
            }
            
        });
        
        LittleBlockRegistry.register(BlockFilters.block(Blocks.CRAFTING_TABLE), x -> new LittleMCBlock(x) {
            
            @Override
            public boolean canInteract() {
                return true;
            }
            
            @Override
            public InteractionResult use(IParentCollection parent, LittleTile tile, LittleBox box, Player player, BlockHitResult result) {
                if (parent.isClient())
                    return InteractionResult.SUCCESS;
                
                player.openMenu(Blocks.CRAFTING_TABLE.getMenuProvider(getState(), parent.getLevel(), parent.getPos()));
                player.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
                return InteractionResult.CONSUME;
            }
            
        });
        
        LittleBlockRegistry.register(BlockFilters.property(BlockStateProperties.AXIS), x -> new LittleMCBlock(x) {
            @Override
            public BlockState rotate(BlockState state, Rotation rotation, LittleVec doubledCenter) {
                return state.setValue(BlockStateProperties.AXIS, rotation.rotate(Axis.get(state.getValue(BlockStateProperties.AXIS))).toVanilla());
            }
        });
        
        LittleBlockRegistry.register(BlockFilters.property(BlockStateProperties.FACING), x -> new LittleMCBlock(x) {
            @Override
            public BlockState rotate(BlockState state, Rotation rotation, LittleVec doubledCenter) {
                return state.setValue(BlockStateProperties.FACING, rotation.rotate(Facing.get(state.getValue(BlockStateProperties.FACING))).toVanilla());
            }
        });
        
        LittleBlockRegistry.register(BlockFilters.instance(LeavesBlock.class), x -> new LittleMCBlock(x) {
            
            @Override
            public boolean cullOverEdge() {
                return false;
            }
            
        });
    }
    
}
