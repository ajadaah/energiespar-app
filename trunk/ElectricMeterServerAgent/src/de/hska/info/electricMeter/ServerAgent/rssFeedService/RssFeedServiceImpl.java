package de.hska.info.electricMeter.ServerAgent.rssFeedService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.apache.log4j.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import de.hska.info.electricMeter.ServerAgent.MeterReadingService.MeterReadingServiceHandler;

public class RssFeedServiceImpl implements HttpHandler {
	
	public static Logger logger = Logger.getLogger(MeterReadingServiceHandler.class);

	public void handle(HttpExchange t) throws IOException {
        InputStream is = t.getRequestBody();
        
        logger.debug("new rss feed request from " + t.getRemoteAddress().toString());
        //read(is); // .. read the request body
        String currentDate = new Date().toString();
        String responseHeader = "<?xml version=\"1.0\" encoding=\"utf-8\"?><rss version=\"2.0\"><channel>" +
        		"<title>Aktuelle Infos Ihres Stromanbieters</title><link>http://www.hs-karlsruhe.de</link><description>tolle Infos von uns fuer Sie!</description>" +
        		"<language>de-de</language><copyright>(c) Stromanbieter</copyright>" +
        				"<pubDate>" + currentDate + "</pubDate>";
        String responseContent = "<item><title>neue Tarife! Fuer Sparfuechse!</title><description>Jetzt neue Tarifoptionen sichern! Schnell klicken!</description>" + // item 1
        		"<link>http://www.hs-karlsruhe.de?i=1</link><author>Ihr Stromanbieter, info@nomail.mail</author>" +
        		"<guid>http://www.hs-karlsruhe.de?i=1</guid><pubDate>" + currentDate + "</pubDate></item>"
        		+ "<item><title>Verlosung beendet! Haben Sie gewonnen?</title><description>Schauen Sie hier, ob Sie gewonnen haben! Jeder Stromzaehler gewinnt!</description>" + // item 2
        		"<link>http://www.hs-karlsruhe.de?i=2</link><author>Ihr Stromanbieter, info@nomail.mail</author>" +
        		"<guid>http://www.hs-karlsruhe.de?i=2</guid><pubDate>" + currentDate + "</pubDate></item>"
        		+ "<item><title>neue Android App, Stromzaehlern auf die schlaue Art!</title><description>Jetzt ganz einfach Ihren Srtomzaehlerstand melden!</description>" + // item 3
        		"<link>http://www.hs-karlsruhe.de?i=3</link><author>Ihr Stromanbieter, info@nomail.mail</author>" +
        		"<guid>http://www.hs-karlsruhe.de?i=3</guid><pubDate>" + currentDate + "</pubDate></item>";
        String responseFooter = "</channel></rss>";
        
        String completeResponse = responseHeader + responseContent + responseFooter;
        t.sendResponseHeaders(200, completeResponse.length());
        OutputStream os = t.getResponseBody();
        os.write(completeResponse.getBytes());
        os.close();
    }

}

/*
 * <?xml version="1.0" encoding="utf-8"?>
 
<rss version="2.0">
 
  <channel>
    <title>Titel des Feeds</title>
    <link>URL der Webpräsenz</link>
    <description>Kurze Beschreibung des Feeds</description>
    <language>Sprache des Feeds (z. B. "de-de")</language>
    <copyright>Autor des Feeds</copyright>
    <pubDate>Erstellungsdatum("Tue, 8 Jul 2008 2:43:19")</pubDate>
    <image>
      <url>URL einer einzubindenden Grafik</url>
      <title>Bildtitel</title>
      <link>URL, mit der das Bild verknüpft ist</link>
    </image>
 
    <item>
      <title>Titel des Eintrags</title>
      <description>Kurze Zusammenfassung des Eintrags</description>
      <link>Link zum vollständigen Eintrag</link>
      <author>Autor des Artikels, E-Mail-Adresse</author>
      <guid>Eindeutige Identifikation des Eintrages</guid>
      <pubDate>Datum des Items</pubDate>
    </item>
 
    <item>
      ...
    </item>
 
  </channel>
 
</rss>
 * */
