package com.example.admin.android_contacts.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.admin.android_contacts.R;
import com.example.admin.android_contacts.bean.SortModel;
import com.example.admin.android_contacts.interFace.FilterListener;
import com.example.admin.android_contacts.utils.PinyinUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsSortAdapter extends BaseAdapter implements SectionIndexer, Filterable {
    private List<SortModel> mList;
    private Context mContext;
    private MyFilter filter = null;// 创建MyFilter对象
    private FilterListener listener = null;// 接口对象

    public ContactsSortAdapter(Context mContext, List<SortModel> list, FilterListener filterListener) {
        this.mContext = mContext;
        this.listener = filterListener;
        if (list == null) {
            this.mList = new ArrayList<>();
        } else {
            this.mList = list;
        }
    }

    /**
     * 当ListView数据发生变化时,调用此方法来更新ListView
     *
     * @param list
     */
    public void updateListView(List<SortModel> list) {
        if (list == null) {
            this.mList = new ArrayList<>();
        } else {
            this.mList = list;
        }
        notifyDataSetChanged();
    }

    public int getCount() {
        if (mList != null) {
            return this.mList.size();
        } else {
            return 0;
        }
    }

    public SortModel getItem(int position) {
        return mList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup arg2) {
        ViewHolder viewHolder;
        final SortModel mContent = mList.get(position);
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.item_contact, arg2, false);
            viewHolder.tvTitle = view.findViewById(R.id.title);
            viewHolder.tvNumber = view.findViewById(R.id.number);
            viewHolder.tvLetter = view.findViewById(R.id.catalog);
            viewHolder.ivHead = view.findViewById(R.id.iv_head);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        //根据position获取分类的首字母的Char ascii值
        int section = getSectionForPosition(position);

        //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        if (position == getPositionForSection(section)) {
            viewHolder.tvLetter.setVisibility(View.VISIBLE);
            viewHolder.tvLetter.setText(mContent.sortLetters);
        } else {
            viewHolder.tvLetter.setVisibility(View.GONE);
        }
        SortModel sortModel = mList.get(position);
        if (sortModel != null) {
            viewHolder.tvTitle.setText(this.mList.get(position).getName());
            viewHolder.tvNumber.setText(this.mList.get(position).getNumber());
            if (this.mList.get(position).getBtHead() != null) {
                Glide.with(mContext).load(this.mList.get(position).getBtHead()).into(viewHolder.ivHead);
            } else {
                viewHolder.ivHead.setImageResource(R.mipmap.icon_head3);
            }
        }
        return view;
    }

    public static class ViewHolder {
        TextView tvLetter;
        TextView tvTitle;
        TextView tvNumber;
        CircleImageView ivHead;
    }

    /**
     * 根据ListView的当前位置获取分类的首字母的Char ascii值
     */
    public int getSectionForPosition(int position) {
        SortModel sortModel = mList.get(position);
        if (sortModel != null) {
            String sortLetters = sortModel.sortLetters;
            if (sortLetters != null) {
                return sortLetters.charAt(0);
            }
            return 0;
        } else {
            return 0;
        }
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    public int getPositionForSection(int section) {
        for (int i = 0; i < getCount(); i++) {
            SortModel sortModel = mList.get(i);
            if (sortModel != null) {
                String sortStr = sortModel.sortLetters;
                char firstChar = sortStr.toUpperCase(Locale.CHINESE).charAt(0);
                if (firstChar == section) {
                    return i;
                }
            }
        }

        return -1;
    }

    @Override
    public Object[] getSections() {
        return null;
    }

    /**
     * 自定义MyAdapter类实现了Filterable接口，重写了该方法
     */
    @Override
    public Filter getFilter() {
        // 如果MyFilter对象为空，那么重写创建一个
        if (filter == null) {
            filter = new MyFilter(mList);
        }
        return filter;
    }

    /**
     * 创建内部类MyFilter继承Filter类，并重写相关方法，实现数据的过滤
     *
     * @author Leo
     */
    class MyFilter extends Filter {

        // 创建集合保存原始数据
        private List<SortModel> original = new ArrayList<>();

        public MyFilter(List<SortModel> list) {
            this.original = list;
        }

        /**
         * 该方法返回搜索过滤后的数据
         */
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            // 创建FilterResults对象
            FilterResults results = new FilterResults();

            /**
             * 没有搜索内容的话就还是给results赋值原始数据的值和大小
             * 执行了搜索的话，根据搜索的规则过滤即可，最后把过滤后的数据的值和大小赋值给results
             *
             */
            if (TextUtils.isEmpty(constraint)) {
                results.values = original;
                results.count = original.size();
            } else {
                // 创建集合保存过滤后的数据
                List<SortModel> mList = new ArrayList<>();
                // 遍历原始数据集合，根据搜索的规则过滤数据
                for (SortModel s : original) {
                    if (s.getName() != null) {
                        String name = PinyinUtil.getPingYin(s.getName());
                        // 这里就是过滤规则的具体实现【规则有很多，大家可以自己决定怎么实现】
                        if (name.trim().toLowerCase().contains(constraint.toString().trim().toLowerCase()) || s.getName().trim().contains(constraint.toString().trim())
                                || s.getNumber().trim().contains(constraint.toString().trim())) {
                            // 规则匹配的话就往集合中添加该数据
                            mList.add(s);
                        }
                    }
                }
                results.values = mList;
                results.count = mList.size();
            }

            // 返回FilterResults对象
            return results;
        }

        /**
         * 该方法用来刷新用户界面，根据过滤后的数据重新展示列表
         */
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            // 获取过滤后的数据
            mList = (List<SortModel>) results.values;
            // 如果接口对象不为空，那么调用接口中的方法获取过滤后的数据，具体的实现在new这个接口的时候重写的方法里执行
            if (listener != null) {
                listener.setFilterData(mList);
            }
            // 刷新数据源显示
            notifyDataSetChanged();
        }
    }
}