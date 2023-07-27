package com.creativemd.littletiles.common.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.creativemd.creativecore.client.rendering.RenderBox;
import com.creativemd.creativecore.client.rendering.model.ICreativeRendered;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.mc.NBTUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.client.gui.configure.SubGuiModeSelector;
import com.creativemd.littletiles.client.render.cache.ItemModelCache;
import com.creativemd.littletiles.common.api.ILittlePlacer;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.type.premade.LittleStructurePremade;
import com.creativemd.littletiles.common.structure.type.premade.LittleStructurePremade.LittleStructurePremadeEntry;
import com.creativemd.littletiles.common.structure.type.premade.LittleStructurePremade.LittleStructureTypePremade;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.place.PlacementMode;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemPremadeStructure extends Item implements ICreativeRendered, ILittlePlacer {
    
    public ItemPremadeStructure() {
        setCreativeTab(LittleTiles.littleTab);
        hasSubtypes = true;
    }
    
    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return super.getUnlocalizedName(stack) + "." + getPremadeId(stack);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public List<RenderBox> getRenderingCubes(IBlockState state, TileEntity te, ItemStack stack) {
        if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("structure"))
            return Collections.EMPTY_LIST;
        
        LittleStructureTypePremade premade = LittleStructurePremade.getType(stack.getTagCompound().getCompoundTag("structure").getString("id"));
        if (premade == null)
            return Collections.EMPTY_LIST;
        LittlePreviews previews = getLittlePreview(stack);
        if (previews == null)
            return Collections.EMPTY_LIST;
        List<RenderBox> cubes = premade.getRenderingCubes(previews);
        if (cubes == null) {
            cubes = new ArrayList<>();
            
            for (LittlePreview preview : previews.allPreviews())
                if (!preview.isInvisible())
                    cubes.add(preview.getCubeBlock(previews.getContext()));
                
            LittlePreview.shrinkCubesToOneBlock(cubes);
        }
        
        return cubes;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public LittleGridContext getPositionContext(ItemStack stack) {
        return ItemMultiTiles.currentContext;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiConfigure getConfigureGUIAdvanced(EntityPlayer player, ItemStack stack) {
        return new SubGuiModeSelector(stack, ItemMultiTiles.currentContext, ItemLittleChisel.currentMode) {
            
            @Override
            public void saveConfiguration(LittleGridContext context, PlacementMode mode) {
                ItemLittleChisel.currentMode = mode;
                ItemMultiTiles.currentContext = context;
            }
        };
    }
    
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
    
    public boolean isInCreativeTab(CreativeTabs targetTab, LittleStructureTypePremade type) {
        CreativeTabs tab = type.getCustomTab();
        if (tab == null)
            tab = getCreativeTab();
        if (tab == targetTab)
            return true;
        return tab != null && (targetTab == CreativeTabs.SEARCH || targetTab == tab);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
        for (LittleStructureTypePremade entry : LittleStructurePremade.getPremadeStructureTypes())
            if (entry.showInCreativeTab && isInCreativeTab(tab, entry))
                list.add(entry.createItemStack());
    }
    
    @Override
    public boolean hasLittlePreview(ItemStack stack) {
        String id = getPremadeId(stack);
        return LittleStructurePremade.getStructurePremadeEntry(id) != null;
    }
    
    public void removeUnnecessaryData(ItemStack stack) {
        if (stack.hasTagCompound()) {
            stack.getTagCompound().removeTag("tiles");
            stack.getTagCompound().removeTag("size");
            stack.getTagCompound().removeTag("min");
        }
    }
    
    private static HashMap<String, LittlePreviews> cachedPreviews = new HashMap<>();
    
    public static void clearCache() {
        cachedPreviews.clear();
    }
    
    private LittlePreviews getPreviews(String id) {
        if (cachedPreviews.containsKey(id))
            return cachedPreviews.get(id).copy();
        LittlePreviews previews = LittleStructurePremade.getPreviews(id);
        if (previews != null)
            return previews.copy();
        return null;
    }
    
    @Override
    public LittlePreviews getLittlePreview(ItemStack stack) {
        String id = getPremadeId(stack);
        LittlePreviews previews = getPreviews(id);
        if (previews != null && previews.structureNBT != null && stack.hasTagCompound() && stack.getTagCompound().hasKey("structure"))
            NBTUtils.mergeNotOverwrite(previews.structureNBT, stack.getTagCompound().getCompoundTag("structure"));
        return previews;
    }
    
    @Override
    public void rotate(EntityPlayer player, ItemStack stack, Rotation rotation, boolean client) {
        String id = getPremadeId(stack);
        LittlePreviews previews = getPreviews(id);
        if (previews.isEmpty())
            return;
        previews.rotatePreviews(rotation, previews.getContext().rotationCenter);
        saveLittlePreview(stack, previews);
    }
    
    @Override
    public void flip(EntityPlayer player, ItemStack stack, Axis axis, boolean client) {
        String id = getPremadeId(stack);
        LittlePreviews previews = getPreviews(id);
        if (previews.isEmpty())
            return;
        previews.flipPreviews(axis, previews.getContext().rotationCenter);
        saveLittlePreview(stack, previews);
    }
    
    @Override
    public void saveLittlePreview(ItemStack stack, LittlePreviews previews) {
        cachedPreviews.put(getPremadeId(stack), previews);
    }
    
    @Override
    public boolean sendTransformationUpdate() {
        return false;
    }
    
    @Override
    public boolean containsIngredients(ItemStack stack) {
        return true;
    }
    
    @Override
    public PlacementMode getPlacementMode(ItemStack stack) {
        if (!ItemMultiTiles.currentMode.canPlaceStructures())
            return PlacementMode.getStructureDefault();
        return ItemMultiTiles.currentMode;
    }
    
    @Override
    public boolean shouldCache() {
        return false;
    }
    
    @Override
    public boolean snapToGridByDefault(ItemStack stack) {
        LittleStructureType type = LittleStructureRegistry.getStructureType(getPremadeId(stack));
        if (type instanceof LittleStructureTypePremade)
            return ((LittleStructureTypePremade) type).snapToGrid;
        return false;
    }
    
    public static String getPremadeId(ItemStack stack) {
        if (stack.hasTagCompound())
            return stack.getTagCompound().getCompoundTag("structure").getString("id");
        return null;
    }
    
    public static LittleStructurePremadeEntry getPremade(ItemStack stack) {
        if (stack.hasTagCompound())
            return LittleStructurePremade.getStructurePremadeEntry(stack.getTagCompound().getCompoundTag("structure").getString("id"));
        return null;
    }
    
}
