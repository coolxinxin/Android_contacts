package com.example.admin.android_contacts.activity;

import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.admin.android_contacts.app.MyApplication;
import com.example.admin.android_contacts.R;
import com.example.admin.android_contacts.adapter.ContactsSortAdapter;
import com.example.admin.android_contacts.bean.SortModel;
import com.example.admin.android_contacts.interFace.FilterListener;
import com.example.admin.android_contacts.utils.ContactsTools;
import com.example.admin.android_contacts.view.SearchView;
import com.example.admin.android_contacts.view.SliderView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ContactsSortAdapter adapter;
    private SearchView etSearch;
    private LinearLayout llAdd;
    private ListView lvContacts;
    private TextView dialog;
    private SliderView sidebar;
    private SwipeRefreshLayout srlAddress;
    private UpDataBooks upDataBooks;
    private MyAsyncQueryHandler mQueryHandler;
    private List<SortModel> mContactList = new ArrayList<>();
    private static final String PHONE_BOOK_LABLE = "phonebook_label";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        init();
        initEvent();
        loadData();
    }

    private void findViews() {
        etSearch = findViewById(R.id.et_search);
        llAdd = findViewById(R.id.ll_add);
        lvContacts = findViewById(R.id.lv_contacts);
        dialog = findViewById(R.id.dialog);
        sidebar = findViewById(R.id.sidebar);
        srlAddress = findViewById(R.id.srl_address);
    }

    private void init() {
        adapter = new ContactsSortAdapter(this, mContactList, new FilterListener() {
            @Override
            public void setFilterData(List<SortModel> list) {
                setItemClick(list);
            }
        });
        lvContacts.setAdapter(adapter);
        sidebar.setTextView(dialog);
        upDataBooks = new UpDataBooks();
        IntentFilter intentFilter = new IntentFilter("com.sasin.updatabooks");
        registerReceiver(upDataBooks, intentFilter);
    }

    private void initEvent() {
        //添加联系人
        llAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddContact.class);
                intent.putExtra("AddOrEditor", "add");
                startActivity(intent);
            }
        });

        sidebar.setOnTouchingLetterChangedListener(new SliderView.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    lvContacts.setSelection(position);
                }
            }
        });
        srlAddress.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateContactsInfo();
                srlAddress.setRefreshing(false);
            }
        });
        getContactsClick(mContactList);
    }

    /**
     * 给listView添加item的单击事件
     *
     * @param filter_lists 过滤后的数据集
     */
    protected void setItemClick(final List<SortModel> filter_lists) {
        getContactsClick(filter_lists);
    }

    private void getContactsClick(final List<SortModel> filter_lists) {
        lvContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
                MyApplication.sortModel = filter_lists.get(position);
                Intent intent = new Intent(MainActivity.this, ContactConfig.class);
                SortModel contact =  adapter.getItem(position);
                intent.putExtra("raw_contact_id", contact.getRawId());
                startActivity(intent);
                etSearch.getText().clear();
            }
        });
    }

    private void loadData() {
        mQueryHandler = new MyAsyncQueryHandler(getContentResolver());
        updateContactsInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        etSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // 如果adapter不为空的话就根据编辑框中的内容来过滤数据
                if (adapter != null) {
                    adapter.getFilter().filter(arg0);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void afterTextChanged(Editable e) {

            }

        });
    }

    long startTime;

    /**
     * 更新联系人信息
     */
    private void updateContactsInfo() {
        startTime = System.currentTimeMillis();
        Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人Uri；
        // 查询的字段
        String[] phoneProjection = {
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,//联系人ID
                ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,//姓名
                ContactsContract.CommonDataKinds.Phone.DATA,//具体数据项
                PHONE_BOOK_LABLE,//首字母
                ContactsContract.CommonDataKinds.Phone.PHOTO_ID//图像ID
        };
        // 按照sort_key升序查詢
        mQueryHandler.startQuery(0, null, phoneUri, phoneProjection, null, null,
                ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY);
    }

    public class MyAsyncQueryHandler extends AsyncQueryHandler {
        public MyAsyncQueryHandler(ContentResolver resolver) {
            super(resolver);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            super.onQueryComplete(token, cookie, cursor);
            mContactList.clear();
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    int contactId = cursor.getInt(0);
                    int rawId = cursor.getInt(1);
                    String name = cursor.getString(2);
                    String number = cursor.getString(3);
                    String sortKey = cursor.getString(4);
                    long photoId = cursor.getLong(5);
                    // 创建联系人对象
                    SortModel contact = new SortModel();
                    contact.setSort_id(contactId);
                    contact.setRawId(rawId);
                    contact.setName(name);
                    contact.setNumber(number);
                    contact.setSortKey(sortKey);
                    contact.setPhotoId(photoId);
                    contact.setSortLetters(sortKey);
                    mContactList.add(contact);
                    if (photoId != 0) {
                        Bitmap bitmap = ContactsTools.getPhoto(contactId, MainActivity.this);
                        contact.setBtHead(bitmap);
                    }
                }
            }
            adapter.notifyDataSetChanged();
            adapter.updateListView(mContactList);
            Log.d(TAG, "loadContacts time = " + (System.currentTimeMillis() - startTime));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(upDataBooks);
    }

    //注册一个广播，插入数据的时候更新
    class UpDataBooks extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateContactsInfo();
        }
    }
}
