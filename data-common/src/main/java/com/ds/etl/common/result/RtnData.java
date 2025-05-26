package com.ds.etl.common.result;

public class RtnData {
    public static final String RTN_CODE_SUCCESS = "000000";
    public static final String RTN_CODE_FAIL = "999999";
    public static final String RTN_STATUS_SUCCESS = "OK";
    public static final String RTN_STATUS_ERROR = "ERROR";
    private String code;
    private String message;
    private Object result;
    private String status;

    public RtnData() {
    }

    public static RtnData ok() {
        return ok((Object) null);
    }

    public static RtnData ok(Object result) {
        RtnData rtnData = new RtnData();
        rtnData.setCode(RTN_CODE_SUCCESS);
        rtnData.setStatus(RTN_STATUS_SUCCESS);
        rtnData.setResult(result);
        return rtnData;
    }

    public static RtnData fail(String message) {
        return fail(RTN_CODE_FAIL, message);
    }

    public static RtnData fail(String code, String message) {
        return fail((Object) null, code, message);
    }

    public static RtnData fail(Object result, String code, String message) {
        RtnData rtnData = new RtnData();
        rtnData.setCode(code);
        rtnData.setMessage(message);
        rtnData.setStatus(RTN_STATUS_ERROR);
        rtnData.setResult(result);
        return rtnData;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getResult() {
        return this.result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

