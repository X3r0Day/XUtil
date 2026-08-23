package me.x3r0day.xutil.client.module.impl.render;

import me.x3r0day.xutil.client.module.Category;
import me.x3r0day.xutil.client.module.Module;
import me.x3r0day.xutil.client.render.MeshBuilder;
import me.x3r0day.xutil.client.render.MeshRenderer;
import me.x3r0day.xutil.client.render.XutilRenderPipelines;
import me.x3r0day.xutil.mixin.client.MultiPlayerGameModeAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4fc;

public class BreakIndicator extends Module {

    private static final int FILL_R = 138;
    private static final int FILL_G = 92;
    private static final int FILL_B = 255;
    private static final int FILL_A = 110;
    private static final double MIN_HALF_SIZE = 0.05;

    private static volatile boolean active;
    private static MeshBuilder mesh;

    public BreakIndicator() {
        super("BreakIndicator", "Fills the block with a growing overlay while you break it.", Category.RENDER);
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

        // top
        mesh.ensureQuadCapacity();
        int a = mesh.vec3(x0, y1, z0).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        int b = mesh.vec3(x1, y1, z0).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        int c = mesh.vec3(x1, y1, z1).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        int d = mesh.vec3(x0, y1, z1).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        mesh.quad(a, b, c, d);

        // bottom
        mesh.ensureQuadCapacity();
        a = mesh.vec3(x0, y0, z0).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        b = mesh.vec3(x0, y0, z1).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        c = mesh.vec3(x1, y0, z1).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        d = mesh.vec3(x1, y0, z0).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        mesh.quad(a, b, c, d);

        // south
        mesh.ensureQuadCapacity();
        a = mesh.vec3(x0, y0, z1).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        b = mesh.vec3(x1, y0, z1).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        c = mesh.vec3(x1, y1, z1).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        d = mesh.vec3(x0, y1, z1).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        mesh.quad(a, b, c, d);

        // north
        mesh.ensureQuadCapacity();
        a = mesh.vec3(x1, y0, z0).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        b = mesh.vec3(x0, y0, z0).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        c = mesh.vec3(x0, y1, z0).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        d = mesh.vec3(x1, y1, z0).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        mesh.quad(a, b, c, d);

        // east
        mesh.ensureQuadCapacity();
        a = mesh.vec3(x1, y0, z1).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        b = mesh.vec3(x1, y0, z0).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        c = mesh.vec3(x1, y1, z0).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        d = mesh.vec3(x1, y1, z1).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        mesh.quad(a, b, c, d);

        // west
        mesh.ensureQuadCapacity();
        a = mesh.vec3(x0, y0, z0).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        b = mesh.vec3(x0, y0, z1).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        c = mesh.vec3(x0, y1, z1).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        d = mesh.vec3(x0, y1, z0).color(FILL_R, FILL_G, FILL_B, FILL_A).next();
        mesh.quad(a, b, c, d);

        MeshRenderer.render(projection, modelView, mesh);
    }
}
