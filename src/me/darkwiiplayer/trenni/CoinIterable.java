package me.darkwiiplayer.trenni;

import java.util.Iterator;

public class CoinIterable implements Iterable<Object>{
	public Iterator<Object> iterator() {
		return CoinType.iterator();
	}
}
