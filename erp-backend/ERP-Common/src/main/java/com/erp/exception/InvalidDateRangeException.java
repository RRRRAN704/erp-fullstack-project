package com.erp.exception;

/*
 * Invalid date range in a report request (missing, reversed or too long)
 */
public class InvalidDateRangeException extends BaseException {

    public InvalidDateRangeException(String msg) {
        super(msg);
    }
}
