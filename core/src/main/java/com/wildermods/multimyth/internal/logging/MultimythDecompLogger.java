package com.wildermods.multimyth.internal.logging;

import java.io.IOException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;

import net.fabricmc.loom.util.IOStringConsumer;

public class MultimythDecompLogger extends IFernflowerLogger implements IOStringConsumer {

	public static final Logger logger = LogManager.getLogger("DECOMP");
	
	@Override
	public void accept(String data) throws IOException {
		logger.info(data);
	}

	@Override
	public void writeMessage(String message, Severity severity) {
		switch(severity) {
			case ERROR:
				logger.error(message);
				break;
			case INFO:
			default:
				logger.info(message);
				break;
			case TRACE:
				logger.trace(message);
				break;
			case WARN:
				logger.warn(message);
				break;
		}
	}

	@Override
	public void writeMessage(String message, Severity severity, Throwable t) {
		writeMessage(message, severity);
		switch(severity) {
			case ERROR:
			default:
				logger.catching(Level.ERROR, t);
				break;
			case INFO:
				logger.catching(Level.INFO, t);
				break;
			case TRACE:
				logger.catching(Level.TRACE, t);
				break;
			case WARN:
				logger.catching(Level.WARN, t);
				break;
		}
	}

}
