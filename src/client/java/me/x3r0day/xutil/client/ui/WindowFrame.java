package me.x3r0day.xutil.client.ui;

import me.x3r0day.xutil.client.module.Category;
import me.x3r0day.xutil.client.module.Module;
import me.x3r0day.xutil.client.module.ModuleManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.List;

public class WindowFrame {

    public static final int WIDTH = 110;
    private static final int HEADER_HEIGHT = 15;
    private static final int ENTRY_HEIGHT = 12;

    private static final int COLOR_HEADER = 0xDD1B1B24;
    private static final int COLOR_BODY = 0xC0121218;
    private static final int COLOR_ACCENT = 0xFF8A5CFF;
    private static final int COLOR_HOVER = 0x30FFFFFF;
    private static final int COLOR_TEXT_ON = 0xFFFFFFFF;
    private static final int COLOR_TEXT_OFF = 0xFF9A9AA5;

    private final Category category;
    private int x;
    private int y;
    private boolean dragging;
    private double dragOffsetX;
    private double dragOffsetY;

    public WindowFrame(Category category, int x, int y) {
        this.category = category;
        this.x = x;
        this.y = y;
    }

    public static int heightFor(Category category) {
        return HEADER_HEIGHT + ModuleManager.getByCategory(category).size() * ENTRY_HEIGHT;
    }

    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        List<Module> modules = ModuleManager.getByCategory(category);
        int bodyHeight = modules.size() * ENTRY_HEIGHT;

        graphics.fill(x, y, x + WIDTH, y + HEADER_HEIGHT, COLOR_HEADER);
        graphics.fill(x, y + HEADER_HEIGHT - 1, x + WIDTH, y + HEADER_HEIGHT, COLOR_ACCENT);
        graphics.centeredText(font, category.getDisplayName(),
            x + WIDTH / 2, y + (HEADER_HEIGHT - font.lineHeight) / 2, COLOR_TEXT_ON);

        if (bodyHeight > 0) {
            graphics.fill(x, y + HEADER_HEIGHT, x + WIDTH, y + HEADER_HEIGHT + bodyHeight, COLOR_BODY);
        }

        int entryY = y + HEADER_HEIGHT;
        for (Module module : modules) {
            if (isHovering(mouseX, mouseY, entryY)) {
                graphics.fill(x, entryY, x + WIDTH, entryY + ENTRY_HEIGHT, COLOR_HOVER);
            }
            int color = module.isEnabled() ? COLOR_TEXT_ON : COLOR_TEXT_OFF;
            graphics.text(font, module.getName(),
                x + 4, entryY + (ENTRY_HEIGHT - font.lineHeight) / 2, color, true);
            entryY += ENTRY_HEIGHT;
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
