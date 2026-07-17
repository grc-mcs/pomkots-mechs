package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.state;

public enum ArenaMatchState {

    // ゲート集合待ち
    WAIT_FOR_ENTRY("WAIT_FOR_ENTRY"),

    // 自動搬送
    TRANSPORT_TO_ARENA("TRANSPORT_TO_ARENA"),

    // 入場演出
    OPENING("OPENING"),

    // 戦闘中
    BATTLE("BATTLE"),

    // 勝者演出
    VICTORY_CEREMONY("VICTORY_CEREMONY"),

    // 帰還
    RETURNING("RETURNING"),

    // 終了
    FINISHED("FINISHED"),

    // キャンセル
    CANCELLED("CANCELLED");


    private final String id;

    ArenaMatchState(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static ArenaMatchState byId(
            String id
    ) {

        for (ArenaMatchState state : values()) {
            if (state.id.equals(id)) {
                return state;
            }
        }

        return CANCELLED;
    }
}
