package de.hska.info.electricMeter.ServerAgent.MeterReadingService;

import java.util.Iterator;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.apache.log4j.Logger;

import de.hska.info.electricMeter.ServerAgent.MeterReadingService.requestHandling.AbstractStorageRequestStrategy;
import de.hska.info.electricMeter.ServerAgent.MeterReadingService.requestHandling.StorageRequestHandlingContext;
import de.hska.info.electricMeter.ServerAgent.MeterReadingService.requestHandling.StorageRequestStrategyLog;

@WebService()
@SOAPBinding(style=Style.DOCUMENT)
public class MeterReadingServiceImpl {
	
	private static Logger logger = Logger.getLogger(MeterReadingServiceImpl.class);
	
	StorageRequestHandlingContext srhContext = null;
	
	public MeterReadingServiceImpl() {
		AbstractStorageRequestStrategy storageRequestStrategy = new StorageRequestStrategyLog();
		srhContext = new StorageRequestHandlingContext(storageRequestStrategy);
	}
	
	/*@WebMethod //(operationName="storeMeterReading") // note: this method is not used anymore, we don't want to spam the server with multiple requests when we can use only one, so it's better to enforce this
	public void storeMeterReading(@WebParam(name="storageRequest") MeterReadingStorageRequest storageRequest) {
		logger.debug("new storeMeterReading request");
		srhContext.executeStrategy(storageRequest);
	}*/
	
	@WebMethod(operationName="storeMeterReadings")
	public void storeMeterReadings(@WebParam(name="storageRequest") List<MeterReadingStorageRequest> storageRequestList) { 
		logger.debug("new storeMeterReadings request: " + storageRequestList.size()); //TODO @XmlElement(required=true)?
		for (MeterReadingStorageRequest meterReadingStorageRequest : storageRequestList) { // TODO handle this better by introducing an executeStrategy in the context that takes a list of storageRequests
			srhContext.executeStrategy(meterReadingStorageRequest);
		}
	}

}
