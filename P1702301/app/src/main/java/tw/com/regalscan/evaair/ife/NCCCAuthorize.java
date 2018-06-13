package tw.com.regalscan.evaair.ife;

import android.content.Context;

import com.google.gson.Gson;
import com.jess.arms.utils.ArmsUtils;
import com.regalscan.sqlitelibrary.TSQL;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.jessyan.retrofiturlmanager.RetrofitUrlManager;
import me.jessyan.rxerrorhandler.core.RxErrorHandler;
import me.jessyan.rxerrorhandler.handler.ErrorHandleSubscriber;
import tw.com.regalscan.R;
import tw.com.regalscan.app.ResponseErrorListenerImpl;
import tw.com.regalscan.component.AESEncrypDecryp;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.evaair.ife.model.api.AuthorizeService;
import tw.com.regalscan.evaair.ife.model.entity.AuthorizeModel;
import tw.com.regalscan.utils.Constant;
import tw.com.regalscan.utils.Cursor;

/**
 * Created by tp00175 on 2017/11/8.
 */

public class NCCCAuthorize {

    private final static String TAG = NCCCAuthorize.class.getSimpleName();

    private Context mContext;
    private RxErrorHandler mRxErrorHandler;
    private IFEDBFunction mIFEDBFunction;
    private TSQL mTSQL;

    public NCCCAuthorize(Context context) {
        mContext = context;
        mRxErrorHandler = RxErrorHandler.builder().with(mContext).responseErrorListener(new ResponseErrorListenerImpl()).build();
        mIFEDBFunction = new IFEDBFunction(mContext, FlightData.SecSeq);
        mTSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
    }

    /**
     * 取授權
     *
     * @param authorizeModel
     * @param authorizeReturn
     */
    public void SendRequestToNCCCGetAuthorize(AuthorizeModel authorizeModel, AuthorizeReturn authorizeReturn) {

        RetrofitUrlManager.getInstance().putDomain("OnlineAuthorize", Constant.AuthorizeUrl);

        authorizeModel.setCA_NO(FlightData.CrewID);
        authorizeModel.setDEPT_DATE(FlightData.FlightDate);
        authorizeModel.setDOC_NO(FlightData.CartNo);
        authorizeModel.setSECTOR_SEQ(Integer.valueOf(FlightData.SecSeq));

        try {
            String json = new Gson().toJson(authorizeModel);

            String sendData = AESEncrypDecryp.getEncryptData(json, "P35SMASC");
            String REAUTH_MARK = authorizeModel.getREAUTH_MARK();

            if (REAUTH_MARK.equals("N")) {
                Authorize(sendData)
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(disposable -> Cursor.Busy(mContext.getString(R.string.Processing_Msg), mContext))
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally(Cursor::Normal)
                    .subscribe(new ErrorHandleSubscriber<AuthorizeModel>(mRxErrorHandler) {
                        @Override
                        public void onNext(AuthorizeModel authorizeModel) {
                            if (processReturn("N", authorizeModel)) {
                                mIFEDBFunction.saveAuthorizeData(authorizeModel);
                                authorizeReturn.success(authorizeModel);
                            } else {
                                authorizeModel.setRECIPT_NO(0);
                                mIFEDBFunction.saveAuthorizeData(authorizeModel);
                                authorizeReturn.failed("Auth fail! Please use another credit card.");
                            }
                        }

                        @Override
                        public void onError(Throwable t) {
                            Cursor.Normal();
                            super.onError(t);
                            writeLog(t.getMessage());
                            authorizeReturn.failed("");
                        }
                    });
            } else {
                DeAuthorize(sendData)
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(disposable -> Cursor.Busy(mContext.getString(R.string.Processing_Msg), mContext))
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnComplete(Cursor::Normal)
                    .subscribe(new ErrorHandleSubscriber<AuthorizeModel>(mRxErrorHandler) {
                        @Override
                        public void onNext(AuthorizeModel authorizeModel) {
                            if (processReturn("Y", authorizeModel)) {
                                mIFEDBFunction.saveAuthorizeData(authorizeModel);
                                authorizeReturn.success(authorizeModel);
                            } else {
                                authorizeModel.setRECIPT_NO(0);
                                mIFEDBFunction.saveAuthorizeData(authorizeModel);
                                authorizeReturn.failed("Auth fail! Please use another credit card.");
                            }
                        }

                        @Override
                        public void onError(Throwable t) {
                            Cursor.Normal();
                            super.onError(t);
                            writeLog(t.getMessage());
                            authorizeReturn.failed("");
                        }
                    });
            }
        } catch (Exception e) {
            writeLog(e.getMessage());
            authorizeReturn.failed("");
        }
    }

    /**
     * 取消授權
     *
     * @param ReceiptNo
     * @param RefundType
     */
    public void canecelAuthorize(String ReceiptNo, String RefundType) {
        if (RefundType.equals("DutyFreeRefund")) {

        } else {

        }
    }

    private Observable<AuthorizeModel> Authorize(String data) {
        return ArmsUtils.obtainAppComponentFromContext(mContext)
            .repositoryManager().obtainRetrofitService(AuthorizeService.class).authorize(data);
    }

    private Observable<AuthorizeModel> DeAuthorize(String data) {
        return ArmsUtils.obtainAppComponentFromContext(mContext).repositoryManager()
            .obtainRetrofitService(AuthorizeService.class).deAuthorize(data);
    }

    /**
     * 判斷授權成功與否
     *
     * @param REAUTH_MARK
     * @param authorizeModel
     * @return
     */
    private boolean processReturn(String REAUTH_MARK, AuthorizeModel authorizeModel) {
        if (REAUTH_MARK.equals("N")) {
            return authorizeModel.getRSPONSE_CODE() != null &&
                authorizeModel.getRSPONSE_CODE().equals("000") &&
                authorizeModel.getAUTH_RETCODE() != null &&
                authorizeModel.getAUTH_RETCODE().equals("1") &&
                authorizeModel.getAPPROVE_CODE() != null &&
                !authorizeModel.getAPPROVE_CODE().equals("");
        } else {
            return authorizeModel.getRSPONSE_CODE() != null &&
                authorizeModel.getRSPONSE_CODE().equals("000");
        }
    }

    public interface AuthorizeReturn {
        void success(AuthorizeModel authorizeModel);

        void failed(String errMsg);
    }

    private void writeLog(String errMsg) {
        mTSQL.WriteLog(FlightData.SecSeq, "System", TAG, "SendRequestToNCCCGetAuthorize", "SendRequestToNCCCGetAuthorize error: " + errMsg);
    }
}
