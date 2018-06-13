package tw.com.regalscan.evaground;

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
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import tw.com.regalscan.R;
import tw.com.regalscan.db02.DBQuery;

public class DiscrepancyListAdapter extends BaseAdapter {

    private final int TYPE_ONE = 0, TYPE_TWO = 1, TYPE_COUNT = 2;
    private String searchRange = "";
    private String user;
    private Context context = null;
    private int total = 0;
    private int drawer = 0;

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

    public DiscrepancyListAdapter(Context c, String user) {
        context = c;
        this.user = user;
        //根据context上下文加载布局，这里的是Demo17Activity本身，即this

//    loadingIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_loading);
        new Thread() {
            public void run() {
                ImageLoaderConfiguration loaderConfiguration = new ImageLoaderConfiguration.Builder(context).defaultDisplayImageOptions(options).build();
                mImageLoader.init(loaderConfiguration);
            }}.start();
//        AppComponent appComponent = ArmsUtils.obtainAppComponentFromContext(context);
//        mImageLoader = appComponent.imageLoader();
    }

    public DiscrepancyListAdapter(Context c, PackageManager pm, List<ItemInfo> list) {
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

    public PreorderReceiptInfo getPreorder(int position) {
        return preorderList.get(position - itemList.size());
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
                        preorderHolder = new DiscrepancyListAdapter.PreorderHolder();
                        preorderHolder.preorderReceiptInfo = v.findViewById(R.id.txtreceipt);
                        preorderHolder.saleState = v.findViewById(R.id.txtSaleState);
                        preorderHolder.imgButton = v.findViewById(R.id.imgItem);

                        v.setTag(preorderHolder);
                        break;
                }

            } else {
                switch (type) {
                    case TYPE_ONE:
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
                        preorderHolder = new DiscrepancyListAdapter.PreorderHolder();
                        preorderHolder.preorderReceiptInfo = v.findViewById(R.id.txtreceipt);
                        preorderHolder.saleState = v.findViewById(R.id.txtSaleState);
                        preorderHolder.imgButton = v.findViewById(R.id.imgItem);

                        v.setTag(preorderHolder);
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
                    if(user.equals("EGAS")){
                        itemholder.newNum.setText(String.valueOf(item.getEgascheck()));
                    }else {
                        itemholder.newNum.setText(String.valueOf(item.getEvacheck()));
                    }
                    itemholder.damage.setText(String.valueOf(item.getDamage()));

                    itemholder.itemInfo.setTextColor(Color.RED);
                    itemholder.originNum.setTextColor(Color.RED);
                    itemholder.originNum2.setTextColor(Color.RED);
                    itemholder.newNum.setTextColor(Color.RED);
                    itemholder.damage.setTextColor(Color.RED);

                    //顯示圖片，沒圖片就用No Image
                    String imgPath = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + "pic" + File.separator
                            + item.getItemCode() + ".jpg";
                    File file = new File(imgPath);
                    Uri imgUri = Uri.fromFile(file);
                    itemholder.imgItemButton.setTag(item.getItemCode());
                    ImageSize imageSize = new ImageSize(200, 200);
                    if(file.exists()) {
                        mImageLoader.loadImage(Uri.decode(imgUri.toString()), imageSize, new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                super.onLoadingComplete(imageUri, view, loadedImage);
                                itemholder.imgItemButton.setImageBitmap(loadedImage);
                            }
                        });
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
        fixList.clear();
        fixList_preorder.clear();
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
            int offset = Integer.valueOf(modifiedItem.getNewNum());

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

    //檢查是否更改為原本的Items
    public boolean checkItemRecovery(String info, String newNum, String damage) {
        for (int i = 0; i < hasChangedItemList.size(); i++) {
            if ((hasChangedItemList.get(i).getItemInfo().equals(info)) &&
                    (hasChangedItemList.get(i).getOriginNum() == Integer.parseInt(newNum)) &&
                    (hasChangedItemList.get(i).getDamage() == Integer.parseInt(damage))) {
                return true;
            }
        }
        return false;
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

    //取得項目索引 (來自Preorder)
    public String getPreorderItemId(String title) {
        if (dataPreorderList.size() == 0) {
            return "null";
        }
        for (int i = 0; i < dataPreorderList.size(); i++) {
            if (dataPreorderList.get(i).getPreorderReceiptInfo().equals(title)) {
                return Integer.toString(i);
            }
        }
        return "null";
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

    List<ItemInfo> fixList=new ArrayList(); //固定不刪除
    List<PreorderReceiptInfo> fixList_preorder=new ArrayList();//固定不刪除
    public void refreshDiscrepancyList(String Company) {
        clear();

        StringBuilder errMsg = new StringBuilder();
        if(Company.equals("EGAS")){
            itemList = DBQuery.getEGASDiscrepancyItemList(context, errMsg);
            preorderList = DBQuery.getEGASDiscrepancyPreOrderList(context, errMsg);
        }else {
            itemList = DBQuery.getEVADiscrepancyItemList(context, errMsg);
            preorderList = DBQuery.getEVADiscrepancyPreOrderList(context, errMsg);
        }

        fixList.addAll(itemList);
        fixList_preorder.addAll(preorderList);
    }

    //新增ListView項目 (來自Items)
    public void addItem(int i, String drawer, String serialCode, String info, int originNum, int newNum, int damage, boolean isModified, String page, int egascheckNum,int evacheckNum,
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

    //設定抽屜項目
    public void setDrawer(int drawerIndex) {
        itemList.clear();
//        for (int i = 0; i < dataItemList.get(drawerIndex).size(); i++) {
//            itemList.add(dataItemList.get(drawerIndex).get(i));
//        }
        itemList.addAll(dataItemList.get(0));
        drawer = drawerIndex;
    }

    //搜尋
    public int search(String range, String page) {
        if (range == null) {
            range = searchRange;
        } else {
            searchRange = range;
        }
        //
        itemList.clear();
        preorderList.clear();
//        List<ItemInfo> searchList=new ArrayList();
//        searchList.addAll(fixList);
//        List<PreorderReceiptInfo> searchList_preorder=new ArrayList();
//        searchList_preorder.addAll(fixList_preorder);

        total = 0;

        if (page.equals("Items") && fixList.size() > 0) {
//            for (int i = 0; i < dataItemList.get(drawer).size(); i++) {
//                if ((dataItemList.get(drawer).get(i).itemInfo.toLowerCase().trim() + dataItemList.get(drawer).get(i).getItemCode() + dataItemList.get
//                        (drawer).get(i).getSerialCode()).contains(range)) {
//                    itemList.add(dataItemList.get(drawer).get(i));
//                    total += dataItemList.get(drawer).get(i).getNewNum();
//                }
//            }
        } else if (page.equals("Discrepancy")) {
            if (fixList.size() > 0) {
                for (int i = 0; i < fixList.size(); i++) {
                    if ((fixList.get(i).itemInfo.toLowerCase().trim() + fixList.get(i).getItemCode() + fixList.get(i).getSerialCode()).contains(range)) {
                        itemList.add(fixList.get(i));
                    }
                }
            }

            if (fixList_preorder.size() > 0) {
                for (int i = 0; i < fixList_preorder.size(); i++) {
                    if (fixList_preorder.get(i).preorderReceiptInfo.contains(range)) {
                        preorderList.add(fixList_preorder.get(i));
                    }
                }
            }
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
