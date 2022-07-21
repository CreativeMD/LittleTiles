package team.creative.littletiles.client.mod.optifine;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.block.mc.BlockTile;

public class ConnectedTexturesModifier {
    
    private static Class connectedProperties;
    private static Method match;
    private static Method matchMeta;
    private static Method getBlockID;
    
    static {
        try {
            connectedProperties = Class.forName("net.optifine.ConnectedProperties");
            match = ObfuscationReflectionHelper.findMethod(connectedProperties, "matchesBlockId", int.class);
            matchMeta = ObfuscationReflectionHelper.findMethod(connectedProperties, "matchesBlock", int.class, int.class);
            getBlockID = ObfuscationReflectionHelper.findMethod(BlockStateBase.class, "getBlockId");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        
    }
    
    public static boolean matches(Object properties, LevelAccessor level, BlockPos pos, BlockState state) {
        try {
            BETiles be = BlockTile.loadBE(level, pos);
            if (be != null) {
                for (Pair<IParentCollection, LittleTile> pair : be.allTiles())
                    if ((Boolean) match.invoke(properties, Block.getId(pair.value.getState())))
                        return true;
                return false;
            }
            return (boolean) match.invoke(properties, (Integer) getBlockID.invoke(state));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public static boolean matches(Object properties, LevelAccessor level, BlockPos pos, int metadata) {
        try {
            BETiles be = BlockTile.loadBE(level, pos);
            if (be != null) {
                for (Pair<IParentCollection, LittleTile> pair : be.allTiles())
                    if ((Boolean) matchMeta.invoke(properties, Block.getId(pair.value.getState()), metadata))
                        return true;
                return false;
            }
            return (boolean) matchMeta.invoke(properties, Block.getId(level.getBlockState(pos)), metadata);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public static boolean isNeighbour(LevelAccessor level, BlockState state, BlockPos pos) {
        BETiles be = BlockTile.loadBE(level, pos);
        if (be != null)
            for (Pair<IParentCollection, LittleTile> pair : be.allTiles())
                if (state == pair.getValue().getState())
                    return true;
        return false;
    }
    
    public static boolean isFullCube(BlockState state) {
        return state.getBlock() instanceof BlockTile;
    }
}
