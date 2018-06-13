package tw.com.regalscan.evaground;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import tw.com.regalscan.R;

/**
 * Created by tp00169 on 2017/3/17.
 */

public class RecieptDetailActivity extends Activity {

  private TextView mtxtTopic, mtxtSaleState;
  private Button btnConfirm, btnCancel;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.reciept_detail_activity);
    this.setFinishOnTouchOutside(false);

    String receiptInfo = (String) getIntent().getExtras().get("receiptInfo");
    String saleState = (String) getIntent().getExtras().get("saleState");

    //商品標題
    mtxtTopic = findViewById(R.id.txtTopic);
    mtxtTopic.setText(receiptInfo);

    //商品販售狀態
    mtxtSaleState = findViewById(R.id.txtChangeState);
    if (saleState.equals("Sale") ) {
      mtxtSaleState.setText("Unsale ?");
    } else if (saleState.equals("UnSale")) {
      mtxtSaleState.setText("Sale ?");
    }

    //按鈕
    btnConfirm = findViewById(R.id.btnConfirm);
    btnConfirm.setOnClickListener(mbtnconfirm_OnClickListener);
    btnCancel = findViewById(R.id.btnCancel);
    btnCancel.setOnClickListener(mbtncancel_OnClickListener);

  }

  private View.OnClickListener mbtnconfirm_OnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      Intent backIntent = getIntent();
      backIntent.putExtra("receiptInfo", mtxtTopic.getText().toString());

      setResult(RESULT_OK, backIntent);
      RecieptDetailActivity.this.finish();
    }
  };


  private View.OnClickListener mbtncancel_OnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      RecieptDetailActivity.this.finish();
    }
  };
}
