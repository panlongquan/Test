package com.ygl.test.http;


import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * author：ygl_panpan on 2016/12/20 12:01
 * email：pan.lq@i70tv.com
 */
public class NetworkUtil {

    private static final OkHttpClient okHttpClient = new OkHttpClient();
    private static final Converter.Factory gsonConvert = GsonConverterFactory.create();
    private static final CallAdapter.Factory rxCallAdapter = RxJavaCallAdapterFactory.create();
    private static RxAndRetrofitApi rxAndRetrofitApi;

    public static RxAndRetrofitApi getApi(String baseUrl){
        if (rxAndRetrofitApi == null){
            Retrofit retrofit = new Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl(baseUrl)
                    .addConverterFactory(gsonConvert)
                    .addCallAdapterFactory(rxCallAdapter)
                    .build();
            rxAndRetrofitApi = retrofit.create(RxAndRetrofitApi.class);
        }
        return rxAndRetrofitApi;
    }

    /**
     * 如果每个请求都需要相同的请求头的话, 请使用OkHttpClient的Interceptor这个方式
     * @return
     */
    public static RxAndRetrofitApi getApiNew(){
        OkHttpClient ok = new OkHttpClient();
        ok.networkInterceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                final Request originalRequest = chain.request();

                final Request requestWithUserAgent = originalRequest.newBuilder()
                        //移除先前默认的Content-Type
                        .removeHeader("Content-Type")
                        //设置Content-Type
                        .addHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                        .removeHeader("User-Agent")
                        .addHeader("User-Agent", "Android")
                        //......
                        .build();
                return chain.proceed(requestWithUserAgent);
            }
        });

        if (rxAndRetrofitApi == null){
            Retrofit retrofit = new Retrofit.Builder()
                    .client(ok)
                    .baseUrl("http://api.t.kilo.iqlin.com/")
                    .addConverterFactory(gsonConvert)
                    .addCallAdapterFactory(rxCallAdapter)
                    .build();
            rxAndRetrofitApi = retrofit.create(RxAndRetrofitApi.class);
        }
        return rxAndRetrofitApi;
    }

}
