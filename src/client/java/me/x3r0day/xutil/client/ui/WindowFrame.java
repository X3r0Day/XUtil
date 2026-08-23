package me.x3r0day.xutil.client.ui;

import me.x3r0day.xutil.client.module.Category;
import me.x3r0day.xutil.client.module.Module;
import me.x3r0day.xutil.client.module.ModuleManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class WindowFrame {

    public static final int WIDTH = 120;
    private static final int HEADER_HEIGHT = 16;
    private static final int ENTRY_HEIGHT = 13;
    private static final int EMPTY_BODY_HEIGHT = 12;
    private static final int SHADOW_OFFSET = 3;

    private static final int ACCENT = 0xFF8A5CFF;
    private static final int COLOR_HEADER = 0xE61B1B24;
    private static final int COLOR_BODY = 0xE6121218;
    private static final int COLOR_HOVER = 0x1AFFFFFF;
    private static final int COLOR_SEPARATOR = 0x10FFFFFF;
    private static final int COLOR_TEXT_ON = 0xFFFFFFFF;
    private static final int COLOR_TEXT_OFF = 0xFF9A9AA5;
    private static final int COLOR_TEXT_MUTED = 0xFF6E6E7A;
    private static final int COLOR_INDICATOR_OFF = 0x669A9AA5;
    private static final int COLOR_SHADOW = 0x40000000;
    private static final int COLOR_TOOLTIP_BG = 0xF01B1B24;
    private static final int COLOR_TOOLTIP_TEXT = 0xFFC8C8D0;
    private static final int TOOLTIP_WIDTH = 160;

    private final Category category;
    private int x;
    private int y;
    private boolean dragging;
    private double dragOffsetX;
    private double dragOffsetY;
    private Module hovered;

    public WindowFrame(Category category, int x, int y) {
        this.category = category;
        this.x = x;
        this.y = y;
    }

    public Category getCategory() {
        return category;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static int heightFor(Category category) {
        return HEADER_HEIGHT + bodyHeight(ModuleManager.getByCategory(category).size());
    }

    private static int bodyHeight(int moduleCount) {
        return moduleCount > 0 ? moduleCount * ENTRY_HEIGHT : EMPTY_BODY_HEIGHT;
    }

    private static int fade(int color, float alpha) {
        if (alpha >= 1f) return color;
        int a = (color >>> 24) & 0xFF;
        a = (int) (a * alpha);
        return (color & 0x00FFFFFF) | (a << 24);
    }

    // ease out with a tiny overshoot, feels like the window pops into place
    private static float easeOutBack(float t) {
        float c1 = 1.2f;
        float c3 = c1 + 1f;
        float u = t - 1f;
        return 1f + c3 * u * u * u + c1 * u * u;
    }

    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY, float anim) {
        List<Module> modules = ModuleManager.getByCategory(category);
        int bodyHeight = bodyHeight(modules.size());
        int windowHeight = HEADER_HEIGHT + bodyHeight;

        float eased = easeOutBack(anim);
        float scale = 0.94f + 0.06f * eased;
        float alpha = Math.min(1f, anim * 2f);
        float rise = (1f - eased) * 6f;

        graphics.pose().pushMatrix();
        graphics.pose().translate(x + WIDTH / 2f, y + windowHeight / 2f);
        graphics.pose().scale(scale, scale);
        graphics.pose().translate(-(x + WIDTH / 2f), -(y + windowHeight / 2f) + rise);

        graphics.fill(x + SHADOW_OFFSET, y + SHADOW_OFFSET, x + WIDTH + SHADOW_OFFSET,
            y + SHADOW_OFFSET + windowHeight, fade(COLOR_SHADOW, alpha));

        graphics.fill(x, y, x + WIDTH, y + HEADER_HEIGHT, fade(COLOR_HEADER, alpha));
        graphics.fill(x, y + HEADER_HEIGHT - 2, x + WIDTH, y + HEADER_HEIGHT, fade(ACCENT, alpha));
        graphics.fill(x, y + HEADER_HEIGHT, x + WIDTH, y + HEADER_HEIGHT + bodyHeight,
            fade(COLOR_BODY, alpha));

        graphics.centeredText(font, category.getDisplayName(), x + WIDTH / 2,
            y + (HEADER_HEIGHT - font.lineHeight) / 2, fade(COLOR_TEXT_ON, alpha));

        hovered = null;
        if (modules.isEmpty()) {
            graphics.centeredText(font, "No modules", x + WIDTH / 2,
                y + HEADER_HEIGHT + (EMPTY_BODY_HEIGHT - font.lineHeight) / 2,
                fade(COLOR_TEXT_MUTED, alpha));
            graphics.pose().popMatrix();
            return;
        }

        int entryY = y + HEADER_HEIGHT;
        for (Module module : modules) {
            boolean hovering = isHovering(mouseX, mouseY, entryY);
            if (hovering) {
                hovered = module;
                graphics.fill(x, entryY, x + WIDTH, entryY + ENTRY_HEIGHT, fade(COLOR_HOVER, alpha));
                graphics.fill(x, entryY, x + 2, entryY + ENTRY_HEIGHT, fade(ACCENT, alpha));
            }
            graphics.fill(x + 4, entryY, x + WIDTH - 4, entryY + 1, fade(COLOR_SEPARATOR, alpha));

            int color = module.isEnabled() ? COLOR_TEXT_ON : COLOR_TEXT_OFF;
            graphics.text(font, module.getName(), x + 6,
                entryY + (ENTRY_HEIGHT - font.lineHeight) / 2, fade(color, alpha), true);

            int indicatorSize = 5;
            int indicatorX = x + WIDTH - indicatorSize - 6;
            int indicatorY = entryY + (ENTRY_HEIGHT - indicatorSize) / 2;
            graphics.fill(indicatorX, indicatorY, indicatorX + indicatorSize,
                indicatorY + indicatorSize,
                fade(module.isEnabled() ? ACCENT : COLOR_INDICATOR_OFF, alpha));
            entryY += ENTRY_HEIGHT;
        }

        graphics.pose().popMatrix();
    }

    public boolean hasHoveredModule() {
        return hovered != null;
    }

    public void renderTooltip(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY,
            int screenWidth, int screenHeight) {
        if (hovered == null) return;

        List<FormattedCharSequence> lines = font.split(
            Component.literal(hovered.getDescription()), TOOLTIP_WIDTH - 14);
        int tooltipHeight = 8 + font.lineHeight + 2 + lines.size() * font.lineHeight;

        int tooltipX = mouseX + 14;
        if (tooltipX + TOOLTIP_WIDTH > screenWidth - 4) {
            tooltipX = mouseX - 14 - TOOLTIP_WIDTH;
        }
        int tooltipY = mouseY + 14;
        if (tooltipY + tooltipHeight > screenHeight - 4) {
            tooltipY = mouseY - 14 - tooltipHeight;
        }

        graphics.fill(tooltipX, tooltipY, tooltipX + TOOLTIP_WIDTH, tooltipY + tooltipHeight,
            COLOR_TOOLTIP_BG);
        graphics.fill(tooltipX, tooltipY, tooltipX + 2, tooltipY + tooltipHeight, ACCENT);
        graphics.text(font, hovered.getName(), tooltipX + 6, tooltipY + 4, COLOR_TEXT_ON, true);

        int lineY = tooltipY + 6 + font.lineHeight;
        for (FormattedCharSequence line : lines) {
            graphics.text(font, line, tooltipX + 6, lineY, COLOR_TOOLTIP_TEXT);
            lineY += font.lineHeight;
        }
    }

    public boolean mouseClicked(MouseButtonEvent event) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (mouseX < x || mouseX >= x + WIDTH) return false;

        if (mouseY >= y && mouseY < y + HEADER_HEIGHT) {
            dragging = true;
            dragOffsetX = mouseX - x;
            dragOffsetY = mouseY - y;
            return true;
        }

        int entryY = y + HEADER_HEIGHT;
        for (Module module : ModuleManager.getByCategory(category)) {
            if (mouseY >= entryY && mouseY < entryY + ENTRY_HEIGHT) {
                if (event.button() == 0) {
                    module.toggle();
                } else if (event.button() == 1) {
                    module.onSecondaryClick();
                }
                return true;
            }
            entryY += ENTRY_HEIGHT;
        }
        return false;
    }

    public boolean mouseDragged(MouseButtonEvent event) {
        if (!dragging) return false;
        x = (int) (event.x() - dragOffsetX);
        y = (int) (event.y() - dragOffsetY);
        return true;
    }

    public void mouseReleased(MouseButtonEvent event) {
        dragging = false;
    }

    private boolean isHovering(int mouseX, int mouseY, int entryY) {
        return mouseX >= x && mouseX < x + WIDTH && mouseY >= entryY && mouseY < entryY + ENTRY_HEIGHT;
    }
}
