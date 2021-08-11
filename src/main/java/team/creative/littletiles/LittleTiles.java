package team.creative.littletiles;

import com.creativemd.littletiles.common.block.BlockArrow;
import com.creativemd.littletiles.common.block.BlockCable;
import com.creativemd.littletiles.common.block.BlockLTFlowingLava;
import com.creativemd.littletiles.common.block.BlockLTFlowingWater;
import com.creativemd.littletiles.common.block.BlockLittleDyeable;
import com.creativemd.littletiles.common.block.BlockLittleDyeable2;
import com.creativemd.littletiles.common.block.BlockLittleDyeableTransparent;
import com.creativemd.littletiles.common.block.BlockSignalConverter;
import com.creativemd.littletiles.common.block.BlockStorageTile;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.config.LittleTilesConfig;

public class LittleTiles {
    
    public static final String modid = "littletiles";
    public static final String version = "1.5.0";
    
    public static BlockEntityType<BETiles> TILES_TE_TYPE;
    public static LittleTilesConfig CONFIG;
    
    public static Block blockTileNoTicking;
    public static Block blockTileTicking;
    public static Block blockTileNoTickingRendered;
    public static Block blockTileTickingRendered;
    
    public static BlockLittleDyeable dyeableBlock = (BlockLittleDyeable) new BlockLittleDyeable().setRegistryName("LTColoredBlock").setUnlocalizedName("LTColoredBlock")
            .setHardness(1.5F);
    public static BlockLittleDyeable2 dyeableBlock2 = (BlockLittleDyeable2) new BlockLittleDyeable2().setRegistryName("LTColoredBlock2").setUnlocalizedName("LTColoredBlock2")
            .setHardness(1.5F);
    public static Block dyeableBlockTransparent = new BlockLittleDyeableTransparent().setRegistryName("LTTransparentColoredBlock").setUnlocalizedName("LTTransparentColoredBlock")
            .setHardness(0.3F);
    public static Block storageBlock = new BlockStorageTile().setRegistryName("LTStorageBlockTile").setUnlocalizedName("LTStorageBlockTile").setHardness(1.5F);
    
    public static Block flowingWater = new BlockLTFlowingWater(BlockLittleDyeableTransparent.LittleDyeableTransparent.WATER).setRegistryName("LTFlowingWater")
            .setUnlocalizedName("LTFlowingWater").setHardness(0.3F);
    public static Block whiteFlowingWater = new BlockLTFlowingWater(BlockLittleDyeableTransparent.LittleDyeableTransparent.WHITE_WATER).setRegistryName("LTWhiteFlowingWater")
            .setUnlocalizedName("LTWhiteFlowingWater").setHardness(0.3F);
    
    public static Block flowingLava = new BlockLTFlowingLava(BlockLittleDyeable.LittleDyeableType.LAVA).setRegistryName("LTFlowingLava").setUnlocalizedName("LTFlowingLava")
            .setHardness(0.3F);
    public static Block whiteFlowingLava = new BlockLTFlowingLava(BlockLittleDyeable.LittleDyeableType.WHITE_LAVA).setRegistryName("LTWhiteFlowingLava")
            .setUnlocalizedName("LTWhiteFlowingLava").setHardness(0.3F);
    
    public static Block singleCable = new BlockCable().setRegistryName("ltsinglecable").setUnlocalizedName("ltsinglecable").setHardness(1.5F);
    
    public static Block inputArrow = new BlockArrow().setRegistryName("ltinput").setUnlocalizedName("ltinput").setHardness(1.5F);
    public static Block outputArrow = new BlockArrow().setRegistryName("ltoutput").setUnlocalizedName("ltoutput").setHardness(1.5F);
    
    public static Block signalConverter = new BlockSignalConverter().setRegistryName("signal_converter").setUnlocalizedName("signal_converter").setHardness(1.5F);
    
    public static Item hammer;
    public static Item recipe;
    public static Item recipeAdvanced;
    public static Item multiTiles;
    public static Item saw;
    public static Item container;
    public static Item wrench;
    public static Item screwdriver;
    public static Item chisel;
    public static Item colorTube;
    public static Item rubberMallet;
    public static Item utilityKnife;
    public static Item grabber;
    public static Item premade;
    
    public static Item blockIngredient;
    
    public static Item blackColorIngredient;
    public static Item cyanColorIngredient;
    public static Item magentaColorIngredient;
    public static Item yellowColorIngredient;
    
    public static CreativeModeTab littleTab = new CreativeModeTab("littletiles") {
        
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(hammer);
        }
    };
    
}
