//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.eva;

import android.device.PiccManager;

public class ContaclessReader {
    PiccManager mPiccManager = new PiccManager();
    int scan_card = -1;
    int SNLen = -1;

    public ContaclessReader() {
    }

    public String GetUID() {
        String UID = null;
        if(this.mPiccManager == null) {
            this.mPiccManager = new PiccManager();
        }

        this.mPiccManager.open();
        byte[] CardType = new byte[2];
        byte[] Atq = new byte[14];
        byte SAK = 1;
        byte[] sak = new byte[]{(byte)SAK};
        byte[] SN = new byte[10];
        this.scan_card = this.mPiccManager.request(CardType, Atq);
        if(this.scan_card > 0) {
            this.SNLen = this.mPiccManager.antisel(SN, sak);
            UID = bytesToHexString(SN, this.SNLen);
        }

        this.mPiccManager.close();
        return UID;
    }

    public String checkEmployee(int blockNum, String keyType, byte[] Key) {
        String data = null;
        boolean flag = true;
        if(this.mPiccManager == null) {
            this.mPiccManager = new PiccManager();
        }

        this.mPiccManager.open();
        byte keytype;
        if(keyType == "A") {
            keytype = 0;
        } else {
            keytype = 1;
        }

        while(flag) {
            byte[] CardType = new byte[2];
            byte[] Atq = new byte[14];
            byte SAK = 1;
            byte[] sak = new byte[]{(byte)SAK};
            byte[] SN = new byte[10];
            byte[] blockData = new byte[16];
            this.scan_card = this.mPiccManager.request(CardType, Atq);
            if(this.scan_card > 0) {
                this.SNLen = this.mPiccManager.antisel(SN, sak);
                int keyAuth = this.mPiccManager.m1_keyAuth(keytype, blockNum, Key.length, Key, this.SNLen, SN);
                if(keyAuth == 0) {
                    this.mPiccManager.m1_readBlock(4, blockData);
                    data = bytesToHexString(blockData, blockData.length);
                    flag = false;
                }
            } else {
                data = "false";
                flag = false;
            }
        }

        if(data != "false") {
            this.mPiccManager.close();
            return convertHexToString(data).replace(" ", "");
        } else {
            return null;
        }
    }

    private static String bytesToHexString(byte[] src, int len) {
        StringBuilder stringBuilder = new StringBuilder("");
        if(src != null && src.length > 0) {
            if(len <= 0) {
                return "";
            } else {
                for(int i = 0; i < len; ++i) {
                    int v = src[i] & 255;
                    String hv = Integer.toHexString(v);
                    if(hv.length() < 2) {
                        stringBuilder.append(0);
                    }

                    stringBuilder.append(hv);
                }

                return stringBuilder.toString();
            }
        } else {
            return null;
        }
    }

    private static String convertHexToString(String hex) {
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        for(int i = 0; i < hex.length() - 1; i += 2) {
            String output = hex.substring(i, i + 2);
            int decimal = Integer.parseInt(output, 16);
            sb.append((char)decimal);
            temp.append(decimal);
        }

        return sb.toString();
    }
}
