package tw.com.regalscan.evaground;


import android.content.Context;
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
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import tw.com.regalscan.R;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.evaground.Models.PreOrderInfo;
import tw.com.regalscan.evaground.Models.PreorderItems;

public class PreorderListAdapter extends BaseAdapter {

    private String user;
    private Context context;
    private List<PreOrderInfo> mPreOrderInfos = new ArrayList<>();
//    private List<List<PreorderItems>> list = new ArrayList<>();//儲存drawer資料

    private List<PreorderItems> preorderList = new ArrayList<>();//畫面顯示用的
    private PreOrderInfo mPreOrderInfo;

    private List<Holder> mHolderList = new ArrayList<>();

    private DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.icon_loading)
            .showImageForEmptyUri(R.drawable.icon_loading)
            .showImageOnFail(R.drawable.icon_loading)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .resetViewBeforeLoading(true)
            .displayer(new FadeInBitmapDisplayer(100))
            .build();

    public PreorderListAdapter(Context c, String user) {
        context = c;
        this.user = user;
    }

    //Adapter的列數總數
    @Override
    public int getCount() {
        return preorderList.size();
    }

    //某列的內容
    @Override
    public PreorderItems getItem(int position) {
        return preorderList.get(position);
    }

    //取得某一列的id
    @Override
    public long getItemId(int position) {
        return position;
    }

    //修改某一列View的內容
    @Override
    public View getView(int position, View v, ViewGroup parent) {

        Holder holder;
        if (v == null) {

            v = LayoutInflater.from(context).inflate(R.layout.item_list_view_preorder, null);
            holder = new Holder();
            holder.preorderinfo = v.findViewById(R.id.txtItemInfo);
            holder.originNum = v.findViewById(R.id.txtOrigin);
            holder.originNum2 = v.findViewById(R.id.txtOrigin2);
            holder.newNum = v.findViewById(R.id.txtNew);
            holder.imgItemButton = v.findViewById(R.id.imgItem);
            if (user.equals("EGAS")) {
                ViewGroup.LayoutParams params = holder.originNum2.getLayoutParams();
                params.width = 0;
                params.height = 0;
                holder.originNum2.setLayoutParams(params);
            }
            mHolderList.add(holder);
            holder.imgItemButton.setTag(holder);

            v.setTag(holder);

        } else {
            holder = (Holder) v.getTag();

        }

        PreorderItems item = preorderList.get(position);

        holder.preorderinfo.setText(item.getPreorderInfo());
        holder.originNum.setText(String.valueOf(item.getSaleQty()));
        holder.originNum2.setText(String.valueOf(item.getOriginNum()));
        holder.newNum.setText(String.valueOf(item.getNewNum()));

        // 更改過後的文字改成紅色
        if (mPreOrderInfo.getStatus().equals("S")) {
            holder.preorderinfo.setTextColor(Color.RED);
            holder.originNum.setTextColor(Color.RED);
            holder.originNum2.setTextColor(Color.RED);
            holder.newNum.setTextColor(Color.RED);
            holder.newNum.setText("0");
        } else {
            holder.preorderinfo.setTextColor(Color.BLACK);
            holder.originNum.setTextColor(Color.BLACK);
            holder.originNum2.setTextColor(Color.BLACK);
            holder.newNum.setTextColor(Color.BLACK);
            holder.newNum.setText(String.valueOf(item.getNewNum()));
        }

        //顯示圖片，沒圖片就用No Image
        String imgPath = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + "pic" + File.separator + item.getItemCode()
                + ".jpg";
        File file = new File(imgPath);
        Uri imgUri = Uri.fromFile(file);
        holder.uri = imgUri.toString();
        holder.imgItemButton.setTag(item.getItemCode());
        if(file.exists()) {
            ImageLoader.getInstance().loadImage(imgUri.toString(), options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    super.onLoadingComplete(imageUri, view, loadedImage);
                    for (Holder holder : mHolderList) {
                        if (holder.uri.equals(imageUri)) {
                            holder.imgItemButton.setImageBitmap(loadedImage);
                        }
                    }
                }
            });
        }else {
            holder.imgItemButton.setImageBitmap(null);
        }


        holder.imgItemButton.setOnClickListener(imgItemButton_OnClickListener);

        return v;
    }

    public void modifiedReceiptChange(String preOrderNo, boolean status) {
        for (PreOrderInfo orderInfo : mPreOrderInfos) {
            if (orderInfo.getPreOrderNo().equals(preOrderNo)) {
                if (!status) {
                    orderInfo.setStatus("N");
                } else {
                    orderInfo.setStatus("S");
                }
            }
        }
    }

    public PreorderItems getImgItem(String itemCode) {
        for (int i = 0; i < preorderList.size(); i++) {
            if (preorderList.get(i).getItemCode().equals(itemCode)) {
                return preorderList.get(i);
            }
        }
        return null;
    }

    public void refreshPR(String Company) {
        mPreOrderInfos.clear();
        String[] preOderType = new String[]{"PR"};

        StringBuilder errMsg = new StringBuilder();

        DBQuery.PreorderInfoPack preorderInfoPack = DBQuery.getPreorderInfo(context, errMsg, null, preOderType, null);
        if (preorderInfoPack.info != null) {
            for (int i = 0; i < preorderInfoPack.info.length; i++) {

                DBQuery.PreorderInformation information = preorderInfoPack.info[i];

                List<PreorderItems> itemsList = new ArrayList<>();

                for (int j = 0; j < information.items.length; j++) {

                    PreorderItems preorderItems = new PreorderItems();
                    if(Company.equals("EGAS"))
                    {
                        if (preorderInfoPack.info[i].EGASSaleFlag.equals("S")) {
                            preorderItems.setPreorderInfo(information.items[j].ItemName);
                            //preorderItems.setOriginNum(information.items[j].SalesQty);
                            preorderItems.setSaleQty(information.items[j].SalesQty);
                            preorderItems.setNewNum(0);
                            preorderItems.setModified(true);
                            preorderItems.setItemCode(information.items[j].ItemCode);
                        } else {
                            preorderItems.setPreorderInfo(information.items[j].ItemName);
                            //preorderItems.setOriginNum(information.items[j].SalesQty);
                            preorderItems.setSaleQty(information.items[j].SalesQty);
                            preorderItems.setNewNum(information.items[j].SalesQty);
                            preorderItems.setModified(false);
                            preorderItems.setItemCode(information.items[j].ItemCode);
                        }
                    } else if(Company.equals("EVA")) {

                        if(preorderInfoPack.info[i].SaleFlag.equals("S")){
                            preorderItems.setSaleQty(0);
                            preorderItems.setModified(true);
                        }else{
                            preorderItems.setSaleQty(information.items[j].SalesQty);
                        }

                        if (preorderInfoPack.info[i].EVASaleFlag.equals("S")) {
                            preorderItems.setNewNum(0);
                            preorderItems.setModified(true);

                        } else {
                            preorderItems.setNewNum(information.items[j].SalesQty);
                            preorderItems.setModified(false);
                        }

                        if (preorderInfoPack.info[i].EGASSaleFlag.equals("S")){
                            preorderItems.setOriginNum(0);
                        }else {
                            preorderItems.setOriginNum(information.items[j].SalesQty);
                        }
                        preorderItems.setPreorderInfo(information.items[j].ItemName);
                        preorderItems.setItemCode(information.items[j].ItemCode);
                    }

                    itemsList.add(preorderItems);
                }
                if(Company.equals("EGAS")) {
                    addPreOrderInfo(information.PreorderNO, information.EGASSaleFlag, itemsList);
                }else {
                    addPreOrderInfo(information.PreorderNO, information.EVASaleFlag, itemsList);
                }

            }
        }
    }

    public void refreshVIP(String Company) {
        mPreOrderInfos.clear();
        String[] preOderVIPType = new String[]{"VP", "VS"};

        StringBuilder errMsg = new StringBuilder();

        DBQuery.PreorderInfoPack VIP = DBQuery.getPreorderInfo(context, errMsg, null, preOderVIPType, null);

        if (VIP.info != null) {
            for (int i = 0; i < VIP.info.length; i++) {

                DBQuery.PreorderInformation information = VIP.info[i];

                List<PreorderItems> itemsList = new ArrayList<>();

                for (int j = 0; j < information.items.length; j++) {

                    PreorderItems preorderItems = new PreorderItems();

                    if(Company.equals("EGAS"))
                    {
                        if (VIP.info[i].EGASSaleFlag.equals("S")) {
                            preorderItems.setPreorderInfo(information.items[j].ItemName);
                            //preorderItems.setOriginNum(information.items[j].SalesQty);
                            preorderItems.setSaleQty(information.items[j].SalesQty);
                            preorderItems.setNewNum(0);
                            preorderItems.setModified(true);
                            preorderItems.setItemCode(information.items[j].ItemCode);
                        } else {
                            preorderItems.setPreorderInfo(information.items[j].ItemName);
                            //preorderItems.setOriginNum(information.items[j].SalesQty);
                            preorderItems.setSaleQty(information.items[j].SalesQty);
                            preorderItems.setNewNum(information.items[j].SalesQty);
                            preorderItems.setModified(false);
                            preorderItems.setItemCode(information.items[j].ItemCode);
                        }
                    }
                    else if(Company.equals("EVA"))
                    {
                        if(VIP.info[i].SaleFlag.equals("S")){
                            preorderItems.setSaleQty(0);
                        }else{
                            preorderItems.setSaleQty(information.items[j].SalesQty);
                        }

                        if (VIP.info[i].EVASaleFlag.equals("S")) {
                            preorderItems.setNewNum(0);
                            preorderItems.setModified(true);
                        } else {
                            preorderItems.setNewNum(information.items[j].SalesQty);
                            preorderItems.setModified(false);
                        }

                        if (VIP.info[i].EGASSaleFlag.equals("S")) {
                            preorderItems.setOriginNum(0);
                        }else {
                            preorderItems.setOriginNum(information.items[j].SalesQty);
                        }
                        preorderItems.setPreorderInfo(information.items[j].ItemName);
                        preorderItems.setItemCode(information.items[j].ItemCode);
                    }

                    itemsList.add(preorderItems);
                }
                if(Company.equals("EGAS")) {
                    addPreOrderInfo(information.PreorderNO, information.EGASSaleFlag, itemsList);
                }else {
                    addPreOrderInfo(information.PreorderNO, information.EVASaleFlag, itemsList);
                }

            }
        }
    }
    public void addPreOrderInfo(String preOrderNo, String status, List<PreorderItems> items) {
        mPreOrderInfos.add(new PreOrderInfo(preOrderNo, status, items));
    }

    public void setReceipt(String preOrderNo) {
        preorderList.clear();
        for (int i = 0; i < mPreOrderInfos.size(); i++) {
            if (mPreOrderInfos.get(i).getPreOrderNo().equals(preOrderNo)) {
                mPreOrderInfo = mPreOrderInfos.get(i);
                preorderList.addAll(mPreOrderInfos.get(i).getPreorderItems());
            }
        }
    }

    public String getCurrentReceipt()
    {
        try {
            return mPreOrderInfo.getPreOrderNo();
        }catch (Exception e){
            return "-1";
        }

    }
    public PreOrderInfo getPreOrderInfo(String preOrderNo) {
        for (PreOrderInfo preOrderInfo : mPreOrderInfos) {
            if (preOrderInfo.getPreOrderNo().equals(preOrderNo)) {
                return preOrderInfo;
            }
        }
        return null;
    }

    public int getTotal(String preOrderNo) {
        int total = 0;
        for (int i = 0; i < mPreOrderInfos.size(); i++) {
            if (mPreOrderInfos.get(i).getPreOrderNo().equals(preOrderNo)) {
                for (int j = 0; j < mPreOrderInfos.get(i).getPreorderItems().size(); j++) {
                    total += mPreOrderInfos.get(i).getPreorderItems().get(j).getSaleQty();
                }
            }
        }
        return total;
    }

    class Holder {

        ImageView imgItemButton;
        public TextView preorderinfo;
        TextView originNum;
        TextView originNum2;
        TextView newNum;
        String uri;
    }

    private ImageView.OnClickListener imgItemButton_OnClickListener = new ImageView.OnClickListener() {
        @Override
        public void onClick(View v) {
            try{
                String position = (String) v.findViewById(R.id.imgItem).getTag();
                itemFunctionClickListener.toolItemFunctionClickListener(position);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    /**
     * 自定義的FuntionButton Click CallBack
     */
    private ItemListFunctionClickListener itemFunctionClickListener;

    public void setFilpperFunctionClickListener(ItemListFunctionClickListener f) {
        itemFunctionClickListener = f;
    }

    public interface ItemListFunctionClickListener {
        void toolItemFunctionClickListener(String itemInfo);
    }
}
