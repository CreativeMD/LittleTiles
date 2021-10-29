package team.creative.littletiles.common.item;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.model.ICreativeRendered;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.client.render.cache.ItemModelCache;
import team.creative.littletiles.common.api.tool.ILittlePlacer;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.SubGuiRecipe;
import team.creative.littletiles.common.gui.SubGuiRecipeAdvancedSelection;
import team.creative.littletiles.common.gui.configure.GuiConfigure;
import team.creative.littletiles.common.gui.configure.SubGuiModeSelector;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.packet.action.BlockPacket;
import team.creative.littletiles.common.packet.action.BlockPacket.BlockPacketAction;
import team.creative.littletiles.common.packet.item.SelectionModePacket;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.placement.selection.SelectionMode;

public class ItemLittleBlueprint extends Item implements ILittlePlacer, ICreativeRendered, IItemTooltip {
    
    public ItemLittleBlueprint() {
        super(new Item.Properties().tab(LittleTiles.LITTLE_TAB));
    }
    
    @Override
    public boolean isComplex() {
        return true;
    }
    
    @Override
    public Component getName(ItemStack stack) {
        if (stack.getOrCreateTag().contains("content") && stack.getOrCreateTagElement("content").contains("structure") && stack.getOrCreateTagElement("structure").contains("name"))
            return new TextComponent(stack.getOrCreateTagElement("content").getCompound("structure").getString("name"));
        return super.getName(stack);
    }
    
    @Override
    public boolean hasTiles(ItemStack stack) {
        return stack.getOrCreateTag().contains("content");
    }
    
    @Override
    public LittleGroup getTiles(ItemStack stack) {
        return LittleGroup.load(stack.getOrCreateTagElement("content"));
    }
    
    @Override
    public PlacementPreview getPlacement(Level level, ItemStack stack, PlacementPosition position, boolean allowLowResolution) {
        return new PlacementPreview(level, getTiles(stack), getPlacementMode(stack), position);
    }
    
    @Override
    public void saveTiles(ItemStack stack, LittleGroup group) {
        stack.getOrCreateTag().put("content", LittleGroup.save(group));
    }
    
    @Override
    public GuiConfigure getConfigure(Player player, ItemStack stack) {
        if (!((ItemLittleBlueprint) stack.getItem()).hasLittlePreview(stack))
            return new SubGuiRecipeAdvancedSelection(stack);
        return new SubGuiRecipe(stack);
    }
    
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 0F;
    }
    
    @Override
    public boolean canDestroyBlockInCreative(Level level, BlockPos pos, ItemStack stack, Player player) {
        return false;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onMouseWheelClickBlock(Level world, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        BlockState state = world.getBlockState(result.getBlockPos());
        if (state.getBlock() instanceof BlockTile) {
            CompoundTag nbt = new CompoundTag();
            nbt.putBoolean("secondMode", LittleActionHandlerClient.isUsingSecondMode(player));
            LittleTiles.NETWORK.sendToServer(new BlockPacket(world, result.getBlockPos(), player, BlockPacketAction.RECIPE, nbt));
            return true;
        }
        return true;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public List<RenderBox> getRenderingBoxes(BlockState state, BlockEntity te, ItemStack stack) {
        if (hasTiles(stack))
            return getTiles(stack).getRenderingBoxes();
        return new ArrayList<RenderBox>();
    }
    
    @OnlyIn(Dist.CLIENT)
    public static BakedModel model;
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyCustomOpenGLHackery(ItemStack stack, TransformType cameraTransformType) {
        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.pushMatrix();
        
        if (cameraTransformType == TransformType.GUI || !stack.hasTagCompound() || !stack.getTagCompound().hasKey("tiles")) {
            if (cameraTransformType == TransformType.GUI)
                GlStateManager.disableDepth();
            if (model == null)
                model = mc.getRenderItem().getItemModelMesher().getModelManager()
                        .getModel(new ModelResourceLocation(LittleTiles.modid + ":recipeadvanced_background", "inventory"));
            ForgeHooksClient.handleCameraTransforms(model, cameraTransformType, false);
            
            mc.getRenderItem().renderItem(new ItemStack(Items.PAPER), model);
            
            if (cameraTransformType == TransformType.GUI)
                GlStateManager.enableDepth();
        }
        GlStateManager.popMatrix();
        
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void saveCachedModel(Facing facing, RenderType layer, List<BakedQuad> cachedQuads, BlockState state, BlockEntity be, ItemStack stack, boolean threaded) {
        ItemModelCache.cacheModel(stack, facing, cachedQuads);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public List<BakedQuad> getCachedModel(Facing facing, RenderType layer, BlockState state, BlockEntity be, ItemStack stack, boolean threaded) {
        return ItemModelCache.requestCache(stack, facing);
    }
    
    @Override
    public boolean containsIngredients(ItemStack stack) {
        return false;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onRightClick(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        if (hasTiles(stack))
            return true;
        getSelectionMode(stack).onRightClick(player, stack, result.getBlockPos());
        LittleTiles.NETWORK.sendToServer(new SelectionModePacket(result.getBlockPos(), true));
        return true;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onClickBlock(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        if (hasTiles(stack))
            return true;
        getSelectionMode(stack).onLeftClick(player, stack, result.getBlockPos());
        LittleTiles.NETWORK.sendToServer(new SelectionModePacket(result.getBlockPos(), false));
        return true;
    }
    
    @Override
    public GuiConfigure getConfigureAdvanced(Player player, ItemStack stack) {
        return new SubGuiModeSelector(stack, ItemMultiTiles.currentContext, ItemMultiTiles.currentMode) {
            
            @Override
            public void saveConfiguration(LittleGridContext context, PlacementMode mode) {
                ItemMultiTiles.currentContext = context;
                ItemMultiTiles.currentMode = mode;
            }
            
        };
    }
    
    @Override
    public PlacementMode getPlacementMode(ItemStack stack) {
        return ItemMultiTiles.currentMode;
    }
    
    @Override
    public LittleGrid getPositionGrid(ItemStack stack) {
        return ItemMultiTiles.currentContext;
    }
    
    @Override
    public LittleVec getCachedSize(ItemStack stack) {
        if (stack.getTag().contains("size"))
            return LittlePreview.getSize(stack);
        return null;
    }
    
    @Override
    public LittleVec getCachedOffset(ItemStack stack) {
        return LittlePreview.getOffset(stack);
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { Minecraft.getInstance().options.keyAttack.getTranslatedKeyMessage(), Minecraft.getInstance().options.keyUse
                .getTranslatedKeyMessage(), Minecraft.getInstance().options.keyPickItem.getTranslatedKeyMessage(), LittleTilesClient.configure.getTranslatedKeyMessage() };
    }
    
    public static SelectionMode getSelectionMode(ItemStack stack) {
        return SelectionMode.getOrDefault(stack.getOrCreateTag().getString("selmode"));
    }
    
    public static void setSelectionMode(ItemStack stack, SelectionMode mode) {
        stack.getOrCreateTag().putString("selmode", mode.name);
    }
}
