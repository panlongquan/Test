package com.ygl.test.http;


import com.ygl.test.greendao.entity.Data;

import java.util.List;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * author：ygl_panpan on 2016/12/20 12:01
 * email：pan.lq@i70tv.com
 */
public interface RxAndRetrofitApi {

    /**
     *  "search/{number}/{page}"是路径, 拼接到base_url后面, 如本例中的URL: "https://api.t.kilo.iqlin.com/search/20/5"
     *  注意1: 请求路径中的未知参数使用@Path标记, get请求的请求参数使用@Query标记, post请求的请求参数使用@Field标记
     *        在这些@后面的括号中填写将要被赋值的参数的key, 调用者只需要传递相应的value进来既可
     *  注意2: 如果base_url的最后面没有带"/", 那么@GET的参数路径必须要加上"/", 比如这样:"/search/{number}/{page}"
     *
     *   使用(常规使用):
     *      NetworkUtil.getApi(Constant.BASE_URL_TEST)
     *           .search(20, 2, "可爱")
     *           .subscribeOn(Schedulers.io())
     *           .observeOn(AndroidSchedulers.mainThread())
     *           .subscribe(new Observer<List<Data>>() {
     *              @Override
     *              public void onCompleted() {
     *                  Log.i("result", "onCompleted");
     *              }
     *
     *              @Override
     *              public void onError(Throwable e) {
     *                  Log.i("result", e.getMessage());
     *              }
     *
     *              @Override
     *              public void onNext(List<Data> datas) {
     *                  Log.i("result", datas.toString());
     *              }
     *          });
     *
     * @param query 请求参数的value
     * @return List<Data>
     *
     *      这个接口是不存在的, 仅仅只是举例, 请不要调用这个接口
     */
    @GET("search/{number}/{page}")
    Observable<List<Data>> search(@Path("number") int nunber, @Path("page") int page, @Query("q") String query);

    /**
     *  "kilo/apis/news/home.action"是路径, 拼接到base_url后面, 如本例中的URL: "https://api.t.kilo.iqlin.com/kilo/apis/news/home.action"
     *  注意1: 请求路径中的未知参数使用@Path标记, get请求的请求参数使用@Query标记, post请求的请求参数使用@Field标记
     *        在这些@后面的括号中填写将要被赋值的参数的key, 调用者只需要传递相应的value进来既可
     *  注意2: 如果base_url的最后面没有带"/", 那么@POST的参数路径必须要加上"/", 比如这样:"/kilo/apis/news/home.action"
     *  注意3: post请求中墙裂建议加上@FormUrlEncoded, 保持request和response编码一致
     *  注意4: 请求头的添加有多种方式, 下面为您演示了两种
     *
     *  使用(常规使用): 同上
     *
     * @param deviceId 设备Id
     * @param location location
     * @return
     *
     *      这个接口是不存在的, 仅仅只是举例, 请不要调用这个接口
     */
    @FormUrlEncoded
    //这样添加头
    @Headers("Content-Type: application/x-www-form-urlencoded;charset=utf-8")
    //或者这样:
//    @Headers({
//            "User-Agent: Android",
//            "version-type: news"
//    })
    @POST("kilo/apis/news/home.action")
    Observable<Data> getHomeNews(@Field("deviceId") String deviceId, @Field("location") String location);


    /**↓↓↓------以下是rxjava和retrofit结合使用的其他用法案例, 不可删除------↓↓↓**/
//    private Subscription subscription;
//
//    /**
//     * 转换map: 把返回的数据转换成更方便处理的格式再交给 Observer。
//     * @param v
//     */
//    public void click2(View v){
//        subscription = NetworkUtil.getApi("base_url")
//                .getBeauties(10, ++page)
//                .map(GankBeautyResultToItemsMapper.getInstance())
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<List<Item>>() {
//                    @Override
//                    public void onCompleted() {
//                        Log.i("result", "onCompleted");
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.i("result", e.getMessage());
//                    }
//
//                    @Override
//                    public void onNext(List<Item> items) {
//                        Log.i("result", items.toString());
//                    }
//                });
//    }
//
//    /**
//     * 接口并行并压合数据Zip: 将不同接口并行请求获取到的数据糅合在一起后再处理。
//     * @param v
//     */
//    public void click3(View v){
//        if (subscription != null && !subscription.isUnsubscribed()) {
//            subscription.unsubscribe();
//        }
//
//        subscription = Observable.zip(
//                NetworkUtil.getApi("base_url").getBeauties(200, 1).map(GankBeautyResultToItemsMapper.getInstance()),
//                NetworkUtil.getApi("base_url").search("装逼"),
//                new Func2<List<Item>, List<ZhuangbiImage>, List<Item>>() {
//                    @Override
//                    public List<Item> call(List<Item> gankItems, List<ZhuangbiImage> zhuangbiImages) {
//                        List<Item> items = new ArrayList<Item>();
//                        for (int i = 0; i < gankItems.size() / 2 && i < zhuangbiImages.size(); i++) {
//                            items.add(gankItems.get(i * 2));
//                            items.add(gankItems.get(i * 2 + 1));
//                            Item zhuangbiItem = new Item();
//                            ZhuangbiImage zhuangbiImage = zhuangbiImages.get(i);
//                            zhuangbiItem.description = zhuangbiImage.description;
//                            zhuangbiItem.imageUrl = zhuangbiImage.image_url;
//                            items.add(zhuangbiItem);
//                        }
//                        return items;
//                    }
//                })
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<List<Item>>() {
//                    @Override
//                    public void onCompleted() {
//                        Log.i("result", "onCompleted");
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.i("result", "Throwable = "+e.getMessage());
//                    }
//
//                    @Override
//                    public void onNext(List<Item> items) {
//                        Log.i("result", items.toString());
//                    }
//                });
//    }
//
//    /**
//     * 接口串行FlatMap: 需要先请求 token 再访问的接口，
//     *      使用 flatMap() 将 token 的请求和实际数据的请求连贯地串起来，而不必写嵌套的 Callback 结构。
//     * @param v
//     */
//    public void click4(View v){
//        if (subscription != null && !subscription.isUnsubscribed()) {
//            subscription.unsubscribe();
//        }
//
//        new FakeApi().getFakeToken("fake_auth_code") //这一步其实就是相当于这样: NetworkUtil.getApi(Constant.BASE_URL_MAP).getBeauties(100, 1), 因为找不到现成合用的接口, 所以伪造了一个本地方法当做接口
//                .flatMap(new Func1<FakeToken, Observable<FakeThing>>() {
//                    @Override
//                    public Observable<FakeThing> call(FakeToken fakeToken) {
//                        //fakeToken 是接口getFakeToken返回的数据
//                        //使用接口getFakeToken返回的数据来请求接口getFakeData
//                        return new FakeApi().getFakeData(fakeToken);
//                    }
//                })
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<FakeThing>() {
//                    @Override
//                    public void onCompleted() {
//                        Log.i("result", "onCompleted");
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.i("result", e.getMessage());
//                    }
//
//                    @Override
//                    public void onNext(FakeThing fakeThing) {
//                        Log.i("result", fakeThing.toString());
//                    }
//                });
//    }
//
//    final FakeToken cachedFakeToken = new FakeToken(true);
//
//    /**
//     * 修复失效并继续之前失败的请求RetryWhen
//     *      对于非一次性的 token （即可重复使用的 token），在获取 token 后将它保存起来反复使用，
//     *      并通过 retryWhen() 实现 token 失效时的自动重新获取，将 token 获取的流程彻底透明化，简化开发流程。
//     * @param v
//     */
//    public void click5(View v){
//        if (subscription != null && !subscription.isUnsubscribed()) {
//            subscription.unsubscribe();
//        }
//
//        Observable.just(null)
//                .flatMap(new Func1<Object, Observable<FakeThing>>() {
//                    @Override
//                    public Observable<FakeThing> call(Object o) {
//                        return cachedFakeToken.token == null ?
//                                Observable.<FakeThing>error(new NullPointerException("token is null")) :
//                                new FakeApi().getFakeData(cachedFakeToken);
//                    }
//                })
//                .retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
//                    @Override
//                    public Observable<?> call(Observable<? extends Throwable> observable) {
//                        return observable.flatMap(new Func1<Throwable, Observable<?>>() {
//                            @Override
//                            public Observable<?> call(Throwable throwable) {
//                                if (throwable instanceof IllegalArgumentException || throwable instanceof NullPointerException){
//                                    return new FakeApi().getFakeToken("fake_auth_code")
//                                            .doOnNext(new Action1<FakeToken>() {
//                                                @Override
//                                                public void call(FakeToken fakeToken) {
//                                                    cachedFakeToken.token = fakeToken.token;
//                                                    cachedFakeToken.expired = fakeToken.expired;
//                                                }
//                                            });
//                                }
//                                return Observable.error(throwable);
//                            }
//                        });
//                    }
//                })
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Action1<FakeThing>() {
//                    @Override
//                    public void call(FakeThing fakeThing) {
//                        Log.i("result", fakeThing.toString());
//                    }
//                }, new Action1<Throwable>() {
//                    @Override
//                    public void call(Throwable throwable) {
//                        Log.i("result", throwable.getMessage());
//                    }
//                });
//    }
//
//    public class GankBeautyResultToItemsMapper implements Func1<GankBeautyResult, List<Item>> {
//
//        private static GankBeautyResultToItemsMapper instance = new GankBeautyResultToItemsMapper();
//
//        private GankBeautyResultToItemsMapper() {
//        }
//
//        public static GankBeautyResultToItemsMapper getInstance() {
//            return instance;
//        }
//
//        @Override
//        public List<Item> call(GankBeautyResult gankBeautyResult) {
//            List<GankBeauty> gankBeauties = gankBeautyResult.beauties;
//            List<Item> items = new ArrayList<>(gankBeauties.size());
//            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'");
//            SimpleDateFormat outputFormat = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
//            for (GankBeauty gankBeauty : gankBeauties) {
//                Item item = new Item();
//                try {
//                    Date date = inputFormat.parse(gankBeauty.createdAt);
//                    item.description = outputFormat.format(date);
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                    item.description = "unknown date";
//                }
//                item.imageUrl = gankBeauty.url;
//                items.add(item);
//            }
//            return items;
//        }
//    }
//
//    /**
//     * 不写成接口是因为这是伪造的一个接口(本地方法)
//     */
//    public class FakeApi {
//
//        Random random = new Random();
//
//        public Observable<FakeToken> getFakeToken(@NonNull String fakeAuth) {
//            return Observable.just(fakeAuth)
//                    .map(new Func1<String, FakeToken>() {
//                        @Override
//                        public FakeToken call(String fakeAuth) {
//                            // Add some random delay to mock the network delay
//                            int fakeNetworkTimeCost = random.nextInt(500) + 500;
//                            try {
//                                Thread.sleep(fakeNetworkTimeCost);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//
//                            FakeToken fakeToken = new FakeToken();
//                            fakeToken.token = createToken();
//                            return fakeToken;
//                        }
//                    });
//        }
//
//        private static String createToken() {
//            return "fake_token_" + System.currentTimeMillis() % 10000;
//        }
//
//        public Observable<FakeThing> getFakeData(FakeToken fakeToken) {
//            return Observable.just(fakeToken)
//                    .map(new Func1<FakeToken, FakeThing>() {
//                        @Override
//                        public FakeThing call(FakeToken fakeToken) {
//                            // Add some random delay to mock the network delay
//                            int fakeNetworkTimeCost = random.nextInt(500) + 500;
//                            try {
//                                Thread.sleep(fakeNetworkTimeCost);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//
//                            if (fakeToken.expired) {
//                                throw new IllegalArgumentException("Token expired!");
//                            }
//
//                            FakeThing fakeData = new FakeThing();
//                            fakeData.id = (int) (System.currentTimeMillis() % 1000);
//                            fakeData.name = "FAKE_USER_" + fakeData.id;
//                            return fakeData;
//                        }
//                    });
//        }
//    }
//
//    public class Item {
//        public String description;
//        public String imageUrl;
//    }
//
//    public class ZhuangbiImage {
//        public String description;
//        public String image_url;
//    }
//
//    public class FakeToken {
//        public String token;
//        public boolean expired;
//
//        public FakeToken() {
//        }
//
//        public FakeToken(boolean expired) {
//            this.expired = expired;
//        }
//    }
//
//    public class FakeThing {
//        public int id;
//        public String name;
//    }
//
//    public class GankBeautyResult {
//        public boolean error;
//        public @SerializedName("results")
//        List<GankBeauty> beauties;
//    }
//
//    public class GankBeauty {
//        public String createdAt;
//        public String url;
//    }
    /**↑↑↑------以上是rxjava和retrofit结合使用的其他用法案例, 不可删除------↑↑↑**/


}
