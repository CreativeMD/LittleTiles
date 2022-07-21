package team.creative.littletiles.common.gui.tool;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.inventory.GuiInventoryGrid;
import team.creative.creativecore.common.gui.controls.inventory.GuiPlayerInventoryGrid;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.mc.LevelUtils;
import team.creative.creativecore.common.util.type.Color;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.api.ingredient.ILittleIngredientInventory;
import team.creative.littletiles.common.gui.configure.GuiConfigure;
import team.creative.littletiles.common.gui.controls.GuiColorProgressBar;
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
    public LittleIngredients bag;
    public SimpleContainer input = new SimpleContainer(1);
    
    public final GuiSyncLocal<EndTag> RELOAD = getSyncHolder().register("reload", v -> {
        tool.changed();
        reinit();
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
                
                ((ItemLittleBag) tool.get().getItem()).setInventory(tool.get(), bag, null);
                RELOAD.send(EndTag.INSTANCE);
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
        
        /*registerEventChanged(x -> {
            Player player = getPlayer();
            if (x.control instanceof GuiSlotBase) {
                if (x.control instanceof SlotControlBlockIngredient) {
                    SlotControlBlockIngredient slot = (SlotControlBlockIngredient) event.source;
                    
                    BlockIngredient blocks = new BlockIngredient().setLimits(ItemLittleBag.inventorySize, ItemLittleBag.maxStackSize);
                    for (int y = 0; y < ItemLittleBag.inventoryHeight; y++) {
                        for (int x = 0; x < ItemLittleBag.inventoryWidth; x++) {
                            int index = x + y * ItemLittleBag.inventoryWidth;
                            BlockIngredientEntry ingredient = ((SlotControlBlockIngredient) get("item" + index)).getIngredient();
                            if (ingredient != null)
                                blocks.add(ingredient);
                        }
                    }
                    
                    bag.set(blocks.getClass(), blocks);
                    ((ItemLittleBag) stack.getItem()).setInventory(stack, bag, null);
                    if (player instanceof EntityPlayerMP)
                        ((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
                    reloadControls();
                } else if (event.source.name.startsWith("input")) {
                    
                }
            }
        });*/
    }
    
    @Override
    public void create() {
        RELOAD.send(EndTag.INSTANCE);
        
        flow = GuiFlow.STACK_Y;
        
        tool.get().getOrCreateTag();
        
        bag = ((ItemLittleBag) tool.get().getItem()).getInventory(tool.get());
        ColorIngredient unit = bag.get(ColorIngredient.class);
        
        GuiParent upper = new GuiParent(GuiFlow.STACK_X);
        add(upper.setExpandableX());
        GuiParent left = new GuiParent();
        upper.add(left);
        
        GuiParent right = new GuiParent(GuiFlow.STACK_Y);
        upper.add(right.setAlign(Align.STRETCH).setExpandableX());
        
        right.add(new GuiInventoryGrid("input", input).addListener(x -> {
            Player player = getPlayer();
            ItemStack input = GuiBag.this.input.getItem(0);
            
            if (input.getItem() instanceof ILittleIngredientInventory) {
                
                LittleIngredients ingredients = ((ILittleIngredientInventory) input.getItem()).getInventory(input);
                
                boolean containsBlocks = ingredients.contains(BlockIngredient.class);
                boolean containsColor = ingredients.contains(ColorIngredient.class);
                
                LittleIngredients remaining = bag.add(ingredients);
                ((ItemLittleBag) tool.get().getItem()).setInventory(tool.get(), bag, null);
                
                if (remaining == null)
                    remaining = new LittleIngredients();
                
                if (remaining.copy().sub(ingredients.copy()) != null) {
                    if (containsBlocks) {
                        updateSlots();
                        player.playSound(SoundEvents.ITEM_FRAME_PLACE, 1.0F, 1.0F);
                    }
                    
                    if (containsColor) {
                        player.playSound(SoundEvents.BREWING_STAND_BREW, 1.0F, 1.0F);
                        RELOAD.send(EndTag.INSTANCE);
                    }
                }
                
                ((ILittleIngredientInventory) input.getItem()).setInventory(input, remaining, null);
            } else {
                LittleIngredients ingredients = LittleIngredient.extractWithoutCount(input, true);
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
                            updateSlots();
                            player.playSound(SoundEvents.ITEM_FRAME_PLACE, 1.0F, 1.0F);
                        }
                        
                        if (containsColor) {
                            player.playSound(SoundEvents.BREWING_STAND_BREW, 1.0F, 1.0F);
                            RELOAD.send(EndTag.INSTANCE);
                        }
                        
                    } else
                        bag = ((ItemLittleBag) tool.get().getItem()).getInventory(tool.get());
                    
                }
            }
            
        }));
        right.add(new GuiColorProgressBar("black", unit.black, ItemLittleBag.colorUnitMaximum, Color.BLACK));
        right.add(new GuiColorProgressBar("cyan", unit.cyan, ItemLittleBag.colorUnitMaximum, Color.CYAN));
        right.add(new GuiColorProgressBar("magenta", unit.magenta, ItemLittleBag.colorUnitMaximum, Color.MAGENTA));
        right.add(new GuiColorProgressBar("yellow", unit.yellow, ItemLittleBag.colorUnitMaximum, Color.YELLOW));
        
        bag = ((ItemLittleBag) tool.get().getItem()).getInventory(tool.get());
        List<BlockIngredientEntry> inventory = bag.get(BlockIngredient.class).getContent();
        
        bagInventory = new SimpleContainer(ItemLittleBag.inventorySize) {
            @Override
            public int getMaxStackSize() {
                return ItemLittleBag.maxStackSizeOfTiles;
            }
        };
        left.add(new GuiInventoryGrid(name, bagInventory, ItemLittleBag.inventoryWidth, ItemLittleBag.inventoryHeight, (c, i) -> new Slot(c, i, 0, 0) {
            
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
            
        }).addListener(x -> {
            updateSlots();
        }));
        
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = new ItemStack(LittleTilesRegistry.BLOCK_INGREDIENT.get());
            stack.getOrCreateTag();
            ItemBlockIngredient.saveIngredient(stack, inventory.get(i));
            bagInventory.setItem(i, stack);
        }
        
        add(new GuiPlayerInventoryGrid(getPlayer()).disableSlot(tool.index));
    }
    
    public void updateSlots() {
        List<BlockIngredientEntry> inventory = bag.get(BlockIngredient.class).getContent();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = new ItemStack(LittleTilesRegistry.BLOCK_INGREDIENT.get());
            stack.getOrCreateTag();
            ItemBlockIngredient.saveIngredient(stack, inventory.get(i));
            bagInventory.setItem(i, stack);
        }
    }
    
    @Override
    public CompoundTag saveConfiguration(CompoundTag nbt) {
        ItemStack stack = input.getItem(0);
        if (!stack.isEmpty())
            getPlayer().drop(stack, true);
        return null;
    }
    
}
