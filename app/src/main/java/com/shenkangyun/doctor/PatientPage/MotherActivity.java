package com.shenkangyun.doctor.PatientPage;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.jaeger.library.StatusBarUtil;
import com.shenkangyun.doctor.BaseFolder.Base;
import com.shenkangyun.doctor.BeanFolder.MotherBean;
import com.shenkangyun.doctor.BeanFolder.PatientBean;
import com.shenkangyun.doctor.MainPage.adapter.PatientListAdapter;
import com.shenkangyun.doctor.PatientPage.Adapter.MotherListAdapter;
import com.shenkangyun.doctor.R;
import com.shenkangyun.doctor.UtilsFolder.GsonCallBack;
import com.shenkangyun.doctor.UtilsFolder.RecyclerViewDivider;
import com.zhy.http.okhttp.OkHttpUtils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MotherActivity extends AppCompatActivity {

    @BindView(R.id.toolBar_title)
    TextView toolBarTitle;
    @BindView(R.id.toolBar)
    Toolbar toolBar;
    @BindView(R.id.mQuestionRecycler)
    RecyclerView mQuestionRecycler;
    @BindView(R.id.easyLayout)
    SwipeRefreshLayout easyLayout;

    private String id;

    private List<MotherBean.DataBean.SubmenuListBean> totalList = new ArrayList<>();
    private LinearLayoutManager manager;
    private MotherListAdapter motherListAdapter;

    private int size;
    private int pageNo = 0;
    private int pageCount = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mother);
        StatusBarUtil.setColor(this, getResources().getColor(R.color.home_red));
        ButterKnife.bind(this);
        setSupportActionBar(toolBar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            toolBarTitle.setText("产后住院母亲情况问卷");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        initView();
        initNetRequest();
        initRefresh();
    }

    private void initView() {
        Intent intent = getIntent();
        id = intent.getStringExtra("id");

        motherListAdapter = new MotherListAdapter();
        manager = new LinearLayoutManager(MotherActivity.this);
        mQuestionRecycler.addItemDecoration(new RecyclerViewDivider(this, LinearLayoutManager.VERTICAL,
                5, getResources().getColor(R.color.white)));
        mQuestionRecycler.setLayoutManager(manager);
        mQuestionRecycler.setAdapter(motherListAdapter);
    }

    private void initNetRequest() {
        pageNo = 0;
        pageCount = 10;
        totalList.clear();
        final List<MotherBean.DataBean.SubmenuListBean> listBeans = new ArrayList<>();
        OkHttpUtils.post()
                .url(Base.URL)
                .addParams("act", "selectMotherNew")
                .addParams("data", new selectMotherNew("2", "1", id, String.valueOf(pageNo), String.valueOf(pageCount),
                        Base.appKey, Base.timeSpan).toJson())
                .build()
                .execute(new GsonCallBack<MotherBean>() {
                    @Override
                    public void onSuccess(MotherBean response) {
                        size = response.getData().getSubmenuList().size();
                        for (int i = 0; i < response.getData().getSubmenuList().size(); i++) {
                            MotherBean.DataBean.SubmenuListBean listBean = new MotherBean.DataBean.SubmenuListBean();
                            String moduleCode = response.getData().getSubmenuList().get(i).getModuleCode();
                            String moduleName = response.getData().getSubmenuList().get(i).getModuleName();
                            String moduleUrl = response.getData().getSubmenuList().get(i).getModuleUrl();

                            listBean.setModuleCode(moduleCode);
                            listBean.setModuleName(moduleName);
                            listBean.setModuleUrl(moduleUrl);
                            listBean.setModuleNum(i + 1);

                            listBeans.add(listBean);
                            totalList.add(listBean);
                        }
                        motherListAdapter.setNewData(listBeans);
                        if (easyLayout.isRefreshing()) {
                            easyLayout.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
        initLoadMore();
        initClick();
    }

    private void initClick() {
        motherListAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                switch (position) {
                    case 0:
                        Intent intentCon = new Intent(MotherActivity.this, ConstitutionActivity.class);
                        intentCon.putExtra("ModuleCode", totalList.get(position).getModuleCode());
                        intentCon.putExtra("ModuleName", totalList.get(position).getModuleName());
                        intentCon.putExtra("ModuleUrl", totalList.get(position).getModuleUrl());
                        intentCon.putExtra("patientID", id);
                        startActivity(intentCon);
                        break;
                    case 1:
                        Intent intentBM = new Intent(MotherActivity.this, TreatmentActivity.class);
                        intentBM.putExtra("ModuleCode", totalList.get(position).getModuleCode());
                        intentBM.putExtra("ModuleName", totalList.get(position).getModuleName());
                        intentBM.putExtra("ModuleUrl", totalList.get(position).getModuleUrl());
                        intentBM.putExtra("patientID", id);
                        startActivity(intentBM);
                        break;
                }
            }
        });
    }

    private void initLoadMore() {
        motherListAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                mQuestionRecycler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final List<MotherBean.DataBean.SubmenuListBean> listBeans = new ArrayList<>();
                        if (!(size < pageCount)) {
                            pageNo = pageNo + size;
                            OkHttpUtils.post().url(Base.URL)
                                    .addParams("act", "selectMotherNew")
                                    .addParams("data", new selectMotherNew("2", "1", id, String.valueOf(pageNo), String.valueOf(pageCount),
                                            Base.appKey, Base.timeSpan).toJson())
                                    .build().execute(new GsonCallBack<MotherBean>() {
                                @Override
                                public void onSuccess(final MotherBean response) throws JSONException {
                                    size = response.getData().getSubmenuList().size();
                                    for (int i = 0; i < response.getData().getSubmenuList().size(); i++) {
                                        MotherBean.DataBean.SubmenuListBean listBean = new MotherBean.DataBean.SubmenuListBean();
                                        String moduleCode = response.getData().getSubmenuList().get(i).getModuleCode();
                                        String moduleName = response.getData().getSubmenuList().get(i).getModuleName();
                                        String moduleUrl = response.getData().getSubmenuList().get(i).getModuleUrl();

                                        listBean.setModuleCode(moduleCode);
                                        listBean.setModuleName(moduleName);
                                        listBean.setModuleUrl(moduleUrl);
                                        listBean.setModuleNum(i + 1);

                                        listBeans.add(listBean);
                                        totalList.add(listBean);
                                    }
                                    motherListAdapter.addData(listBeans);
                                    motherListAdapter.loadMoreComplete();
                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });
                        } else {
                            motherListAdapter.loadMoreEnd();
                        }
                    }
                }, 2000);

            }
        }, mQuestionRecycler);
        initClick();
    }

    private void initRefresh() {
        easyLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initNetRequest();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    static class selectMotherNew {

        private String appType;
        private String mobileType;
        private String patientID;
        private String pageNo;
        private String pageCount;
        private String appKey;
        private String timeSpan;

        public selectMotherNew(String appType, String mobileType, String patientID, String pageNo,
                               String pageCount, String appKey, String timeSpan) {
            this.appType = appType;
            this.mobileType = mobileType;
            this.patientID = patientID;
            this.pageNo = pageNo;
            this.pageCount = pageCount;
            this.appKey = appKey;
            this.timeSpan = timeSpan;
        }

        public String toJson() {
            return new Gson().toJson(this);
        }
    }
}
