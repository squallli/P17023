package tw.com.regalscan.component;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextThemeWrapper;

import tw.com.regalscan.R;

/**
 * Created by ponylai on 2014/12/19.
 */
public class MessageBox {

    private static boolean mResultBoolean;
    private static String mResultString;

    public static class LoopStopException extends RuntimeException {

        //或者：extends Throwable
        public LoopStopException() {
        }

        public LoopStopException(String message) {
            super(message);
        }
    }


    public enum Icon {

        /**
         * 警告圖示
         */
        Alert(android.R.drawable.ic_dialog_alert),

        /**
         * 提示圖示
         */
        Info(android.R.drawable.ic_dialog_info);

        // 定義私有變數
        private int nCode;

        // 建構函數，列舉類型只能是私有
        Icon(int _nCode) {
            this.nCode = _nCode;
        }

        private int getIcon() {
            return this.nCode;
        }
    }

    // 按確定後不做事
    public static boolean show(String Title, String Message, Icon icon, Context contex, final boolean isHandler) {

        // make a handler that throws a runtime exception when a message is received
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(android.os.Message mesg) {
                throw new LoopStopException();
            }
        };

        AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(new ContextThemeWrapper(contex, R.style.AppTheme));
        MyAlertDialog.setTitle(Title);
        MyAlertDialog.setMessage(Message);
        MyAlertDialog.setIcon(icon.getIcon());
        MyAlertDialog.setCancelable(false);

        DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //如果不做任何事情 就會直接關閉 對話方塊
//                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                //imm.showSoftInput(mEditText, 0);

                mResultBoolean = true;
                if (isHandler) {
                    handler.sendMessage(handler.obtainMessage());
                }


            }
        };

        MyAlertDialog.setNeutralButton("確認", OkClick);
        MyAlertDialog.show();

//        Util.barcodeClass.unregisterReceiver();

        if (isHandler) {

            // loop till a runtime exception is triggered.
            try {
                Looper.loop();
            } catch (LoopStopException e2) {
            } catch (Exception ex) {
            }
        }

//        Util.barcodeClass.registerReceiver();

        return mResultBoolean;

    }

    public static boolean show(String Title, String Message, Context contex, final boolean isHandler) {

        // make a handler that throws a runtime exception when a message is received
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(android.os.Message mesg) {
                throw new LoopStopException();
            }
        };

        AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(new ContextThemeWrapper(contex, R.style.AppTheme));
        MyAlertDialog.setTitle(Title);
        MyAlertDialog.setMessage(Message);
        MyAlertDialog.setCancelable(false);

        DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //如果不做任何事情 就會直接關閉 對話方塊
//                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                //imm.showSoftInput(mEditText, 0);

                mResultBoolean = true;
                if (isHandler) {
                    handler.sendMessage(handler.obtainMessage());
                }


            }
        };

        MyAlertDialog.setNeutralButton("確認", OkClick);
        MyAlertDialog.show();

//        Util.barcodeClass.unregisterReceiver();

        if (isHandler) {

            // loop till a runtime exception is triggered.
            try {
                Looper.loop();
            } catch (LoopStopException e2) {
            } catch (Exception ex) {
            }
        }

//        Util.barcodeClass.registerReceiver();

        return mResultBoolean;

    }


    // 按確定後做事
    public static boolean show(String Title, String Message, Icon icon, Context contex, String YesString) {

        // make a handler that throws a runtime exception when a message is received
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(android.os.Message mesg) {
                throw new LoopStopException();
            }
        };

        AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(new ContextThemeWrapper(contex, R.style.AppTheme));
        MyAlertDialog.setTitle(Title);
        MyAlertDialog.setMessage(Message);
        MyAlertDialog.setIcon(icon.getIcon());
        MyAlertDialog.setCancelable(false);

        DialogInterface.OnClickListener YesClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                mResultBoolean = true;
                handler.sendMessage(handler.obtainMessage());

            }
        };

        MyAlertDialog.setPositiveButton(YesString, YesClick);
        MyAlertDialog.show();

//        Util.barcodeClass.unregisterReceiver();

        // loop till a runtime exception is triggered.
        try {
            Looper.loop();
        } catch (LoopStopException e2) {
        } catch (Exception ex) {
        }

//        Util.barcodeClass.registerReceiver();

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mResultBoolean;

    }

    public static boolean show(String Title, String Message, Context contex, String YesString) {

        // make a handler that throws a runtime exception when a message is received
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(android.os.Message mesg) {
                throw new LoopStopException();
            }
        };

        AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(new ContextThemeWrapper(contex, R.style.AppTheme));
        MyAlertDialog.setTitle(Title);
        MyAlertDialog.setMessage(Message);
        MyAlertDialog.setCancelable(false);

        DialogInterface.OnClickListener YesClick = (dialog, which) -> {

            mResultBoolean = true;
            handler.sendMessage(handler.obtainMessage());

        };

        MyAlertDialog.setPositiveButton(YesString, YesClick);
        MyAlertDialog.show();

//        Util.barcodeClass.unregisterReceiver();

        // loop till a runtime exception is triggered.
        try {
            Looper.loop();
        } catch (LoopStopException e2) {
        } catch (Exception ex) {
        }

//        Util.barcodeClass.registerReceiver();

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mResultBoolean;

    }


    // 確定&取消
    public static boolean show(String Title, String Message, Icon icon, Context contex, String YesString, String NoString) {

        // make a handler that throws a runtime exception when a message is received
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(android.os.Message mesg) {
                throw new LoopStopException();
            }
        };

        AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(new ContextThemeWrapper(contex, R.style.AppTheme));
        MyAlertDialog.setTitle(Title);
        MyAlertDialog.setMessage(Message);
        MyAlertDialog.setIcon(icon.getIcon());
        MyAlertDialog.setCancelable(false);

        DialogInterface.OnClickListener YesClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                mResultBoolean = true;
                handler.sendMessage(handler.obtainMessage());

            }
        };

        DialogInterface.OnClickListener NoClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mResultBoolean = false;
                handler.sendMessage(handler.obtainMessage());
            }
        };

        MyAlertDialog.setPositiveButton(YesString, YesClick);
        MyAlertDialog.setNegativeButton(NoString, NoClick);
        MyAlertDialog.show();

//        Util.barcodeClass.unregisterReceiver();

        // loop till a runtime exception is triggered.
        try {
            Looper.loop();
        } catch (LoopStopException e2) {
        } catch (Exception ex) {
        }

//        Util.barcodeClass.registerReceiver();

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mResultBoolean;

    }

    public static boolean show(String Title, String Message, Context contex, String YesString, String NoString) {

        // make a handler that throws a runtime exception when a message is received
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(android.os.Message mesg) {
                throw new LoopStopException();
            }
        };

        AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(new ContextThemeWrapper(contex, R.style.AppTheme));
        MyAlertDialog.setTitle(Title);
        MyAlertDialog.setMessage(Message);
        MyAlertDialog.setCancelable(false);

        DialogInterface.OnClickListener YesClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                mResultBoolean = true;
                handler.sendMessage(handler.obtainMessage());

            }
        };

        DialogInterface.OnClickListener NoClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mResultBoolean = false;
                handler.sendMessage(handler.obtainMessage());
            }
        };

        MyAlertDialog.setPositiveButton(YesString, YesClick);
        MyAlertDialog.setNegativeButton(NoString, NoClick);
        MyAlertDialog.show();

//        Util.barcodeClass.unregisterReceiver();

        // loop till a runtime exception is triggered.
        try {
            Looper.loop();
        } catch (LoopStopException e2) {
        } catch (Exception ex) {
        }

//        Util.barcodeClass.registerReceiver();

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mResultBoolean;

    }


    public static String ListDialog(String Title, final String[] Item, Context contex) {

        // make a handler that throws a runtime exception when a message is received
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(android.os.Message mesg) {
                throw new LoopStopException();
            }
        };

        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(contex, R.style.ListDialogTheme);
//        AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(contex);
        AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(new ContextThemeWrapper(contex, R.style.AppTheme));
        MyAlertDialog.setTitle(Title);
        MyAlertDialog.setCancelable(true);

        DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                mResultString = Item[which];
                handler.sendMessage(handler.obtainMessage());
            }
        };

        MyAlertDialog.setItems(Item, OkClick);
        MyAlertDialog.show();

//        Util.barcodeClass.unregisterReceiver();

        // loop till a runtime exception is triggered.
        try {
            Looper.loop();
        } catch (LoopStopException e2) {
        } catch (Exception ex) {
        }

//        Util.barcodeClass.registerReceiver();

        return mResultString;

    }

    public static void drawerShow(String Title, String Message, Context context, String YesString, String NoString, IMsgBoxOnClick msgBoxOnClick) {

        AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AppTheme));
        MyAlertDialog.setTitle(Title);
        MyAlertDialog.setMessage(Message);
        MyAlertDialog.setCancelable(false);

        DialogInterface.OnClickListener YesClick = (dialog, which) -> msgBoxOnClick.onYesClick();

        DialogInterface.OnClickListener NoClick = (dialog, which) -> msgBoxOnClick.onNoClick();

        MyAlertDialog.setPositiveButton(YesString, YesClick);
        MyAlertDialog.setNegativeButton(NoString, NoClick);
        MyAlertDialog.show();
    }

    public static void drawerShow(String Title, String Message, Context context, String YesString, IMsgBoxOnClick msgBoxOnClick) {

        AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AppTheme));
        MyAlertDialog.setTitle(Title);
        MyAlertDialog.setMessage(Message);
        MyAlertDialog.setCancelable(false);

        DialogInterface.OnClickListener YesClick = (dialog, which) -> msgBoxOnClick.onYesClick();

        MyAlertDialog.setPositiveButton(YesString, YesClick);
        MyAlertDialog.show();
    }

    public static void threeBtnShow(String Title, String Message, Context context, String YesString, String NoString, String CancelString, IMsgBoxThreeBtnClick msgBoxThreeBtnClick) {
        AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AppTheme));
        MyAlertDialog.setTitle(Title);
        MyAlertDialog.setMessage(Message);
        MyAlertDialog.setCancelable(false);

        DialogInterface.OnClickListener YesClick = (dialog, which) -> msgBoxThreeBtnClick.onYesClick();

        DialogInterface.OnClickListener NoClick = (dialog, which) -> msgBoxThreeBtnClick.onNoClick();

        DialogInterface.OnClickListener CancelClick = (dialog, which) -> msgBoxThreeBtnClick.onCancelClick();

        MyAlertDialog.setPositiveButton(YesString, YesClick);
        MyAlertDialog.setNegativeButton(NoString, NoClick);
        MyAlertDialog.setNeutralButton(CancelString, CancelClick);
        MyAlertDialog.show();
    }
}
