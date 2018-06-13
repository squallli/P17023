package tw.com.regalscan.evaground;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import tw.com.regalscan.R;
import tw.com.regalscan.app.entity.News;

/**
 * Created by rguser on 2017/2/20.
 */

public class AnnouncementActivity extends Activity {

  private TextView matxtTitle = null, mContent;
  private Button mabtnConfirm;


  private void InitializeComponent() {
    matxtTitle = findViewById(R.id.atxtTitle);
    mContent = findViewById(R.id.tv_content);

    mabtnConfirm = findViewById(R.id.abtnConfirm);
    mabtnConfirm.setOnClickListener(mabtnConfirml_OnClickListener);
  }

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.announcement_activity);
    this.setFinishOnTouchOutside(false);

    InitializeComponent();

    Bundle bundle = this.getIntent().getExtras();
    if (bundle != null) {
      String temp = bundle.getString("announcementTitle");
      String content = bundle.getString("content");
      matxtTitle.setText(temp);
      mContent.setText(content);
    }

    News news = getIntent().getParcelableExtra("News");

    if (news != null) {
      String title = news.getTITLE();
      String content = news.getCONTENT();

      matxtTitle.setText(title);
      mContent.setText(content);
    }
  }

  private View.OnClickListener mabtnConfirml_OnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      AnnouncementActivity.this.finish();
    }
  };
}
