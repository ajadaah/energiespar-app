package de.hska.info.electricMeter.ServerAgent.MeterReadingService.requestHandling;

import java.util.List;

import de.hska.info.electricMeter.ServerAgent.MeterReadingService.MeterReadingStorageRequest;

public class StorageRequestStrategyComposite implements
		AbstractStorageRequestStrategy {
	
	private List<AbstractStorageRequestStrategy> storageRequestStrategyList = null;
	
	public StorageRequestStrategyComposite(List<AbstractStorageRequestStrategy> storageRequestStrategyList) {
		this.storageRequestStrategyList = storageRequestStrategyList;
	}

	@Override
	public void execute(MeterReadingStorageRequest storageRequest) {
		for (AbstractStorageRequestStrategy storageRequestStrategy : storageRequestStrategyList) {
			storageRequestStrategy.execute(storageRequest);
		}
	}

}
