package com.manridy.sdk.ble;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.manridy.sdk.Watch;
import com.manridy.sdk.bean.BloodOxygen;
import com.manridy.sdk.bean.BloodPressure;
import com.manridy.sdk.bean.Clock;
import com.manridy.sdk.bean.Ecg;
import com.manridy.sdk.bean.Fatigue;
import com.manridy.sdk.bean.Gps;
import com.manridy.sdk.bean.Heart;
import com.manridy.sdk.bean.Microcirculation;
import com.manridy.sdk.bean.Sleep;
import com.manridy.sdk.bean.Sport;
import com.manridy.sdk.bean.User;
import com.manridy.sdk.bean.View;
import com.manridy.sdk.callback.BleActionListener;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.callback.BleHistoryListener;
import com.manridy.sdk.callback.BleNotifyListener;
import com.manridy.sdk.common.BitUtil;
import com.manridy.sdk.common.LogUtil;
import com.manridy.sdk.common.TimeUtil;
import com.manridy.sdk.type.InfoType;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.TypeInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;


/**
 * 蓝牙数据解析
 */
public class BleParse {
    private final String TAG = BleParse.class.getSimpleName();
    private final byte CALL_TIME = 0x0;
    private final byte CALL_CLOCK = 0x1;
    private final byte CALL_SHAKE = 0x2;
    private final byte CALL_SPORT = 0x3;
    private final byte CALL_CLEAR_SPORT = 0x4;
    private final byte CALL_GPS = 0x5;
    private final byte CALL_USER = 0x6;
    private final byte CALL_SPORT_TARGET = 0x7;
    private final byte CALL_INFO_ALERT = 0x8;
    private final byte CALL_HEART_RATE_TEST = 0x9;
    private final byte CALL_HEART_RATE = 0x0A;
    private final byte CALL_SLEEP_INFO = 0x0C;
    private final byte NOTIFY_GPS = 0x0D;
    private final byte CALL_FIRMWARE_VERSION = 0x0F;
    private final byte  CALL_FIND_OR_LOST = 0x10;
    private final byte CALL_BLOOD_PRESSURE = 0x11;
    private final byte CALL_BLOOD_OXYGEN = 0x12;
    private final byte CALL_DOUBLE_ONOFF = 0x13;
    private final byte CALL_FACTORY_TEST = 0x14;
    private final byte CALL_PALMING = 0x15;
    private final byte CALL_SEDENTARY_ALERT = 0x16;
    private final byte CALL_ECG_HEART_RATE = 0x41;

    private final byte CALL_DO_NOT_DISTURB = 0x2e; //免打扰消息命令
    private final byte CALL_FATIGUE = 0x31; //疲劳度
    private final byte CALL_MICRO_TEST = 0x32; //微循环测量
    public static final byte CALL_MICRO_INFO = 0x33; //微循环信息

    private static BleParse instance;
    private byte[] data;//蓝牙数据
    private byte head;//头部 消息类型
    private byte[] body = new byte[18];//消息体
    private byte crc;//校验码
    private int infoType;//消息类型
    private Gson gson;

    private BleNotifyListener sportNotifyListener;
    private BleNotifyListener sleepNotifyListener;
    private BleNotifyListener hrNotifyListener;
    private BleNotifyListener microNotifyListener;
    private BleNotifyListener fatigueNotifyListener;
    private BleNotifyListener ecgHrNotifyListener;
    private BleNotifyListener bpNotifyListener;
    private BleNotifyListener boNotifyListener;
    private BleNotifyListener stepNotifyListener;
    private BleNotifyListener runNotifyListener;
    private BleNotifyListener sleepStatsNotifyListener;
    private BleNotifyListener hrCorrectingNotifyListener;
    private BleNotifyListener ecgNotifyListener;
    private BleNotifyListener hrBaseLineListener;

    private BleHistoryListener stepHistoryListener;
    private BleHistoryListener sleepHistoryListener;
    private BleHistoryListener hrHistoryListener;
    private BleHistoryListener microHistoryListener;
    private BleHistoryListener bpHistoryListener;
    private BleHistoryListener boHistoryListener;
    private BleHistoryListener runHistoryListener;

    private BleActionListener actionListener;

    private BleCallback timingHrTestListener;



    public synchronized static BleParse getInstance() {
        if (instance == null) {
            instance = new BleParse();
        }
        return instance;
    }

    private BleParse(){
        gson = new Gson();
    }



    BleCallback bleCallback;
    //传入数据与结果回调
    public void setBleParseData( byte[] data,BleCallback bleCallback) {
//        if(this.data!=null){
//            if(Arrays.equals(this.data,data)){
//                return;
//            }
//        }
        this.data = data;
        this.bleCallback = bleCallback;
        bodyParse();
    }


    //判断效验码
    private boolean crcCheck(){
        crc = data[data.length-1];
        if (crc == 0) {
            return true;
        }
        return false;
    }

    //解析头部
    private boolean headCheck(){
        boolean suc = false;
        head = data[0];
        byte[] bits= BitUtil.getBitArray(head);//得到头部的8个bit
        if (bits[0] == 0) {//最高位为0代表成功
            suc = true;
        }else if (bits[0] == 1){//为1代表消息错误
            suc = false;
        }
        infoType = BitUtil.getInfoType(bits);//剩余7个位为消息类型
        Log.i(TAG,""+suc+":"+infoType+":"+Integer.toHexString(head)+":"+Integer.toBinaryString(head));
        return suc;
    }

    //解析消息体
    private synchronized void bodyParse(){
        System.arraycopy(data, 1, body, 0, body.length);//从第2个字节到19字节为消息体
//        Log.i(TAG, "head: "+ head);
        if (headCheck()) {
            String result = "";
//            Log.i(TAG, "infoType: "+ infoType);
            switch (infoType) {
                case CALL_TIME://时间
                    result = parseTime();
                    break;
                case CALL_CLOCK://闹钟
                    result = parseClock();
                    break;
                case CALL_SHAKE://震动
                    result = String.valueOf(body[0] == 0 ? false :true);
                    break;
                case CALL_SPORT://计步
                    result = parseStep();
                    break;
                case CALL_CLEAR_SPORT://清除运动
                    result = String.valueOf(true);
                    break;
                case CALL_GPS://gps
                    result = parseGPS();
                    break;
                case CALL_USER://用户
                    result = parseUser();
                    break;
                case CALL_SPORT_TARGET://运动目标
                    result = parseSportTarget();
                    break;
                case CALL_INFO_ALERT://消息提醒
                    result = String.valueOf(true);
                    break;
                case CALL_HEART_RATE_TEST://心率测量
                    result = String.valueOf(true);
                    break;
                case CALL_HEART_RATE://心率
                    result = parseHr();
                    break;
                case CALL_SLEEP_INFO://睡眠
                    result = parseSleep();
                    break;
                case NOTIFY_GPS://睡眠上报
                    result = parseSleep();
                    break;
                case CALL_FIRMWARE_VERSION://固件版本
                    result = parseFirmware();
                    break;
                case CALL_FIND_OR_LOST://查找/丢失
                    result = String.valueOf(true);
                    if (actionListener != null) {//停止查找腕表
                        actionListener.onAction(1602,null);
                    }
                    break;
                case CALL_BLOOD_PRESSURE://血压
                    result = parseBp();
                    break;
                case CALL_BLOOD_OXYGEN://血氧
                    result = parseBo();
                    break;
                case CALL_DOUBLE_ONOFF://双击开关
                    break;
                case CALL_FACTORY_TEST://工厂测试
                    break;
                case CALL_PALMING://翻腕亮屏
                    break;
                case CALL_SEDENTARY_ALERT://久坐提醒
                    result = String.valueOf(true);
                    break;
                case 0x1C:
                    int type = body[0];//1 查询窗口数量 2 窗口状态设置/查询 4 子窗口查询
                    if (type == 4) {
                        result = parseWindowChild();
                    }
                    break;
                case 0x1A://分段计步
                    result = parseStepSection(body);
                    break;
                case 0x1B://训练模式协议头
                    result = parseSport();
                    break;
                case 0x23:
                    result = parseStatsSleep();
                    break;
                case 0x22:
                    if(timingHrTestListener!=null){
                        timingHrTestListener.onSuccess(null);
                    }
                    String datas = "";
                    for (int i = 0;i<data.length;i++){
                        datas+="["+i+"]:"+data[i];
                    }
                    Log.i("sendCmd:re","sendCmd:"+datas);
                    break;
                case 0x29:
                    String datas29 = "";
                    for (int i = 0;i<data.length;i++){
                        datas29+="["+i+"]:"+Integer.parseInt(""+data[i], 10);
                    }
                    Log.i("0x29sendCmd:re","sendCmd:"+datas29);
                    break;
                case 0x41:
                    result = parseEcgData();
                    break;
                case 0x42:
                    result = parseHrBaseLine();
                    break;
                case CALL_DO_NOT_DISTURB:  //免打扰
                    result = BitUtil.parseByte2HexStr(data);
                    break;
                case CALL_FATIGUE:  //疲劳度
                    result = parseFatigue();
                    break;
                case CALL_MICRO_TEST:
                    result = parseMicroTest(data);
                    if (actionListener != null) {
                        actionListener.onAction(9200,data); //ACTION_MICRO_TESTED=9200
                    }
                    break;
                case CALL_MICRO_INFO:
                    result = parseMicro();
                    break;
            }
            if (bleCallback == null) {
                return;
            }
            bleCallback.onSuccess(result);
            bleCallback = null;
        }else {
//            if (bleCallback == null) return;
//            bleCallback.onFailure(new BleException(ERROR_CODE_PARSE,"解析数据失败"));
            switch (infoType){
                case 0x22:
                    if(timingHrTestListener!=null){
                        timingHrTestListener.onFailure(null);
                    }
                    String datas = "";
                    for (int i = 0;i<data.length;i++){
                        datas+="["+i+"]:"+data[i];
                    }
                    Log.i("sendCmd:re","sendCmd:"+datas);
                    break;
                case 0x29:
                    String datas29 = "";
                    for (int i = 0;i<data.length;i++){
                        datas29+="["+i+"]:"+Integer.parseInt(""+data[i], 10);
                    }
                    Log.i("0x29sendCmd:re","false:"+datas29);
                    break;
            }
            parseOther(data);
        }

    }


    private String parseEcgData(){
        String result= "";
        int ty = body[0];
        if(ty == 3){
           result = parseEcgHr();
        }else{
           result = parseEcg();
        }
        return result;
    }

    private String parseEcg() {
        String result= "";
        int userId = body[0]&0x0f;
        int dataPackage = body[0]&0xf0;
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            int rx = (data[i*2] << 8 & 0xff00) | (data[i*2+1] & 0xff);
            list.add(rx);
        }
        Ecg ecg = new Ecg(userId,dataPackage,list);
        result = gson.toJson(ecg);
        if (ecgNotifyListener != null) {
            ecgNotifyListener.onNotify(result);
        }
        Log.i("parseEcg()",result);
        return result;
    }

    private String parseEcgHr() {
        String result;
        int ty = body[0];//心率操作状态 0最近一次 1历史心率
        int hr = body[11]&0xff;//心率
        byte[] hrBLength = new byte[2];
        byte[] hrBNum = new byte[2];
        byte[] hrBDates = new byte[6];
        //拷贝字节
        System.arraycopy(body,1,hrBLength,0,hrBLength.length);
        System.arraycopy(body,3,hrBNum,0,hrBNum.length);
        System.arraycopy(body,5,hrBDates,0,hrBDates.length);
        String hrDate = BitUtil.bytesToDate(hrBDates,0);//得到时间
        String hrDay = BitUtil.bytesToDate(hrBDates,4);
        int hrLength = BitUtil.byte3ToInt(hrBLength);//得到总包数
        int hrNum = BitUtil.byte3ToInt(hrBNum);//得到包编号
        //数据填充模板
        Heart heart = new Heart(hrDate,hrDay,hrLength,hrNum,hr);
        result = gson.toJson(heart);

        if (ty == 3){
            if (bleCallback == null) {
                if (ecgHrNotifyListener != null) {
                    ecgHrNotifyListener.onNotify(result);
                }
            }
        }

        //打印日志
        LogUtil.i(TAG,"ecg心率数据："+ result);
        return result;
    }


    private String parseHrBaseLine() {
        byte[] hrBaseLines = new byte[2];
        System.arraycopy(body,1,hrBaseLines,0,hrBaseLines.length);
        int hrBaseLine = BitUtil.byte3ToInt(hrBaseLines);
        String result = "";
//        int THI = body[1];
//        int TLI = body[2];
        result = result + hrBaseLine;
//        Log.i("parseHrBaseLine()","THI:"+THI+"TLI:"+TLI+"hrBaseLine:"+result);
        if(hrBaseLineListener != null) {
            hrBaseLineListener.onNotify(result);
        }
        return result;
    }

    public void parseOther(byte[] data){
        int type = data[1];
        switch (type) {
            case 16:
                int tt = data[3];
                if (tt>0) {
                    if (actionListener != null) {//开始查找手机
                        actionListener.onAction(1601,null);
                    }
                }else {
                    if (actionListener != null) {//停止查找手机
                        actionListener.onAction(1600,null);
                    }
                }
                break;
            case 25:
                if (data[2] == (byte) 0x80) {//结束拍照
                    if (actionListener != null) {
                        actionListener.onAction(2580,null);
                    }
                }else if (data[3] == (byte) 0x81){//开始拍照
                    if (actionListener != null) {
                        actionListener.onAction(2581,null);
                    }
                }
                break;
            case 9:  //心率定时测量完成fc 09 00
                int ss = data[3];//测试类型
                if (data[2] == 0) {
                    if (actionListener != null) {
                        actionListener.onAction(900,ss);
                    }
                }else if (data[2] == 2){
                    if (actionListener != null) {
                        actionListener.onAction(902,ss);
                    }
                }
//                Log.d(TAG, "parseOther: --------------------------------------------------->>>>>>>>>"+BitUtil.parseByte2HexStr(data));
                break;
            case 8:
                if (data[2] == 3 && data[3] == 0) {
                    if (actionListener != null) {
                        actionListener.onAction(830,null);
                    }
                }else if (data[2] == 3 && data[3] == 1) {
                    if (actionListener != null) {
                        actionListener.onAction(831, null);
                    }
                }
                break;

        }
    }

    private String parseWindowChild() {
        int dataLength = body[1];//数据包数量
        int dataNum = body[2];//数据编号
        byte[] dataStates = new byte[2];
        byte[] datas = new byte[13];
        System.arraycopy(body,3,dataStates,0,dataStates.length);
        System.arraycopy(body,5,datas,0,datas.length);
        String str = BitUtil.bitToString(dataStates);
        List<View> viewList = new ArrayList<>();
        for (int i = 0; i < datas.length; i++) {
            int invalid = datas[i] >> 7;
            if (invalid == -1) {
                char display = str.charAt((str.length()-1-i));
                int id = datas[i] & 0x7F;
                View view =new View(id,display == '1');
                viewList.add(view);
            }
        }
        String result = gson.toJson(viewList);
        return result;
    }


    @NonNull
    private String parseTime() {
        byte[] dates = new byte[6];
        System.arraycopy(body,0,dates,0,dates.length);//拷贝时间字节
        String date = BitUtil.bytesToDate(dates,0);//字节转化string
        int week = body[6];//星期标识
        String result = date + TimeUtil.week(week);
        LogUtil.i(TAG,"设置时间："+result);
        return result;
    }

    private String parseClock() {
        //得到闹钟开关数据
        int en = body[0];//闹钟操作状态 0代表设置返回 1代表查询返回
        int[] onOffs = new int[5];//得到5组闹钟状态数据 0取消闹钟 1设打开闹钟 2关闭闹钟
        for (int i = 0; i < onOffs.length; i++) {
            onOffs[i] = body[i+1];
        }
        //得到闹钟数据
        String[] clocks = new String[5];//得到5组闹钟数据
        for (int i = 0; i < clocks.length; i++) {
            clocks[i] = BitUtil.byteBcd2Str(body[(2*i)+6])+":"+BitUtil.byteBcd2Str(body[(2*i)+7]);
        }
        //数据填充模型
        List<Clock> clockList  = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            boolean onoff = false;
            if (onOffs[i] == 1) {//闹钟开启状态
                onoff = true;
                clockList.add(new Clock(clocks[i],onoff));
            }else if (onOffs[i] == 2){//闹钟关闭状态
                onoff = false;
                clockList.add(new Clock(clocks[i],onoff));
            }else {//闹钟取消

            }
        }
        String result = gson.toJson(clockList);
        LogUtil.i(TAG,"闹钟数据："+ result);
        return result;
    }

    private String parseStep() {
        byte En = body[0];
        Sport sport = new Sport();
        if (En == (byte)0x80) {
            sport.setHisLength(body[1]&0xff);
            LogUtil.e(TAG, "计步历史条数：" + sport.toString());
        }else if (En == (byte) 0xC0){
            byte[] hisDay = new byte[3];
            byte[] hisNum = new byte[3];
            int hisLength = body[1];
            int hisCount = body[2];
            System.arraycopy(body,3,hisDay,0,hisDay.length);
            System.arraycopy(body,8,hisNum,0,hisNum.length);
            String day = BitUtil.bytesToDate(hisDay,4);
            int num = BitUtil.byte3ToInt(hisNum);
            sport.setHisLength(hisLength);
            sport.setHisCount(hisCount);
            sport.setStepNum(num);
            sport.setStepDay(day);
            LogUtil.e(TAG, "计步历史数据：" + sport.toString());
        }else {
            int[] datas = new int[3];//创建运动数据数组
            for (int i = 1; i < 4; i++) {
                byte[] bs = new byte[3];//每个运动数据3个字节
                System.arraycopy(body, (3 * i) - 2, bs, 0, bs.length);//从body拷贝字节
                datas[i - 1] = BitUtil.byte3ToInt(bs);//解析3个字节代表的数据
            }
            //数据填充模板
            sport.setStepDate(new Date());
            sport.setStepDay(TimeUtil.getYMD(new Date()));
            sport.setStepNum(datas[0]);
            sport.setStepMileage(datas[1]);
            sport.setStepCalorie(datas[2]);
            //打印数据
            LogUtil.e(TAG, "计步数据：" + sport.toString());
        }
        String result = gson.toJson(sport);
        if (En == 0x07) {
            if (bleCallback == null) {
                if (sportNotifyListener != null) {
                    sportNotifyListener.onNotify(result);
                }
            }
        }
        //打印数据
        LogUtil.i(TAG,"运动数据："+ result);
        return result;
    }

    private String parseGPS() {
        Gps gps = new Gps();
        byte[] length = new byte[2];//Gps总包数
        byte[] num = new byte[2];//编号
        byte[] bState = new byte[6];//位置状态 0起点 1过程 2终点
        byte[] gDates = new byte[5];//时间
        byte[] gLon = new byte[4];//进度
        byte[] gLat = new byte[4];//纬度
        byte[] bits = BitUtil.getBitArray(body[4]);//SS字节
        byte[] gDay = new byte[2];//日期
        //拷贝字节
        System.arraycopy(body,0,length,0,length.length);
        System.arraycopy(body,2,num,0,num.length);
        System.arraycopy(body,5,gDates,0,gDates.length);
        System.arraycopy(body,10,gLat,0,gLat.length);
        System.arraycopy(body,14,gLon,0,gLon.length);
        System.arraycopy(bits,2,bState,0,bState.length);
        System.arraycopy(body,5,gDay,0,gDay.length);
        int state =BitUtil.bitsToInt(bState);//得到位置状态
        int lonType = bits[0];//经度类型 0东经 1西经
        int latType = bits[1];//纬度类型 0北纬 1南纬
        int len = BitUtil.byte3ToInt(length);//得到总包数
        int nu = BitUtil.byte3ToInt(num);//得到数据包编号
        String sDate = BitUtil.bytesToDate(gDates,2);//得到记录时间
        String sDay = BitUtil.bytesToDate(gDates,3);//得到记录时间
        float flat = BitUtil.byte2float(gLat,0);//得到纬度
        float flon = BitUtil.byte2float(gLon,0);//得到进度
        gps.setGpsDate(sDate);
        gps.setDay(sDay);
        gps.setMapLat(flat);
        gps.setMapLong(flon);
        gps.setMapState(state);
        gps.setMapLat(latType);
        gps.setMapLongType(lonType);
        gps.setMapLength(len);
        gps.setMapNum(nu);
        String result = gson.toJson(gps,Gps.class);
        LogUtil.i(TAG,"Gps数据："+ result.toString());
        return result;
    }

    private String parseUser() {
        int weight = Integer.valueOf(BitUtil.bitToString(body[0]),2);
        int height = Integer.valueOf(BitUtil.bitToString(body[1]),2);
        User user = new User(height+"",weight+"");
        String result = gson.toJson(user,User.class);
        LogUtil.i(TAG,"身体数据："+ result.toString());
        return result;
    }

    private String parseSportTarget() {
        byte[] bs = new byte[3];
        System.arraycopy(body,0,bs,0,bs.length);
        int target = BitUtil.byte3ToInt(bs);
        String result = putIntJson("target",target);
        LogUtil.i(TAG,"步数目标："+ result);
        return result;
    }



    private String parseHr() {
        String result;
        int ty = body[0];//心率操作状态 0最近一次 1历史心率
        int hr = body[11]&0xff;//心率
        byte[] hrBLength = new byte[2];
        byte[] hrBNum = new byte[2];
        byte[] hrBDates = new byte[6];
        //拷贝字节
        System.arraycopy(body,1,hrBLength,0,hrBLength.length);
        System.arraycopy(body,3,hrBNum,0,hrBNum.length);
        System.arraycopy(body,5,hrBDates,0,hrBDates.length);
        String hrDate = BitUtil.bytesToDate(hrBDates,0);//得到时间
        String hrDay = BitUtil.bytesToDate(hrBDates,4);
        int hrLength = BitUtil.byte3ToInt(hrBLength);//得到总包数
        int hrNum = BitUtil.byte3ToInt(hrBNum);//得到包编号
        //数据填充模板
        Heart heart = new Heart(hrDate,hrDay,hrLength,hrNum,hr);
        result = gson.toJson(heart);

        if (ty == 3){
            if (bleCallback == null) {
                if (hrNotifyListener != null) {
                    hrNotifyListener.onNotify(result);
                }
            }
        }else if (ty == 1){
            if (hrHistoryListener != null) {
                hrHistoryListener.onHistory(result);
            }
        }

        //打印日志
        LogUtil.i(TAG,"心率数据："+ result);
        return result;
    }

    /**
     * 解析微循环
     * */
    private String parseMicro() {
        String result;
        int ty = body[0];//
        byte[] microLen = new byte[2];
        System.arraycopy(body, 1, microLen, 0, microLen.length);//取出微循环数据条数
        byte[] microNum = new byte[2];
        System.arraycopy(body, 3, microNum, 0, microNum.length);//取出微循环数据编号
        byte[] bsDate = new byte[4];
        System.arraycopy(body, 5, bsDate, 0, bsDate.length);//取出时间戳4个字节
        byte[] microData = new byte[4];
        System.arraycopy(body, 9, microData, 0, microData.length);//取出微循环数据4个字节
        int i_micro = ((0xff000000 & (microData[3] << 24)) | (0x00ff0000 & (microData[2] << 16)) |
                (0x0000ff00 & (microData[1] << 8)) | (microData[0] & 0x000000ff));
        float f_micro = Float.intBitsToFloat(i_micro);
        float micro = (float) Math.round(f_micro * 10000) / 10000;//保留4位小数
//        Log.d(TAG, "parseMicro: " + micro);
        long date = BitUtil.bytesToLong(bsDate)*1000;//转换为毫秒
        date -= TimeZone.getDefault().getRawOffset();//减去时区
        Microcirculation microcirculation = new Microcirculation();
        String data = TimeUtil.stampToDate(date, "yyyy-MM-dd HH:mm:ss");
//        String day = TimeUtil.stampToDate(date, "HH:mm:ss");
        String day = TimeUtil.stampToDate(date, "yyyy-MM-dd");
//        Log.i(TAG, "parseMicro data: " + data);
        microcirculation.setType(CALL_MICRO_INFO);
        microcirculation.setTr(ty);
        microcirculation.setDate(data);
        microcirculation.setDay(day);
        microcirculation.setMicro(micro);
        int i_microLen = BitUtil.byte3ToInt(microLen);
        microcirculation.setMicroLength(i_microLen);
        int i_microNum = BitUtil.byte3ToInt(microNum);//得到包编号
        microcirculation.setMicroNum(i_microNum);
        result = gson.toJson(microcirculation);
        if (ty == 0){
            if (bleCallback == null) {
                if (microNotifyListener != null) {
                    microNotifyListener.onNotify(result);
                }
            }
        } else if (1 == ty) {
            if (microHistoryListener != null) {
                microHistoryListener.onHistory(result);
            }
        } else if (2 == ty) {
//            Log.d(TAG, "parseMicro: ty=========================================" + ty);
        }
        LogUtil.i(TAG,"微循环数据："+ result);
        return result;
    }

    /**
     * 解析微循环测量
     * 获取微循环测量状态
     * ty: 00 不能正常开始测量
     *     01 设备正常进入微循环测试模式
     *     03 测量结束
     * */
    private String parseMicroTest(byte[] data) {
        String result="";
//        int ty = body[0];//
        result = BitUtil.parseByte2HexStr(data);
        return result;
    }
    /**
     * 解析疲劳度
     * */
    private String parseFatigue() {
        String result;
        int ty = body[0];//获取疲劳状态
        int fa = body[1];//疲劳状态
        Fatigue fatigue = new Fatigue();
        fatigue.setFa(fa);
        fatigue.setTy(ty);
        result = gson.toJson(fatigue);

//        if (bleCallback == null) {
            if (fatigueNotifyListener != null) {
                fatigueNotifyListener.onNotify(result);
            }
//        }

        LogUtil.i(TAG,"疲劳度数据："+ result);
        return result;
    }


    private String parseStatsSleep(){
        String result = "";
        byte[] sleepStart = new byte[4];
        byte[] sleepEnd = new byte[4];
        int sum = BitUtil.doubleByteToInt(body[9],body[10]);
        int deep = BitUtil.doubleByteToInt(body[11],body[12]);
        int light = BitUtil.doubleByteToInt(body[13],body[14]);
        int sober = BitUtil.doubleByteToInt(body[15],body[16]);
        System.arraycopy(body,1,sleepStart,0,sleepStart.length);
        System.arraycopy(body,5,sleepEnd,0,sleepEnd.length);
        long dateStart = BitUtil.bytesToLong(sleepStart)*1000;
        dateStart -= TimeZone.getDefault().getRawOffset();//减去时区
        long dateEnd = BitUtil.bytesToLong(sleepEnd)*1000;
        dateEnd -= TimeZone.getDefault().getRawOffset();//减去时区
        Sleep sleep = new Sleep(TimeUtil.getYMD(new Date(dateEnd)),sum,0,
                TimeUtil.longToTime(dateStart),
                TimeUtil.longToTime(dateEnd),4,deep,light,sober);
        if (sleepStatsNotifyListener != null) {
            result = gson.toJson(sleep);
            sleepStatsNotifyListener.onNotify(result);
        }
        return result;
    }

    private String parseSleep() {
        int sleepLength = body[1];//睡眠历史总条数
        int sleepNum = body[2];//睡眠包编号
        byte[] startTimeBs = new byte[5];//开始时间
        byte[] endTimeBs = new byte[5];//结束时间
        byte[] sleepTimeBs = new byte[2];//深睡时间
//        byte[] lightTimeBs = new byte[2];//浅睡时间
        //拷贝字节
        System.arraycopy(body,3,startTimeBs,0,startTimeBs.length);
        System.arraycopy(body,8,endTimeBs,0,endTimeBs.length);
        System.arraycopy(body,13,sleepTimeBs,0,sleepTimeBs.length);
//        System.arraycopy(body,15,lightTimeBs,0,lightTimeBs.length);
        //解析数据
        String startTime = BitUtil.bytesToDate(startTimeBs,1);//解析开始时间
        String endTime = BitUtil.bytesToDate(endTimeBs,1);//解析结束时间
        int time = BitUtil.byte3ToInt(sleepTimeBs);//解析深度睡眠
//        int light = BitUtil.byte3ToInt(lightTimeBs);//解析浅度睡眠
        String day = BitUtil.bytesToDate(endTimeBs,4);
        int type = body[0] ;
        int dataType = body[15];//睡眠数据类型 1深睡 2浅睡 3清醒
        if (body[11] >= 0x20) {//如果日期时间大于20点判断为今天
            day = TimeUtil.getCalculateDay(day,+1);
        }
        int deep = 0,light = 0,awake = 0;
        if (dataType == 1) {
            deep = time;
        }else if (dataType == 2){
            light = time;
        }else if (dataType == 3){
            awake = time;
        }
        //数据填充模板
        Sleep sleep = new Sleep(day, sleepLength, sleepNum, startTime, endTime, dataType, deep, light, awake);
        //////////////////////////存睡眠原始数据测试用/////////////////////////
        String s_sleepData = BitUtil.parseByte2HexStr(data);
        sleep.setS_sleep_data(s_sleepData);
        //////////////////////////////////////////////////////////////////////
        String result = gson.toJson(sleep);
        if (type == 3){
            if (bleCallback == null) {
                if (sleepNotifyListener != null) {
                    sleepNotifyListener.onNotify(result);
                }
            }
        }else if (type == 1){
            if (sleepHistoryListener != null) {
                sleepHistoryListener.onHistory(result);
            }
        }
        //打印日志
        LogUtil.i(TAG,"睡眠数据："+ result);
        return result;
    }

    private String parseFirmware(){
        String result = "";
        if (body[0] == 0x05) {
            byte[] date = new byte[3];
            byte[]  btType = new byte[2];
            System.arraycopy(body,1,date,0,date.length);
            System.arraycopy(data,11,btType,0,btType.length);
            BitUtil.bytesToDate(date,4);
            String ver = (data[7]&0xff)+"."+(data[8]&0xff)+"."+(data[9]&0xff);
            JSONObject jsonObject = new JSONObject();
            String type = "";
            if (data[10] == 0x55) {
                type = BitUtil.bcd2Str(btType);
                StringBuilder builder = new StringBuilder();
                for (int x =4;x-type.length()>0;x--){
                    builder.append("0");
                }
                builder.append(type);
                type = builder.toString();
            }
            try {
                jsonObject.put("firmwareVersion",ver);
                jsonObject.put("firmwareType",(type));
                result = jsonObject.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            LogUtil.i(TAG,"固件信息："+result);
        }else if (body[0] == 0x06){//电池电量
            byte[] date = new byte[6];
            System.arraycopy(body,1,date,0,date.length);
            int battery = body[7]&0xff;
            int batteryState = body[8];
            result = putIntsJson(new String[]{"battery","batteryState"},new int[]{battery,batteryState});
            if (actionListener != null) {
                actionListener.onAction(1660,result);
            }
            LogUtil.i(TAG,"固件信息："+result);
        }else if (body[0] == 0x50){
            byte date = body[1];
            result = ""+date;
            if(hrCorrectingNotifyListener!=null){
                hrCorrectingNotifyListener.onNotify(result);
            }
            Log.i(TAG,"0x50:"+date);
        }

        return result;
    }


    private String parseBp() {
        int brTy = body[0];
        BloodPressure bloodPressure = new BloodPressure();
        switch (brTy){
            case 0:
                int hp = BitUtil.byteToInt(body[11]);//高压
                int lp = BitUtil.byteToInt(body[12]);//低压
                int hr = body[13]&0xff;//心率
                byte[] brDates = new byte[6];
                System.arraycopy(body,5,brDates,0,brDates.length);
                String bDate = BitUtil.bytesToDate(brDates,0);//得到时间
                String bDay = BitUtil.bytesToDate(brDates,4);
                bloodPressure.setBpDate(bDate);
                bloodPressure.setBpDay(bDay);
                bloodPressure.setBpHp(hp);
                bloodPressure.setBpLp(lp);
                bloodPressure.setBpHr(hr);
                LogUtil.e(TAG, "血压当前"+ bloodPressure.toString() );
                break;
            case 1:
                bloodPressure = getBloodPressure(bloodPressure);
                LogUtil.e(TAG, "血压历史"+bloodPressure.toString() );
                break;
            case 2:
                byte[] bLength = new byte[2];
                System.arraycopy(body,1,bLength,0,bLength.length);
                int bLen = BitUtil.byte3ToInt(bLength);//得到总包数
                bloodPressure.setBpLength(bLen);
                break;
            case 3:
                bloodPressure = getBloodPressure(bloodPressure);
                break;
        }
        String result = gson.toJson(bloodPressure,BloodPressure.class);
        if (brTy == 3){
            if (bleCallback == null) {
                if (bpNotifyListener != null) {
                    bpNotifyListener.onNotify(result);
                }
            }
        }else if (brTy == 1){
            if (bpHistoryListener != null) {
                bpHistoryListener.onHistory(result);
            }
        }
        LogUtil.i(TAG, "血压数据"+result);
        return result;
    }

    private BloodPressure getBloodPressure(BloodPressure bloodPressure) {
        int hpHis = BitUtil.byteToInt(body[11]);//高压
        int lpHis = BitUtil.byteToInt(body[12]);//低压
        int hrHis = body[13]&0xff;//心率
        byte[] brBLength = new byte[2];
        byte[] brBNum = new byte[2];
        byte[] brBDates = new byte[6];
        //拷贝字节
        System.arraycopy(body,1,brBLength,0,brBLength.length);
        System.arraycopy(body,3,brBNum,0,brBNum.length);
        System.arraycopy(body,5,brBDates,0,brBDates.length);
        String brDate = BitUtil.bytesToDate(brBDates,0);//得到时间
        String brDay = BitUtil.bytesToDate(brBDates,4);
        int brLength = BitUtil.byte3ToInt(brBLength);//得到总包数
        int brNum = BitUtil.byte3ToInt(brBNum);//得到包编号
        bloodPressure = new BloodPressure(brDate,brDay,brLength,brNum,hpHis,lpHis,hrHis);
        return bloodPressure;
    }

    private String parseBo() {
        int brTy = body[0];
        BloodOxygen bloodOxygen = new BloodOxygen();
        switch (brTy){
            case 0:
                int hp = body[11]&0xff;//整数
                int lp = body[12];//小数
                byte[] brDates = new byte[6];
                System.arraycopy(body,5,brDates,0,brDates.length);
                String bDate = BitUtil.bytesToDate(brDates,0);//得到时间
                String bDay = BitUtil.bytesToDate(brDates,4);
                bloodOxygen.setboDay(bDay);
                bloodOxygen.setboDate(bDate);
                bloodOxygen.setboRate(String.valueOf(hp));
                LogUtil.e(TAG, "血氧当前"+ bloodOxygen.toString() );
                break;
            case 1:
                bloodOxygen = getBloodOxygen(bloodOxygen);
                break;
            case 2:
                byte[] bLength = new byte[2];
                System.arraycopy(body,1,bLength,0,bLength.length);
                int bLen = BitUtil.byte3ToInt(bLength);//得到总包数
                bloodOxygen.setboLength(bLen);
                break;
            case 3:
                bloodOxygen = getBloodOxygen(bloodOxygen);
                break;
        }
        String result = gson.toJson(bloodOxygen,BloodOxygen.class);
        if (brTy == 3){
            if (bleCallback == null) {
                if (boNotifyListener != null) {
                    boNotifyListener.onNotify(result);
                }
            }
        }else if (brTy == 1){
            if (boHistoryListener != null) {
                boHistoryListener.onHistory(result);
            }
        }
        LogUtil.i(TAG, "血氧数据"+result);
        return result;
    }

    @NonNull
    private BloodOxygen getBloodOxygen(BloodOxygen bloodOxygen) {
        int hpHis = body[11]&0xff;//整数
        int lpHis = body[12];//小数
        byte[] brBLength = new byte[2];
        byte[] brBNum = new byte[2];
        byte[] brBDates = new byte[6];
        //拷贝字节
        System.arraycopy(body,1,brBLength,0,brBLength.length);
        System.arraycopy(body,3,brBNum,0,brBNum.length);
        System.arraycopy(body,5,brBDates,0,brBDates.length);
        String brDate = BitUtil.bytesToDate(brBDates,0);//得到时间
        String brDay = BitUtil.bytesToDate(brBDates,4);
        int brLength = BitUtil.byte3ToInt(brBLength);//得到总包数
        int brNum = BitUtil.byte3ToInt(brBNum);//得到包编号
        bloodOxygen = new BloodOxygen(brDate,brDay,brLength,brNum,hpHis+"."+lpHis);
        LogUtil.e(TAG, "血氧历史"+bloodOxygen.toString() );
        return bloodOxygen;
    }

    public String parseStepSection(byte[] body) {
        byte en = body[0];
        byte[] bs = new byte[3];
        System.arraycopy(body,1,bs,0,bs.length);
        Sport stepModel = new Sport();
        int ah = ((bs[0] <<4)&0x0ff0 | (bs[1] >>4)&0x0f) & 0x0fff;
        int ch = (((bs[1] & 0x0f) << 8) | (bs[2] & 0xff)) & 0x0fff;
        if (en >> 2 == 1 || en == 1 ) {//分段历史数据/上报
            byte[] bsStep = new byte[3];
            byte[] bsCalorie = new byte[3];
            byte[] bsMileage = new byte[3];
            byte[] bsDate = new byte[4];
            int time =  body[17] & 0xff;
            System.arraycopy(body,4,bsStep,0,bsStep.length);
            System.arraycopy(body,7,bsCalorie,0,bsCalorie.length);
            System.arraycopy(body,10,bsMileage,0,bsMileage.length);
            System.arraycopy(body,13,bsDate,0,bsDate.length);
            long date = BitUtil.bytesToLong(bsDate)*1000;
            date -= TimeZone.getDefault().getRawOffset();//减去时区
            int step = BitUtil.byte3ToInt(bsStep);
            int calorie = BitUtil.byte3ToInt(bsCalorie);
            int mileage = BitUtil.byte3ToInt(bsMileage);
            stepModel.setStepNum(step);
            stepModel.setStepMileage(mileage);
            stepModel.setStepCalorie(calorie);
            stepModel.setHisLength(ah);
            stepModel.setHisCount(ch);
            stepModel.setStepDate(new Date(date));
            stepModel.setStepDay(TimeUtil.getYMD(new Date(date)));
            stepModel.setStepTime(time);
            stepModel.setStepType(1);
        }else if (en >> 1 == 1){//分段历史数量
            stepModel.setHisLength(ah);
        }
        String result = gson.toJson(stepModel,Sport.class);
        if (en == 1){//上报
            if (stepNotifyListener != null) {
                stepNotifyListener.onNotify(stepModel);
            }
        }else if (en >> 2 == 1){
            if (stepHistoryListener != null) {
                stepHistoryListener.onHistory(result);
            }
        }
        LogUtil.i(TAG, "分段计步"+result);
        return result;
    }

    private String parseSport() {
        String bits = BitUtil.bitToString(body[0]);//获得En字节的所有位
        int mode = Integer.parseInt(bits.substring(0,4),2);//获得运动模式7-4位
        int en = Integer.parseInt(bits.substring(4,8),2);//获得数据模型0-3位

        String str = BitUtil.bitToString(body[1]);//获得ss字节的所有位
        int ss = Integer.parseInt(str.substring(6,8),2);//获得步行状态0-1位

        byte[] bsNumInfo = new byte[3];//获得包号3字节的所有位
        System.arraycopy(body,2,bsNumInfo,0,bsNumInfo.length);
        String cc = BitUtil.bitToString(bsNumInfo);
        int packageLength = Integer.parseInt(cc.substring(0,12),2);
        int packageNum = Integer.parseInt(cc.substring(12,24),2);

        byte[] bsSports = new byte[7];
        System.arraycopy(body,5,bsSports,0,bsSports.length);
        String dd = BitUtil.bitToString(bsSports);
        LogUtil.e(TAG, "sport bits == "+dd);
        int step = Integer.parseInt(dd.substring(5,22),2);
        int ka = Integer.parseInt(dd.substring(22,39),2);
        int mi = Integer.parseInt(dd.substring(39,56),2);

        byte[] bsDate = new byte[4];
        System.arraycopy(body, 12, bsDate, 0, bsDate.length);
        long date = BitUtil.bytesToLong(bsDate)*1000;
        date -= TimeZone.getDefault().getRawOffset();//减去时区
        String date2 = BitUtil.timeStamp2Date(String.valueOf(date),"");
//        Log.i(TAG, "parseSport: " + date2);

        byte[] bsTime = new byte[2];
        System.arraycopy(body,16,bsTime,0,bsTime.length);
        int time = BitUtil.byte3ToInt(bsTime);

        Sport stepModel = new Sport();
        if (en == 3 || en == 1 ) {//分段历史数据/上报
            stepModel.setStepNum(step);
            stepModel.setStepMileage(mi);
            stepModel.setStepCalorie(ka);
            stepModel.setHisLength(packageLength);
            stepModel.setHisCount(packageNum);
            Date date1 = new Date(date);
            stepModel.setStepDate(date1);
            String s_date1 = TimeUtil.getYMD(date1);
            stepModel.setStepDay(s_date1);
            stepModel.setStepTime(time);
            stepModel.setStepType(ss+2);//0代表当前 1代表分段计步
            stepModel.setSportMode(mode);
        }else if (en == 2){//运动历史数量
            stepModel.setHisLength(packageLength);
        }
        String result = gson.toJson(stepModel,Sport.class);
        if (en == 3){//上报
            if (runNotifyListener != null) {
                runNotifyListener.onNotify(stepModel);
            }
        }else if (en == 1){
            if (runHistoryListener != null) {
                runHistoryListener.onHistory(result);
            }
        }
        LogUtil.i(TAG, "运动"+result);
        return result;
    }


    public void setSportNotifyListener(BleNotifyListener sportNotifyListener) {
        this.sportNotifyListener = sportNotifyListener;
    }

    public void setStepNotifyListener(BleNotifyListener stepNotifyListener) {
        this.stepNotifyListener = stepNotifyListener;
    }

    public void setRunNotifyListener(BleNotifyListener runNotifyListener) {
        this.runNotifyListener = runNotifyListener;
    }

    public void setSleepNotifyListener(BleNotifyListener sleepNotifyListener) {
        this.sleepNotifyListener = sleepNotifyListener;
    }

    public void setHrNotifyListener(BleNotifyListener hrNotifyListener) {
        this.hrNotifyListener = hrNotifyListener;
    }
    public void setMicroNotifyListener(BleNotifyListener microNotifyListener) {
        this.microNotifyListener = microNotifyListener;
    }

    public void setFatigueNotifyListener(BleNotifyListener fatigueNotifyListener) {
        this.fatigueNotifyListener = fatigueNotifyListener;
    }

    public void setEcgHrNotifyListener(BleNotifyListener ecgHrNotifyListener) {
        this.ecgHrNotifyListener = ecgHrNotifyListener;
    }

    public void setBpNotifyListener(BleNotifyListener bpNotifyListener) {
        this.bpNotifyListener = bpNotifyListener;
    }

    public void setBoNotifyListener(BleNotifyListener boNotifyListener) {
        this.boNotifyListener = boNotifyListener;
    }

    public void setActionListener(BleActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setStepHistoryListener(BleHistoryListener stepHistoryListener) {
        this.stepHistoryListener = stepHistoryListener;
    }

    public void setRunHistoryListener(BleHistoryListener runHistoryListener) {
        this.runHistoryListener = runHistoryListener;
    }

    public void setSleepHistoryListener(BleHistoryListener sleepHistoryListener) {
        this.sleepHistoryListener = sleepHistoryListener;
    }

    public void setHrHistoryListener(BleHistoryListener hrHistoryListener) {
        this.hrHistoryListener = hrHistoryListener;
    }

    public void setMicroHistoryListener(BleHistoryListener microHistoryListener) {
        this.microHistoryListener = microHistoryListener;
    }

    public void setBpHistoryListener(BleHistoryListener bpHistoryListener) {
        this.bpHistoryListener = bpHistoryListener;
    }

    public void setBoHistoryListener(BleHistoryListener boHistoryListener) {
        this.boHistoryListener = boHistoryListener;
    }

    public void setSleepStatsNotifyListener(BleNotifyListener sleepStatsNotifyListener) {
        this.sleepStatsNotifyListener = sleepStatsNotifyListener;
    }

    public void setHrCorrectingNotifyListener(BleNotifyListener hrCorrectingNotifyListener){
        this.hrCorrectingNotifyListener = hrCorrectingNotifyListener;
    }

    public void setEcgNotifyListener(BleNotifyListener ecgNotifyListener) {
        this.ecgNotifyListener = ecgNotifyListener;
    }

    public void setHrBaseLineListener(BleNotifyListener hrBaseLineListener){
        this.hrBaseLineListener = hrBaseLineListener;
    }

    private String putIntJson(String key, int value){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(key,value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    private String putIntsJson(String[] keys,int[] values){
        JSONObject jsonObject = new JSONObject();
        try {
            for (int i = 0; i < keys.length; i++) {
                jsonObject.put(keys[i],values[i]);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    private String putStringJson(String key,String value){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(key,value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public void setTimingHrTestListener(BleCallback timingHrTestListener) {
        this.timingHrTestListener = timingHrTestListener;
    }
}

