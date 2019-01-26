package com.manridy.iband.bean;

public class MRDServerRequestBean {

    /**
     * status : 1
     * code : 10001
     * msg : 存储成功！
     */

    private int status;
    private String code;
    private String msg;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
