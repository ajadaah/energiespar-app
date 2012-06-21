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

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

/**
 * Project status demo chart.
 */
public class Liniendiagramm extends AbstractChart {
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
    String[] titles = new String[] { "Stromzähler" };
    List<Date[]> dates = new ArrayList<Date[]>();
    List<double[]> values = new ArrayList<double[]>();
    int length = titles.length;
    for (int i = 0; i < length; i++) {
    	
//    	Calendar cal = GregorianCalendar.getInstance();
//    	cal.set(2008, 9, 1);
//    	Date d = cal.getTime();

      dates.add(new Date[12]);
      dates.get(i)[0] = new Date(112, 1, 1);
      dates.get(i)[1] = new Date(112, 1, 8);
      dates.get(i)[2] = new Date(112, 1, 15);
      dates.get(i)[3] = new Date(112, 1, 22);
      dates.get(i)[4] = new Date(112, 1, 29);
      dates.get(i)[5] = new Date(112, 2, 5);
      dates.get(i)[6] = new Date(112, 2, 12);
      dates.get(i)[7] = new Date(112, 2, 19);
      dates.get(i)[8] = new Date(112, 2, 26);
      dates.get(i)[9] = new Date(112, 3, 3);
      dates.get(i)[10] = new Date(112, 3, 10);
      dates.get(i)[11] = new Date(112, 3, 17);
    }
//    values.add(new double[] { 152, 123, 122, 140, 135, 115, 120, 115, 112, 106, 100, 107 });
    values.add(new double[] { 26, 25, 26, 29, 33, 31, 29, 25, 26, 23, 27, 30 });
    length = values.get(0).length;
    int[] colors = new int[] { Color.DKGRAY };
    PointStyle[] styles = new PointStyle[] { PointStyle.POINT };
    XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
    renderer.setMarginsColor(Color.WHITE);
    setChartSettings(renderer, "Verbrauchsstatistik", "Datum", "Stromverbrauch in kWh", dates.get(0)[0].getTime(),
        dates.get(0)[11].getTime(), 0, 50, Color.BLACK, Color.BLACK);
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
