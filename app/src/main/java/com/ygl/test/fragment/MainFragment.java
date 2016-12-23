package com.ygl.test.fragment;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ygl.test.R;
import com.ygl.test.adapter.MainRecyclerAdapter;
import com.ygl.test.greendao.entity.ImageEntity;
import com.ygl.test.http.NetworkUtil;
import com.ygl.test.inter.OnFragmentInteractionListener;
import com.ygl.test.listener.RecyclerItemClickListener;

import java.util.List;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private RecyclerView recycler;
    private SwipeRefreshLayout swipeRefresh;

    private MainRecyclerAdapter adapter = null;
    private static String[] qArr = new String[]{"可爱", "110", "在下", "装逼"};
    private static int index;
    protected Subscription subscription;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i("plq", "==============");
        adapter = new MainRecyclerAdapter(getActivity());
        initView(view);
        initControl();
        initData();
    }

    private void initView(View v) {
        recycler = (RecyclerView) v.findViewById(R.id.recycler);
        swipeRefresh = (SwipeRefreshLayout) v.findViewById(R.id.swipeRefresh);
    }

    private void initControl() {
        recycler.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        recycler.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), adapter.onItemClickListener));
        recycler.setAdapter(adapter);
        recycler.setOnScrollListener(new RecyclerView.OnScrollListener() {
            //用来标记是否正在向最后一个滑动
            boolean isSlidingToLast = false;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                // 当不滚动时
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //获取最后一个完全显示的ItemPosition
                    int lastVisibleItem = manager.findLastCompletelyVisibleItemPosition();
                    int totalItemCount = manager.getItemCount();

                    // 判断是否滚动到底部，并且是向右滚动
                    if (lastVisibleItem == (totalItemCount - 1) && isSlidingToLast) {
                        //加载更多功能的代码
                        Toast.makeText(MainFragment.this.getActivity(), "加载更多", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //dx用来判断横向滑动方向，dy用来判断纵向滑动方向
                if (dy > 0) {
                    //大于0表示正在向右(下)滚动
                    isSlidingToLast = true;
                } else {
                    //小于等于0表示 页面刚初始化并且触碰列表 或向左(上)滚动
                    isSlidingToLast = false;
                }
            }
        });

        swipeRefresh.setColorSchemeColors(Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW);
        swipeRefresh.setRefreshing(true);
        swipeRefresh.setOnRefreshListener(this);
    }

    private void initData() {
        requestApi(qArr[index % qArr.length]);
    }

    private void requestApi(String param) {
        unsubscribe();

        subscription = NetworkUtil.getApi("http://zhuangbi.info/")
                .search(param)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<ImageEntity>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        swipeRefresh.setRefreshing(false);
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(List<ImageEntity> list) {
                        for (ImageEntity entity : list) {
                            Log.i("plq", "url = "+entity.getImage_url());
                        }
                        swipeRefresh.setRefreshing(false);
                        adapter.setList(list);
                        index++;
                    }
                });
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unsubscribe();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onRefresh() {
        requestApi(qArr[index % qArr.length]);
    }

    protected void unsubscribe() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

}
