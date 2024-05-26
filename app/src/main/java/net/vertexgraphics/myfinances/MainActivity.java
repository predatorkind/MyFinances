package net.vertexgraphics.myfinances;


import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener

{

    private TextView currentBalanceText;
    private TextView fundsAvailableText;
    private TextView fundsAvailableText2;




    private UserPrefs userPrefs;
    private BillsAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);



       // MobileAds.initialize(getApplicationContext(),"ca-app-pub-6071190261989044~8801683217");


        userPrefs = UserPrefs.getInstance(this);
        userPrefs.loadIndexes();
        userPrefs.loadBills();
        userPrefs.loadBalance();
        userPrefs.loadIncome();


        ListView billsList = findViewById(R.id.billsList);
        adapter = new BillsAdapter(MainActivity.this, userPrefs.bills);
        billsList.setAdapter(adapter);
        Drawable myDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.save_icon, null);
        billsList.setOverscrollFooter(myDrawable);


        TextView currentDateText = findViewById(R.id.currentDateText);
        currentBalanceText = findViewById(R.id.currentBalanceText);
        currentDateText.setTextColor(Color.DKGRAY);
        currentBalanceText.setTextColor(Color.DKGRAY);
        fundsAvailableText = findViewById(R.id.funds_available);
        fundsAvailableText2 = findViewById(R.id.funds_available_till_nextpay);
        currentBalanceText.setOnClickListener(this);
        fundsAvailableText.setOnClickListener(this);
        fundsAvailableText2.setOnClickListener(this);


        currentDateText.setText(userPrefs.getTimeStamp(false));
        currentBalanceText.setText(String.valueOf(userPrefs.currentBalance));






    }




    @Override
    protected void onResume()
    {
        super.onResume();
        userPrefs.receiveIncome();
        adapter.notifyDataSetChanged();
        updateUi();

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);

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
        Objects.requireNonNull(dialog.getWindow()).setGravity(Gravity.BOTTOM);

        dialogSpinner = dialog.findViewById(R.id.addsubfundsdialogSpinner);
        label = dialog.findViewById(R.id.addsubfundsdialogLabel);

        String[] deductibles = {getString(R.string.food_string), getString(R.string.shopping_string),getString(R.string.savings_string),getString(R.string.installment_string),getString(R.string.transfer_string),getString(R.string.adjustment_string), getString(R.string.cash), getString(R.string.other_string)};
        String[] creditables = {getString(R.string.overtime_string), getString(R.string.winnings_string),getString(R.string.loan_string), getString(R.string.transfer_string), getString(R.string.adjustment_string), getString(R.string.other_string)};
        String[] setFundsTag = {getString(R.string.adjustment_string)};
        final ArrayAdapter<CharSequence> adapterAddSub = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        adapterAddSub.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dialogSpinner.setAdapter(adapterAddSub);

        dialogEditText = dialog.findViewById(R.id.addsubfundsdialogEditText);
        dialogEditText.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);

        dialogAcceptButton = dialog.findViewById(R.id.addsubfundsdialogButton1);
        dialogCancelButton = dialog.findViewById(R.id.addsubfundsdialogButton2);
        dialogCancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.setOnShowListener(p1 -> {
            dialogEditText.requestFocus();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        });


        int itemId = item.getItemId();
        if (itemId == R.id.add_funds) {
            adapterAddSub.clear();
            adapterAddSub.addAll(creditables);
            label.setText(getString(R.string.addFunds_string));
            dialogAcceptButton.setOnClickListener(v -> {
                float amount;
                try {
                    amount = Float.parseFloat(dialogEditText.getText().toString());
                    String tag = dialogSpinner.getSelectedItem().toString();
                    addFunds(amount, tag);
                } catch (NumberFormatException e) {

                    Toast.makeText(getApplicationContext(), R.string.invalidAmount_string, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }

                dialog.dismiss();
            });

            dialog.show();
        } else if (itemId == R.id.sub_funds) {
            adapterAddSub.clear();
            adapterAddSub.addAll(deductibles);
            dialog.setTitle(getString(R.string.subFunds_string));
            label.setText(getString(R.string.subFunds_string));
            dialogAcceptButton.setOnClickListener(v -> {
                float amount;
                try {
                    amount = Float.parseFloat(dialogEditText.getText().toString());
                    String tag = dialogSpinner.getSelectedItem().toString();
                    subFunds(amount, tag);
                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), getString(R.string.invalidAmount_string), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }

                dialog.dismiss();
            });

            dialog.show();
        } else if (itemId == R.id.set_funds) {
            adapterAddSub.clear();
            adapterAddSub.addAll(setFundsTag);
            label.setText(getString(R.string.setFunds_string));
            dialogAcceptButton.setOnClickListener(v -> {
                float amount;
                try {
                    amount = Float.parseFloat(dialogEditText.getText().toString());
                    String tag = dialogSpinner.getSelectedItem().toString();
                    setFunds(amount, tag);
                } catch (NumberFormatException e) {

                    Toast.makeText(getApplicationContext(), "Invalid amount!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }

                dialog.dismiss();
            });

            dialog.show();
        } else if (itemId == R.id.new_bill) {
            i = new Intent(this, BillActivity.class);
            i.putExtra("newBillFlag", "true");

            startActivity(i);
        } else if (itemId == R.id.view_log) {
            i = new Intent(this, LogActivity.class);
            startActivity(i);
        } else if (itemId == R.id.income_setup) {
            i = new Intent(this, IncomeActivity.class);
            startActivity(i);
        }
        return true;
    }

    @Override
    public void onClick(View view)
    {
        int id = view.getId();
        if (id == R.id.currentBalanceText) {
            Toast.makeText(this, getString(R.string.yourCurBal_string), Toast.LENGTH_SHORT).show();
        } else if (id == R.id.funds_available) {
            if (userPrefs.overdraft < 0) {
                Toast.makeText(this, getString(R.string.posOverOf_string) + " " + userPrefs.overdraft, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.fundsAvailUntil_string) + " " + userPrefs.getDateString(userPrefs.income.getCutOffDate()), Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.funds_available_till_nextpay) {
            Toast.makeText(this, getString(R.string.fundsAvailUntil_string) + " " + userPrefs.getDateString(userPrefs.income.getNextPay()), Toast.LENGTH_SHORT).show();
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


}

