package main.util;

import java.util.logging.Level;

public class InformationEvent {

	public enum Type {
		LOG, THROWABLE
	}

	private final Level level;
	private final String log;
	private final Throwable throwable;
	private final Type type;
	
	public InformationEvent(@SuppressWarnings("SameParameterValue") Level level, String log) {
		this.level = level;
		this.log = log;
		this.type = Type.LOG;
        this.throwable = null;
	}
	
	public InformationEvent(Throwable throwable) {
        this.level = null;
        this.log = null;
		this.throwable = throwable;
		this.type = Type.THROWABLE;
	}
	
	public Level getLevel() {
		return level;
	}

	public String getLog() {
		return log;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public Type getType() {
		return type;
	}
	
}
