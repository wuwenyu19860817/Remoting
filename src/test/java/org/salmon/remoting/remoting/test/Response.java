package org.salmon.remoting.remoting.test;

public class Response {
	@Override
	public String toString() {
		return "Response [codeVersion=" + codeVersion + ", resId=" + resId + ", body=" + body + "]";
	}
	private String codeVersion = null;
	private String resId = "resId"; 
	public String getCodeVersion() {
		return codeVersion;
	}
	public void setCodeVersion(String codeVersion) {
		this.codeVersion = codeVersion;
	}
	private String body = "res body";
	public String getResId() {
		return resId;
	}
	public void setResId(String resId) {
		this.resId = resId;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
}