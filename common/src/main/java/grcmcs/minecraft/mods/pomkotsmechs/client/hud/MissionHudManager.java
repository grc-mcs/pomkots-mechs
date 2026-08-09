package grcmcs.minecraft.mods.pomkotsmechs.client.hud;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import java.util.ArrayDeque;
import java.util.Deque;

public final class MissionHudManager {
    private static Objective objective;
    private static Presentation presentation;
    private static final Deque<Presentation> PRESENTATION_QUEUE = new ArrayDeque<>();
    private static int presentationTick;

    private MissionHudManager() { }

    public static void setObjective(Component mission, Component stage, Component text,
                                    Component progress, int remainingTicks) {
        objective = new Objective(mission, stage, text, progress, remainingTicks);
    }
    public static void clearObjective() { objective = null; }
    public static Objective objective() { return objective; }

    public static void show(String style, Component title, Component message, int duration) {
        Presentation next = new Presentation(style, title, message, Math.max(1, duration));
        if (presentation != null) { PRESENTATION_QUEUE.addLast(next); return; }
        start(next);
    }

    private static void start(Presentation next) {
        presentation = next;
        presentationTick = 0;
        if (Minecraft.getInstance().player != null)
            Minecraft.getInstance().player.playSound(PomkotsMechs.SE_PANEL_OPEN.get(), 1.0F, 1.0F);
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.level == null) { objective = null; presentation = null; PRESENTATION_QUEUE.clear(); return; }
        if (minecraft.isPaused() || presentation == null) return;
        presentationTick++;
        if (presentationTick == presentation.duration() - MissionPresentationHud.EXIT_TICKS
                && minecraft.player != null) {
            minecraft.player.playSound(PomkotsMechs.SE_PANEL_CLOSE.get(), 1.0F, 1.0F);
        }
        if (presentationTick >= presentation.duration()) {
            presentation = null;
            Presentation next = PRESENTATION_QUEUE.pollFirst();
            if (next != null) start(next);
        }
    }

    public static PresentationSnapshot presentation(float partialTick) {
        if (presentation == null) return null;
        float time = presentationTick + partialTick;
        float alpha = Math.min(1.0F, time / MissionPresentationHud.ENTER_TICKS);
        alpha = Math.min(alpha, Math.max(0.0F, (presentation.duration() - time) / MissionPresentationHud.EXIT_TICKS));
        boolean flicker = presentation.duration() - time < MissionPresentationHud.EXIT_TICKS + 8
                && ((int) time & 1) == 0;
        return new PresentationSnapshot(presentation, alpha, flicker);
    }

    public record Objective(Component mission, Component stage, Component text,
                            Component progress, int remainingTicks) { }
    public record Presentation(String style, Component title, Component message, int duration) { }
    public record PresentationSnapshot(Presentation value, float alpha, boolean flicker) { }
}
