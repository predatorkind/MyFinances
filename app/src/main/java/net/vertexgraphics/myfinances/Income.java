package net.vertexgraphics.myfinances;

public class Income
{
	private float amount;
	private boolean weekly;
	private int dayOfMonth;
	private int dayOfWeek;
	private long lastPay; 
	private long nextPay;
	private long cutoffDate;

	public Income(float amount, boolean weekly, int dayOfMonth, int dayOfWeek, long lastPay, long nextPay, long cutoffDate){

		this.amount = amount;
		this.weekly = weekly;
		this.dayOfMonth = dayOfMonth;
		this.dayOfWeek = dayOfWeek;
		this.lastPay = lastPay;
		this.nextPay = nextPay;
		this.cutoffDate = cutoffDate;
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

	public void setDayOfWeek(int dayOfWeek){
		this.dayOfWeek = dayOfWeek;
	}

	public void setLastPay(long lastPaid){
		this.lastPay = lastPaid;
	}

	public void setNextPay(long dueDate){
		this.nextPay = dueDate;
	}
	
	public void setCutOffDate(long cutOff){
		this.cutoffDate = cutOff;
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

	public int getDayOfWeek(){
		return dayOfWeek;
	}

	public long getLastPay(){
		return lastPay;
	}

	public long getNextPay(){
		return nextPay;
	}
	
	public long getCutOffDate(){
		return cutoffDate;
	}
}
