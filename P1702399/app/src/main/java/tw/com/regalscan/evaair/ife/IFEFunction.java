package tw.com.regalscan.evaair.ife;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import aero.panasonic.inflight.crew.services.cartmanagement.v1.CrewCartManager;
import aero.panasonic.inflight.crew.services.cartmanagement.v1.model.CrewCart;
import aero.panasonic.inflight.crew.services.cartmanagement.v1.model.FailedItem;
import aero.panasonic.inflight.crew.services.cartmanagement.v1.requestAttribute.CreateCartRequest;
import aero.panasonic.inflight.crew.services.cartmanagement.v1.requestAttribute.GetCartRequest;
import aero.panasonic.inflight.crew.services.cartmanagement.v1.requestAttribute.UpdateCartRequest;
import aero.panasonic.inflight.crew.services.catalog.CatalogInventoryV1;
import aero.panasonic.inflight.crew.services.ordermanagement.v1.*;
import aero.panasonic.inflight.crew.services.ordermanagement.v1.Error;
import aero.panasonic.inflight.services.IInFlightCallback;
import aero.panasonic.inflight.services.InFlight;
import aero.panasonic.inflight.services.common.v2.SeatClass;
import com.regalscan.sqlitelibrary.TSQL;
import io.reactivex.Observable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import timber.log.Timber;
import tw.com.regalscan.R;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.evaair.ife.Education.EducationCrewCart;
import tw.com.regalscan.evaair.ife.Education.EducationCrewOrder;
import tw.com.regalscan.evaair.ife.entity.Catalog;
import tw.com.regalscan.evaair.ife.entity.IFEReturnData;
import tw.com.regalscan.wifi.WifiUtil;

/**
 * Created by tp00175 on 2017/11/27.
 */

public class IFEFunction {

    private static final String TAG = IFEFunction.class.getSimpleName();
    private Context mContext;
    private TSQL mTSQL;
    private ArrayList<Catalog> mCatalogs;
    private Catalog mCatalog;
    private IFEReturnData mIFEReturnData = new IFEReturnData();
    private IFEDBFunction mIFEDBFunction;

    //IFE
    private CatalogInventoryV1 mCatalogInventoryV1;
    private CrewOrderManager mCrewOrderManager;
    private CrewCartManager mCrewCartManager;
    private InFlight mInFlight;

    public IFEFunction(Context context) {

        mContext = context;

        mTSQL = TSQL.getINSTANCE(context, FlightData.SecSeq, "P17023");
        mIFEDBFunction = new IFEDBFunction(context, FlightData.SecSeq);

        mInFlight = new InFlight();
        mInFlight.setAppId(context, "E3A9b8688828d44f214f61b1d5b789b3b916191badf9906e924bb0dee363~dV01457864d73c9727e5514dece284c2135");

        initCatalogInventoryV1();
        initCrewOrderManager();
        initCrewCartManager();
    }

    private void showErrorMsg(String errMsg) {
        MessageBox.show("", errMsg, mContext, "Ok");
    }

    private void saveErrorMsg(String function, String errMsg) {
        mTSQL.WriteLog(FlightData.SecSeq, "IFE", TAG, function, errMsg);
    }

    private void initCatalogInventoryV1() {
        CatalogInventoryV1.initService(mContext.getApplicationContext(), CatalogInventoryV1.CatalogType.SHOPPING, new IInFlightCallback() {
            @Override
            public synchronized void onInitServiceComplete(Object o, String s) {
                if (mCatalogInventoryV1 != null) return;
                mCatalogInventoryV1 = (CatalogInventoryV1)o;
            }

            @Override
            public void onInitServiceFailed(String s, InFlight.Error error) {
                saveErrorMsg("CatalogInventoryV1", generateErrorMsg(error));
                showErrorMsg(generateErrorMsg(error));
            }
        }, mInFlight);
    }

    private void initCrewOrderManager() {
        CrewOrderManager.initService(mContext.getApplicationContext(), CrewOrderManager.CatalogType.SHOPPING, new IInFlightCallback() {
            @Override
            public synchronized void onInitServiceComplete(Object o, String s) {
                if (mCrewOrderManager != null) return;
                mCrewOrderManager = (CrewOrderManager)o;
            }

            @Override
            public void onInitServiceFailed(String s, InFlight.Error error) {
                saveErrorMsg("CrewOrderManager", generateErrorMsg(error));
                showErrorMsg(generateErrorMsg(error));
            }
        }, mInFlight);
    }

    private void initCrewCartManager() {
        CrewCartManager.initService(mContext.getApplicationContext(), new IInFlightCallback() {
            @Override
            public synchronized void onInitServiceComplete(Object o, String s) {
                if (mCrewCartManager != null) return;
                mCrewCartManager = (CrewCartManager)o;
            }

            @Override
            public void onInitServiceFailed(String s, InFlight.Error error) {
                saveErrorMsg("CrewCartManager", generateErrorMsg(error));
                showErrorMsg(generateErrorMsg(error));
            }
        }, mInFlight);
    }

    /**
     * 取得IFE Enable Item
     *
     * @return
     */
    public Observable<IFEReturnData> getEnableItem() {

        Timber.tag(TAG).w("Get enable item.");

        return Observable.create(e -> mCatalogInventoryV1.requestEnabledItems(SeatClass.BUSINESS, new CatalogInventoryV1.EnabledItemsReceivedListener() {
            @Override
            public void onEnabledItemsReceived(JSONArray jsonArray) {

                Timber.tag(TAG).w("Get enable item success.");

                if (jsonArray != null) {
                    if (mCatalogs != null) mCatalogs.clear();
                    else mCatalogs = new ArrayList<>();

                    populateCatalogs(jsonArray);

                    mIFEReturnData.setErrMsg("");
                    mIFEReturnData.setSuccess(true);
                    mIFEReturnData.setData(mCatalogs);

                    e.onNext(mIFEReturnData);
                    e.onComplete();
                }
            }

            @Override
            public void onError(CatalogInventoryV1.Error error) {

                Timber.tag(TAG).w("Get enable item fail.");
                saveErrorMsg("GetEnableItem", generateErrorMsg(error));

                mIFEReturnData.setErrMsg(generateErrorMsg(error));
                mIFEReturnData.setSuccess(false);
                mIFEReturnData.setData(null);

                e.onNext(mIFEReturnData);
                e.onComplete();
            }
        }).executeAsyc());
    }

    /**
     * 同步庫存
     *
     * @param skuWithQty
     * @return
     */
    public Observable<IFEReturnData> pushInventory(final HashMap<String, Integer> skuWithQty) {

        Timber.tag(TAG).w("Push inventory.");

        return Observable.create(e -> mCatalogInventoryV1.pushInventory(skuWithQty, new CatalogInventoryV1.PushInventoryListener() {
            @Override
            public void onInventoryPushed(List<String> list, Map<String, CatalogInventoryV1.Error> map) {
                Timber.tag(TAG).w("Push inventory success.");

                mIFEReturnData.setErrMsg("");
                mIFEReturnData.setSuccess(true);
                mIFEReturnData.setData(null);

                e.onNext(mIFEReturnData);
                e.onComplete();
            }

            @Override
            public void onError(CatalogInventoryV1.Error error) {

                Timber.tag(TAG).w("Push inventory fail.");
                saveErrorMsg("PushInventory", generateErrorMsg(error));

                mIFEReturnData.setErrMsg(generateErrorMsg(error));
                mIFEReturnData.setSuccess(false);
                mIFEReturnData.setData(null);

                e.onNext(mIFEReturnData);
                e.onComplete();
            }
        }).executeAsyc());
    }

    /**
     * 取得訂單
     *
     * @param orderID     訂單編號
     * @param orderStatus 訂單狀態
     * @return
     */
    public Observable<IFEReturnData> getOrders(String orderID, OrderStatus orderStatus) {

        Timber.tag(TAG).w("Get orders");

        return Observable.create(e -> {

            OrderFilterAttr orderFilterAttr = new OrderFilterAttr();
            orderFilterAttr.setCurrency("US");

            if (orderStatus != null) {
                if (orderStatus.equals(OrderStatus.ORDER_STATUS_OPEN)) {
                    orderFilterAttr.setOrderStatus(OrderStatus.ORDER_STATUS_OPEN);
                } else if (orderStatus.equals(OrderStatus.ORDER_STATUS_PROCESSING)) {
                    orderFilterAttr.setOrderStatus(OrderStatus.ORDER_STATUS_PROCESSING);
                }
            }

            mCrewOrderManager.getOrders(orderID, orderFilterAttr, new CrewOrderManager.OrderListener() {
                @Override
                public void onOrderReceived(List<CrewOrder> list) {

                    Timber.tag(TAG).w("Get order success.");

                    mIFEReturnData.setErrMsg("");
                    mIFEReturnData.setSuccess(true);
                    mIFEReturnData.setData(list);

                    e.onNext(mIFEReturnData);
                    e.onComplete();
                }

                @Override
                public void onCrewOrderError(Error error) {
                    if (FlightData.isEducationMode) {
                        Timber.tag(TAG).w("Get order fail.");
                        saveErrorMsg("GetOrder", generateErrorMsg(error));

                        mIFEReturnData.setErrMsg(generateErrorMsg(error));
                        mIFEReturnData.setSuccess(false);
                        mIFEReturnData.setData(null);

                        e.onNext(mIFEReturnData);
                        e.onComplete();
                    } else {
                        Timber.tag(TAG).w("Get order success.");

                        EducationCrewOrder educationCrewOrder = null;

                        if (orderStatus != null) {
                            if (orderStatus.equals(OrderStatus.ORDER_STATUS_OPEN)) {
                                educationCrewOrder = new EducationCrewOrder(OrderStatus.ORDER_STATUS_OPEN);
                            } else if (orderStatus.equals(OrderStatus.ORDER_STATUS_PROCESSING)) {
                                educationCrewOrder = new EducationCrewOrder(OrderStatus.ORDER_STATUS_PROCESSING);
                            }
                        }

                        mIFEReturnData.setErrMsg("");
                        mIFEReturnData.setSuccess(true);

                        if (orderStatus != null) {
                            mIFEReturnData.setData(educationCrewOrder.getOrders());
                        }

                        e.onNext(mIFEReturnData);
                        e.onComplete();
                    }
                }
            });
        });
    }

    /**
     * 將訂單狀態改為 processing
     *
     * @param crewOrder
     * @return
     */
    public Observable<IFEReturnData> initOrder(CrewOrder crewOrder) {

        Timber.tag(TAG).w("Init order.");

        return Observable.create(e -> crewOrder.initiateOrderProcessing(new InitiateOrderProcessingRequest(""), new CrewOrder.OrderProcessingInitiatedListener() {
            @Override
            public void onOrderProcessingInitiated() {
                Timber.tag(TAG).w("Init order success.");

                mIFEReturnData.setData(null);
                mIFEReturnData.setSuccess(true);
                mIFEReturnData.setErrMsg("");

                e.onNext(mIFEReturnData);
                e.onComplete();
            }

            @Override
            public void onCrewOrderError(Error error) {
                if (FlightData.isEducationMode) {
                    Timber.tag(TAG).w("Init order fail.");
                    saveErrorMsg("InitOrder", generateErrorMsg(error));

                    mIFEReturnData.setData(null);
                    mIFEReturnData.setSuccess(false);
                    mIFEReturnData.setErrMsg(generateErrorMsg(error));

                    e.onNext(mIFEReturnData);
                    e.onComplete();
                } else {
                    Timber.tag(TAG).w("Init order success.");

                    mIFEReturnData.setData(null);
                    mIFEReturnData.setSuccess(true);
                    mIFEReturnData.setErrMsg("");

                    e.onNext(mIFEReturnData);
                    e.onComplete();
                }
            }
        }));
    }

    /**
     * 把Order狀態從 processing 改為 open
     *
     * @param crewOrder
     * @return
     */
    public Observable<IFEReturnData> revertOrder(CrewOrder crewOrder) {

        Timber.tag(TAG).w("Revert order.");

        return Observable.create(e -> crewOrder.revertOrder(new RevertOrderRequest(""), new CrewOrder.RevertOrderListener() {
            @Override
            public void onOrderReverted() {
                Timber.tag(TAG).w("Revert order success.");

                mIFEReturnData.setData(null);
                mIFEReturnData.setSuccess(true);
                mIFEReturnData.setErrMsg("");

                e.onNext(mIFEReturnData);
                e.onComplete();
            }

            @Override
            public void onCrewOrderError(Error error) {
                if (FlightData.isEducationMode) {
                    Timber.tag(TAG).w("Revert order fail.");
                    saveErrorMsg("RevertOrder", generateErrorMsg(error));

                    mIFEReturnData.setData(null);
                    mIFEReturnData.setSuccess(false);
                    mIFEReturnData.setErrMsg(generateErrorMsg(error));

                    e.onNext(mIFEReturnData);
                    e.onComplete();
                } else {
                    Timber.tag(TAG).w("Revert order success.");

                    mIFEReturnData.setData(null);
                    mIFEReturnData.setSuccess(true);
                    mIFEReturnData.setErrMsg("");

                    e.onNext(mIFEReturnData);
                    e.onComplete();
                }
            }
        }));
    }

    /**
     * 取消訂單
     *
     * @param crewOrder
     * @return
     */
    public Observable<IFEReturnData> cancelOrder(CrewOrder crewOrder) {

        Timber.tag(TAG).w("Cancel order.");

        return Observable.create(e -> crewOrder.cancelOrder(new CancelOrderRequest(""), new CrewOrder.CancelOrderListener() {
            @Override
            public void onOrderCanceled() {
                Timber.tag(TAG).w("Cancel order success.");

                mIFEReturnData.setData(null);
                mIFEReturnData.setSuccess(true);
                mIFEReturnData.setErrMsg("");

                e.onNext(mIFEReturnData);
                e.onComplete();
            }

            @Override
            public void onCrewOrderError(Error error) {
                Timber.tag(TAG).w("Cancel order fail.");
                saveErrorMsg("CancelOrder", generateErrorMsg(error));

                mIFEReturnData.setData(null);
                mIFEReturnData.setSuccess(false);
                mIFEReturnData.setErrMsg(generateErrorMsg(error));

                e.onNext(mIFEReturnData);
                e.onComplete();
            }
        }));
    }

    /**
     * 完成訂單
     *
     * @param crewOrder
     * @return
     */
    public Observable<IFEReturnData> completeOrder(CrewOrder crewOrder) {

        Timber.tag(TAG).w("Complete order.");

        return Observable.create(e -> crewOrder.completeOrder(new CompleteOrderRequest("", false), new CrewOrder.CompleteOrderListener() {
            @Override
            public void onOrderCompleted() {
                Timber.tag(TAG).w("Complete order success.");

                mIFEReturnData.setData(null);
                mIFEReturnData.setSuccess(true);
                mIFEReturnData.setErrMsg("");

                e.onNext(mIFEReturnData);
                e.onComplete();
            }

            @Override
            public void onCrewOrderError(Error error) {
                Timber.tag(TAG).w("Complete order fail.");
                saveErrorMsg("CompleteOrder", generateErrorMsg(error));

                mIFEReturnData.setData(null);
                mIFEReturnData.setSuccess(false);
                mIFEReturnData.setErrMsg(generateErrorMsg(error));

                e.onNext(mIFEReturnData);
                e.onComplete();
            }
        }));
    }

    /**
     * 將訂單轉換為購物車
     *
     * @param crewOrder
     * @return
     */
    public Observable<IFEReturnData> convertToCart(CrewOrder crewOrder) {

        Timber.tag(TAG).w("Convert to cart.");

        return Observable.create(e -> crewOrder.convertToCart(new CrewOrder.ConvertToCartListener() {
            @Override
            public void onOrderConvertedToCart() {
                Timber.tag(TAG).w("Convert to cart success");

                mIFEReturnData.setData(null);
                mIFEReturnData.setSuccess(true);
                mIFEReturnData.setErrMsg("");

                e.onNext(mIFEReturnData);
                e.onComplete();
            }

            @Override
            public void onCrewOrderError(Error error) {
                Timber.tag(TAG).w("Convert to cart fail");
                saveErrorMsg("ConvertToCart", generateErrorMsg(error));

                mIFEReturnData.setData(null);
                mIFEReturnData.setSuccess(false);
                mIFEReturnData.setErrMsg(generateErrorMsg(error));

                e.onNext(mIFEReturnData);
                e.onComplete();
            }
        }));
    }

    /**
     * 建立購物車
     *
     * @param seatNo 座位號碼
     * @return
     */
    public Observable<IFEReturnData> createCart(String seatNo) {

        Timber.tag(TAG).w("Create cart.");

        CreateCartRequest createCartRequest = new CreateCartRequest();
        if (seatNo.length() > 3) {
            createCartRequest.setSeatClass(SeatClass.BUSINESS);
        }

        return Observable.create(e -> mCrewCartManager.createCart(seatNo, mIFEDBFunction.getCatalogID(), createCartRequest, new CrewCartManager.CrewCartListener() {
            @Override
            public void onCrewCartCreated(CrewCart crewCart, Map<FailedItem, CrewCartManager.Error> map) {
                Timber.tag(TAG).w("Create cart success");

                mIFEReturnData.setData(crewCart);
                mIFEReturnData.setSuccess(true);
                mIFEReturnData.setErrMsg("");

                e.onNext(mIFEReturnData);
                e.onComplete();
            }

            @Override
            public void onCrewCartAutoSubmitted(String s) {

            }

            @Override
            public void onError(CrewCartManager.Error error) {

                String s = "{\"cart_id\":\"1\",\"seat\":\"3A\",\"seat_class\":\"BUSINESS\",\"catalog_id\":\"1\"}";
                JSONObject jsonObject = null;

                try {
                    jsonObject = new JSONObject(s);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }

                if (FlightData.isEducationMode) {

                    Timber.tag(TAG).w("Create cart fail");
                    saveErrorMsg("CreateCart", generateErrorMsg(error));

                    mIFEReturnData.setData(new CrewCart(mContext, jsonObject));
                    mIFEReturnData.setSuccess(false);
                    mIFEReturnData.setErrMsg(generateErrorMsg(error));

                    e.onNext(mIFEReturnData);
                    e.onComplete();
                } else {
                    Timber.tag(TAG).w("Create cart success");

                    mIFEReturnData.setData(new CrewCart(mContext, jsonObject));
                    mIFEReturnData.setSuccess(true);
                    mIFEReturnData.setErrMsg("");

                    e.onNext(mIFEReturnData);
                    e.onComplete();
                }
            }
        }));
    }

    /**
     * 更新購物車，用於新增商品
     *
     * @param crewCart
     * @param updateItemWithQuantity
     * @return
     */
    public Observable<IFEReturnData> updateCart(CrewCart crewCart, Map<aero.panasonic.inflight.crew.services.cartmanagement.v1.model.Item, Integer> updateItemWithQuantity) {

        Timber.tag(TAG).w("Update cart.");

        return Observable.create(e -> {

            UpdateCartRequest updateCartRequest = new UpdateCartRequest();
            updateCartRequest.setItemWithQuantity(updateItemWithQuantity);

            crewCart.updateCart(updateCartRequest, new CrewCartManager.CrewCartUpdatedListener() {
                @Override
                public void onCrewCartUpdated(CrewCart crewCart, Map<FailedItem, CrewCartManager.Error> map) {

                    if (map.size() > 0) {
                        for (FailedItem failedItem : map.keySet()) {
                            Timber.tag(TAG).w("Update cart fail");

                            CrewCartManager.Error error = map.get(failedItem);

                            saveErrorMsg("UpdateCart", generateErrorMsg(error));

                            mIFEReturnData.setData(null);
                            mIFEReturnData.setSuccess(false);
                            mIFEReturnData.setErrMsg("IFE Qty not enough");

                            e.onNext(mIFEReturnData);
                        }
                        e.onComplete();
                    } else {
                        Timber.tag(TAG).w("Update cart success");

                        mIFEReturnData.setData(crewCart);
                        mIFEReturnData.setSuccess(true);
                        mIFEReturnData.setErrMsg("");

                        e.onNext(mIFEReturnData);
                        e.onComplete();
                    }
                }

                @Override
                public void onError(CrewCartManager.Error error) {
                    if (FlightData.isEducationMode) {

                        Timber.tag(TAG).w("Update cart fail");
                        saveErrorMsg("UpdateCart", generateErrorMsg(error));

                        mIFEReturnData.setErrMsg(generateErrorMsg(error));
                        mIFEReturnData.setSuccess(false);
                        mIFEReturnData.setData(null);

                        e.onNext(mIFEReturnData);
                        e.onComplete();
                    } else {
                        Timber.tag(TAG).w("Update cart success");

                        mIFEReturnData.setData(crewCart);
                        mIFEReturnData.setSuccess(true);
                        mIFEReturnData.setErrMsg("");

                        e.onNext(mIFEReturnData);
                        e.onComplete();
                    }
                }
            });
        });
    }

    /**
     * 清空購物車
     *
     * @return
     */
    public Observable<IFEReturnData> emptyCart(CrewCart crewCart) {
        return Observable.create(e -> crewCart.emptyCart(new CrewCartManager.CrewCartEmptiedListener() {
            @Override
            public void onCrewCartEmptied() {
                Timber.tag(TAG).w("Empty cart success");

                mIFEReturnData.setData("");
                mIFEReturnData.setSuccess(true);
                mIFEReturnData.setErrMsg("");

                e.onNext(mIFEReturnData);
                e.onComplete();
            }

            @Override
            public void onError(CrewCartManager.Error error) {
                Timber.tag(TAG).w("Empty cart fail");
                saveErrorMsg("EmptyCart", generateErrorMsg(error));

                mIFEReturnData.setErrMsg(generateErrorMsg(error));
                mIFEReturnData.setSuccess(false);
                mIFEReturnData.setData(null);

                e.onNext(mIFEReturnData);
                e.onComplete();
            }
        }));
    }

    /**
     * 將購物車轉換為訂單
     *
     * @param crewCart
     * @return
     */
    public Observable<IFEReturnData> submitCart(CrewCart crewCart) {

        Timber.tag(TAG).w("Submit cart.");

        return Observable.create(e -> crewCart.submitCart(new CrewCartManager.CrewCartSubmittedListener() {
            @Override
            public void onCrewCartSubmitted(String orderID) {
                Timber.tag(TAG).w("Submit cart success");

                if (!TextUtils.isEmpty(orderID)) {
                    mIFEReturnData.setData(orderID);
                    mIFEReturnData.setSuccess(true);
                    mIFEReturnData.setErrMsg("");
                } else {
                    mIFEReturnData.setData("");
                    mIFEReturnData.setSuccess(false);
                    mIFEReturnData.setErrMsg("");
                }

                e.onNext(mIFEReturnData);
                e.onComplete();
            }

            @Override
            public void onError(CrewCartManager.Error error) {
                Timber.tag(TAG).w("Submit cart fail");
                saveErrorMsg("SubmitCart", generateErrorMsg(error));

                mIFEReturnData.setErrMsg(generateErrorMsg(error));
                mIFEReturnData.setSuccess(false);
                mIFEReturnData.setData(null);

                e.onNext(mIFEReturnData);
                e.onComplete();
            }
        }));
    }

    /**
     * 依照座位號碼取得購物車
     *
     * @param seatNo 座位號碼
     * @return
     */
    public Observable<IFEReturnData> getCart(String seatNo) {

        Timber.tag(TAG).w("Get cart.");

        return Observable.create(e -> {
            GetCartRequest cartRequest = new GetCartRequest();
            if (seatNo.length() > 3) {
                cartRequest.setSeatClass(SeatClass.BUSINESS);
            }
            mCrewCartManager.getCarts(seatNo, cartRequest, new CrewCartManager.CrewCartReceivedListener() {
                @Override
                public void onCrewCartReceived(List<CrewCart> list) {

                    if (list.get(0).getItems().size() > 0) {
                        Timber.tag(TAG).w("Get cart success");

                        mIFEReturnData.setData(list.get(0));
                        mIFEReturnData.setSuccess(true);
                        mIFEReturnData.setErrMsg("");

                        e.onNext(mIFEReturnData);
                        e.onComplete();
                    } else {
                        Timber.tag(TAG).w("Get cart fail");

                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(mContext.getString(R.string.IFE_Communication_Error)).append(":\r\n").append(" ").append("Empty cart");

                        saveErrorMsg("GetCart", stringBuilder.toString());

                        mIFEReturnData.setErrMsg(stringBuilder.toString());
                        mIFEReturnData.setSuccess(false);
                        mIFEReturnData.setData(null);

                        e.onNext(mIFEReturnData);
                        e.onComplete();
                    }
                }

                @Override
                public void onError(CrewCartManager.Error error) {
                    String s = "{\"cart_id\":\"1\",\"seat\":\"3A\",\"seat_class\":\"BUSINESS\",\"catalog_id\":\"1\"}";
                    JSONObject jsonObject = null;

                    try {
                        jsonObject = new JSONObject(s);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }

                    if (FlightData.isEducationMode) {
                        Timber.tag(TAG).w("Get cart fail");
                        saveErrorMsg("GetCart", generateErrorMsg(error));

                        mIFEReturnData.setErrMsg(generateErrorMsg(error));
                        mIFEReturnData.setSuccess(false);
                        mIFEReturnData.setData(null);

                        e.onNext(mIFEReturnData);
                        e.onComplete();
                    } else {
                        Timber.tag(TAG).w("Get cart success");

                        EducationCrewCart educationCrewCart = new EducationCrewCart(mContext);
                        mIFEReturnData.setData(educationCrewCart.getCrewCart(seatNo));
                        mIFEReturnData.setSuccess(true);
                        mIFEReturnData.setErrMsg("");

                        e.onNext(mIFEReturnData);
                        e.onComplete();
                    }
                }
            });
        });
    }

    /**
     * 取得商品詳細資料
     *
     * @param sku
     * @return
     */
    public Observable<IFEReturnData> requestCatalogItem(String sku) {

        Timber.tag(TAG).w("Request catalog item.");

        return Observable.create(e -> mCatalogInventoryV1.requestCatalogItemBySku(sku, "eng", new CatalogInventoryV1.CatalogItemReceivedListener() {
            @Override
            public void onCatalogItemReceived(JSONArray jsonArray) {
                Timber.tag(TAG).w("Request catalog item success");

                populateCatalog(jsonArray);

                mIFEReturnData.setData(mCatalog);
                mIFEReturnData.setSuccess(true);
                mIFEReturnData.setErrMsg("");

                e.onNext(mIFEReturnData);
                e.onComplete();
            }

            @Override
            public void onError(CatalogInventoryV1.Error error) {
                if (FlightData.isEducationMode) {

                    Timber.tag(TAG).w("Request catalog item fail");
                    saveErrorMsg("RequestCatalogItem", generateErrorMsg(error));

                    mIFEReturnData.setErrMsg(generateErrorMsg(error));
                    mIFEReturnData.setSuccess(false);
                    mIFEReturnData.setData(null);

                    e.onNext(mIFEReturnData);
                    e.onComplete();
                } else {
                    Timber.tag(TAG).w("Request catalog item success");

                    mIFEReturnData.setData(null);
                    mIFEReturnData.setSuccess(true);
                    mIFEReturnData.setErrMsg("");

                    e.onNext(mIFEReturnData);
                    e.onComplete();
                }
            }
        }).executeAsyc());
    }

    /**
     * 調整IFE庫存
     *
     * @param sku         IFE ID
     * @param adjustCount 調整數量
     * @return
     */
    public Observable<IFEReturnData> adjustItem(String sku, int adjustCount) {

        Timber.tag(TAG).w("Adjust item.");

        return Observable.create(e -> mCatalogInventoryV1.adjustCatalogItemCount(sku, adjustCount, "eng", new CatalogInventoryV1.CatalogItemAdjustmentListener() {
            @Override
            public void onCatalogItemAdjusted(JSONArray jsonArray, CatalogInventoryV1.CatalogType catalogType) {
                Timber.tag(TAG).w("Adjust item success");

                mIFEReturnData.setData(null);
                mIFEReturnData.setSuccess(true);
                mIFEReturnData.setErrMsg("");

                e.onNext(mIFEReturnData);
                e.onComplete();
            }

            @Override
            public void onError(CatalogInventoryV1.Error error) {
                if (FlightData.isEducationMode) {
                    Timber.tag(TAG).w("Adjust item fail");
                    saveErrorMsg("AdjustItem", generateErrorMsg(error));

                    mIFEReturnData.setErrMsg(generateErrorMsg(error));
                    mIFEReturnData.setSuccess(false);
                    mIFEReturnData.setData(null);

                    e.onNext(mIFEReturnData);
                    e.onComplete();
                } else {
                    Timber.tag(TAG).w("Adjust item success");

                    mIFEReturnData.setData(null);
                    mIFEReturnData.setSuccess(true);
                    mIFEReturnData.setErrMsg("");

                    e.onNext(mIFEReturnData);
                    e.onComplete();
                }
            }
        }).executeAsyc());
    }

    public boolean errorProcessing(Activity activity, String errMsg) {
        if (MessageBox.show("", errMsg, mContext, "Ok")) {
            if (!MessageBox.show("", activity.getString(R.string.Try_Again), activity, "Yes", "No")) {
                if (MessageBox.show("", activity.getString(R.string.IFE_Disable), activity, "Ok")) {
                    FlightData.IFEConnectionStatus = false;
                    FlightData.OnlineAuthorize = false;
                    WifiUtil wifiUtil = new WifiUtil(mContext);
                    wifiUtil.disconnect();
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void populateCatalogs(JSONArray json) {
        Catalog catalog;
        for (int i = 0; i < json.length(); i++) {
            try {
                catalog = new Catalog(json.getJSONObject(i));
                if (catalog != null) {
                    mCatalogs.add(catalog);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void populateCatalog(JSONArray json) {
        if (json != null && json.length() > 0) {
            JSONObject jsonObject = json.optJSONObject(0);
            JSONArray jsonArray = jsonObject.optJSONArray("items");
            if (jsonArray != null && jsonArray.length() > 0) {
                Catalog catalog;
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        catalog = new Catalog(jsonArray.getJSONObject(i));
                        if (catalog != null) {
                            mCatalog = catalog;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private String generateErrorMsg(Object error) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(mContext.getString(R.string.IFE_Communication_Error)).append(":\r\n").append(" ");
        if (error instanceof Error) {
            stringBuilder.append(Error.getErrorMessage((Error)error));
        } else if (error instanceof CrewCartManager.Error) {
            stringBuilder.append(CrewCartManager.Error.getErrorMessage((CrewCartManager.Error)error));
        } else if (error instanceof InFlight.Error) {
            stringBuilder.append(InFlight.Error.getErrorMessage((InFlight.Error)error));
        } else if (error instanceof CatalogInventoryV1.Error) {
            stringBuilder.append(CatalogInventoryV1.Error.getErrorMessage((CatalogInventoryV1.Error)error));
        }
        return stringBuilder.toString();
    }
}
