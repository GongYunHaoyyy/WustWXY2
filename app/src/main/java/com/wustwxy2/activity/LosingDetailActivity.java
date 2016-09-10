package com.wustwxy2.activity;



import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.wustwxy2.R;
import com.wustwxy2.adapter.BaseAdapterHelper;
import com.wustwxy2.adapter.QuickAdapter;
import com.wustwxy2.bean.Found;
import com.wustwxy2.bean.Lost;
import com.wustwxy2.bean.User;

import org.w3c.dom.Text;

import java.io.File;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;

import static com.wustwxy2.R.id.tv_describe;
import static com.wustwxy2.R.id.tv_describe_detail;
import static com.wustwxy2.R.id.tv_photo;
import static com.wustwxy2.R.id.tv_time;
import static com.wustwxy2.R.id.tv_title;

public class LosingDetailActivity extends BaseActivity {

    private static final String TAG = "LosingDetail";

    // �첽����ͼƬ
    private ImageLoader mImageLoader;
    private DisplayImageOptions options;

    public static String IMAGE_CACHE_PATH = "imageloader/Cache"; // ͼƬ����·��

    private ProgressDialog dialog;

    Toolbar toolbar;
    private SystemBarTintManager tintManager;
    //���ڻ�ȡ����ʱ��ʾ�Ľ���
    RelativeLayout layout_progress;
    //��ȡ�����ݺ���ʾ�Ľ���
    ScrollView layout_scrollView;
    //������ʾ����
    LinearLayout layout_no;
    TextView tv_no;

    TextView tv_nickname;
    TextView tv_time;
    TextView tv_phone;
    TextView tv_title;
    TextView tv_describe;
    ImageView photo_detail;

    @Override
    public void setContentView() {
        setContentView(R.layout.activity_losing_detail);
    }

    @Override
    public void initViews() {
        layout_progress = (RelativeLayout) findViewById(R.id.progress);
        layout_scrollView = (ScrollView)findViewById(R.id.scrollView_detail);
        layout_no = (LinearLayout) findViewById(R.id.layout_no);
        tv_no = (TextView) findViewById(R.id.tv_no);

        tv_nickname = (TextView)findViewById(R.id.tv_nickname);
        tv_time = (TextView)findViewById(R.id.tv_time_detail);
        tv_phone = (TextView)findViewById(R.id.tv_phone_detail);
        tv_title = (TextView)findViewById(R.id.tv_title_detail);
        tv_describe = (TextView)findViewById(R.id.tv_describe_detail);
        //initToolbar();
        initWindow();
    }

    @Override
    public void initListeners() {

    }

    @Override
    public void initData() {
        // ʹ��ImageLoader֮ǰ��ʼ��
        initImageLoader();
        // ��ȡͼƬ����ʵ��
        mImageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showStubImage(R.mipmap.loading)
                .showImageForEmptyUri(R.mipmap.loading)
                .showImageOnFail(R.mipmap.loading)
                .cacheInMemory(true).cacheOnDisc(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY).build();

        //��ʼ���Ի���
        dialog = new ProgressDialog(this);
        //���ý�������ʽ
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setIndeterminate(true);
        //ʧȥ�����ʱ�򣬲���ȥ�Ի���
        dialog.setCancelable(false);
        dialog.setTitle("���ڼ���");
        showDetail();
    }

    private void showDetail(){
        showView();
        dialog.show();
        String objectId = getIntent().getStringExtra("objectId");
        final String time = getIntent().getStringExtra("time");
        final String phone = getIntent().getStringExtra("phone");
        final String title = getIntent().getStringExtra("title");
        final String describe = getIntent().getStringExtra("describe");
        String from = getIntent().getStringExtra("from");
        Log.i(TAG, "time:"+ time );
        Log.i(TAG, "phone:"+ phone );
        Log.i(TAG, "title��"+ title);
        Log.i(TAG, "describe:" + describe);
        if(from.equals("Lost")){
            BmobQuery<Lost> query = new BmobQuery<Lost>();
            query.getObject(objectId, new QueryListener<Lost>() {
                @Override
                public void done(Lost lost, BmobException e) {
                    if(e==null){
                        User user = lost.getAuthor();
                        String nickname = user.getNickname();
                        Log.i(TAG, "nickname:" + nickname);
                        if(nickname==null){
                            tv_nickname.setText(getResources().getText(R.string.default_name));
                        }else {
                            tv_nickname.setText(nickname);
                        }
                        tv_time.setText(time);
                        tv_phone.setText(phone);
                        tv_title.setText(title);
                        tv_describe.setText(describe);
                        String url = lost.getPhotoUrl();
                        Log.i(TAG,"photoUrl" + url);
                        if(url!=null){
                            ImageView imageView = new ImageView(LosingDetailActivity.this);
                            // �첽����ͼƬ
                            mImageLoader.displayImage( url, imageView, options);
                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        }
                        dialog.dismiss();
                    }else{
                        dialog.dismiss();
                        Log.d(TAG, e.getMessage());
                        ShowToast("��ʾ����ʧ�ܣ�������������");
                        showErrorView();
                    }
                }
            });
        }else{
            BmobQuery<Found> query = new BmobQuery<Found>();
            query.getObject(objectId, new QueryListener<Found>() {
                @Override
                public void done(Found found, BmobException e) {
                    if(e==null){
                        User user = found.getAuthor();
                        String nickname = user.getNickname();
                        if(nickname==null){
                            tv_nickname.setText(getResources().getText(R.string.default_name));
                        }else {
                            tv_nickname.setText(nickname);
                        }
                        tv_time.setText(time);
                        tv_phone.setText(phone);
                        tv_title.setText(title);
                        tv_describe.setText(describe);
                        String url = found.getPhotoUrl();
                        if(url!=null){
                            ImageView imageView = new ImageView(LosingDetailActivity.this);
                            // �첽����ͼƬ
                            mImageLoader.displayImage( url, imageView, options);
                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        }
                        dialog.dismiss();
                    }else{
                        dialog.dismiss();
                        Log.d(TAG, e.getMessage());
                        ShowToast("��ʾ����ʧ�ܣ�������������");
                        showErrorView();
                    }
                }
            });
        }

    }

    private void showView() {
        layout_scrollView.setVisibility(View.VISIBLE);
        layout_progress.setVisibility(View.GONE);
    }

    private void showErrorView() {
        layout_scrollView.setVisibility(View.GONE);
        layout_progress.setVisibility(View.GONE);
        layout_no.setVisibility(View.VISIBLE);
        tv_no.setText(getResources().getText(R.string.losing_error));
    }

    /*public void initToolbar() {
        toolbar = (Toolbar)findViewById(R.id);
        toolbar.setTitle("");
        this.setSupportActionBar(toolbar);
    }*/

    //��ʼ��ͼƬ�����������õĿ�Դ��ͼƬ���ؿ��
    private void initImageLoader() {
        File cacheDir = com.nostra13.universalimageloader.utils.StorageUtils
                .getOwnCacheDirectory(getApplicationContext(),
                        IMAGE_CACHE_PATH);

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true).cacheOnDisc(true).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                this).defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new LruMemoryCache(12 * 1024 * 1024))
                .memoryCacheSize(12 * 1024 * 1024)
                .discCacheSize(32 * 1024 * 1024).discCacheFileCount(100)
                .discCache(new UnlimitedDiscCache(cacheDir))
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .tasksProcessingOrder(QueueProcessingType.LIFO).build();

        ImageLoader.getInstance().init(config);
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
