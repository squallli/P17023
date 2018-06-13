package tw.com.regalscan.evaground.Models;

public class clsMenu {
    private String MenuString;
    private Boolean Enable;

    public clsMenu(String menuString,Boolean enable)
    {
        setEnable(enable);
        setMenuString(menuString);
    }

    public String getMenuString() {
        return MenuString;
    }

    public void setMenuString(String menuString) {
        MenuString = menuString;
    }

    public Boolean getEnable() {
        return Enable;
    }

    public void setEnable(Boolean enable) {
        Enable = enable;
    }
}
