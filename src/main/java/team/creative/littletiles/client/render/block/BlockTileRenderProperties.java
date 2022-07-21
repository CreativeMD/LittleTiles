package team.creative.littletiles.client.render.block;

import java.util.Random;

import com.mojang.math.Vector3d;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.IBlockRenderProperties;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.math.box.LittleBox;

public class BlockTileRenderProperties implements IBlockRenderProperties {
    
    private final Random random = new Random();
    
    public static final BlockTileRenderProperties INSTANCE = new BlockTileRenderProperties();
    
    @Override
    public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        LittleTileContext context = LittleTileContext.selectFocused(level, pos, Minecraft.getInstance().player);
        if (context.isComplete()) {
            BlockState particleState = context.tile.getState();
            context.box.getShape(context.parent.getGrid()).forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                double d1 = Math.min(1.0D, maxX - minX);
                double d2 = Math.min(1.0D, maxY - minY);
                double d3 = Math.min(1.0D, maxZ - minZ);
                int i = Math.max(2, Mth.ceil(d1 / 0.25D));
                int j = Math.max(2, Mth.ceil(d2 / 0.25D));
                int k = Math.max(2, Mth.ceil(d3 / 0.25D));
                
                for (int l = 0; l < i; ++l) {
                    for (int i1 = 0; i1 < j; ++i1) {
                        for (int j1 = 0; j1 < k; ++j1) {
                            double d4 = (l + 0.5D) / i;
                            double d5 = (i1 + 0.5D) / j;
                            double d6 = (j1 + 0.5D) / k;
                            double d7 = d4 * d1 + minX;
                            double d8 = d5 * d2 + minY;
                            double d9 = d6 * d3 + minZ;
                            manager.add(new TerrainParticle((ClientLevel) level, pos.getX() + d7, pos.getY() + d8, pos
                                    .getZ() + d9, d4 - 0.5D, d5 - 0.5D, d6 - 0.5D, particleState, pos).updateSprite(particleState, pos));
                        }
                    }
                }
                
            });
        }
        return true;
    }
    
    @Override
    public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
        BlockPos pos = ((BlockHitResult) target).getBlockPos();
        Direction direction = ((BlockHitResult) target).getDirection();
        LittleTileContext context = LittleTileContext.selectFocused(level, pos, Minecraft.getInstance().player);
        if (context.isComplete()) {
            BlockState blockstate = context.tile.getState();
            if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
                int i = pos.getX();
                int j = pos.getY();
                int k = pos.getZ();
                
                AABB aabb = context.box.getBB(context.parent.getGrid(), pos);
                double d0 = i + random.nextDouble() * (aabb.maxX - aabb.minX - 0.2F) + 0.1F + aabb.minX;
                double d1 = j + random.nextDouble() * (aabb.maxY - aabb.minY - 0.2F) + 0.1F + aabb.minY;
                double d2 = k + random.nextDouble() * (aabb.maxZ - aabb.minZ - 0.2F) + 0.1F + aabb.minZ;
                if (direction == Direction.DOWN)
                    d1 = j + aabb.minY - 0.1F;
                
                if (direction == Direction.UP)
                    d1 = j + aabb.maxY + 0.1F;
                
                if (direction == Direction.NORTH)
                    d2 = k + aabb.minZ - 0.1F;
                
                if (direction == Direction.SOUTH)
                    d2 = k + aabb.maxZ + 0.1F;
                
                if (direction == Direction.WEST)
                    d0 = i + aabb.minX - 0.1F;
                
                if (direction == Direction.EAST)
                    d0 = i + aabb.maxX + 0.1F;
                
                manager.add((new TerrainParticle((ClientLevel) level, d0, d1, d2, 0.0D, 0.0D, 0.0D, blockstate, pos).updateSprite(blockstate, pos)).setPower(0.2F).scale(0.6F));
            }
        }
        return true;
    }
    
    @Override
    public Vector3d getFogColor(BlockState state, LevelReader level, BlockPos pos, Entity entity, Vector3d originalColor, float partialTicks) {
        BETiles be = BlockTile.loadBE(level, pos);
        if (be != null)
            for (Pair<IParentCollection, LittleTile> pair : be.allTiles())
                for (LittleBox box : pair.getValue())
                    if (box.getBB(pair.key.getGrid(), pos).intersects(entity.getBoundingBox()))
                        return pair.value.getFogColor(pair.key, entity, originalColor, partialTicks);
                    
        return originalColor;
    }
    
}
