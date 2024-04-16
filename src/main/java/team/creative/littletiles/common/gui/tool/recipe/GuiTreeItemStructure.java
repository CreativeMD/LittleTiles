package team.creative.littletiles.common.gui.tool.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.controls.parent.GuiScrollY;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.controls.tree.GuiTree;
import team.creative.creativecore.common.gui.controls.tree.GuiTreeItem;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.util.mc.LanguageUtils;
import team.creative.littletiles.LittleTilesGuiRegistry;
import team.creative.littletiles.client.level.little.FakeClientLevel;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.gui.AnimationPreview;
import team.creative.littletiles.common.gui.signal.GuiComponentSearch;
import team.creative.littletiles.common.gui.signal.GuiSignalComponent;
import team.creative.littletiles.common.gui.signal.dialog.GuiDialogSignalEvents.GuiSignalEvent;
import team.creative.littletiles.common.gui.tool.recipe.test.RecipeTestError;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.animation.PhysicalState;
import team.creative.littletiles.common.structure.animation.context.AnimationContext;
import team.creative.littletiles.common.structure.registry.gui.LittleStructureGui;
import team.creative.littletiles.common.structure.registry.gui.LittleStructureGuiRegistry;
import team.creative.littletiles.common.structure.relative.StructureAbsolute;
import team.creative.littletiles.common.structure.signal.output.InternalSignalOutput;
import team.creative.littletiles.common.structure.signal.output.SignalExternalOutputHandler;
import team.creative.littletiles.server.level.little.FakeServerLevel;

public class GuiTreeItemStructure extends GuiTreeItem implements AnimationContext {
    
    public final GuiRecipe recipe;
    public LittleGroup group;
    public LittleStructure structure;
    public LittleStructureGui gui;
    public GuiComponentSearch signalSearch = new GuiComponentSearch(this);
    
    private GuiSignalEvent[] internalOutputs;
    private HashMap<Integer, GuiSignalEvent> externalOutputs;
    private LittleVecGrid offset;
    private int index;
    private String title;
    private List<RecipeTestError> errors;
    
    public PhysicalState physicalState = new PhysicalState();
    private StructureAbsolute center;
    
    public GuiTreeItemStructure(GuiRecipe recipe, GuiTree tree, LittleGroup group, int index) {
        super("tree_item", tree);
        this.recipe = recipe;
        this.group = group;
        if (group.hasStructure()) {
            this.structure = group.getStructureType().createStructure(null);
            this.structure.load(group.getStructureTag());
        }
        this.index = index;
        refreshAnimation();
        updateTitle();
        updateSignalOutputs();
    }
    
    private void updateSignalOutputs() {
        GuiSignalComponent[] internal = signalSearch.internalOutputs();
        if (internal != null) {
            this.internalOutputs = new GuiSignalEvent[internal.length];
            for (int i = 0; i < internal.length; i++) {
                if (structure == null)
                    this.internalOutputs[i] = new GuiSignalEvent(internal[i], (InternalSignalOutput) null);
                else
                    this.internalOutputs[i] = new GuiSignalEvent(internal[i], structure.getOutput(i));
            }
        } else
            this.internalOutputs = null;
        
        externalOutputs = new HashMap<>();
        for (GuiSignalComponent output : signalSearch.externalOutputs())
            if (structure == null)
                externalOutputs.put(output.index(), new GuiSignalEvent(output, (InternalSignalOutput) null));
            else if (output.external())
                externalOutputs.put(output.index(), new GuiSignalEvent(output, structure.getExternalOutput(output.index())));
    }
    
    public LittleStructureType getStructureType() {
        if (gui != null)
            return gui.type();
        if (structure != null)
            return structure.type;
        return null;
    }
    
    public GuiSignalEvent[] internalOutputs() {
        return internalOutputs;
    }
    
    public int externalOutputCount() {
        return externalOutputs.size();
    }
    
    public Iterable<GuiSignalEvent> externalOutputs() {
        return externalOutputs.values();
    }
    
    public GuiSignalEvent getInternalOutput(int index) {
        if (internalOutputs != null && index >= 0 && index < internalOutputs.length)
            return internalOutputs[index];
        return null;
    }
    
    public GuiSignalEvent getExternalOutput(int index) {
        return externalOutputs.get(index);
    }
    
    public void setInternalOutput(int index, GuiSignalEvent event) {
        if (internalOutputs != null && index >= 0 && index < internalOutputs.length)
            internalOutputs[index] = event;
    }
    
    public void setExternalOutput(int index, GuiSignalEvent event) {
        externalOutputs.put(index, event);
    }
    
    @Nullable
    public GuiSignalEvent getSignalOutput(boolean external, int index) {
        if (external)
            return getExternalOutput(index);
        return getInternalOutput(index);
    }
    
    @Nullable
    public void setSignalOutput(boolean external, int index, GuiSignalEvent event) {
        if (external)
            setExternalOutput(index, event);
        else
            setInternalOutput(index, event);
    }
    
    private void setEventsToStructure() {
        if (structure == null)
            return;
        
        for (int i = 0; i < internalOutputs.length; i++) {
            GuiSignalEvent event = internalOutputs[i];
            InternalSignalOutput output = structure.getOutput(i);
            output.condition = event.condition;
            output.handler = event.getHandler(output, structure);
        }
        
        HashMap<Integer, SignalExternalOutputHandler> map = new HashMap<>();
        for (GuiSignalEvent event : externalOutputs.values())
            if (event.condition != null)
                map.put(event.component.index(), new SignalExternalOutputHandler(null, event.component.index(), event.condition, (x) -> event.getHandler(x, structure)));
        structure.setExternalOutputs(map);
    }
    
    @Override
    protected void select() {
        super.select();
        updateTitle();
        recipe.types.forceSelect(LittleStructureGuiRegistry.get(structure != null ? structure.type : null, group));
    }
    
    public void onNameChanged(GuiTextfield field) {
        String text = field.getText();
        if (text.isBlank())
            text = null;
        updateTitle(text, gui.type());
        tree.reflowTree();
    }
    
    public void load() {
        recipe.animation.reset();
        gui = recipe.types.getSelected();
        recipe.control = gui.create(this);
        recipe.control.setExpandableY();
        recipe.config.clear();
        GuiScrollY scroll = new GuiScrollY("config") {
            
            @Override
            public ControlFormatting getControlFormatting() {
                return ControlFormatting.TRANSPARENT;
            }
            
        }.setHover(true);
        scroll.add(recipe.control);
        recipe.config.add(scroll);
        recipe.control.create(structure);
        recipe.config.init();
        recipe.types.setEnabled(recipe.control.canChangeType());
        
        GuiParent info = new GuiParent("infoStructure", GuiFlow.STACK_X).setVAlign(VAlign.CENTER);
        recipe.config.add(info);
        
        info.add(new GuiLabel("info").setTitle(Component.literal(group.totalTiles() + " " + LanguageUtils.translate("gui.tile.count") + " " + group
                .totalBoxes() + " " + LanguageUtils.translate("gui.box.count"))));
        
        GuiParent parent = new GuiParent("bottomStructure", GuiFlow.STACK_X).setVAlign(VAlign.CENTER);
        recipe.config.add(parent);
        
        parent.add(new GuiLabel("name_label").setTranslate("gui.recipe.structure.name"));
        GuiTextfield text = new GuiTextfield("name");
        if (structure != null && structure.name != null)
            text.setText(structure.name);
        else
            text.setText("");
        parent.add(text.setEnabled(gui.supportsName()).setDim(100, 7));
        parent.add(new GuiButton("signal", x -> LittleTilesGuiRegistry.SIGNAL_EVENTS_DIALOG.open(getIntegratedParent(), new CompoundTag()).init(this)).setTranslate(
            "gui.signal.events").setEnabled(gui.type() != null));
        updateSignalOutputs();
        
        onNameChanged(text);
        recipe.reflow();
    }
    
    public void save() {
        LittleStructureType type = recipe.types.getSelected().type();
        structure = recipe.control.save(type != null ? type.createStructure(null) : null);
        if (structure != null) {
            GuiParent parent = recipe.config.get("bottomStructure");
            GuiTextfield textfield = parent.get("name");
            structure.name = textfield.getText().isBlank() ? null : textfield.getText();
        }
        setEventsToStructure();
        updateTitle();
    }
    
    @Override
    protected void deselect() {
        super.deselect();
        updateTitle();
        save();
    }
    
    public void updateTitle() {
        updateTitle(structure != null ? structure.name : null, structure != null ? structure.type : null);
    }
    
    protected void updateTitle(String name, LittleStructureType type) {
        int index = getParentItem() != null ? getParentItem().indexOf(this) : this.index;
        boolean hasStructureName = true;
        if (name == null) {
            hasStructureName = false;
            if (type != null)
                name = type.id + " " + index;
            else
                name = "none " + index;
        }
        
        if (hasStructureName)
            name = ChatFormatting.ITALIC + "" + name;
        
        this.title = name;
        
        if (selected())
            name = "<" + name + ">";
        
        if (errors != null && !errors.isEmpty())
            name = ChatFormatting.RED + name;
        
        setTitle(Component.literal(name));
    }
    
    public void updateTooltip() {
        if (errors == null || errors.isEmpty()) {
            setTooltip((List) null);
            return;
        }
        
        List<Component> tooltip = new ArrayList<>();
        if (errors.size() == 1)
            tooltip.add(translatable("gui.recipe.test.error.single"));
        else
            tooltip.add(translatable("gui.recipe.test.error.multiple", errors.size()));
        
        for (RecipeTestError error : errors)
            tooltip.add(error.tooltip(this));
        
        setTooltip(tooltip);
    }
    
    public void clearErrors() {
        if (errors != null)
            errors.clear();
    }
    
    public void addError(RecipeTestError error) {
        if (errors == null)
            errors = new ArrayList<>();
        errors.add(error);
    }
    
    public String getTitle() {
        return title;
    }
    
    @OnlyIn(Dist.CLIENT)
    public void refreshAnimation() {
        FakeClientLevel fakeLevel = FakeServerLevel.createClient("animationViewer");
        CompletableFuture.supplyAsync(() -> {
            try {
                return new AnimationPreview(fakeLevel, structure, group);
            } catch (LittleActionException e) {
                throw new RuntimeException(e);
            }
        }).whenComplete((preview, throwable) -> {
            recipe.storage.completed(this, preview);
            if (throwable != null)
                throwable.printStackTrace();
        });
    }
    
    @Override
    public void removed() {
        super.removed();
        if (!moving)
            recipe.storage.removed(this);
    }
    
    public GuiTreeItemStructure duplicate() {
        CompoundTag nbt;
        if (structure == null)
            nbt = null;
        else {
            nbt = new CompoundTag();
            structure.save(nbt);
        }
        GuiTreeItemStructure item = new GuiTreeItemStructure(recipe, tree, new LittleGroup(nbt, group.copy(), Collections.EMPTY_LIST), getParentItem().itemsCount());
        for (GuiTreeItem child : items())
            if (child instanceof GuiTreeItemStructure s)
                item.addItem(s.duplicate());
        return item;
    }
    
    public void setOffset(LittleVecGrid vec) {
        this.offset = vec;
    }
    
    public LittleVecGrid getOffset() {
        return offset;
    }
    
    public void move(LittleVecGrid vec) {
        group.move(vec);
        refreshAnimation();
        
        for (GuiTreeItem item : items())
            ((GuiTreeItemStructure) item).move(vec);
    }
    
    public void applyOffset() {
        if (offset != null)
            move(offset);
        offset = null;
    }
    
    public void resetOffset() {
        offset = null;
    }
    
    @OnlyIn(Dist.CLIENT)
    public void prepareRendering(AnimationPreview preview) {
        preview.set(physicalState);
        if (hasNewCenter()) {
            preview.setCenter(center);
            center = null;
        }
    }
    
    public void setNewCenter(StructureAbsolute center) {
        this.center = center;
    }
    
    public StructureAbsolute getCenter() {
        return center;
    }
    
    public boolean hasNewCenter() {
        return center != null;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void play(SoundEvent event, float volume, float pitch) {
        GuiControl.playSound(event, volume, pitch);
    }
    
    @Override
    public boolean isGui() {
        return true;
    }
    
    @Override
    public AnimationContext getChild(int id) {
        if (id < 0 || itemsCount() <= id)
            return null;
        return (AnimationContext) getItem(id);
    }
    
    @Override
    public LittleStructure getChildStructure(int id) {
        if (id < 0 || itemsCount() <= id)
            return null;
        return ((GuiTreeItemStructure) getItem(id)).structure;
    }
}
