package net.vertexgraphics.myfinances;


public class Bill
{
    private int id;
    private String name;
    private float amount;
    private boolean weekly;
    private int dayOfMonth;
    private String dayOfWeek;
    private long lastPaid;
    private long dueDate;

    public Bill(int id, String name,float amount, boolean weekly, int dayOfMonth, String dayOfWeek, long lastPaid, long dueDate){
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.weekly = weekly;
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek = dayOfWeek;
        this.lastPaid = lastPaid;
        this.dueDate = dueDate;
    }

    public void setId(int id){
        this.id = id;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setAmount(float amount){
        this.amount = amount;
    }

    public void setWeeklyFlag(boolean weekly){
        this.weekly = weekly;
    }

    public void setDayOfMonth(int dayOfMonth){
        this.dayOfMonth = dayOfMonth;
    }

    public void setDayOfWeek(String dayOfWeek){
        this.dayOfWeek = dayOfWeek;
    }

    public void setLastPaid(long lastPaid){
        this.lastPaid = lastPaid;
    }

    public void setDueDate(long dueDate){
        this.dueDate = dueDate;
    }

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public float getAmount(){
        return amount;
    }

    public boolean getWeeklyFlag(){
        return weekly;
    }

    public int getDayOfMonth(){
        return dayOfMonth;
    }

    public String getDayOfWeek(){
        return dayOfWeek;
    }

    public long getLastPaid(){
        return lastPaid;
    }

    public long getDueDate(){
        return dueDate;
    }

    public String getDetails(){
        //todo
        return "";
    }
}
