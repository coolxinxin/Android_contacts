package com.example.admin.android_contacts.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by admin on 2018/5/22.
 */

public class ContactsTools {
    private static final String TAG = "ContactsTools";
    private static long mContactId;
    private static ContentResolver resolver;


    public ContactsTools() {
    }

    /**
     * 判断号码是否为联系人号码
     *
     * @param context
     * @param phone
     * @return
     */
    public static boolean isPhoneExists(Context context, String phone) {
        if (null != lookupNameByPhoneNumber(context, phone)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 查询号码是否存在
     *
     * @param context
     * @param phoneNumber
     * @return null 不存在
     */
    public static String lookupNameByPhoneNumber(Context context, String phoneNumber) {
        String name;
        try {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Cursor cursor = context.getContentResolver().query(uri,
                    new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
            name = null;
            if (cursor == null) {
                name = null;
            }
            try {
                if (cursor.moveToFirst()) {
                    name = cursor.getColumnName(0);
                }
            } finally {
                cursor.close();
            }
            return name;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    // 通过电话号获取联系人姓名
    public static String getContactName1(String number, Context context) {
        if (TextUtils.isEmpty(number)) {
            return null;
        }
        ContentResolver resolver = context.getContentResolver();
        Uri lookupUri = null;
        String[] projection = new String[]{ContactsContract.PhoneLookup._ID,
                ContactsContract.PhoneLookup.DISPLAY_NAME};
        Cursor cursor = null;
        try {
            lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(number));
            cursor = resolver.query(lookupUri, projection, null, null, null);
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                lookupUri = Uri.withAppendedPath(
                        android.provider.Contacts.Phones.CONTENT_FILTER_URL,
                        Uri.encode(number));
                cursor = resolver
                        .query(lookupUri, projection, null, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String ret = null;
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            ret = cursor.getString(1);
        }
        cursor.close();
        return ret;

    }


    /**
     * 通过手机号获取联系人id
     *
     * @param context
     * @param number
     * @return
     */
    public static long getContactId(Context context, String number) {
        Cursor c = null;
        try {
            c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
            if (c != null && c.moveToFirst()) {
                while (!c.isAfterLast()) {
                    if (PhoneNumberUtils.compare(number, c.getString(1))) {
                        return Long.valueOf(c.getString(0));
                    }
                    c.moveToNext();
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return 0;
    }

    /**
     * 修改联系人
     *
     * @throws Exception
     */
    public static void contactsUpdate(final Context context, final long contactId, final String name, final String telNum, final String isNullEmail, final String email, final String isNullAddress, final String address, final long isNullPhotoId, final Bitmap headBitmap) throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Uri uri = Uri.parse("content://com.android.contacts/data");//对data表的所有数据操作
                ContentResolver resolver = context.getContentResolver();
                ContentValues values = new ContentValues();
                values.put("data1", telNum);
                int phoneNumber = resolver.update(uri, values, "mimetype=? and raw_contact_id=?", new String[]{"vnd.android.cursor.item/phone_v2", contactId + ""});
                Log.d(TAG, "contactsUpdate phoneNumber = " + phoneNumber);
                values.clear();

                values.put("data1", name);
                int nameNum = resolver.update(uri, values, "mimetype=? and raw_contact_id=?", new String[]{"vnd.android.cursor.item/name", contactId + ""});
                Log.d(TAG, "contactsUpdate nameNum = " + nameNum);
                values.clear();

                Log.d(TAG, "contactsUpdate isNullEmail =" + (isNullEmail == null));
                if (isNullEmail == null) {
                    //add email
                    values.put("raw_contact_id", contactId);
                    values.put(ContactsContract.Contacts.Data.MIMETYPE, "vnd.android.cursor.item/email_v2");
                    values.put("data1", email);
                    resolver.insert(uri, values);
                    values.clear();
                } else {
                    values.put("data1", email);
                    int emailnum = resolver.update(uri, values, "mimetype=? and raw_contact_id=?", new String[]{"vnd.android.cursor.item/email_v2", contactId + ""});
                    Log.d(TAG, "contactsUpdate emailnum = " + emailnum);
                    values.clear();
                }
                Log.d(TAG, "contactsUpdate isNullAddress =" + (isNullAddress == null));
                if (isNullAddress == null) {
                    //添加地址
                    values.put("raw_contact_id", contactId);
                    values.put(ContactsContract.Contacts.Data.MIMETYPE, "vnd.android.cursor.item/postal-address_v2");
                    values.put("data1", address);
                    resolver.insert(uri, values);
                } else {
                    values.put("data1", address);
                    int addressnum = resolver.update(uri, values, "mimetype=? and raw_contact_id=?", new String[]{"vnd.android.cursor.item/postal-address_v2", contactId + ""});
                    Log.d(TAG, "contactsUpdate addressnum = " + addressnum);
                    values.clear();
                }

                if (headBitmap == null) {

                } else {
                    if (isNullPhotoId == 0) {
                        final ByteArrayOutputStream os = new ByteArrayOutputStream();
                        // 将Bitmap压缩成PNG编码，质量为100%存储
                        headBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                        byte[] avatar = os.toByteArray();
                        values.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, contactId);
                        values.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
                        values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, avatar);
                        resolver.insert(uri, values);
                    }
                    final ByteArrayOutputStream os = new ByteArrayOutputStream();
                    // 将Bitmap压缩成PNG编码，质量为100%存储
                    headBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                    byte[] avatar = os.toByteArray();
                    values.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, contactId);
                    values.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
                    values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, avatar);
                    resolver.update(uri, values, "mimetype=? and raw_contact_id=?", new String[]{ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE, contactId + ""});
                }
                Intent intent = new Intent();
                intent.setAction("com.sasin.updatabooks");
                context.sendBroadcast(intent);
            }
        }).start();
    }


    /**
     * 获取联系人头像
     *
     * @param contactId
     * @param
     * @param context
     * @return
     */
    public static Bitmap getPhoto(int contactId, Context context) {

        Uri uri = ContentUris.withAppendedId(
                ContactsContract.Contacts.CONTENT_URI, contactId);

        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(
                context.getContentResolver(), uri);
        if (input != null) {
            Bitmap photo = BitmapFactory.decodeStream(input);
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return photo;
        } else {
            Log.d(TAG, "First try failed to load photo!");

        }

        return null;
    }

    public static byte[] BitmapToBytes(Bitmap bmp) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();//初始化一个流对象
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);//把bitmap100%高质量压缩 到 output对象里
        bmp.recycle();//自由选择是否进行回收

        byte[] result = output.toByteArray();//转换成功了
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }

    /**
     * 此处应该做头像缓存处理
     * 否则每次加载比较耗时
     * 通过号码获取联系人头像
     *
     * @param
     * @param
     * @param context
     * @return
     */
    public static Bitmap getHeadPhoto(String number, Context context) {
        mContactId = getContactId(context, number);
        Uri uri = ContentUris.withAppendedId(
                ContactsContract.Contacts.CONTENT_URI, mContactId);

        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(
                context.getContentResolver(), uri);
        if (input != null) {
            Bitmap photo = BitmapFactory.decodeStream(input);
            return photo;
        } else {
            Log.d(TAG, "First try failed to load photo!");
        }
        return null;
    }

    // 删除联系人
    public static void deleteContact(final long rawContactId, final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentResolver resolver = context.getContentResolver();
                resolver.delete(ContactsContract.RawContacts.CONTENT_URI, ContactsContract.Data._ID + " =?", new String[]{rawContactId + ""});
                Intent intent = new Intent();
                intent.setAction("com.sasin.updatabooks");
                context.sendBroadcast(intent);
            }
        }).start();

    }
}
