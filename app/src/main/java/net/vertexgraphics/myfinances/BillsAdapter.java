package net.vertexgraphics.myfinances;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

class BillsAdapter extends ArrayAdapter<Bill>
{
    private Context context;
    private UserPrefs userPrefs;
    BillsAdapter(Context context, ArrayList<Bill> bills){

        super(context, 0, bills);
        this.context = context;
        userPrefs = UserPrefs.getInstance(context);
    }


    @Override
    public @NonNull View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {

        Bill bill = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.bill_list_item,parent, false);
        }
        userPrefs.checkPayable(bill);

        TextView listItemText = (TextView) convertView.findViewById(R.id.billListItemText);
        Button payBillButton = (Button) convertView.findViewById(R.id.billListItemButton);
        payBillButton.setEnabled(false);

        if(userPrefs.payable)payBillButton.setEnabled(true);
        payBillButton.setTag(bill);

        listItemText.setText(bill.getName()+" - " +bill.getAmount()+" - "+userPrefs.getDateString(bill.getDueDate()));
        listItemText.setTag(bill);
        listItemText.setTextColor(Color.DKGRAY);
        listItemText.setPaintFlags(0);
        if(userPrefs.payable)listItemText.setTextColor(Color.BLACK);
        if(bill.getDueDate()>userPrefs.income.getCutOffDate()){
            listItemText.setTextColor(Color.GRAY);
            listItemText.setPaintFlags(listItemText.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
        }
        if(System.currentTimeMillis()>bill.getDueDate())listItemText.setTextColor(Color.RED);

        payBillButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view)
            {
                Bill bill = (Bill) view.getTag();
                userPrefs.payBill(bill.getId());
                String logEntry = userPrefs.getTimeStamp(true)+" "+ context.getString(R.string.paid_string)+" "+ bill.getAmount() + " - "+bill.getName();
                userPrefs.writeLog(logEntry);
                notifyDataSetChanged();
                MainActivity activity = (MainActivity) context;
                activity.updateUi();
            }


        });

        listItemText.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view)
            {
                Bill bill = (Bill) view.getTag();


                Intent i = new Intent(context.getApplicationContext(), BillActivity.class);
                i.putExtra("newBillFlag", "false");
                i.putExtra("index", bill.getId());
                context.startActivity(i);
            }


        });

        return convertView;
    }


}

