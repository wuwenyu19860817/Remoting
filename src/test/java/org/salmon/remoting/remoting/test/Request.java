package org.salmon.remoting.remoting.test;

public class Request {
	private String sid = "sid";
	@Override
	public String toString() {
		return "Request [sid=" + sid + ", body=" + body + "]";
	}
	public String getSid() {
		return sid;
	}
	public void setSid(String sid) {
		this.sid = sid;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	private String body = "request body";
}