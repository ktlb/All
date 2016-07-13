package proxy;

public abstract class HttpLine {
	protected String protocol;
	protected String method;
	protected String url;
	protected String httpLine;
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getHttpLine() {
		return httpLine;
	}
	public void setHttpLine(String httpLine) {
		this.httpLine = httpLine;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	protected String description;
	protected String statusCode;
	
	public byte[] getBytes() {
		return toString().getBytes();
	}
}
