package models.player;

import models.map.Zone;

public abstract class Character {
    public long id;
    public String name;
    public byte gender;
    public short head;
    
    public Location location;
    public Zone zone;
    
    public NPoint nPoint;
    public EffectSkill effectSkill;

    public abstract boolean isDie();
    public abstract int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack);
}
