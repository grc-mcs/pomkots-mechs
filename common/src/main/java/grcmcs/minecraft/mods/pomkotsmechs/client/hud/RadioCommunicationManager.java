package grcmcs.minecraft.mods.pomkotsmechs.client.hud;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.radio.RadioInterruptMode;
import grcmcs.minecraft.mods.pomkotsmechs.radio.RadioMessage;
import grcmcs.minecraft.mods.pomkotsmechs.radio.RadioText;
import net.minecraft.client.Minecraft;

import java.util.ArrayDeque;
import java.util.Deque;

public final class RadioCommunicationManager {
    public static final int ENTER_TICKS = 6;
    public static final int TRANSITION_TICKS = 4;
    public static final int EXIT_TICKS = 6;
    public static final int FLICKER_TICKS = 8;
    private static final Deque<RadioMessage> QUEUE = new ArrayDeque<>();
    private static RadioMessage current;
    private static int textIndex;
    private static int ticksInText;
    private static Phase phase = Phase.IDLE;
    private static int phaseTicks;

    private RadioCommunicationManager() {
    }

    public static void enqueue(RadioMessage message, RadioInterruptMode mode) {
        if (mode == RadioInterruptMode.REPLACE) {
            QUEUE.clear();
            current = null;
            phase = Phase.IDLE;
        }
        if (current == null) start(message);
        else QUEUE.addLast(message);
    }

    public static void stop() {
        QUEUE.clear();
        if (current != null && phase != Phase.EXITING && phase != Phase.FLICKERING) {
            phase = Phase.FLICKERING;
            phaseTicks = 0;
        }
    }

    public static void clear() {
        QUEUE.clear();
        current = null;
        textIndex = ticksInText = phaseTicks = 0;
        phase = Phase.IDLE;
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null) {
            clear();
            return;
        }
        if (minecraft.isPaused() || current == null) return;

        switch (phase) {
            case ENTERING -> {
                if (++phaseTicks >= ENTER_TICKS) {
                    phase = Phase.SHOWING;
                    phaseTicks = 0;
                }
            }
            case SHOWING -> {
                if (++ticksInText >= currentText().durationTicks()) {
                    phase = textIndex + 1 < current.texts().size() ? Phase.TRANSITION_OUT : Phase.FLICKERING;
                    phaseTicks = 0;
                }
            }
            case TRANSITION_OUT -> {
                if (++phaseTicks >= TRANSITION_TICKS) {
                    textIndex++;
                    ticksInText = 0;
                    phase = Phase.TRANSITION_IN;
                    phaseTicks = 0;
                }
            }
            case TRANSITION_IN -> {
                if (++phaseTicks >= TRANSITION_TICKS) {
                    phase = Phase.SHOWING;
                    phaseTicks = 0;
                }
            }
            case FLICKERING -> {
                if (++phaseTicks >= FLICKER_TICKS) {
                    phase = Phase.EXITING;
                    phaseTicks = 0;
                    playCloseSound();
                }
            }
            case EXITING -> {
                if (++phaseTicks >= EXIT_TICKS) finishCurrent();
            }
            case IDLE -> { }
        }
    }

    public static Snapshot snapshot(float partialTick) {
        if (current == null) return null;
        float panelAlpha = switch (phase) {
            case ENTERING -> Math.min(1.0F, (phaseTicks + partialTick) / ENTER_TICKS);
            case EXITING -> Math.max(0.0F, 1.0F - (phaseTicks + partialTick) / EXIT_TICKS);
            default -> 1.0F;
        };
        float textAlpha = switch (phase) {
            case TRANSITION_OUT -> Math.max(0.0F, 1.0F - (phaseTicks + partialTick) / TRANSITION_TICKS);
            case TRANSITION_IN -> Math.min(1.0F, (phaseTicks + partialTick) / TRANSITION_TICKS);
            default -> panelAlpha;
        };
        boolean flicker = phase == Phase.FLICKERING && (phaseTicks & 1) == 0;
        return new Snapshot(current, currentText(), panelAlpha, textAlpha, flicker);
    }

    private static RadioText currentText() {
        return current.texts().get(textIndex);
    }

    private static void start(RadioMessage message) {
        current = message;
        textIndex = ticksInText = phaseTicks = 0;
        phase = Phase.ENTERING;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.playSound(PomkotsMechs.SE_PANEL_OPEN.get(), 1.0F, 1.0F);
        }
    }

    private static void playCloseSound() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.playSound(PomkotsMechs.SE_PANEL_CLOSE.get(), 1.0F, 1.0F);
        }
    }

    private static void finishCurrent() {
        current = null;
        phase = Phase.IDLE;
        RadioMessage next = QUEUE.pollFirst();
        if (next != null) start(next);
    }

    private enum Phase { IDLE, ENTERING, SHOWING, TRANSITION_OUT, TRANSITION_IN, FLICKERING, EXITING }

    public record Snapshot(RadioMessage message, RadioText text, float panelAlpha, float textAlpha,
                           boolean flicker) { }
}
