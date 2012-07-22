/**
 * Copyright (C) 2009, 2010 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.hska.rbmk.statistik;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import de.hska.rbmk.datenVerwaltung.DbAdapter;
import de.hska.rbmk.datenVerwaltung.MeterReadingEntry;
import de.hska.rbmk.datenVerwaltung.MeterType;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

/**
 * Project status demo chart.
 */
public class Liniendiagramm extends AbstractChart {
	
	private SQLiteDatabase db;
	private DbAdapter dbAdapter;
  /**
   * Returns the chart name.
   * 
   * @return the chart name
   */
  public String getName() {
    return "Project tickets status";
  }

  /**
   * Returns the chart description.
   * 
   * @return the chart description
   */
  public String getDesc() {
    return "The opened tickets and the fixed tickets (time chart)";
  }

  /**
   * Executes the chart demo.
   * 
   * @param context the context
   * @return the built intent
   */
  public Intent execute(Context context) {
	  /*
	  dbAdapter = new DbAdapter(context);
	  
	  dbAdapter.open();
	  db = dbAdapter.getDb();
	  
	  // TODO: hardcoded atm
	  String meterNumber = "1000412";
	  
	  String query = "SELECT "+DbAdapter.KEY_TIMESTAMP+","+DbAdapter.KEY_METERVALUE+" FROM "+DbAdapter.DATABASE_TABLE_METERREADINGS+" WHERE "+DbAdapter.KEY_METERNUMBER+" = ?";
	  Cursor queryCursor = db.rawQuery(query, new String[] { meterNumber });
	  
	  List<Date> readingDates = new ArrayList<Date>();
	  List<Date> mondayDates = new ArrayList<Date>();
	  List<Integer> readingValues = new ArrayList<Integer>();
	  List<Integer> readingDiff = new ArrayList<Integer>();
	  
	  if (queryCursor.getCount() != 0) {
		  queryCursor.moveToFirst();
		  for (int i = 0; i < 10; i++) {
		  readingDates.add(new Date(Long.valueOf(queryCursor.getString(queryCursor.getColumnIndexOrThrow(DbAdapter.KEY_TIMESTAMP)))));
		  readingValues.add(queryCursor.getInt(queryCursor.getColumnIndexOrThrow(DbAdapter.KEY_METERVALUE)));
		  
		  if (queryCursor.isLast())
			  break;
		  else
			  queryCursor.moveToNext();
		  }
	  }

	  queryCursor.close();
	  dbAdapter.close();
	  

	    09.07.2012
		16.07.2012
		23.07.2012
		30.07.2012
		06.08.2012

	  
	  Calendar cal = GregorianCalendar.getInstance();
	  Calendar tempCal = GregorianCalendar.getInstance();
	  
	  Date startDate = new Date();
	  
	  String datumString = (String) DateFormat.format("E, MMMM dd, yyyy", cal);
	  
	  Toast.makeText(context, datumString, Toast.LENGTH_LONG).show();
	  
	  cal.setTime(readingDates.get(0));
	  if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY)
	  {
		  startDate.setTime(cal.getTimeInMillis());
	  }
	  else
	  {
		  cal.add(Calendar.DAY_OF_YEAR, (8-cal.get(Calendar.DAY_OF_WEEK)));
		  startDate.setTime(cal.getTimeInMillis());
	  }
	  
	  mondayDates.add(startDate);
	  cal.setTime(startDate);
	  
	  while (true) {
		  cal.add(Calendar.DAY_OF_YEAR, 7);
		  tempCal.setTime(readingDates.get(readingDates.size()-1));
		  if (cal.get(Calendar.DAY_OF_YEAR) <= tempCal.get(Calendar.DAY_OF_YEAR))
		  	mondayDates.add(cal.getTime());
		  else
		  	break;
	  }
	  
	  readingDiff.add(0); // erster Wert immer 0
	  readingDiff.add(0); // zweiter Wert immer 0
	  	   */

	  String[] titles = new String[] { "Stromzähler 1000412" };
	  List<Date[]> dates = new ArrayList<Date[]>();
	  List<double[]> values = new ArrayList<double[]>();
	  int length = titles.length;

//	  Calendar cal = GregorianCalendar.getInstance();
//	  cal.set(2008, 9, 1);
//	  Date d = cal.getTime();

	  for (int i = 0; i < length; i++) {
		  dates.add(new Date[5]);
		  dates.get(i)[0] = new Date(112, 7, 9);
		  dates.get(i)[1] = new Date(112, 7, 16);
		  dates.get(i)[2] = new Date(112, 7, 23);
		  dates.get(i)[3] = new Date(112, 7, 30);
		  dates.get(i)[4] = new Date(112, 8, 6);
	  }
	  //    values.add(new double[] { 152, 123, 122, 140, 135, 115, 120, 115, 112, 106, 100, 107 });
	  values.add(new double[] { 0, -15, -2, -17.75, 29.25 });
	  length = values.get(0).length;
	  int[] colors = new int[] { Color.DKGRAY };
	  PointStyle[] styles = new PointStyle[] { PointStyle.POINT };
	  XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
	  renderer.setMarginsColor(Color.WHITE);
	  setChartSettings(renderer, "Verbrauchsstatistik", "Datum", "Wochendifferenz in kWh", dates.get(0)[0].getTime(),
			  dates.get(0)[4].getTime(), -50, 50, Color.BLACK, Color.BLACK);
	  renderer.setXLabels(5);
	  renderer.setYLabels(10);
	  length = renderer.getSeriesRendererCount();
	  for (int i = 0; i < length; i++) {
		  SimpleSeriesRenderer seriesRenderer = renderer.getSeriesRendererAt(i);
		  seriesRenderer.setDisplayChartValues(true);
	  }
	  return ChartFactory.getTimeChartIntent(context, buildDateDataset(titles, dates, values),
			  renderer, "dd.MM.yyyy");
  }

}
