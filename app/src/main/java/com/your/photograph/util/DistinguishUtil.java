package com.your.photograph.util;


import android.content.Context;

/**
 * 初始化
 */
public class DistinguishUtil {

    //设置APPID/AK/SK
    public static final String APP_ID = "15983278";
    public static final String API_KEY = "SC7ew1lroQccKBq2TSCSlQcw";
    public static final String SECRET_KEY = "qlBC0herZRHoGYjRq5uFq4rhURBW6Nff";


    public  Context mContext;

    public static DistinguishUtil instance;

    public DistinguishUtil(Context context){
        this.mContext=context;
    }



    public static DistinguishUtil getInstance(Context context){
        if (instance==null){
            synchronized (DistinguishUtil.class){
                if (instance==null){
                    instance=new DistinguishUtil(context);
                }
            }
        }
        return instance;
    }



    //IDCardParams.ID_CARD_SIDE_FRONT
   //IDCardParams.ID_CARD_SIDE_BACK


}
