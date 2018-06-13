package tw.com.regalscan.evaground.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by tp00175 on 2017/5/23.
 */

public class NewsInfo {

    @SerializedName("TITLE")
    private String title;
    @SerializedName("CONTENT")
    private String content;

    public NewsInfo(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
