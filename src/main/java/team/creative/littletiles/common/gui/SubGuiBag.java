package team.creative.littletiles.common.gui;

import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.mc.LevelUtils;
import team.creative.creativecore.common.util.type.Color;
import team.creative.littletiles.common.gui.controls.GuiColorProgressBar;
import team.creative.littletiles.common.ingredient.ColorIngredient;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.ingredient.LittleInventory;
import team.creative.littletiles.common.item.ItemColorIngredient;
import team.creative.littletiles.common.item.ItemColorIngredient.ColorIngredientType;
import team.creative.littletiles.common.item.ItemLittleBag;

public class SubGuiBag extends GuiLayer {
    
    public ContainerSlotView item;
    public LittleIngredients bag;
    
    public final GuiSyncLocal<StringTag> DROP_COLOR = getSyncHolder().register("drop_color", nbt -> {
        ColorIngredientType type = ColorIngredientType.getType(nbt.getAsString());
        ColorIngredient color = bag.get(ColorIngredient.class);
        if (color != null && !color.isEmpty()) {
            int amount = Math.min(type.getIngredient(color), ColorIngredient.bottleSize);
            if (amount > 0) {
                type.setIngredient(color, type.getIngredient(color) - amount);
                
                Player player = getPlayer();
                LittleInventory inventory = new LittleInventory(player);
                ItemStack colorStack = ItemColorIngredient.generateItemStack(type, amount);
                if (!inventory.addStack(colorStack))
                    LevelUtils.dropItem(player, colorStack);
                
                ((ItemLittleBag) stack.getItem()).setInventory(stack, bag, null);
                if (player instanceof ServerPlayer)
                    ((ServerPlayer) player).sendContainerToPlayer(player.getInventory());
                tick();
            }
        }
    });
    
    public final GuiSyncLocal<EndTag> RELOAD = getSyncHolder().register("reload", v -> {
        stack.setTagCompound(nbt);
        reinit();
    });
    
    public SubGuiBag(ContainerSlotView view) {
        super("bag");
        this.item = view;
        registerEventClick(x -> {
            if (x.control instanceof GuiColorProgressBar)
                DROP_COLOR.send(StringTag.valueOf(x.control.name));
        });
    }
    
    @Override
    public void create() {
        item.get().getOrCreateTag();
        
        bag = ((ItemLittleBag) item.get().getItem()).getInventory(item.get());
        ColorIngredient unit = bag.get(ColorIngredient.class);
        
        add(new GuiColorProgressBar("black", ItemLittleBag.colorUnitMaximum, unit.black, Color.BLACK));
        add(new GuiColorProgressBar("cyan", ItemLittleBag.colorUnitMaximum, unit.cyan, Color.CYAN));
        add(new GuiColorProgressBar("magenta", ItemLittleBag.colorUnitMaximum, unit.magenta, Color.MAGENTA));
        add(new GuiColorProgressBar("yellow", ItemLittleBag.colorUnitMaximum, unit.yellow, Color.YELLOW));
    }
    
}
