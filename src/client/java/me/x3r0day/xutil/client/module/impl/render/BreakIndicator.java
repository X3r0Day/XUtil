package me.x3r0day.xutil.client.module.impl.render;

import me.x3r0day.xutil.client.module.Category;
import me.x3r0day.xutil.client.module.Module;
import me.x3r0day.xutil.client.render.MeshBuilder;
import me.x3r0day.xutil.client.render.MeshRenderer;
import me.x3r0day.xutil.client.render.XutilRenderPipelines;
import me.x3r0day.xutil.client.ui.GuiTheme;
import me.x3r0day.xutil.client.ui.OptionListScreen;
import me.x3r0day.xutil.client.ui.OptionListScreen.CycleRow;
import me.x3r0day.xutil.client.ui.OptionListScreen.KeybindRow;
import me.x3r0day.xutil.client.ui.OptionToggle;
import me.x3r0day.xutil.mixin.client.MultiPlayerGameModeAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4fc;

import java.util.List;

public class BreakIndicator extends Module {

    private static final int FILL_A = 110;
    private static final double MIN_HALF_SIZE = 0.05;

    private static final String[] COLOR_NAMES = {
        "Purple", "Blue", "Green", "Yellow", "Orange", "Red", "Pink", "White"
    };

    private static volatile boolean active;
    private static volatile boolean rainbow = false;
    private static volatile int colorIndex = 0;
    private static MeshBuilder mesh;

    public static final List<OptionToggle> TOGGLES = List.of(
        new OptionToggle("Rainbow", () -> rainbow, () -> rainbow = !rainbow)
    );

    public static final List<CycleRow> CYCLES = List.of(
        new CycleRow("Color",
            () -> COLOR_NAMES[colorIndex % COLOR_NAMES.length],
            () -> colorIndex = (colorIndex + 1) % COLOR_NAMES.length)
    );

    public BreakIndicator() {
        super("BreakIndicator", "Fills the block with a growing overlay while you break it.", Category.RENDER);
    }

    @Override
    public void onSecondaryClick() {
        Minecraft mc = Minecraft.getInstance();
        Screen parent = mc.gui.screen();
        if (parent != null) {
            mc.gui.setScreen(new OptionListScreen("Break Indicator",
                "Click a row to change the color", TOGGLES, CYCLES,
                List.of(new KeybindRow("Keybind", getKeybind())), parent));
        }
    }

    @Override
    protected void onEnable() {
        active = true;
    }

    @Override
    protected void onDisable() {
        active = false;
    }

    public static void render3D(Matrix4fc projection, Matrix4fc modelView, float delta) {
        Minecraft mc = Minecraft.getInstance();
        if (!active || mc.player == null || mc.level == null || mc.gameMode == null) return;
        if (!mc.gameMode.isDestroying()) return;

        MultiPlayerGameModeAccessor accessor = (MultiPlayerGameModeAccessor) mc.gameMode;
        BlockPos pos = accessor.xutil$getDestroyBlockPos();
        float progress = accessor.xutil$getDestroyProgress();
        if (pos == null || progress <= 0f) return;

        VoxelShape shape = mc.level.getBlockState(pos).getShape(mc.level, pos);
        if (shape.isEmpty()) return;
        AABB box = shape.bounds().move(pos.getX(), pos.getY(), pos.getZ());

        double centerX = (box.minX + box.maxX) / 2;
        double centerY = (box.minY + box.maxY) / 2;
        double centerZ = (box.minZ + box.maxZ) / 2;
        double halfX = Math.max(MIN_HALF_SIZE, (box.maxX - box.minX) / 2 * progress);
        double halfY = Math.max(MIN_HALF_SIZE, (box.maxY - box.minY) / 2 * progress);
        double halfZ = Math.max(MIN_HALF_SIZE, (box.maxZ - box.minZ) / 2 * progress);

        double x0 = centerX - halfX;
        double y0 = centerY - halfY;
        double z0 = centerZ - halfZ;
        double x1 = centerX + halfX;
        double y1 = centerY + halfY;
        double z1 = centerZ + halfZ;

        if (mesh == null) {
            mesh = new MeshBuilder(XutilRenderPipelines.OVERLAY
                .getVertexFormatBinding(0));
        }
        mesh.begin();

        int[] rgb = currentColor();

        // top
        mesh.ensureQuadCapacity();
        int a = mesh.vec3(x0, y1, z0).color(rgb[0], rgb[1], rgb[2], FILL_A).next();
        int b = mesh.vec3(x1, y1, z0).color(rgb[0], rgb[1], rgb[2], FILL_A).next();
        int c = mesh.vec3(x1, y1, z1).color(rgb[0], rgb[1], rgb[2], FILL_A).next();
        int d = mesh.vec3(x0, y1, z1).color(rgb[0], rgb[1], rgb[2], FILL_A).next();
        mesh.quad(a, b, c, d);

        // bottom
        mesh.ensureQuadCapacity();
        a = mesh.vec3(x0, y0, z0).color(rgb[0], rgb[1], rgb[2], FILL_A).next();
        b = mesh.vec3(x0, y0, z1).color(rgb[0], rgb[1], rgb[2], FILL_A).next();
        c = mesh.vec3(x1, y0, z1).color(rgb[0], rgb[1], rgb[2], FILL_A).next();
        d = mesh.vec3(x1, y0, z0).color(rgb[0], rgb[1], rgb[2], FILL_A).next();
        mesh.quad(a, b, c, d);

        // south
        mesh.ensureQuadCapacity();
        a = mesh.vec3(x0, y0, z1).color(rgb[0], rgb[1], rgb[2], FILL_A).next();
        b = mesh.vec3(x1, y0, z1).color(rgb[0], rgb[1], rgb[2], FILL_A).next();
        c = mesh.vec3(x1, y1, z1).color(rgb[0], rgb[1], rgb[2], FILL_A).next();
        d = mesh.vec3(x0, y1, z1).color(rgb[0], rgb[1], rgb[2], FILL_A).next();
        mesh.quad(a, b, c, d);

        // north
        mesh.ensureQuadCapacity();
        a = mesh.vec3(x1, y0, z0).color(rgb[0], rgb[1], rgb[2], FILL_A).next();
        b = mesh.vec3(x0, y0, z0).color(rgb[0], rgb[1], rgb[2], FILL_A).next();
        c = mesh.vec3(x0, y1, z0).color(rgb[0], rgb[1], rgb[2], FILL_A).next();
        d = mesh.vec3(x1, y1, z0).color(rgb[0], rgb[1], rgb[2], FILL_A).next();
        mesh.quad(a, b, c, d);

        // east
        mesh.ensureQuadCapacity();
        a = mesh.vec3(x1, y0, z1).color(rgb[0], rgb[1], rgb[2], FILL_A).next();
        b = mesh.vec3(x1, y0, z0).color(rgb[0], rgb[1], rgb[2], FILL_A).next();
        c = mesh.vec3(x1, y1, z0).color(rgb[0], rgb[1], rgb[2], FILL_A).next();
        d = mesh.vec3(x1, y1, z1).color(rgb[0], rgb[1], rgb[2], FILL_A).next();
        mesh.quad(a, b, c, d);

        // west
        mesh.ensureQuadCapacity();
        a = mesh.vec3(x0, y0, z0).color(rgb[0], rgb[1], rgb[2], FILL_A).next();
        b = mesh.vec3(x0, y0, z1).color(rgb[0], rgb[1], rgb[2], FILL_A).next();
        c = mesh.vec3(x0, y1, z1).color(rgb[0], rgb[1], rgb[2], FILL_A).next();
        d = mesh.vec3(x0, y1, z0).color(rgb[0], rgb[1], rgb[2], FILL_A).next();
        mesh.quad(a, b, c, d);

        MeshRenderer.render(projection, modelView, mesh);
    }

    private static int[] currentColor() {
        if (rainbow) {
            float hue = (System.currentTimeMillis() % 3600) / 3600f;
            float x = 1f - Math.abs((hue * 6f) % 2f - 1f);
            float r, g, b;
            if (hue < 1f / 6f) {
                r = 1f; g = x; b = 0f;
            } else if (hue < 2f / 6f) {
                r = x; g = 1f; b = 0f;
            } else if (hue < 3f / 6f) {
                r = 0f; g = 1f; b = x;
            } else if (hue < 4f / 6f) {
                r = 0f; g = x; b = 1f;
            } else if (hue < 5f / 6f) {
                r = x; g = 0f; b = 1f;
            } else {
                r = 1f; g = 0f; b = x;
            }
            return new int[]{(int) (r * 255), (int) (g * 255), (int) (b * 255)};
        }
        int color = GuiTheme.PALETTE[colorIndex % GuiTheme.PALETTE.length];
        return new int[]{(color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF};
    }
}
