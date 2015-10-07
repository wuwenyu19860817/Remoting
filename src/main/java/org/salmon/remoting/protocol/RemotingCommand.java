package org.salmon.remoting.protocol;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.salmon.remoting.CommandCustomHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端服务端交互对象
 */
public class RemotingCommand {

	private static final Logger log = LoggerFactory.getLogger(RemotingCommand.class);

	public static final String defaultVersion = "0.0.0";

	public static final String splitVersion = "-";

	/**
	 * 序列号发生器
	 */
	private static AtomicInteger RequestId = new AtomicInteger(0);

	/**
	 * 占低位 00(双向请求) 01(双向响应) 10(单向请求)
	 */
	private static final int RPC_TYPE = 0;

	/**
	 * 占高位 00(双向请求) 01(双向响应) 10(单向请求)
	 */
	private static final int RPC_ONEWAY = 1;

	/**
	 * Header 部分
	 */
	private int code;// RPC服务编号code 或 响应错误码
	private String version = defaultVersion;// 请求版本号(默认单版本部署版本号0.0.0)
	private int opaque = RequestId.getAndIncrement();// 请求序列号
	private int flag = 0;// 二进制表示请求响应和单向双向
	private String remark;// 响应的备注
	private HashMap<String, String> extFields;// customHeader转换而来

	private transient CommandCustomHeader customHeader;// 自定义消息头不参与传输

	/**
	 * Body 部分
	 */
	private transient byte[] body;

	private RemotingCommand() {
	}

	public static RemotingCommand createRequestCommand(int code, CommandCustomHeader customHeader, byte[] body) {
		return createRequestCommand(code, defaultVersion, customHeader, body);
	}

	public static RemotingCommand createRequestCommand(int code, byte[] body) {
		return createRequestCommand(code, null, null, body);
	}

	public static RemotingCommand createRequestCommand(int code, String version, byte[] body) {
		return createRequestCommand(code, version, null, body);
	}

	public static RemotingCommand createRequestCommand(int code, String version, CommandCustomHeader customHeader,
			byte[] body) {
		RemotingCommand cmd = new RemotingCommand();
		cmd.setCode(code);
		cmd.setVersion(version);
		cmd.customHeader = customHeader;
		cmd.setBody(body);
		return cmd;
	}

	public static RemotingCommand createResponseCommand(int code, String remark) {
		RemotingCommand cmd = new RemotingCommand();
		cmd.markResponseType();
		cmd.setCode(code);
		cmd.setRemark(remark);
		return cmd;
	}

	private byte[] buildHeader() {
		if (this.customHeader != null) {
			Field[] fields = this.customHeader.getClass().getDeclaredFields();
			if (null == this.extFields) {
				this.extFields = new HashMap<String, String>();
			}

			for (Field field : fields) {
				if (!Modifier.isStatic(field.getModifiers())) {
					String name = field.getName();
					if (!name.startsWith("this")) {
						Object value = null;
						try {
							field.setAccessible(true);
							value = field.get(this.customHeader);
						} catch (Throwable e) {
							log.error("RemotingCommand buildHeader error.", e);
						}

						if (value != null) {
							this.extFields.put(name, value.toString());
						}
					}
				}
			}
		}
		return RemotingSerializable.encode(this);
	}

	public ByteBuffer encodeHeader() {
		int bodyLength = this.body != null ? this.body.length : 0;
		// 1> header length size
		int length = 4;

		// 2> header data length
		byte[] headerData = this.buildHeader();
		length += headerData.length;

		// 3> body data length
		length += bodyLength;

		ByteBuffer result = ByteBuffer.allocate(4 + length - bodyLength);

		// length
		result.putInt(length);

		// header length
		result.putInt(headerData.length);

		// header data
		result.put(headerData);

		result.flip();

		return result;
	}

	public static RemotingCommand decode(final ByteBuffer byteBuffer) {
		int length = byteBuffer.limit();
		int headerLength = byteBuffer.getInt();

		byte[] headerData = new byte[headerLength];
		byteBuffer.get(headerData);

		int bodyLength = length - 4 - headerLength;
		byte[] bodyData = null;
		if (bodyLength > 0) {
			bodyData = new byte[bodyLength];
			byteBuffer.get(bodyData);
		}

		RemotingCommand cmd = RemotingSerializable.decode(headerData, RemotingCommand.class);
		cmd.body = bodyData;
		return cmd;
	}

	public void markResponseType() {
		int bits = 1 << RPC_TYPE;
		this.flag |= bits;
	}

	public boolean isResponseType() {
		int bits = 1 << RPC_TYPE;
		return (this.flag & bits) == bits;
	}

	public void markOnewayRPC() {
		int bits = 1 << RPC_ONEWAY;
		this.flag |= bits;
	}

	public boolean isOnewayRPC() {
		int bits = 1 << RPC_ONEWAY;
		return (this.flag & bits) == bits;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public RemotingCommandType getType() {
		if (this.isResponseType()) {
			return RemotingCommandType.RESPONSE_COMMAND;
		}
		return RemotingCommandType.REQUEST_COMMAND;
	}

	public int getOpaque() {
		return opaque;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public HashMap<String, String> getExtFields() {
		return extFields;
	}

	public void setExtFields(HashMap<String, String> extFields) {
		this.extFields = extFields;
	}

	public String getRemark() {
		return remark;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}

	public void setOpaque(int opaque) {
		this.opaque = opaque;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public byte[] getBody() {
		return body;
	}

	@Override
	public String toString() {
		return "RemotingCommand [code=" + code + ", version=" + version + ", opaque=" + opaque + ", flag(B)="
				+ Integer.toBinaryString(flag) + ", remark=" + remark + ", extFields=" + extFields + "]";
	}

	public enum RemotingCommandType {
		REQUEST_COMMAND, RESPONSE_COMMAND;
	}

	public class RemotingSysResponseCode {
		// 成功
		public static final int SUCCESS = 0;
		// 发生了未捕获异常
		public static final int SYSTEM_ERROR = 1;
		// 由于线程池拥堵，系统繁忙
		public static final int SYSTEM_BUSY = 2;
		// 请求代码不支持
		public static final int REQUEST_CODE_NOT_SUPPORTED = 3;
	}

}
