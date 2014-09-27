package me.darkwiiplayer.trenni;

public class PurseEntry {
	private String coin;
	private int value;
	
	public PurseEntry(String _coin, int _value) {
		coin = _coin;
		value = _value;
	}
	
	public String getName() {
		return coin;
	}
	public Integer getAmount() {
		return value;
	}
	public String getInfo() {
		return coin + ": " + value;
	}
}
