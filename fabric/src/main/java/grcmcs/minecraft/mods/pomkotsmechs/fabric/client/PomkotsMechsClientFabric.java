package grcmcs.minecraft.mods.pomkotsmechs.fabric.client;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechsClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;

public final class PomkotsMechsClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PomkotsMechsClient.initialize();

        //TODO 後で消す。MultiPlayerGameModelMixinで解決するようなら消す
//        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
//            if (Utils.isRidingPomkotsMechs(player)) {
//                return InteractionResult.FAIL;
//            }
//            return InteractionResult.PASS;
//        });
//
//        UseItemCallback.EVENT.register((player, world, hand) -> {
//            if (Utils.isRidingPomkotsMechs(player)) {
//                return new InteractionResultHolder<>(InteractionResult.FAIL, player.getItemInHand(hand));
//            }
//            return new InteractionResultHolder<>(InteractionResult.PASS, player.getItemInHand(hand));
//        });
    }
}
