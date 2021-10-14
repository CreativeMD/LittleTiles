package mcjty.theoneprobe.api;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class CompoundText {

    private Component component = null;

    public static CompoundText create() {
        return new CompoundText();
    }

    /**
     * Conveniance for the common case where a label and info is required
     * Equivalent to CompoundText.create().labelInfo(label, value)
     */
    public static CompoundText createLabelInfo(String label, Object value) {
        return CompoundText.create().labelInfo(label, value);
    }

    /// Use this to set a style that the player can configure client-side. This is the recommended way to do text formatting
    public CompoundText style(TextStyleClass style) {
        Component cmp = new TextComponent(style.toString());
        return newComponent(cmp);
    }

    /// The prefered way to do translatable text
    public CompoundText text(Component cmp) {
        return newComponent(cmp);
    }

    /// Only use this for small strings or numbers for which no translation is useful
    public CompoundText text(String text) {
        return newComponent(new TextComponent(text));
    }

    /// A common usage: label + info
    public CompoundText labelInfo(String label, Object value) {
        return style(TextStyleClass.LABEL).text(label).style(TextStyleClass.INFO).text(String.valueOf(value));
    }

    /// Shorthand for style(TextStyleClass.INFO).text(new TranslationTextComponent(translationKey):
    public CompoundText info(String translationKey) {
        return info(new TranslatableComponent(translationKey));
    }

    /// Shorthand for style(TextStyleClass.INFO).text(cmp):
    public CompoundText info(Component cmp) {
        return style(TextStyleClass.INFO).text(cmp);
    }

    /// Shorthand for style(TextStyleClass.INFOIMP).text(new TranslationTextComponent(translationKey):
    public CompoundText important(String translationKey) {
        return important(new TranslatableComponent(translationKey));
    }

    /// Shorthand for style(TextStyleClass.INFOIMP).text(cmp):
    public CompoundText important(Component cmp) {
        return style(TextStyleClass.INFOIMP).text(cmp);
    }

    /// Shorthand for style(TextStyleClass.WARNING).text(new TranslationTextComponent(translationKey):
    public CompoundText warning(String translationKey) {
        return warning(new TranslatableComponent(translationKey));
    }

    /// Shorthand for style(TextStyleClass.WARNING).text(cmp):
    public CompoundText warning(Component cmp) {
        return style(TextStyleClass.WARNING).text(cmp);
    }

    /// Shorthand for style(TextStyleClass.ERROR).text(new TranslationTextComponent(translationKey):
    public CompoundText error(String translationKey) {
        return error(new TranslatableComponent(translationKey));
    }

    /// Shorthand for style(TextStyleClass.ERROR).text(cmp):
    public CompoundText error(Component cmp) {
        return style(TextStyleClass.ERROR).text(cmp);
    }

    /// Shorthand for style(TextStyleClass.LABEL).text(new TranslationTextComponent(translationKey):
    public CompoundText label(String translationKey) {
        return label(new TranslatableComponent(translationKey));
    }

    /// Shorthand for style(TextStyleClass.LABEL).text(cmp):
    public CompoundText label(Component cmp) {
        return style(TextStyleClass.LABEL).text(cmp);
    }

    /// Shorthand for style(TextStyleClass.OK).text(new TranslationTextComponent(translationKey):
    public CompoundText ok(String translationKey) {
        return ok(new TranslatableComponent(translationKey));
    }

    /// Shorthand for style(TextStyleClass.OK).text(cmp):
    public CompoundText ok(Component cmp) {
        return style(TextStyleClass.OK).text(cmp);
    }

    /// Shorthand for style(TextStyleClass.NAME).text(new TranslationTextComponent(translationKey):
    public CompoundText name(String translationKey) {
        return name(new TranslatableComponent(translationKey));
    }

    /// Shorthand for style(TextStyleClass.NAME).text(cmp):
    public CompoundText name(Component cmp) {
        return style(TextStyleClass.NAME).text(cmp);
    }

    /// Shorthand for style(TextStyleClass.PROGRESS).text(new TranslationTextComponent(translationKey):
    public CompoundText progress(String translationKey) {
        return progress(new TranslatableComponent(translationKey));
    }

    /// Shorthand for style(TextStyleClass.PROGRESS).text(cmp):
    public CompoundText progress(Component cmp) {
        return style(TextStyleClass.PROGRESS).text(cmp);
    }

    private CompoundText newComponent(Component cmp) {
        if (component == null) {
            component = cmp;
        } else if (component instanceof MutableComponent mutable) {
            mutable.append(cmp);
        } else {
            component = component.copy().append(cmp);
        }
        return this;
    }

    public Component get() {
        return component;
    }
}
