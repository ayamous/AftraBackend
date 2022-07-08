package ma.itroad.aace.eth.core.service;

import org.slf4j.Logger;

public abstract class LoggerSupport {
	protected Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
}
