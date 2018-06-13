package tw.com.regalscan.utils;

import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.idyl.winzipaes.AesZipFileEncrypter;

/**
 * Created by tp00175 on 2017/10/11.
 */

public class Compress {

  private static final int BUFFER = 2048;

  private List<String> _files = new ArrayList<>();
  private String _zipFile;

  public Compress(List<String> files, String zipFile) {
    _files = files;
    _zipFile = zipFile;
  }

  public void zip() {
    try {
      BufferedInputStream origin = null;
      FileOutputStream dest = new FileOutputStream(_zipFile);

      ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

      byte data[] = new byte[BUFFER];

      for (int i = 0; i < _files.size(); i++) {
        Log.v("Compress", "Adding: " + _files.get(i));
        FileInputStream fi = new FileInputStream(_files.get(i));
        origin = new BufferedInputStream(fi, BUFFER);
        ZipEntry entry = new ZipEntry(_files.get(i).substring(_files.get(i).lastIndexOf("/") + 1));
        out.putNextEntry(entry);
        int count;
        while ((count = origin.read(data, 0, BUFFER)) != -1) {
          out.write(data, 0, count);
        }
        origin.close();
      }
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
