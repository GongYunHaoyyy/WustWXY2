package com.wustwxy2.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.wustwxy2.R;
import com.wustwxy2.bean.Found;
import com.wustwxy2.bean.Lost;
import com.wustwxy2.i.IMainPresenter;
import com.wustwxy2.i.IMainView;
import com.wustwxy2.util.MainPresenter;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

public class EditLosingActivity extends BaseActivity implements View.OnClickListener, IMainView {

    Toolbar toolbar;
    private SystemBarTintManager tintManager;
    private IMainPresenter mMainPresenter;
    //����д�ĸ���
    EditText edit_title, edit_phone, edit_describe;
    //���ذ�ť��ȷ����ť
    Button btn_back, btn_true;
    //�����������Ի������
    private ProgressDialog dialog;
    //ToolBar�ϵı���
    TextView tv_add;

    String objectId = "";
    String from = "";

    String old_title = "";
    String old_describe = "";
    String old_phone = "";

    @Override
    public void setContentView() {
        setContentView(R.layout.activity_edit_losing);
    }

    @Override
    public void initViews() {
        tv_add = (TextView) findViewById(R.id.tv_add);
        btn_back = (Button) findViewById(R.id.btn_back);
        btn_true = (Button) findViewById(R.id.btn_true);
        edit_phone = (EditText) findViewById(R.id.losing_edit_phone);
        edit_describe = (EditText) findViewById(R.id.losing_edit_describe);
        edit_title = (EditText) findViewById(R.id.losing_edit_title);
        initWindow();
    }

    @Override
    public void initListeners() {
        btn_back.setOnClickListener(this);
        btn_true.setOnClickListener(this);
    }

    @Override
    public void initData() {
        objectId = getIntent().getStringExtra("objectId");
        from = getIntent().getStringExtra("from");
        old_title = getIntent().getStringExtra("title");
        old_phone = getIntent().getStringExtra("phone");
        old_describe = getIntent().getStringExtra("describe");

        edit_title.setText(old_title);
        edit_describe.setText(old_describe);
        edit_phone.setText(old_phone);


        if (from.equals("Lost")) {
            tv_add.setText("�޸�ʧ����Ϣ");
        } else {
            tv_add.setText("�޸�������Ϣ");
        }

        mMainPresenter = new MainPresenter(this, this);

        //���ý�����
        dialog = new ProgressDialog(this);
        //���ý�������ʽ
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setIndeterminate(true);
        //ʧȥ�����ʱ�򣬲���ȥ�Ի���
        dialog.setCancelable(false);
        dialog.setTitle("�����ύ�޸�");
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == btn_true) {
            commitChanges(objectId);
        } else if (v == btn_back) {
            finish();
        }
    }

    String title = "";
    String describe = "";
    String phone="";

    private void commitChanges(String objectId){
        dialog.show();
        title = edit_title.getText().toString();
        describe = edit_describe.getText().toString();
        phone = edit_phone.getText().toString();
        if(from.equals("Lost")){
            Lost lost = new Lost();
            lost.setTitle(title);
            lost.setDescribe(describe);
            lost.setPhone(phone);
            lost.update(objectId, new UpdateListener() {
                @Override
                public void done(BmobException e) {
                    if(e==null){
                        ShowToast("�޸ĳɹ�");
                        dialog.dismiss();
                        setResult(RESULT_OK);
                        finish();
                    }
                    else{
                        ShowToast("�޸�ʧ��");
                        dialog.dismiss();
                    }
                }
            });
        }else{
            Found found = new Found();
            found.setTitle(title);
            found.setDescribe(describe);
            found.setPhone(phone);
            found.update(objectId, new UpdateListener() {
                @Override
                public void done(BmobException e) {
                    if(e==null){
                        ShowToast("�޸ĳɹ�");
                        dialog.dismiss();
                        setResult(RESULT_OK);
                        finish();
                    }
                    else{
                        ShowToast("�޸�ʧ��");
                        dialog.dismiss();
                    }
                }
            });
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

    @Override
    public void showImageView(Bitmap bitmap, String fileName) {

    }
}
