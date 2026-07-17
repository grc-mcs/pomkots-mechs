package grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena;

import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchRuntime;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaFighterData;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaRank;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public class ArenaMatchResultData {

    public static class FighterResult {
        public static final int RESULT_DRAW = 0;
        public static final int RESULT_WIN = 1;
        public static final int RESULT_LOSE = 2;

        private UUID fighterId;

        private String displayName;

        private int resultType;

        // Before
        private int ratingBefore;
        private int winsBefore;
        private int lossesBefore;
        private int drawsBefore;
        private String rankBefore;

        // After
        private int ratingAfter;
        private int winsAfter;
        private int lossesAfter;
        private int drawsAfter;
        private String rankAfter;

        public FighterResult() {
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public int getDrawsAfter() {
            return drawsAfter;
        }

        public void setDrawsAfter(int drawsAfter) {
            this.drawsAfter = drawsAfter;
        }

        public int getDrawsBefore() {
            return drawsBefore;
        }

        public void setDrawsBefore(int drawsBefore) {
            this.drawsBefore = drawsBefore;
        }

        public UUID getFighterId() {
            return fighterId;
        }

        public void setFighterId(UUID fighterId) {
            this.fighterId = fighterId;
        }

        public int getLossesAfter() {
            return lossesAfter;
        }

        public void setLossesAfter(int lossesAfter) {
            this.lossesAfter = lossesAfter;
        }

        public int getLossesBefore() {
            return lossesBefore;
        }

        public void setLossesBefore(int lossesBefore) {
            this.lossesBefore = lossesBefore;
        }

        public int getRatingAfter() {
            return ratingAfter;
        }

        public void setRatingAfter(int ratingAfter) {
            this.ratingAfter = ratingAfter;
        }

        public int getRatingBefore() {
            return ratingBefore;
        }

        public void setRatingBefore(int ratingBefore) {
            this.ratingBefore = ratingBefore;
        }

        public int getWinsAfter() {
            return winsAfter;
        }

        public void setWinsAfter(int winsAfter) {
            this.winsAfter = winsAfter;
        }

        public int getWinsBefore() {
            return winsBefore;
        }

        public void setWinsBefore(int winsBefore) {
            this.winsBefore = winsBefore;
        }

        public String getRankAfter() {
            return rankAfter;
        }

        public void setRankAfter(String rankAfter) {
            this.rankAfter = rankAfter;
        }

        public String getRankBefore() {
            return rankBefore;
        }

        public void setRankBefore(String rankBefore) {
            this.rankBefore = rankBefore;
        }

        public int getResultType() {
            return resultType;
        }

        public void setResultType(int resultType) {
            this.resultType = resultType;
        }
    }

    public static class Entry {
        private FighterResult challengerResult;
        private FighterResult opponentResult;

        public Entry(FighterResult selfResult, FighterResult targetResult) {
            this.challengerResult = selfResult;
            this.opponentResult = targetResult;
        }

        public FighterResult getChallengerResult() {
            return challengerResult;
        }

        public void setChallengerResult(FighterResult challengerResult) {
            this.challengerResult = challengerResult;
        }

        public FighterResult getOpponentResult() {
            return opponentResult;
        }

        public void setOpponentResult(FighterResult opponentResult) {
            this.opponentResult = opponentResult;
        }
    }

    public static void write(
            FriendlyByteBuf buf,
            Entry entry
    ) {

        writeFighterResult(
                buf,
                entry.getChallengerResult()
        );

        writeFighterResult(
                buf,
                entry.getOpponentResult()
        );
    }

    public static Entry read(
            FriendlyByteBuf buf
    ) {

        FighterResult self =
                readFighterResult(
                        buf
                );

        FighterResult target =
                readFighterResult(
                        buf
                );

        return new Entry(
                self,
                target
        );
    }

    private static void writeFighterResult(
            FriendlyByteBuf buf,
            FighterResult result
    ) {

        buf.writeUUID(
                result.getFighterId()
        );

        buf.writeUtf(
                result.getDisplayName()
        );

        buf.writeInt(
                result.getRatingBefore()
        );

        buf.writeInt(
                result.getRatingAfter()
        );

        buf.writeInt(
                result.getWinsBefore()
        );

        buf.writeInt(
                result.getWinsAfter()
        );

        buf.writeInt(
                result.getLossesBefore()
        );

        buf.writeInt(
                result.getLossesAfter()
        );

        buf.writeInt(
                result.getDrawsBefore()
        );

        buf.writeInt(
                result.getDrawsAfter()
        );

        buf.writeUtf(
                result.getRankBefore()
        );

        buf.writeUtf(
                result.getRankAfter()
        );

        buf.writeInt(
                result.getResultType()
        );
    }

    private static FighterResult readFighterResult(
            FriendlyByteBuf buf
    ) {

        FighterResult result = new FighterResult();

        result.setFighterId(
                buf.readUUID()
        );

        result.setDisplayName(
                buf.readUtf()
        );

        result.setRatingBefore(
                buf.readInt()
        );

        result.setRatingAfter(
                buf.readInt()
        );

        result.setWinsBefore(
                buf.readInt()
        );

        result.setWinsAfter(
                buf.readInt()
        );

        result.setLossesBefore(
                buf.readInt()
        );

        result.setLossesAfter(
                buf.readInt()
        );

        result.setDrawsBefore(
                buf.readInt()
        );

        result.setDrawsAfter(
                buf.readInt()
        );

        result.setRankBefore(
                buf.readUtf()
        );

        result.setRankAfter(
                buf.readUtf()
        );

        result.setResultType(
                buf.readInt()
        );

        return result;
    }

    public static ArenaMatchResultData.Entry createBattleResult(
            ArenaFighterData challenger,
            ArenaFighterData opponent,
            ArenaMatchRuntime.BattleResult result
    ) {

        int challengerRatingBefore =
                challenger.getRating();

        int opponentRatingBefore =
                opponent.getRating();

        int challengerRatingAfter =
                challengerRatingBefore;

        int opponentRatingAfter =
                opponentRatingBefore;

        int challengerWinsAfter =
                challenger.getWins();

        int challengerLossesAfter =
                challenger.getLosses();

        int challengerDrawsAfter =
                challenger.getDraws();

        int opponentWinsAfter =
                opponent.getWins();

        int opponentLossesAfter =
                opponent.getLosses();

        int opponentDrawsAfter =
                opponent.getDraws();

        double challengerScore;
        double opponentScore;

        switch (result.getResultType()) {

            case CHALLENGER_WIN -> {

                challengerScore = 1.0;
                opponentScore = 0.0;

                challengerWinsAfter++;
                opponentLossesAfter++;
            }

            case OPPONENT_WIN -> {

                challengerScore = 0.0;
                opponentScore = 1.0;

                challengerLossesAfter++;
                opponentWinsAfter++;
            }

            case DRAW -> {

                challengerScore = 0.5;
                opponentScore = 0.5;

                challengerDrawsAfter++;
                opponentDrawsAfter++;
            }

            default -> throw new IllegalStateException();
        }

        challengerRatingAfter =
                calculateElo(
                        challengerRatingBefore,
                        opponentRatingBefore,
                        challengerScore,
                        getKFactor(challenger)
                );

        opponentRatingAfter =
                calculateElo(
                        opponentRatingBefore,
                        challengerRatingBefore,
                        opponentScore,
                        getKFactor(opponent)
                );

        FighterResult challengerResult =
                new FighterResult();

        challengerResult.setFighterId(
                challenger.getFighterId()
        );

        challengerResult.setDisplayName(
                challenger.getDisplayName()
        );

        challengerResult.setRatingBefore(
                challengerRatingBefore
        );

        challengerResult.setRatingAfter(
                challengerRatingAfter
        );

        challengerResult.setWinsBefore(
                challenger.getWins()
        );

        challengerResult.setWinsAfter(
                challengerWinsAfter
        );

        challengerResult.setLossesBefore(
                challenger.getLosses()
        );

        challengerResult.setLossesAfter(
                challengerLossesAfter
        );

        challengerResult.setDrawsBefore(
                challenger.getDraws()
        );

        challengerResult.setDrawsAfter(
                challengerDrawsAfter
        );

        challengerResult.setRankBefore(
                ArenaRank.getRank(
                        challengerRatingBefore
                ).name()
        );

        challengerResult.setRankAfter(
                ArenaRank.getRank(
                        challengerRatingAfter
                ).name()
        );

        challengerResult.setResultType(
                convertResult(
                        result,
                        challenger.getFighterId()
                )
        );

        FighterResult opponentResult =
                new FighterResult();

        opponentResult.setFighterId(
                opponent.getFighterId()
        );

        opponentResult.setDisplayName(
                opponent.getDisplayName()
        );

        opponentResult.setRatingBefore(
                opponentRatingBefore
        );

        opponentResult.setRatingAfter(
                opponentRatingAfter
        );

        opponentResult.setWinsBefore(
                opponent.getWins()
        );

        opponentResult.setWinsAfter(
                opponentWinsAfter
        );

        opponentResult.setLossesBefore(
                opponent.getLosses()
        );

        opponentResult.setLossesAfter(
                opponentLossesAfter
        );

        opponentResult.setDrawsBefore(
                opponent.getDraws()
        );

        opponentResult.setDrawsAfter(
                opponentDrawsAfter
        );

        opponentResult.setRankBefore(
                ArenaRank.getRank(
                        opponentRatingBefore
                ).name()
        );

        opponentResult.setRankAfter(
                ArenaRank.getRank(
                        opponentRatingAfter
                ).name()
        );

        opponentResult.setResultType(
                convertResult(
                        result,
                        opponent.getFighterId()
                )
        );

        return new ArenaMatchResultData.Entry(
                challengerResult,
                opponentResult
        );
    }

    private static int calculateElo(
            int selfRating,
            int opponentRating,
            double score,
            int k
    ) {

        double expected =
                1.0 /
                        (
                                1.0
                                        + Math.pow(
                                        10.0,
                                        (opponentRating - selfRating)
                                                / 400.0
                                )
                        );

        var res = selfRating
                + (int)Math.round(
                k * (score - expected)
        );

        return Math.max(res, 0);
    }

    private static int getKFactor(
            ArenaFighterData fighter
    ) {

        int totalBattles =
                fighter.getWins()
                        + fighter.getLosses()
                        + fighter.getDraws();

        return totalBattles < 20
                ? 64
                : 32;
    }

    private static int convertResult(
            ArenaMatchRuntime.BattleResult result,
            UUID fighterId
    ) {

        if (result.getResultType()
                == ArenaMatchRuntime.BattleResult.ResultType.DRAW) {
            return FighterResult.RESULT_DRAW;
        }

        if (fighterId.equals(
                result.getWinnerId()
        )) {
            return FighterResult.RESULT_WIN;
        }

        return FighterResult.RESULT_LOSE;
    }
}
