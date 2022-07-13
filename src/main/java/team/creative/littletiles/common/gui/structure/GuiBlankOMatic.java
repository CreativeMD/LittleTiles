package team.creative.littletiles.common.gui.structure;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.client.avatar.AvatarItemStack;
import com.creativemd.creativecore.common.gui.client.style.DisplayStyle;
import com.creativemd.creativecore.common.gui.controls.container.SlotControlNoSync;
import com.creativemd.creativecore.common.gui.controls.gui.GuiAvatarLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiProgressBar;
import com.creativemd.creativecore.common.gui.event.container.SlotChangeEvent;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.inventory.GuiSlot;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.style.display.DisplayColor;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.common.recipe.BlankOMaticRecipeRegistry;
import team.creative.littletiles.common.recipe.BlankOMaticRecipeRegistry.BleachRecipe;
import team.creative.littletiles.common.structure.type.premade.LittleBlankOMatic;

public class GuiBlankOMatic extends GuiLayer {
    
    private static final DisplayColor SELECTED_DISPLAY = new DisplayColor(ColorUtils.YELLOW);
    public LittleBlankOMatic whitener;
    public SimpleContainer whiteInput = new SimpleContainer(1);
    
    public GuiBlankOMatic(LittleBlankOMatic whitener) {
        super("whitener");
        this.whitener = whitener;
        updateVolume();
    }
    
    @Override
    public void create() {
        controls.add(new GuiVariantSelector("variant", 30, 8, 100, 100));
        controls.add(new GuiLabel("cost", 132, 30));
        GuiAvatarLabel label = new GuiAvatarLabel("", 148, 40, 0, null) {
            @Override
            public boolean mousePressed(int x, int y, int button) {
                return false;
            }
            
        };
        label.name = "avatar";
        label.height = 60;
        label.avatarSize = 32;
        controls.add(label);
        
        controls.add(new GuiButton("craft", 135, 10) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setBoolean("craft", true);
                nbt.setInteger("amount", GuiScreen.isShiftKeyDown() ? 2 : GuiScreen.isCtrlKeyDown() ? 1 : 0);
                GuiVariantSelector selector = (GuiVariantSelector) get("variant");
                nbt.setInteger("variant", selector.selected);
                sendPacketToServer(nbt);
            }
        });
        controls.add(new GuiProgressBar("volume", 8, 60, 80, 6, BlankOMaticRecipeRegistry.bleachTotalVolume, whitener.whiteColor));
        updateVariants();
        addSlotToContainer(new Slot(whitener.inventory, 0, 8, 10) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return !BlankOMaticRecipeRegistry.getRecipe(stack).isEmpty();
            }
        });
        addSlotToContainer(new Slot(whiteInput, 0, 8, 40) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return BlankOMaticRecipeRegistry.getVolume(stack) > 0;
            }
        });
        addPlayerSlotsToContainer(player);
    }
    
    @Override
    public void receiveContainerPacket(NBTTagCompound nbt) {
        GuiProgressBar volume = (GuiProgressBar) get("volume");
        volume.pos = nbt.getInteger("volume");
    }
    
    @CustomEventSubscribe
    public void changed(GuiControlChangedEvent event) {
        if (event.source.is("variant"))
            updateLabel();
    }
    
    public void updateLabel() {
        GuiVariantSelector selector = (GuiVariantSelector) get("variant");
        IBlockState state = selector.getSeleted();
        ItemStack stack;
        if (state != null)
            stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
        else
            stack = ItemStack.EMPTY;
        GuiAvatarLabel label = (GuiAvatarLabel) get("avatar");
        label.avatar = new AvatarItemStack(stack);
        GuiLabel cost = (GuiLabel) get("cost");
        BleachRecipe recipe = selector.getSelectedRecipe();
        cost.setCaption(recipe != null ? "cost: " + recipe.needed : "");
    }
    
    public void updateVariants() {
        GuiVariantSelector selector = (GuiVariantSelector) get("variant");
        selector.setRecipes(BlankOMaticRecipeRegistry.getRecipe(whitener.inventory.getStackInSlot(0)));
        updateLabel();
    }
    
    @CustomEventSubscribe
    public void slotChanged(SlotChangeEvent event) {
        int volume = BlankOMaticRecipeRegistry.getVolume(whiteInput.getStackInSlot(0));
        if (volume > 0) {
            ItemStack stack = whiteInput.getStackInSlot(0);
            boolean added = false;
            while (!stack.isEmpty() && volume + whitener.whiteColor <= BlankOMaticRecipeRegistry.bleachTotalVolume) {
                stack.shrink(1);
                whitener.whiteColor += volume;
                added = true;
            }
            if (added)
                player.playSound(SoundEvents.BREWING_STAND_BREW, 1.0F, 1.0F);
            updateVolume();
        }
        updateVariants();
    }
    
    public void updateVolume() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("volume", whitener.whiteColor);
        sendNBTToGui(nbt);
    }
    
    @Override
    public void onPacketReceive(NBTTagCompound nbt) {
        if (nbt.getBoolean("craft")) {
            int amount = nbt.getInteger("amount");
            ItemStack stack = whitener.inventory.getStackInSlot(0);
            int stackSize = 1;
            if (amount > 1)
                stackSize = stack.getCount();
            List<BleachRecipe> recipes = BlankOMaticRecipeRegistry.getRecipe(stack);
            if (!recipes.isEmpty()) {
                int index = 0;
                int variant = nbt.getInteger("variant");
                BleachRecipe selected = null;
                IBlockState state = null;
                for (BleachRecipe recipe : recipes) {
                    if (variant >= index + recipe.results.length)
                        index += recipe.results.length;
                    else {
                        selected = recipe;
                        state = recipe.results[variant - index];
                        break;
                    }
                }
                if (selected == null)
                    return;
                boolean result = !selected.isResult(stack);
                if (result && selected.needed > 0)
                    stackSize = Math.min(stackSize, whitener.whiteColor / selected.needed);
                ItemStack newStack = new ItemStack(state.getBlock(), stackSize, state.getBlock().getMetaFromState(state));
                stack.shrink(stackSize);
                if (!player.addItemStackToInventory(newStack))
                    player.dropItem(newStack, false);
                if (result && selected.needed > 0)
                    whitener.whiteColor -= stackSize * selected.needed;
                updateVolume();
            }
        }
    }
    
    public static class GuiVariantSelector extends GuiParent {
        
        public List<Pair<BleachRecipe, BlockState>> states;
        private int selected = 0;
        
        public GuiVariantSelector(String name, int x, int y, int width, int height) {
            super(name, x, y, width, height);
        }
        
        @Override
        public boolean hasBackground() {
            return false;
        }
        
        @Override
        public boolean hasBorder() {
            return false;
        }
        
        public BleachRecipe getSelectedRecipe() {
            if (selected >= 0 && selected < states.size())
                return states.get(selected).key;
            return null;
        }
        
        public BlockState getSeleted() {
            if (selected >= 0 && selected < states.size())
                return states.get(selected).value;
            return null;
        }
        
        public void select(int index) {
            this.selected = index;
            for (int i = 0; i < controls.size(); i++) {
                GuiSlotControlSelect slot = (GuiSlotControlSelect) controls.get(i);
                slot.selected = index == i;
            }
            raiseEvent(new GuiControlChangedEvent(this));
        }
        
        public void setRecipes(List<BleachRecipe> recipes) {
            List<Pair<BleachRecipe, BlockState>> states = new ArrayList<>();
            for (int i = 0; i < recipes.size(); i++) {
                BleachRecipe recipe = recipes.get(i);
                for (int j = 0; j < recipe.results.length; j++)
                    states.add(new Pair<>(recipe, recipe.results[j]));
            }
            controls.clear();
            this.states = states;
            InventoryBasic basic = new InventoryBasic("", false, states.size());
            for (int i = 0; i < states.size(); i++) {
                IBlockState state = states.get(i).value;
                final int index = i;
                int col = i % 4;
                int row = i / 4;
                basic.setInventorySlotContents(i, new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)));
                addControl(new GuiSlotControlSelect(this, col, row, index, basic).setTooltip("cost: " + states.get(i).key.needed));
            }
            if (selected >= states.size())
                select(0);
            else
                select(selected);
        }
        
    }
    
    public static class GuiSlotControlSelect extends GuiSlot {
        
        public final GuiVariantSelector selector;
        public final int index;
        public boolean selected = false;
        
        public GuiSlotControlSelect(GuiVariantSelector selector, int col, int row, int index, InventoryBasic basic) {
            super(col * 18, row * 18, new SlotControlNoSync(new Slot(basic, index, col * 18, row * 18)));
            this.selector = selector;
            this.index = index;
        }
        
        @Override
        public DisplayStyle getBorderDisplay(DisplayStyle display) {
            if (selected)
                return SELECTED_DISPLAY;
            return super.getBorderDisplay(display);
        }
        
        @Override
        public boolean mousePressed(int posX, int posY, int button) {
            selector.select(index);
            playSound(SoundEvents.UI_BUTTON_CLICK);
            return super.mousePressed(posX, posY, button);
        }
        
    }
    
}
