
package org.travice.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.Collection;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Network {

    public Network() {
    }

    public Network(CellTower cellTower) {
        addCellTower(cellTower);
    }

    private Integer homeMobileCountryCode;

    public Integer getHomeMobileCountryCode() {
        return homeMobileCountryCode;
    }

    public void setHomeMobileCountryCode(Integer homeMobileCountryCode) {
        this.homeMobileCountryCode = homeMobileCountryCode;
    }

    private Integer homeMobileNetworkCode;

    public Integer getHomeMobileNetworkCode() {
        return homeMobileNetworkCode;
    }

    public void setHomeMobileNetworkCode(Integer homeMobileNetworkCode) {
        this.homeMobileNetworkCode = homeMobileNetworkCode;
    }

    private String radioType = "gsm";

    public String getRadioType() {
        return radioType;
    }

    public void setRadioType(String radioType) {
        this.radioType = radioType;
    }

    private String carrier;

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    private Boolean considerIp = false;

    public Boolean getConsiderIp() {
        return considerIp;
    }

    public void setConsiderIp(Boolean considerIp) {
        this.considerIp = considerIp;
    }

    private Collection<CellTower> cellTowers;

    public Collection<CellTower> getCellTowers() {
        return cellTowers;
    }

    public void setCellTowers(Collection<CellTower> cellTowers) {
        this.cellTowers = cellTowers;
    }

    public void addCellTower(CellTower cellTower) {
        if (cellTowers == null) {
            cellTowers = new ArrayList<>();
        }
        cellTowers.add(cellTower);
    }

    private Collection<WifiAccessPoint> wifiAccessPoints;

    public Collection<WifiAccessPoint> getWifiAccessPoints() {
        return wifiAccessPoints;
    }

    public void setWifiAccessPoints(Collection<WifiAccessPoint> wifiAccessPoints) {
        this.wifiAccessPoints = wifiAccessPoints;
    }

    public void addWifiAccessPoint(WifiAccessPoint wifiAccessPoint) {
        if (wifiAccessPoints == null) {
            wifiAccessPoints = new ArrayList<>();
        }
        wifiAccessPoints.add(wifiAccessPoint);
    }

}
