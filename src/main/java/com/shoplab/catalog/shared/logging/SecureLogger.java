package com.shoplab.catalog.shared.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecureLogger {

    private final Logger logger;

    private SecureLogger(Logger logger) {
        this.logger = logger;
    }

    public static SecureLogger getLogger(Class<?> clazz) {
        return new SecureLogger(LoggerFactory.getLogger(clazz));
    }

    public void info(String message, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(sanitize(message), sanitizeArgs(args));
        }
    }

    public void warn(String message, Object... args) {
        if (logger.isWarnEnabled()) {
            logger.warn(sanitize(message), sanitizeArgs(args));
        }
    }

    public void error(String message, Object... args) {
        if (logger.isErrorEnabled()) {
            logger.error(sanitize(message), sanitizeArgs(args));
        }
    }

    public void error(String message, Throwable throwable) {
        if (logger.isErrorEnabled()) {
            logger.error(sanitize(message), throwable);
        }
    }

    private String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return input.replace('\n', '_').replace('\r', '_');
    }

    private Object[] sanitizeArgs(Object... args) {
        if (args == null) {
            return null;
        }
        Object[] clean = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String s) {
                clean[i] = sanitize(s);
            } else {
                clean[i] = args[i];
            }
        }
        return clean;
    }
}
