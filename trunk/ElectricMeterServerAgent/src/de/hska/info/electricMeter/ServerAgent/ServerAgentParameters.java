package de.hska.info.electricMeter.ServerAgent;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.NoValidator;
import com.beust.jcommander.validators.PositiveInteger;

public class ServerAgentParameters {
	
	@Parameter(names = {"--port", "-p"}, required = false, description = "Sets the port for the meter reading service. Default ist 8765.", validateWith= PositiveInteger.class)
	private int port = 8765;
	
	@Parameter(names = {"--host", "-h"}, required = false, description = "Sets the host for the meter reading service. Default ist localhost. Can be set to an IP adress or an actual server name/domain adress.", validateWith=NoValidator.class)
	private String host = "localhost";

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

}
