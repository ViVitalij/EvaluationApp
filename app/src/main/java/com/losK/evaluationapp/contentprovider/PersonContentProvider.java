package com.losK.evaluationapp.contentprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.losK.evaluationapp.helper.PersonSQLiteHelper;
import com.losK.evaluationapp.model.Person;

public class PersonContentProvider extends ContentProvider {

    private PersonSQLiteHelper databaseHelper;

    private static final int PEOPLE = 10;

    private static final int PERSON_ID = 20;

    private static final String AUTHORITY = "com.losK.evaluationapp.contentprovider";

    private static final String BASE_PATH = Person.TABLE;

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    private static final UriMatcher uriMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, PEOPLE);
        uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", PERSON_ID);
    }

    @Override
    public boolean onCreate() {
        databaseHelper = new PersonSQLiteHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(Person.TABLE);

        int uriType = uriMatcher.match(uri);
        switch (uriType) {
            case PEOPLE:
                break;
            case PERSON_ID:
                queryBuilder.appendWhere(Person.ID + "="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        Cursor cursor = queryBuilder.query(sqLiteDatabase, projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        int uriType = uriMatcher.match(uri);
        long id;
        switch (uriType) {
            case PEOPLE:
                id = sqLiteDatabase.insert(Person.TABLE, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase sqlDB = databaseHelper.getWritableDatabase();
        int rowsDeleted;
        switch (uriType) {
            case PEOPLE:
                rowsDeleted = sqlDB.delete(Person.TABLE, selection,
                        selectionArgs);
                break;
            case PERSON_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(
                            Person.TABLE,
                            Person.ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(
                            Person.TABLE,
                            Person.ID + "=" + id
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase sqlDB = databaseHelper.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case PEOPLE:
                rowsUpdated = sqlDB.update(Person.TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case PERSON_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(Person.TABLE,
                            values,
                            Person.ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(Person.TABLE,
                            values,
                            Person.ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}