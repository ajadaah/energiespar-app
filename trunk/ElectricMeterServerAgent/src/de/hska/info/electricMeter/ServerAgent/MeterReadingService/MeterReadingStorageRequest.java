package de.hska.info.electricMeter.ServerAgent.MeterReadingService;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

public class MeterReadingStorageRequest {
	
	@XmlElement(required=true, nillable=false)
	private String connectionInfo;
	@XmlElement(required=true, nillable=false)
	private Date timeStamp;
	@XmlElement(required=true, nillable=false)
	private int meterNumber;
	@XmlElement(required=true, nillable=false)
	private int meterValue;
	@XmlElement(required=true, nillable=false)
	private MeterType meterType;
	@XmlElement(required=true, nillable=false)
	private boolean meterValueRevised;
	
	
	public String getConnectionInfo() {
		return connectionInfo;
	}
	public Date getTimeStamp() {
		return timeStamp;
	}
	public int getMeterNumber() {
		return meterNumber;
	}
	public int getMeterValue() {
		return meterValue;
	}
	public MeterType getMeterType() {
		return meterType;
	}
	public boolean isMeterValueRevised() {
		return meterValueRevised;
	}

}
