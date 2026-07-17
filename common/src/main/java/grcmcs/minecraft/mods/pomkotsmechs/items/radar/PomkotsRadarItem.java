package grcmcs.minecraft.mods.pomkotsmechs.items.radar;

import grcmcs.minecraft.mods.pomkotsmechs.client.gui.RadarTargetSelectMenu;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.RadarTargetSelectScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.pilot.PilotMenuProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PomkotsRadarItem extends Item {

    private static final String NBT_TARGETS      = "RadarTargets";
    private static final String NBT_SELECTED_IDX = "RadarSelectedIndex";

    public PomkotsRadarItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
//            Minecraft.getInstance().setScreen(
//                    new RadarTargetSelectScreen(
//                            new RadarTargetSelectMenu(0, player.getInventory()),
//                            player.getInventory(),
//                            Component.literal("Radar Target")
//                    )
//            );

            player.openMenu(
                    new SimpleMenuProvider(
                            RadarTargetSelectMenu::new,
                            Component.literal("Pomkots Radar")
                    )
            );
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    public static void addTarget(ItemStack stack, RadarTarget target) {
        var targets = getTargets(stack);


        for (int i = 0; i < targets.size(); i++) {
            RadarTarget t = targets.get(i);

            if (t.label().equals(target.label())) {
                setSelectedIndex(stack, i);
                return;
            }
        }

        targets.add(target);
        setTargets(stack, targets);
        setSelectedIndex(stack, targets.size() - 1);
    }

    public static List<RadarTarget> getTargets(ItemStack stack) {
        ListTag list = stack.getOrCreateTag().getList(NBT_TARGETS, Tag.TAG_COMPOUND);
        List<RadarTarget> result = new ArrayList<>();

        for (Tag tag : list) {
            result.add(RadarTarget.fromNbt((CompoundTag) tag));
        }
        return result;
    }

    public static void setTargets(ItemStack stack, List<RadarTarget> targets) {
        ListTag list = new ListTag();
        for (RadarTarget t : targets) list.add(RadarTarget.toNbt(t));
        stack.getOrCreateTag().put(NBT_TARGETS, list);
    }

    public static int getSelectedIndex(ItemStack stack) {
        return stack.getOrCreateTag().getInt(NBT_SELECTED_IDX);
    }

    public static void setSelectedIndex(ItemStack stack, int index) {
        stack.getOrCreateTag().putInt(NBT_SELECTED_IDX, index);
    }

    public static Optional<RadarTarget> getActiveTarget(ItemStack stack) {
        var targets = getTargets(stack);
        if (targets.isEmpty()) return Optional.empty();
        int idx = Mth.clamp(getSelectedIndex(stack), 0, targets.size() - 1);
        return Optional.of(targets.get(idx));
    }

    // サーバーside tick処理
    public static void onServerTick(ServerPlayer player) {
        // エンティティの探索は負荷が高そう＆複雑な作りになりそうなので、現時点では一旦作成を保留する

        // メインハンドとオフハンド両方チェック ← ③
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!(stack.getItem() instanceof PomkotsRadarItem)) continue;

            RadarTarget active = PomkotsRadarItem.getActiveTarget(stack).orElse(null);
            if (!(active instanceof RadarTarget.EntityTarget et)) continue;

            // UUIDが未解決のみ対象
            if (et.uuid() != null) continue;

            // 20秒に1回 ← level.getGameTime()で間引き
            if (player.level().getGameTime() % (20 * 20) != 0) continue;

            // ④ディメンションチェック
            ResourceLocation playerDim = player.level().dimension().location();
            if (!playerDim.equals(et.dimension())) continue;

            resolveSelector(player, stack, et);
        }
    }

    private static void resolveSelector(ServerPlayer player, ItemStack stack, RadarTarget.EntityTarget et) {
//        try {
//            EntitySelector selector = new EntitySelectorParser(
//                    new StringReader(et.selector)
//            ).parse();
//
//            CommandSourceStack source = player.createCommandSourceStack()
//                    .withPermission(2);
//
//            List<Entity> entities = selector.findEntities(source);
//            if (entities.isEmpty()) return;
//
//            UUID uuid = entities.get(0).getUUID();
//
//            // NBT更新してクライアントに同期
//            List<RadarTarget> targets = PomkotsRadarItem.getTargets(stack);
//            int idx = PomkotsRadarItem.getSelectedIndex(stack);
//            targets.set(idx, new RadarTarget.EntityTarget(
//                    uuid,
//                    et.selector(),
//                    et.label(),
//                    et.dimension()
//            ));
//            PomkotsRadarItem.setTargets(stack, targets);
//
//            // クライアントへパケット送信
////            sendUUIDToClient(player, uuid, idx);
//
//        } catch (CommandSyntaxException e) {
//            PomkotsMechs.LOGGER.error("Invalid entity selector: {}", et.selector(), e);
//        }
    }
}
