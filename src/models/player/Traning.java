package models.player;

public class Traning {

    private int top;
    private int topWhis;
    private int time;
    private long lastTime;
    private int lastTop;
    private long lastRewardTime;

    public Traning() {
    }

    public int getTop() { return top; }
    public void setTop(int top) { this.top = top; }

    public int getTopWhis() { return topWhis; }
    public void setTopWhis(int topWhis) { this.topWhis = topWhis; }

    public int getTime() { return time; }
    public void setTime(int time) { this.time = time; }

    public long getLastTime() { return lastTime; }
    public void setLastTime(long lastTime) { this.lastTime = lastTime; }

    public int getLastTop() { return lastTop; }
    public void setLastTop(int lastTop) { this.lastTop = lastTop; }

    public long getLastRewardTime() { return lastRewardTime; }
    public void setLastRewardTime(long lastRewardTime) { this.lastRewardTime = lastRewardTime; }
}
