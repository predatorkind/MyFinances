package net.vertexgraphics.myfinances;


import android.app.Dialog;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;

import java.util.Objects;

public class LogActivity extends AppCompatActivity implements View.OnClickListener
{

	UserPrefs userPrefs;
	TextView logText;
	String[] logContentLines;
	Toolbar toolbar;
	//private CustomSoftKeyboard keyboard;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.log);
		toolbar = findViewById(R.id.toolbar);
		
		toolbar.setTitle(getString(R.string.log_string));
		setSupportActionBar(toolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
		
		
		userPrefs = UserPrefs.getInstance(this);
		logText = findViewById(R.id.logText);
		logText.setMovementMethod(new ScrollingMovementMethod());
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.log_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		
		
		final Dialog dialog = new Dialog(this);
		final EditText editText;
		TextView label;
		Button accept;
		Button cancel;
		View.OnClickListener cancelListener = p1 -> dialog.dismiss();

        int itemId = item.getItemId();
        if (itemId == R.id.filter) {
            dialog.setContentView(R.layout.edit_name_dialog);
            editText = dialog.findViewById(R.id.editnamedialogEditText);
            Objects.requireNonNull(dialog.getWindow()).setGravity(Gravity.BOTTOM);

            label = dialog.findViewById(R.id.editnamedialogLabel);
            label.setText(getString(R.string.setFilter_string));
            cancel = dialog.findViewById(R.id.editnamedialogButton2);
            cancel.setOnClickListener(cancelListener);
            accept = dialog.findViewById(R.id.editnamedialogButton1);
            accept.setOnClickListener(p1 -> {
                updateLog(editText.getText().toString());

                dialog.dismiss();
            });

            dialog.setOnShowListener(p1 -> {
                editText.requestFocus();
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            });

            dialog.show();
        } else if (itemId == R.id.item_deleteLog) {
            userPrefs.deleteLog();
            updateLog("");
        } else if (itemId == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
		
		
		
		return true;
	}
	
	
	
	
	

	@Override
	protected void onResume()
	{
		
		super.onResume();
		updateLog("");
	}
	
	

	private void updateLog(String filter){
		
		logContentLines = userPrefs.readLog(filter);
		logText.setText("");

		for(String s: logContentLines){
			if(!s.isEmpty()){
				logText.append(s+ "\n");
			}
		}
	}


	@Override
	public void onClick(View view) {
		//TODO
	}
}
