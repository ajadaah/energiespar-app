package de.hska.info.electricMeter;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import de.hska.info.electricMeter.archive.ArchiveActivity;
import de.hska.info.electricMeter.dataStorage.MeterReadingEntry;
import de.hska.info.electricMeter.dataStorage.MeterReadingsDbAdapter;
import de.hska.info.electricMeter.meterSelection.MeterSelectionActivity;
import de.hska.info.electricMeter.syncService.SynchronizationService;

public class ElectricMeterActivity extends Activity {
	
	private TextView mAdScrollerView;
	private TextView mLastEntryView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setupListeners();
		mAdScrollerView = (TextView) this.findViewById(R.id.adScroller);
		mLastEntryView = (TextView) this.findViewById(R.id.lastEntry);
		if (savedInstanceState != null && savedInstanceState.containsKey("lastValue") && savedInstanceState.containsKey("rssText")) {
			Log.d("ElectricMeter", "used savedInstanceState to recover");
			mLastEntryView.setText(savedInstanceState.getCharSequence("lastValue"));
	    	mAdScrollerView.setText(savedInstanceState.getCharSequence("rssText"));
	    	mAdScrollerView.setSelected(true);	
		} else {
			Log.d("ElectricMeter", "didn't use savedInstanceState to recover");
			MeterReadingsDbAdapter dbAdapter = new MeterReadingsDbAdapter(getApplicationContext());
			dbAdapter.open();
			MeterReadingEntry currentMeterReading = dbAdapter.getLatestMeterReading();
			if (currentMeterReading != null) {
				mLastEntryView.setText("letzte Erfassung: " + DateFormat.format("dd.MM.yyyy, kk:mm", currentMeterReading.getTimestamp()));
			}
			dbAdapter.close();
			
			mAdScrollerView.setSelected(true);		
			final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			String server_agent_url = sp.getString("pref_server_agent_url", "http://localhost:8765");
			Log.d("ElectricMeter", "Server Agent: " + server_agent_url);
			new DownloadRSSTask().execute(server_agent_url.substring(0, server_agent_url.lastIndexOf(":")) + ":8080/rss.xml");
		}
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0,0,0,"Einstellungen");
		return true;
	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	super.onOptionsItemSelected(item);

	switch (item.getItemId()){

	case 0:
	        startActivity(new Intent("de.hska.info.ElectricMeter.PREFERENCE"));
	        break;

	default:
	        break;
	}

	        return false;
	}
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	outState.putCharSequence("lastValue", mLastEntryView.getText());
    	outState.putCharSequence("rssText", mAdScrollerView.getText());
    }

    private void setupListeners() {
	Button cameraBtn = (Button) findViewById(R.id.btn_start_camera);
	cameraBtn.setOnClickListener(new View.OnClickListener() {

	    public void onClick(View view) {
		Intent cameraIntent = new Intent(view.getContext(), MeterSelectionActivity.class);
		startActivityForResult(cameraIntent, 0);
	    }
	});
	
	Button archive = (Button) findViewById(R.id.btn_start_archive);
    archive.setOnClickListener(new View.OnClickListener() {
    	
        public void onClick(View view) {
            Intent archiveIntent = new Intent(view.getContext(), ArchiveActivity.class);
            startActivityForResult(archiveIntent, 0);
        }
    });
	
    }
    
    private class DownloadRSSTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urlStrings) {
			URL url;
			Log.d("ElectricMeter", "trying to download " + urlStrings[0]);
	    	StringBuilder sb = new StringBuilder();
	    	try {
		    	url = new URL(urlStrings[0]);
		    	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		
		    	if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
		            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		            DocumentBuilder db = dbf.newDocumentBuilder();
		            Document doc;
		            doc = db.parse(url.openStream());
		            doc.getDocumentElement().normalize();
		            NodeList itemLst = doc.getElementsByTagName("item");
		
		            for(int i=0; i < itemLst.getLength(); i++) {	
		                Node item = itemLst.item(i);
		                if(item.getNodeType() == Node.ELEMENT_NODE) {
		                      Element ielem = (Element)item;
		                      NodeList title = ielem.getElementsByTagName("title");
		                      NodeList link = ielem.getElementsByTagName("link");

		                      sb.append(" ### ");
		                      sb.append(" <a href=\""); 
		                      sb.append(link.item(0).getChildNodes().item(0).getNodeValue());
		                      sb.append("\">");
		                      sb.append(title.item(0).getChildNodes().item(0).getNodeValue());
		                      sb.append("</a>");
		                } // endif == Node.ELEMENT_NODE
		            } // endfor
		    	} // endif == HttpURLConnection.HTTP_OK
	    	} catch (MalformedURLException e) {
	    		// TODO Auto-generated catch block
	    		e.printStackTrace();
	    	} catch (DOMException e) {
	    		// TODO Auto-generated catch block
	    		e.printStackTrace();
	    	} catch (ConnectException e) {
	    		Log.e("ElectricMeter", "Could not connect to server: " + e.getMessage());
	    	} catch (IOException e) {
	    		// TODO Auto-generated catch block
	    		e.printStackTrace();
	    	} catch (ParserConfigurationException e) {
	    		// TODO Auto-generated catch block
	    		e.printStackTrace();
	    	} catch (SAXException e) {
	    		// TODO Auto-generated catch block
	    		e.printStackTrace();
	    	}
	    	String retValue = sb.toString();
	    	Log.d("ElectricMeter", "rssText: " + retValue);
	    	return retValue;
		}
		
		protected void onPostExecute(String result) {
			mAdScrollerView.setText(result);
			//Linkify.addLinks(mAdScrollerView, Linkify.WEB_URLS);
			mAdScrollerView.setText(Html.fromHtml(result));
		           // "<b>text3:</b>  Text with a " + "<a href=\"http://www.google.com\">link</a> " +     "created in the Java source code using HTML."));
			mAdScrollerView.setMovementMethod(LinkMovementMethod.getInstance());

		}
    	
    }
    
}