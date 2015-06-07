package rmoka.msgstore;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Ramakant on 6/2/2015.
 */
public class EditActivity extends Activity {

    private EditText tview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_text);

        tview = (EditText) findViewById(R.id.editTextBox);
        tview.setText(this.getIntent().getStringExtra("originalMessage"));
        Button svBtn = (Button) findViewById(R.id.edit_saveBtn);
        svBtn.setOnClickListener(svClickListener);
    }

    View.OnClickListener svClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent modifiedText = new Intent();
            modifiedText.setAction("rmoka.msgstore.MODIFIED");
            modifiedText.putExtra("message", tview.getText().toString());
            EditActivity.this.sendBroadcast(modifiedText);
            finish();
        }
    };
}
