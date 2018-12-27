package com.example.admin.android_contacts.bean;


import android.graphics.Bitmap;


/**
 * 排序模型
 */
public class SortModel  {

    public int sort_id;


    public String sortLetters;
    public String name;
    public String number;
    public String sortKey;
    public Bitmap btHead;
    public String email;
    public String address;
    public String simpleNumber;
    public String lookUp;
    public int rawId;
    public long photoId;

    @Override
    public String toString() {
        return "SortModel{" +
                "sort_id=" + sort_id +
                ", sortLetters='" + sortLetters + '\'' +
                ", name='" + name + '\'' +
                ", number='" + number + '\'' +
                ", sortKey='" + sortKey + '\'' +
                ", btHead=" + btHead +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", simpleNumber='" + simpleNumber + '\'' +
                ", lookUp='" + lookUp + '\'' +
                ", rawId=" + rawId +
                ", photoId=" + photoId +
                '}';
    }

    public int getRawId() {
        return rawId;
    }

    public void setRawId(int rawId) {
        this.rawId = rawId;
    }

    public long getPhotoId() {
        return photoId;
    }

    public void setPhotoId(long photoId) {
        this.photoId = photoId;
    }

    public String getLookUp() {
        return lookUp;
    }

    public void setLookUp(String lookUp) {
        this.lookUp = lookUp;
    }

    public int getSort_id() {
        return sort_id;
    }

    public void setSort_id(int sort_id) {
        this.sort_id = sort_id;
    }

    public String getSimpleNumber() {
        return simpleNumber;
    }

    public void setSimpleNumber(String simpleNumber) {
        this.simpleNumber = simpleNumber;
    }

    public SortModel() {
    }

    public String getSortLetters() {
        return sortLetters;

    }

    public SortModel(String name, String number, String email, String address) {
        this.name = name;
        this.number = number;
        this.email = email;
        this.address = address;
    }

    public void setSortLetters(String sortLetters) {
        this.sortLetters = sortLetters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
        if (number != null) {
            this.simpleNumber = number.replaceAll("\\-|\\s", "");
        }
    }

    public String getSortKey() {
        return sortKey;
    }

    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

    public Bitmap getBtHead() {
        return btHead;
    }

    public void setBtHead(Bitmap btHead) {
        this.btHead = btHead;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
