package com.manridy.iband.common;

import android.content.Context;
import android.os.Environment;
import android.util.Log;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class OtaDfuInitiator {
    private ArrayList<String> ota_data_queue = new ArrayList<String>();
    private String otaChecksum;
    private int otaFilePackTotalNum;
    private String otaFilePath;
    private Context mContext;

    public OtaDfuInitiator(Context mContext) {
        this.mContext = mContext;
    }

    public OtaDfuInitiator(String otaFilePath) {
        this.otaFilePath = otaFilePath;
    }

    public boolean loadImageFile() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                byte[] buffer = new byte[16];
                int idx = 0;
                InputStream fis = null;
                if (otaFilePath != null) {
                    File file = new File(otaFilePath);
                    fis = new FileInputStream(file);
                } else {
//                    int ota_resourceId;
//            ota_resourceId = R.raw.hb093_ota;
//                    ota_resourceId = R.raw.hb093_ota_v107_alpha;
//                    fis = mContext.getResources().openRawResource(ota_resourceId);
                }
                if (fis == null) return false;
                int size = fis.available();//文件总字节数
                otaFilePackTotalNum = size / 16;
//                EventBus.getDefault().post(new EventMessage(EventMessage.MSG_WHAT_FILE_LOAD, otaFilePackTotalNum));

                ota_data_queue.clear();
                int checksum = 0;

                while (fis.read(buffer, 0, 16) > 0) {

                    for (int i = 0; i < 16; i++) {
                        checksum += buffer[i] & 0xFF;
                        checksum &= 0xFFFF;
                    }

                    final StringBuilder stringBuilder = new StringBuilder(buffer.length);
                    for (byte byteChar : buffer) {
                        stringBuilder.append(String.format("%02X", byteChar));
                    }

                    String data = "FA01" + HexUtil.convertToString16(idx) + stringBuilder.toString();
                    ota_data_queue.add(data);
                    idx++;
                    Log.e("ota", "ota data =" + data);
//                    EventBus.getDefault().post(new EventMessage(EventMessage.MSG_WHAT_FILE_LOADING, idx));
                }

                fis.close();
//                EventBus.getDefault().post(new EventMessage(EventMessage.MSG_WHAT_FILE_LOADED));
//                Log.i(tag, "升级文件数据已载入内存(从资源文件中)共计" + ota_data_queue.size() + "块");


                otaChecksum = HexUtil.convertToString16(checksum & 0xFFFF);
//                Log.e(tag, "CheckSum = " + otaChecksum);

            } catch (OutofRangeException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    public byte[] otaDfuCmd() {
        int packNum;
        packNum = ota_data_queue.size();
        byte packNum_h = (byte) ((packNum & 0xff00) >> 8);
        byte packNum_l = (byte) (packNum & 0x00ff);
        //0xFA 00 AH AL 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
        byte[] ble_trigDFU = new byte[]{
                (byte) 0xFA, 0x00, packNum_h, packNum_l,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00
        };
        return ble_trigDFU;
    }

    public byte[] getOtaNextPack() {
        //0xFA 01 IH IL D0 D1 D2 D3 D4 D5 D6 D7 D8 D9 DA DB DC DD DE DF
        if (ota_data_queue.size() > 0) {
            String s_16byte_data = ota_data_queue.remove(0);
            byte[] b_16byte_data = HexUtil.hexStringToBytes(s_16byte_data);
            return b_16byte_data;
        } else {//数据取出完
            //0xFA 03 C0 C1 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
            String s_data_end = "FA03" + otaChecksum + "00000000000000000000000000000000";
            byte[] b_data_end = HexUtil.hexStringToBytes(s_data_end);
            return b_data_end;
        }
    }

    public byte[] getOtaEndDfu() {
        //0xFA 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
        String s_dfu_end = "FA02000000000000000000000000000000000000";
        byte[] b_dfu_end = HexUtil.hexStringToBytes(s_dfu_end);
        return b_dfu_end;
    }

    private int otaPackCnt = 0;
    public void resetPackCnt() {
        otaPackCnt = 0;
    }

    public int otaPackCntAccumulate() {
        otaPackCnt++;
        return otaPackCnt;
    }

    public int progress() {
        int progressFilePackNum = otaFilePackTotalNum - ota_data_queue.size();
//        int progress = (progressFilePackNum / otaFilePackTotalNum) * 100;
        return progressFilePackNum;
    }

    public int getOtaFilePackTotalNum() {
        return otaFilePackTotalNum;
    }
}
