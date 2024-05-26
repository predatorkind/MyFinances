package net.vertexgraphics.myfinances;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;



public class IncomeActivity extends AppCompatActivity implements View.OnClickListener
{
	UserPrefs userPrefs;
	Toolbar toolbar;
	
	TextView incomeAmountText;
	TextView incomeFrequencyText;
	TextView incomeDoMText;
	TextView incomeDoWText;
	TextView incomeLastPay;
	TextView incomeNextPay;
	TextView incomeCutOffDateText;
	
	Income tempIncome;
	
	//AdView adView;

	private CustomSoftKeyboard keyboard;
	

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.income);
		userPrefs = UserPrefs.getInstance(this);
		
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setTitle(R.string.incomeSetup_string);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		//adView = (AdView) findViewById(R.id.adView);
		//AdRequest adRequest = new AdRequest.Builder().build();
		if(userPrefs.showAds && !BuildConfig.DEBUG) {
			//adView.loadAd(adRequest);
		}
		incomeAmountText = (TextView) findViewById(R.id.incomeAmountText);
		incomeFrequencyText = (TextView) findViewById(R.id.incomeFrequencyText);
		incomeDoWText = (TextView) findViewById(R.id.incomeDoWText);
		incomeDoMText = (TextView) findViewById(R.id.incomeDoMText);
		incomeLastPay = (TextView) findViewById(R.id.incomeLastPayText);
		incomeNextPay = (TextView) findViewById(R.id.incomeNextPayText);
		incomeCutOffDateText = (TextView) findViewById(R.id.incomeCutOffDay);
		
		incomeAmountText.setOnClickListener(this);
		incomeFrequencyText.setOnClickListener(this);
		incomeDoWText.setOnClickListener(this);
		incomeDoMText.setOnClickListener(this);
		incomeCutOffDateText.setOnClickListener(this);
		
		tempIncome = userPrefs.getIncome();
		
		
		incomeAmountText.setText(String.valueOf(tempIncome.getAmount()));
		if(tempIncome.getWeeklyFlag()){
			incomeFrequencyText.setText(getString(R.string.weekly_string));
			incomeDoMText.setText(getString(R.string.na_string));
			incomeDoWText.setText(getDowString(tempIncome.getDayOfWeek()));
		}else{
			incomeFrequencyText.setText(getString(R.string.monthly_string));
			incomeDoMText.setText(String.valueOf(tempIncome.getDayOfMonth()));
			incomeDoWText.setText(getString(R.string.na_string));
		}
		
		if(tempIncome.getNextPay() == 0){
			tempIncome.setNextPay(userPrefs.getDueDate(false, Integer.parseInt(incomeDoMText.getText().toString()), ""));
			tempIncome.setCutOffDate(userPrefs.getDueDate(false, Integer.parseInt(incomeDoMText.getText().toString()), ""));
		}
		
		incomeLastPay.setText(userPrefs.getDateString(tempIncome.getLastPay()));
		incomeNextPay.setText(userPrefs.getDateString(tempIncome.getNextPay()));
		incomeCutOffDateText.setText(userPrefs.getDateString(tempIncome.getCutOffDate()));
		
	}

	@Override
	protected void onResume()
	{
		
		super.onResume();
		//if(adView != null){
		//	adView.resume();
		//}
	}

	@Override
	protected void onPause()
	{
		//if(adView != null){
		//	adView.pause();
		//}
		super.onPause();
	}

	@Override
	protected void onDestroy()
	{
		//if(adView != null){
		//	adView.destroy();
		//}
		super.onDestroy();
	}
	
	
	
	
	
	
	
	private String getDowString(int day){
		if(day == 1) return getString(R.string.sunday_string);
		if(day == 2) return getString(R.string.monday_string);
		if(day == 3) return getString(R.string.tuesday_string);
		if(day == 4) return getString(R.string.wednesday_string);
		if(day == 5) return getString(R.string.thursday_string);
		if(day == 6) return getString(R.string.friday_string);
		return getString(R.string.saturday_string);
		
	}
	
	private int getDowInt(String day){
		if(day.equals(getString(R.string.sunday_string)))return 1;
		if(day.equals(getString(R.string.monday_string)))return 2;
		if(day.equals(getString(R.string.tuesday_string)))return 3;
		if(day.equals(getString(R.string.wednesday_string)))return 4;
		if(day.equals(getString(R.string.thursday_string)))return 5;
		if(day.equals(getString(R.string.friday_string)))return 6;
		return 7;
		
	}
	
	private void changeMade(){
		toolbar.getMenu().getItem(0).setEnabled(true);
		toolbar.getMenu().getItem(0).setIcon(R.drawable.save_icon);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.income_menu, menu);
		menu.getItem(0).setIcon(R.drawable.save_icongrey);
		menu.getItem(0).setEnabled(false);
		return true;
	}
	
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onNavigateUp();
		
		switch(item.getItemId()){
			case R.id.item_save_income:
				userPrefs.income = tempIncome;
				userPrefs.saveIncome();
				
				String logEntry = userPrefs.getTimeStamp(true)+" "+ getString(R.string.incomeChanged_string);
				userPrefs.writeLog(logEntry);
				finish();
				break;
		}
		return true;
	}

	@Override
	public void onClick(View view)
	{
		
		final Dialog dialog = new Dialog(this);
		final EditText editText;
		final Spinner spinner;
		TextView dialogLabel;
		Button acceptButton;
		Button cancelButton;
		
		
		
		View.OnClickListener dismissListener = new View.OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					dialog.dismiss();
				}
				
			
		};
		
		switch(view.getId()){
			case R.id.incomeAmountText:
				dialog.setContentView(R.layout.edit_name_dialog);
                dialog.getWindow().setGravity(Gravity.BOTTOM);
				dialogLabel = (TextView) dialog.findViewById(R.id.editnamedialogLabel);
				dialogLabel.setText(R.string.incomeAmount_string);
				editText = (EditText) dialog.findViewById(R.id.editnamedialogEditText);
				editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
				keyboard = new CustomSoftKeyboard(dialog, R.id.keyboard, R.layout.qwerty_keboard, true, R.id.editnamedialogEditText);
				keyboard.registerEditText(editText);
				acceptButton = (Button) dialog.findViewById(R.id.editnamedialogButton1);
				cancelButton = (Button) dialog.findViewById(R.id.editnamedialogButton2);
				cancelButton.setOnClickListener(dismissListener);
				acceptButton.setOnClickListener(new View.OnClickListener(){

						@Override
						public void onClick(View p1)
						{
							try{
								tempIncome.setAmount(Float.parseFloat(editText.getText().toString()));
							}catch(NumberFormatException e){
								tempIncome.setAmount(0);
							}
							incomeAmountText.setText(String.valueOf(tempIncome.getAmount()));
							changeMade();
							dialog.dismiss();
						}
						
					
				});
				dialog.setOnShowListener(new Dialog.OnShowListener(){

						@Override
						public void onShow(DialogInterface p1)
						{
							//showSoftKeyboard(editText);
						}


					});
                dialog.setOnKeyListener(new Dialog.OnKeyListener(){

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
			case R.id.incomeFrequencyText:
				dialog.setContentView(R.layout.spinner_dialog);
				dialogLabel = (TextView) dialog.findViewById(R.id.spinnerdialogLabel);
				dialogLabel.setText(getString(R.string.frequency_string));
				spinner = (Spinner) dialog.findViewById(R.id.spinnerdialogSpinner);
				String frequency[] = {getString(R.string.monthly_string), getString(R.string.weekly_string)};

				
				ArrayAdapter<CharSequence> adapterFrequency = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, frequency);
				adapterFrequency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner.setAdapter(adapterFrequency);
				acceptButton = (Button) dialog.findViewById(R.id.spinnerdialogButton1);
				cancelButton = (Button) dialog.findViewById(R.id.spinnerdialogButton2);
				cancelButton.setOnClickListener(dismissListener);
				acceptButton.setOnClickListener(new View.OnClickListener(){

						@Override
						public void onClick(View p1)
						{
							String result= spinner.getSelectedItem().toString();
							incomeFrequencyText.setText(result);
							if(result.equals(getString(R.string.monthly_string))){
								incomeDoMText.setClickable(true);
								incomeDoWText.setClickable(false);
								incomeDoWText.setText(getString(R.string.na_string));
								incomeDoMText.setText("1");
								tempIncome.setWeeklyFlag(false);
								tempIncome.setDayOfWeek(0);
								tempIncome.setDayOfMonth(1);
								tempIncome.setNextPay(userPrefs.getDueDate(false, Integer.parseInt(incomeDoMText.getText().toString()), ""));
								incomeNextPay.setText(userPrefs.getDateString(tempIncome.getNextPay()));
							}else{
								incomeDoMText.setClickable(false);
								incomeDoWText.setClickable(true);
								incomeDoMText.setText(getString(R.string.na_string));
								incomeDoWText.setText(getString(R.string.monday_string));
								tempIncome.setWeeklyFlag(true);
								tempIncome.setDayOfWeek(2);
								tempIncome.setDayOfMonth(0);
								tempIncome.setNextPay(userPrefs.getDueDate(true, 0, incomeDoWText.getText().toString()));

								incomeNextPay.setText(userPrefs.getDateString(tempIncome.getNextPay()));

							}
							changeMade();
							dialog.dismiss();
						}
						
					
				});
				dialog.show();
				break;
			case R.id.incomeDoMText:
				dialog.setContentView(R.layout.spinner_dialog);
				dialogLabel = (TextView) dialog.findViewById(R.id.spinnerdialogLabel);
				dialogLabel.setText(getString(R.string.chooseDom_string));
				String dayofmonth[] = {"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29", "30", "31"};

				spinner = (Spinner) dialog.findViewById(R.id.spinnerdialogSpinner);
				ArrayAdapter<CharSequence> adapterDoM = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, dayofmonth);
				adapterDoM.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner.setAdapter(adapterDoM);
				acceptButton = (Button) dialog.findViewById(R.id.spinnerdialogButton1);
				cancelButton = (Button) dialog.findViewById(R.id.spinnerdialogButton2);
				cancelButton.setOnClickListener(dismissListener);
				acceptButton.setOnClickListener(new View.OnClickListener(){

						@Override
						public void onClick(View p1)
						{
							incomeDoMText.setText(spinner.getSelectedItem().toString());
							tempIncome.setDayOfMonth(Integer.parseInt(spinner.getSelectedItem().toString()));
							//set temp income check temp income day field
							tempIncome.setNextPay(userPrefs.getDueDate(false, Integer.parseInt(incomeDoMText.getText().toString()), ""));
							incomeNextPay.setText(userPrefs.getDateString(tempIncome.getNextPay()));
							changeMade();
							dialog.dismiss();
						}
						
					
				});
				dialog.show();
				break;
			case R.id.incomeDoWText:
				dialog.setContentView(R.layout.spinner_dialog);
				dialogLabel = (TextView) dialog.findViewById(R.id.spinnerdialogLabel);
				dialogLabel.setText(getString(R.string.chooseDow_string));
				String dayofweek[] = {getString(R.string.monday_string), getString(R.string.tuesday_string), getString(R.string.wednesday_string), getString(R.string.thursday_string), getString(R.string.friday_string), getString(R.string.saturday_string), getString(R.string.sunday_string)};

				spinner = (Spinner) dialog.findViewById(R.id.spinnerdialogSpinner);
				ArrayAdapter<CharSequence> adapterDoW = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, dayofweek);
				adapterDoW.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner.setAdapter(adapterDoW);
				acceptButton = (Button) dialog.findViewById(R.id.spinnerdialogButton1);
				cancelButton = (Button) dialog.findViewById(R.id.spinnerdialogButton2);
				cancelButton.setOnClickListener(dismissListener);
				acceptButton.setOnClickListener(new View.OnClickListener(){

						@Override
						public void onClick(View p1)
						{
							incomeDoWText.setText(spinner.getSelectedItem().toString());
							tempIncome.setNextPay(userPrefs.getDueDate(true, 0, incomeDoWText.getText().toString()));
							tempIncome.setDayOfWeek(getDowInt( spinner.getSelectedItem().toString()));
							incomeNextPay.setText(userPrefs.getDateString(tempIncome.getNextPay()));
							changeMade();
							dialog.dismiss();
						}


					});
				dialog.show();
				break;
			case R.id.incomeCutOffDay:
				dialog.setContentView(R.layout.spinner_dialog);
				dialogLabel = (TextView) dialog.findViewById(R.id.spinnerdialogLabel);
				dialogLabel.setText(R.string.chooseCutoffDay_string);
				String cuttoffDays[] = {"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29", "30", "31"};

				spinner = (Spinner) dialog.findViewById(R.id.spinnerdialogSpinner);
				ArrayAdapter<CharSequence> adapterCutOffDate = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, cuttoffDays);
				adapterCutOffDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner.setAdapter(adapterCutOffDate);
				acceptButton = (Button) dialog.findViewById(R.id.spinnerdialogButton1);
				cancelButton = (Button) dialog.findViewById(R.id.spinnerdialogButton2);
				cancelButton.setOnClickListener(dismissListener);
				acceptButton.setOnClickListener(new View.OnClickListener(){

						@Override
						public void onClick(View p1)
						{
							int result = Integer.parseInt(spinner.getSelectedItem().toString());
							
							tempIncome.setCutOffDate(userPrefs.getDueDate(false, result, ""));
							incomeCutOffDateText.setText(userPrefs.getDateString(tempIncome.getCutOffDate()));
							changeMade();
							dialog.dismiss();
						}


					});
				dialog.show();
				break;
		}
	}
	
	public void showSoftKeyboard(View view) {
		if (view.requestFocus()) {
			InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
		}
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
