package models.task;

public class TaskPlayer {

    public TaskMain taskMain;

    public SideTask sideTask;

    public ClanTask clanTask;

    public TaskDanhHieu taskdh;

    public boolean isJustDoneMainTask;
    public int pendingNpcSayNpcId = -1;
    public String pendingNpcSayText;

    public TaskPlayer() {
        this.sideTask = new SideTask();
        this.clanTask = new ClanTask();
        this.taskdh = new TaskDanhHieu();
    }

    public void dispose() {
        this.taskMain = null;
        this.sideTask = null;
        this.clanTask = null;
        this.taskdh = null;
        this.isJustDoneMainTask = false;
        this.pendingNpcSayNpcId = -1;
        this.pendingNpcSayText = null;
    }

}
