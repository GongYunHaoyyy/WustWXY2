package com.wustwxy2.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.wustwxy2.R;
import com.wustwxy2.adapter.BaseAdapterHelper;
import com.wustwxy2.adapter.QuickAdapter;
import com.wustwxy2.bean.Found;
import com.wustwxy2.bean.Lost;
import com.wustwxy2.bean.User;
import com.wustwxy2.config.Constants;
import com.wustwxy2.i.IPopupItemClick;
import com.wustwxy2.models.EditPopupWindow;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;

import static com.wustwxy2.R.id.tv_describe;
import static com.wustwxy2.R.id.tv_photo;
import static com.wustwxy2.R.id.tv_time;
import static com.wustwxy2.R.id.tv_title;

public class MyLosingActivity extends BaseActivity implements IPopupItemClick, AdapterView.OnItemLongClickListener,
        SwipeRefreshLayout.OnRefreshListener, AbsListView.OnScrollListener {

    private static final String TAG = "MYLOSING";
    Toolbar toolbar;
    private SystemBarTintManager tintManager;
    //声明进度条对话框对象
    private ProgressDialog dialog;
    //进度条最大值
    private static final int PROGRESS_MAX = 100;

    ListView listview;

    protected QuickAdapter<Lost> LostAdapter;// 失物

    protected QuickAdapter<Found> FoundAdapter;// 招领

    //RelativeLayout progress;
    LinearLayout layout_no;
    TextView tv_no;

    private String from;

    //下拉刷新布局
    SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView() {
        setContentView(R.layout.activity_my_losing);
    }

    @Override
    public void initViews() {
        initToolbar();
        initWindow();
        //progress = (RelativeLayout) findViewById(R.id.progress);
        layout_no = (LinearLayout) findViewById(R.id.layout_no);
        tv_no = (TextView) findViewById(R.id.tv_no);
        listview = (ListView) findViewById(R.id.list_lost_mine);
        initEditPop();
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.losing_fresh_detail);
        initRefresh();
    }

    @Override
    public void initListeners() {
        listview.setOnItemLongClickListener(this);
        listview.setOnScrollListener(this);
        refreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void initData() {
        from = getIntent().getStringExtra("from");
        if (from.equals(getResources().getText(R.string.lost))) {
            toolbar.setTitle(getResources().getText(R.string.my_lost));
        } else {
            toolbar.setTitle(getResources().getText(R.string.my_found));
        }

        if (LostAdapter == null) {
            LostAdapter = new QuickAdapter<Lost>(this, R.layout.item_list_losing) {
                @Override
                protected void convert(BaseAdapterHelper helper, Lost lost) {
                    helper.setText(tv_title, lost.getTitle())
                            .setText(tv_describe, lost.getDescribe())
                            .setText(tv_time, lost.getCreatedAt())
                            .setText(tv_photo, lost.getPhone());
                }
            };
        }
        if (FoundAdapter == null) {
            FoundAdapter = new QuickAdapter<Found>(this, R.layout.item_list_losing) {
                @Override
                protected void convert(BaseAdapterHelper helper, Found found) {
                    helper.setText(tv_title, found.getTitle())
                            .setText(tv_describe, found.getDescribe())
                            .setText(tv_time, found.getCreatedAt())
                            .setText(tv_photo, found.getPhone());
                }
            };
        }

        if (from.equals(getResources().getText(R.string.lost))) {
            Log.i(TAG, from);
            listview.setAdapter(LostAdapter);
            startRefresh();
        } else {
            Log.i(TAG, from);
            listview.setAdapter(FoundAdapter);
            startRefresh();
        }
        dialog = new ProgressDialog(this);
        //设置进度条样式
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setIndeterminate(true);
        //失去焦点的时候，不是去对话框
        dialog.setCancelable(false);
        dialog.setMessage("正在删除...");
    }

    public void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initRefresh() {
        //设置圈圈颜色
        refreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.yellow,
                R.color.titleLightBlue, R.color.green);
        //设置大小
        refreshLayout.setSize(SwipeRefreshLayout.DEFAULT);
        //设置背景颜色
        refreshLayout.setProgressBackgroundColorSchemeResource(R.color.fresh_bg);
        //设置距离顶部距离
        refreshLayout.setProgressViewEndTarget(true, 200);
    }

    /**
     * 查询全部失物信息 queryLosts
     *
     * @return void
     * @throws
     */
    private void queryLosts() {
        showView();
        BmobQuery<Lost> query = new BmobQuery<Lost>();
        BmobUser user = BmobUser.getCurrentUser(User.class);
        query.addWhereEqualTo("author", user);
        query.order("-updatedAt");// 按照修改时间排序
        query.findObjects(new FindListener<Lost>() {
            @Override
            public void done(List<Lost> losts, BmobException e) {
                if (e == null) {
                    LostAdapter.clear();
                    FoundAdapter.clear();
                    if (losts == null || losts.size() == 0) {
                        showErrorView(0);
                        if (refreshLayout != null)
                            //刷新的spinner停止旋转
                            refreshLayout.setRefreshing(false);
                        LostAdapter.notifyDataSetChanged();
                        return;
                    }
                    //progress.setVisibility(View.GONE);
                    LostAdapter.addAll(losts);
                    listview.setAdapter(LostAdapter);
                    Log.i(TAG, losts.size() + "");
                    if (refreshLayout != null)
                        //刷新的spinner停止旋转
                        refreshLayout.setRefreshing(false);
                } else {
                    if (refreshLayout != null)
                        //刷新的spinner停止旋转
                        refreshLayout.setRefreshing(false);
                    Log.i(TAG, "queryLost失败");
                    showErrorView(2);
                }
            }
        });
    }

    public void queryFounds() {
        showView();
        BmobQuery<Found> query = new BmobQuery<Found>();
        BmobUser user = BmobUser.getCurrentUser(User.class);
        query.addWhereEqualTo("author", user);
        query.order("-updatedAt");// 按照修改时间排序
        query.findObjects(new FindListener<Found>() {

            @Override
            public void done(List<Found> founds, BmobException e) {
                if (e == null) {
                    LostAdapter.clear();
                    FoundAdapter.clear();
                    if (founds == null || founds.size() == 0) {
                        showErrorView(1);
                        FoundAdapter.notifyDataSetChanged();
                        if (refreshLayout != null)
                            //刷新的spinner停止旋转
                            refreshLayout.setRefreshing(false);
                        return;
                    }
                    FoundAdapter.addAll(founds);
                    listview.setAdapter(FoundAdapter);
                    //progress.setVisibility(View.GONE);
                    Log.i(TAG, founds.size() + "");
                    if (refreshLayout != null)
                        //刷新的spinner停止旋转
                        refreshLayout.setRefreshing(false);
                } else {
                    if (refreshLayout != null)
                        //刷新的spinner停止旋转
                        refreshLayout.setRefreshing(false);
                    showErrorView(2);
                }
            }
        });
    }

    /**
     * 请求出错或者无数据时候显示的界面 showErrorView
     *
     * @return void
     * @throws
     */
    private void showErrorView(int tag) {
        //progress.setVisibility(View.GONE);
        listview.setVisibility(View.GONE);
        layout_no.setVisibility(View.VISIBLE);
        if (tag == 0) {
            tv_no.setText(getResources().getText(R.string.list_no_data_lost));
        } else if (tag == 1) {
            tv_no.setText(getResources().getText(R.string.list_no_data_found));
        } else {
            tv_no.setText(getResources().getText(R.string.losing_error));
        }
    }

    private void showView() {
        listview.setVisibility(View.VISIBLE);
        layout_no.setVisibility(View.GONE);
    }

    @Override
    public void onEdit(View v) {
        // TODO Auto-generated method stub
        Intent intent = new Intent(this, EditLosingActivity.class);
        String title = "";
        String describe = "";
        String phone = "";
        String objectId = "";
        if (from.equals(getResources().getText(R.string.lost))) {
            title = LostAdapter.getItem(position).getTitle();
            describe = LostAdapter.getItem(position).getDescribe();
            phone = LostAdapter.getItem(position).getPhone();
            objectId = LostAdapter.getItem(position).getObjectId();

        } else {
            title = FoundAdapter.getItem(position).getTitle();
            describe = FoundAdapter.getItem(position).getDescribe();
            phone = FoundAdapter.getItem(position).getPhone();
            objectId = FoundAdapter.getItem(position).getObjectId();
        }
        intent.putExtra("describe", describe);
        intent.putExtra("phone", phone);
        intent.putExtra("title", title);
        intent.putExtra("from", from);
        intent.putExtra("objectId", objectId);
        startActivityForResult(intent, Constants.REQUESTCODE_ADD);
    }

    @Override
    public void onDelete(View v) {
        // TODO Auto-generated method stub
        if (from.equals(getResources().getText(R.string.lost))) {
            deleteLost();
        } else {
            deleteFound();
        }
    }

    private void deleteLost() {
        String objectId = LostAdapter.getItem(position).getObjectId();
        BmobQuery<Lost> query = new BmobQuery<Lost>();
        //显示对话框
        dialog.show();
        //根据objectId查找出这条记录，从而获取photoUrl
        query.getObject(objectId, new QueryListener<Lost>() {

            @Override
            public void done(Lost object, BmobException e) {
                if (e == null) {
                    BmobFile file = new BmobFile();
                    String url = object.getPhotoUrl();
                    //如果url为空，即说明未上传图片，故不需删除图片
                    if(url == null){
                        Log.d(TAG, "url null");
                        Lost lost = new Lost();
                        lost.setObjectId(LostAdapter.getItem(position).getObjectId());
                        //删除相应的记录
                        lost.delete(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                    LostAdapter.remove(position);
                                    ShowToast("删除成功");
                                    dialog.dismiss();
                                } else {
                                    ShowToast("删除失败");
                                    dialog.dismiss();
                                }
                            }
                        });
                    }else{
                        Log.i("DELETELOSING", object.getTitle() + ":" + url);
                        file.setUrl(url);
                        //根据url删除文件
                        file.delete(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                    Lost lost = new Lost();
                                    lost.setObjectId(LostAdapter.getItem(position).getObjectId());
                                    //删除相应的记录
                                    lost.delete(new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {
                                            if (e == null) {
                                                LostAdapter.remove(position);
                                                ShowToast("删除成功");
                                                dialog.dismiss();
                                            } else {
                                                ShowToast("删除失败");
                                                dialog.dismiss();
                                            }
                                        }
                                    });
                                } else {
                                    ShowToast("删除失败");
                                    dialog.dismiss();
                                }
                            }
                        });
                    }
                } else {
                    ShowToast("删除失败");
                    dialog.dismiss();
                }
            }
        });
    }

    private void deleteFound() {
        String objectId = FoundAdapter.getItem(position).getObjectId();
        BmobQuery<Found> query = new BmobQuery<Found>();
        //显示对话框
        dialog.show();
        //根据objectId查找出这条记录，从而获取photoUrl
        query.getObject(objectId, new QueryListener<Found>() {

            @Override
            public void done(Found object, BmobException e) {
                if (e == null) {
                    BmobFile file = new BmobFile();
                    String url = object.getPhotoUrl();
                    //如果url为空，即说明未上传图片，故不需删除图片
                    if(url == null){
                        Log.d(TAG, "url null");
                        Found found = new Found();
                        found.setObjectId(FoundAdapter.getItem(position).getObjectId());
                        //删除相应的记录
                        found.delete(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                    FoundAdapter.remove(position);
                                    ShowToast("删除成功");
                                    dialog.dismiss();
                                } else {
                                    ShowToast("删除失败");
                                    dialog.dismiss();
                                }
                            }
                        });
                    }else{
                        Log.i("DELETELOSING", object.getTitle() + ":" + url);
                        file.setUrl(url);
                        //根据url删除文件
                        file.delete(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                    Found found = new Found();
                                    found.setObjectId(FoundAdapter.getItem(position).getObjectId());
                                    //删除相应的记录
                                    found.delete(new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {
                                            if (e == null) {
                                                FoundAdapter.remove(position);
                                                ShowToast("删除成功");
                                                dialog.dismiss();
                                            } else {
                                                ShowToast("删除失败");
                                                dialog.dismiss();
                                            }
                                        }
                                    });
                                } else {
                                    ShowToast("删除失败");
                                    dialog.dismiss();
                                }
                            }
                        });
                    }
                } else {
                    ShowToast("删除失败");
                    dialog.dismiss();
                }
            }
        });
    }


    private void initEditPop() {
        mPopupWindow = new EditPopupWindow(this, 200, 48);
        mPopupWindow.setOnPopupItemClickListner(this);
    }

    EditPopupWindow mPopupWindow;
    int position;

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {
        // TODO Auto-generated method stub
        position = arg2;
        int[] location = new int[2];
        arg1.getLocationOnScreen(location);
        mPopupWindow.showAtLocation(arg1, Gravity.RIGHT | Gravity.TOP,
                location[0], getStateBar() + location[1]);
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case Constants.REQUESTCODE_ADD:// 添加成功之后的回调
                String tag = from;
                if (tag.equals(getResources().getText(R.string.lost))) {
                    queryLosts();
                } else {
                    queryFounds();
                }
                break;
        }
    }

    //设置沉浸式状态栏和导航栏
    private void initWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintColor(getResources().getColor(R.color.colorPrimary));
            tintManager.setStatusBarTintEnabled(true);
        }
    }

    @Override
    public void onRefresh() {
        if (from.equals(getResources().getText(R.string.lost))) {
            queryLosts();
        } else {
            queryFounds();
        }
    }

    //人工使刷新圈开始转动
    private void startRefresh() {
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
            }
        });
        onRefresh();
    }

    //监听滑动事件，如到达第一项才可下滑更新
    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem == 0) {
            refreshLayout.setEnabled(true);
        } else {
            refreshLayout.setEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home://增加点击事件
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
