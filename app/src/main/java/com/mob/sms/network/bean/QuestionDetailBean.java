package com.mob.sms.network.bean;

public class QuestionDetailBean {
    public String msg;
    public int code;
    public DataBean data;

    public static class DataBean {
        public int id;
        public String title;
        public int sort;
        public String answer;
    }
}
