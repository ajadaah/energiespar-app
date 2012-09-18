package de.hska.info.electricMeter.ServerAgent;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import de.hska.info.electricMeter.ServerAgent.MeterReadingService.MeterReadingServiceHandler;
import de.hska.info.electricMeter.ServerAgent.rssFeedService.RssFeedServiceHandler;

public class ServerAgentMain {
	
	private static Logger logger = Logger.getRootLogger();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		PropertyConfigurator.configure("etc/log4j.properties");
		
		logger.info("MeterReading Server Agent v1.0");
		logger.info("==============================");

		ServerAgentParameters params = new ServerAgentParameters();
		JCommander cmd = new JCommander(params);
		final MeterReadingServiceHandler serviceHandler = new MeterReadingServiceHandler();
		final RssFeedServiceHandler rssFeedServiceHandler = new RssFeedServiceHandler();
	    try {
	        cmd.parse(args);
	        cmd.setProgramName("java -jar ElectricMeterServerAgent.jar");
	        //cmd.usage();
	        //
	        serviceHandler.startService(params);
	        rssFeedServiceHandler.startService(params);
	        //
	        Runtime.getRuntime().addShutdownHook(new Thread() {
	        	public void run() {
	        		Logger.getRootLogger().info("shutting down the server agent due to external shutdown call...");
	        		serviceHandler.stopService();
	        		rssFeedServiceHandler.stopService();
	        	}
	        });
	    } catch (ParameterException ex) {
	        //System.out.println(ex.getMessage());
	    	logger.error(ex.getMessage());
	    	serviceHandler.stopService();
    		rssFeedServiceHandler.stopService();
	    	cmd.usage();
	    }
		
	}

}
