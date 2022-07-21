package team.creative.littletiles.common.structure.type;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeConfig;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.registry.LittleStructureGuiParser;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;

public class LittleLadder extends LittleStructure {
    
    public LittleLadder(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {}
    
    @Override
    protected void saveExtra(CompoundTag nbt) {}
    
    public static boolean isLivingOnLadder(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull LivingEntity entity) {
        boolean isSpectator = (entity instanceof Player && ((Player) entity).isSpectator());
        if (isSpectator)
            return false;
        if (!ForgeConfig.SERVER.fullBoundingBoxLadders.get()) {
            return state.getBlock().isLadder(state, level, pos, entity);
        } else {
            MutableBlockPos tmp = new MutableBlockPos();
            AABB bb = entity.getBoundingBox();
            int mX = Mth.floor(bb.minX);
            int mY = Mth.floor(bb.minY);
            int mZ = Mth.floor(bb.minZ);
            for (int y2 = mY; y2 < bb.maxY; y2++) {
                for (int x2 = mX; x2 < bb.maxX; x2++) {
                    for (int z2 = mZ; z2 < bb.maxZ; z2++) {
                        tmp.set(x2, y2, z2);
                        state = level.getBlockState(tmp);
                        if (state.getBlock().isLadder(state, level, tmp, entity)) {
                            return true;
                        }
                    }
                }
            }
            bb = entity.getBoundingBox().inflate(0.0001);
            mX = Mth.floor(bb.minX);
            mY = Mth.floor(bb.minY);
            mZ = Mth.floor(bb.minZ);
            for (int y2 = mY; y2 < bb.maxY; y2++) {
                for (int x2 = mX; x2 < bb.maxX; x2++) {
                    for (int z2 = mZ; z2 < bb.maxZ; z2++) {
                        tmp.set(x2, y2, z2);
                        state = level.getBlockState(tmp);
                        if (state.getBlock() instanceof BlockTile && state.getBlock().isLadder(state, level, tmp, entity)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }
    
    public static class LittleLadderParser extends LittleStructureGuiParser {
        
        public LittleLadderParser(GuiParent parent, AnimationGuiHandler handler) {
            super(parent, handler);
        }
        
        @Override
        public void createControls(LittleGroup previews, LittleStructure structure) {}
        
        @Override
        public LittleLadder parseStructure(LittleGroup previews) {
            return createStructure(LittleLadder.class, null);
        }
        
        @Override
        protected LittleStructureType getStructureType() {
            return LittleStructureRegistry.getStructureType(LittleLadder.class);
        }
    }
}
