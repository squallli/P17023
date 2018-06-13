package tw.com.regalscan.evaground;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import tw.com.regalscan.R;
import tw.com.regalscan.db02.DBQuery;

public class ItemListAdapter extends BaseAdapter {

    private final int TYPE_ONE = 0, TYPE_TWO = 1, TYPE_COUNT = 2;
    private String searchRange = "";
    private String user;
    private Context context = null;
    private int total = 0;
    private int drawer = 0;
    Activity mActivity;
    PackageManager packageManager = null;

    ItemHolder itemholder = null;

//  ImageLoader mImageLoader = ImageLoader.getInstance();

    private ImageLoader mImageLoader = ImageLoader.getInstance();

    private ItemInfo item;

    DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.drawable.icon_loading)
            .cacheInMemory(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    //Items
    List<ItemInfo> itemList = new ArrayList<>();//畫面顯示用的
    List<ItemInfo> itemListEvaBlack = new ArrayList<>();//畫面顯示用的
    List<List<ItemInfo>> dataItemList = new ArrayList<>();//儲存drawer資料
    List<ItemInfo> hasChangedItemList = new ArrayList<>();//儲存更改過的原始資料
    List<List<ItemInfo>> saveData = new ArrayList<>();

    //Preorder receipt
    List<PreorderReceiptInfo> preorderList = new ArrayList<>();///畫面顯示用的
    List<PreorderReceiptInfo> dataPreorderList = new ArrayList<>();//儲存drawer資料
    List<PreorderReceiptInfo> savePreOrderData = new ArrayList<>();

    /**
     * 設定是否更改文字顏色
     */
    boolean ModifiedTextColor = false;

    public ItemListAdapter(Activity activity,Context c, String user) {
        context = c;
        mActivity=activity;
        this.user = user;
        //根据context上下文加载布局，这里的是Demo17Activity本身，即this

        new Thread() {
            public void run() {
                ImageLoaderConfiguration loaderConfiguration = new ImageLoaderConfiguration.Builder(context).defaultDisplayImageOptions(options).build();
                mImageLoader.init(loaderConfiguration);
            }}.start();

    }

    public ItemListAdapter(Context c, PackageManager pm, List<ItemInfo> list) {
        context = c;
        packageManager = pm;
        itemList = list;
    }

    //Adapter的列數總數
    @Override
    public int getCount() {
        return itemList.size() + preorderList.size();
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < itemList.size()) {
            return TYPE_ONE;
        } else {
            return TYPE_TWO;
        }
    }

    //某列的內容
    @Override
    public ItemInfo getItem(int position) {
        return itemList.get(position);
    }

    //取得某一列的id
    @Override
    public long getItemId(int position) {
        return position;
    }

    //修改某一列View的內容
    @Override
    public View getView(int position, View v, ViewGroup parent) {
        try {
            PreorderHolder preorderHolder = null;
            int type = getItemViewType(position);
            if (v == null) {
                switch (type) {
                    case TYPE_ONE:
                        v = LayoutInflater.from(context).inflate(R.layout.item_list_view, null);

                        itemholder = new ItemHolder();

                        itemholder.itemInfo = v.findViewById(R.id.txtItemInfo);
                        itemholder.originNum = v.findViewById(R.id.txtOrigin);
                        itemholder.originNum2 = v.findViewById(R.id.txtOrigin2);
                        itemholder.newNum = v.findViewById(R.id.txtNew);
                        itemholder.damage = v.findViewById(R.id.txtDamage);
                        itemholder.imgItemButton = v.findViewById(R.id.imgItem);
                        if (user.equals("EGAS")) {
                            ViewGroup.LayoutParams params = itemholder.originNum2.getLayoutParams();
                            params.width = 0;
                            params.height = 0;
                            itemholder.originNum2.setLayoutParams(params);
                        }

                        v.setTag(itemholder);
                        break;
                    case TYPE_TWO:
                        v = LayoutInflater.from(context).inflate(R.layout.item_list_view_preorder_receipt, null);
                        preorderHolder = new PreorderHolder();
                        preorderHolder.preorderReceiptInfo = v.findViewById(R.id.txtreceipt);
                        preorderHolder.saleState = v.findViewById(R.id.txtSaleState);
                        preorderHolder.imgButton = v.findViewById(R.id.imgItem);

                        v.setTag(preorderHolder);
                        break;
                }
            } else {
                switch (type) {
                    case TYPE_ONE:
                        itemholder = (ItemHolder) v.getTag();
                        break;
                    case TYPE_TWO:
                        preorderHolder = (PreorderHolder) v.getTag();
                        break;
                }
            }

            switch (type) {
                case TYPE_ONE:
                    item = itemList.get(position);

                    //設定文字
                    itemholder.itemInfo.setText(item.getDrawer() + " - " + item.getItemCode() + "\n" + item.getItemInfo());
                    itemholder.originNum.setText(String.valueOf(item.getOriginNum()));
                    itemholder.originNum2.setText(String.valueOf(item.getEgascheck()));
                    if (user.equals("EGAS")) {
                        itemholder.newNum.setText(String.valueOf(item.getNewNum()));
                        // 更改過後的文字改成紅色
                        if (item.getNewNum() != item.getOriginNum() || item.getDamage()>0) {
                            itemholder.itemInfo.setTextColor(Color.RED);
                            itemholder.originNum.setTextColor(Color.RED);
                            itemholder.originNum2.setTextColor(Color.RED);
                            itemholder.newNum.setTextColor(Color.RED);
                            itemholder.damage.setTextColor(Color.RED);
                        } else {
                            itemholder.itemInfo.setTextColor(Color.BLACK);
                            itemholder.originNum.setTextColor(Color.BLACK);
                            itemholder.originNum2.setTextColor(Color.BLACK);
                            itemholder.newNum.setTextColor(Color.BLACK);
                            itemholder.damage.setTextColor(Color.BLACK);
                        }
                    } else {
                        itemholder.newNum.setText(String.valueOf(item.getEvacheck()));
                        // 更改過後的文字改成紅色
                        if (item.getEgascheck() != item.getOriginNum() || item.getDamage()>0) {
                            itemholder.itemInfo.setTextColor(Color.RED);
                            itemholder.originNum.setTextColor(Color.RED);
                            itemholder.originNum2.setTextColor(Color.RED);
                            itemholder.newNum.setTextColor(Color.RED);
                            itemholder.damage.setTextColor(Color.RED);
                        } else {
                            itemholder.itemInfo.setTextColor(Color.BLACK);
                            itemholder.originNum.setTextColor(Color.BLACK);
                            itemholder.originNum2.setTextColor(Color.BLACK);
                            itemholder.newNum.setTextColor(Color.BLACK);
                            itemholder.damage.setTextColor(Color.BLACK);
                        }
                    }
                    itemholder.damage.setText(String.valueOf(item.getDamage()));



                    //顯示圖片，沒圖片就用No Image
                    String imgPath = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + "pic" + File.separator
                            + item.getItemCode() + ".jpg";
                    File file = new File(imgPath);
                    Uri imgUri = Uri.fromFile(file);
                    itemholder.imgItemButton.setTag(item.getItemCode());
                    ImageSize imageSize = new ImageSize(200, 200);

                    if (file.exists()) {
                        mImageLoader.displayImage(Uri.decode(imgUri.toString()),itemholder.imgItemButton);
                    }else {
                        itemholder.imgItemButton.setImageBitmap(null);
                    }

                    itemholder.imgItemButton.setOnClickListener(imgItemButton_OnClickListener);

                    break;

                case TYPE_TWO:
                    PreorderReceiptInfo receipt = preorderList.get(position - itemList.size());

                    //設定文字
                    preorderHolder.preorderReceiptInfo.setText(receipt.getPreorderReceiptInfo() + "\n" + "Preorder receipt");
                    preorderHolder.saleState.setText(receipt.getSaleState());

                    //設定顏色
                    preorderHolder.preorderReceiptInfo.setTextColor(Color.RED);
                    preorderHolder.saleState.setTextColor(Color.RED);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return v;
    }

    //清除資料
    public void clear() {
        dataItemList.clear();
        itemList.clear();
        itemListEvaBlack.clear();
    }

    //根據品名取得此項目
    public ItemInfo getItem(String title) {
        for (int i = 0; i < itemList.size(); i++) {
            if (itemList.get(i).getItemInfo().equals(title)) {
                return itemList.get(i);
            }
        }
        return null;
    }

    public ItemInfo getImgItem(String itemCode) {
        for (int i = 0; i < itemList.size(); i++) {
            if (itemList.get(i).getItemCode().equals(itemCode)) {
                return itemList.get(i);
            }
        }
        return null;
    }

    //修改某列的數量和顏色
    public int modifiedItemChange(ArrayList position, int stock, int damage, boolean isChange) {

        for (int k = 0; k < position.size(); k++) {

            String[] positionArray = position.get(k).toString().split(";");
            int i = Integer.valueOf(positionArray[0]);
            int j = Integer.valueOf(positionArray[1]);

            ItemInfo modifiedItem = dataItemList.get(i).get(j);
            int offset = modifiedItem.getNewNum();

            modifiedItem.setNewNum(stock);
            modifiedItem.setDamage(damage);
            modifiedItem.setModified(isChange);
            dataItemList.get(i).set(j, modifiedItem);
            if (itemList.size() > j) {
                //如果搜尋列有此項目則做搜詢之下的商品修改
                if (itemList.get(j).itemInfo.equals(dataItemList.get(i).get(j).itemInfo)) {
                    itemList.set(j, modifiedItem);
                    total = total + (stock - offset);
                }
                //如果搜尋列無此商品
                else {
                    total += 0;
                }
            }
        }
        return total;
    }


    //變更過的Items紀錄起來(在Items頁簽中)
    public ItemInfo addHasChangedItem(String info, ArrayList position) {
        boolean flag = false;
        for (int l = 0; l < position.size(); l++) {
            String[] positionArray = position.get(l).toString().split(";");
            int i = Integer.valueOf(positionArray[0]);
            int j = Integer.valueOf(positionArray[1]);

            for (int k = 0; k < hasChangedItemList.size(); k++) {
                if (hasChangedItemList.get(k).getItemInfo().equals(info)) {
                    flag = true;
                }
            }
            if (!flag) {
                String drawer, itemcode = "";
                int originNum, newNum, damage;
                boolean isModified;
                drawer = dataItemList.get(i).get(j).getDrawer();
                itemcode = dataItemList.get(i).get(j).getItemCode();
                originNum = dataItemList.get(i).get(j).getOriginNum();
                newNum = dataItemList.get(i).get(j).getNewNum();
                damage = dataItemList.get(i).get(j).getDamage();
                isModified = false;
                ItemInfo item = new ItemInfo();
                item.setDrawer(drawer);
                item.setItemCode(itemcode);
                item.setItemInfo(info);
                item.setOriginNum(originNum);
                item.setNewNum(newNum);
                item.setDamage(damage);
                item.setModified(isModified);
                hasChangedItemList.add(item);

                return item;
            }
        }
        return null;
    }


    //取得項目索引 (Items 或Discrepancy中來自Items)
    public ArrayList getItemId(String title) {
        ArrayList<String> result = new ArrayList<>();

        for (int i = 0; i < dataItemList.size(); i++) {
            for (int j = 0; j < dataItemList.get(i).size(); j++) {
                if (dataItemList.get(i).get(j).getItemInfo().equals(title)) {
                    result.add(Integer.toString(i) + ";" + Integer.toString(j));
                }
            }
        }
        return result;
    }


    //清除項目 (來自Items)
    public void removeItem(String position) {
        if (position.equals("null")) {
            return;
        }
        String[] positionArray = position.split(";");
        int i = Integer.valueOf(positionArray[0]);
        int j = Integer.valueOf(positionArray[1]);

        if (itemList.size() > j) {
            //如果搜尋列有此項目則remove
            if (itemList.get(j).itemInfo.equals(dataItemList.get(i).get(j).itemInfo)) {
                itemList.remove(j);
            }
        }
        if (dataItemList.size() > 0) {
            dataItemList.get(i).remove(j);
        }

    }



    //創建List<ItemInfo>
    public void createInsideList(int index) {
        if ((dataItemList.size() - 1) < index) {
            List<ItemInfo> insideList = new ArrayList<ItemInfo>();
            dataItemList.add(insideList);
        } else {
            return;
        }
    }


    public void refreshItemList(String Company) {
        clear();

        StringBuilder errMsg = new StringBuilder();
        DBQuery.DrawNoPack drawNoPack = DBQuery.getAllDrawerNo(context, errMsg, null, null, false);
        DBQuery.ItemDataPack itemDataPack;

        if (Company.equals("EGAS")) {
            //drawer
            for (int i = 0; i < drawNoPack.drawers.length; i++) {
                createInsideList(i);
                if (drawNoPack.drawers[i].DrawNo.equals("All Drawer")) {

                    itemDataPack = DBQuery.getModifyProductEGAS(context, errMsg, "9", null, null, 0);

                    for (int j = 0; j < itemDataPack.items.length; j++) {

                        if (itemDataPack.items[j].EGASCheckQty != itemDataPack.items[j].StandQty || itemDataPack.items[j].EGASDamageQty != 0) {

                            addItem(i, itemDataPack.items[j].DrawNo, itemDataPack.items[j].SerialCode, itemDataPack.items[j].ItemName,
                                    itemDataPack.items[j].StandQty, itemDataPack.items[j].EGASCheckQty, itemDataPack.items[j].EGASDamageQty, true, "",
                                    itemDataPack.items[j].EVACheckQty, itemDataPack.items[j].EGASCheckQty, itemDataPack.items[j].ItemCode);

                        } else {
                            addItem(i, itemDataPack.items[j].DrawNo, itemDataPack.items[j].SerialCode, itemDataPack.items[j].ItemName,
                                    itemDataPack.items[j].StandQty, itemDataPack.items[j].EGASCheckQty, itemDataPack.items[j].EVADamageQty, false, "",
                                    itemDataPack.items[j].EVACheckQty, itemDataPack.items[j].EGASCheckQty, itemDataPack.items[j].ItemCode);
                        }
                    }
                } else {
                    itemDataPack = DBQuery.getModifyProductEGAS(context, errMsg, "9", null, drawNoPack.drawers[i].DrawNo, 0);
                    for (int j = 0; j < itemDataPack.items.length; j++) {
                        if (itemDataPack.items[j].EGASCheckQty != itemDataPack.items[j].StandQty || itemDataPack.items[j].EGASDamageQty != 0) {
                            addItem(i, itemDataPack.items[j].DrawNo, itemDataPack.items[j].SerialCode, itemDataPack.items[j].ItemName,
                                    itemDataPack.items[j].StandQty, itemDataPack.items[j].EGASCheckQty, itemDataPack.items[j].EGASDamageQty, true, "",
                                    itemDataPack.items[j].EVACheckQty, itemDataPack.items[j].EGASCheckQty, itemDataPack.items[j].ItemCode);
                        } else {
                            addItem(i, itemDataPack.items[j].DrawNo, itemDataPack.items[j].SerialCode, itemDataPack.items[j].ItemName,
                                    itemDataPack.items[j].StandQty, itemDataPack.items[j].EGASCheckQty, itemDataPack.items[j].EGASDamageQty, false, "",
                                    itemDataPack.items[j].EVACheckQty, itemDataPack.items[j].EGASCheckQty, itemDataPack.items[j].ItemCode);
                        }
                    }
                }
            }
            itemList.get(0).getDamage();
        } else {
            for (int i = 0; i < drawNoPack.drawers.length; i++) {
                createInsideList(i);
                if (drawNoPack.drawers[i].DrawNo.equals("All Drawer")) {
                    itemDataPack = DBQuery.getModifyProductEVA(context, errMsg, "9", null, null, 0);
                    for (int j = 0; j < itemDataPack.items.length; j++) {
                        if (itemDataPack.items[j].EGASCheckQty != itemDataPack.items[j].EndQty) {
                            addItem(i, itemDataPack.items[j].DrawNo, itemDataPack.items[j].SerialCode, itemDataPack.items[j].ItemName,
                                    itemDataPack.items[j].EndQty, itemDataPack.items[j].EVACheckQty, itemDataPack.items[j].EVADamageQty, true, "",
                                    itemDataPack.items[j].EGASCheckQty, itemDataPack.items[j].EVACheckQty, itemDataPack.items[j].ItemCode);
                        } else {
                            addItem_EvaBalck(i, itemDataPack.items[j].DrawNo, itemDataPack.items[j].SerialCode, itemDataPack.items[j].ItemName,
                                    itemDataPack.items[j].EndQty, itemDataPack.items[j].EVACheckQty, itemDataPack.items[j].EVADamageQty, false, "",
                                    itemDataPack.items[j].EGASCheckQty, itemDataPack.items[j].EVACheckQty, itemDataPack.items[j].ItemCode);
                        }
                    }

                } else {
                    itemDataPack = DBQuery.getModifyProductEVA(context, errMsg, "9", null, drawNoPack.drawers[i].DrawNo, 0);
                    for (int j = 0; j < itemDataPack.items.length; j++) {
                        if (itemDataPack.items[j].EGASCheckQty != itemDataPack.items[j].EndQty) {
                            addItem(i, itemDataPack.items[j].DrawNo, itemDataPack.items[j].SerialCode, itemDataPack.items[j].ItemName,
                                    itemDataPack.items[j].EndQty, itemDataPack.items[j].EVACheckQty, itemDataPack.items[j].EVADamageQty, true, "",
                                    itemDataPack.items[j].EGASCheckQty, itemDataPack.items[j].EVACheckQty, itemDataPack.items[j].ItemCode);
                        } else {
                            addItem(i, itemDataPack.items[j].DrawNo, itemDataPack.items[j].SerialCode, itemDataPack.items[j].ItemName,
                                    itemDataPack.items[j].EndQty, itemDataPack.items[j].EVACheckQty, itemDataPack.items[j].EVADamageQty, false, "",
                                    itemDataPack.items[j].EGASCheckQty, itemDataPack.items[j].EVACheckQty, itemDataPack.items[j].ItemCode);
                        }
                    }
                }
            }
            dataItemList.get(0).addAll(itemListEvaBlack); //將紅色及黑色合併
            itemList.clear();
            itemList.addAll(dataItemList.get(0));
        }


    }


    //新增ListView項目 (來自Items)
    public void addItem(int i, String drawer, String serialCode, String info, int originNum, int newNum, int damage, boolean isModified, String page, int egascheckNum, int evacheckNum,
                        String itemcode) {

        boolean check;
        check = checkItem(newNum, damage);
        if (check) {
            ItemInfo item = new ItemInfo();
            item.setDrawer(drawer);
            item.setItemCode(itemcode);
            item.setItemInfo(info);
            item.setOriginNum(originNum);
            item.setNewNum(newNum);
            item.setDamage(damage);
            item.setEgascheck(egascheckNum);
            item.setEvacheck(evacheckNum);
            item.setSerialCode(serialCode);
            item.setModified(isModified);
            dataItemList.get(i).add(item);

            if (page.equals("discrepancy")) {
                itemListSort(dataItemList.get(i));
                itemList.add(item);
                itemListSort(itemList);
            } else {
                itemList.add(item);
            }
        }
    }

    //新增ListView項目 (來自Items)
    public void addItem_EvaBalck(int i, String drawer, String serialCode, String info, int originNum, int newNum, int damage, boolean isModified, String page, int egascheckNum, int evacheckNum,
                                 String itemcode) {

        boolean check;
        check = checkItem(newNum, damage);
        if (check) {
            ItemInfo item = new ItemInfo();
            item.setDrawer(drawer);
            item.setItemCode(itemcode);
            item.setItemInfo(info);
            item.setOriginNum(originNum);
            item.setNewNum(newNum);
            item.setDamage(damage);
            item.setEgascheck(egascheckNum);
            item.setEvacheck(evacheckNum);
            item.setSerialCode(serialCode);
            item.setModified(isModified);

            itemListEvaBlack.add(item);

        }
    }


    //設定抽屜項目
    public void setDrawer(int drawerIndex) {
        itemList.clear();
            for (int i = 0; i < dataItemList.get(drawerIndex).size(); i++) {
                itemList.add(dataItemList.get(drawerIndex).get(i));
            }
//        itemList.addAll(dataItemList.get(0));
        drawer = drawerIndex;
    }

    //搜尋
    public int search(String range, String page) {
        if (range == null) {
            range = searchRange;
        } else {
            searchRange = range;
        }

        itemList.clear();
        preorderList.clear();
        total = 0;

        if (page.equals("Items") && dataItemList.size() > 0) {
            for (int i = 0; i < dataItemList.get(drawer).size(); i++) {
                if ((dataItemList.get(drawer).get(i).itemInfo.toLowerCase().trim() + dataItemList.get(drawer).get(i).getItemCode() + dataItemList.get
                        (drawer).get(i).getSerialCode()).contains(range)) {
                    itemList.add(dataItemList.get(drawer).get(i));
                    total += dataItemList.get(drawer).get(i).getNewNum();
                }
            }
        } else if (page.equals("Discrepancy")) {
//            if (dataItemList.size() > 0) {
//                for (int i = 0; i < dataItemList.get(drawer).size(); i++) {
//                    if ((dataItemList.get(drawer).get(i).itemInfo.toLowerCase().trim() + dataItemList.get(drawer).get(i).getItemCode() + dataItemList.get
//                            (drawer).get(i).getSerialCode()).contains(range)) {
//                        itemList.add(dataItemList.get(drawer).get(i));
//                    }
//                }
//            }
//
//            if (dataPreorderList.size() > 0) {
//                for (int i = 0; i < dataPreorderList.size(); i++) {
//                    if (dataPreorderList.get(i).preorderReceiptInfo.contains(range)) {
//                        preorderList.add(dataPreorderList.get(i));
//                    }
//                }
//            }
        }

        return total;
    }

    //檢查瑕疵品量是否大於回庫量
    private boolean checkItem(int newNum, int damage) {
        return damage <= newNum;
    }

    //排序 (來自Items)
    private static void itemListSort(List<ItemInfo> list) {
        try {

            int j;
            String n;
            for (int i = 1; i < list.size(); i++) {
                ItemInfo item;
                item = list.get(i);
                n = list.get(i).getItemInfo();

                for (j = i - 1; j >= 0 && compare(list.get(j).getItemInfo(), n); --j) {
                    list.set(j + 1, list.get(j));
                }
                list.set(j + 1, item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //比較商品名稱順序(排列用)
    private static boolean compare(String left, String right) {

        for (int i = 0; i < left.length(); i++) {
            if ((int) left.charAt(i) < (int) right.charAt(i)) {
                return false;
            } else if ((int) left.charAt(i) > (int) right.charAt(i)) {
                return true;
            }
        }
        return false;
    }


    static class ItemHolder {

        public ImageView imgItemButton;
        public TextView itemInfo;
        public TextView originNum;
        public TextView originNum2;
        public TextView newNum;
        public TextView damage;
    }

    static class PreorderHolder {

        public TextView preorderReceiptInfo;
        public TextView saleState;
        public ImageView imgButton;
    }

    private ImageView.OnClickListener imgItemButton_OnClickListener = new ImageView.OnClickListener() {
        @Override
        public void onClick(View v) {
            String position = (String) v.findViewById(R.id.imgItem).getTag();
            itemFunctionClickListener.toolItemFunctionClickListener(position);
        }
    };


    /**
     * item Picture Click CallBack
     */
    private ItemListFunctionClickListener itemFunctionClickListener;

    public void setFilpperFunctionClickListener(ItemListFunctionClickListener f) {
        itemFunctionClickListener = f;
    }

    public interface ItemListFunctionClickListener {
        void toolItemFunctionClickListener(String itemInfo);
    }
}
