package team.creative.littletiles.common.ingredient;

import com.creativemd.littletiles.common.util.ingredient.rules.BlockIngredientRule;
import com.creativemd.littletiles.common.util.ingredient.rules.IngredientRules;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.gen.feature.structure.StructurePiece.BlockSelector;

public class IngredientUtils {
    
    public static BlockIngredientEntry getBlockIngredient(Block block, int meta, double value) {
        for (Pair<BlockSelector, BlockIngredientRule> pair : IngredientRules.getBlockRules())
            if (pair.key.is(block, meta))
                return pair.value.getBlockIngredient(block, meta, value);
        return create(block, meta, value);
    }
    
    @Deprecated
    /** Don't use it, except if it's about BlockIngredientRule */
    public static BlockIngredientEntry create(Block block, int meta, double value) {
        return new BlockIngredientEntry(block, meta, value);
    }
    
    public static BlockIngredientEntry loadBlockIngredient(CompoundNBT nbt) {
        Block block = Block.getBlockFromName(nbt.getString("block"));
        if (block == null || block instanceof AirBlock)
            return null;
        if (nbt.getDouble("volume") > 0)asdas
            return new BlockIngredientEntry(block, nbt.getInt("meta"), nbt.getDouble("volume"));
        return null;
    }
}
