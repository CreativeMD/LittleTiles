package team.creative.littletiles.common.gui.tool;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.DataResult;

import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.control.inventory.GuiInventoryGrid;
import team.creative.creativecore.common.gui.control.inventory.GuiPlayerInventoryGrid;
import team.creative.creativecore.common.gui.control.inventory.IGuiInventory;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.mc.LevelUtils;
import team.creative.creativecore.common.util.type.Color;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.common.ingredient.ILittleIngredientInventory;
import team.creative.littletiles.common.gui.control.GuiColorProgressBar;
import team.creative.littletiles.common.ingredient.BlockIngredient;
import team.creative.littletiles.common.ingredient.BlockIngredientEntry;
import team.creative.littletiles.common.ingredient.ColorIngredient;
import team.creative.littletiles.common.ingredient.LittleIngredient;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.ingredient.LittleInventory;
import team.creative.littletiles.common.ingredient.NotEnoughIngredientsException.NotEnoughSpaceException;
import team.creative.littletiles.common.item.ItemBlockIngredient;
import team.creative.littletiles.common.item.ItemColorIngredient;
import team.creative.littletiles.common.item.ItemColorIngredient.ColorIngredientType;
import team.creative.littletiles.common.item.ItemLittleBag;

public class GuiBag extends GuiConfigure {
    
    public SimpleContainer bagInventory;
    public GuiInventoryGrid bagInventoryGui;
    public LittleIngredients bag;
    public SimpleContainer input = new SimpleContainer(1);
    private List<IGuiInventory> inventories = new ArrayList<>();
    private List<IGuiInventory> inventoriesInv = new ArrayList<>();
    
    public final GuiSyncLocal<CompoundTag> RELOAD = getSyncHolder().register("reload", nbt -> {
        DataResult<ItemStack> result = ItemStack.CODEC.parse(NbtOps.INSTANCE, nbt);
        tool.set(result.getOrThrow());
        load();
    });
    
    public final GuiSyncLocal<StringTag> DROP_COLOR = getSyncHolder().register("drop_color", nbt -> {
        ColorIngredientType type = ColorIngredientType.getType(nbt.getAsString());
        ColorIngredient color = bag.get(ColorIngredient.class);
        if (color != null && !color.isEmpty()) {
            int amount = Math.min(type.getIngredient(color), ColorIngredient.BOTTLE_SIZE);
            if (amount > 0) {
                type.setIngredient(color, type.getIngredient(color) - amount);
                
                Player player = getPlayer();
                LittleInventory inventory = new LittleInventory(player);
                ItemStack colorStack = ItemColorIngredient.generateItemStack(type, amount);
                if (!inventory.addStack(colorStack))
                    LevelUtils.dropItem(player, colorStack);
                
                saveBagInventory();
                sendUpdateToClient();
                tick();
            }
        }
    });
    
    public GuiBag(ContainerSlotView view) {
        super("bag", view);
        registerEventClick(x -> {
            if (x.control instanceof GuiColorProgressBar)
                DROP_COLOR.send(StringTag.valueOf(x.control.name));
        });
        
    }
    
    public void sendUpdateToClient() {
        if (isClient())
            return;
        
        DataResult<Tag> dataresult = ItemStack.CODEC.encode(tool.get(), NbtOps.INSTANCE, new CompoundTag());
        RELOAD.send((CompoundTag) dataresult.getOrThrow());
    }
    
    protected GuiInventoryGrid addInventory(GuiInventoryGrid inventory) {
        inventories.add(inventory);
        inventoriesInv.add(0, inventory);
        return inventory;
    }
    
    @Override
    public void create() {
        sendUpdateToClient();
        flow = GuiFlow.STACK_Y;
        
        bag = ((ItemLittleBag) tool.get().getItem()).getInventory(tool.get());
        ColorIngredient unit = bag.get(ColorIngredient.class);
        
        GuiParent upper = new GuiParent(GuiFlow.STACK_X);
        add(upper);
        GuiParent left = new GuiParent();
        upper.add(left);
        
        GuiParent right = new GuiParent(GuiFlow.STACK_Y);
        upper.add(right.setAlign(Align.STRETCH).setExpandableX());
        
        GuiInventoryGrid inputInventory;
        right.add(inputInventory = new GuiInventoryGrid("input", input).addListener(x -> {
            Player player = getPlayer();
            ItemStack input = GuiBag.this.input.getItem(0);
            
            if (input.getItem() instanceof ILittleIngredientInventory inv) {
                
                LittleIngredients ingredients = inv.getInventory(input);
                
                boolean containsBlocks = ingredients.contains(BlockIngredient.class);
                boolean containsColor = ingredients.contains(ColorIngredient.class);
                
                LittleIngredients remaining = bag.add(ingredients);
                ((ItemLittleBag) tool.get().getItem()).setInventory(tool.get(), bag, null);
                
                if (remaining == null)
                    remaining = new LittleIngredients();
                
                if (remaining.copy().sub(ingredients.copy()) != null) {
                    if (containsBlocks) {
                        clearItemCache();
                        player.playSound(SoundEvents.ITEM_FRAME_PLACE, 1.0F, 1.0F);
                    }
                    
                    if (containsColor) {
                        player.playSound(SoundEvents.BREWING_STAND_BREW, 1.0F, 1.0F);
                        sendUpdateToClient();
                    }
                }
                
                inv.setInventory(input, remaining, null);
                saveBagInventory();
            } else {
                LittleIngredients ingredients = LittleIngredient.extractWithoutCount(provider(), input, true);
                if (ingredients != null) {
                    ingredients.scale(input.getCount());
                    
                    boolean containsBlocks = ingredients.contains(BlockIngredient.class);
                    boolean containsColor = ingredients.contains(ColorIngredient.class);
                    
                    LittleIngredients overflow = bag.add(ingredients);
                    if (overflow == null || (!overflow.contains(BlockIngredient.class) && !overflow.contains(ColorIngredient.class))) {
                        
                        input.setCount(0);
                        ((ItemLittleBag) tool.get().getItem()).setInventory(tool.get(), bag, null);
                        
                        LittleInventory inventory = new LittleInventory(player);
                        try {
                            if (overflow != null)
                                inventory.give(overflow);
                        } catch (NotEnoughSpaceException e) {}
                        
                        if (containsBlocks) {
                            clearItemCache();
                            player.playSound(SoundEvents.ITEM_FRAME_PLACE, 1.0F, 1.0F);
                        }
                        
                        if (containsColor) {
                            player.playSound(SoundEvents.BREWING_STAND_BREW, 1.0F, 1.0F);
                            sendUpdateToClient();
                        }
                        
                    } else
                        bag = ((ItemLittleBag) tool.get().getItem()).getInventory(tool.get());
                    
                }
                
                saveBagInventory();
            }
            
        }));
        int colorStorage = LittleTiles.CONFIG.general.bag.colorStorage;
        right.add(new GuiColorProgressBar("black", unit.black, colorStorage, Color.BLACK));
        right.add(new GuiColorProgressBar("cyan", unit.cyan, colorStorage, Color.CYAN));
        right.add(new GuiColorProgressBar("magenta", unit.magenta, colorStorage, Color.MAGENTA));
        right.add(new GuiColorProgressBar("yellow", unit.yellow, colorStorage, Color.YELLOW));
        right.add(new GuiColorProgressBar("alpha", unit.alpha, colorStorage, Color.WHITE));
        
        bag = ((ItemLittleBag) tool.get().getItem()).getInventory(tool.get());
        
        bagInventory = new SimpleContainer(LittleTiles.CONFIG.general.bag.inventorySize);
        left.add(bagInventoryGui = new GuiInventoryGrid(name, bagInventory, LittleTiles.CONFIG.general.bag.inventoryWidth, LittleTiles.CONFIG.general.bag.inventoryHeight, (c,
                i) -> new BagSlot(c, i)));
        
        add(addInventory(new GuiPlayerInventoryGrid(getPlayer())).disableSlot(tool.index));
        
        addInventory(inputInventory);
        addInventory(bagInventoryGui);
    }
    
    public void load() {
        bag = ((ItemLittleBag) tool.get().getItem()).getInventory(tool.get());
        ColorIngredient unit = bag.get(ColorIngredient.class);
        get("black", GuiColorProgressBar.class).pos = unit.black;
        get("cyan", GuiColorProgressBar.class).pos = unit.cyan;
        get("magenta", GuiColorProgressBar.class).pos = unit.magenta;
        get("yellow", GuiColorProgressBar.class).pos = unit.yellow;
        get("alpha", GuiColorProgressBar.class).pos = unit.alpha;
        clearItemCache();
    }
    
    public void clearItemCache() {
        for (int i = 0; i < bagInventoryGui.inventorySize(); i++)
            ((BagSlot) bagInventoryGui.getSlot(i).slot).resetCache();
    }
    
    @Override
    public boolean isExpandableX() {
        return false;
    }
    
    @Override
    public boolean isExpandableY() {
        return false;
    }
    
    public void saveBagInventory() {
        ((ItemLittleBag) tool.get().getItem()).setInventory(tool.get(), bag, null);
    }
    
    @Override
    public void closed() {
        super.closed();
        ItemStack stack = input.getItem(0);
        if (!stack.isEmpty())
            getPlayer().drop(stack, true);
    }
    
    @Override
    public boolean saveConfiguration(PatchedDataComponentMap data) {
        return false;
    }
    
    @Override
    public Iterable<IGuiInventory> inventoriesToInsert() {
        return inventories;
    }
    
    @Override
    public Iterable<IGuiInventory> inventoriesToExtract() {
        return inventoriesInv;
    }
    
    public class BagSlot extends Slot {
        
        private ItemStack cache;
        private boolean full;
        
        public BagSlot(Container container, int index) {
            super(container, index, 0, 0);
        }
        
        public void resetCache() {
            cache = null;
        }
        
        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
        
        public BlockIngredientEntry getEntry() {
            List<BlockIngredientEntry> entries = bag.get(BlockIngredient.class).getContent();
            if (entries.size() > getSlotIndex())
                return entries.get(getSlotIndex());
            return null;
        }
        
        @Override
        public ItemStack getItem() {
            if (cache == null) {
                BlockIngredientEntry entry = getEntry();
                if (entry == null || entry.isEmpty()) {
                    cache = ItemStack.EMPTY;
                    full = false;
                } else {
                    cache = ItemBlockIngredient.of(entry);
                    cache.setCount(Math.max(1, (int) entry.value));
                    full = entry.value >= 1;
                }
            }
            return cache;
        }
        
        @Override
        public ItemStack remove(int count) {
            BlockIngredientEntry entry = getEntry();
            if (entry == null)
                return ItemStack.EMPTY;
            
            ItemStack taken;
            
            if (full) {
                taken = entry.getBlockStack();
                taken.setCount(Math.min(count, (int) entry.value));
                entry.value -= taken.getCount();
            } else {
                taken = cache.copy();
                taken.set(LittleTilesRegistry.BLOCK_INGREDIENT_ENTRY, entry.copy());
                entry.value = 0;
            }
            
            if (entry.isEmpty())
                bag.get(BlockIngredient.class).getContent().remove(getSlotIndex());
            
            cache = null;
            saveBagInventory();
            clearItemCache();
            return taken;
        }
    }
    
}
