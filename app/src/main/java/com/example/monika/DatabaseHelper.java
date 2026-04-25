package com.example.monika;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SyramDB";
    private static final int DATABASE_VERSION = 3; // Naikkan versi untuk tambah kolom foto

    // Table Users
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PHOTO = "photo_path"; // Kolom baru untuk foto profil

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_PHOTO + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        addDefaultUsers(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_PHOTO + " TEXT");
        }
    }

    private void addDefaultUsers(SQLiteDatabase db) {
        insertUser(db, "admin@syram.com", "admin123", "Admin SYRAM");
        insertUser(db, " user@syram.com", "123456", "User SYRAM");
        insertUser(db, "heriheri666.hh@gmail.com", "123456", "Heri Khairi");
        insertUser(db, "herisadega193@gmail.com", "123456", "SADEGA Heri");
    }

    private void insertUser(SQLiteDatabase db, String email, String password, String name) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_NAME, name);
        db.insert(TABLE_USERS, null, values);
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_EMAIL + " = ?" + " AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};
        Cursor cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};
        Cursor cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public String getUserName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_NAME};
        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        String name = "User";
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }

    // Fungsi untuk update nama user
    public void updateUserName(String email, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, newName);
        db.update(TABLE_USERS, values, COLUMN_EMAIL + " = ?", newSourceArgs(email));
    }

    // Fungsi untuk update foto profil
    public void updateUserPhoto(String email, String photoPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PHOTO, photoPath);
        db.update(TABLE_USERS, values, COLUMN_EMAIL + " = ?", newSourceArgs(email));
    }

    // Fungsi untuk mengambil path foto profil
    public String getUserPhoto(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_PHOTO};
        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        String photoPath = null;
        if (cursor.moveToFirst()) {
            photoPath = cursor.getString(0);
        }
        cursor.close();
        return photoPath;
    }

    private String[] newSourceArgs(String email) {
        return new String[]{email};
    }
}
