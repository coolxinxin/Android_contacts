# Android_contacts
读取系统2000条联系人时间大概为 1300毫秒
1.这个查询的是在显示界面的字段 剩下的字段是在联系人的详情界面去查询，这样的好处是每次都只需要去查询一个人不用查询全部
查询一个联系人剩下的那些信息还是非常快的


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
2. 将我们在主界面查询到的RawId传到联系人详情界面，以便查询剩下的所有信息


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
