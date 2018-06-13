package tw.com.regalscan.app.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by tp00175 on 2017/10/3.
 */

public class News implements Parcelable {
    private String TITLE;
    private String CONTENT;

    protected News(Parcel parcel) {
        this.TITLE = parcel.readString();
        this.CONTENT = parcel.readString();
    }

    public static final Creator<News> CREATOR = new Creator<News>() {
        @Override
        public News createFromParcel(Parcel parcel) {
            return new News(parcel);
        }

        @Override
        public News[] newArray(int size) {
            return new News[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.TITLE);
        parcel.writeString(this.CONTENT);
    }

    public News() {
    }

    public News(JSONObject jsonObject) {
        this.TITLE = jsonObject.getString("TITLE");
        this.CONTENT = jsonObject.getString("CONTENT");
    }

    public String getTITLE() {
        return TITLE;
    }

    public String getCONTENT() {
        return CONTENT;
    }

    public void setTITLE(String TITLE) {
        this.TITLE = TITLE;
    }

    public void setCONTENT(String CONTENT) {
        this.CONTENT = CONTENT;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
