package tw.com.regalscan.FTP;

import android.content.Context;
import android.device.DeviceManager;
import android.os.Environment;

import com.jess.arms.utils.ArmsUtils;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import timber.log.Timber;
import tw.com.regalscan.app.entity.Setting;
import tw.com.regalscan.db.PublicFunctions;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.utils.EvaUtils;

public class FTPFunction {

    private final String TAG = "FTP State";
    private FTPClient mFTPClient = null;

    private static String HOST_PATH = "";
    private static String black_HOST_PATH = "";
    private static String black_cup_REMOTE_PATH;
    private String USERNAME,black_USERNAME;
    private String PASSWORD,black_PASSWORD;
    private int PORT, black_PORT;

    // FTP 檔名
    private static String REMOTE_PATH = "";
    private static String PICTURE_UPLOAD_PATH = "";
    private static String RS_UPLOAD_PATH = "";
//  private static final String REMOTE_BLACK = "/Back_Up/TEST/BLACK.zip"; //VM、CUP差異資料庫 BLACK.zip
//  private static final String REMOTE_CUP = "/Back_Up/TEST/CUP.zip"; //CUP主檔壓縮檔 CUP.zip
//  private static final String REMOTE_Image = "/Back_Up/TEST/IMAGE.zip"; //商品圖片壓縮檔 IMAGE.zip
//  private static final String REMOTE_Test_cer = "/Back_Up/TEST/Test.cer";

    // 下載
    private static final String DOWNLOAD_FILE_PATH = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DOWNLOADS;
    private static final String TXT_FILE_PATH = Environment.getExternalStorageDirectory() + File.separator + "Download" + File.separator + "EVAPOSDownloadText";
    private static final String IMG_FILE_PATH =
            Environment.getExternalStorageDirectory() + File.separator + "Download" + File.separator + "pic";
    private static final String SD_PATH = Environment.getExternalStorageDirectory().getPath();

    private Context mContext;

    public List<String> mStrings = new ArrayList<>();

    private DeviceManager mDeviceManager = new DeviceManager();

    private String UDID = mDeviceManager.getDeviceId();

    public FTPFunction(Context context) {
        mContext = context;
        Setting setting = (Setting) ArmsUtils.obtainAppComponentFromContext(mContext).extras().get("Setting");
        REMOTE_PATH = "/ftp_posuser/DOWNLOAD/";
        PICTURE_UPLOAD_PATH = "/ftp_posuser/UPLOAD/";
        RS_UPLOAD_PATH = "/ftp_posuser/CAROUSEL/";

        HOST_PATH = setting.getFTPServerIP();
        USERNAME = setting.getFTPUserName();
        PASSWORD = setting.getFTPUserPassword();
        PORT = Integer.valueOf(setting.getFTPServerPort());

        black_cup_REMOTE_PATH = "/Download/";
        black_HOST_PATH = setting.getBlackFTPServerIP();
        black_USERNAME = setting.getBlackFTPUserName();
        black_PASSWORD = setting.getBlackFTPUserPassword();
        black_PORT = Integer.valueOf(setting.getBlackFTPServerPort());
    }

    public boolean DownloadFlightInfo(String date, String flightNo, String CartNo) {

        try {
            String filename = date + "_" + flightNo + "_" + CartNo + ".zip";

            String remoteFile = REMOTE_PATH + UDID + "/" + filename;

            return ftpConnect(HOST_PATH, PORT, USERNAME, PASSWORD, DOWNLOAD_FILE_PATH + File.separator + filename, remoteFile);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean DownloadBlackList() {

        try {

            String REMOTE_BLACK = black_cup_REMOTE_PATH + "BLACKN.zip";

            return ftpConnect(black_HOST_PATH, black_PORT, black_USERNAME, black_PASSWORD, DOWNLOAD_FILE_PATH + File.separator + "BLACK.zip", REMOTE_BLACK);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean DownloadCUP() {

        try {

            String REMOTE_CUP = black_cup_REMOTE_PATH + "CUP.zip";

            return ftpConnect(black_HOST_PATH, black_PORT, black_USERNAME, black_PASSWORD, DOWNLOAD_FILE_PATH + File.separator + "CUP.zip", REMOTE_CUP);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean DownloadImg() {
        try {

            String REMOTE_Image = REMOTE_PATH + "/IMAGE/IMAGE.zip";

            return ftpConnect(HOST_PATH, PORT, USERNAME, PASSWORD, DOWNLOAD_FILE_PATH + File.separator + "IMAGE.zip", REMOTE_Image);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean DownloadCA(String crtName) {
        try {
            String REMOTE_Cer = REMOTE_PATH + "/UPDATE/EVAPOS.cer";

            ftpConnect(HOST_PATH, PORT, USERNAME, PASSWORD, DOWNLOAD_FILE_PATH + File.separator + "EVAPOS.cer", REMOTE_Cer);

            String RMOTE_CERT = REMOTE_PATH + "/UPDATE/" + crtName;

            ftpConnect(HOST_PATH, PORT, USERNAME, PASSWORD, DOWNLOAD_FILE_PATH + File.separator + crtName, RMOTE_CERT);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void DownloadFont() {
        try {

            String REMOTE_Image = REMOTE_PATH + "/UPDATE/DroidSansMono.ttf";

            ftpConnect(HOST_PATH, PORT, USERNAME, PASSWORD, DOWNLOAD_FILE_PATH + File.separator + "DroidSansMono.ttf", REMOTE_Image);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean UnZipFlightInfo(String date, String flightNo, String CartNo) {
        try {

            String filename = date + "_" + flightNo + "_" + CartNo + ".zip";

//            EvaUtils.deleteFile(DOWNLOAD_FILE_PATH + File.separator, "P17023.db3");

            if (!unZip(new File(DOWNLOAD_FILE_PATH + File.separator + filename), new File(TXT_FILE_PATH))) {
                return false;
            }

            EvaUtils.deleteFile(DOWNLOAD_FILE_PATH + File.separator, filename);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean UnZipBlackList() {
        try {

            EvaUtils.deleteFile(DOWNLOAD_FILE_PATH + File.separator, "Black.db3");

            if (!unZip(new File(DOWNLOAD_FILE_PATH + File.separator + "BLACK.zip"), new File(DOWNLOAD_FILE_PATH))) {
                return false;
            }

            EvaUtils.deleteFile(DOWNLOAD_FILE_PATH + File.separator, "BLACK.zip");

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean UnZipCUP() {
        try {

            EvaUtils.deleteFile(DOWNLOAD_FILE_PATH + File.separator, "CUPBlack.db3");

            if (!unZip(new File(DOWNLOAD_FILE_PATH + File.separator + "CUP.zip"), new File(DOWNLOAD_FILE_PATH))) {
                return false;
            }
            EvaUtils.deleteFile(DOWNLOAD_FILE_PATH + File.separator, "CUP.zip");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean UnZipImg() {
        try {
            if (!unZip(new File(DOWNLOAD_FILE_PATH + File.separator + "IMAGE.zip"), new File(IMG_FILE_PATH))) {
                return false;
            }
            EvaUtils.deleteFile(DOWNLOAD_FILE_PATH + File.separator, "IMAGE.zip");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public JSONObject DBArchB() throws Exception {

        JSONObject ret = new JSONObject();

        try {

            JSONObject ResponseJsonObject;

            String ErrorMessage;

            PublicFunctions pFunction = new PublicFunctions(mContext, "0");

            //建立基本資料庫
            ResponseJsonObject = pFunction.CreateNewDatabase();
            if (!ResponseJsonObject.getString("ReturnCode").equals("0")) {
                ErrorMessage = ResponseJsonObject.getString("ReturnMessage");
                ret.put("Result", false);
                ret.put("ErrMsg", ErrorMessage);
                return ret;
            }


            //寫入基本資料庫
            ResponseJsonObject = pFunction.InsertFlightData();
            if (!ResponseJsonObject.getString("ReturnCode").equals("0")) {
                ErrorMessage = ResponseJsonObject.getString("ReturnMessage");
                ret.put("Result", false);
                ret.put("ErrMsg", ErrorMessage);
                return ret;
            }

            final StringBuilder errMsg = new StringBuilder();

            DBQuery.BasicSalesInfo salesInfo = DBQuery.checkBasicSalesInfoIsReady(mContext, errMsg);

            if (salesInfo == null) {
                ret.put("Result", false);
                ret.put("ErrMsg", errMsg.toString());
                return ret;
            }

            ret.put("Result", true);
            ret.put("ErrMsg", "");
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            ret.put("Result", false);
            ret.put("ErrMsg", e.toString());
            return ret;
        }
    }

    private boolean ftpConnect(String host, int port, String username, String password, String downloadFile, String remoteFile) {

        try {

            mFTPClient = new FTPClient();

            mFTPClient.connect(host, port);
            Timber.tag(TAG).d("Connected. Reply: %s", mFTPClient.getReplyString());

            // now check the reply code, if positive mean connection success
            if (FTPReply.isPositiveCompletion(mFTPClient.getReplyCode())) {

                // 登入
                mFTPClient.login(username, password);
                Timber.tag(TAG).d("Logged in Code = %s", mFTPClient.getReplyCode());

                // Set File Transfer Mode
                mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
                Timber.tag(TAG).d("Downloading");

                //檔案傳輸模式(主動或被動)
                mFTPClient.enterLocalPassiveMode();
                mFTPClient.setAutodetectUTF8(true);

                // 儲存
                File downloadFilePath = new File(downloadFile);
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFilePath));
                boolean success = mFTPClient.retrieveFile(remoteFile, outputStream);
                outputStream.close();

                if (success) {
                    Timber.tag(TAG).d("Download Success");
                    return true;
                } else {
                    Timber.tag(TAG).d("Download Failed Code = %s", mFTPClient.getReplyCode());
                    return false;
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (mFTPClient.isConnected()) {
                    mFTPClient.logout();
                    mFTPClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    private boolean unZip(File zipFile, File dest) {

        BufferedOutputStream bos;
        try {

            if (!dest.exists()) {
                dest.mkdirs();
            }

            FileInputStream fis = new FileInputStream(zipFile);
            ZipInputStream zipStream = new ZipInputStream(fis);
            ZipEntry zipEntry;
            while ((zipEntry = zipStream.getNextEntry()) != null) {
                String zipEntryName = zipEntry.getName();

                // 直接將檔案指向我們希望它解壓的位置
                File file = new File(dest, zipEntryName);

                if (file.exists()) {
                    Timber.tag("解壓縮").w("Already exist! Deleted File And Continued.");
                } else {
                    if (zipEntry.isDirectory()) {
                        file.mkdirs();
                    } else {
                        final int BUFFER_SIZE = 1024;
                        byte buffer[] = new byte[BUFFER_SIZE];
                        FileOutputStream fileOutputStream = new FileOutputStream(file);

                        bos = new BufferedOutputStream(fileOutputStream, BUFFER_SIZE);

                        int count;
                        while ((count = zipStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
                            bos.write(buffer, 0, count);
                        }
                        bos.flush();
                        bos.close();
                    }
                }
            }
            zipStream.close();
            fis.close();

            return true;
        } catch (Exception e) {
            Timber.tag("解壓縮").e("unZip fail!! Error Msg: %s", e.toString());
            e.printStackTrace();
        }

        return false;
    }

    private boolean ftpUploadFile(String host, int port, String username, String password, String localFilePath, String remoteFilePath) {

        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(host, port);
            ftpClient.login(username, password);
            ftpClient.enterLocalPassiveMode();

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            File localFile = new File(localFilePath);

            InputStream inputStream = new FileInputStream(localFile);
            boolean isSuccess = ftpClient.storeFile(remoteFilePath, inputStream);
            inputStream.close();
            return isSuccess;

        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean uploadPicture(String fileName) {
        String remotePath = PICTURE_UPLOAD_PATH + File.separator + fileName;
        return ftpUploadFile(HOST_PATH, PORT, USERNAME, PASSWORD, DOWNLOAD_FILE_PATH + File.separator + fileName, remotePath);
    }

    public boolean uploadRSFile(String fileName) {
        String remotePath = RS_UPLOAD_PATH + File.separator + fileName;
        return ftpUploadFile(HOST_PATH, PORT, USERNAME, PASSWORD, DOWNLOAD_FILE_PATH + File.separator + "Upload"  + File.separator + fileName, remotePath);
    }
    public void deleteAll(File path) {
        if (!path.exists()) {
            return;
        }
        if (path.isFile()) {
            path.delete();
            return;
        }
        File[] files = path.listFiles();
        for (File file : files) {
            deleteAll(file);
        }
        path.delete();
    }
}
