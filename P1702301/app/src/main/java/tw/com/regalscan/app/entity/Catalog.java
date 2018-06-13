package tw.com.regalscan.app.entity;

import java.util.HashMap;
import java.util.Iterator;

import aero.panasonic.inflight.services.utils.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by guos on 9/22/15.
 */
public class Catalog {

//    private static final String TAG = Catalog.class.getSimpleName();

//    [
//{
//    "title":{
//    "eng": "Soda" // returns default language title provided by airline data
//},
//    "sku": "12344",
//        "initial_count":10, //  count when initial inventory was loaded on the aircraft.
//        "last_adjustment":-3,
//        "last_adjustment_time":,
//    "last_adjustment_by":"crew1"
//    "current_count":2, // Current total inventory including pending orders
//        "pending_fulfillment":1
//},
//    {
//        "title":{
//        "eng": "chips"
//    },
//        "sku": "149839",
//            "initial_count":10,
//            "current_count":7,
//    },
//    {
//        "title":{
//        "eng": "Juice"
//    },
//        "sku": "34393",
//            "initial_count":10,
//            "current_count":7,
//    }
//    ]

//    [{"current_count":-1,"initial_count":-1,"sku":"#169","title":{"zhk":"瑷嘉莎吊墜項鏈套裝"}},
//    {"current_count":-1,"initial_count":-1,"sku":"#170","title":{"zhk":"瑷嘉莎甜美小皮包"}},
//    {"current_count":-1,"initial_count":-1,"sku":"#202","title":{"zhk":"万宝路"}},
//    {"current_count":-1,"initial_count":-1,"sku":"#203","title":{"zhk":"白金万宝路"}},
//    {"current_count":-1,"initial_count":-1,"sku":"#205","title":{"zhk":"爱喜蓝 200支"}},
//    {"current_count":412,"initial_count":412,"sku":"1788","title":{"zhk":"科颜氏 高保湿面霜"}},
//    {"current_count":412,"initial_count":412,"sku":"2021","title":{"zhk":"科顏氏祛皱紧肤精华液"}},
//    {"current_count":412,"initial_count":412,"sku":"2260","title":{"zhk":"阿玛尼粉饼粉盒"}},
//    {"current_count":-1,"initial_count":-1,"sku":"2263","title":{"zhk":"兰蔻 最佳香氛锦盒"}},
//    {"current_count":-1,"initial_count":-1,"sku":"2502","title":{"zhk":"科莱丽 声波声彻净颜仪Mia2"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000037","title":{"zhk":"外交官双肩背包DB-717L"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000038","title":{"zhk":"外交官配皮拉杆箱DL-112-1"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000040","title":{"zhk":"东航女士指甲钳"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000115","title":{"zhk":"美致1:14兰博基尼蝙蝠电动遥控车"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000118","title":{"zhk":"维多利亚十字勋章高档扳扣皮带5302"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000120","title":{"zhk":"奔腾婴儿理发器PL403"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000121","title":{"zhk":"乐扣乐扣3合1魔力防滑衣架（10只装）"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000129","title":{"zhk":"奔腾PS2208剃须刀（俊爽）"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000130","title":{"zhk":"菲特尼斯TBJ-001迷你踏步机"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000131","title":{"zhk":"百易特YP-8058A特厚型真空压缩袋8件套"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000133","title":{"zhk":"欧姆龙血糖仪HEA-231"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000136","title":{"zhk":"尚朋堂电压力煲YS-PC5018G"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000141","title":{"zhk":"BlackHole黑洞系列三折伞"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000143","title":{"zhk":"GUESS手提包套装"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000145","title":{"zhk":"飞利浦电吹风BHC1005"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000148","title":{"zhk":"莱克车载空气净化器KJ101"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000149","title":{"zhk":"莱克车载吸尘器 VC-PW1005Q"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000150","title":{"zhk":"莱克金属卷烫睫毛器HC-EB101"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000154","title":{"zhk":"莱克挂烫机GT1021"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000163","title":{"zhk":"东航小号丝巾"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000164","title":{"zhk":"东航16G飞机U盘"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000165","title":{"zhk":"东航大号丝巾"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000167","title":{"zhk":"东航男士指甲钳"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000190","title":{"zhk":"Zwilling双立人 ZWILLING Prime双耳煎炒锅24cm 64067-240-922"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000191","title":{"zhk":"宝马BMW 女式手表不锈钢浅色表盘80262311775"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000192","title":{"zhk":"宝马BMW 男士手表不锈钢黑色表盘80262318663"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000193","title":{"zhk":"缤特力 Backbeat Go2蓝牙耳机 黑色"}},
//    {"current_count":-1,"initial_count":-1,"sku":"a100000194","title":{"z

  private HashMap<String, String> titles;

  private String sku;
  private String productId = "";
  private String catalogId = "";
  private String seatClass = "";
  private String itemId;
  private int initialCount;
  private int currentCount;
  private int lastAdjustment;
  private int lastAdjustmentTime;
  private int pendingFulfillment;
  private String lastAdjustmentBy;
  private int quantityToAdd;

  public Catalog() {
    titles = new HashMap<>();
  }

  public Catalog(JSONObject jsonObject) {
    this();

    try {
      JSONObject titleJson = jsonObject.optJSONObject("title");
      if (titleJson != null) {
        Iterator<String> it = titleJson.keys();
        while (it.hasNext()) {
          String key = it.next();
          titles.put(key, titleJson.getString(key));
        }
      }
      sku = jsonObject.optString("sku", "");
      itemId = jsonObject.optString("item_id", "");
      catalogId = jsonObject.optString("catalog_id", "");
      seatClass = jsonObject.optString("seat_class", "").toLowerCase();
      productId = jsonObject.optString("product_id", "");
      initialCount = jsonObject.optInt("initial_count", -1);
      currentCount = jsonObject.optInt("current_count", -1);
      lastAdjustment = jsonObject.optInt("last_adjustment", 0);
      lastAdjustmentTime = jsonObject.optInt("last_adjustment_time", -1);
      pendingFulfillment = jsonObject.optInt("pending_fulfillment", -1);
      lastAdjustmentBy = jsonObject.optString("last_adjustment_by", "");

    } catch (JSONException e) {
      Log.exception(e);
    }
  }

  public String getTitle() {
    if (titles.containsKey("eng")) {
      return titles.get("eng");
    }
    for (String key : titles.keySet()) {
      return titles.get(key);
    }
    return "title";
  }

  public String getSku() {
    return sku;
  }

  public String getItemId() {
    return itemId;
  }

  public int getInitialCount() {
    return initialCount;
  }

  public void setInitialCount(int n) {
    initialCount = n;
  }

  public int getCurrentCount() {
    return currentCount;
  }

  public void setCurrentCount(int n) {
    currentCount = n;
  }

  public int getLastAdjustment() {
    return lastAdjustment;
  }

  public int getLastAdjustmentTime() {
    return lastAdjustmentTime;
  }

  public int getPendingFulfillment() {
    return pendingFulfillment;
  }

  public String getLastAdjustmentBy() {
    return lastAdjustmentBy;
  }

  public String getProductId() {
    return productId;
  }

  public void setProductId(String productId) {
    this.productId = productId;
  }

  public String getCatalogId() {
    return catalogId;
  }

  public void setCatalogId(String catalogId) {
    this.catalogId = catalogId;
  }

  public String getSeatClass() {
    return seatClass;
  }

  public void setSeatClass(String seatClass) {
    this.seatClass = seatClass;
  }

  public void setTitles(HashMap<String, String> titles) {
    this.titles = titles;
  }

  public void setSku(String sku) {
    this.sku = sku;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  public int getQuantityToAdd() {
    return quantityToAdd;
  }

  public void setQuantityToAdd(int n) {
    quantityToAdd = n;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("SeatClass: " + seatClass);
    sb.append(", sku: " + sku);
    sb.append(", itemId: " + itemId);
    sb.append(", catalogId: " + catalogId);
    sb.append(", productId: " + productId);
    sb.append(", quantityToAdd: " + quantityToAdd);
    return sb.toString();
  }

  public String toUpdateSQL() {
    String SQL = "UPDATE ALLPARTS SET ItemID = '" + getItemId() + "' " + "WHERE IFEID = '" + getSku() + "'";
    return SQL;
  }
}
