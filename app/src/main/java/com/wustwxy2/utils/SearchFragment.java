package com.wustwxy2.utils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.wustwxy2.R;
import com.wustwxy2.adapter.AdAdapter;
import com.wustwxy2.adapter.MyGridAdapter;
import com.wustwxy2.models.AdDomain;
import com.wustwxy2.models.MyGridView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by fubicheng on 2016/7/12.
 */
public class SearchFragment extends Fragment{

    private static final String TAG = "SearchFragment";
    Toolbar toolbar;
    //TabLayout tabLayout;

    public static String IMAGE_CACHE_PATH = "imageloader/Cache"; // ͼƬ����·��

    private ViewPager adViewPager;
    private List<ImageView> imageViews;// ������ͼƬ����

    private List<View> dots; // ͼƬ�������ĵ���Щ��
    private List<View> dotList;

    private int currentItem = 0; // ��ǰͼƬ��������
    // ��������ָʾ��
    private View dot0;
    private View dot1;
    private View dot2;
    private View dot3;
    private View dot4;

    private ScheduledExecutorService scheduledExecutorService;

    // �첽����ͼƬ
    private ImageLoader mImageLoader;
    private DisplayImageOptions options;

    // �ֲ�banner������
    private List<AdDomain> adList;

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            adViewPager.setCurrentItem(currentItem);
        }
    };

    //9����ť����
    private MyGridView gridview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        initToolbar();
        initGridView(view);
        //initTableLayout();
        // ʹ��ImageLoader֮ǰ��ʼ��
        initImageLoader();
        // ��ȡͼƬ����ʵ��
        mImageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showStubImage(R.mipmap.top_banner_android)
                .showImageForEmptyUri(R.mipmap.top_banner_android)
                .showImageOnFail(R.mipmap.top_banner_android)
                .cacheInMemory(true).cacheOnDisc(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY).build();
        initAdData(view);
        startAd();
        initGridView(view);
        return view;
    }

    public void initToolbar()
    {
        toolbar = (Toolbar)getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("��Ϣ��ѯ");
        setHasOptionsMenu(true);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
    }

    /*public void initTableLayout() {
        tabLayout = (TabLayout)getActivity().findViewById(R.id.sliding_tabs);
        tabLayout.setBackgroundColor(getResources().getColor(R.color.titleLightBlue));
    }*/

    private void initGridView(View view) {
        gridview=(MyGridView) view.findViewById(R.id.gridview);
        gridview.setAdapter(new MyGridAdapter(getActivity()));
        //���Ƶ�������ת
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:{
                        startActivity(new Intent(getActivity(),SearchLibActivity.class));
                    }break;
                    case 1:{
                        startActivity(new Intent(getActivity(),SearchTableActivity.class));
                    }break;
                    case 2:{
                        startActivity(new Intent(getActivity(),SearchGradeActivity.class));
                    }break;
                    case 3:{
                        startActivity(new Intent(getActivity(),SearchCardActivity.class));
                    }break;
                    case 4:{
                        startActivity(new Intent(getActivity(),SearchLosingActivity.class));
                    }break;
                    case 5:{
                        startActivity(new Intent(getActivity(),SearchBusActivity.class));
                    }break;
                    case 6:{
                        startActivity(new Intent(getActivity(),SearchMapActivity.class));
                    }break;
                    case 7:{
                        startActivity(new Intent(getActivity(),SearchComputerActivity.class));
                    }break;
                    case 8:{
                        startActivity(new Intent(getActivity(),SearchEngActivity.class));
                    }break;
                }
            }
        });
    }

    //��ʼ��ͼƬ�����������õĿ�Դ��ͼƬ���ؿ��
    private void initImageLoader() {
        File cacheDir = com.nostra13.universalimageloader.utils.StorageUtils
                .getOwnCacheDirectory(getActivity().getApplicationContext(),
                        IMAGE_CACHE_PATH);

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true).cacheOnDisc(true).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getActivity()).defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new LruMemoryCache(12 * 1024 * 1024))
                .memoryCacheSize(12 * 1024 * 1024)
                .discCacheSize(32 * 1024 * 1024).discCacheFileCount(100)
                .discCache(new UnlimitedDiscCache(cacheDir))
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .tasksProcessingOrder(QueueProcessingType.LIFO).build();

        ImageLoader.getInstance().init(config);
    }

    private void initAdData(View view) {
        // �������
        adList = getBannerAd();

        imageViews = new ArrayList<ImageView>();

        // ��
        dots = new ArrayList<View>();
        dotList = new ArrayList<View>();
        dot0 = view.findViewById(R.id.v_dot0);
        dot1 = view.findViewById(R.id.v_dot1);
        dot2 = view.findViewById(R.id.v_dot2);
        dot3 = view.findViewById(R.id.v_dot3);
        dot4 = view.findViewById(R.id.v_dot4);
        dots.add(dot0);
        dots.add(dot1);
        dots.add(dot2);
        dots.add(dot3);
        dots.add(dot4);

        adViewPager = (ViewPager) view.findViewById(R.id.vp);
        adViewPager.setAdapter(new AdAdapter(adList,imageViews));// �������ViewPagerҳ���������
        // ����һ������������ViewPager�е�ҳ��ı�ʱ����
        adViewPager.setOnPageChangeListener(new MyPageChangeListener());
        addDynamicView();
    }

    private void addDynamicView() {
        // ��̬���ͼƬ������ָʾ��Բ��
        // ��ʼ��ͼƬ��Դ
        for (int i = 0; i < adList.size(); i++) {
            ImageView imageView = new ImageView(getActivity());
            // �첽����ͼƬ
            mImageLoader.displayImage(adList.get(i).getImgUrl(), imageView,
                    options);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageViews.add(imageView);
            dots.get(i).setVisibility(View.VISIBLE);
            dotList.add(dots.get(i));
        }
    }

    private void startAd() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        // ��Activity��ʾ������ÿ�����л�һ��ͼƬ��ʾ
        scheduledExecutorService.scheduleAtFixedRate(new ScrollTask(), 1, 5,
                TimeUnit.SECONDS);
    }

    /**
     * �ֲ��㲥ģ������
     *
     * @return
     */
    public static List<AdDomain> getBannerAd() {
        List<AdDomain> adList = new ArrayList<AdDomain>();

        AdDomain adDomain = new AdDomain();
        adDomain.setId("108078");
        adDomain.setImgUrl("http://g.hiphotos.baidu.com/image/w%3D310/sign=bb99d6add2c8a786be2a4c0f5708c9c7/d50735fae6cd7b8900d74cd40c2442a7d9330e29.jpg");
        adDomain.setAd(false);
        adList.add(adDomain);

        AdDomain adDomain2 = new AdDomain();
        adDomain2.setId("108078");
        adDomain2.setImgUrl("http://g.hiphotos.baidu.com/image/w%3D310/sign=7cbcd7da78f40ad115e4c1e2672e1151/eaf81a4c510fd9f9a1edb58b262dd42a2934a45e.jpg");
        adDomain2.setAd(false);
        adList.add(adDomain2);

        AdDomain adDomain3 = new AdDomain();
        adDomain3.setId("108078");
        adDomain3.setImgUrl("http://e.hiphotos.baidu.com/image/w%3D310/sign=392ce7f779899e51788e3c1572a6d990/8718367adab44aed22a58aeeb11c8701a08bfbd4.jpg");
        adDomain3.setAd(false);
        adList.add(adDomain3);

        AdDomain adDomain4 = new AdDomain();
        adDomain4.setId("108078");
        adDomain4.setImgUrl("http://d.hiphotos.baidu.com/image/w%3D310/sign=54884c82b78f8c54e3d3c32e0a282dee/a686c9177f3e670932e4cf9338c79f3df9dc55f2.jpg");
        adDomain4.setAd(false);
        adList.add(adDomain4);

        AdDomain adDomain5 = new AdDomain();
        adDomain5.setId("108078");
        adDomain5.setImgUrl("http://e.hiphotos.baidu.com/image/w%3D310/sign=66270b4fe8c4b7453494b117fffd1e78/0bd162d9f2d3572c7dad11ba8913632762d0c30d.jpg");
        adDomain5.setAd(true); // �����ǹ��
        adList.add(adDomain5);

        return adList;
    }

    private class ScrollTask implements Runnable {

        @Override
        public void run() {
            synchronized (adViewPager) {
                currentItem = (currentItem + 1) % imageViews.size();
                handler.obtainMessage().sendToTarget();
            }
        }
    }

    //����ͼƬ���л�
    private class MyPageChangeListener implements ViewPager.OnPageChangeListener {

        private int oldPosition = 0;

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int position) {
            currentItem = position;
            AdDomain adDomain = adList.get(position);
            dots.get(oldPosition).setBackgroundResource(R.drawable.dot_normal);
            dots.get(position).setBackgroundResource(R.drawable.dot_focused);
            oldPosition = position;
        }
    }

    //���´����˵�ʱ�ı�TableLayout��ToolBar
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionsMenu()");
        menu.clear();
        toolbar.setTitle("��Ϣ��ѯ");
        /*toolbar.setBackgroundColor(getResources().getColor(R.color.titleLightBlue));
        tabLayout.setBackgroundColor(getResources().getColor(R.color.titleLightBlue));*/
        inflater.inflate(R.menu.mainmenu, menu);
    }

    @Override
    public void onStop() {
        super.onStop();
        // ��Activity���ɼ���ʱ��ֹͣ�л�
        scheduledExecutorService.shutdown();
    }
}
