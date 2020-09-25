package com.dataexpo.rna.common;

public class URLs {
    //private static final String DOMAIN = "https://auth.dataexpo.com.cn/";
    private static final String DOMAIN = "http://192.168.1.30:8090/";

    public static final String LOGIN = "https://actifchina.leoscn.com/api/index/qrVerify";

    public static final String VerifyExpo = DOMAIN + "gate/verifyExpoById";

    public static final String uploadData = "http://saas.dataexpo.com.cn/custom-access/api/insert.do";

    public static final String queryUser = DOMAIN + "gate/findUserByFileCode";

    public static final String checkIn = DOMAIN + "gate/checkIn";

    public static final String offLineCheckIn = DOMAIN + "gate/updatePrintByCode";

    public static final String offLineUploadCT = DOMAIN + "gate/uploadIdCardAndTemperature";

    public static final String checkActive = DOMAIN + "gate/pdaCheckActive";

    public static final String pdaActiveSignUp = DOMAIN + "gate/pdaActiveSignUp";

    public static final String findCheckIn = "http://saas.dataexpo.com.cn/custom-access/entrance/checkSearchName.html";
}