package com.wustwxy2.models;

import android.util.SparseArray;
import android.view.View;

/**
 * Created by fubicheng on 2016/7/15.
 */

//ʹ��SparseArray�����HashMap��������ܣ���ʡ�ڴ�
//��SparseArray��������ɢ��������ҵ�ʱ�����ܻ����HashMap
//���������  http://www.open-open.com/lib/view/open1402906434918.html
public class BaseViewHolder {
    @SuppressWarnings("unchecked")
    public static <T extends View> T get(View view, int id) {
        SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
        if (viewHolder == null) {
            viewHolder = new SparseArray<View>();
            view.setTag(viewHolder);
        }
        View childView = viewHolder.get(id);
        if (childView == null) {
            childView = view.findViewById(id);
            viewHolder.put(id, childView);
        }
        return (T) childView;
    }

}