package de.hska.info.electricMeter.ServerAgent.MeterReadingService.requestHandling;

import de.hska.info.electricMeter.ServerAgent.MeterReadingService.MeterReadingStorageRequest;

public class StorageRequestHandlingContext {
	
	private AbstractStorageRequestStrategy storageRequestStrategy = null;
	
	public StorageRequestHandlingContext(AbstractStorageRequestStrategy storageRequestStrategy) {
		this.storageRequestStrategy = storageRequestStrategy;
	}
	
	public void executeStrategy(MeterReadingStorageRequest storageRequest) {
		this.storageRequestStrategy.execute(storageRequest);
	}

}
