package com.haidousm.tech_news_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.haidousm.tech_news_app.models.Article;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "TechNewsApp.db";
    public static final String ARTICLES_TABLE_NAME = "articles";
    public static final String ARTICLES_COLUMN_ID = "id";
    public static final String ARTICLES_COLUMN_TITLE = "title";
    public static final String ARTICLES_COLUMN_CONTENT = "content";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(String.format(
                "CREATE TABLE %s (%s INTEGER primary key, %s TEXT, %s TEXT)",
                ARTICLES_TABLE_NAME, ARTICLES_COLUMN_ID, ARTICLES_COLUMN_TITLE, ARTICLES_COLUMN_CONTENT));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", ARTICLES_TABLE_NAME));
        onCreate(db);
    }

    public boolean insertArticle(Article article) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ARTICLES_COLUMN_ID, article.getID());
        contentValues.put(ARTICLES_COLUMN_TITLE, article.getTitle());
        contentValues.put(ARTICLES_COLUMN_CONTENT, article.getContent());
        return db.insert(ARTICLES_TABLE_NAME, null, contentValues) >= 0;
    }

    public List<Article> getAllArticles() {
        List<Article> articles = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(String.format("SELECT * FROM %s", ARTICLES_TABLE_NAME), null);
        if (cursor.moveToFirst()) {
            do {
                articles.add(new Article(cursor.getLong(0), cursor.getString(1), cursor.getString(2)));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return articles;
    }


}
