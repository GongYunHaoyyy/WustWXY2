package com.wustwxy2.utils;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.Window;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.wustwxy2.R;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;                             //����toolbar
    private MainFragment mf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initFragment(savedInstanceState);
        initToolbar();
        initWindow();
    }

    public void initToolbar()
    {
        toolbar = (Toolbar)this.findViewById(R.id.toolbar);
        toolbar.setTitle("������Ѷ");                     // �������������setSupportActionBar֮ǰ����Ȼ����Ч
        setSupportActionBar(toolbar);
    }

    /**
     * Ϊҳ����س�ʼ״̬��fragment
     */
    public void initFragment(Bundle savedInstanceState)
    {
        //�ж�activity�Ƿ��ؽ���������ǣ�����Ҫ���½���fragment.
        if(savedInstanceState==null) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            mf = new MainFragment();
            ft.replace(R.id.frame_main, mf).commit();
        }
    }

    //���ó���ʽ״̬������֪ͨ����Ϊ��ɫ
    private void initWindow(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
        }

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(R.color.colorPrimary);
    }

    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }


}
