package com.mob.sms.network.bean;

public class BaseResponse<T> {

    public String msg;
    public int code;
    public T data;
}
