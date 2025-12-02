package team.creative.littletiles.common.gui.structure;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import team.creative.creativecore.Side;
import team.creative.creativecore.common.config.converation.ConfigTypeConveration;
import team.creative.creativecore.common.config.gui.GuiConfigSubControlNested;
import team.creative.creativecore.common.config.holder.ConfigHolderObject;
import team.creative.creativecore.common.config.holder.CreativeConfigRegistry;
import team.creative.creativecore.common.config.holder.ICreativeConfigHolder;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.control.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.control.simple.GuiButton;
import team.creative.creativecore.common.gui.control.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.type.list.SortingList;
import team.creative.littletiles.common.structure.type.LittleItemHolder;

public class GuiItemHolder extends GuiLayer {
    
    public LittleItemHolder holder;
    
    public final GuiSyncLocal<CompoundTag> SAVE = getSyncHolder().register("save", x -> {
        holder.loadSettings(x, provider());
        holder.updateStructure();
        closeThisLayer();
    });
    
    public GuiCheckBox locked;
    public GuiConfigSubControlNested sortingControl;
    
    public GuiItemHolder(LittleItemHolder holder) {
        super("item_holder", 200, 230);
        this.holder = holder;
        flow = GuiFlow.STACK_Y;
    }
    
    @Override
    public void create() {
        add(locked = new GuiCheckBox("locked", holder.locked).setTranslate("gui.structure.locked"));
        SortingList sortingList = new SortingList(holder.whitelist);
        if (holder.filter != null)
            sortingList.entries.addAll(holder.filter);
        
        ICreativeConfigHolder holder = ConfigHolderObject.createUnrelated(CreativeConfigRegistry.ROOT, Side.SERVER, sortingList, new SortingList());
        add(sortingControl = new GuiConfigSubControlNested("filter", ConfigTypeConveration.FAKE_PARENT, null, Side.SERVER, null, true));
        sortingControl.load(holder, sortingList);
        sortingControl.createControls();
        
        GuiLeftRightBox bottom = new GuiLeftRightBox();
        add(bottom);
        bottom.addRight(new GuiButton("save", x -> {
            CompoundTag nbt = new CompoundTag();
            
            nbt.putBoolean("locked", locked.value);
            
            sortingControl.save();
            SortingList sorting = (SortingList) sortingControl.value;
            nbt.putBoolean("f_white", sorting.isWhitelist);
            if (!sorting.entries.isEmpty()) {
                ListTag list = new ListTag();
                for (int i = 0; i < sorting.entries.size(); i++)
                    list.add(sorting.entries.get(i).save(provider()));
                nbt.put("filter", list);
            }
            
            SAVE.send(nbt);
            closeThisLayer();
        }).setTranslate("gui.save"));
    }
    
}
