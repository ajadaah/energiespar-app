package de.hska.info.electricMeter.dataStorage;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum MeterType {

	ELECTRICITY(0), WATER(1), GAS(2); // specific values are set as these are stored in the internal sqlite database

	private static final Map<Integer, MeterType> lookup = new HashMap<Integer, MeterType>();

	static {
		for (MeterType meterType : EnumSet.allOf(MeterType.class))
			lookup.put(meterType.getValue(), meterType);
	}

	private final int value;

	private MeterType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
	
	public static MeterType get(int value) {
		return lookup.get(value);
	}

}
