package net.vertexgraphics.myfinances;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LogActivity extends AppCompatActivity implements View.OnClickListener
{

	UserPrefs userPrefs;
	TextView logText;
	String[] logContentLines;
	Toolbar toolbar;
	private CustomSoftKeyboard keyboard;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.log);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		
		toolbar.setTitle(getString(R.string.log_string));
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		
		userPrefs = UserPrefs.getInstance(this);
		logText = (TextView) findViewById(R.id.logText);
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
		View.OnClickListener cancelListener = new View.OnClickListener(){

			@Override
			public void onClick(View p1)
			{
				dialog.dismiss();
			}
			
			
		};
		
		switch (item.getItemId()){
			case R.id.filter:
				
				dialog.setContentView(R.layout.edit_name_dialog);
				editText = (EditText) dialog.findViewById(R.id.editnamedialogEditText);
				dialog.getWindow().setGravity(Gravity.BOTTOM);
				keyboard = new CustomSoftKeyboard(dialog, R.id.keyboard, R.layout.qwerty_keboard, false, R.id.editnamedialogEditText);
				keyboard.registerEditText(editText);
				label = (TextView) dialog.findViewById(R.id.editnamedialogLabel);
				label.setText(getString(R.string.setFilter_string));
				cancel = (Button) dialog.findViewById(R.id.editnamedialogButton2);
				cancel.setOnClickListener(cancelListener);
				accept = (Button) dialog.findViewById(R.id.editnamedialogButton1);
				accept.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View p1)
						{
							updateLog(editText.getText().toString());
							
							dialog.dismiss();
						}
						
					
				});
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            keyboard.hideCustomKeyboard();
                        }
                        return true;
                    }
                });
				dialog.show();
				break;
				
				
			case R.id.item_deleteLog:
				userPrefs.deleteLog();
				updateLog("");
				break;
				
			case android.R.id.home:
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
			if(!s.equals("")){
				logText.append(s+ "\n");
			}
		}
	}
	@Override
	public void onClick(View p1)
	{
		// TODO: Implement this method
	}

	@Override
	public void onBackPressed() {
		if(keyboard!= null && keyboard.isCustomKeyboardVisible()){
			keyboard.hideCustomKeyboard();
		}else {
			super.onBackPressed();
		}
	}
}
