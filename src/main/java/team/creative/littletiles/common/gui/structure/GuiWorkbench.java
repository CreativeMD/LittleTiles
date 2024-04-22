package team.creative.littletiles.common.gui.structure;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.EndTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.controls.collection.GuiListBoxBase;
import team.creative.creativecore.common.gui.controls.inventory.GuiPlayerInventoryGrid;
import team.creative.creativecore.common.gui.controls.inventory.GuiSlot;
import team.creative.creativecore.common.gui.controls.inventory.IGuiInventory;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.mc.PlayerUtils;
import team.creative.creativecore.common.util.text.content.ContentItemStack;
import team.creative.littletiles.api.common.tool.ILittlePlacer;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.ingredient.BlockIngredient;
import team.creative.littletiles.common.ingredient.BlockIngredientEntry;
import team.creative.littletiles.common.ingredient.ColorIngredient;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.ingredient.LittleInventory;
import team.creative.littletiles.common.ingredient.NotEnoughIngredientsException;
import team.creative.littletiles.common.ingredient.StackIngredient;
import team.creative.littletiles.common.ingredient.StackIngredientEntry;
import team.creative.littletiles.common.item.ItemLittleBlueprint;
import team.creative.littletiles.common.item.ItemMultiTiles;
import team.creative.littletiles.common.mod.chiselsandbits.ChiselsAndBitsManager;
import team.creative.littletiles.common.placement.PlacementHelper;

public class GuiWorkbench extends GuiLayer {
    
    public SimpleContainer crafting = new SimpleContainer(2);
    public final GuiSyncLocal<EndTag> CRAFT = getSyncHolder().register("craft", x -> {
        ItemStack input1 = crafting.getItem(0);
        ItemStack input2 = crafting.getItem(1);
        
        GuiListBoxBase<GuiLabel> listBox = get("missing");
        listBox.clearItems();
        
        if (!input1.isEmpty()) {
            if (input1.getItem() instanceof ItemLittleBlueprint item && input2.isEmpty()) {
                if (!item.hasTiles(input1))
                    return;
                LittleGroup group = item.getTiles(input1);
                
                Player player = getPlayer();
                LittleInventory inventory = new LittleInventory(player);
                LittleIngredients ingredients = LittleAction.getIngredients(group);
                
                try {
                    if (LittleAction.checkAndTake(player, inventory, ingredients)) {
                        ItemStack stack = ItemMultiTiles.of(group);
                        if (!player.getInventory().add(stack))
                            player.drop(stack, false);
                    }
                } catch (NotEnoughIngredientsException e2) {
                    LittleIngredients missing = e2.getIngredients();
                    
                    BlockIngredient blocks = missing.get(BlockIngredient.class);
                    if (blocks != null)
                        for (BlockIngredientEntry ingredient : blocks)
                            listBox.addItem(new GuiLabel("").setTitle(MutableComponent.create(new ContentItemStack(ingredient.getBlockStack())).append(BlockIngredient.printVolume(
                                ingredient.value, true))));
                        
                    ColorIngredient color = missing.get(ColorIngredient.class);
                    if (color != null) {
                        if (color.black > 0)
                            listBox.addItem(new GuiLabel("").setTitle(Component.literal(color.getBlackDescription())));
                        if (color.cyan > 0)
                            listBox.addItem(new GuiLabel("").setTitle(Component.literal(color.getCyanDescription())));
                        if (color.magenta > 0)
                            listBox.addItem(new GuiLabel("").setTitle(Component.literal(color.getMagentaDescription())));
                        if (color.yellow > 0)
                            listBox.addItem(new GuiLabel("").setTitle(Component.literal(color.getYellowDescription())));
                    }
                    
                    StackIngredient stacks = missing.get(StackIngredient.class);
                    if (stacks != null)
                        for (StackIngredientEntry stack : stacks)
                            listBox.addItem(new GuiLabel("").setTitle(MutableComponent.create(new ContentItemStack(stack.stack)).append("" + stack.count)));
                }
                
            } else if (ChiselsAndBitsManager.isChiselsAndBitsStructure(input1)) {
                LittleGroup group = ChiselsAndBitsManager.getGroup(input1);
                if (group != null && !group.isEmpty() && input2.isEmpty()) {
                    crafting.setItem(0, ItemStack.EMPTY);
                    crafting.setItem(1, ItemMultiTiles.of(group));
                }
            } else {
                ILittlePlacer tile = PlacementHelper.getLittleInterface(input1);
                if (tile != null && !input2.isEmpty() && (input2.getItem() instanceof ItemLittleBlueprint))
                    input2.setTag(input1.getOrCreateTag().copy());
            }
        }
    });
    
    public GuiWorkbench() {
        super("workbench");
    }
    
    @Override
    public void create() {
        flow = GuiFlow.STACK_Y;
        align = Align.CENTER;
        add(new CraftingGrid());
        add(new GuiListBoxBase<GuiLabel>("missing", false, new ArrayList<>()).setDim(150, 100));
        add(new GuiPlayerInventoryGrid(getPlayer()).setUnexpandableX());
    }
    
    @Override
    public void closed() {
        super.closed();
        PlayerUtils.addOrDrop(getPlayer(), crafting);
    }
    
    public class CraftingGrid extends GuiParent implements IGuiInventory {
        
        private List<GuiSlot> slots = new ArrayList<>();
        
        public CraftingGrid() {
            super("crafting", GuiFlow.STACK_X);
            setVAlign(VAlign.CENTER);
            addSlot(new GuiSlot(crafting, 0));
            add(new GuiLabel("->").setTitle(Component.literal("->")));
            addSlot(new GuiSlot(crafting, 1));
            add(new GuiButton("craft", x -> CRAFT.sendAndExecute(GuiWorkbench.this, EndTag.INSTANCE)).setTranslate("gui.craft"));
        }
        
        public GuiChildControl addSlot(GuiSlot slot) {
            slots.add(slot);
            return addControl(slot);
        }
        
        @Override
        public GuiSlot getSlot(int index) {
            return slots.get(index);
        }
        
        @Override
        public int inventorySize() {
            return slots.size();
        }
        
        @Override
        public String name() {
            return name;
        }
        
        @Override
        public void setChanged() {}
        
        @Override
        public void setChanged(int slotIndex) {}
        
    }
}
