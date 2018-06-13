package tw.com.regalscan.component;

public class FlightInfoManager {
    private static FlightInfoManager instance;

    private static String CAName= ""; //登入CA Name
    private static String currentSecSeq=""; //目前航班編號

    //此類別無法使用普通的方法實例化
    private FlightInfoManager(){
        super();
    }

    //用來取得唯一實例物件的方法
    public static FlightInfoManager getInstance(){
        if (instance == null){
            instance = new FlightInfoManager();
        }
        return instance;
    }

    // 3. OpenActivity: 目前使用之航段編號
    public void setCurrentSecSeq(String ss){ currentSecSeq=ss; }
    public String getCurrentSecSeq(){ return currentSecSeq; }

    // 5. OpenActivity: 機上CA資訊
    public void setCAName(String caName) { CAName = caName; }
    public String getCAName() { return CAName; }

}
