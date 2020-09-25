package com.dataexpo.rna.pojo;

public class SignData {
    private Integer id;
    private String a_code;
    private String eucode;
    private String name;
    private String idcard;
    private String temp;
    private String time;
    private String address;
    private String cardFace;
    private Integer expoId;
    //数据类型，0离线模式数据，1在线模式数据
    private Integer modeType;

    public SignData(){}

    public SignData(int id, String eucode, String time, String name, String idcard, String temperature, Integer modeType, String address) {
        this.id = id;
        this.eucode = eucode;
        this.time = time;
        this.name = name;
        this.idcard = idcard;
        this.temp = temperature;
        this.modeType = modeType;
        this.address = address;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getModeType() {
        return modeType;
    }

    public void setModeType(Integer modeType) {
        this.modeType = modeType;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getEucode() {
        return eucode;
    }

    public void setEucode(String eucode) {
        this.eucode = eucode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdcard() {
        return idcard;
    }

    public void setIdcard(String idcard) {
        this.idcard = idcard;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getA_code() {
        return a_code;
    }

    public void setA_code(String a_code) {
        this.a_code = a_code;
    }

    public String getCardFace() {
        return cardFace;
    }

    public void setCardFace(String cardFace) {
        this.cardFace = cardFace;
    }

    public Integer getExpoId() {
        return expoId;
    }

    public void setExpoId(Integer expoId) {
        this.expoId = expoId;
    }
}
