package com.manridy.iband.bean;



import org.litepal.crud.DataSupport;

public class EcgDataBean extends DataSupport {
    private int id;//主键
    private String ecg_data_id;
    private String ecg_time;
    private String ecg;
    private int rate_aided_signal;

    private String username;


    public int getRate_aided_signal() {
        return rate_aided_signal;
    }

    public void setRate_aided_signal(int rate_aided_signal) {
        this.rate_aided_signal = rate_aided_signal;
    }

    public EcgDataBean(){
//        this.username = HealthApplication.getIntance().getUsername();
        this.username = "1";
    }

    public String getEcg() {
        return ecg;
    }

    public void setEcg(String ecg) {
        this.ecg = ecg;
    }

    public String getEcg_data_id() {
        return ecg_data_id;
    }

    public void setEcg_data_id(String ecg_data_id) {
        this.ecg_data_id = ecg_data_id;
    }

    public String getEcg_time() {
        return ecg_time;
    }

    public void setEcg_time(String ecg_time) {
        this.ecg_time = ecg_time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


}
