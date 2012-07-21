package de.hska.rbmk;

public class UmweltRating {

	private float stromverbrauch;
	private int fuellmenge;
	
	public float UmweltRating(float stromverbrauch, int fuellmenge) {
		this.stromverbrauch = stromverbrauch;
		this.fuellmenge = fuellmenge;
		
		float kwh_pro_kilo = stromverbrauch/fuellmenge;
		
//			A 	unter 0,19 kWh/kg
//			B 	0,19 – 0,23 kWh/kg
//			C 	0,24 – 0,27 kWh/kg
//			D 	0,28 – 0,31 kWh/kg
//			E 	0,32 – 0,35 kWh/kg
//			F 	0,36 – 0,39 kWh/kg
//			G	über 0,39 kWh/kg

		return (kwh_pro_kilo-(1/10))/(1/35);
	}
}
