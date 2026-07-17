package grcmcs.minecraft.mods.pomkotsmechs.items.coin;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PomCoinItem extends AbstractPomCoinItem {
    public PomCoinItem(Properties properties) {
        super(properties);
    }

    @Override
    protected @Nullable Item getLowerCoin() {
        return null;
    }

    @Override
    protected @Nullable Item getHigherCoin() {
        return PomkotsMechs.POM_COIN_SILVER.get();
    }
}
