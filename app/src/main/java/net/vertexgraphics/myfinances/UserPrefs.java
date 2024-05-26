package net.vertexgraphics.myfinances;




import android.content.Context;
import android.content.SharedPreferences;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;


public class UserPrefs
{
    private static UserPrefs userPrefs = null;

    private static final String PREFS_NAME = "net.vertexgraphics.bills";
    private static final String KEY_PREFIX = "key_BILL";
    private static final String KEY_INDEXES = "key_INDEXES";
    private static final String KEY_NAME = "key_BILL_NAME";
    private static final String KEY_AMOUNT = "key_BILL_AMOUNT";
    private static final String KEY_WEEKLY = "key_BILL_WEEKLY";
    private static final String KEY_DAYOFMONTH = "key_BILL_DAYOFMONTH";
    private static final String KEY_DAYOFWEEK = "key_BILL_DAYOFWEEK";
    private static final String KEY_LASTPAID = "key_LASTPAID";
    private static final String KEY_DUEDATE = "key_DUEDATE";
    private static final String KEY_LASTBALANCE = "key_LASTBALANCE";
    private static final String KEY_CURRENTBALANCE = "key_CURRENTBALANCE";
    private static final String KEY_INCOMEAMOUNT = "key_INCOMEAMOUNT";
    private static final String KEY_INCOMEFREQUENCY = "key_INCOMEFREQUENCY";
    private static final String KEY_INCOMEDAYOFWEEK = "key_INCOMEDAYOFWEEK";
    private static final String KEY_INCOMEDAYOFMONTH = "key_INCOMEDAYOFMONTH";
    private static final String KEY_LASTPAY = "key_LASTPAY";
    private static final String KEY_NEXTPAY = "key_NEXTPAY";
    private static final String KEY_CUTOFF = "key_CUTOFF";
    private static Context context;

    private String indexesString;
    public ArrayList<String> indexes;
    public ArrayList<Bill> bills;
    public float lastBalance;
    public float currentBalance;
    public boolean payable = false;
    public Income income;
    public float overdraft = 0;
    public boolean showAds = false;

    private static SharedPreferences settings;
    private static SharedPreferences.Editor editor;

    private long msTime;

    private Date currentDate;





    private SimpleDateFormat formatter;

    private Calendar dueDateCalendar;

    private Calendar newDueDateCalendar;

    private Calendar currentCalendar;



    private UserPrefs(){

    }

    public static UserPrefs getInstance(Context ctx){


        context = ctx;
        if(userPrefs == null) userPrefs= new UserPrefs();

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();



        return userPrefs;
    }

    //balance methods

    public void payBill(int billId){
        for(Bill bill: bills){
            if(bill.getId() == billId){
                setCurrentBalance(-bill.getAmount());
                saveBalance();
                bill.setLastPaid(System.currentTimeMillis());
                newDueDateCalendar = new GregorianCalendar();
                newDueDateCalendar.setTimeInMillis(bill.getDueDate());
                if(bill.getWeeklyFlag()){
                    newDueDateCalendar.add(Calendar.WEEK_OF_MONTH, 1);
                    bill.setDueDate(newDueDateCalendar.getTimeInMillis());
                }else{
                    newDueDateCalendar.add(Calendar.MONTH,1);
                    bill.setDueDate(newDueDateCalendar.getTimeInMillis());
                }
                saveBills();
                break;
            }
        }
    }


    public void setCurrentBalance(float amount){
        lastBalance = currentBalance;
        currentBalance += amount;
    }

    public float getCurrentBalance(){
        return currentBalance;
    }

    public void saveBalance(){
        editor.putFloat(KEY_LASTBALANCE, lastBalance);
        editor.putFloat(KEY_CURRENTBALANCE, currentBalance);
        editor.commit();
    }

    public void loadBalance(){
        lastBalance = settings.getFloat(KEY_LASTBALANCE, 0);
        currentBalance = settings.getFloat(KEY_CURRENTBALANCE, 0);
    }

    //funds available calculation
    public float calcAvailableFunds(){
        overdraft = currentBalance;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        float fundsAvailable = currentBalance;
        Calendar cutOffCalendar = new GregorianCalendar();
        cutOffCalendar.setTimeInMillis(income.getCutOffDate());
        Calendar compareCalendar = new GregorianCalendar();
        Calendar nextPay = new GregorianCalendar();
        nextPay.setTimeInMillis(income.getNextPay());
        for(Bill bill:bills){
            compareCalendar.setTimeInMillis(bill.getDueDate());
            while(compareCalendar.before(cutOffCalendar)){
                if(bill.getWeeklyFlag()){
                    compareCalendar.add(Calendar.DAY_OF_MONTH, 7);
                }
                if(!bill.getWeeklyFlag()){
                    compareCalendar.add(Calendar.MONTH, 1);
                }
                fundsAvailable -= bill.getAmount();

            }
            compareCalendar.setTimeInMillis(bill.getDueDate());
            while(compareCalendar.before(nextPay)&&(!sdf.format(bill.getDueDate()).equals(sdf.format(income.getNextPay())))){
                if(bill.getWeeklyFlag()){
                    compareCalendar.add(Calendar.DAY_OF_MONTH, 7);
                }
                if(!bill.getWeeklyFlag()){
                    compareCalendar.add(Calendar.MONTH, 1);
                }
                overdraft -= bill.getAmount();
            }
        }

        compareCalendar.setTimeInMillis(income.getNextPay());
        while(compareCalendar.before(cutOffCalendar)){
            if(income.getWeeklyFlag()){

                compareCalendar.add(Calendar.DAY_OF_MONTH, 7);
            }
            if(!income.getWeeklyFlag()){
                compareCalendar.add(Calendar.MONTH, 1);
            }
            fundsAvailable += income.getAmount();
        }




        return fundsAvailable;
    }



    //bill methods

    public void checkPayable(Bill bill){
        Calendar current = new GregorianCalendar();
        Calendar due = new GregorianCalendar();
        due.setTimeInMillis(bill.getDueDate());

        if(bill.getWeeklyFlag()){
            current.add(Calendar.DAY_OF_WEEK,3);
            payable = current.after(due);
        }else{
            current.add(Calendar.WEEK_OF_MONTH,1);
            payable = current.after(due);
        }

    }

    private String getFieldKey (int id, String fieldKey){
        return KEY_PREFIX + id + fieldKey;
    }

    public void saveIndexes(){
        indexesString = "";
        for(String index: indexes){
            indexesString = indexesString + index +"##";
        }
        editor.putString(KEY_INDEXES, indexesString);
        editor.commit();
    }



    public void loadIndexes(){
        indexes = new ArrayList<String>();
        indexesString = settings.getString(KEY_INDEXES, "");

        if(indexesString.equals("")){
            // no indexes
        }else{
            String[] newindexes = indexesString.split("##");

            Collections.addAll(indexes, newindexes);

        }
    }

    public void deleteId(String index){

        String idToRemove=null;
        for(String id: indexes){
            if(id.equals(index)){
                idToRemove=id;

            }
        }
        indexes.remove(idToRemove);


    }

    public void addId(String index){
        indexes.add(index);



    }

    public String getFreeId(){
        if(indexes.size()>=1){

            int x = 1;
            for( String id: indexes){
                if(Integer.parseInt(id) >= x) x= Integer.parseInt(id)+1;
            }
            return String.valueOf( x);

        }else{
            return "1";
        }
    }

    public void loadBills() {
        bills = new ArrayList<Bill>();
        if(indexes.size() != 0){

            Bill bill;
            for(String id: indexes){
                bill = getBill(Integer.parseInt(id));
                bills.add(bill);
            }



        }
    }

    public void addBill(Bill bill){
        if (bills == null) bills = new ArrayList<Bill>();
        bills.add(bill);
    }

    public void saveBills(){
        for(Bill bill:bills){
            setBill(bill);
        }
    }
    public void setBill(Bill bill){
        if (bill == null) return;

        int id = bill.getId();
        editor.putString(getFieldKey(id,KEY_NAME), bill.getName());
        editor.putFloat(getFieldKey(id, KEY_AMOUNT), bill.getAmount());
        editor.putBoolean(getFieldKey(id, KEY_WEEKLY), bill.getWeeklyFlag());
        editor.putInt(getFieldKey(id, KEY_DAYOFMONTH), bill.getDayOfMonth());
        editor.putString(getFieldKey(id, KEY_DAYOFWEEK), bill.getDayOfWeek());
        editor.putLong(getFieldKey(id, KEY_LASTPAID), bill.getLastPaid());
        editor.putLong(getFieldKey(id, KEY_DUEDATE), bill.getDueDate());
        editor.commit();
    }

    public Bill getBill(int id){
        String name = settings.getString(getFieldKey(id, KEY_NAME), "");
        float amount = settings.getFloat(getFieldKey(id, KEY_AMOUNT), 0);
        boolean weeklyFlag = settings.getBoolean(getFieldKey(id, KEY_WEEKLY), false);
        int dayOfMonth = settings.getInt(getFieldKey(id, KEY_DAYOFMONTH), 1);
        String dayOfWeek = settings.getString(getFieldKey(id, KEY_DAYOFWEEK), "Monday");
        long lastPaid = settings.getLong(getFieldKey(id, KEY_LASTPAID), 1);
        long dueDate = settings.getLong(getFieldKey(id,KEY_DUEDATE), 1);

        return new Bill(id, name, amount, weeklyFlag, dayOfMonth, dayOfWeek, lastPaid, dueDate);

    }

    public void deleteBill(int id){
        editor.remove(getFieldKey(id, KEY_NAME));
        editor.remove(getFieldKey(id, KEY_AMOUNT));
        editor.remove(getFieldKey(id, KEY_WEEKLY));
        editor.remove(getFieldKey(id, KEY_DAYOFWEEK));
        editor.remove(getFieldKey(id, KEY_DAYOFMONTH));
        editor.remove(getFieldKey(id, KEY_LASTPAID));
        editor.remove(getFieldKey(id, KEY_DUEDATE));
        Bill billToRemove= null;
        for(Bill bill: bills){
            if( bill.getId() == id){
                billToRemove = bill;

            }
        }
        bills.remove(billToRemove);
        deleteId(Integer.toString(id));
    }
    //date methods

    public long getDueDate (boolean weekly, int dayNumber, String day){
        // get input from text fields and create due date
        dueDateCalendar = new GregorianCalendar();
        try
        {
            TimeUnit.MILLISECONDS.sleep(1);
        }
        catch (InterruptedException e)
        {}

        currentCalendar = new GregorianCalendar();


        if(weekly){
            int dayOfWeek = 0;
            if(day.equals(context.getString(R.string.sunday_string)))dayOfWeek = 1;
            if(day.equals(context.getString(R.string.monday_string)))dayOfWeek = 2;
            if(day.equals(context.getString(R.string.tuesday_string)))dayOfWeek = 3;
            if(day.equals(context.getString(R.string.wednesday_string)))dayOfWeek=4;
            if(day.equals(context.getString(R.string.thursday_string)))dayOfWeek=5;
            if(day.equals(context.getString(R.string.friday_string)))dayOfWeek=6;
            if(day.equals(context.getString(R.string.saturday_string)))dayOfWeek=7;

            dueDateCalendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
            if(dueDateCalendar.before(currentCalendar))

            {

                dueDateCalendar.add(Calendar.DAY_OF_WEEK, 7);

            }



        }else{
            dueDateCalendar.set(Calendar.DAY_OF_MONTH, dayNumber);
            if(dueDateCalendar.before(currentCalendar)){
                dueDateCalendar.add(Calendar.MONTH, 1);
            }


        }
        return dueDateCalendar.getTimeInMillis();

    }

    //Log methods

    public String getTimeStamp(boolean timeAndDate){

        msTime = System.currentTimeMillis();
        currentDate = new Date(msTime);
        if(timeAndDate){
            formatter = new SimpleDateFormat("yyyy/MM/dd hh:mm");
        }else{
            formatter = new SimpleDateFormat("yyyy/MM/dd");
        }
        String formattedDate = formatter.format(currentDate);

        return formattedDate;
    }

    public String getDateString(long time){
        Date date = new Date(time);
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(date);
    }

    public void writeLog(String entry){
        try{


            String filePath = "log.txt";

            entry = entry + "###";
            FileOutputStream fos = context.openFileOutput(filePath, Context.MODE_APPEND);
            fos.write(entry.getBytes());
            fos.close();

            //Toast.makeText(this, "log written", Toast.LENGTH_SHORT).show();

            //return true;

        }catch(IOException e){
            e.printStackTrace();
            //return false;
        }
    }

    public String[] readLog(String filter){
        String content = "empty";
        BufferedReader br = null;
        //File log = getDir("log.txt", MODE_APPEND);
        //if(!log.exists()){
        //return "no log";
        //}

        try{

            StringBuffer output = new StringBuffer();
            String filePath = "log.txt";

            FileInputStream fis = context.openFileInput(filePath);

            InputStreamReader isr = new InputStreamReader(fis);

            br = new BufferedReader(isr);
            String line = "";

            while((line=br.readLine()) != null){
                output.append(line);
            }

            content = output.toString();

        }catch(IOException e){
            content = context.getString(R.string.noLog_string);
        }

        String[] contentLines = content.split("###");
        if(!filter.equals("")){

            int c= 0;
            for(String line: contentLines){
                if(!line.contains(filter)){

                    contentLines[c] = "";
                }
                c++;
            }
        }

        return contentLines;
    }


    public void deleteLog(){
        context.deleteFile("log.txt");
    }



    //income methods

    public void loadIncome(){
        float amount = settings.getFloat(KEY_INCOMEAMOUNT, 0);
        boolean freq = settings.getBoolean(KEY_INCOMEFREQUENCY, false);
        int dom = settings.getInt(KEY_INCOMEDAYOFMONTH,1);
        int dow = settings.getInt(KEY_INCOMEDAYOFWEEK, 1);
        long lpay = settings.getLong(KEY_LASTPAY, 0);
        long npay = settings.getLong(KEY_NEXTPAY, 0);
        long cDate = settings.getLong(KEY_CUTOFF, 1);

        income = new Income(amount, freq, dom, dow, lpay, npay, cDate);
    }

    public void saveIncome(){
        editor.putFloat(KEY_INCOMEAMOUNT, income.getAmount());
        editor.putBoolean(KEY_INCOMEFREQUENCY, income.getWeeklyFlag());
        editor.putInt(KEY_INCOMEDAYOFMONTH, income.getDayOfMonth());
        editor.putInt(KEY_INCOMEDAYOFWEEK, income.getDayOfWeek());
        editor.putLong(KEY_LASTPAY, income.getLastPay());
        editor.putLong(KEY_NEXTPAY, income.getNextPay());
        editor.putLong(KEY_CUTOFF, income.getCutOffDate());
        editor.commit();
    }

    public Income getIncome(){
        return income;
    }

    public void setIncome(Income changedIncome){
        income = changedIncome;
    }

    public void receiveIncome(){
        Calendar currentDay =  new GregorianCalendar();
        Calendar incomeDay = new GregorianCalendar();
        incomeDay.setTimeInMillis(income.getNextPay());
        Calendar cutoffDay = new GregorianCalendar();
        cutoffDay.setTimeInMillis(income.getCutOffDate());
        if(currentDay.after(incomeDay)|| (currentDay.get(Calendar.DAY_OF_YEAR)== incomeDay.get(Calendar.DAY_OF_YEAR)&&currentDay.get(Calendar.YEAR)==incomeDay.get(Calendar.YEAR))){
            setCurrentBalance(income.getAmount());
            saveBalance();

            income.setLastPay(income.getNextPay());
            if(income.getWeeklyFlag()){
                incomeDay.add(Calendar.DAY_OF_YEAR, 7);
                income.setNextPay(incomeDay.getTimeInMillis());
                saveIncome();
            }else{
                incomeDay.add(Calendar.MONTH, 1);
                income.setNextPay(incomeDay.getTimeInMillis());
                saveIncome();
            }
            if(currentDay.get(Calendar.DAY_OF_YEAR)== cutoffDay.get(Calendar.DAY_OF_YEAR)&&currentDay.get(Calendar.YEAR)==cutoffDay.get(Calendar.YEAR)){
                cutoffDay.add(Calendar.MONTH, 1);
                income.setCutOffDate(cutoffDay.getTimeInMillis());
                saveIncome();
            }
        }
    }



}
