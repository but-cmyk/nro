package models.task;

public class TaskPlayer {

    public TaskMain taskMain;

    public SideTask sideTask;

    public ClanTask clanTask;

    public TaskDanhHieu taskdh;

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
    }

}
