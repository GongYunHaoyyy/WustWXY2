package com.wustwxy2.utils;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wustwxy2.adapter.FindTabAdapter;
import com.wustwxy2.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fubicheng on 2016/7/13.
 */
public class MainFragment extends Fragment {
    private TabLayout tab_FindFragment_title;           //����TabLayout
    private ViewPager vp_FindFragment_pager;            //����viewPager
    private FindTabAdapter fAdapter;                    //����adapter

    private List<Fragment> list_fragment;                //����Ҫװfragment���б�
    private List<String> list_title;                     //tab�����б�

    private NewsFragment newsFragment;                   //����fragment
    private SearchFragment searchFragment;               //��Ϣ��ѯfragment
    private MesFragment mesFragment;                     //������Ϣfragment

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        initControls(view);

        return view;
    }

    /**
     * ��ʼ�����ؼ�
     */
    private void initControls(View view) {

        tab_FindFragment_title = (TabLayout)view.findViewById(R.id.sliding_tabs);
        vp_FindFragment_pager = (ViewPager)view.findViewById(R.id.viewpager);

        //��ʼ����fragment
        newsFragment = new NewsFragment();
        searchFragment = new SearchFragment();
        mesFragment = new MesFragment();

        //��fragmentװ���б���
        list_fragment = new ArrayList<>();
        list_fragment.add(newsFragment);
        list_fragment.add(searchFragment);
        list_fragment.add(mesFragment);

        //�����Ƽ���tab�����б���������£�����Ӧ����values/arrays.xml�н��ж���Ȼ�����
        list_title = new ArrayList<>();
        list_title.add("������Ѷ");
        list_title.add("��Ϣ��ѯ");
        list_title.add("������Ϣ");

        //����TabLayout��ģʽ
        tab_FindFragment_title.setTabMode(TabLayout.MODE_FIXED);
        //ΪTabLayout���tab����
        tab_FindFragment_title.addTab(tab_FindFragment_title.newTab().setText(list_title.get(0)));
        tab_FindFragment_title.addTab(tab_FindFragment_title.newTab().setText(list_title.get(1)));
        tab_FindFragment_title.addTab(tab_FindFragment_title.newTab().setText(list_title.get(2)));


        fAdapter = new FindTabAdapter(getActivity().getSupportFragmentManager(),list_fragment,list_title);

        //viewpager����adapter
        vp_FindFragment_pager.setAdapter(fAdapter);
        //tab_FindFragment_title.setViewPager(vp_FindFragment_pager);
        //TabLayout����viewpager
        tab_FindFragment_title.setupWithViewPager(vp_FindFragment_pager);
        //tab_FindFragment_title.set
    }
}
