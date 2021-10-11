package team.creative.littletiles.common.block.little;

import javax.annotation.Nullable;

import com.creativemd.littletiles.common.api.block.BlockLog;
import com.creativemd.littletiles.common.api.block.BlockRotatedPillar;
import com.creativemd.littletiles.common.api.block.BlockSelectorAnd;
import com.creativemd.littletiles.common.api.block.BlockSelectorClass;
import com.creativemd.littletiles.common.api.block.BlockSelectorProperty;
import com.creativemd.littletiles.common.api.block.BlockWorkbench;
import com.creativemd.littletiles.common.api.block.ContainerWorkbench;
import com.creativemd.littletiles.common.api.block.EntityLivingBase;
import com.creativemd.littletiles.common.api.block.EntityPlayer;
import com.creativemd.littletiles.common.api.block.EnumFacing;
import com.creativemd.littletiles.common.api.block.EnumHand;
import com.creativemd.littletiles.common.api.block.IBlockState;
import com.creativemd.littletiles.common.api.block.IParentTileList;
import com.creativemd.littletiles.common.api.block.ISpecialBlockHandler;
import com.creativemd.littletiles.common.api.block.InventoryPlayer;
import com.creativemd.littletiles.common.api.block.LittleGridContext;
import com.creativemd.littletiles.common.api.block.LittlePreview;
import com.creativemd.littletiles.common.api.block.SpecialBlockHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TntBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.filter.block.BlockFilters;
import team.creative.littletiles.common.entity.EntitySizedTNTPrimed;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.parent.IParentCollection;

public class LittleBlocks {
    
    static {
        LittleBlockRegistry.register(BlockFilters.block(Blocks.BARRIER), x -> new LittleMCBlock(x) {
            
            @Override
            public boolean randomTicks() {
                return true;
            }
            
            @Override
            @OnlyIn(Dist.CLIENT)
            public void randomDisplayTick(IParentCollection parent, LittleTile tile, java.util.Random rand) {
                Minecraft mc = Minecraft.getInstance();
                ItemStack itemstack = mc.player.getMainHandItem();
                if (mc.player.isCreative() && itemstack.is(Blocks.BARRIER))
                    mc.level.addParticle.spawnParticle(EnumParticleTypes.BARRIER, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, 0.0D, 0.0D, 0.0D, new int[0]);
            }
        });
        
        LittleBlockRegistry.register(BlockFilters.instance(TntBlock.class), new ISpecialBlockHandler() {
            
            @Override
            public boolean onBlockActivated(IParentTileList parent, LittleTile tile, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
                if (heldItem != null && (heldItem.getItem() == Items.FLINT_AND_STEEL || heldItem.getItem() == Items.FIRE_CHARGE)) {
                    if (!parent.getWorld().isRemote)
                        explodeTile(parent, tile, player, false);
                    parent.getTe().updateTiles(x -> x.get(parent).remove(tile));
                    
                    if (heldItem.getItem() == Items.FLINT_AND_STEEL)
                        heldItem.damageItem(1, player);
                    else if (!player.capabilities.isCreativeMode)
                        heldItem.shrink(1);
                    
                    return true;
                }
                return false;
            }
            
            @Override
            public void onTileExplodes(IParentTileList parent, LittleTile tile, Explosion explosion) {
                explodeTile(parent, tile, explosion.getExplosivePlacedBy(), true);
            }
            
            public void explodeTile(IParentTileList parent, LittleTile tile, EntityLivingBase entity, boolean randomFuse) {
                BlockPos pos = parent.getPos();
                LittleVec size = tile.getSize();
                LittleVec min = tile.getMinVec();
                LittleGridContext context = parent.getContext();
                EntitySizedTNTPrimed entitytntprimed = new EntitySizedTNTPrimed(parent.getWorld(), pos.getX() + min.getPosX(context) + size.getPosX(context) / 2, pos.getY() + min
                        .getPosY(context) + size.getPosY(context) / 2, pos.getZ() + min.getPosZ(context) + size.getPosZ(context) / 2, entity, context, size);
                if (randomFuse)
                    entitytntprimed.setFuse((short) (parent.getWorld().rand.nextInt(entitytntprimed.getFuse() / 4) + entitytntprimed.getFuse() / 8));
                parent.getWorld().spawnEntity(entitytntprimed);
                parent.getWorld()
                        .playSound((EntityPlayer) null, entitytntprimed.posX, entitytntprimed.posY, entitytntprimed.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
            
        });
        
        SpecialBlockHandler.registerSpecialHandler(Blocks.CRAFTING_TABLE, new ISpecialBlockHandler() {
            
            @Override
            public boolean onBlockActivated(IParentTileList parent, LittleTile tile, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
                if (parent.getWorld().isRemote) {
                    return true;
                } else {
                    player.displayGui(new BlockWorkbench.InterfaceCraftingTable(parent.getWorld(), parent.getPos()) {
                        
                        @Override
                        public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
                            return new ContainerWorkbench(playerInventory, parent.getWorld(), parent.getPos()) {
                                
                                @Override
                                public boolean canInteractWith(EntityPlayer playerIn) {
                                    return true;
                                }
                            };
                        }
                    });
                    player.addStat(StatList.CRAFTING_TABLE_INTERACTION);
                    return true;
                }
            }
            
        });
        
        SpecialBlockHandler
                .registerSpecialHandler(new BlockSelectorAnd(new BlockSelectorClass(BlockLog.class), new BlockSelectorProperty(BlockLog.LOG_AXIS)), new ISpecialBlockHandler() {
                    
                    public Axis logAxisToNormal(BlockLog.EnumAxis axis) {
                        switch (axis) {
                        case X:
                            return Axis.X;
                        case Y:
                            return Axis.Y;
                        case Z:
                            return Axis.Z;
                        default:
                            return null;
                        }
                    }
                    
                    @Override
                    public void rotatePreview(Rotation rotation, LittlePreview preview, LittleVec doubledCenter) {
                        IBlockState state = BlockUtils.getState(preview.getBlock(), preview.getMeta());
                        Axis axis = logAxisToNormal(state.getValue(BlockLog.LOG_AXIS));
                        if (axis != null)
                            preview.getTileData().setString("block", preview.getBlockName() + ":" + preview.getBlock()
                                    .getMetaFromState(state.withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.fromFacingAxis(RotationUtils.rotate(axis, rotation)))));
                    }
                    
                });
        
        SpecialBlockHandler
                .registerSpecialHandler(new BlockSelectorAnd(new BlockSelectorClass(BlockRotatedPillar.class), new BlockSelectorProperty(BlockRotatedPillar.AXIS)), new ISpecialBlockHandler() {
                    
                    @Override
                    public void rotatePreview(Rotation rotation, LittlePreview preview, LittleVec doubledCenter) {
                        preview.getTileData().setInteger("meta", RotationUtils
                                .rotate(BlockUtils.getState(preview.getBlock(), preview.getMeta()).getValue(BlockRotatedPillar.AXIS), rotation).ordinal());
                    }
                    
                });
    }
    
}
