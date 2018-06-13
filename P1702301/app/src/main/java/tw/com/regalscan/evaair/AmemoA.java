package tw.com.regalscan.evaair;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.text.SimpleDateFormat;
import java.util.Date;

import tw.com.regalscan.utils.Tools;


public class AmemoA extends Activity{

    // IFE Order State:
//    1. open: to processing
//    2. processing: open, cancel, complete
//    3. cancel: 不能調整
//    4. complete: cancel(refund), 退還inventory

    /*
    * 6/16再進行版本號更改
    * 給EVA後回歸V1.00.00
    * */

    // listview的滑動摩擦係數 (滑動速度體感控制)
//    itemListView.setFriction(ViewConfiguration.getScrollFriction() * 123);

//    (離線)POS可販售數量= POSStock
//    (連線)IFE
//    basket內使用者調整的數量= SalesQty
//
//    商品庫存量不足會直接回錯誤訊息
//    贈品庫存量不足會回正常物品，要比對PosStock與SalesQty
//
//
//    DiscountFlag: 有沒有使用到折扣
//    DiscountType: 當下真正使用的折扣
//    DiscountList: 所有滿足條件的折扣身分(包含主被動)
//
//
//    Item細項內Origin和USD Price會為相同
//    打折後的金額看USDAmount


    /* -------------------------購物車搜尋條碼都要EditText內加上actionSend------------------------ */

    //spinner: height=50dp


//    //小視窗按空白處不會消失
//    dialog.setCanceledOnTouchOutside(false);


//    //back鍵和alert dialog
//    setCancelable(false);


//    //activiy按空白處不會消失
//    this.setFinishOnTouchOutside(false);
//    @Override
//    public void onBackPressed() { }


//    英文字母預設全大寫
//    android:inputType="textCapCharacters"


//    只允許輸入中文與數字
//    android:digits="@string/english_and_number_input"


//    //edit Text單行
//    替代single line
//    android:maxLines="1"


//    //第一個英文字母大寫
//    editor.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);


    //設定textView底線
//    String udata="Underlined Text";
//    SpannableString content = new SpannableString(udata);
//    content.setSpan(new UnderlineSpan(), 0, udata.length(), 0);
//    mTextView.setText(content);


    //單行顯示textview
//    android:ellipsize="end"
//    android:singleLine="true"



    Date time=new Date(System.currentTimeMillis());
    SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
    String startTime=  sdf.format(time);


    //點空白處自動隱藏鍵盤
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                View view = getCurrentFocus();
                Tools.hideSoftKeyboard(ev, view, this);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    //鎖返回鍵和menu
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean result;

        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                result = false;
                break;
            case KeyEvent.KEYCODE_MENU:
                result = false;
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                result = true;
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                result = true;
                break;
            default:
                result = super.onKeyDown(keyCode, event);
                break;
        }

        return result;
    }
}
