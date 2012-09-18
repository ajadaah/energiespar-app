package de.hska.info.electricMeter.ServerAgent.rssFeedService;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

import com.sun.net.httpserver.HttpServer;

import de.hska.info.electricMeter.ServerAgent.ServerAgentParameters;
import de.hska.info.electricMeter.ServerAgent.ServiceHandler;
import de.hska.info.electricMeter.ServerAgent.MeterReadingService.MeterReadingServiceHandler;


public class RssFeedServiceHandler implements ServiceHandler {
	
	public static Logger logger = Logger.getLogger(MeterReadingServiceHandler.class);
	
	HttpServer server = null;

	@Override
	public void startService(ServerAgentParameters params) {
		// TODO Auto-generated method stub
		try {
			logger.info("launching RSSFeedService at address http://" + params.getHost() + ":8080/rss.xml.");
			server = HttpServer.create(new InetSocketAddress(8080), -1);
			server.createContext("/", new RssFeedServiceImpl());
			server.setExecutor(null); // creates a default executor
			server.start();
			logger.info("RSSFeedService launched successfully.");
		} catch (IOException e) {
			// TODO: handle exception
		}
		
	}

	@Override
	public void stopService() {
		if (server != null) {
			logger.info("shutting down RSSFeedService. this could take up to 10 seconds.");
			server.stop(10); // wait for a maximum of 10 seconds for all ongoing requests to finish
			logger.info("RSSFeedService shut down successfully.");
		}
		
	}

}
