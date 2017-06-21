package com.losK.evaluationapp;

import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.losK.evaluationapp.adapter.PersonAdapter;
import com.losK.evaluationapp.contentprovider.PersonContentProvider;
import com.losK.evaluationapp.model.Person;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int READ_CONTACT_REQUEST = 343;

    private static final int PICK_CONTACT = 224;

    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    @BindView(R.id.empty_list_view)
    protected View emptyListView;

    @BindView(R.id.list)
    protected ListView list;

    private PersonAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        initList();
    }

    private void initList() {
        getLoaderManager().initLoader(0, null, this);
        list.setEmptyView(emptyListView);
        adapter = new PersonAdapter(this, null);
        list.setAdapter(adapter);
        registerForContextMenu(list);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete:
                long itemId = adapter.getItemId(info.position);
                getContentResolver().delete(Uri.parse(PersonContentProvider.CONTENT_URI +
                        "/" + itemId), null, null);
                return true;
            default:
                return super.onContextItemSelected(item);
        }

    }

    @OnClick(R.id.import_contact_button)
    public void importContactClicked() {
        if (validatePermissions()) return;
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    private boolean validatePermissions() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
                    READ_CONTACT_REQUEST);
            Log.i(getString(R.string.tag), getString(R.string.permissions_needed));
            return true;
        }
        return false;
    }

    @OnClick(R.id.import_all_contacts_button)
    public void importAllContactsClicked() {
        if (validatePermissions()) return;

        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String contactId = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cursor.getInt(cursor.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor personCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{contactId}, null);
                    if (personCursor != null) {
                        while (personCursor.moveToNext()) {
                            String phoneNo = personCursor.getString(personCursor.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER));

                            addPerson(name, phoneNo, 0.0);
                        }
                        personCursor.close();
                    }
                }
            }
            cursor.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_CONTACT_REQUEST && grantResults[0] != -1) {
            importContactClicked();
        }
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {

                    Uri contactData = data.getData();
                    Cursor cursor = getContentResolver().query(contactData, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {

                        String number = "";
                        String id = cursor.getString(cursor.getColumnIndexOrThrow
                                (ContactsContract.Contacts._ID));

                        String hasPhone = cursor.getString(cursor.getColumnIndex
                                (ContactsContract.Contacts.HAS_PHONE_NUMBER));

                        if (hasPhone.equalsIgnoreCase("1")) {
                            Cursor phones = getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                    null, null);
                            if (phones != null) {
                                phones.moveToFirst();
                                number = phones.getString(phones.getColumnIndex("data1"));
                                phones.close();
                            }
                        }
                        String name = cursor.getString(cursor.getColumnIndex
                                (ContactsContract.Contacts.DISPLAY_NAME));
                        addPerson(name, number, 0.0);
                        cursor.close();
                    }
                }
                break;
        }
    }

    private ContentValues addPerson(String name, String phoneNumber, Double rating) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Person.NAME, name);
        contentValues.put(Person.PHONE, phoneNumber);
        contentValues.put(Person.RATING, rating);
        getContentResolver().insert(PersonContentProvider.CONTENT_URI, contentValues);
        return contentValues;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, PersonContentProvider.CONTENT_URI,
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}