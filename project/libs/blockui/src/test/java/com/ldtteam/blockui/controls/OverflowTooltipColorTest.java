package com.ldtteam.blockui.controls;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class OverflowTooltipColorTest
{
    @Test
    public void tooltipStylingDoesNotReplaceTheLabelsXmlColor()
    {
        final var title = Component.literal("Opciones estéticas:");
        final var label = Component.literal("Mostrar mensajes de ayuda:");
        final var lines = List.of(title, label);

        // paragraphBreak is the same in-place styling step used by build(),
        // without needing a Minecraft screen or a render context.
        final var tooltip = AbstractTextElement.overflowTooltipBuilder(lines).paragraphBreak();
        assertEquals(0xFFFFFF, tooltip.getText().get(0).getStyle().getColor().getValue());
        assertEquals(0xFFFFFF, tooltip.getText().get(1).getStyle().getColor().getValue());
        assertEquals(Style.EMPTY, title.getStyle());
        assertEquals(Style.EMPTY, label.getStyle());
        assertNull(title.getStyle().getColor());
        assertNull(label.getStyle().getColor());

        // Rebuilding a tooltip after changing the text must also be harmless.
        label.append(" actualizado");
        AbstractTextElement.overflowTooltipBuilder(lines).paragraphBreak();
        assertEquals(Style.EMPTY, label.getStyle());
        assertEquals("Mostrar mensajes de ayuda: actualizado", label.getString());
    }

    @Test
    public void tooltipKeepsExplicitFormattingAndDoesNotMutateSiblings()
    {
        final var name = Component.literal("Destiny").withStyle(ChatFormatting.BLUE);
        final var title = Component.literal("Colonia: ").append(name);
        final var formatted = Component.literal("Opciones estéticas:").withStyle(ChatFormatting.BOLD);
        final var originalNameStyle = name.getStyle();
        final var originalFormattedStyle = formatted.getStyle();

        final var tooltip = AbstractTextElement.overflowTooltipBuilder(List.of(title, formatted)).paragraphBreak();
        assertEquals(originalFormattedStyle, tooltip.getText().get(1).getStyle());
        assertEquals(originalNameStyle, tooltip.getText().get(0).getSiblings().get(0).getStyle());
        assertEquals(Style.EMPTY, title.getStyle());
        assertEquals(originalNameStyle, name.getStyle());
        assertEquals(originalFormattedStyle, formatted.getStyle());
        assertEquals("Colonia: Destiny", title.getString());
    }
}
