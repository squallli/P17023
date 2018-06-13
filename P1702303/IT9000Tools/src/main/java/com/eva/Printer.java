//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.eva;

import android.device.PrinterManager;
import android.graphics.Bitmap;

public class Printer {

  private PrinterManager mPrinterManager = new PrinterManager();
  private int hight = 0;
  int BigFontSize = 38;
  int SmallFontSize = 24;
  private String font = "/mnt/sdcard/Download/DroidSansMono.ttf";

  public Printer() {
    this.hight = 0;
  }

  public boolean Open() {
    try {
      this.hight = 0;
      this.mPrinterManager.open();
      this.mPrinterManager.setGrayLevel(30);
      this.mPrinterManager.setSpeedLevel(80);
      this.mPrinterManager.setupPage(384, -1);
      return true;
    } catch (Exception var2) {
      throw var2;
    }
  }

  public boolean Close() {
    try {
      this.mPrinterManager.clearPage();
      this.mPrinterManager.close();
      return true;
    } catch (Exception var2) {
      throw var2;
    }
  }

  public void printImg(Bitmap bitmap) {
    try {
      int e = this.mPrinterManager.drawBitmap(bitmap, 0, 0);
      this.hight += e;
    } catch (Exception var3) {
      throw var3;
    }
  }

  public void printSmallText(String PrintText) {
    try {
      int e = this.mPrinterManager.drawText(PrintText, 0, this.hight, this.font, this.SmallFontSize, false, false, 0);
      this.hight += e;
    } catch (Exception var3) {
      throw var3;
    }
  }

  public void printBigText(String PrintText) {
    try {
      int e = this.mPrinterManager.drawText(PrintText, 0, this.hight, this.font, this.BigFontSize, false, false, 0);
      this.hight += e;
    } catch (Exception var3) {
      throw var3;
    }
  }

  public void printQRCode(String qrcodeLeft, String qrcodeRight) {
    try {
      this.mPrinterManager.drawBarcode(qrcodeLeft, 5, this.hight, 58, 4, 20, 0);
      int e = this.mPrinterManager.drawBarcode(qrcodeRight, 210, this.hight, 58, 4, 20, 0);
      this.hight += e;
    } catch (Exception var4) {
      throw var4;
    }
  }

  public void printCenterQRCode(String qrcode) {
    try {
      int e;
      if (qrcode.length() > 50) {
        e = this.mPrinterManager.prn_drawBarcode(qrcode, 100, this.hight, 58, 5, 20, 0);
      } else {
        e = this.mPrinterManager.prn_drawBarcode(qrcode, 120, this.hight, 58, 5, 20, 0);
      }
      this.hight += e;
    } catch (Exception var3) {
      throw var3;
    }
  }

  public void printSpace(int SpaceCount) {
    try {
      for (int e = 0; e < SpaceCount; ++e) {
        int high = this.mPrinterManager.drawText(" ", 0, this.hight, this.font, this.SmallFontSize, false, false, 0);
        this.hight += high;
      }

    } catch (Exception var4) {
      throw var4;
    }
  }

  public void printLine() {
    try {
      int e = this.mPrinterManager.drawText("----------------------------------------------------", 0, this.hight, this.font, this.SmallFontSize, false, false, 0);
      this.hight += e;
    } catch (Exception var2) {
      throw var2;
    }
  }

  public int printPage() {
    int ret = -1;

    try {
      ret = this.mPrinterManager.printPage(0);
    } catch (Exception var3) {
      var3.printStackTrace();
    }

    return ret;
  }
}
