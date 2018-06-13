package tw.com.regalscan.component;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by Heidi on 2017/8/28.
 */

public class SpinnerHideItemAdapter extends ArrayAdapter<String> {

  private int hidingItemIndex;

  public SpinnerHideItemAdapter(Context context, int textViewResourceId, ArrayList<String> objects, int hidingItemIndex) {
    super(context, textViewResourceId, objects);
    this.hidingItemIndex = hidingItemIndex;
  }

  @Override
  public View getDropDownView(int position, View convertView, ViewGroup parent) {
    View v;
    if (position == hidingItemIndex) {
      TextView tv = new TextView(getContext());
      tv.setVisibility(View.GONE);
      v = tv;
    } else {
      v = super.getDropDownView(position, null, parent);
    }
    return v;
  }
}
