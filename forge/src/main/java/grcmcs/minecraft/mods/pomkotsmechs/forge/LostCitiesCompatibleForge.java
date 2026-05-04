package grcmcs.minecraft.mods.pomkotsmechs.forge;

import grcmcs.minecraft.mods.pomkotsmechs.sounds.bgm.BGMState;
import grcmcs.minecraft.mods.pomkotsmechs.sounds.bgm.LostCitiesCompatible;
import mcjty.lostcities.LostCities;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;


public class LostCitiesCompatibleForge extends LostCitiesCompatible {
    @Override
    public BGMState getBGMState(ServerPlayer serverPlayer, BGMState oldState) {
        var lostCitiesImp = LostCities.lostCitiesImp;
        var lostCitiesInfo = lostCitiesImp.getLostInfo(serverPlayer.level());

        if (lostCitiesInfo == null) {
            return null;
        }

        var chunkPos = serverPlayer.chunkPosition();
        var chunkInfo = lostCitiesInfo.getChunkInfo(chunkPos.x, chunkPos.z);

        if (chunkInfo != null) {
            if (chunkInfo.isCity()) {
                // City Infoは街の中心じゃないと常にNullらしいので諦める…
//                PomkotsMechs.LOGGER.info("BuildingType" + chunkInfo.getBuildingType());
//                var cityInfo = chunkInfo.getCityInfo();
//                if (cityInfo != null) {
//                    PomkotsMechs.LOGGER.info("CityStyle" + cityInfo.getCityStyle());
//                }

//                var newState = BGMState.RUIN_CITY;
//                if (oldState != newState) {
//                    showTitlesForPlayers("Ruined City", serverPlayer);
//                }

//                return newState;

                return BGMState.RUIN_CITY;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private void showTitlesForPlayers(String title, ServerPlayer player) {
        // フェードイン 20 ticks, 表示 60 ticks, フェードアウト 20 ticks
        ClientboundSetTitlesAnimationPacket timesPacket =
                new ClientboundSetTitlesAnimationPacket(20, 60, 20);

        // メインタイトル
        ClientboundSetTitleTextPacket titlePacket =
                new ClientboundSetTitleTextPacket(Component.literal(title));

        player.connection.send(timesPacket);
        player.connection.send(titlePacket);
    }
}
