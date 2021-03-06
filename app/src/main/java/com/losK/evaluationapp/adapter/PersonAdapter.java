package com.losK.evaluationapp.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.losK.evaluationapp.R;
import com.losK.evaluationapp.contentprovider.PersonContentProvider;
import com.losK.evaluationapp.model.Person;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PersonAdapter extends CursorAdapter {

    @BindView(R.id.name_text_view)
    protected TextView nameTextView;

    @BindView(R.id.rating_bar)
    protected RatingBar ratingBar;

    public PersonAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.person_row, parent, false);
        inflate.setLongClickable(true);
        return inflate;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        ButterKnife.bind(this, view);

        nameTextView.setText(cursor.getString(cursor.getColumnIndex(Person.NAME)));
        int id = cursor.getInt(cursor.getColumnIndex(Person.ID));
        ratingBar.setRating((float) cursor.getDouble(cursor.getColumnIndex(Person.RATING)));
        ratingBar.setTag(id);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean inNotInit) {
                if (inNotInit) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(Person.RATING, rating);
                    context.getContentResolver().update(
                            Uri.parse(PersonContentProvider.CONTENT_URI + "/" + ratingBar.getTag()),
                            contentValues,
                            null,
                            null
                    );
                }
            }
        });
    }
}
