package com.example.admin.android_contacts.activity;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.android_contacts.app.MyApplication;
import com.example.admin.android_contacts.R;
import com.example.admin.android_contacts.utils.ContactsTools;
import com.linchaolong.android.imagepicker.ImagePicker;
import com.linchaolong.android.imagepicker.cropper.CropImage;
import com.linchaolong.android.imagepicker.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddContact extends AppCompatActivity {

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.iv_head)
    ImageView ivHead;
    @BindView(R.id.ll_head)
    LinearLayout llHead;
    @BindView(R.id.et_name)
    EditText etName;
    @BindView(R.id.et_number)
    EditText etNumber;
    @BindView(R.id.et_email)
    EditText etEmail;
    @BindView(R.id.et_address)
    EditText etAddress;
    @BindView(R.id.tv_finish)
    TextView tvFinish;
    @BindView(R.id.ll_parent)
    LinearLayout llParent;
    private String addOrEditor;
    private ImagePicker imagePicker;
    private Bitmap headBitmap;
    public static final String TAG = "AddContact";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        ButterKnife.bind(this);
        init();
        initEvent();
    }

    private void init() {
        Intent intent = getIntent();
        addOrEditor = intent.getStringExtra("AddOrEditor");
        if (addOrEditor.equals("editor")) {//editor编辑联系人进入
            if (MyApplication.sortModel != null) {
                if (MyApplication.sortModel.getBtHead() != null) {
                    ivHead.setImageBitmap(MyApplication.sortModel.getBtHead());
                }
                if (MyApplication.sortModel.getName() != null) {
                    etName.setText(MyApplication.sortModel.getName());
                }
                if (MyApplication.sortModel.getNumber() != null) {
                    etNumber.setText(MyApplication.sortModel.getNumber());
                }
                if (MyApplication.sortModel.getEmail() != null) {
                    etEmail.setText(MyApplication.sortModel.getEmail());
                }
                if (MyApplication.sortModel.getAddress() != null) {
                    etAddress.setText(MyApplication.sortModel.getAddress());
                }
            }
        }
    }

    protected void initEvent() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        llHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagePicker = new ImagePicker();
                // 设置标题
                imagePicker.setTitle(getString(R.string.setHead));
                // 设置是否裁剪图片
                imagePicker.setCropImage(true);
                // 启动图片选择器
                imagePicker.startChooser(AddContact.this, new ImagePicker.Callback() {
                    // 选择图片回调
                    @Override
                    public void onPickImage(Uri imageUri) {

                    }

                    // 裁剪图片回调
                    @Override
                    public void onCropImage(Uri imageUri) {
                        ivHead.setImageURI(imageUri);
                        try {
                            headBitmap = MediaStore.Images.Media.getBitmap(AddContact.this.getContentResolver(), imageUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    // 自定义裁剪配置
                    @Override
                    public void cropConfig(CropImage.ActivityBuilder builder) {
                        builder
                                // 是否启动多点触摸
                                .setMultiTouchEnabled(false)
                                // 设置网格显示模式
                                .setGuidelines(CropImageView.Guidelines.OFF)
                                // 圆形/矩形
                                .setCropShape(CropImageView.CropShape.OVAL)
                                // 调整裁剪后的图片最终大小
                                .setRequestedSize(400, 400)
                                // 宽高比
                                .setAspectRatio(9, 9);
                    }

                    // 用户拒绝授权回调
                    @Override
                    public void onPermissionDenied(int requestCode, String[] permissions,
                                                   int[] grantResults) {
                    }
                });
            }
        });
        tvFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取数据
                final String name = etName.getText().toString();
                final String telNum = etNumber.getText().toString();
                final String address = etAddress.getText().toString();
                final String email = etEmail.getText().toString();
                Log.d(TAG, name + "-Add Contact-" + telNum);
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(telNum)) {
                    Toast.makeText(AddContact.this, R.string.nameOrTelnumNotNull, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (addOrEditor.equals("editor")) {//从编辑联系人进入则是更新，其他的则是添加
                    try {
                        Log.d(TAG, "Update the contact ID =" + MyApplication.sortModel.getSort_id());
                        ContactsTools.contactsUpdate(AddContact.this, MyApplication.sortModel.getRawId(), name, telNum, MyApplication.sortModel.getEmail(), email, MyApplication.sortModel.getAddress(), address, MyApplication.sortModel.getPhotoId(), headBitmap);
                        Log.d(TAG, "sortModel sort_id = " + MyApplication.sortModel.getSort_id());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //插入数据
                    //插入raw_contacts表，并获取_id属性
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
                            ContentResolver resolver = getContentResolver();
                            ContentValues values = new ContentValues();
                            long contact_id = ContentUris.parseId(resolver.insert(uri, values));
                            //插入data表
                            uri = Uri.parse("content://com.android.contacts/data");
                            //add Name
                            values.put("raw_contact_id", contact_id);
                            values.put(ContactsContract.Contacts.Data.MIMETYPE, "vnd.android.cursor.item/name");
                            values.put("data1", name);
                            resolver.insert(uri, values);
                            values.clear();
                            //add Phone
                            values.put("raw_contact_id", contact_id);
                            values.put(ContactsContract.Contacts.Data.MIMETYPE, "vnd.android.cursor.item/phone_v2");
                            values.put("data1", telNum);
                            resolver.insert(uri, values);
                            values.clear();
                            //add email
                            if (!email.equals("")) {
                                Log.d(TAG, "email = " + email);
                                values.put("raw_contact_id", contact_id);
                                values.put(ContactsContract.Contacts.Data.MIMETYPE, "vnd.android.cursor.item/email_v2");
                                values.put("data1", email);
                                resolver.insert(uri, values);
                                values.clear();
                            }
                            if (!address.equals("")) {
                                Log.d(TAG, "address = " + address);
                                values.put("raw_contact_id", contact_id);
                                values.put(ContactsContract.Contacts.Data.MIMETYPE, "vnd.android.cursor.item/postal-address_v2");
                                values.put("data1", address);
                                resolver.insert(uri, values);
                            }
                            //添加地址

                            //添加联系人的头像
                            if (headBitmap == null) {
                                headBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_head4);
                            }
                            final ByteArrayOutputStream os = new ByteArrayOutputStream();
                            // 将Bitmap压缩成PNG编码，质量为100%存储
                            headBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                            byte[] avatar = os.toByteArray();
                            values.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, contact_id);
                            values.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
                            values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, avatar);
                            resolver.insert(uri, values);
                            Intent intent = new Intent();
                            intent.setAction("com.sasin.updatabooks");//更新电话本
                            sendBroadcast(intent);
                        }
                    }).start();
                }
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imagePicker.onActivityResult(AddContact.this, requestCode, resultCode, data);
    }
}
