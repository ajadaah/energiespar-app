package de.hska.rbmk.datenVerwaltung;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MeterReadingEntry {

	public int rowID = -1;
	public String connectionInfo;
	public Date timestamp;
	public int meterNumber;
	public int meterValue;
	public MeterType meterType;
	public boolean isMeterValueRevised;
	public boolean isSynchronized;

	public MeterReadingEntry(String connectionInfo, Date timestamp,
			int meterNumber, int meterValue, MeterType meterType,
			boolean isMeterValueRevised, boolean isSynchronized) {
		super();
		this.connectionInfo = connectionInfo;
		this.timestamp = timestamp;
		this.meterNumber = meterNumber;
		this.meterValue = meterValue;
		this.meterType = meterType;
		this.isMeterValueRevised = isMeterValueRevised;
		this.isSynchronized = isSynchronized;
	}

	public MeterReadingEntry(int rowID, String connectionInfo, Date timestamp,
			int meterNumber, int meterValue, MeterType meterType,
			boolean isMeterValueRevised, boolean isSynchronized) {
		super();
		this.rowID = rowID;
		this.connectionInfo = connectionInfo;
		this.timestamp = timestamp;
		this.meterNumber = meterNumber;
		this.meterValue = meterValue;
		this.meterType = meterType;
		this.isMeterValueRevised = isMeterValueRevised;
		this.isSynchronized = isSynchronized;
	}

	public String getConnectionInfo() {
		return connectionInfo;
	}

	public Date getTimestamp() {
		return timestamp;
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
		return isMeterValueRevised;
	}

	public boolean isSynchronized() {
		return isSynchronized;
	}

	public String toXML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<storageRequest>");
		sb.append("<connectionInfo>");
		sb.append(this.connectionInfo);
		sb.append("</connectionInfo>");
		sb.append("<timeStamp>");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		sb.append(df.format(this.timestamp));
		sb.append("</timeStamp>");
		sb.append("<meterNumber>");
		sb.append(this.meterNumber);
		sb.append("</meterNumber>");
		sb.append("<meterValue>");
		sb.append(this.meterValue);
		sb.append("</meterValue>");
		sb.append("<meterType>");
		sb.append(meterType.name());
		sb.append("</meterType>");
		sb.append("<meterValueRevised>");
		sb.append(this.isMeterValueRevised);
		sb.append("</meterValueRevised>");
		sb.append("</storageRequest>");
		return sb.toString();
	}

}
