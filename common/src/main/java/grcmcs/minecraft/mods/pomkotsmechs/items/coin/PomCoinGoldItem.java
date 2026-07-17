package grcmcs.minecraft.mods.pomkotsmechs.items.coin;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;


public class PomCoinGoldItem extends AbstractPomCoinItem {
    public PomCoinGoldItem(Properties properties) {
        super(properties);
    }

    @Override
    protected @Nullable Item getLowerCoin() {
        return PomkotsMechs.POM_COIN_SILVER.get();
    }

    @Override
    protected @Nullable Item getHigherCoin() {
        return null;
    }
}
