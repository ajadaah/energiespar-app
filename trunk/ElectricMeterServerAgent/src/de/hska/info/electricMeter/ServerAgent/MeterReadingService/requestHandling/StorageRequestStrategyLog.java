package de.hska.info.electricMeter.ServerAgent.MeterReadingService.requestHandling;

import org.apache.log4j.Logger;

import de.hska.info.electricMeter.ServerAgent.MeterReadingService.MeterReadingStorageRequest;

public class StorageRequestStrategyLog implements
		AbstractStorageRequestStrategy {
	
	private static Logger logger = Logger.getLogger(StorageRequestStrategyLog.class);

	@Override
	public void execute(MeterReadingStorageRequest storageRequest) {
		logger.info("handling a storageRequest...");
		logger.info("+++ request details: connection info: " + storageRequest.getConnectionInfo()
				+"; time stamp: " + storageRequest.getTimeStamp()
				+"; meter number: " + storageRequest.getMeterNumber()
				+"; meter value:" + storageRequest.getMeterValue()
				+"; meter type: " + storageRequest.getMeterType()
				+"; meter reading revised: " + storageRequest.isMeterValueRevised()
				+ " +++");
	}

}
