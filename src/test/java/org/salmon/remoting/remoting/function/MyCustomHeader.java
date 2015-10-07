package org.salmon.remoting.remoting.function;

import org.salmon.remoting.CommandCustomHeader;

public class MyCustomHeader implements CommandCustomHeader {
	private String first;
	private String second;
	public String getFirst() {
		return first;
	}
	@Override
	public String toString() {
		return "MyCustomHeader [first=" + first + ", second=" + second + "]";
	}
	public void setFirst(String first) {
		this.first = first;
	}
	public String getSecond() {
		return second;
	}
	public void setSecond(String second) {
		this.second = second;
	}
}
