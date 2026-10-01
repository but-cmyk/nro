package models.tournament.whis_arena;

public class WhisArenaBot {

    public int id;
    public String name;
    public byte gender; // 0: Trái Đất, 1: Namek, 2: Xayda
    public short head;
    public short body;
    public short leg;
    public short flagBag;
    public short aura;
    public long hpMax;
    public long mpMax;
    public int dame;
    public int arenaWins; // Điểm xếp hạng võ đài
    public int rank;
    public WhisArenaLobbyBot lobbyBot;
    public boolean isCompeting;

    public WhisArenaBot(int id, String name, byte gender, short head, short body, short leg, 
                        short flagBag, short aura, long hpMax, long mpMax, int dame, int arenaWins) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.head = head;
        this.body = body;
        this.leg = leg;
        this.flagBag = flagBag;
        this.aura = aura;
        this.hpMax = hpMax;
        this.mpMax = mpMax;
        this.dame = dame;
        this.arenaWins = arenaWins;
    }
}
