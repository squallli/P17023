package tw.com.regalscan.evaground.mvp.presenter;

import java.io.*;
import java.util.Calendar;
import javax.inject.Inject;

import android.app.AlertDialog;
import android.app.Application;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.EditText;

import com.jess.arms.di.scope.ActivityScope;
import com.jess.arms.http.imageloader.ImageLoader;
import com.jess.arms.integration.AppManager;
import com.jess.arms.mvp.BasePresenter;
import com.jess.arms.utils.DataHelper;
import com.jess.arms.utils.RxLifecycleUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.jessyan.rxerrorhandler.core.RxErrorHandler;
import tw.com.regalscan.evaground.Controller.BackupController;
import tw.com.regalscan.evaground.MessageBox;
import tw.com.regalscan.evaground.Models.Flight;
import tw.com.regalscan.evaground.Models.FlightData;
import tw.com.regalscan.evaground.mvp.contract.OfflineDownloadContract;


@ActivityScope
public class OfflineDownloadPresenter extends BasePresenter<OfflineDownloadContract.Model, OfflineDownloadContract.View> {
    private RxErrorHandler mErrorHandler;
    private Application mApplication;
    private ImageLoader mImageLoader;
    private AppManager mAppManager;

    @Inject
    public OfflineDownloadPresenter(OfflineDownloadContract.Model model, OfflineDownloadContract.View rootView
        , RxErrorHandler handler, Application application
        , ImageLoader imageLoader, AppManager appManager) {
        super(model, rootView);
        this.mErrorHandler = handler;
        this.mApplication = application;
        this.mImageLoader = imageLoader;
        this.mAppManager = appManager;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mErrorHandler = null;
        this.mAppManager = null;
        this.mImageLoader = null;
        this.mApplication = null;
    }

    /**
     * 初始化日期選擇視窗
     *
     * @param etDatePicker
     */
    public void initDatePicker(EditText etDatePicker) {
        Calendar mCalendar = Calendar.getInstance();
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH) + 1;
        int day = mCalendar.get(Calendar.DAY_OF_MONTH);

        etDatePicker.setInputType(InputType.TYPE_NULL);
        etDatePicker.setText(String.valueOf(year) + String.format("%02d", month) + String.format("%02d", day));
        etDatePicker.setOnClickListener(view -> {
            new DatePickerDialog(mAppManager.getCurrentActivity(), (view1, year1, month1, day1) -> {
                String dateTime = String.valueOf(year1) + String.format("%02d",month1+1) + String.format("%02d",day1);
                etDatePicker.setText(dateTime);
            },  mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH)).show();
//            final DatePickerDialog mDialog = new DatePickerDialog(mAppManager.getCurrentActivity(), AlertDialog.THEME_HOLO_LIGHT, null, year, month, day);
//            mDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes",
//                (dialog, which) -> {
//                    //通過mDialog.getDatePicker獲得dialog上的DatePicker組件，然後可以獲取日期信息
//                    DatePicker datePicker = mDialog.getDatePicker();
//                    etDatePicker.setText(String.valueOf(datePicker.getYear()) +
//                        String.format("%02d", datePicker.getMonth() + 1) +
//                        String.format("%02d", datePicker.getDayOfMonth()));
//                    etDatePicker.setText("");
//                });
//            //取消按鈕，如果不需要直接不設置即可
//            mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No",
//                (dialog, which) -> System.out.println("BUTTON_NEGATIVE~~"));
//            mDialog.show();
        });
    }

    public void backUpDBFile() {

        String outputPath = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + "BackUp";
        String inputPath = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DOWNLOADS;

        InputStream in = null;
        OutputStream out = null;

        DataHelper.makeDirs(new File(Environment.getExternalStorageDirectory() + File.separator + Environment
            .DIRECTORY_DOWNLOADS + File.separator + "BackUp"));

        try {
            in = new FileInputStream(inputPath + File.separator + "P17023.db3");
            String date = String.valueOf(Calendar.getInstance().get(Calendar.YEAR)) +
                String.format("%02d", Calendar.getInstance().get(Calendar.MONTH) + 1) +
                String.format("%02d", Calendar.getInstance().get(Calendar.DATE));
            out = new FileOutputStream(outputPath + File.separator + "P17023.db3-" + date);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file
//            new File(inputPath + File.separator + "P17023.db3").delete();


        } catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
            return;
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
            return;
        }
    }

    public void createNewDBFile(String CartNo, String FlightDate, Flight[] flights) {

        FlightData fd = new FlightData();
        fd.setCartNo(CartNo);
        fd.setFlightDate(FlightDate);
        fd.setFlights(flights);

        Observable.just(BackupController.backup(mAppManager.getCurrentActivity(), fd))
            .subscribeOn(Schedulers.io())
            .doOnSubscribe(disposable -> mRootView.showLoading())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(RxLifecycleUtils.bindToLifecycle(mRootView))
            .subscribe(aBoolean -> {
                mRootView.hideLoading();
                if (aBoolean) {
                    if (MessageBox.show("", "Download success!", mAppManager.getCurrentActivity(), "Ok")) {
                        mRootView.killMyself();
                    }
                } else {
                    MessageBox.show("", "Download fail, please download again!", mAppManager.getCurrentActivity(),
                        "Ok");
                }
            });
    }
}
