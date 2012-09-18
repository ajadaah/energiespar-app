
package de.hska.info.electricMeter.ServerAgent.MeterReadingService.jaxws;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "storeMeterReadings", namespace = "http://MeterReadingService.ServerAgent.electricMeter.info.hska.de/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "storeMeterReadings", namespace = "http://MeterReadingService.ServerAgent.electricMeter.info.hska.de/")
public class StoreMeterReadings {

    @XmlElement(name = "storageRequest", namespace = "")
    private List<de.hska.info.electricMeter.ServerAgent.MeterReadingService.MeterReadingStorageRequest> storageRequest;

    /**
     * 
     * @return
     *     returns List<MeterReadingStorageRequest>
     */
    public List<de.hska.info.electricMeter.ServerAgent.MeterReadingService.MeterReadingStorageRequest> getStorageRequest() {
        return this.storageRequest;
    }

    /**
     * 
     * @param storageRequest
     *     the value for the storageRequest property
     */
    public void setStorageRequest(List<de.hska.info.electricMeter.ServerAgent.MeterReadingService.MeterReadingStorageRequest> storageRequest) {
        this.storageRequest = storageRequest;
    }

}
