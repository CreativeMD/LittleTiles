package team.creative.littletiles.client.tool.mode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.settings.KeyModifier;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.config.premade.KeyConfig;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.control.menu.GuiMenu;
import team.creative.creativecore.common.gui.control.menu.GuiMenuRoot;
import team.creative.creativecore.common.gui.control.simple.GuiLabel;
import team.creative.creativecore.common.gui.extension.GuiExtensionCreator;
import team.creative.creativecore.common.gui.extension.GuiExtensionCreator.ExtensionDirection;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.tree.NamedTree;
import team.creative.littletiles.client.render.overlay.OverlayRenderer.OverlayGuiLayer;
import team.creative.littletiles.client.tool.LittleTool;
import team.creative.littletiles.client.tool.mode.BuildingModeTopBar.BuildingModeInfo;

public class BuildingModeRules extends BuildingModeFeature implements BuildingModeInfo {
    
    private static final Minecraft MC = Minecraft.getInstance();
    private GuiLabel label;
    protected GuiExtensionCreator<GuiLabel, GuiMenuRoot<BooleanSupplier>> ex;
    
    private NamedTree<BooleanSupplier> data;
    private List<BuildingModeRule> rules;
    private String selected;
    
    @CreativeConfig
    public KeyConfig key = new KeyConfig(InputConstants.KEY_R, KeyModifier.NONE);
    
    @Override
    public void createInfo(GuiParent parent) {
        parent.add(label = new GuiLabel("rules").setTitle(Component.translatable("building.rules").append(" (").append(key.getTranslatedKeyMessage()).append(")")));
        ex = new GuiExtensionCreator<GuiLabel, GuiMenuRoot<BooleanSupplier>>(label);
    }
    
    @Override
    public void create(OverlayGuiLayer gui, LittleTool tool, List<BuildingModeFeature> allFeatures) {
        rules = new ArrayList<>();
        for (BuildingModeFeature feature : allFeatures)
            if (feature instanceof BuildingModeRule r)
                rules.add(r);
    }
    
    @Override
    public void remove(OverlayGuiLayer gui) {
        close();
        ex = null;
        label = null;
        data = null;
    }
    
    protected GuiMenuRoot<BooleanSupplier> createBox(GuiExtensionCreator<GuiLabel, GuiMenuRoot<BooleanSupplier>> creator) {
        return new GuiMenuRoot<BooleanSupplier>(data, creator, x -> Component.translatable("building." + x), (key, supplier) -> {
            if (supplier.getAsBoolean())
                close();
        });
    }
    
    public void close() {
        selected = null;
        ex.close();
    }
    
    public void out() {
        var parent = data.folder(selected);
        if (parent == null) {
            if (selected.isBlank())
                selected = data.firstKey();
            else {
                String[] path = selected.split("\\.");
                selected = String.join(".", Arrays.copyOf(path, path.length - 1));
                out();
                
            }
            updateSelection();
            return;
        }
        if (parent.parent() != null) {
            parent = parent.parent();
            var entry = ex.get().getEntry(parent.path());
            if (entry == null)
                return;
            entry.close();
            selected = parent.path();
            updateSelection();
        }
    }
    
    public void in() {
        var parent = data.folder(selected);
        if (parent == null)
            return;
        if (parent.hasChildren()) {
            var entry = ex.get().getEntry(selected);
            if (entry == null)
                return;
            
            entry.open();
            selected += "." + parent.firstKey();
            updateSelection();
        }
    }
    
    public void previous() {
        var folder = data.folder(selected);
        if (folder == null) {
            out();
            return;
        }
        var parent = folder.parent();
        String subPath = folder.name();
        String previous = null;
        for (Entry<String, NamedTree<BooleanSupplier>> entry : parent.entries()) {
            if (entry.getKey().equals(subPath) && previous != null) {
                selected = parent.path().isBlank() ? previous : parent.path() + "." + previous;
                updateSelection();
                return;
            }
            previous = entry.getKey();
        }
        if (previous != null) {
            selected = parent.path().isBlank() ? previous : parent.path() + "." + previous;
            updateSelection();
        }
    }
    
    public void next() {
        var folder = data.folder(selected);
        if (folder == null) {
            out();
            return;
        }
        var parent = folder.parent();
        String subPath = folder.name();
        boolean found = false;
        for (Entry<String, NamedTree<BooleanSupplier>> entry : parent.entries()) {
            if (found) {
                selected = parent.path().isBlank() ? entry.getKey() : parent.path() + "." + entry.getKey();
                updateSelection();
                return;
            }
            if (entry.getKey().equals(subPath))
                found = true;
        }
        String first = parent.firstKey();
        if (first != null) {
            selected = first;
            updateSelection();
        }
    }
    
    protected void updateSelection() {
        if (!ex.hasExtension())
            return;
        var entryTree = ex.get().createEntryTree();
        for (Tuple<String, GuiMenu<BooleanSupplier>.GuiMenuEntry> entry : entryTree.all())
            entry.value.setHighlighted(selected.startsWith(entry.getKey()));
    }
    
    private boolean is(KeyMapping key, int keyCode, int scanCode) {
        return key.matches(keyCode, scanCode) && key.getKeyModifier().isActive(null);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int action, int modifiers) {
        if (this.key.matchesPress(keyCode, action)) {
            if (!ex.hasExtension()) {
                data = new NamedTree<>();
                data.add("reset", () -> {
                    for (BuildingModeRule rule : rules)
                        rule.reset();
                    return true;
                });
                for (BuildingModeRule rule : rules)
                    rule.populate(data);
                
                selected = data.firstKey();
            }
            ex.toggle(this::createBox, ExtensionDirection.BELOW_OR_ABOVE);
            updateSelection();
            return true;
        } else if (ex.hasExtension() && (action == InputConstants.PRESS || action == InputConstants.REPEAT)) {
            if (keyCode == InputConstants.KEY_ESCAPE) {
                close();
                return true;
            } else if (is(MC.options.keyUp, keyCode, scanCode)) {
                previous();
                return true;
            } else if (is(MC.options.keyDown, keyCode, scanCode)) {
                next();
                return true;
            } else if (is(MC.options.keyLeft, keyCode, scanCode)) {
                out();
                return true;
            } else if (is(MC.options.keyRight, keyCode, scanCode)) {
                in();
                return true;
            } else if (is(MC.options.keyJump, keyCode, scanCode)) {
                var value = data.get(selected);
                if (value != null && value.getAsBoolean())
                    close();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, action, modifiers);
    }
    
    public static interface BuildingModeRule {
        
        public void populate(NamedTree<BooleanSupplier> tree);
        
        public void reset();
        
    }
    
}
