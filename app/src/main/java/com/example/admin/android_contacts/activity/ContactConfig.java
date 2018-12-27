package com.example.admin.android_contacts.activity;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.admin.android_contacts.app.MyApplication;
import com.example.admin.android_contacts.R;
import com.example.admin.android_contacts.bean.SortModel;
import com.example.admin.android_contacts.utils.ContactsTools;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ContactConfig extends AppCompatActivity {
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.iv_head)
    CircleImageView ivHead;
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.iv_more)
    ImageView ivMore;
    @BindView(R.id.tv_del)
    TextView tvDel;
    @BindView(R.id.tv_edit)
    TextView tvEdit;
    @BindView(R.id.rl_change)
    RelativeLayout rlChange;
    @BindView(R.id.tv_number)
    TextView tvNumber;
    @BindView(R.id.ll_num)
    LinearLayout llNum;
    @BindView(R.id.tv_email)
    TextView tvEmail;
    @BindView(R.id.tv_address)
    TextView tvAddress;
    private SortModel sortModel;
    public static final String TAG = "ContactConfig";
    private int mContactId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_config);
        ButterKnife.bind(this);
        init();
        initEvent();
    }

    protected void init() {
        mContactId = getIntent().getIntExtra("raw_contact_id", 0);
        if (mContactId > 0) {
            //根据raw_contact_id查询data数据库表，得到联系人邮箱和地址
            ContentResolver contentResolver = this.getContentResolver();
            Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Email.DATA},
                    ContactsContract.CommonDataKinds.Email.RAW_CONTACT_ID + "=?", new String[]{String.valueOf(mContactId)}, null);
            while (cursor.moveToNext()) {
                String email = cursor.getString(0);
                tvEmail.setText(email);
                MyApplication.sortModel.setEmail(email);
            }
            cursor.close();
            Cursor cursor1 = contentResolver.query(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.StructuredPostal.STREET,
                            ContactsContract.CommonDataKinds.StructuredPostal.POBOX, ContactsContract.CommonDataKinds.StructuredPostal.NEIGHBORHOOD,
                            ContactsContract.CommonDataKinds.StructuredPostal.CITY, ContactsContract.CommonDataKinds.StructuredPostal.REGION, ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE},
                    ContactsContract.CommonDataKinds.StructuredPostal.RAW_CONTACT_ID + "=?", new String[]{String.valueOf(mContactId)}, null);
            while (cursor1.moveToNext()) {
                String street = cursor1.getString(0);
                String pobx = cursor1.getString(1);
                String community = cursor1.getString(2);
                String city = cursor1.getString(3);
                String region = cursor1.getString(4);
                String postCode = cursor1.getString(5);
                StringBuilder sb = new StringBuilder();
                if (street != null) {
                    sb.append(street);
                }
                if (pobx != null) {
                    sb.append(pobx);
                }
                if (community != null) {
                    sb.append(community);
                }
                if (city != null) {
                    sb.append(city);
                }
                if (region != null) {
                    sb.append(region);
                }
                if (postCode != null) {
                    sb.append(postCode);
                }
                tvAddress.setText(sb.toString());
                MyApplication.sortModel.setAddress(sb.toString());
            }
            cursor1.close();
        }
        if (MyApplication.sortModel != null) {
            sortModel = MyApplication.sortModel;
        }
        Log.d(TAG, "init: " + MyApplication.sortModel.toString());
        if (sortModel.getBtHead() != null) {
            ivHead.setImageBitmap(sortModel.getBtHead());//头像
        }
        if (!TextUtils.isEmpty(sortModel.getName())) {
            tvName.setText(sortModel.getName());//姓名
        }
        if (!TextUtils.isEmpty(sortModel.getNumber())) {
            tvNumber.setText(sortModel.getNumber());//号码
        }
    }


    protected void initEvent() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ivMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rlChange.getVisibility() == View.GONE) {
                    rlChange.setVisibility(View.VISIBLE);
                } else {
                    rlChange.setVisibility(View.GONE);
                }
            }
        });
        //删除联系人
        tvDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(ContactConfig.this)
                        .setTitle(R.string.hint)
                        .setMessage(R.string.del_contact)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ContactsTools.deleteContact(sortModel.getRawId(), ContactConfig.this);
                                Log.d(TAG, "sortModel sort_id = " + sortModel.getSort_id());
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();

            }
        });
        //编辑联系人
        tvEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ContactConfig.this, AddContact.class);
                intent.putExtra("AddOrEditor", "editor");
                startActivity(intent);
                finish();

            }
        });
    }
}
