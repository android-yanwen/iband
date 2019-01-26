package com.manridy.iband.common;



/**
 * Event消息模板
 * Created by jarLiao.
 */

public  class EventMessage {
    public static final int MSG_WHAT_UPDATE_PROGRESS = 1;
    public static final int MSG_WHAT_UPDATE_SUCCESS = 4;
    public static final int MSG_WHAT_UPDATE_FAIL = 0;
//    public static final int MSG_WHAT_FILE_TOTAL_SIZE = 2;
    //    public static final int MSG_WHAT_FILE_LOADED = 4;
//    public static final int MSG_WHAT_FILE_LOAD = 5;
    public static final int MSG_WHAT_UPDATE_CMD_VIEW = 3;
    private int what;//消息id
    private String msg;//消息内容
    private Object object;//传递数据

    public EventMessage(int what) {
        this.what = what;
    }

    public EventMessage(String msg) {
        this.msg = msg;
    }

    public EventMessage(int what, String msg) {
        this.what = what;
        this.msg = msg;
    }

    public EventMessage(int what, Object object) {
        this.what = what;
        this.object = object;
    }

    public EventMessage(String msg, Object object) {
        this.msg = msg;
        this.object = object;
    }

    public EventMessage(int what, String msg, Object object) {
        this.what = what;
        this.msg = msg;
        this.object = object;
    }

    public int getWhat() {
        return what;
    }

    public void setWhat(int what) {
        this.what = what;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}