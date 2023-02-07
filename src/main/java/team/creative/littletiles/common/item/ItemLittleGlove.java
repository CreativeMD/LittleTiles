package team.creative.littletiles.common.item;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.controls.collection.GuiStackSelector;
import team.creative.creativecore.common.gui.controls.simple.GuiColorPicker;
import team.creative.creativecore.common.gui.controls.simple.GuiShowItem;
import team.creative.creativecore.common.gui.controls.simple.GuiSteppedSlider;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.mc.TooltipUtils;
import team.creative.creativecore.common.util.registry.NamedHandlerRegistry;
import team.creative.creativecore.common.util.type.Color;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActionPlace;
import team.creative.littletiles.common.action.LittleActionPlace.PlaceAction;
import team.creative.littletiles.common.api.tool.ILittlePlacer;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.LittleGuiUtils;
import team.creative.littletiles.common.gui.tool.GuiConfigure;
import team.creative.littletiles.common.gui.tool.GuiGlove;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;
import team.creative.littletiles.common.level.LittleLevelScanner;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.packet.action.BlockPacket;
import team.creative.littletiles.common.packet.action.BlockPacket.BlockPacketAction;
import team.creative.littletiles.common.packet.action.VanillaBlockPacket;
import team.creative.littletiles.common.packet.action.VanillaBlockPacket.VanillaBlockAction;
import team.creative.littletiles.common.placement.PlacementHelper;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.mode.PlacementMode;

public class ItemLittleGlove extends Item implements ILittlePlacer, IItemTooltip {
    
    public static final NamedHandlerRegistry<GloveMode> MODES = new NamedHandlerRegistry<>(null);
    
    static {
        MODES.registerDefault("pixel", new PixelMode());
        MODES.register("place_preview", new PlacePreviewMode());
        MODES.register("replace", new ReplaceMode());
    }
    
    public ItemLittleGlove() {
        super(new Item.Properties().stacksTo(1));
    }
    
    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return false;
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
    public boolean hasTiles(ItemStack stack) {
        return getMode(stack).hasTiles(stack);
    }
    
    @Override
    public LittleGroup getTiles(ItemStack stack) {
        return getMode(stack).getTiles(stack);
    }
    
    @Override
    public LittleGroup getLow(ItemStack stack) {
        return getTiles(stack);
    }
    
    @Override
    public PlacementPreview getPlacement(Level level, ItemStack stack, PlacementPosition position, boolean allowLowResolution) {
        return PlacementPreview.relative(level, stack, position, allowLowResolution);
    }
    
    @Override
    public void saveTiles(ItemStack stack, LittleGroup previews) {
        getMode(stack).setTiles(previews, stack);
    }
    
    @Override
    public boolean containsIngredients(ItemStack stack) {
        return false;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClickAir(Player player, ItemStack stack) {
        getMode(stack).leftClickAir(player.level, player, stack);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onClickBlock(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        getMode(stack).leftClickBlock(level, player, stack, result);
        return true;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onRightClick(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        return getMode(stack).rightClickBlock(level, player, stack, result);
    }
    
    @Override
    public boolean onMouseWheelClickBlock(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        return getMode(stack).wheelClickBlock(level, player, stack, result);
    }
    
    @Override
    public PlacementMode getPlacementMode(ItemStack stack) {
        return ItemMultiTiles.currentMode;
    }
    
    @Override
    public GuiConfigure getConfigure(Player player, ContainerSlotView view) {
        return ItemLittleGlove.getMode(view.get()).getGui(player, view, ((ILittlePlacer) view.get().getItem()).getPositionGrid(view.get()));
    }
    
    @Override
    public LittleGrid getPositionGrid(ItemStack stack) {
        return ItemMultiTiles.currentGrid;
    }
    
    public static GloveMode getMode(ItemStack stack) {
        return MODES.get(stack.getOrCreateTag().getString("mode"));
    }
    
    public static void setMode(ItemStack stack, GloveMode mode) {
        stack.getOrCreateTag().putString("mode", MODES.getId(mode));
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { Component.translatable(getMode(stack).title), LittleTilesClient.configure.getTranslatedKeyMessage() };
    }
    
    public static abstract class GloveMode {
        
        public final String title;
        
        public GloveMode(String name) {
            this.title = "grabber.mode." + name;
        }
        
        public void addExtraInformation(CompoundTag nbt, List<Component> tooltip) {}
        
        @OnlyIn(Dist.CLIENT)
        public void leftClickAir(Level level, Player player, ItemStack stack) {}
        
        @OnlyIn(Dist.CLIENT)
        public void leftClickBlock(Level level, Player player, ItemStack stack, BlockHitResult result) {}
        
        @OnlyIn(Dist.CLIENT)
        public boolean rightClickBlock(Level level, Player player, ItemStack stack, BlockHitResult result) {
            return true;
        }
        
        @OnlyIn(Dist.CLIENT)
        public abstract boolean wheelClickBlock(Level level, Player player, ItemStack stack, BlockHitResult result);
        
        @OnlyIn(Dist.CLIENT)
        public abstract boolean renderBlockSeparately(ItemStack stack);
        
        @OnlyIn(Dist.CLIENT)
        public abstract GuiConfigure getGui(Player player, ContainerSlotView view, LittleGrid grid);
        
        public boolean hasTiles(ItemStack stack) {
            return true;
        }
        
        public abstract LittleGroup getTiles(ItemStack stack);
        
        public abstract void setTiles(LittleGroup previews, ItemStack stack);
        
        public abstract LittleElement getSeparateRenderingPreview(ItemStack stack);
        
        public abstract void vanillaBlockAction(Level level, ItemStack stack, BlockPos pos, BlockState state);
        
        public abstract void littleBlockAction(Level level, BETiles be, LittleTileContext context, ItemStack stack, BlockPos pos, CompoundTag nbt);
        
    }
    
    public static abstract class SimpleMode extends GloveMode {
        
        public SimpleMode(String name) {
            super(name);
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean wheelClickBlock(Level level, Player player, ItemStack stack, BlockHitResult result) {
            BlockState state = level.getBlockState(result.getBlockPos());
            if (LittleAction.isBlockValid(state)) {
                LittleTiles.NETWORK.sendToServer(new VanillaBlockPacket(result.getBlockPos(), VanillaBlockAction.GRABBER));
                return true;
            } else if (state.getBlock() instanceof BlockTile) {
                CompoundTag nbt = new CompoundTag();
                nbt.putBoolean("secondMode", LittleActionHandlerClient.isUsingSecondMode());
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
        
        public static void setElement(CompoundTag nbt, LittleElement element) {
            nbt.put("element", element.save(new CompoundTag()));
        }
    }
    
    public static class PixelMode extends SimpleMode {
        
        public PixelMode() {
            super("pixel");
        }
        
        @Override
        public GuiConfigure getGui(Player player, ContainerSlotView view, LittleGrid grid) {
            return new GuiGlove(this, view, 140, 140, grid) {
                
                public LittleVec size;
                
                {
                    registerEventClick(x -> {
                        if (x.control.is("sliced"))
                            updateLabel();
                    });
                    registerEventChanged(x -> {
                        size.x = (int) ((GuiSteppedSlider) get("sizeX")).value;
                        size.y = (int) ((GuiSteppedSlider) get("sizeY")).value;
                        size.z = (int) ((GuiSteppedSlider) get("sizeZ")).value;
                        updateLabel();
                    });
                }
                
                @Override
                public void create() {
                    super.create();
                    LittleGrid oldContext = LittleGrid.get(view.get().getTag());
                    LittleElement element = ItemLittleGlove.SimpleMode.getElement(view.get());
                    LittleBox box = PixelMode.getBox(view.get());
                    if (oldContext != grid)
                        box.convertTo(oldContext, grid);
                    if (box.minX == box.maxX)
                        box.maxX++;
                    if (box.minY == box.maxY)
                        box.maxY++;
                    if (box.minZ == box.maxZ)
                        box.maxZ++;
                    size = box.getSize();
                    
                    add(new GuiSteppedSlider("sizeX", size.x, 1, grid.count));
                    add(new GuiSteppedSlider("sizeY", size.y, 1, grid.count));
                    add(new GuiSteppedSlider("sizeZ", size.z, 1, grid.count));
                    
                    add(new GuiColorPicker("picker", new Color(element.color), LittleTiles.CONFIG.isTransparencyEnabled(getPlayer()), LittleTiles.CONFIG
                            .getMinimumTransparency(getPlayer())));
                    
                    add(new GuiShowItem("item", ItemStack.EMPTY).setDim(32, 32));
                    
                    GuiStackSelector selector = new GuiStackSelector("preview", getPlayer(), LittleGuiUtils.getCollector(getPlayer()), true);
                    selector.setSelectedForce(element.getBlock().getStack());
                    add(selector);
                    
                    updateLabel();
                }
                
                public LittleElement getElement() {
                    GuiStackSelector selector = (GuiStackSelector) get("preview");
                    GuiColorPicker picker = (GuiColorPicker) get("picker");
                    ItemStack selected = selector.getSelected();
                    
                    if (!selected.isEmpty() && selected.getItem() instanceof BlockItem)
                        return new LittleElement(Block.byItem(selected.getItem()).defaultBlockState(), picker.color.toInt());
                    else
                        return ItemLittleGlove.SimpleMode.getElement(view.get());
                }
                
                public LittleBox getBox() {
                    return new LittleBox(0, 0, 0, size.x, size.y, size.z);
                }
                
                public void updateLabel() {
                    ((GuiShowItem) get("avatar")).stack = ItemMultiTiles.of(getElement(), grid, getBox());
                }
                
                @Override
                public CompoundTag saveConfiguration(CompoundTag nbt) {
                    SimpleMode.setElement(nbt, getElement());
                    setBox(nbt, getBox());
                    grid.set(nbt);
                    return super.saveConfiguration(nbt);
                }
            };
        }
        
        @Override
        public void addExtraInformation(CompoundTag nbt, List<Component> tooltip) {
            super.addExtraInformation(nbt, tooltip);
            tooltip.add(Component.literal(TooltipUtils.printColor(ItemLittleGlove.SimpleMode.getElement(nbt).color)));
        }
        
        @Override
        public LittleGroup getTiles(ItemStack stack) {
            LittleGroup group = new LittleGroup();
            group.addTile(LittleGrid.get(stack.getTag()), new LittleTile(SimpleMode.getElement(stack), getBox(stack)));
            return group;
        }
        
        public static LittleBox getBox(ItemStack stack) {
            if (stack.getOrCreateTag().contains("box"))
                return LittleBox.create(stack.getTag().getIntArray("box"));
            LittleBox box = new LittleBox(0, 0, 0, 1, 1, 1);
            setBox(stack.getOrCreateTag(), box);
            return box;
        }
        
        public static void setBox(CompoundTag nbt, LittleBox box) {
            nbt.putIntArray("box", box.getArray());
        }
        
        @Override
        public void setTiles(LittleGroup previews, ItemStack stack) {}
        
    }
    
    public static class PlacePreviewMode extends GloveMode {
        
        public PlacePreviewMode() {
            super("place_preview");
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean wheelClickBlock(Level level, Player player, ItemStack stack, BlockHitResult result) {
            BlockState state = level.getBlockState(result.getBlockPos());
            if (state.getBlock() instanceof BlockTile) {
                CompoundTag nbt = new CompoundTag();
                nbt.putBoolean("secondMode", LittleActionHandlerClient.isUsingSecondMode());
                LittleTiles.NETWORK.sendToServer(new BlockPacket(level, result.getBlockPos(), player, BlockPacketAction.GRABBER, nbt));
                return true;
            }
            return false;
        }
        
        @Override
        public void littleBlockAction(Level level, BETiles be, LittleTileContext context, ItemStack stack, BlockPos pos, CompoundTag nbt) {
            LittleGroup previews = new LittleGroup();
            if (nbt.getBoolean("secondMode")) {
                for (Pair<IParentCollection, LittleTile> pair : be.allTiles())
                    previews.add(pair.key.getGrid(), pair.value, pair.value);
            } else
                previews.add(context.parent.getGrid(), context.tile, context.box);
            stack.getOrCreateTag().put("tiles", LittleGroup.save(previews));
        }
        
        public static LittleGroup getPreviews(ItemStack stack) {
            if (stack.getOrCreateTag().contains("tiles"))
                return LittleGroup.load(stack.getOrCreateTagElement("tiles"));
            
            LittleGroup group = new LittleGroup();
            group.add(LittleGrid.defaultGrid(), new LittleElement(Blocks.STONE.defaultBlockState(), ColorUtils.WHITE), new LittleBox(0, 0, 0, 1, 1, 1));
            stack.getOrCreateTag().put("tiles", LittleGroup.save(group));
            return group;
        }
        
        public static BlockPos getOrigin(ItemStack stack) {
            CompoundTag nbt = stack.getOrCreateTag();
            return new BlockPos(nbt.getInt("ox"), nbt.getInt("oy"), nbt.getInt("oz"));
        }
        
        public static void setOrigin(ItemStack stack, BlockPos pos) {
            CompoundTag nbt = stack.getOrCreateTag();
            nbt.putInt("ox", pos.getX());
            nbt.putInt("oy", pos.getY());
            nbt.putInt("oz", pos.getZ());
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean renderBlockSeparately(ItemStack stack) {
            return false;
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public GuiConfigure getGui(Player player, ContainerSlotView view, LittleGrid grid) {
            return new GuiGlove(this, view, 140, 140, grid) {};
        }
        
        @Override
        public LittleGroup getTiles(ItemStack stack) {
            return getPreviews(stack);
        }
        
        @Override
        public void setTiles(LittleGroup previews, ItemStack stack) {
            stack.getOrCreateTag().put("tiles", LittleGroup.save(previews));
        }
        
        @Override
        public LittleElement getSeparateRenderingPreview(ItemStack stack) {
            return null;
        }
        
        @Override
        public void vanillaBlockAction(Level level, ItemStack stack, BlockPos pos, BlockState state) {}
        
    }
    
    public static class ReplaceMode extends SimpleMode {
        
        public ReplaceMode() {
            super("replace");
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public GuiGlove getGui(Player player, ContainerSlotView view, LittleGrid grid) {
            return new GuiGlove(this, view, 140, 140, grid) {
                
                @Override
                public void create() {
                    super.create();
                    LittleElement element = SimpleMode.getElement(view.get());
                    
                    add(new GuiColorPicker("picker", new Color(element.color), LittleTiles.CONFIG.isTransparencyEnabled(getPlayer()), LittleTiles.CONFIG
                            .getMinimumTransparency(getPlayer())));
                    
                    GuiStackSelector selector = new GuiStackSelector("preview", getPlayer(), LittleGuiUtils.getCollector(getPlayer()), true);
                    selector.setSelectedForce(element.getBlock().getStack());
                    add(selector);
                }
                
                public LittleElement getElement() {
                    GuiStackSelector selector = (GuiStackSelector) get("preview");
                    GuiColorPicker picker = (GuiColorPicker) get("picker");
                    ItemStack selected = selector.getSelected();
                    
                    if (!selected.isEmpty() && selected.getItem() instanceof BlockItem)
                        return new LittleElement(Block.byItem(selected.getItem()).defaultBlockState(), picker.color.toInt());
                    else
                        return ItemLittleGlove.SimpleMode.getElement(view.get());
                }
                
                @Override
                public CompoundTag saveConfiguration(CompoundTag nbt) {
                    SimpleMode.setElement(nbt, getElement());
                    return super.saveConfiguration(nbt);
                }
            };
        }
        
        @Override
        public boolean hasTiles(ItemStack stack) {
            return false;
        }
        
        @Override
        public boolean rightClickBlock(Level level, Player player, ItemStack stack, BlockHitResult result) {
            if (PlacementHelper.canBlockBeUsed(level, result.getBlockPos()))
                return LittleTilesClient.ACTION_HANDLER.execute(new LittleActionPlace(PlaceAction.ABSOLUTE, PlacementPreview
                        .absolute(level, PlacementMode.replace, new LittleGroupAbsolute(LittleLevelScanner.scan(level, result.getBlockPos(), null), getElement(stack)), Facing
                                .get(result.getDirection()))));
            return false;
        }
        
        @Override
        public LittleElement getSeparateRenderingPreview(ItemStack stack) {
            return SimpleMode.getElement(stack);
        }
        
        @Override
        public LittleGroup getTiles(ItemStack stack) {
            return null;
        }
        
        @Override
        public void setTiles(LittleGroup previews, ItemStack stack) {}
        
    }
    
}
