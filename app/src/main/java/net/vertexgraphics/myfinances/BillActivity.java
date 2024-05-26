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
import android.widget.Toast;

//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BillActivity extends AppCompatActivity implements View.OnClickListener
{

    Toolbar toolbar;


    Button payButton;

    TextView billNameText;
    TextView billAmountText;
    TextView billFrequencyText;
    TextView billDoWText;
    TextView billDoMText;
    TextView billLastPaidText;
    TextView billDueDateText;

    UserPrefs userPrefs;
    int billId;
    Bill currentBill;
    Bundle extras;

    //AdView adView;

    private CustomSoftKeyboard keyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.bill);
        toolbar= (Toolbar) findViewById(R.id.toolbar);

        toolbar.setTitle(getString(R.string.billDetails_string));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userPrefs= UserPrefs.getInstance(this);

        //adView = (AdView) findViewById(R.id.adView);
        //AdRequest adRequest = new AdRequest.Builder().build();
        if(userPrefs.showAds && !BuildConfig.DEBUG) {
        //    adView.loadAd(adRequest);
        }

        payButton = (Button) findViewById(R.id.payButton);


        billNameText = (TextView) findViewById(R.id.billNameText);
        billAmountText = (TextView) findViewById(R.id.billAmountText);
        billFrequencyText = (TextView) findViewById(R.id.billFrequencyText);
        billDoWText = (TextView) findViewById(R.id.billDoWText);
        billDoMText = (TextView) findViewById(R.id.billDoMText);
        billLastPaidText = (TextView) findViewById(R.id.billLastPaidText);
        billDueDateText = (TextView) findViewById(R.id.billDueDateText);

        billNameText.setOnClickListener(this);
        billAmountText.setOnClickListener(this);
        billFrequencyText.setOnClickListener(this);
        billDoWText.setOnClickListener(this);
        billDoMText.setOnClickListener(this);
        payButton.setOnClickListener(this);




        extras = getIntent().getExtras();


        if(extras.getString("newBillFlag").equals("true")){

            billDoWText.setClickable(false);
            payButton.setEnabled(false);
            billId = Integer.parseInt( userPrefs.getFreeId());
            toolbar.setTitle(getString(R.string.createBill_string));
            currentBill = new Bill(billId, "", 0, true,1, "",0,System.currentTimeMillis());
            billDueDateText.setText(userPrefs.getDateString(userPrefs.getDueDate(false, 1, "")));


        }else{
            billId = extras.getInt("index");

            currentBill = userPrefs.getBill(billId);
            payButton.setEnabled(false);


            billNameText.setText(currentBill.getName());
            billAmountText.setText(String.valueOf(currentBill.getAmount()));
            if(currentBill.getWeeklyFlag()){
                billFrequencyText.setText(getString(R.string.weekly_string));
                billDoWText.setClickable(true);
                billDoMText.setClickable(false);
            }else{
                billFrequencyText.setText(getString(R.string.monthly_string));
                billDoWText.setClickable(false);
                billDoMText.setClickable(true);
            }
            billDoWText.setText(currentBill.getDayOfWeek());
            billDoMText.setText(String.valueOf(currentBill.getDayOfMonth()));

            if(!(currentBill.getLastPaid()==0)){
                billLastPaidText.setText(userPrefs.getDateString(currentBill.getLastPaid()));
            }

            billDueDateText.setText(userPrefs.getDateString(currentBill.getDueDate()));
            toolbar.setTitle(getString(R.string.editBill_string));

        }

    }

    @Override
    protected void onResume()
    {

        super.onResume();

      //  if(adView != null){
       //     adView.resume();
       // }
    }

    @Override
    protected void onPause()
    {
       // if(adView != null){
        //    adView.pause();
       // }
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
       // if(adView != null){
      //      adView.destroy();
       // }
        super.onDestroy();
    }







    private void changeMade(){
        toolbar.getMenu().getItem(0).setEnabled(true);
        toolbar.getMenu().getItem(0).setIcon(R.drawable.save_icon);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.bill_menu, menu);
        menu.getItem(0).setEnabled(false);
        menu.getItem(0).setIcon(R.drawable.save_icongrey);
        if(extras.getString("newBillFlag").equals("true")){
            menu.getItem(2).setEnabled(false);
            menu.getItem(1).setEnabled(false);
            payButton.setEnabled(false);
        }else{

            userPrefs.checkPayable(currentBill);
            menu.getItem(1).setEnabled(userPrefs.payable);
            payButton.setEnabled(userPrefs.payable);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onNavigateUp();

        switch (item.getItemId()){

            case R.id.save_bill:
                String billName;
                float billAmount;
                boolean freq;
                int dom;
                String dow;
                long lastPaid;
                long dueDate;
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
                Date date = new Date();
                String lastPaidString = (String) billLastPaidText.getText();
                String dueDateString = (String) billDueDateText.getText();


                billName = (String) billNameText.getText();

                try{
                    billAmount = Float.parseFloat((String)billAmountText.getText());
                }catch(NumberFormatException e){
                    billAmount = 0;
                }

                if(billFrequencyText.getText().equals(getString(R.string.weekly_string))){
                    freq = true;
                }else{
                    freq = false;
                }

                try{
                    dom = Integer.parseInt((String)billDoMText.getText());
                }catch(NumberFormatException e){
                    dom = 0;
                }
                dow = (String) billDoWText.getText();

                if (!lastPaidString.equals(getString(R.string.na_string))){
                    try
                    {
                        date = parser.parse(lastPaidString);
                    }
                    catch (ParseException ignored)
                    {}
                    lastPaid = date.getTime();
                }else{
                    lastPaid = 0;
                }

                if(!dueDateString.equals(getString(R.string.na_string))){
                    try
                    {
                        date = parser.parse(dueDateString);

                    }
                    catch (ParseException ignored)
                    {}
                    dueDate = date.getTime();
                }else{
                    dueDate=0;
                }

                currentBill.setName(billName);
                currentBill.setAmount(billAmount);
                currentBill.setWeeklyFlag(freq);
                currentBill.setDayOfMonth(dom);
                currentBill.setDayOfWeek(dow);
                currentBill.setDueDate(dueDate);
                currentBill.setLastPaid(lastPaid);


                if(extras.getString("newBillFlag").equals("true")){



                    userPrefs.setBill(currentBill);
                    userPrefs.addBill(currentBill);
                    userPrefs.addId(String.valueOf(billId));
                    userPrefs.saveIndexes();
                    String logEntry = userPrefs.getTimeStamp(true)+" "+ getString(R.string.billSaved_string)+": "+currentBill.getName();
                    userPrefs.writeLog(logEntry);

                    Toast.makeText(this, getString(R.string.billSaved_string), Toast.LENGTH_SHORT).show();

                }else if(extras.getString("newBillFlag").equals("false")){

                    userPrefs.setBill(currentBill);
                    for(Bill bill: userPrefs.bills){
                        if(bill.getId()==billId)userPrefs.bills.set(userPrefs.bills.indexOf(bill), currentBill);
                    }

                    String logEntry = userPrefs.getTimeStamp(true)+" "+ getString(R.string.billChanged_string)+": "+currentBill.getName();
                    userPrefs.writeLog(logEntry);

                    Toast.makeText(this, getString(R.string.billChanged_string), Toast.LENGTH_SHORT).show();

                }
                finish();
                break;
            case R.id.pay_bill:
                userPrefs.payBill(currentBill.getId());
                String logEntry = userPrefs.getTimeStamp(true)+" "+ getString(R.string.paid_string)+" "+ currentBill.getAmount() + " - "+currentBill.getName();
                userPrefs.writeLog(logEntry);
                billLastPaidText.setText(userPrefs.getDateString(userPrefs.getBill(currentBill.getId()).getLastPaid()));
                billDueDateText.setText(userPrefs.getDateString(userPrefs.getBill(currentBill.getId()).getDueDate()));
                userPrefs.checkPayable(userPrefs.getBill(currentBill.getId()));
                toolbar.getMenu().getItem(1).setEnabled(userPrefs.payable);
                payButton.setEnabled(userPrefs.payable);
                break;
            case R.id.delete_bill:
                userPrefs.deleteBill(billId);
                userPrefs.deleteId(String.valueOf(billId));
                userPrefs.saveIndexes();

                String logEntry2 = userPrefs.getTimeStamp(true)+" "+ getString(R.string.billDeleted_string)+": "+currentBill.getName();
                userPrefs.writeLog(logEntry2);

                finish();

                break;
        }
        return true;
    }



    public void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    @Override
    public void onClick(View v){

        final Dialog dialog = new Dialog(this);
        final EditText dialogEditText;
        final Spinner dialogSpinner;
        TextView dialogLabel;
        Button dialogAcceptButton;
        Button dialogCancelButton;



        switch (v.getId()){

            case R.id.billNameText:

                dialog.setContentView(R.layout.edit_name_dialog);
                dialog.getWindow().setGravity(Gravity.BOTTOM);

                dialogLabel = (TextView) dialog.findViewById(R.id.editnamedialogLabel);
                dialogLabel.setText(getString(R.string.billName_string));
                dialogEditText = (EditText) dialog.findViewById(R.id.editnamedialogEditText);
                dialogEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                keyboard = new CustomSoftKeyboard(dialog, R.id.keyboard, R.layout.qwerty_keboard, false, R.id.editnamedialogEditText);
                keyboard.registerEditText(dialogEditText);
                dialogAcceptButton = (Button) dialog.findViewById(R.id.editnamedialogButton1);
                dialogCancelButton = (Button) dialog.findViewById(R.id.editnamedialogButton2);
                dialogAcceptButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        billNameText.setText(dialogEditText.getText().toString());
                        changeMade();
                        dialog.dismiss();
                    }
                });
                dialogCancelButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        dialog.dismiss();
                    }
                });

                dialog.setOnShowListener(new Dialog.OnShowListener(){

                    @Override
                    public void onShow(DialogInterface p1)
                    {
                        //showSoftKeyboard(dialogEditText);
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
            case R.id.billAmountText:

                dialog.setContentView(R.layout.edit_name_dialog);
                dialog.getWindow().setGravity(Gravity.BOTTOM);
                dialogLabel = (TextView) dialog.findViewById(R.id.editnamedialogLabel);
                dialogLabel.setText(getString(R.string.billAmount_string));
                dialogEditText = (EditText) dialog.findViewById(R.id.editnamedialogEditText);
                dialogEditText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                keyboard = new CustomSoftKeyboard(dialog, R.id.keyboard, R.layout.qwerty_keboard, true, R.id.editnamedialogEditText);
                keyboard.registerEditText(dialogEditText);
                dialogAcceptButton = (Button) dialog.findViewById(R.id.editnamedialogButton1);
                dialogCancelButton = (Button) dialog.findViewById(R.id.editnamedialogButton2);
                dialogAcceptButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        billAmountText.setText(dialogEditText.getText().toString());
                        changeMade();
                        dialog.dismiss();
                    }
                });
                dialogCancelButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        dialog.dismiss();
                    }
                });
                dialog.setOnShowListener(new Dialog.OnShowListener(){

                    @Override
                    public void onShow(DialogInterface p1)
                    {
                        //showSoftKeyboard(dialogEditText);
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
            case R.id.billFrequencyText:

                dialog.setContentView(R.layout.spinner_dialog);
                dialogLabel = (TextView) dialog.findViewById(R.id.spinnerdialogLabel);
                dialogLabel.setText(getString(R.string.billFreq_string));
                String frequency[] = {getString(R.string.monthly_string), getString(R.string.weekly_string)};

                dialogSpinner = (Spinner) dialog.findViewById(R.id.spinnerdialogSpinner);
                ArrayAdapter<CharSequence> adapterFrequency = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, frequency);
                adapterFrequency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                dialogSpinner.setAdapter(adapterFrequency);
                dialogAcceptButton = (Button) dialog.findViewById(R.id.spinnerdialogButton1);
                dialogCancelButton = (Button) dialog.findViewById(R.id.spinnerdialogButton2);
                dialogAcceptButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        String result= dialogSpinner.getSelectedItem().toString();
                        billFrequencyText.setText(result);
                        if(result.equals(getString(R.string.monthly_string))){
                            billDoMText.setClickable(true);
                            billDoWText.setClickable(false);
                            billDoWText.setText(getString(R.string.na_string));
                            billDoMText.setText("1");
                            currentBill.setDueDate(userPrefs.getDueDate(false, Integer.parseInt(billDoMText.getText().toString()), ""));
                            billDueDateText.setText(userPrefs.getDateString(currentBill.getDueDate()));
                        }else{
                            billDoMText.setClickable(false);
                            billDoWText.setClickable(true);
                            billDoMText.setText(getString(R.string.na_string));
                            billDoWText.setText(getString(R.string.monday_string));
                            currentBill.setDueDate(userPrefs.getDueDate(true, 0, billDoWText.getText().toString()));

                            billDueDateText.setText(userPrefs.getDateString(currentBill.getDueDate()));

                        }
                        changeMade();
                        dialog.dismiss();

                    }
                });
                dialogCancelButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        dialog.dismiss();
                    }
                });
                dialog.show();
                break;
            case R.id.billDoWText:
                dialog.setContentView(R.layout.spinner_dialog);
                dialogLabel = (TextView) dialog.findViewById(R.id.spinnerdialogLabel);
                dialogLabel.setText(getString(R.string.chooseDow_string));
                String dayofweek[] = {getString(R.string.monday_string), getString(R.string.tuesday_string), getString(R.string.wednesday_string), getString(R.string.thursday_string),getString(R.string.friday_string), getString(R.string.saturday_string), getString(R.string.sunday_string)};

                dialogSpinner = (Spinner) dialog.findViewById(R.id.spinnerdialogSpinner);
                ArrayAdapter<CharSequence> adapterDoW = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, dayofweek);
                adapterDoW.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                dialogSpinner.setAdapter(adapterDoW);
                dialogAcceptButton = (Button) dialog.findViewById(R.id.spinnerdialogButton1);
                dialogCancelButton = (Button) dialog.findViewById(R.id.spinnerdialogButton2);
                dialogAcceptButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        billDoWText.setText(dialogSpinner.getSelectedItem().toString());
                        currentBill.setDueDate(userPrefs.getDueDate(true, 0, billDoWText.getText().toString()));

                        billDueDateText.setText(userPrefs.getDateString(currentBill.getDueDate()));
                        changeMade();
                        dialog.dismiss();
                    }
                });
                dialogCancelButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        dialog.dismiss();
                    }
                });
                dialog.show();
                break;
            case R.id.billDoMText:
                dialog.setContentView(R.layout.spinner_dialog);
                dialogLabel = (TextView) dialog.findViewById(R.id.spinnerdialogLabel);
                dialogLabel.setText(getString(R.string.chooseDom_string));
                String dayofmonth[] = {"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28"};

                dialogSpinner = (Spinner) dialog.findViewById(R.id.spinnerdialogSpinner);
                ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, dayofmonth);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                dialogSpinner.setAdapter(adapter);
                dialogAcceptButton = (Button) dialog.findViewById(R.id.spinnerdialogButton1);
                dialogCancelButton = (Button) dialog.findViewById(R.id.spinnerdialogButton2);
                dialogAcceptButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        billDoMText.setText(dialogSpinner.getSelectedItem().toString());
                        currentBill.setDueDate(userPrefs.getDueDate(false, Integer.parseInt(billDoMText.getText().toString()), ""));
                        billDueDateText.setText(userPrefs.getDateString(currentBill.getDueDate()));
                        changeMade();
                        dialog.dismiss();
                    }
                });
                dialogCancelButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        dialog.dismiss();
                    }
                });
                dialog.show();
                break;
            case R.id.payButton:
                userPrefs.payBill(currentBill.getId());
                String logEntry = userPrefs.getTimeStamp(true)+" "+ getString(R.string.paid_string)+" "+ currentBill.getAmount() + " - "+currentBill.getName();
                userPrefs.writeLog(logEntry);
                billLastPaidText.setText(userPrefs.getDateString(userPrefs.getBill(currentBill.getId()).getLastPaid()));
                billDueDateText.setText(userPrefs.getDateString(userPrefs.getBill(currentBill.getId()).getDueDate()));
                userPrefs.checkPayable(userPrefs.getBill(currentBill.getId()));
                toolbar.getMenu().getItem(1).setEnabled(userPrefs.payable);
                payButton.setEnabled(userPrefs.payable);
                break;

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


