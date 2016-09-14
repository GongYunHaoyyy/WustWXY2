package com.wustwxy2.models;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.wustwxy2.R;
import com.wustwxy2.adapter.NewsAdapter;
import com.wustwxy2.util.BrowseNewsAvtivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/17.
 */
public class JWCActivity extends Fragment {
    private NewsAdapter adapter;
    private List<News> newsList;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {

            String news= (String) msg.obj;
            if(news.length()<10)//����ַ�������С�������ݹ������ǿ��ַ���������ʾ��վ�ر���Ϣ
                newsList.add(new News("������վ�ѹر�",null));
            else {
                String getnews[]=news.split(",");
                for(int i=0;i<48;i+=2)
                {
                    newsList.add(new News(getnews[i],getnews[i+1]));

                }}
            adapter.notifyDataSetChanged();
        }
    };
    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,  Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_get_news, container, false);//���������ļ�
        ListView lv = (ListView) rootView.findViewById(R.id.lvNews);
        newsList = new ArrayList<News>();
        adapter = new NewsAdapter(getActivity(), newsList);
        parseHtml(handler);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {//listview�����Ӧ�¼�
                News news = newsList.get(position);
                Intent intent = new Intent(getActivity(), BrowseNewsAvtivity.class);
                intent.putExtra("href", news.getHref());
                if(news.getHref()!=null)
                    startActivity(intent);
            }
        });


        return rootView;
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    public static void parseHtml (final Handler handler){
        final String url="http://202.114.242.231:8036/default.html";
        new Thread(new Runnable() {
            @Override
            public void run() { //ʹ���̣߳���Jsoup������ҳhtml�ļ�����ȡ���ű��������
                try {
                    String str="";

                    {
                        Document doc = Jsoup.connect(url).get();
                        Elements elements = doc.select("div .mainframe_2").select("li");//��ȡHTML�ļ���ָ��λ�õ�����
                        for (Element ele : elements) {
                            str = str+ele.getElementsByTag("li").text()+ ","+ele.getElementsByTag("a").first().attr("href")+",";
                        }
                        Message msg=new Message();
                        msg.obj=str;
                        handler.sendMessage(msg);}
                }
                catch (Exception e) {//�����ȡ������ҳ���򴫵ݿ��ַ����������߳̽��д���
                    Message msg=new Message();
                    msg.obj=" ";
                    handler.sendMessage(msg);
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
