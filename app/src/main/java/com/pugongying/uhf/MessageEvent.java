package com.pugongying.uhf;

public class MessageEvent {
    private int type;
    private Object data;

    public MessageEvent(int type) {
        this.type = type;
        this.data = null;
    }

    public MessageEvent(int type, Object data) {
        this.type = type;
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public Object getData() {
        return data;
    }
}
