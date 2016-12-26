package com.ygl.test;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ygl.test.activity.SimpleMovieRecorderActivity;
import com.ygl.test.fragment.LoginFragment;
import com.ygl.test.fragment.MainFragment;
import com.ygl.test.fragment.TabFragment;
import com.ygl.test.inter.OnFragmentInteractionListener;

public class MainActivity extends AppCompatActivity implements OnFragmentInteractionListener{

    private Toolbar toolBar;
    private TextView toolbarTitle;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initToolbar();
        initDrawerLayout();
        initNavigationView();
        switchToMain();
    }

    private void initView() {
        toolBar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
    }

    private void initNavigationView() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_item_main:
                        switchToMain();
                        break;
                    case R.id.navigation_item_login:
                        switchToLogin();
                        break;
                    case R.id.navigation_item_tab:
                        switchToTab();
                        break;
                }
                item.setChecked(true);
                drawerLayout.closeDrawers();
                return true;
            }
        });
    }

    private void initToolbar() {
//        toolBar.setLogo(R.drawable.xlistview_arrow);
//        toolBar.setTitle("标题");
//        toolBar.setSubtitle("子标题");
        setSupportActionBar(toolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbarTitle.setText("主页");
    }

    private void initDrawerLayout() {
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolBar, R.string.drawer_open,
                R.string.drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
//                toolbarTitle.setText("打开中");
//                getSupportActionBar().setTitle("打开中");
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
//                toolbarTitle.setText("主页");
//                getSupportActionBar().setTitle("主页");
                supportInvalidateOptionsMenu();
            }

        };
        drawerToggle.syncState();
        drawerLayout.addDrawerListener(drawerToggle);
    }

    private void switchToMain() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, new MainFragment()).commit();
        toolbarTitle.setText(getString(R.string.main));
    }

    private void switchToTab() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, new TabFragment()).commit();
        toolbarTitle.setText(getString(R.string.tab));
    }

    private void switchToLogin() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, new LoginFragment()).commit();
        toolbarTitle.setText(getString(R.string.login));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.ab_search).getActionView();
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("plq", "......");
            }
        });

        // 分享,导入v7下的包
        MenuItem item=menu.findItem(R.id.action_share);
        ShareActionProvider sap= (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        Intent intent=new Intent(Intent.ACTION_SEND);// 如果是多条图片和文本, action改成:ACTION_SEND_MULTIPLE

        // 分享文本, 验证可用
//        intent.setType("text/plain");
//        intent.putExtra(Intent.EXTRA_TEXT,"hi jiujie zhu,do u have lunch?");

        // or分享图片(单张), EXTRA_STREAM:图片以二进制的形式进行传递, 验证可用
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("/storage/emulated/0/1.png"));

        // 发送多张图片
//        intent.putStringArrayListExtra(Intent.EXTRA_STREAM, new ArrayList<Uri>());

        if(sap!=null){
            sap.setShareIntent(intent);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                Toast.makeText(this, "setting", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.simple_video:
                startActivity(new Intent(this, SimpleMovieRecorderActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Toast.makeText(this.getApplicationContext(), "测试, uri = "+uri, Toast.LENGTH_SHORT).show();
    }
}
