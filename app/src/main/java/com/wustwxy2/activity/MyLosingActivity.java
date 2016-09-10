package com.wustwxy2.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
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

public class MyLosingActivity extends BaseActivity implements IPopupItemClick, AdapterView.OnItemLongClickListener {

    Toolbar toolbar;
    private SystemBarTintManager tintManager;
    //�����������Ի������
    private ProgressDialog dialog;
    //���������ֵ
    private static final int PROGRESS_MAX=100;

    ListView listview;

    protected QuickAdapter<Lost> LostAdapter;// ʧ��

    protected QuickAdapter<Found> FoundAdapter;// ����

    RelativeLayout progress;
    LinearLayout layout_no;
    TextView tv_no;


    private String from;

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
        progress = (RelativeLayout) findViewById(R.id.progress);
        layout_no = (LinearLayout) findViewById(R.id.layout_no);
        tv_no = (TextView) findViewById(R.id.tv_no);
        listview = (ListView) findViewById(R.id.list_lost_mine);
        initEditPop();
    }

    @Override
    public void initListeners() {
        listview.setOnItemLongClickListener(this);
    }

    @Override
    public void initData() {
        from = getIntent().getStringExtra("from");
        if (from.equals("Lost")) {
            toolbar.setTitle("�ҵ�ʧ��");
        } else {
            toolbar.setTitle("�ҵ�Ѱ��");
        }

        if (LostAdapter == null) {
            LostAdapter = new QuickAdapter<Lost>(this, R.layout.item_list) {
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
            FoundAdapter = new QuickAdapter<Found>(this, R.layout.item_list) {
                @Override
                protected void convert(BaseAdapterHelper helper, Found found) {
                    helper.setText(tv_title, found.getTitle())
                            .setText(tv_describe, found.getDescribe())
                            .setText(tv_time, found.getCreatedAt())
                            .setText(tv_photo, found.getPhone());
                }
            };
        }

        if (from.equals("Lost")) {
            Log.i(TAG, from);
            listview.setAdapter(LostAdapter);
            queryLosts();
        } else {
            Log.i(TAG, from);
            listview.setAdapter(FoundAdapter);
            queryFounds();
        }
        dialog = new ProgressDialog(this);
        //���ý�������ʽ
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setIndeterminate(true);
        //ʧȥ�����ʱ�򣬲���ȥ�Ի���
        dialog.setCancelable(false);
        dialog.setTitle("����ɾ��");
        dialog.setMax(PROGRESS_MAX);
    }

    public void initToolbar() {
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("");
        this.setSupportActionBar(toolbar);
    }

    /**
     * ��ѯȫ��ʧ����Ϣ queryLosts
     *
     * @return void
     * @throws
     */
    private void queryLosts() {
        showView();
        BmobQuery<Lost> query = new BmobQuery<Lost>();
        BmobUser user = BmobUser.getCurrentUser(User.class);
        query.addWhereEqualTo("author", user);
        query.order("-updatedAt");// �����޸�ʱ������
        query.findObjects(new FindListener<Lost>() {
            @Override
            public void done(List<Lost> losts, BmobException e) {
                if(e==null){
                    LostAdapter.clear();
                    FoundAdapter.clear();
                    if (losts == null || losts.size() == 0) {
                        showErrorView(0);
                        LostAdapter.notifyDataSetChanged();
                        return;
                    }
                    progress.setVisibility(View.GONE);
                    LostAdapter.addAll(losts);
                    listview.setAdapter(LostAdapter);
                    Log.i(TAG, losts.size()+"");
                }
                else{
                    Log.i(TAG, "queryLostʧ��");
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
        query.order("-updatedAt");// �����޸�ʱ������
        query.findObjects(new FindListener<Found>() {

            @Override
            public void done(List<Found> founds, BmobException e) {
                if(e==null){
                    LostAdapter.clear();
                    FoundAdapter.clear();
                    if (founds == null || founds.size() == 0) {
                        showErrorView(1);
                        FoundAdapter.notifyDataSetChanged();
                        return;
                    }
                    FoundAdapter.addAll(founds);
                    listview.setAdapter(FoundAdapter);
                    progress.setVisibility(View.GONE);
                    Log.i(TAG, founds.size()+"");
                }
                else{
                    showErrorView(2);
                }
            }
        });
    }

    /**
     * ����������������ʱ����ʾ�Ľ��� showErrorView
     *
     * @return void
     * @throws
     */
    private void showErrorView(int tag) {
        progress.setVisibility(View.GONE);
        listview.setVisibility(View.GONE);
        layout_no.setVisibility(View.VISIBLE);
        if (tag == 0) {
            tv_no.setText(getResources().getText(R.string.list_no_data_lost));
        } else if(tag == 1){
            tv_no.setText(getResources().getText(R.string.list_no_data_found));
        }else {
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
        if (from.equals("Lost")) {
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
        intent.putExtra("objectId",objectId);
        startActivityForResult(intent, Constants.REQUESTCODE_ADD);
    }

    @Override
    public void onDelete(View v) {
        // TODO Auto-generated method stub
        if (from.equals("Lost")) {
            deleteLost();
        } else {
            deleteFound();
        }
    }

    private void deleteLost() {
        String objectId = LostAdapter.getItem(position).getObjectId();
        BmobQuery<Lost> query = new BmobQuery<Lost>();
        //��ʾ�Ի���
        dialog.show();
        //����objectId���ҳ�������¼���Ӷ���ȡphotoUrl
        query.getObject(objectId, new QueryListener<Lost>() {

            @Override
            public void done(Lost object, BmobException e) {
                if(e==null){
                    BmobFile file = new BmobFile();
                    Log.i(TAG, object.getTitle() + ":"+ object.getPhotoUrl());
                    file.setUrl(object.getPhotoUrl());
                    //����urlɾ���ļ�
                    file.delete(new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if(e==null){
                                Lost lost = new Lost();
                                lost.setObjectId(LostAdapter.getItem(position).getObjectId());
                                //ɾ����Ӧ�ļ�¼
                                lost.delete(new UpdateListener() {
                                    @Override
                                    public void done(BmobException e) {
                                        if(e == null){
                                            LostAdapter.remove(position);
                                            ShowToast("ɾ���ɹ�");
                                            dialog.dismiss();
                                        }
                                        else
                                            ShowToast("ɾ��ʧ��");
                                    }
                                });
                            }
                            else
                                ShowToast("ɾ��ʧ��");
                        }
                    });
                }else{
                    ShowToast("ɾ��ʧ��");
                }
            }

        });
    }

    private void deleteFound() {
        String objectId = FoundAdapter.getItem(position).getObjectId();
        BmobQuery<Found> query = new BmobQuery<Found>();
        //��ʾ�Ի���
        dialog.show();
        //����objectId���ҳ�������¼���Ӷ���ȡphotoUrl
        query.getObject(objectId, new QueryListener<Found>() {

            @Override
            public void done(Found object, BmobException e) {
                if(e==null){
                    BmobFile file = new BmobFile();
                    Log.i(TAG, object.getTitle() + ":"+ object.getPhotoUrl());
                    file.setUrl(object.getPhotoUrl());
                    //����urlɾ���ļ�
                    file.delete(new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if(e==null){
                                Found found = new Found();
                                found.setObjectId(FoundAdapter.getItem(position).getObjectId());
                                //ɾ����Ӧ�ļ�¼
                                found.delete(new UpdateListener() {
                                    @Override
                                    public void done(BmobException e) {
                                        if(e == null){
                                            FoundAdapter.remove(position);
                                            ShowToast("ɾ���ɹ�");
                                            dialog.dismiss();
                                        }
                                        else
                                            ShowToast("ɾ��ʧ��");
                                    }
                                });
                            }
                            else
                                ShowToast("ɾ��ʧ��");
                        }
                    });
                }else{
                    ShowToast("ɾ��ʧ��");
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
            case Constants.REQUESTCODE_ADD:// ��ӳɹ�֮��Ļص�
                String tag = from;
                if (tag.equals("Lost")) {
                    queryLosts();
                } else {
                    queryFounds();
                }
                break;
        }
    }

    //���ó���ʽ״̬���͵�����
    private void initWindow(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintColor(getResources().getColor(R.color.colorPrimary));
            tintManager.setStatusBarTintEnabled(true);
        }
    }
}
