package rmoka.msgstore;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/*
    This class represents the main activity, in which user can enter the messages and they are enlisted below
    the edit text view and save button.
 */
public class MainActivity extends Activity {

    private List<String> messages = new ArrayList<String>();
    private DBHelper mDbHelper;
    private ArrayAdapter<String> msgsAdapter;
    private int longPressSelectPosition;
    private ListView list;
    private EditMsgReceiver editMsgReceiver;


    @Override
    /*
        Called when the app is opened.
        Initializing all the properties.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDbHelper = new DBHelper(this);
        mDbHelper.onCreate(mDbHelper.getWritableDatabase());

        //Following receiver listens to broadcast messages whose action is "rmoka.msgstore.MODIFIED"
        //This message is sent by EditActivity, activity in which message is edited.
        editMsgReceiver = new EditMsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("rmoka.msgstore.MODIFIED");
        registerReceiver(editMsgReceiver, intentFilter);

        //Adding onClickListener to Button. It saves the message in editText field to the list.
        Button saveBtn = (Button) findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(saveBtnOnClickListener);

        //To list view, on clicking item, it should be copied to clipboard.
        //On long press, context menu should pop-up, asking to edit/delete.
        //ArrayAdapter takes care of the items in the list.
        list = (ListView) findViewById(R.id.list);
        list.setOnItemClickListener(itemClickListener);
        registerForContextMenu(list);
        msgsAdapter = new ArrayAdapter<String>(this, R.layout.row_element, messages);
        list.setAdapter(msgsAdapter);
        retrieveData();
    }

    @Override
    /*
        Inflates the menu, showing options edit or delete.
        Also, saves the position of item in the list.
     */
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (R.id.list == v.getId()) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            longPressSelectPosition = info.position;
            menu.setHeaderTitle("Edit/Delete Message");
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.edit_save_menu, menu);
        }
    }

    @Override
    /*
        Starts EditActivity Intent, if edit is selected. Else, deletes the message.
     */
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.edit:
                Intent editIntent = new Intent(MainActivity.this, EditActivity.class);
                editIntent.putExtra("originalMessage", msgsAdapter.getItem(longPressSelectPosition));
                MainActivity.this.startActivity(editIntent);
                return true;
            case R.id.delete:
                msgsAdapter.remove(msgsAdapter.getItem(longPressSelectPosition));
                return true;
        }
        return false;
    }

    /*
        Copies the data as PrimaryClip and toasts a small message "Coped to Clipboard"
     */
    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ClipboardManager cbManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("SelectItem", msgsAdapter.getItem(position));
            cbManager.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(), "Copied to Clipboard", Toast.LENGTH_LONG).show();
        }
    };

    /*
        On clicking save button, message entered in EditText view is saved to array adapter linked to listview.
     */
    private View.OnClickListener saveBtnOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            EditText edText = (EditText) findViewById(R.id.textbox);
            msgsAdapter.add(edText.getText().toString());
            msgsAdapter.notifyDataSetChanged();
            edText.setText("", TextView.BufferType.NORMAL);
        }
    };

    private class EditMsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            /*
                Gets the modified message from EditActivity. Removes initial message and sets modified message
                in that position.
             */
            String modifiedMsg = (String) intent.getExtras().get("message");
            msgsAdapter.remove(msgsAdapter.getItem(longPressSelectPosition));
            msgsAdapter.insert(modifiedMsg, longPressSelectPosition);
            msgsAdapter.notifyDataSetChanged();
        }
    }

    /*
        This method takes care of saving the data in sqlite database,
        so that data persists on closing the app.
     */
    @Override
    protected void onPause() {
        super.onPause();
        int i = 0;
        mDbHelper.getWritableDatabase().delete(MessageDBEntry.TABLE_NAME, null, null);
        for (i = 0; i < msgsAdapter.getCount(); i++) {
            insertData(String.valueOf(i), msgsAdapter.getItem(i));
        }
    }

    /*
            Inserts data in sqlite database in the table with name "messages"
         */
    public long insertData(String position, String content) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MessageDBEntry.MSG_POSITION, position);
        values.put(MessageDBEntry.MSG_CONTENT, content);

        long newRowId;
        newRowId = db.insert(
                MessageDBEntry.TABLE_NAME,
                MessageDBEntry.MSG_POSITION,
                values);

        return newRowId;
    }

    /*
            Retrieves data from sqlite database and populates arrayadapter linked to list view.
         */
    public void retrieveData() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        try {
            int i = 0;


            String[] projection = {
                    MessageDBEntry._ID,
                    MessageDBEntry.MSG_POSITION,
                    MessageDBEntry.MSG_CONTENT,
            };


            Cursor cur = db.query(
                    MessageDBEntry.TABLE_NAME,  // The table to query
                    projection,                               // The columns to return
                    null,                                // The columns for the WHERE clause
                    null,                            // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    null                                 // The sort order
            );

            cur.moveToFirst();
            while (cur.isAfterLast() == false) {
                msgsAdapter.insert(cur.getString(2), Integer.valueOf(cur.getString(1)));
                cur.moveToNext();
            }
            cur.close();
            msgsAdapter.notifyDataSetChanged();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(editMsgReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
