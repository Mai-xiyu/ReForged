package net.neoforged.neoforge.client.extensions;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;

/**
 * Extension interface for {@link Font}.
 */
public interface IFontExtension {

    FormattedText ELLIPSIS = FormattedText.of("...");

    Font self();

    /**
     * Truncates the given text to fit within maxWidth, appending "..." if needed.
     */
    default FormattedText ellipsize(FormattedText text, int maxWidth) {
        Font font = self();
        int ellipsisWidth = font.width(ELLIPSIS);
        if (font.width(text) <= maxWidth) {
            return text;
        }
        return FormattedText.composite(
                font.substrByWidth(text, maxWidth - ellipsisWidth),
                ELLIPSIS
        );
    }
}
