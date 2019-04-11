package com.your.photograph.bean;

public enum  BankTypeEnum {

    Unknown("Unknown","未知类型"),
    Debit("Debit","借记卡"),
    Credit("Credit","信用卡");

    private String bankType;

    BankTypeEnum(String key,String value){
        this.bankType=key;
    }
}
