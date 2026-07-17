package grcmcs.minecraft.mods.pomkotsmechs.items.coin;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;


public class PomCoinSilverItem extends AbstractPomCoinItem {
    public PomCoinSilverItem(Properties properties) {
        super(properties);
    }

    @Override
    protected @Nullable Item getLowerCoin() {
        return PomkotsMechs.POM_COIN.get();
    }

    @Override
    protected @Nullable Item getHigherCoin() {
        return PomkotsMechs.POM_COIN_GOLD.get();
    }
}
