package com.erp.exception;

/*
 * The same action was requested again too soon (e.g. another password reset code)
 */
public class TooManyRequestsException extends BaseException {

    public TooManyRequestsException(String msg) {
        super(msg);
    }
}
