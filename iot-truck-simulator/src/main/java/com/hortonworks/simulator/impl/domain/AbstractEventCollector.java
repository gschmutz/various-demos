package com.hortonworks.simulator.impl.domain;

import akka.actor.UntypedActor;
import org.apache.log4j.Logger;

public abstract class AbstractEventCollector extends UntypedActor {

	protected Logger logger = Logger.getLogger(this.getClass());

	public AbstractEventCollector() {
	}


}
