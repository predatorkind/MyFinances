package net.vertexgraphics.myfinances;



import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener

{

    private Toolbar toolbar;
    private TextView currentBalanceText;
    private TextView fundsAvailableText;
    private TextView fundsAvailableText2;

    private CustomSoftKeyboard keyboard;

    private int addFundsInt = 0;
    private int subFundsInt = 1;
    private int setFunds = 2;
    private int newBillInt = 3;
    private int viewLogInt = 4;
    private int incomeSetupInt = 5;
    private int removeAddsInt = 6;
    private int resetInt = 7;


    private UserPrefs userPrefs;
    private BillsAdapter adapter;

    //private AdView adView;

    //IabHelper iabHelper;
    static final String SKU_REMOVEADS = "net.vertexgraphics.myfinances.removeads";
    //static final String SKU_REMOVEADS = "android.test.purchased";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);



       // MobileAds.initialize(getApplicationContext(),"ca-app-pub-6071190261989044~8801683217");


        userPrefs = UserPrefs.getInstance(this);
        userPrefs.loadIndexes();
        userPrefs.loadBills();
        userPrefs.loadBalance();
        userPrefs.loadIncome();

       // adView = (AdView) findViewById(R.id.adView);
        //AdRequest adRequest = new AdRequest.Builder().build();

        //AdRequest adRequest = new AdRequest.Builder().build();

        if(userPrefs.showAds && !BuildConfig.DEBUG) {
            //adView.loadAd(adRequest);
        }else{
            //ViewGroup.LayoutParams params = adView.getLayoutParams();
            //params.height = 0;
            //adView.setLayoutParams(params);
        }

        ListView billsList = (ListView) findViewById(R.id.billsList);
        adapter = new BillsAdapter(MainActivity.this, userPrefs.bills);
        billsList.setAdapter(adapter);
        Drawable myDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.save_icon, null);
        billsList.setOverscrollFooter(myDrawable);


        TextView currentDateText = (TextView) findViewById(R.id.currentDateText);
        currentBalanceText = (TextView) findViewById(R.id.currentBalanceText);
        currentDateText.setTextColor(Color.DKGRAY);
        currentBalanceText.setTextColor(Color.DKGRAY);
        fundsAvailableText = (TextView) findViewById(R.id.funds_available);
        fundsAvailableText2 = (TextView) findViewById(R.id.funds_available_till_nextpay);
        currentBalanceText.setOnClickListener(this);
        fundsAvailableText.setOnClickListener(this);
        fundsAvailableText2.setOnClickListener(this);


        currentDateText.setText(userPrefs.getTimeStamp(false));
        currentBalanceText.setText(String.valueOf(userPrefs.currentBalance));





        String base64Code ="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAplxJQn7zqB1DU5T2FTZKVyMKbXXjWTNNBYr0HrZjpqq+BEgmxPlcwRAxr6FRN60wgvemezPsfBkbM5FRdGSS+6cDRqISnkRVR0VUm+uk3ffhTlSzAQAWtUj7cJ9p5xujmZHt+yhnsB+VV0YpzXVicAILSyFi3iDXhc84WkmNESCL8O7Evuc7EHyHGH4PDfPQ2PkBYFs5lWB3vz9n9iBzTduTwNvgFLT9yAQC3iCaqA5L9l/Ae8CKUG/7zpezxSlA/DihDcCSe2KaDHFDzwY/qoEv6SPopGXEQHNl1VH6eE/1e4W3P726Wl8SfGrVn1yx/kpduf2+sDyWx9TFV79G2QIDAQAB";
       // iabHelper = new IabHelper(this, base64Code);


    }
/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(!iabHelper.handleActivityResult(requestCode,resultCode, data)){
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener(){

        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase purchase)
        {
            if(result.isFailure()){


            }else if(purchase.getSku().equals(SKU_REMOVEADS)){

                toolbar.getMenu().getItem(removeAddsInt).setEnabled(false);
                //toolbar.getMenu().getItem(resetInt).setEnabled(true);
                userPrefs.showAds = false;

            }
        }


    };

    public void consumeItem(){
        iabHelper.queryInventoryAsync(mReceivedInventoryListener);
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener = new IabHelper.QueryInventoryFinishedListener(){

        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inv)
        {
            if(result.isFailure()){
                //handle Failure
            }else{
                iabHelper.consumeAsync(inv.getPurchase(SKU_REMOVEADS), mConsumeFinishedListener);

            }
        }


    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener(){

        @Override
        public void onConsumeFinished(Purchase purchase, IabResult result)
        {
            if(result.isSuccess()){
                toolbar.getMenu().getItem(removeAddsInt).setEnabled(true);
                //toolbar.getMenu().getItem(resetInt).setEnabled(false);
            }else{
                //handle error
            }
        }


    }; */



    @Override
    protected void onResume()
    {
        super.onResume();
        userPrefs.receiveIncome();
        adapter.notifyDataSetChanged();
        updateUi();
        //if(adView != null){
            //adView.resume();
        //}
    }

    @Override
    protected void onPause()
    {
       // if(adView != null){
       //     adView.pause();
       // }
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {

        super.onDestroy();
    }




    public void updateUi(){
        currentBalanceText.setText(String.format("%.02f",(userPrefs.currentBalance)));

        fundsAvailableText.setText(String.format("%.02f",(userPrefs.calcAvailableFunds())));
        fundsAvailableText2.setText(String.format("%.02f",(userPrefs.overdraft)));
        fundsAvailableText.setTextColor(Color.DKGRAY);
        fundsAvailableText2.setTextColor(Color.DKGRAY);
        if(userPrefs.overdraft < 0){
            fundsAvailableText.setTextColor(Color.RED);
            fundsAvailableText2.setTextColor(Color.RED);
        }

    }
/*
    IabHelper.QueryInventoryFinishedListener
            mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory)
        {
            if (result.isFailure()) {
                //Log.d("MainActivity", "Inventory Query Failed!");
                return;
            }

            userPrefs.showAds = !inventory.hasPurchase(SKU_REMOVEADS);

            if(userPrefs.showAds) {
                //Log.d("MainActivity", "Query result: "+result);
                //Log.d("MainActivity", "Enabling Ad Removal");
                toolbar.getMenu().getItem(removeAddsInt).setEnabled(true);
                //toolbar.getMenu().getItem(resetInt).setEnabled(false);
            }else{
                toolbar.getMenu().getItem(removeAddsInt).setEnabled(false);
               // toolbar.getMenu().getItem(resetInt).setEnabled(true);
            }
        }
    };
*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        toolbar.getMenu().getItem(removeAddsInt).setEnabled(false);
       /* toolbar.getMenu().getItem(resetInt).setEnabled(false);
        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener(){

            @Override
            public void onIabSetupFinished(IabResult result)
            {
                if(!result.isSuccess()){
                    //Log.d("MainActivity", "Iap setup failed: " + result);
                }else{
                    //Log.d("MainActivity","Iap setup complete");
                    List additionalSkuList = new ArrayList();
                    additionalSkuList.add(SKU_REMOVEADS);
                    //
                    iabHelper.queryInventoryAsync(true, additionalSkuList,
                            mQueryFinishedListener);
                }
            }


        });
        */

        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent i;
        final Dialog dialog = new Dialog(this);
        final EditText dialogEditText;
        final Spinner dialogSpinner;
        TextView label;
        Button dialogAcceptButton;
        Button dialogCancelButton;
        dialog.setContentView(R.layout.add_sub_funds_dialog);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        keyboard = new CustomSoftKeyboard(dialog, R.id.keyboard, R.layout.qwerty_keboard, true, R.id.addsubfundsdialogEditText);

        dialogSpinner = (Spinner) dialog.findViewById(R.id.addsubfundsdialogSpinner);
        label = (TextView) dialog.findViewById(R.id.addsubfundsdialogLabel);

        String deductibles[] = {getString(R.string.food_string), getString(R.string.shopping_string),getString(R.string.savings_string),getString(R.string.installment_string),getString(R.string.transfer_string),getString(R.string.adjustment_string), getString(R.string.other_string)};
        String creditables[] = {getString(R.string.overtime_string), getString(R.string.winnings_string),getString(R.string.loan_string), getString(R.string.transfer_string), getString(R.string.adjustment_string), getString(R.string.other_string)};
        String setFundsTag[] = {getString(R.string.adjustment_string)};
        final ArrayAdapter<CharSequence> adapterAddSub = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        adapterAddSub.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dialogSpinner.setAdapter(adapterAddSub);

        dialogEditText = (EditText) dialog.findViewById(R.id.addsubfundsdialogEditText);
        dialogEditText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        keyboard.registerEditText(dialogEditText);
        dialogAcceptButton = (Button) dialog.findViewById(R.id.addsubfundsdialogButton1);
        dialogCancelButton = (Button) dialog.findViewById(R.id.addsubfundsdialogButton2);
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

        switch (item.getItemId()){
            case R.id.add_funds:
                adapterAddSub.clear();
                adapterAddSub.addAll(creditables);
                label.setText(getString(R.string.addFunds_string));
                dialogAcceptButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        float amount;
                        try{
                            amount = Float.parseFloat(dialogEditText.getText().toString());
                            String tag = dialogSpinner.getSelectedItem().toString();
                            addFunds(amount, tag);
                        }catch(NumberFormatException e){

                            Toast.makeText(getApplicationContext(), R.string.invalidAmount_string, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }

                        dialog.dismiss();
                    }
                });

                dialog.show();
                break;
            case R.id.sub_funds:
                adapterAddSub.clear();
                adapterAddSub.addAll(deductibles);
                dialog.setTitle(getString(R.string.subFunds_string));
                label.setText(getString(R.string.subFunds_string));
                dialogAcceptButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        float amount;
                        try{
                            amount = Float.parseFloat(dialogEditText.getText().toString());
                            String tag = dialogSpinner.getSelectedItem().toString();
                            subFunds(amount, tag);
                        }catch(NumberFormatException e){
                            Toast.makeText(getApplicationContext(), getString(R.string.invalidAmount_string), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }

                        dialog.dismiss();
                    }
                });

                dialog.show();
                break;
            case R.id.set_funds:
                adapterAddSub.clear();
                adapterAddSub.addAll(setFundsTag);
                label.setText(getString(R.string.setFunds_string));
                dialogAcceptButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        float amount;
                        try{
                            amount = Float.parseFloat(dialogEditText.getText().toString());
                            String tag = dialogSpinner.getSelectedItem().toString();
                            setFunds(amount, tag);
                        }catch(NumberFormatException e){

                            Toast.makeText(getApplicationContext(), "Invalid amount!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }

                        dialog.dismiss();
                    }
                });

                dialog.show();
                break;
            case R.id.new_bill:
                i = new Intent(this, BillActivity.class);
                i.putExtra("newBillFlag", "true");

                startActivity(i);
                break;
            case R.id.view_log:
                i = new Intent(this, LogActivity.class);
                startActivity(i);

                break;
            case R.id.income_setup:
                i = new Intent(this, IncomeActivity.class);
                startActivity(i);
                break;
            case R.id.remove_ads:
                Log.d("MainActivity", "Initiating purchase..");
                //iabHelper.launchPurchaseFlow(this, SKU_REMOVEADS, 10001, mPurchaseFinishedListener, "");


                break;
            //case R.id.reset:
             //   consumeItem();
               // break;
        }
        return true;
    }

    @Override
    public void onClick(View view)
    {
        switch(view.getId()){
            case R.id.currentBalanceText:
                Toast.makeText(this, getString(R.string.yourCurBal_string), Toast.LENGTH_SHORT).show();
                break;
            case R.id.funds_available:

                if(userPrefs.overdraft < 0){
                    Toast.makeText(this, getString(R.string.posOverOf_string)+" "+userPrefs.overdraft, Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, getString(R.string.fundsAvailUntil_string)+" "+ userPrefs.getDateString(userPrefs.income.getCutOffDate()), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.funds_available_till_nextpay:
                Toast.makeText(this, getString(R.string.fundsAvailUntil_string)+ " "+userPrefs.getDateString(userPrefs.income.getNextPay()), Toast.LENGTH_SHORT).show();
                break;

        }
    }








    public void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);

        }
    }



    public void addFunds(float amount, String tag){
        userPrefs.setCurrentBalance(amount);
        userPrefs.saveBalance();
        updateUi();
        String logEntry = userPrefs.getTimeStamp(true)+" "+ getString(R.string.added_string)+" "+ String.format("%.02f",amount) + " - "+tag;
        userPrefs.writeLog(logEntry);
    }

    public void subFunds(float amount, String tag){
        userPrefs.setCurrentBalance(-amount);
        userPrefs.saveBalance();
        updateUi();
        String logEntry = userPrefs.getTimeStamp(true)+" "+getString(R.string.deducted_string)+" "+ String.format("%.02f",amount)+ " - "+tag;
        userPrefs.writeLog(logEntry);
    }


    public void setFunds(float amount,String tag){
        float adjustment = amount - userPrefs.getCurrentBalance();
        userPrefs.setCurrentBalance(-userPrefs.getCurrentBalance());
        userPrefs.setCurrentBalance(amount);
        userPrefs.saveBalance();
        updateUi();
        String logEntry = userPrefs.getTimeStamp(true)+" "+getString(R.string.balanceSet_string)+" "+amount+ " - "+tag+" ("+String.format("%.02f", adjustment)+")";
        userPrefs.writeLog(logEntry);
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

