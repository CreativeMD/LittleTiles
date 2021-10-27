package team.creative.littletiles.common.item;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.client.avatar.AvatarItemStack;
import com.creativemd.creativecore.client.rendering.model.CreativeBakedModel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiAvatarLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiColorPicker;
import com.creativemd.creativecore.common.gui.controls.gui.custom.GuiStackSelectorAll;
import com.creativemd.creativecore.common.gui.event.container.SlotChangeEvent;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.littletiles.common.action.block.LittleActionReplace;
import com.creativemd.littletiles.common.tile.LittleTileColored;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tile.registry.LittleTileRegistry;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.mojang.blaze3d.platform.GlStateManager;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.model.ICreativeRendered;
import team.creative.creativecore.common.gui.controls.simple.GuiSteppedSlider;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.event.GuiControlClickEvent;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.mc.LanguageUtils;
import team.creative.creativecore.common.util.mc.TooltipUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.api.tool.ILittlePlacer;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.LittleSubGuiUtils;
import team.creative.littletiles.common.gui.SubContainerConfigure;
import team.creative.littletiles.common.gui.SubGuiGrabber;
import team.creative.littletiles.common.gui.configure.GuiConfigure;
import team.creative.littletiles.common.gui.configure.SubGuiModeSelector;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.packet.action.BlockPacket;
import team.creative.littletiles.common.packet.action.BlockPacket.BlockPacketAction;
import team.creative.littletiles.common.packet.action.VanillaBlockPacket;
import team.creative.littletiles.common.packet.action.VanillaBlockPacket.VanillaBlockAction;
import team.creative.littletiles.common.placement.PlacementHelper;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.mode.PlacementMode;

public class ItemLittleGrabber extends Item implements ICreativeRendered, ILittlePlacer, IItemTooltip {
    
    public ItemLittleGrabber() {
        super(new Item.Properties().tab(LittleTiles.LITTLE_TAB).stacksTo(1));
    }
    
    @Override
    public boolean isComplex() {
        return true;
    }
    
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 0F;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        getMode(stack).addExtraInformation(stack.getTag(), tooltip);
    }
    
    @Override
    public boolean canDestroyBlockInCreative(Level leve, BlockPos pos, ItemStack stack, Player player) {
        return false;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public List<RenderBox> getRenderingCubes(BlockState state, BlockEntity te, ItemStack stack) {
        return getMode(stack).getRenderingCubes(stack);
    }
    
    @OnlyIn(Dist.CLIENT)
    public static BakedModel model;
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyCustomOpenGLHackery(ItemStack stack, TransformType cameraTransformType) {
        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.pushMatrix();
        
        model = mc.getRenderItem().getItemModelMesher().getModelManager().getModel(new ModelResourceLocation(LittleTiles.modid + ":grabber_background", "inventory"));
        ForgeHooksClient
                .handleCameraTransforms(model, cameraTransformType, cameraTransformType == TransformType.FIRST_PERSON_LEFT_HAND || cameraTransformType == TransformType.THIRD_PERSON_LEFT_HAND);
        
        mc.getRenderItem().renderItem(new ItemStack(Items.PAPER), model);
        
        if (cameraTransformType == TransformType.GUI) {
            GlStateManager.translate(0.1, 0.1, 0);
            GlStateManager.scale(0.7, 0.7, 0.7);
            
            GrabberMode mode = getMode(stack);
            if (mode.renderBlockSeparately(stack)) {
                LittlePreview preview = mode.getSeparateRenderingPreview(stack);
                ItemStack blockStack = new ItemStack(preview.getBlock(), 1, preview.getMeta());
                model = mc.getRenderItem().getItemModelWithOverrides(blockStack, mc.world, mc.player); // getItemModelMesher().getItemModel(blockStack);
                if (!(model instanceof CreativeBakedModel))
                    ForgeHooksClient.handleCameraTransforms(model, cameraTransformType, false);
                
                GlStateManager.disableDepth();
                GlStateManager.pushMatrix();
                GlStateManager.translate(-0.5F, -0.5F, -0.5F);
                
                try {
                    if (model.isBuiltInRenderer()) {
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        GlStateManager.enableRescaleNormal();
                        TileEntityItemStackRenderer.instance.renderByItem(blockStack);
                    } else {
                        Color color = preview.hasColor() ? ColorUtils.IntToRGBA(preview.getColor()) : ColorUtils.IntToRGBA(ColorUtils.WHITE);
                        color.setAlpha(255);
                        ReflectionHelper.findMethod(RenderItem.class, "renderModel", "func_191967_a", IBakedModel.class, int.class, ItemStack.class)
                                .invoke(mc.getRenderItem(), model, ColorUtils.RGBAToInt(color), blockStack);
                    }
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                
                GlStateManager.popMatrix();
                
            }
            
            GlStateManager.enableDepth();
            
        }
        
        GlStateManager.popMatrix();
        
    }
    
    @Override
    public boolean hasTiles(ItemStack stack) {
        return true;
    }
    
    @Override
    public LittleGroupAbsolute getTiles(ItemStack stack) {
        return getMode(stack).getTiles(stack);
    }
    
    @Override
    public void saveTiles(ItemStack stack, LittleGroupAbsolute previews) {
        getMode(stack).setTiles(previews, stack);
    }
    
    @Override
    public boolean containsIngredients(ItemStack stack) {
        return false;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClickAir(Player player, ItemStack stack) {
        getMode(stack).onClickBlock(null, player, stack, null);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onClickBlock(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        getMode(stack).onClickBlock(level, player, stack, result);
        return true;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onRightClick(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        return getMode(stack).onRightClick(level, player, stack, result);
    }
    
    @Override
    public boolean onMouseWheelClickBlock(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        return getMode(stack).onMouseWheelClickBlock(level, player, stack, result);
    }
    
    @Override
    public PlacementMode getPlacementMode(ItemStack stack) {
        return ItemMultiTiles.currentMode;
    }
    
    @Override
    public GuiConfigure getConfigure(Player player, ItemStack stack) {
        return ItemLittleGrabber.getMode(stack).getGui(player, stack, ((ILittlePlacer) stack.getItem()).getPositionGrid(stack));
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
    public LittleGrid getPositionGrid(ItemStack stack) {
        return ItemMultiTiles.currentContext;
    }
    
    public static GrabberMode getMode(String name) {
        GrabberMode mode = GrabberMode.modes.get(name);
        if (mode != null)
            return mode;
        return GrabberMode.defaultMode;
    }
    
    public static GrabberMode getMode(ItemStack stack) {
        GrabberMode mode = GrabberMode.modes.get(stack.getOrCreateTag().getString("mode"));
        if (mode != null)
            return mode;
        return GrabberMode.defaultMode;
    }
    
    public static void setMode(ItemStack stack, GrabberMode mode) {
        stack.getOrCreateTag().putString("mode", mode.name);
    }
    
    public static GrabberMode[] getModes() {
        return GrabberMode.modes.values().toArray(new GrabberMode[0]);
    }
    
    public static int indexOf(GrabberMode mode) {
        GrabberMode[] modes = getModes();
        for (int i = 0; i < modes.length; i++) {
            if (modes[i] == mode)
                return i;
        }
        return -1;
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { getMode(stack).getLocalizedName(), LittleTilesClient.configure.getTranslatedKeyMessage(), LittleTilesClient.configureAdvanced
                .getTranslatedKeyMessage() };
    }
    
    public static abstract class GrabberMode {
        
        public static HashMap<String, GrabberMode> modes = new LinkedHashMap<>();
        
        public static PixelMode pixelMode = new PixelMode();
        public static PlacePreviewMode placePreviewMode = new PlacePreviewMode();
        public static ReplaceMode replaceMode = new ReplaceMode();
        
        public static GrabberMode defaultMode = pixelMode;
        
        public final String name;
        public final String title;
        
        public GrabberMode(String name) {
            this.name = name;
            this.title = "grabber.mode." + name;
            modes.put(name, this);
        }
        
        public void addExtraInformation(CompoundTag nbt, List<Component> tooltip) {}
        
        public String getLocalizedName() {
            return LanguageUtils.translate(title);
        }
        
        @OnlyIn(Dist.CLIENT)
        public void onClickBlock(@Nullable Level level, Player player, ItemStack stack, @Nullable BlockHitResult result) {}
        
        @OnlyIn(Dist.CLIENT)
        public boolean onRightClick(Level level, Player player, ItemStack stack, BlockHitResult result) {
            return true;
        }
        
        @OnlyIn(Dist.CLIENT)
        public abstract boolean onMouseWheelClickBlock(Level level, Player player, ItemStack stack, BlockHitResult result);
        
        @OnlyIn(Dist.CLIENT)
        public abstract List<RenderBox> getRenderingCubes(ItemStack stack);
        
        @OnlyIn(Dist.CLIENT)
        public abstract boolean renderBlockSeparately(ItemStack stack);
        
        @OnlyIn(Dist.CLIENT)
        public abstract GuiConfigure getGui(Player player, ItemStack stack, LittleGrid grid);
        
        public abstract LittleGroupAbsolute getTiles(ItemStack stack);
        
        public abstract void setTiles(LittleGroupAbsolute previews, ItemStack stack);
        
        public abstract LittleElement getSeparateRenderingPreview(ItemStack stack);
        
        public abstract void vanillaBlockAction(Level level, ItemStack stack, BlockPos pos, BlockState state);
        
        public abstract void littleBlockAction(Level level, BETiles be, LittleTileContext context, ItemStack stack, BlockPos pos, CompoundTag nbt);
        
    }
    
    public static abstract class SimpleMode extends GrabberMode {
        
        public SimpleMode(String name) {
            super(name);
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean onMouseWheelClickBlock(Level level, Player player, ItemStack stack, BlockHitResult result) {
            BlockState state = level.getBlockState(result.getBlockPos());
            if (LittleAction.isBlockValid(state)) {
                LittleTiles.NETWORK.sendToServer(new VanillaBlockPacket(result.getBlockPos(), VanillaBlockAction.GRABBER));
                return true;
            } else if (state.getBlock() instanceof BlockTile) {
                CompoundTag nbt = new CompoundTag();
                nbt.putBoolean("secondMode", LittleActionHandlerClient.isUsingSecondMode(player));
                LittleTiles.NETWORK.sendToServer(new BlockPacket(level, result.getBlockPos(), player, BlockPacketAction.GRABBER, nbt));
                return true;
            }
            return false;
        }
        
        public LittleGrid getGrid(ItemStack stack) {
            return LittleGrid.get(stack.getTag());
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public List<RenderBox> getRenderingCubes(ItemStack stack) {
            return Collections.emptyList();
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean renderBlockSeparately(ItemStack stack) {
            return true;
        }
        
        @Override
        public void vanillaBlockAction(Level level, ItemStack stack, BlockPos pos, BlockState state) {
            if (LittleAction.isBlockValid(state))
                setElement(stack, new LittleElement(state, ColorUtils.WHITE));
        }
        
        @Override
        public void littleBlockAction(Level level, BETiles be, LittleTileContext context, ItemStack stack, BlockPos pos, CompoundTag nbt) {
            if (LittleAction.isBlockValid(context.tile.getState()))
                setElement(stack, new LittleElement(context.tile.getState(), ColorUtils.WHITE));
        }
        
        @Override
        public LittleElement getSeparateRenderingPreview(ItemStack stack) {
            return getElement(stack);
        }
        
        public static LittleElement getElement(ItemStack stack) {
            if (stack.getOrCreateTag().contains("element"))
                return new LittleElement(stack.getOrCreateTagElement("element"));
            LittleElement element = new LittleElement(Blocks.STONE.defaultBlockState(), ColorUtils.WHITE);
            setElement(stack, element);
            return element;
        }
        
        public static LittleElement getElement(CompoundTag nbt) {
            if (nbt.contains("element"))
                return new LittleElement(nbt.getCompound("element"));
            LittleElement element = new LittleElement(Blocks.STONE.defaultBlockState(), ColorUtils.WHITE);
            nbt.put("element", element.save(new CompoundTag()));
            return element;
        }
        
        public static void setElement(ItemStack stack, LittleElement element) {
            element.save(stack.getOrCreateTagElement("element"));
        }
    }
    
    public static class PixelMode extends SimpleMode {
        
        public PixelMode() {
            super("pixel");
        }
        
        @Override
        public GuiConfigure getGui(Player player, ItemStack stack, LittleGrid grid) {
            return new SubGuiGrabber(this, stack, 140, 140, context) {
                public LittleVec size;
                public boolean isColored = false;
                
                @Override
                public void createControls() {
                    super.createControls();
                    LittleGridContext oldContext = LittleGridContext.get(stack.getTagCompound());
                    LittlePreview preview = ItemLittleGrabber.SimpleMode.getPreview(stack);
                    preview.convertTo(oldContext, context);
                    if (preview.box.minX == preview.box.maxX)
                        preview.box.maxX++;
                    if (preview.box.minY == preview.box.maxY)
                        preview.box.maxY++;
                    if (preview.box.minZ == preview.box.maxZ)
                        preview.box.maxZ++;
                    size = preview.box.getSize();
                    
                    controls.add(new GuiSteppedSlider("sizeX", 25, 20, 50, 10, size.x, 1, context.size));
                    controls.add(new GuiSteppedSlider("sizeY", 25, 35, 50, 10, size.y, 1, context.size));
                    controls.add(new GuiSteppedSlider("sizeZ", 25, 50, 50, 10, size.z, 1, context.size));
                    
                    Color color = ColorUtils.IntToRGBA(preview.getColor());
                    controls.add(new GuiColorPicker("picker", 0, 70, color, LittleTiles.CONFIG.isTransparencyEnabled(getPlayer()), LittleTiles.CONFIG
                            .getMinimumTransparency(getPlayer())));
                    
                    GuiAvatarLabel label = new GuiAvatarLabel("", 110, 20, 0, null);
                    label.name = "avatar";
                    label.height = 60;
                    label.avatarSize = 32;
                    controls.add(label);
                    
                    GuiStackSelectorAll selector = new GuiStackSelectorAll("preview", 0, 120, 112, getPlayer(), LittleSubGuiUtils.getCollector(getPlayer()), true);
                    selector.setSelectedForce(preview.getBlockStack());
                    controls.add(selector);
                    
                    updateLabel();
                }
                
                public LittlePreview getPreview(LittleGridContext context) {
                    GuiStackSelectorAll selector = (GuiStackSelectorAll) get("preview");
                    ItemStack selected = selector.getSelected();
                    
                    if (!selected.isEmpty() && selected.getItem() instanceof ItemBlock) {
                        LittleTile tile = new LittleTile(((ItemBlock) selected.getItem()).getBlock(), selected.getMetadata());
                        tile.setBox(new LittleBox(0, 0, 0, context.size, context.size, context.size));
                        return tile.getPreviewTile();
                    } else
                        return ItemLittleGrabber.SimpleMode.getPreview(stack);
                }
                
                public void updateLabel() {
                    GuiAvatarLabel label = (GuiAvatarLabel) get("avatar");
                    
                    LittlePreview preview = getPreview(context);
                    
                    GuiColorPicker picker = (GuiColorPicker) get("picker");
                    preview.setColor(ColorUtils.RGBAToInt(picker.color));
                    preview.box.set(0, 0, 0, size.x, size.y, size.z);
                    
                    label.avatar = new AvatarItemStack(ItemBlockTiles.getStackFromPreview(context, preview));
                }
                
                @CustomEventSubscribe
                public void onSlotChange(SlotChangeEvent event) {
                    ItemStack slotStack = container.getSlots().get(0).getStack();
                    Block block = Block.getBlockFromItem(slotStack.getItem());
                    if (block instanceof BlockTile) {
                        LittlePreviews previews = ((ILittlePlacer) slotStack.getItem()).getLittlePreview(slotStack);
                        if (previews.size() > 0) {
                            int colorInt = previews.get(0).getColor();
                            Vec3i color = ColorUtils.IntToRGB(colorInt);
                            if (colorInt == -1)
                                color = new Vec3i(255, 255, 255);
                            
                            GuiColorPicker picker = (GuiColorPicker) get("picker");
                            picker.color.set(color.getX(), color.getY(), color.getZ());
                        }
                    }
                    updateLabel();
                }
                
                @CustomEventSubscribe
                public void onClick(GuiControlClickEvent event) {
                    if (event.source.is("sliced"))
                        updateLabel();
                }
                
                @CustomEventSubscribe
                public void onChange(GuiControlChangedEvent event) {
                    size.x = (int) ((GuiSteppedSlider) get("sizeX")).value;
                    size.y = (int) ((GuiSteppedSlider) get("sizeY")).value;
                    size.z = (int) ((GuiSteppedSlider) get("sizeZ")).value;
                    updateLabel();
                }
                
                @Override
                public void saveConfiguration() {
                    LittlePreview preview = getPreview(context);
                    preview.box.set(0, 0, 0, size.x, size.y, size.z);
                    GuiColorPicker picker = (GuiColorPicker) get("picker");
                    preview.setColor(ColorUtils.RGBAToInt(picker.color));
                    setPreview(stack, preview);
                    context.set(stack.getTagCompound());
                }
            };
        }
        
        @Override
        public void addExtraInformation(CompoundTag nbt, List<Component> tooltip) {
            super.addExtraInformation(nbt, tooltip);
            tooltip.add(new TextComponent(TooltipUtils.printColor(ItemLittleGrabber.SimpleMode.getElement(nbt).color)));
        }
        
        @Override
        public LittleGroupAbsolute getTiles(ItemStack stack) {
            LittleGroupAbsolute group = new LittleGroupAbsolute(pos, grid)
        }
        
        public static LittleBox getBox(ItemStack stack) {
            if (stack.getOrCreateTag().contains("box"))
                return LittleBox.create(stack.getTag().getIntArray("box"));
            LittleBox box = new LittleBox(0, 0, 0, 1, 1, 1);
            setBox(stack, box);
            return box;
        }
        
        public static void setBox(ItemStack stack, LittleBox box) {
            stack.getOrCreateTag().putIntArray("box", box.getArray());
        }
        
        @Override
        public void setTiles(LittleGroupAbsolute previews, ItemStack stack) {}
        
    }
    
    public static class PlacePreviewMode extends GrabberMode {
        
        public PlacePreviewMode() {
            super("place_preview");
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean onMouseWheelClickBlock(Level level, Player player, ItemStack stack, BlockHitResult result) {
            BlockState state = level.getBlockState(result.getBlockPos());
            if (state.getBlock() instanceof BlockTile) {
                CompoundTag nbt = new CompoundTag();
                nbt.putBoolean("secondMode", LittleActionHandlerClient.isUsingSecondMode(player));
                LittleTiles.NETWORK.sendToServer(new BlockPacket(level, result.getBlockPos(), player, BlockPacketAction.GRABBER, nbt));
                return true;
            }
            return false;
        }
        
        @Override
        public void littleBlockAction(Level level, BETiles be, LittleTileContext context, ItemStack stack, BlockPos pos, CompoundTag nbt) {
            LittlePreviews previews = new LittlePreviews(te.getContext());
            if (nbt.getBoolean("secondMode")) {
                for (Pair<IParentTileList, LittleTile> pair : te.allTiles())
                    previews.addWithoutCheckingPreview(pair.value.getPreviewTile());
            } else
                previews.addWithoutCheckingPreview(tile.getPreviewTile());
            ItemLittleGrabber.PlacePreviewMode.setPreview(stack, previews);
        }
        
        public static LittlePreviews getPreview(ItemStack stack) {
            if (!stack.hasTagCompound())
                stack.setTagCompound(new NBTTagCompound());
            
            LittlePreviews previews = LittlePreview.getPreview(stack);
            if (previews.size() == 0) {
                IBlockState state = stack.getTagCompound().hasKey("state") ? Block.getStateById(stack.getTagCompound().getInteger("state")) : Blocks.STONE.getDefaultState();
                LittleTile tile = stack.getTagCompound().hasKey("color") ? new LittleTileColored(state.getBlock(), state.getBlock().getMetaFromState(state), stack.getTagCompound()
                        .getInteger("color")) : new LittleTile(state.getBlock(), state.getBlock().getMetaFromState(state));
                tile.setBox(new LittleBox(0, 0, 0, 1, 1, 1));
                LittlePreview preview = tile.getPreviewTile();
                
                previews.addWithoutCheckingPreview(preview);
                setPreview(stack, previews);
            }
            
            return previews;
        }
        
        public static void setPreview(ItemStack stack, LittlePreviews previews) {
            if (!stack.hasTagCompound())
                stack.setTagCompound(new NBTTagCompound());
            
            LittlePreview.savePreview(previews, stack);
        }
        
        public static BlockPos getOrigin(ItemStack stack) {
            if (!stack.hasTagCompound())
                stack.setTagCompound(new NBTTagCompound());
            
            return new BlockPos(stack.getTagCompound().getInteger("ox"), stack.getTagCompound().getInteger("oy"), stack.getTagCompound().getInteger("oz"));
        }
        
        public static void setOrigin(ItemStack stack, BlockPos pos) {
            if (!stack.hasTagCompound())
                stack.setTagCompound(new NBTTagCompound());
            
            stack.getTagCompound().setInteger("ox", pos.getX());
            stack.getTagCompound().setInteger("oy", pos.getY());
            stack.getTagCompound().setInteger("oz", pos.getZ());
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public List<RenderBox> getRenderingCubes(ItemStack stack) {
            return LittlePreview.getCubesForStackRendering(stack);
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean renderBlockSeparately(ItemStack stack) {
            return false;
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public GuiConfigure getGui(Player player, ItemStack stack, LittleGrid grid) {
            return new SubGuiGrabber(this, stack, 140, 140, grid) {
                
                @Override
                public void saveConfiguration() {
                
                }
                
            };
        }
        
        @Override
        public LittlePreviews getPreviews(ItemStack stack) {
            return getPreview(stack);
        }
        
        @Override
        public void setPreviews(LittlePreviews previews, ItemStack stack) {
            PlacePreviewMode.setPreview(stack, previews);
        }
        
    }
    
    public static class ReplaceMode extends SimpleMode {
        
        public ReplaceMode() {
            super("replace");
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public SubGuiGrabber getGui(EntityPlayer player, ItemStack stack, LittleGridContext context) {
            return new SubGuiGrabber(this, stack, 140, 140, context) {
                
                @Override
                public void createControls() {
                    LittlePreview preview = ItemLittleGrabber.SimpleMode.getPreview(stack);
                    
                    Color color = ColorUtils.IntToRGBA(preview.getColor());
                    controls.add(new GuiColorPicker("picker", 0, 70, color, LittleTiles.CONFIG.isTransparencyEnabled(getPlayer()), LittleTiles.CONFIG
                            .getMinimumTransparency(getPlayer())));
                    
                    GuiStackSelectorAll selector = new GuiStackSelectorAll("preview", 0, 120, 112, getPlayer(), LittleSubGuiUtils.getCollector(getPlayer()), true);
                    selector.setSelectedForce(preview.getBlockStack());
                    controls.add(selector);
                    super.createControls();
                }
                
                public LittlePreview getPreview(LittleGridContext context) {
                    GuiStackSelectorAll selector = (GuiStackSelectorAll) get("preview");
                    ItemStack selected = selector.getSelected();
                    
                    if (!selected.isEmpty() && selected.getItem() instanceof ItemBlock) {
                        LittleTile tile = new LittleTile(((ItemBlock) selected.getItem()).getBlock(), selected.getMetadata());
                        tile.setBox(new LittleBox(0, 0, 0, 1, 1, 1));
                        return tile.getPreviewTile();
                    } else
                        return ItemLittleGrabber.SimpleMode.getPreview(stack);
                }
                
                @Override
                public void saveConfiguration() {
                    LittlePreview preview = getPreview(context);
                    GuiColorPicker picker = (GuiColorPicker) get("picker");
                    preview.setColor(ColorUtils.RGBAToInt(picker.color));
                    if (stack.getTagCompound().hasKey("preview")) {
                        LittlePreview oldPreview = LittleTileRegistry.loadPreview(stack.getTagCompound().getCompoundTag("preview"));
                        if (oldPreview != null)
                            preview.box = oldPreview.box;
                    }
                    setPreview(stack, preview);
                    context.set(stack.getTagCompound());
                }
            };
        }
        
        @Override
        public SubContainerConfigure getContainer(EntityPlayer player, ItemStack stack) {
            return new SubContainerConfigure(player, stack);
        }
        
        @Override
        public LittlePreviews getPreviews(ItemStack stack) {
            return new LittlePreviews(LittleGridContext.get());
        }
        
        @Override
        public boolean onRightClick(World world, EntityPlayer player, ItemStack stack, RayTraceResult result) {
            if (PlacementHelper.canBlockBeUsed(world, result.getBlockPos()))
                new LittleActionReplace(world, result.getBlockPos(), player, getPreview(stack)).execute();
            return false;
        }
        
        @Override
        public LittlePreview getSeparateRenderingPreview(ItemStack stack) {
            return getPreview(stack);
        }
        
    }
    
}
