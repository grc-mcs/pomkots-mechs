package grcmcs.minecraft.mods.pomkotsmechs.items.coin;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractPomCoinItem extends Item {

    protected AbstractPomCoinItem(Properties properties) {
        super(properties);
    }

    /**
     * 1枚消費して64枚入手する先
     */
    @Nullable
    protected abstract Item getLowerCoin();

    /**
     * 64枚消費して1枚入手する先
     */
    @Nullable
    protected abstract Item getHigherCoin();

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            @NotNull InteractionHand hand
    ) {
        ItemStack stack =
                player.getItemInHand(hand);

        if (!level.isClientSide()) {

            if (player.isShiftKeyDown()) {
                exchangeUp(
                        (ServerPlayer) player,
                        stack
                );
            } else {
                exchangeDown(
                        (ServerPlayer) player,
                        stack
                );
            }
        }

        return InteractionResultHolder.sidedSuccess(
                stack,
                level.isClientSide()
        );
    }

    private void exchangeUp(
            ServerPlayer player,
            ItemStack stack
    ) {
        Item higherCoin = getHigherCoin();

        if (higherCoin == null) {
            return;
        }

        if (stack.getCount() < 64) {
            return;
        }

        ItemStack result =
                new ItemStack(
                        higherCoin,
                        1
                );

        if (!player.getInventory().add(
                result.copy()
        )) {

            player.displayClientMessage(
                    Component.translatable(
                            "text.pomkotsmechs.messages.inventory_full"
                    ),
                    true
            );

            return;
        }

        stack.shrink(64);

        playExchangeSound(player);
    }

    private void exchangeDown(
            ServerPlayer player,
            ItemStack stack
    ) {
        Item lowerCoin = getLowerCoin();

        if (lowerCoin == null) {
            return;
        }

        ItemStack result =
                new ItemStack(
                        lowerCoin,
                        64
                );

        if (!player.getInventory().add(
                result.copy()
        )) {
            player.displayClientMessage(
                    Component.translatable(
                            "text.pomkotsmechs.messages.inventory_full"
                    ),
                    true
            );

            return;
        }

        stack.shrink(1);

        playExchangeSound(player);
    }

    protected void playExchangeSound(
            ServerPlayer player
    ) {
        player.level().playSound(
                null,
                player.blockPosition(),
                PomkotsMechs.SE_COIN.get(),
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );
    }

    @Override
    public void appendHoverText(
            @NotNull ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            @NotNull TooltipFlag flag
    ) {
        tooltip.add(
                Component.translatable(
                        "tooltip.pomkotsmechs.pom_coin_1"
                )
        );
        tooltip.add(
                Component.translatable(
                        "tooltip.pomkotsmechs.pom_coin_2"
                )
        );
    }
}
