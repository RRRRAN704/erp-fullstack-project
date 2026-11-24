package com.erp.exception;

/**
 * The LLM behind the AI assistant could not be reached (out of credit, timeout, outage...).
 */
public class AiUnavailableException extends BaseException {

    public AiUnavailableException(String msg) {
        super(msg);
    }
}
