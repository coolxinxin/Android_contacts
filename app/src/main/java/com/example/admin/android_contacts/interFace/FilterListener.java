package com.example.admin.android_contacts.interFace;


import com.example.admin.android_contacts.bean.SortModel;

import java.util.List;

/**
 * Created by ${LEO} on 2018/12/19.
 */

public interface FilterListener {

    /**
     * 接口类，抽象方法用来获取过滤后的数据
     *
     * @author Leo
     */
    void setFilterData(List<SortModel> list);// 获取过滤后的数据
}
