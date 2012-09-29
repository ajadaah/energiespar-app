package de.hska.rbmk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import de.hska.rbmk.statistik.*;
import de.hska.rbmk.sync.*;
import de.hska.rbmk.zaehlerstand.*;
import de.hska.rbmk.verbrauchsrechner.*;
import de.hska.rbmk.datenVerwaltung.DbAdapter;
import de.hska.rbmk.geraetevergleich.*;
import de.hska.rbmk.geraeteverwaltung.GeraeteverwaltungActivity;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class StartbildschirmActivity extends Activity {

	ImageButton b1, b2;
	
	public String server_ip, server_port;
	
	public SharedPreferences settings;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startbildschirm);

		ActionBar actionBar = getActionBar();
		actionBar.setTitle(R.string.app_name);
	}

	public void zaehlerstandErfassen(View v) {
		final Intent i = new Intent(this, ZaehlerstandErfassenActivity.class);
		startActivity(i);
	}

	public void verbrauchsstatistik(View v) {
		final Intent chart = new Liniendiagramm().execute(this);
		startActivity(chart);
	}

	public void verbrauchsrechner(View v) {
		final Intent i = new Intent(this, WaschmaschinenActivity.class);
		startActivity(i);
	}

	public void geraetevergleich(View v) {
		final Intent i = new Intent(this, GeraetevergleichActivity.class);
		startActivity(i);
	}

	public void geraeteverwaltung(View v) {
		final Intent i = new Intent(this, GeraeteverwaltungActivity.class);
		startActivity(i);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) 
		{
		case R.id.menu_sync:
		{
	        settings = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
	        
	        server_ip = settings.getString("ip_address", "192.168.0.1");
	        server_port = settings.getString("port_number", "7676");
			
	    	// start connection service and set extras (IP and PORT)
	    	Intent serviceIntent = new Intent(this, ConnectionService.class);
	    	serviceIntent.putExtra("ip address", server_ip);
	    	serviceIntent.putExtra("port number", server_port);
	    	startService(serviceIntent);
	    	
//			Toast.makeText(this, "Synchronisation"+server_ip+" "+server_port, Toast.LENGTH_SHORT).show();
			return true;
		}

		case R.id.menu_einstellungen:
		{
			startActivity(new Intent(this, EinstellungenActivity.class));
			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
		/*
		case R.id.menu_diff:
		{
			
			String DATABASE_PATH = "/data/data/de.hska.rbmk/databases/";
			String DATABASE_NAME = "AppDatenbank.db";

			try {
				//Open your local db as the input stream
				InputStream myInput = this.getAssets().open(DATABASE_NAME);


				// Path to the just created empty db
				String outFileName = DATABASE_PATH + DATABASE_NAME + ".original";

				//Open the empty db as the output stream
				OutputStream myOutput = new FileOutputStream(outFileName);

				//transfer bytes from the inputfile to the outputfile
				byte[] mybuffer = new byte[1024];
				int length;
				while ((length = myInput.read(mybuffer))>0){
					myOutput.write(mybuffer, 0, length);
				}

				//Close the streams
				myOutput.flush();
				myOutput.close();
				myInput.close();

				String command = DATABASE_PATH+"diff -u "+DATABASE_PATH+DATABASE_NAME+".original "+DATABASE_PATH+DATABASE_NAME;

				Process proc = null;
				ProcessBuilder pb = new ProcessBuilder();
				proc = pb.command(command)
						.redirectErrorStream(true).start();
				BufferedReader bReader = new BufferedReader(new InputStreamReader(
						proc.getInputStream()));

				//	            	File wd = new File(DATABASE_PATH);

				//	                Process process = Runtime.getRuntime().exec(command, null, null);

				// Reads stdout.
				// NOTE: You can write to stdin of the command using
				//       process.getOutputStream().
				//	                BufferedReader bReader = new BufferedReader(
				//	                        new InputStreamReader(process.getInputStream()));
				int read;
				char[] buffer = new char[4096];
				StringBuffer output = new StringBuffer();
				while ((read = bReader.read(buffer)) > 0) {
					output.append(buffer, 0, read);
				}
				bReader.close();

				// Waits for the command to finish.
				proc.waitFor();

				Toast.makeText(this, output.toString(), Toast.LENGTH_SHORT).show();


				break;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 
			return true;
		}
		*/

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.startbildschirm_menu, menu);
		return true;
	}	

}