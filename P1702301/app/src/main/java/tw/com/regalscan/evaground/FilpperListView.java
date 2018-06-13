package tw.com.regalscan.evaground;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ListView;

public class FilpperListView extends ListView {
    private float myLastX = -1;
    private float myLastY = -1;
    private boolean delete = false;
    //自定义的滑动删除监听
    private FilpperDeleteListener filpperDeleterListener;

    public FilpperListView(Context context) {
        super(context);
    }

    public FilpperListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 获得第一个点的x坐标
                myLastX = ev.getX(0);
                myLastY = ev.getY(0);
                break;

            case MotionEvent.ACTION_MOVE:

                // 得到最后一个点的坐标
                float deltaX = ev.getX(ev.getPointerCount() - 1) - myLastX;
                float deltaY = Math.abs(ev.getY(ev.getPointerCount() - 1) - myLastY);

//                // 可以滑动删除的条件：横向滑动大于100，竖直差小于50
//                if (deltaX > 100.0 && deltaY < 50) {
//                    delete = true;
//                }

                delete = (deltaX > 100.0 && deltaY < 50);

                break;

            case MotionEvent.ACTION_UP:

                if (delete && filpperDeleterListener != null) {
                    filpperDeleterListener.filpperDelete(myLastX, myLastY);
                }

                reset();

                break;
        }
        return super.onTouchEvent(ev);
    }

    public void reset() {
        delete = false;
        myLastX = -1;
        myLastY = -1;
    }

    //    public void setFilpperDeleteListener(FilpperDeleteListener f) {
//        filpperDeleterListener = f;
//    }
//
    //自定义的接口
    public interface FilpperDeleteListener {
        void filpperDelete(float xPosition, float yPosition);
    }

    //執行刪除動畫
    public boolean deleteItem(int position) {

        //根据坐标获得滑动删除的item的index
        final int index = position;

        //一下两步是获得该条目在屏幕显示中的相对位置，直接根据index删除会空指針异常。因为listview中的child只有当前在屏幕中显示的才不会为空
        int firstVisiblePostion = this.getFirstVisiblePosition();
        View view = this.getChildAt(index - firstVisiblePostion);

        TranslateAnimation tranAnimation = new TranslateAnimation(0, this.getWidth(), 0, 0);
        tranAnimation.setDuration(100);
        tranAnimation.setFillEnabled(true);
        tranAnimation.setFillAfter(true);
        view.startAnimation(tranAnimation);

        //当动画播放完毕后，删除。否则不会出现动画效果（自己试验的）。
        tranAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
//                //删除一个item
//                adapter.removeItem(index);
//                adapter.notifyDataSetChanged();

            }
        });

        return true;
    }

}
