package mod.flatcoloredblocks.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

public class BlockFlatColored extends Block {
    
    public BlockFlatColored(Material materialIn) {
        super(materialIn);
    }
    
    public int colorFromState(final IBlockState state) {
        return 0;
    }
}
