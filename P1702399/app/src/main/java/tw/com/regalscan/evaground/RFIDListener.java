package tw.com.regalscan.evaground;

import android.device.PiccManager;

/**
 * Created by tp00175 on 2017/4/10.
 */

public class RFIDListener extends PiccManager {

    public RFIDListener() {
        super();
    }

    @Override
    public int open() {
        return super.open();
    }

    @Override
    public int request(byte[] mode, byte[] atq) {
        return super.request(mode, atq);
    }

    @Override
    public int antisel(byte[] sn, byte[] sak) {
        return super.antisel(sn, sak);
    }

    @Override
    public int activate() {
        return super.activate();
    }

    @Override
    public int m1_keyAuth(int keyType, int blnNo, int keylen, byte[] keyBuf, int iSeriNumlen,
            byte[] seriNum) {
        return super.m1_keyAuth(keyType, blnNo, keylen, keyBuf, iSeriNumlen, seriNum);
    }

    @Override
    public int m1_readBlock(int blkNo, byte[] pReadBuf) {
        return super.m1_readBlock(blkNo, pReadBuf);
    }
}
