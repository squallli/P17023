package tw.com.regalscan.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;

import tw.com.regalscan.R;
import tw.com.regalscan.component.AsyncImageFileLoader;
import tw.com.regalscan.customClass.ItemInfo;
import tw.com.regalscan.utils.Tools;

public class ItemListPictureModifyAdapter extends BaseAdapter {

    private Context context;
    private List<ItemInfo> itemList = new ArrayList<>();

    private Bitmap loadingIcon;
    private AsyncImageFileLoader asyncImageFileLoader;
    private ListView itemListView;

    //listview寬度
    private int mRightWidth = 0;

    //是否可以點圖片放大
    private boolean isPictureZoomIn = true;

    public void setIsPictureZoomIn(boolean b) {
        isPictureZoomIn = b;
    }

    //是否可以調整商品數量
    private boolean isModifiedItem = true;

    public void setIsModifiedItem(boolean b) {
        isModifiedItem = b;
    }

    //畫面從右數來第二個項目標籤是否可見
    private boolean isRightTwoVisible = true;

    public void setIsRightTwoVisible(boolean b) {
        isRightTwoVisible = b;
    }

    //是否顯示金額
    private boolean isMoneyVisible = true;

    public void setIsMoneyVisible(boolean b) {
        isMoneyVisible = b;
    }

    //是否加上CheckBoxListener
    private boolean isCheckLayout = false;

    public void setIsCheckLayout(boolean b) {
        isCheckLayout = b;
    }

    //是否加上滑動刪除
    private boolean isSwipeDelete = false;

    //是否online購物車
    private boolean isOnlineSale = false;

    public void setIsOnlineSale(boolean b) {
        isOnlineSale = b;
    }

    public boolean isOnlineSale() {
        return isOnlineSale;
    }

    //是否把Qty欄位放置Price
    private boolean isQtyPutPrice = false;

    public void setQtyPutPrice(boolean b) {
        isQtyPutPrice = b;
    }

    //是否是Preorder (有重複的itemCode, 先load所有Key進來)
    private boolean isPreorder = false;

    public void setIsPreorder(boolean b) {
        isPreorder = b;
    }

    private ArrayList<String> imageKeyCode = new ArrayList<>();
    private HashMap<String, Bitmap> imageCache = new HashMap<>();

    public void setImageKeyCodeList(ArrayList<String> list) {
        imageKeyCode = list;

        // Loading所有圖片
        for (int i = 0; i < imageKeyCode.size(); i++) {
            Bitmap bmp = asyncImageFileLoader.loadImageFromFile(imageKeyCode.get(i), 200, 200);
            imageCache.put(imageKeyCode.get(i), bmp);
        }
    }


    // 1. 可滑動刪除的adapter (Damage, UpdateCpCheck, Basket, CrewCart, OrderEdit, ProcessingOrderEdit, TransferOut)
    public ItemListPictureModifyAdapter(Context c, ListView listView, int rightWidth) {
        context = c;
        itemListView = listView;
        mRightWidth = rightWidth;
        isSwipeDelete = true;
        isPreorder = false;

        loadingIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_loading);
        asyncImageFileLoader = new AsyncImageFileLoader();
    }

    // 2. 無法滑動刪除的adapter (Catalog, Refund, OrderDetail, ProcessingOrderDetail, PreorderSale, VipPaid, VipSale
    // , FragmentReport, ReportTransfer, CancelTransferOut, TransferIn, Update)
    public ItemListPictureModifyAdapter(Context c, ListView listView) {
        context = c;
        itemListView = listView;
        isSwipeDelete = false;
        isPreorder = false;

        loadingIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_loading);
        asyncImageFileLoader = new AsyncImageFileLoader();
    }

    // 3. 無法滑動刪除, 且為Preorder, VipPaid, VipSale, 需先load所有圖片檔
    public ItemListPictureModifyAdapter(Context c, ListView listView, boolean _isPreorder) {
        context = c;
        itemListView = listView;
        isPreorder = _isPreorder;

        loadingIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_loading);
        asyncImageFileLoader = new AsyncImageFileLoader();
    }

    public List<ItemInfo> getItemList() {
        return itemList;
    }

    //Adapter的列數總數
    @Override
    public int getCount() {
        return itemList.size();
    }

    //某列的內容
    @Override
    public ItemInfo getItem(int position) {
        return itemList.get(position);
    }

    public ItemInfo getItem(String itemCode) {
        for (int i = 0; i < itemList.size(); i++) {
            if (itemList.get(i).getItemCode().equals(itemCode)) {
                return itemList.get(i);
            }
        }
        return null;
    }

    //是否有商品訂購量大於庫存量
    public boolean isQtyMoreThanStock(boolean isGift) {
        if (isGift) {
            // 贈品檢查: 應贈送量 > 庫存量 或 庫存量=0
            for (int i = 0; i < itemList.size(); i++) {
                if (itemList.get(i).getPrice() == 0 &&
                    ((Integer)itemList.get(i).getQty() > itemList.get(i).getStock()
                        || itemList.get(i).getStock() == 0)) {
                    return true;
                }
            }
        } else {
            for (int i = 0; i < itemList.size(); i++) {
                if (itemList.get(i).getPrice() > 0 && (Integer)itemList.get(i).getQty() > itemList.get(i).getStock()) {
                    return true;
                }
            }
        }
        return false;
    }

    //贈送贈品數量是否有不足
    public boolean isGiftLack() {
        for (int i = 0; i < itemList.size(); i++) {
            if (itemList.get(i).getPrice() == 0 && itemList.get(i).isModified()) {
                return true;
            }
        }
        return false;
    }

    //修改某列的顏色
    public void modifiedItemColorChange(int position, boolean isChange) {
        ItemInfo modifiedItem = itemList.get(position);
        modifiedItem.setModified(isChange);
        itemList.set(position, modifiedItem);
    }

    //修改某列的數量
    public void modifiedItemChange(int position, int qty, int stock) {
        ItemInfo modifiedItem = itemList.get(position);
        modifiedItem.setQty(qty);
        modifiedItem.setStock(stock);
        itemList.set(position, modifiedItem);
    }

    //加入某列的Checkbox checked
    public void itemChecked(int position, boolean isChecked) {
        ItemInfo modifiedItem = itemList.get(position);
        modifiedItem.setCheck(isChecked);
        itemList.set(position, modifiedItem);
    }

    //取得某一列的id
    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getItemId(String itemCode) {
        for (int i = 0; i < itemList.size(); i++) {
            if (itemList.get(i).getItemCode().equals(itemCode)) {
                return i;
            }
        }
        return -1;
    }

    public void removeItem(int position) {
        itemList.remove(position);
    }

    public void clear() {
        itemList.clear();
    }

    public void addItem(String itemCode, String serialNo, String moneyType, double price,
                        String itemName, int stock, int qty) {
        ItemInfo item = new ItemInfo(itemCode, serialNo, moneyType, price, itemName, stock, qty);
        itemList.add(item);
    }

    // preorder內用qty的位置顯示金額(有小數點)
    // 地面預定單沒有serial code, 將itemCode存進serial Code的欄位，才可顯示
    public void addItem(String itemCode, String serialNo, String monyType, double price,
                        String itemName, int stock, double qty) {
        ItemInfo item = new ItemInfo(itemCode, serialNo, monyType, price, itemName, stock, qty);
        itemList.add(item);
    }

    // discount
//    public void addItem(String itemCode, String serialNo, String monyType, double price,
//                        String itemName, int stock, int qty, boolean canDiscount) {
//        ItemInfo item = new ItemInfo(itemCode, serialNo, monyType, price, itemName, stock, qty, canDiscount);
//        itemList.add(item);
//    }

    //修改數量
    public void addItem(String itemCode, String serialNo, String monyType, double price,
                        String itemName, int stock, int qty, boolean canDiscount, boolean isModified) {
        ItemInfo item = new ItemInfo(itemCode, serialNo, monyType, price, itemName, stock, qty, canDiscount, isModified);
        itemList.add(item);
    }

    //ife
    public void addItem(String itemCode, String serialNo, String monyType, double price,
                        String itemName, int stock, int qty, int ife, boolean canDiscount, boolean isModified) {
        ItemInfo item = new ItemInfo(itemCode, serialNo, monyType, price, itemName, stock, qty, ife, canDiscount, isModified);
        itemList.add(item);
    }


    //修改某一列View的內容
    @Override
    public View getView(int position, View v, ViewGroup parent) {

        Holder holder = new Holder();

        //IFE上需要使用check畫面
        if (isCheckLayout) {
            v = LayoutInflater.from(context).inflate(R.layout.item_list_view_pic_two_check, null);
            holder.check = v.findViewById(R.id.checkBox2);
//      holder.check.setEnabled(false);
        }
        //滑動刪除
        else if (isSwipeDelete) {

            //設定右二資訊(Stock)是否要有
            if (!isRightTwoVisible) {
                v = LayoutInflater.from(context).inflate(R.layout.item_list_view_pic_one_delete, null);
            } else {
                v = LayoutInflater.from(context).inflate(R.layout.item_list_view_pic_two_delete, null);
            }

            holder.item_left = v.findViewById(R.id.item_left);
            holder.item_right = v.findViewById(R.id.item_right);
            holder.item_right_btn = v.findViewById(R.id.item_right_btn);

            LayoutParams lp1 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            holder.item_left.setLayoutParams(lp1);

            LayoutParams lp2 = new LayoutParams(mRightWidth, LayoutParams.MATCH_PARENT);
            holder.item_right.setLayoutParams(lp2);
        }
        //設定右二資訊(Stock)是否要有
        else if (!isRightTwoVisible) {
            v = LayoutInflater.from(context).inflate(R.layout.item_list_view_pic_one, null);
        } else {
            v = LayoutInflater.from(context).inflate(R.layout.item_list_view_pic_two, null);
        }


            /* 共用Layout */
        holder.layout_txtItem_parent = v.findViewById(R.id.layout_txtItem_parent);       //整個商品資訊文字
        holder.layout_moneyInfo = v.findViewById(R.id.txtMoneyInfo);     //整個金額資訊文字

        //商品資訊文字細項
        holder.magazineNum = v.findViewById(R.id.txtMagazineNum);
        holder.monyType = v.findViewById(R.id.txtMoneyType);
        holder.price = v.findViewById(R.id.txtMoney);
        holder.itemName = v.findViewById(R.id.txtItemInfo);

        //商品數量細項
        holder.rightOne = v.findViewById(R.id.txtQty);
        if (isRightTwoVisible) {
            holder.rightTwo = v.findViewById(R.id.txtStock);
        }

        //商品圖片
        holder.imgItemButton = v.findViewById(R.id.imgItem);

        v.setTag(holder);

        //設定商品金額是否顯示
        if (isMoneyVisible) {
            holder.layout_moneyInfo.setVisibility(View.VISIBLE);
        } else {
            holder.layout_moneyInfo.setVisibility(View.GONE);
        }

        //設定雜誌編號是否可見
        holder.magazineNum.setVisibility(View.VISIBLE);

        //設定文字內容
        ItemInfo item = itemList.get(position);
        holder.magazineNum.setText(item.getSerialNo());
        holder.monyType.setText(item.getMonyType());

        //整數價格判斷
        if (item.getIntegerPrice() != -1) {
            holder.price.setText(String.valueOf(item.getIntegerPrice()));
        } else {
            holder.price.setText(String.valueOf(item.getPrice()));
        }

        holder.itemName.setText(item.getItemName());

        if (isRightTwoVisible) {
            // 在Preorder Sale和Vip Paid, Qty被放置Price
            if (isQtyPutPrice) {
                if (item.getIntegerQty() != -1) {
                    holder.rightOne.setText(String.valueOf(item.getIntegerQty()));
                } else {
                    holder.rightOne.setText(Tools.getModiMoneyString(Double.parseDouble(item.getQty().toString())));
                }
            } else {
                holder.rightOne.setText(String.valueOf(item.getQty()));
            }
            holder.rightTwo.setText(String.valueOf(item.getStock()));
        } else {
            holder.rightOne.setText(String.valueOf(item.getQty()));
        }

        //是IFE Layout就改成設定checkbox
        if (isCheckLayout) {
            if (item.isCheck()) {
                holder.check.setChecked(true);
            } else {
                holder.check.setChecked(false);
            }
        }

        // 不打折商品顯示黑色粗體字
        if (!item.isCanDiscount()) {
            //金錢設定為黑色
            holder.monyType.setTextColor(Color.BLACK);
            holder.price.setTextColor(Color.BLACK);

            // setTypeface(字體, 0為正常1為粗體2為斜體3為粗斜體)
            holder.magazineNum.setTypeface(Typeface.DEFAULT, 1);
            holder.monyType.setTypeface(Typeface.DEFAULT, 1);
            holder.price.setTypeface(Typeface.DEFAULT, 1);
            holder.itemName.setTypeface(Typeface.DEFAULT, 1);
            holder.rightOne.setTypeface(Typeface.DEFAULT, 1);
            if (isRightTwoVisible) {
                holder.rightTwo.setTypeface(Typeface.DEFAULT, 1);
            }
        }
        // 回復原本顏色
        else {
            //金錢設為灰色
            holder.monyType.setTextColor(Color.parseColor("#666666"));
            holder.price.setTextColor(Color.parseColor("#666666"));

            holder.magazineNum.setTypeface(Typeface.DEFAULT, 0);
            holder.monyType.setTypeface(Typeface.DEFAULT, 0);
            holder.price.setTypeface(Typeface.DEFAULT, 0);
            holder.itemName.setTypeface(Typeface.DEFAULT, 0);
            holder.rightOne.setTypeface(Typeface.DEFAULT, 0);
            if (isRightTwoVisible) {
                holder.rightTwo.setTypeface(Typeface.DEFAULT, 0);
            }
        }

        // 更改數量後的文字改成紅色
        if (item.isModified()) {
            holder.magazineNum.setTextColor(Color.RED);
            holder.monyType.setTextColor(Color.RED);
            holder.price.setTextColor(Color.RED);
            holder.itemName.setTextColor(Color.RED);
            holder.rightOne.setTextColor(Color.RED);
            if (isRightTwoVisible) {
                holder.rightTwo.setTextColor(Color.RED);
            }
        }
        //回復原本顏色
        else {
            holder.magazineNum.setTextColor(Color.BLACK);
            holder.monyType.setTextColor(v.getResources().getColor(R.color.colorGray));
            holder.price.setTextColor(v.getResources().getColor(R.color.colorGray));
            holder.itemName.setTextColor(Color.BLACK);
            holder.rightOne.setTextColor(Color.BLACK);
            if (isRightTwoVisible) {
                holder.rightTwo.setTextColor(Color.BLACK);
            }
        }

        holder.imgItemButton.setTag(item.getItemCode());
        //商品圖片設定
        if (isPreorder) {
            Bitmap bitmap = imageCache.get(item.getItemCode());
            if (bitmap != null) {
                holder.imgItemButton.setImageBitmap(bitmap);
            } else {
                holder.imgItemButton.setImageBitmap(loadingIcon); //顯示預設的圖片
            }
        } else {
            //設定此mHolder.icon的tag為檔名，讓之後的callback function可以針對此mHolder.icon替換圖片
            Bitmap cachedBitmap = asyncImageFileLoader.loadBitmap(item.getItemCode(), 200, 200, (imageBitmap, imageFile) -> {
                // 利用檔案名稱找尋當前mHolder.icon
                ImageView imageViewByTag = itemListView.findViewWithTag(imageFile);
                if (imageViewByTag != null && imageBitmap != null) {
                    imageViewByTag.setImageBitmap(imageBitmap);
                }
            });
            if (cachedBitmap != null) {
                holder.imgItemButton.setImageBitmap(cachedBitmap);
            } else {
                holder.imgItemButton.setImageBitmap(loadingIcon); //顯示預設的圖片
            }
        }

        if (isPictureZoomIn) {
            holder.imgItemButton.setOnClickListener(v13 -> {
                String itemInfo = (String)v13.findViewById(R.id.imgItem).getTag();
                itemFunctionClickListener.toolItemFunctionClickListener(itemInfo);
            });
        }

        //修改數量
        holder.layout_txtItem_parent.setTag(position);
        if (isModifiedItem) {
            holder.layout_txtItem_parent.setOnClickListener(v1 -> {
                int position1 = (Integer)v1.findViewById(R.id.layout_txtItem_parent).getTag();
                itemInfoClickListener.txtItemInfoClickListener(position1);
            });
        }

        //滑動刪除
        if (isSwipeDelete) {
            holder.item_right_btn.setTag(position);
            holder.item_right_btn.setOnClickListener(v12 -> {
                int position12 = (Integer)v12.findViewById(R.id.item_right_btn).getTag();
                itemSwipeDelete.swipeDeleteListener(position12);
            });
        }

        //IFE的CheckBox
        if (isCheckLayout) {
            holder.check.setTag(position);

            holder.check.setOnCheckedChangeListener((compoundButton, b) -> itemCheckBoxListener.checkBoxListener(position, b));
        }

        return v;
    }

    class Holder {

        public ImageView imgItemButton;         //商品圖片

        public RelativeLayout layout_txtItem_parent;   //商品文字資訊的Layout

        public TextView magazineNum;            //雜誌編號
        public TextView itemName;               //商品名稱
        public TextView rightOne;
        public TextView rightTwo;

        public RelativeLayout layout_moneyInfo; //幣別和價格的Layout
        public TextView monyType;               //幣別
        public TextView price;            //價格

        public CheckBox check;                  //IFE有Checkbox的項目

        public View item_left;
        public View item_right;                 //滑動刪除的view
        public ImageButton item_right_btn;      //滑動刪除的Btn
    }

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

    /**
     * itemInfo Click CallBack
     */
    private ItemInfoClickListener itemInfoClickListener;

    public void setItemInfoClickListener(ItemInfoClickListener f) {
        itemInfoClickListener = f;
    }

    public interface ItemInfoClickListener {

        void txtItemInfoClickListener(int position);
    }

    /**
     * item Swipe Delete CallBack
     */
    private ItemSwipeDeleteListener itemSwipeDelete;

    public void setItemSwipeListener(ItemSwipeDeleteListener f) {
        itemSwipeDelete = f;
    }

    public interface ItemSwipeDeleteListener {

        void swipeDeleteListener(int position);
    }

    /**
     * itemCheckbox Checked CallBack
     */
    private ItemCheckBoxListener itemCheckBoxListener;

    public void setCheckBoxCheckedListener(ItemCheckBoxListener f) {
        itemCheckBoxListener = f;
    }

    public interface ItemCheckBoxListener {

        void checkBoxListener(int position, boolean isChecked);
    }
}
