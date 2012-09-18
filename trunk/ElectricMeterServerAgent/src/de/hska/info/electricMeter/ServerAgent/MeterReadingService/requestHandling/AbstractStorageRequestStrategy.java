package de.hska.info.electricMeter.ServerAgent.MeterReadingService.requestHandling;

import de.hska.info.electricMeter.ServerAgent.MeterReadingService.MeterReadingStorageRequest;

public interface AbstractStorageRequestStrategy {
	
	void execute(MeterReadingStorageRequest storageRequest);

}
