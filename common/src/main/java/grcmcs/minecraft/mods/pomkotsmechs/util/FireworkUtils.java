package grcmcs.minecraft.mods.pomkotsmechs.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class FireworkUtils {

    /**
     * ランダムな花火を打ち上げる
     * @param level ワールド
     * @param pos 打ち上げ位置
     */
    public static void launchRandomFirework(ServerLevel level, Vec3 pos) {
        // 花火ロケットを作成
        ItemStack fireworkStack = createRandomFirework(level);

        // 花火エンティティを生成
        FireworkRocketEntity firework = new FireworkRocketEntity(
                level,
                pos.x,
                pos.y,
                pos.z,
                fireworkStack
        );

        level.addFreshEntity(firework);
    }

    /**
     * ランダムな花火アイテムを作成
     */
    public static ItemStack createRandomFirework(Level level) {
        ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
        CompoundTag tag = stack.getOrCreateTagElement("Fireworks");

        // 飛行時間（1~3）
        int flightDuration = 1 + level.random.nextInt(3);
        tag.putByte("Flight", (byte) flightDuration);

        // 爆発効果を1~3個追加
        int explosionCount = 1 + level.random.nextInt(3);
        ListTag explosions = new ListTag();

        for (int i = 0; i < explosionCount; i++) {
            explosions.add(createRandomExplosion(level.random));
        }

        tag.put("Explosions", explosions);

        return stack;
    }

    /**
     * ランダムな爆発効果を作成
     */
    private static CompoundTag createRandomExplosion(RandomSource random) {
        CompoundTag explosion = new CompoundTag();

        // 形状（0=小さい玉、1=大きい玉、2=星型、3=クリーパー型、4=バースト）
        int[] shapes = {0, 1, 2, 3, 4};
        explosion.putByte("Type", (byte) shapes[random.nextInt(shapes.length)]);

        // 色（1~5色）
        int colorCount = 1 + random.nextInt(5);
        int[] colors = new int[colorCount];
        for (int i = 0; i < colorCount; i++) {
            colors[i] = getRandomFireworkColor(random);
        }
        explosion.putIntArray("Colors", colors);

        // フェード色（50%の確率で追加）
        if (random.nextBoolean()) {
            int fadeColorCount = 1 + random.nextInt(3);
            int[] fadeColors = new int[fadeColorCount];
            for (int i = 0; i < fadeColorCount; i++) {
                fadeColors[i] = getRandomFireworkColor(random);
            }
            explosion.putIntArray("FadeColors", fadeColors);
        }

        // エフェクト
        if (random.nextFloat() < 0.3F) { // 30%の確率
            explosion.putBoolean("Trail", true); // 軌跡
        }

        if (random.nextFloat() < 0.3F) { // 30%の確率
            explosion.putBoolean("Flicker", true); // きらめき
        }

        return explosion;
    }

    /**
     * ランダムな花火の色を取得
     */
    private static int getRandomFireworkColor(RandomSource random) {
        int[] commonColors = {
                0xFF0000,  // 赤
                0x00FF00,  // 緑
                0x0000FF,  // 青
                0xFFFF00,  // 黄色
                0xFF00FF,  // マゼンタ
                0x00FFFF,  // シアン
                0xFFFFFF,  // 白
                0xFF8800,  // オレンジ
                0x8800FF,  // 紫
                0xFF0088,  // ピンク
        };

        return commonColors[random.nextInt(commonColors.length)];
    }
    /**
     * 連続して花火を打ち上げる
     */
    public static void launchFireworkShow(ServerLevel level, Vec3 centerPos, int count, int delayTicks) {
        for (int i = 0; i < count; i++) {
            final int index = i;

            // 遅延実行
            level.getServer().execute(() -> {
                scheduleFirework(level, centerPos, index * delayTicks);
            });
        }
    }

    private static void scheduleFirework(ServerLevel level, Vec3 centerPos, int delayTicks) {
        MinecraftServer server = level.getServer();

        // スケジューラーで遅延実行
        server.tell(new TickTask(server.getTickCount() + delayTicks, () -> {
            // ランダムな位置にオフセット
            double offsetX = (level.random.nextDouble() - 0.5) * 10.0;
            double offsetZ = (level.random.nextDouble() - 0.5) * 10.0;

            Vec3 pos = centerPos.add(offsetX, 0, offsetZ);

            // ランダムなテーマで打ち上げ
            FireworkTheme theme = FireworkTheme.values()[
                    level.random.nextInt(FireworkTheme.values().length)
                    ];

            launchThemedFirework(level, pos, theme);
        }));
    }

    /**
     * 円形に花火を打ち上げる
     */
    public static void launchCircleFireworks(ServerLevel level, Vec3 centerPos,
                                             int count, double radius) {
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double x = centerPos.x + Math.cos(angle) * radius;
            double z = centerPos.z + Math.sin(angle) * radius;

            Vec3 pos = new Vec3(x, centerPos.y, z);

            // 少し遅延をずらして打ち上げ
            final int delay = i * 2; // 2tick間隔
            level.getServer().tell(new TickTask(
                    level.getServer().getTickCount() + delay,
                    () -> launchRandomFirework(level, pos)
            ));
        }
    }

    /**
     * ランダムな範囲に花火を打ち上げる
     */
    public static void launchRandomAreaFireworks(ServerLevel level, Vec3 centerPos,
                                                 double range, int count, int totalDurationTicks) {
        for (int i = 0; i < count; i++) {
            int delay = level.random.nextInt(totalDurationTicks);

            level.getServer().tell(new TickTask(
                    level.getServer().getTickCount() + delay,
                    () -> {
                        double x = centerPos.x + (level.random.nextDouble() - 0.5) * range * 2;
                        double z = centerPos.z + (level.random.nextDouble() - 0.5) * range * 2;

                        launchRandomFirework(level, new Vec3(x, centerPos.y, z));
                    }
            ));
        }
    }


    /**
     * 特定のテーマの花火を打ち上げる
     */
    public static void launchThemedFirework(ServerLevel level, Vec3 pos, FireworkTheme theme) {
        ItemStack fireworkStack = createThemedFirework(level.random, theme);

        FireworkRocketEntity firework = new FireworkRocketEntity(
                level,
                pos.x,
                pos.y,
                pos.z,
                fireworkStack
        );

        level.addFreshEntity(firework);
    }

    /**
     * テーマ別の花火を作成
     */
    public static ItemStack createThemedFirework(RandomSource random, FireworkTheme theme) {
        ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
        CompoundTag tag = stack.getOrCreateTagElement("Fireworks");

        tag.putByte("Flight", (byte) theme.flightDuration);

        ListTag explosions = new ListTag();
        for (int i = 0; i < theme.explosionCount; i++) {
            explosions.add(createThemedExplosion(random, theme));
        }

        tag.put("Explosions", explosions);

        return stack;
    }

    private static CompoundTag createThemedExplosion(RandomSource random, FireworkTheme theme) {
        CompoundTag explosion = new CompoundTag();

        explosion.putByte("Type", (byte) theme.shapes[random.nextInt(theme.shapes.length)]);

        // テーマの色を使用
        int colorCount = Math.min(theme.colors.length, 1 + random.nextInt(3));
        int[] colors = new int[colorCount];
        for (int i = 0; i < colorCount; i++) {
            colors[i] = theme.colors[random.nextInt(theme.colors.length)];
        }
        explosion.putIntArray("Colors", colors);

        // フェード色
        if (theme.useFadeColors && random.nextBoolean()) {
            int[] fadeColors = new int[1 + random.nextInt(2)];
            for (int i = 0; i < fadeColors.length; i++) {
                fadeColors[i] = theme.fadeColors[random.nextInt(theme.fadeColors.length)];
            }
            explosion.putIntArray("FadeColors", fadeColors);
        }

        explosion.putBoolean("Trail", theme.trail);
        explosion.putBoolean("Flicker", theme.flicker);

        return explosion;
    }

    /**
     * 花火のテーマ
     */
    public enum FireworkTheme {
        RANDOM(
                new int[]{0, 1, 2, 3, 4},
                new int[]{0xFF0000, 0x00FF00, 0x0000FF, 0xFFFF00, 0xFF00FF, 0x00FFFF, 0xFFFFFF},
                new int[]{0xFFFFFF, 0xFFFF00, 0xFF8800},
                true, false, false,
                2, 1 + new Random().nextInt(3)
        ),

        CELEBRATION(
                new int[]{1, 4}, // 大きい玉とバースト
                new int[]{0xFFFF00, 0xFF8800, 0xFF0000}, // 暖色系
                new int[]{0xFFFFFF, 0xFFFF88},
                true, true, true,
                3, 2 + new Random().nextInt(2)
        ),

        COOL(
                new int[]{0, 2}, // 小さい玉と星型
                new int[]{0x0000FF, 0x00FFFF, 0x8800FF}, // 寒色系
                new int[]{0xFFFFFF, 0x88FFFF},
                true, false, true,
                2, 1 + new Random().nextInt(2)
        ),

        CREEPER(
                new int[]{3}, // クリーパー型のみ
                new int[]{0x00FF00}, // 緑
                new int[]{0xFFFFFF},
                false, false, false,
                2, 1
        ),

        RAINBOW(
                new int[]{1, 4},
                new int[]{0xFF0000, 0xFF8800, 0xFFFF00, 0x00FF00, 0x0000FF, 0x8800FF},
                new int[]{0xFFFFFF},
                true, true, false,
                2, 3
        );

        final int[] shapes;
        final int[] colors;
        final int[] fadeColors;
        final boolean useFadeColors;
        final boolean trail;
        final boolean flicker;
        final int flightDuration;
        final int explosionCount;

        FireworkTheme(int[] shapes, int[] colors, int[] fadeColors,
                      boolean useFadeColors, boolean trail, boolean flicker,
                      int flightDuration, int explosionCount) {
            this.shapes = shapes;
            this.colors = colors;
            this.fadeColors = fadeColors;
            this.useFadeColors = useFadeColors;
            this.trail = trail;
            this.flicker = flicker;
            this.flightDuration = flightDuration;
            this.explosionCount = explosionCount;
        }
    }
}
