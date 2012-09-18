package de.hska.info.electricMeter.ServerAgent.MeterReadingService;

import java.net.InetAddress;

import javax.xml.ws.Endpoint;

import org.apache.log4j.Logger;

import de.hska.info.electricMeter.ServerAgent.ServerAgentParameters;
import de.hska.info.electricMeter.ServerAgent.ServiceHandler;

public class MeterReadingServiceHandler implements ServiceHandler {
	
	public static Logger logger = Logger.getLogger(MeterReadingServiceHandler.class);
	
	Endpoint serviceEndpoint = null;

	public MeterReadingServiceHandler() {
		
	}

	@Override
	public void startService(ServerAgentParameters params) {
		//String publishAddress = "http://" + params.getHost() + ":" + params.getPort() + "/meterReadingService";
		String publishAddress = "http://192.168.1.100:8080/meterReadingService";
		logger.info("launching MeterReadingService at address " + publishAddress + " ...");
		MeterReadingServiceImpl meterReadingService = new MeterReadingServiceImpl();
		serviceEndpoint = Endpoint.publish(publishAddress, meterReadingService); // TODO check for java.net.BindException
		if(serviceEndpoint.isPublished()) {
			logger.info("MeterReadingService launched successfully.");
		} else {
			logger.error("MeterReadingService could not be launched. Exiting to system");
			System.exit(0); // no point in keeping the server agent running when no web service can be initialized
		}
	}
	
	@Override
	public void stopService() {
		if (serviceEndpoint != null) {
			logger.info("shutting down MeterReadingService...");
			serviceEndpoint.stop();
			logger.info("MeterReadingService shut down successfully.");
		}		
	}

}
