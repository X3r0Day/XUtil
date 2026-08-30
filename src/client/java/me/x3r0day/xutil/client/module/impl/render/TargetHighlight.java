package me.x3r0day.xutil.client.module.impl.render;

import me.x3r0day.xutil.client.module.Category;
import me.x3r0day.xutil.client.module.Module;
import me.x3r0day.xutil.client.render.MeshBuilder;
import me.x3r0day.xutil.client.render.MeshRenderer;
import me.x3r0day.xutil.client.render.XutilRenderPipelines;
import me.x3r0day.xutil.client.ui.OptionListScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;

import java.util.Optional;

public class TargetHighlight extends Module {

    private static final int FILL_R = 255;
    private static final int FILL_G = 60;
    private static final int FILL_B = 60;
    private static final int FILL_A = 120;

    private static volatile boolean active;
    private static MeshBuilder mesh;

    public TargetHighlight() {
        super("TargetHighlight", "Highlights the living entity under your crosshair if it is in hit range.",
            Category.RENDER);
    }

    @Override
    public void onSecondaryClick() {
        Minecraft mc = Minecraft.getInstance();
        Screen parent = mc.gui.screen();
        if (parent != null) {
            mc.gui.setScreen(OptionListScreen.createKeybindScreen("Target Highlight",
                "Bind a key to toggle the module", getKeybind(), parent));
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
        if (!active || mc.player == null || mc.level == null) return;

        LivingEntity target = findTarget(mc, delta);
        if (target == null) return;

        if (mesh == null) {
            mesh = new MeshBuilder(XutilRenderPipelines.OVERLAY.getVertexFormatBinding(0));
        }
        mesh.begin();
        drawBox(mesh, target.getBoundingBox());
        MeshRenderer.render(projection, modelView, mesh);
    }

    private static LivingEntity findTarget(Minecraft mc, float delta) {
        Vec3 eye = mc.player.getEyePosition(delta);
        Vec3 view = mc.player.getViewVector(delta);
        Vec3 end = eye.add(view.scale(mc.player.entityInteractionRange()));

        LivingEntity best = null;
        double bestDistSqr = Double.MAX_VALUE;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || living == mc.player || !living.isAlive()) {
                continue;
            }

            Optional<Vec3> hit = living.getBoundingBox().clip(eye, end);
            if (hit.isEmpty()) continue;

            double distSqr = eye.distanceToSqr(hit.get());
            if (distSqr < bestDistSqr) {
                bestDistSqr = distSqr;
                best = living;
            }
        }
        return best;
    }

    private static void drawBox(MeshBuilder mesh, AABB box) {
        double x0 = box.minX;
        double y0 = box.minY;
        double z0 = box.minZ;
        double x1 = box.maxX;
        double y1 = box.maxY;
        double z1 = box.maxZ;

        int a;
        int b;
        int c;
        int d;

        mesh.ensureQuadCapacity();
        a = mesh.vec3(x0, y1, z0).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        b = mesh.vec3(x1, y1, z0).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        c = mesh.vec3(x1, y1, z1).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        d = mesh.vec3(x0, y1, z1).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        mesh.quad(a, b, c, d);

        mesh.ensureQuadCapacity();
        a = mesh.vec3(x0, y0, z0).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        b = mesh.vec3(x0, y0, z1).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        c = mesh.vec3(x1, y0, z1).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        d = mesh.vec3(x1, y0, z0).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        mesh.quad(a, b, c, d);

        mesh.ensureQuadCapacity();
        a = mesh.vec3(x0, y0, z1).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        b = mesh.vec3(x1, y0, z1).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        c = mesh.vec3(x1, y1, z1).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        d = mesh.vec3(x0, y1, z1).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        mesh.quad(a, b, c, d);

        mesh.ensureQuadCapacity();
        a = mesh.vec3(x1, y0, z0).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        b = mesh.vec3(x0, y0, z0).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        c = mesh.vec3(x0, y1, z0).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        d = mesh.vec3(x1, y1, z0).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        mesh.quad(a, b, c, d);

        mesh.ensureQuadCapacity();
        a = mesh.vec3(x1, y0, z1).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        b = mesh.vec3(x1, y0, z0).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        c = mesh.vec3(x1, y1, z0).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        d = mesh.vec3(x1, y1, z1).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        mesh.quad(a, b, c, d);

        mesh.ensureQuadCapacity();
        a = mesh.vec3(x0, y0, z0).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        b = mesh.vec3(x0, y0, z1).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        c = mesh.vec3(x0, y1, z1).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        d = mesh.vec3(x0, y1, z0).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        mesh.quad(a, b, c, d);
    }
}
