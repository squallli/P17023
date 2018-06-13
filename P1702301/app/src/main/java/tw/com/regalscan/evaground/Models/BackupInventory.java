package tw.com.regalscan.evaground.Models;

public class BackupInventory {
    private String SecSeq;
    private String DrawNo;
    private String ItemCode;
    private String StandQty;
    private Integer StartQty;
    private Integer  AdjustQty;
    private Integer SalesQty;
    private Integer TransferQty;
    private Integer  DamageQty;
    private Integer EndQty;
    private Integer  EGASCheckQty;
    private Integer EGASDamageQty;
    private Integer EVACheckQty;
    private Integer EVADamageQty;

    public String getSecSeq() {
        return SecSeq;
    }

    public void setSecSeq(String secSeq) {
        SecSeq = secSeq;
    }

    public String getDrawNo() {
        return DrawNo;
    }

    public void setDrawNo(String drawNo) {
        DrawNo = drawNo;
    }

    public String getItemCode() {
        return ItemCode;
    }

    public void setItemCode(String itemCode) {
        ItemCode = itemCode;
    }

    public String getStandQty() {
        return StandQty;
    }

    public void setStandQty(String standQty) {
        StandQty = standQty;
    }

    public Integer getStartQty() {
        return StartQty;
    }

    public void setStartQty(Integer startQty) {
        StartQty = startQty;
    }

    public Integer getAdjustQty() {
        return AdjustQty;
    }

    public void setAdjustQty(Integer adjustQty) {
        AdjustQty = adjustQty;
    }

    public Integer getSalesQty() {
        return SalesQty;
    }

    public void setSalesQty(Integer salesQty) {
        SalesQty = salesQty;
    }

    public Integer getTransferQty() {
        return TransferQty;
    }

    public void setTransferQty(Integer transferQty) {
        TransferQty = transferQty;
    }

    public Integer getDamageQty() {
        return DamageQty;
    }

    public void setDamageQty(Integer damageQty) {
        DamageQty = damageQty;
    }

    public Integer getEndQty() {
        return EndQty;
    }

    public void setEndQty(Integer endQty) {
        EndQty = endQty;
    }

    public Integer getEGASCheckQty() {
        return EGASCheckQty;
    }

    public void setEGASCheckQty(Integer EGASCheckQty) {
        this.EGASCheckQty = EGASCheckQty;
    }

    public Integer getEGASDamageQty() {
        return EGASDamageQty;
    }

    public void setEGASDamageQty(Integer EGASDamageQty) {
        this.EGASDamageQty = EGASDamageQty;
    }

    public Integer getEVACheckQty() {
        return EVACheckQty;
    }

    public void setEVACheckQty(Integer EVACheckQty) {
        this.EVACheckQty = EVACheckQty;
    }

    public Integer getEVADamageQty() {
        return EVADamageQty;
    }

    public void setEVADamageQty(Integer EVADamageQty) {
        this.EVADamageQty = EVADamageQty;
    }
}
