package team.creative.littletiles.common.item;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.google.common.base.Charsets;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.model.ICreativeRendered;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.render.cache.ItemModelCache;
import team.creative.littletiles.common.api.tool.ILittlePlacer;
import team.creative.littletiles.common.gui.configure.SubGuiConfigure;
import team.creative.littletiles.common.gui.configure.SubGuiModeSelector;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.structure.type.premade.LittleStructurePremade;

public class ItemMultiTiles extends Item implements ICreativeRendered, ILittlePlacer {
    
    public static PlacementMode currentMode = PlacementMode.getDefault();
    public static LittleGridContext currentContext;
    
    public ItemMultiTiles() {
        hasSubtypes = true;
        setCreativeTab(LittleTiles.littleTab);
    }
    
    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("structure") && stack.getTagCompound().getCompoundTag("structure").hasKey("name"))
            return stack.getTagCompound().getCompoundTag("structure").getString("name");
        return super.getItemStackDisplayName(stack);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (stack.hasTagCompound()) {
            String id = "none";
            if (stack.getTagCompound().hasKey("structure"))
                id = stack.getTagCompound().getCompoundTag("structure").getString("id");
            tooltip.add("structure: " + id);
            tooltip.add("contains " + stack.getTagCompound().getInteger("count") + " tiles");
        }
    }
    
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        /* ItemStack stack = player.getHeldItem(hand); if(stack.hasTagCompound()) return
         * Item.getItemFromBlock(LittleTiles.blockTile).onItemUse(player, world, pos,
         * hand, facing, hitX, hitY, hitZ); */
        return EnumActionResult.PASS;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
        if (isInCreativeTab(tab))
            for (ExampleStructures example : ExampleStructures.values())
                if (example.stack != null)
                    list.add(example.stack);
    }
    
    @Override
    public void saveLittlePreview(ItemStack stack, LittlePreviews previews) {
        LittlePreview.savePreview(previews, stack);
    }
    
    @Override
    public boolean hasLittlePreview(ItemStack stack) {
        return true;
    }
    
    @Override
    public LittlePreviews getLittlePreview(ItemStack stack) {
        return LittlePreview.getPreview(stack);
    }
    
    @Override
    public LittlePreviews getLittlePreview(ItemStack stack, boolean allowLowResolution) {
        return LittlePreview.getPreview(stack, allowLowResolution);
    }
    
    @Override
    public List<RenderBox> getRenderingCubes(IBlockState state, TileEntity te, ItemStack stack) {
        return LittlePreview.getCubesForStackRendering(stack);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void applyCustomOpenGLHackery(ItemStack stack, TransformType cameraTransformType) {}
    
    @Override
    @SideOnly(Side.CLIENT)
    public void saveCachedModel(EnumFacing facing, BlockRenderLayer layer, List<BakedQuad> cachedQuads, IBlockState state, TileEntity te, ItemStack stack, boolean threaded) {
        if (stack != null)
            ItemModelCache.cacheModel(stack, facing, cachedQuads);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public List<BakedQuad> getCachedModel(EnumFacing facing, BlockRenderLayer layer, IBlockState state, TileEntity te, ItemStack stack, boolean threaded) {
        if (stack == null)
            return null;
        return ItemModelCache.requestCache(stack, facing);
    }
    
    @Override
    public PlacementMode getPlacementMode(ItemStack stack) {
        if (!currentMode.canPlaceStructures() && stack.hasTagCompound() && stack.getTagCompound().hasKey("structure"))
            return PlacementMode.getStructureDefault();
        return currentMode;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiConfigure getConfigureGUIAdvanced(EntityPlayer player, ItemStack stack) {
        return new SubGuiModeSelector(stack, ItemMultiTiles.currentContext, ItemMultiTiles.currentMode) {
            
            @Override
            public void saveConfiguration(LittleGridContext context, PlacementMode mode) {
                ItemMultiTiles.currentContext = context;
                ItemMultiTiles.currentMode = mode;
            }
            
        };
    }
    
    @Override
    public boolean containsIngredients(ItemStack stack) {
        return true;
    }
    
    @Override
    public LittleGridContext getPositionContext(ItemStack stack) {
        return currentContext;
    }
    
    @Override
    public LittleVec getCachedSize(ItemStack stack) {
        if (stack.getTagCompound().hasKey("size"))
            return LittlePreview.getSize(stack);
        return null;
    }
    
    @Override
    public LittleVec getCachedOffset(ItemStack stack) {
        return LittlePreview.getOffset(stack);
    }
    
    public static void reloadExampleStructures() {
        for (ExampleStructures example : ExampleStructures.values()) {
            try {
                example.stack = new ItemStack(LittleTiles.multiTiles);
                example.stack.setTagCompound(JsonToNBT
                        .getTagFromJson(IOUtils.toString(LittleStructurePremade.class.getClassLoader().getResourceAsStream(example.getFileName()), Charsets.UTF_8)));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Could not load '" + example.name() + " example structure!");
            }
        }
    }
    
    private static enum ExampleStructures {
        
        BASIC_LEVER,
        DOUBLE_DOOR,
        LIGHT_SWITCH,
        SIMPLE_LIGHT,
        STONE_PLATE,
        WOODEN_PLATE;
        
        public ItemStack stack;
        
        public String getFileName() {
            return "assets/" + LittleTiles.modid + "/example/" + name().toLowerCase() + ".struct";
        }
        
    }
    
}
