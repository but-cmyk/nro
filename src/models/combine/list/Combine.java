package models.combine.list;

import models.item.Item;
import java.util.ArrayList;
import java.util.List;

public class Combine {

    public long lastTimeCombine;

    public List<Item> itemsCombine;
    public int typeCombine;

    public int getTypeCombine() { return this.typeCombine; }
    public void setTypeCombine(int typeCombine) { this.typeCombine = typeCombine; }

    public int goldCombine;
    public int gemCombine;
    public float ratioCombine;
    public int countDaNangCap;
    public short countDaBaoVe;
    public List<Integer> quantities;

    public Combine() {
        this.itemsCombine = new ArrayList<>();
        this.quantities = new ArrayList<>();
    }

    public void clearItemCombine() {
        this.itemsCombine.clear();
    }

    public void clearParamCombine() {
        this.goldCombine = 0;
        this.gemCombine = 0;
        this.ratioCombine = 0;
        this.countDaNangCap = 0;
        this.countDaBaoVe = 0;
        if (this.quantities != null) {
            this.quantities.clear();
        }
    }

    public void dispose() {
        this.itemsCombine = null;
        if (this.quantities != null) {
            this.quantities.clear();
            this.quantities = null;
        }
    }
}
