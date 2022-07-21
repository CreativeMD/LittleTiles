package team.creative.littletiles.common.gui.structure;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBoxMapped;
import team.creative.creativecore.common.gui.controls.collection.GuiStackSelector;
import team.creative.creativecore.common.gui.controls.inventory.GuiInventoryGrid;
import team.creative.creativecore.common.gui.controls.inventory.GuiPlayerInventoryGrid;
import team.creative.creativecore.common.gui.controls.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiCounter;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButtonMapped;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.LittleGuiUtils;
import team.creative.littletiles.common.item.ItemLittleBlueprint;
import team.creative.littletiles.common.structure.type.premade.LittleStructureBuilder;
import team.creative.littletiles.common.structure.type.premade.LittleStructureBuilder.LittleStructureBuilderType;

public class GuiBuilder extends GuiLayer {
    
    public LittleStructureBuilder builder;
    public final GuiSyncLocal<CompoundTag> CRAFT = getSyncHolder().register("craft", nbt -> {
        if ((getPlayer().isCreative() && builder.inventory.getItem(0).isEmpty()) || builder.inventory.getItem(0).getItem() instanceof ItemLittleBlueprint) {
            int width = nbt.getInt("width");
            int height = nbt.getInt("height");
            int thickness = nbt.getInt("thickness");
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(nbt.getString("block")));
            LittleGrid grid = LittleGrid.get(nbt.getInt("grid"));
            
            builder.lastBlockState = block.defaultBlockState();
            builder.lastSizeX = width;
            builder.lastSizeY = height;
            builder.lastThickness = thickness;
            builder.lastGrid = grid.count;
            builder.lastStructureType = nbt.getString("type");
            builder.updateStructure();
            
            LittleStructureBuilderType type = LittleStructureBuilder.REGISTRY.get(builder.lastStructureType);
            if (type != null) {
                ItemStack stack = builder.inventory.getItem(0);
                if (stack.isEmpty()) {
                    stack = new ItemStack(LittleTilesRegistry.BLUEPRINT.get());
                    builder.inventory.setItem(0, stack);
                }
                stack.setTag(LittleGroup.save(type.construct(grid, width, height, thickness, block.defaultBlockState())));
            }
        }
    });
    
    public GuiBuilder(LittleStructureBuilder builder) {
        super("structure_builder");
        this.builder = builder;
    }
    
    @Override
    public void create() {
        flow = GuiFlow.STACK_Y;
        GuiParent config = new GuiParent(GuiFlow.STACK_X);
        add(config.setExpandableX());
        config.add(new GuiLabel("widthLabel").setTranslate("gui.frame_builder.width"));
        config.add(new GuiLabel("heightLabel").setTranslate("gui.frame_builder.height"));
        config.add(new GuiLabel("thicknessLabel").setTranslate("gui.frame_builder.thickness"));
        config.add(new GuiCounter("width", builder.lastSizeX, 1, Integer.MAX_VALUE));
        config.add(new GuiCounter("height", builder.lastSizeY, 1, Integer.MAX_VALUE));
        config.add(new GuiCounter("thickness", builder.lastThickness, 1, Integer.MAX_VALUE));
        
        GuiComboBoxMapped<LittleStructureBuilderType> box = new GuiComboBoxMapped<>("type", new TextMapBuilder<LittleStructureBuilderType>()
                .addEntrySet(LittleStructureBuilder.REGISTRY.entrySet(), x -> new TranslatableComponent("structure." + x.getKey() + ".name")));
        box.select(LittleStructureBuilder.REGISTRY.get(builder.lastStructureType));
        add(box.setExpandableX());
        
        add(new GuiStateButtonMapped<LittleGrid>("grid", LittleGrid.mapBuilder()));
        GuiStackSelector selector = new GuiStackSelector("preview", getPlayer(), LittleGuiUtils.getCollector(getPlayer()), true);
        selector.setSelectedForce(new ItemStack(builder.lastBlockState.getBlock()));
        add(selector.setExpandableX());
        
        add(new GuiLeftRightBox().addLeft(new GuiLabel("failed").setTranslate("gui.frame_builder.failed").setVisible(false)).addRight(new GuiButton("craft", x -> {
            if ((getPlayer().isCreative() && builder.inventory.getItem(0).isEmpty()) || builder.inventory.getItem(0).getItem() instanceof ItemLittleBlueprint) {
                get("failed").visible = false;
                CompoundTag nbt = new CompoundTag();
                GuiCounter width = (GuiCounter) get("width");
                nbt.putInt("width", width.getValue());
                GuiCounter height = (GuiCounter) get("height");
                nbt.putInt("height", height.getValue());
                GuiCounter thickness = (GuiCounter) get("thickness");
                nbt.putInt("thickness", thickness.getValue());
                GuiComboBoxMapped<LittleStructureBuilderType> type = (GuiComboBoxMapped<LittleStructureBuilderType>) get("type");
                nbt.putString("type", type.getSelected().type.id);
                GuiStateButtonMapped<LittleGrid> gridButton = (GuiStateButtonMapped<LittleGrid>) get("grid");
                LittleGrid grid = gridButton.getSelected();
                nbt.putInt("grid", grid.count);
                ItemStack stack = selector.getSelected();
                Block block = Block.byItem(stack.getItem());
                nbt.putString("block", block.getRegistryName().toString());
                CRAFT.send(nbt);
            } else
                get("failed").visible = true;
            
        }).setTranslate("gui.frame_builder.craft")));
        
        add(new GuiInventoryGrid("builder", builder.inventory));
        add(new GuiPlayerInventoryGrid(getPlayer()));
    }
    
}
