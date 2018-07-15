package com.pugongying.uhf;

import com.alibaba.fastjson.JSONObject;
import com.uhf.uhf.Common.InventoryBuffer;

public class ScanEntity {
    private InventoryBuffer.InventoryTagMap map;
    private JSONObject obj;
    private boolean show;

    public ScanEntity(InventoryBuffer.InventoryTagMap map) {
        this.map = map;
        this.show = false;
    }

    public ScanEntity(InventoryBuffer.InventoryTagMap map, JSONObject obj) {
        this.map = map;
        this.obj = obj;
        this.show = false;
    }

    public ScanEntity(InventoryBuffer.InventoryTagMap map, JSONObject obj, boolean show) {
        this.map = map;
        this.obj = obj;
        this.show = show;
    }

    public InventoryBuffer.InventoryTagMap getMap() {
        return map;
    }

    public void setMap(InventoryBuffer.InventoryTagMap map) {
        this.map = map;
    }

    public JSONObject getObj() {
        return obj;
    }

    public void setObj(JSONObject obj) {
        this.obj = obj;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }
}
