package me.x3r0day.xutil.client.module.impl.world;

import me.x3r0day.xutil.client.module.Category;
import me.x3r0day.xutil.client.module.Module;
import me.x3r0day.xutil.client.ui.OptionListScreen;
import me.x3r0day.xutil.client.ui.OptionListScreen.KeybindRow;
import me.x3r0day.xutil.client.ui.OptionToggle;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public class AutoTool extends Module {

    private static volatile boolean recheck = true;
    private static int previousSlot = -1;
    private static boolean switched;

    public static final List<OptionToggle> TOGGLES = List.of(
        new OptionToggle("Recheck while mining", () -> recheck, () -> recheck = !recheck)
    );

    public AutoTool() {
        super("AutoTool", "Picks the best tool for the block you break, enchants included.", Category.WORLD);
    }

    @Override
    protected void saveSettings(JsonObject json) {
        json.addProperty("recheck", recheck);
    }

    @Override
    protected void loadSettings(JsonObject json) {
        recheck = GsonHelper.getAsBoolean(json, "recheck", false);
    }

    @Override
    public void onSecondaryClick() {
        Minecraft mc = Minecraft.getInstance();
        Screen parent = mc.gui.screen();
        if (parent != null) {
            mc.gui.setScreen(new OptionListScreen("AutoTool",
                "Click a row to change behavior", TOGGLES,
                List.of(new KeybindRow("Keybind", getKeybind())), parent));
        }
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gui.screen() != null) return;

        if (mc.hitResult instanceof BlockHitResult hit) {
            boolean mining = mc.options.keyAttack.isDown();
            if (mining) {
                if (previousSlot == -1) {
                    previousSlot = mc.player.getInventory().getSelectedSlot();
                }
                if (recheck || !switched) {
                    int best = bestSlot(mc, hit.getBlockPos());
                    if (best != -1) {
                        mc.player.getInventory().setSelectedSlot(best);
                        switched = true;
                    }
                }
            } else if (previousSlot != -1) {
                mc.player.getInventory().setSelectedSlot(previousSlot);
                previousSlot = -1;
                switched = false;
            }
        } else if (previousSlot != -1) {
            mc.player.getInventory().setSelectedSlot(previousSlot);
            previousSlot = -1;
            switched = false;
        }
    }

    private int bestSlot(Minecraft mc, BlockPos pos) {
        BlockState state = mc.level.getBlockState(pos);
        boolean wantSilk = state.is(Blocks.SPAWNER) || state.is(Blocks.TRIAL_SPAWNER)
            || mc.player.isShiftKeyDown();

        int best = -1;
        double bestScore = 1.0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            double score = stack.getDestroySpeed(state);

            int efficiency = levelOf(mc, stack, Enchantments.EFFICIENCY);
            if (efficiency > 0) {
                score += efficiency * efficiency + 1;
            }

            if (wantSilk) {
                score += levelOf(mc, stack, Enchantments.SILK_TOUCH) * 2;
            } else {
                score += levelOf(mc, stack, Enchantments.FORTUNE) * 2;
            }

            if (score > bestScore) {
                bestScore = score;
                best = i;
            }
        }
        return best;
    }

    private int levelOf(Minecraft mc, ItemStack stack, ResourceKey<Enchantment> enchant) {
        Holder<Enchantment> holder = mc.player.registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchant);
        return EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
    }
}
