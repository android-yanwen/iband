package com.manridy.iband.bean;

import com.manridy.applib.utils.LogUtil;
import com.manridy.applib.utils.TimeUtil;

import org.litepal.crud.DataSupport;

/**
 * 血压模板
 * 应用于血压数据显示
 * Created by jarLiao on 2016/10/25.
 */

public class BpModel extends DataSupport {
    private int id;//主键
    private String bpDate;//时间
    private String bpDay;//天
    private int bpLength;//血压条数
    private int bpNum;//血压编号
    private int bpHp;//高压
    private int bpLp;//低压
    private int bpHr;//心率
    private long updateDate;//数据更新时间

    public BpModel() {
    }


    public BpModel(String bpDate, String bpDay, int bpHp, int bpLp, int bpHr) {
        this.bpDate = bpDate;
        this.bpDay = bpDay;
        this.bpHp = bpHp;
        this.bpLp = bpLp;
        this.bpHr = bpHr;
    }

    public BpModel(String bpDate, String bpDay, int bpLength, int bpNum, int bpHp, int bpLp, int bpHr) {
        this.bpDate = bpDate;
        this.bpDay = bpDay;
        this.bpLength = bpLength;
        this.bpNum = bpNum;
        this.bpHp = bpHp;
        this.bpLp = bpLp;
        this.bpHr = bpHr;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBpDate() {
        return bpDate;
    }

    public void setBpDate(String bpDate) {
        this.bpDate = bpDate;
    }

    public String getBpDay() {
        return bpDay;
    }

    public void setBpDay(String bpDay) {
        this.bpDay = bpDay;
    }

    public int getBpLength() {
        return bpLength;
    }

    public void setBpLength(int bpLength) {
        this.bpLength = bpLength;
    }

    public int getBpNum() {
        return bpNum;
    }

    public void setBpNum(int bpNum) {
        this.bpNum = bpNum;
    }

    public int getBpHp() {
        return bpHp;
    }

    public void setBpHp(int bpHp) {
        this.bpHp = bpHp;
    }

    public int getBpLp() {
        return bpLp;
    }

    public void setBpLp(int bpLp) {
        this.bpLp = bpLp;
    }

    public int getBpHr() {
        return bpHr;
    }

    public void setBpHr(int bpHr) {
        this.bpHr = bpHr;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }

    public void saveToDate(){
        this.setUpdateDate(System.currentTimeMillis());
        save();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("BpModel{");
        sb.append("id=").append(id);
        sb.append(", bpDate='").append(bpDate).append('\'');
        sb.append(", bpDay='").append(bpDay).append('\'');
        sb.append(", bpLength=").append(bpLength);
        sb.append(", bpNum=").append(bpNum);
        sb.append(", bpHp=").append(bpHp);
        sb.append(", bpLp=").append(bpLp);
        sb.append(", bpHr=").append(bpHr);
        sb.append(", updateDate=").append(TimeUtil.getNowYMDHMSTime(updateDate));
        sb.append('}');
        return sb.toString();
    }



//    public boolean equalsValues(BpModel bpModel){
//        boolean flag = true;
//        if(!getBpDate().equals(bpModel.getBpDate())){
//            return false;
//        }
//        if(!getBpDay().equals(bpModel.getBpDay())){
//            return false;
//        }
//        if(getBpHp()!=bpModel.getBpHp()){
//            return false;
//        }
//        if(getBpHr()!=bpModel.getBpHr()){
//            return false;
//        }
//        if(get){
//
//        }
//        getBpLength();
//        getBpLp();
//        getBpNum();
//        getBpDay();
//        getBpDate();
//        getUpdateDate();
//    }
}
