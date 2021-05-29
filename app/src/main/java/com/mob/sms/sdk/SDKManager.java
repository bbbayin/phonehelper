package com.mob.sms.sdk;

import java.util.Observable;

public class SDKManager extends Observable {

    public void init(){
        setChanged();
        notifyObservers();
    }
}
