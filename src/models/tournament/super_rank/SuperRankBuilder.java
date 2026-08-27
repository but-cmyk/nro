package models.tournament.super_rank;

public class SuperRankBuilder {

    private long id;
    private int rank;
    private long lastPKTime;
    private long lastTimeReward;
    private int ticket;
    private int win;
    private int lose;
    private String info;

    private int head;
    private int body;
    private int leg;
    private String name;

    public SuperRankBuilder() {
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public void setId(int id) { this.id = id; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public long getLastPKTime() { return lastPKTime; }
    public void setLastPKTime(long lastPKTime) { this.lastPKTime = lastPKTime; }

    public long getLastTimeReward() { return lastTimeReward; }
    public void setLastTimeReward(long lastTimeReward) { this.lastTimeReward = lastTimeReward; }

    public int getTicket() { return ticket; }
    public void setTicket(int ticket) { this.ticket = ticket; }

    public int getWin() { return win; }
    public void setWin(int win) { this.win = win; }

    public int getLose() { return lose; }
    public void setLose(int lose) { this.lose = lose; }

    public String getInfo() { return info; }
    public void setInfo(String info) { this.info = info; }

    public int getHead() { return head; }
    public void setHead(int head) { this.head = head; }

    public int getBody() { return body; }
    public void setBody(int body) { this.body = body; }

    public int getLeg() { return leg; }
    public void setLeg(int leg) { this.leg = leg; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public void dispose() {
        name = null;
        info = null;
    }
}