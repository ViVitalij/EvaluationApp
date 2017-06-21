package com.losK.evaluationapp.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.losK.evaluationapp.model.Person.ID;
import static com.losK.evaluationapp.model.Person.NAME;
import static com.losK.evaluationapp.model.Person.PHONE;
import static com.losK.evaluationapp.model.Person.RATING;
import static com.losK.evaluationapp.model.Person.TABLE;

public class PersonSQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "evaluation.db";

    private static final int DATABASE_VERSION = 4;

    private static final String DATABASE_CREATE =
            "create table "
                    + TABLE + "( "
                    + ID + " integer primary key autoincrement, "
                    + NAME + " text not null,"
                    + RATING + " real not null,"
                    + PHONE + " text not null"
                    + ");";

    private static final String DATABASE_DROP = "DROP TABLE IF EXISTS " + TABLE;

    public PersonSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int j) {
        sqLiteDatabase.execSQL(DATABASE_DROP);
        onCreate(sqLiteDatabase);
    }
}
