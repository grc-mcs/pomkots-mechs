package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.state;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaMatchResultData;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchContext;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchProcessor;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchRuntime;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaFighterData;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaRank;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaSavedData;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.util.ArenaUtil;
import grcmcs.minecraft.mods.pomkotsmechs.util.TitleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class FinishedProcessor extends AbstractArenaMatchStateProcessor {

    @Override
    public void onEnter(ArenaMatchContext context) {
        if (context.runtime().getBossBar() != null) {
            context.runtime()
                    .getBossBar()
                    .removeAllPlayers();
        }

        var challengerData = ArenaUtil.getChallengerData(context);
        var opponentData = ArenaUtil.getOpponentData(context);

        ArenaMatchResultData.Entry result = ArenaMatchResultData.createBattleResult(
                challengerData,
                opponentData,
                context.runtime().getResult()
        );

        // SHOW
        if (context.getChallengerEntity() instanceof ServerPlayer sp1) {
            ArenaUtil.openResultScreen(sp1, result);
            addAdvancement(ArenaRank.getRank(result.getChallengerResult().getRankAfter()), sp1);

            if (context.getChallengerMechEntity() instanceof Pmvc01Entity mech1) {
                ArenaUtil.supplyMechConsumables(mech1);
            }
        }

        if (context.getOpponentEntity() instanceof ServerPlayer sp2) {
            ArenaUtil.openResultScreen(sp2, result);
            addAdvancement(ArenaRank.getRank(result.getOpponentResult().getRankAfter()), sp2);

            if (context.getOpponentMechEntity() instanceof Pmvc01Entity mech2) {
                ArenaUtil.supplyMechConsumables(mech2);
            }
        }

        // SAVE
        saveResultToFighter(challengerData, result.getChallengerResult());
        saveResultToFighter(opponentData, result.getOpponentResult());

        context.arena().setActiveMatch(null);
        ArenaSavedData.get(context.server()).setDirty();
    }

    private void addAdvancement(ArenaRank rank, ServerPlayer sp) {
        switch (rank) {
            case D, E -> Utils.completeAdvancement(PomkotsMechs.id("pomkots_mechs/arena_rank_d"), sp);
            case C -> Utils.completeAdvancement(PomkotsMechs.id("pomkots_mechs/arena_rank_c"), sp);
            case B -> Utils.completeAdvancement(PomkotsMechs.id("pomkots_mechs/arena_rank_b"), sp);
            case A -> Utils.completeAdvancement(PomkotsMechs.id("pomkots_mechs/arena_rank_a"), sp);
            case S -> Utils.completeAdvancement(PomkotsMechs.id("pomkots_mechs/arena_rank_s"), sp);
        }
    }

    private void saveResultToFighter(ArenaFighterData fighter, ArenaMatchResultData.FighterResult result) {
        fighter.setDraws(result.getDrawsAfter());
        fighter.setWins(result.getWinsAfter());
        fighter.setLosses(result.getLossesAfter());
        fighter.setRating(result.getRatingAfter());
        fighter.setRank(ArenaRank.getRank(result.getRankAfter()));
    }

    @Override
    public void tick(ArenaMatchContext context) {
    }

    @Override
    public void onExit(ArenaMatchContext context) {

    }

    @Override
    public void onCancel(ArenaMatchContext context) {

    }
}