/*
 * 类说明：网路连接、数据发送、小文件发送的封装
 * 编写的人：Giam
 * 时间：2014/07/09
 */
package net.carslink.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class HttpPostUtil {
	
	URL url;
	HttpURLConnection conn;
	String boundary = UUID.randomUUID().toString();//随机生成边界标识
	Map<String, String> textParams = new HashMap<String, String>();
	Map<String, File> fileparams = new HashMap<String, File>();
	Map<String, byte[]> filebyte = new HashMap<String, byte[]>();
	DataOutputStream ds;

	public HttpPostUtil(String url) throws Exception {
		this.url = new URL(url);
	}

	// 添加文本参数
	public void addTextParameter(String name, String value) {
		textParams.put(name, value);
	}

	// 添加文件
	public void addFileParameter(String name, File value) {
		fileparams.put(name, value);
	}
	
	//添加文件字节
	public void addFileByte(String name, byte[] buffer) {
		filebyte.put(name, buffer);
	}
	
	public void addHasMapTextParameter(HashMap<String, String> infos){
		textParams = infos;
	}
	
	public void addHasMapFileParameter(HashMap<String, File> files){
		fileparams = files;
	}
	
	public void addHasMapByteParameter(HashMap<String, byte[]> bytes){
		filebyte = bytes;
	}
	
	// 清除文本参数
	public void clearAllParameters() {
		textParams.clear();
		fileparams.clear();
		filebyte.clear();
	}

	//发�?数据到服务器
	public HttpURLConnection send() throws Exception {
		initConnection();
		try {
			conn.connect();
		} catch (SocketTimeoutException e) {
			// something
			e.printStackTrace();
			throw new RuntimeException();
		}
		ds = new DataOutputStream(conn.getOutputStream());
		writeFileParams();
		writeStringParams();
		writeFileByte();
		paramsEnd();
		
		String cookie=conn.getHeaderField("Set-Cookie");
		if(cookie!=null){
			//PowerOnActivity.sessionID=cookie.substring(0, cookie.indexOf(";"));		
		}
		conn.getInputStream();
		
		return conn;
	}

	public byte[] rtdata(InputStream in) throws IOException{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int b;
		while ((b = in.read()) != -1) {
			out.write(b);
		}
		in.close();
		conn.disconnect();
		return out.toByteArray();
	}
	
	//初始化网络连�?	
	private void initConnection() throws Exception {
		conn = (HttpURLConnection) this.url.openConnection();
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setRequestProperty("Connection", "Keep-Alive");
		conn.setConnectTimeout(50000);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Charset", "UTF-8");
		conn.setRequestProperty("Content-Type","multipart/form-data; boundary=" + boundary);	
		
		//conn.setRequestProperty("Cookie", PowerOnActivity.sessionID);
	}

	//写入报头参数
	private void writeStringParams() throws Exception {
		Set<String> keySet = textParams.keySet();
		for (Iterator<String> it = keySet.iterator(); it.hasNext();) {
			String name = it.next();
			String value = textParams.get(name);
			ds.writeBytes("--" + boundary + "\r\n");
			ds.writeBytes("Content-Disposition: form-data; name=\"" + name
					+ "\"\r\n");
			ds.writeBytes("\r\n");
			ds.writeBytes(encode(value) + "\r\n");
		}
	}
	
	private void writeFileByte() throws Exception{
		Set<String> keySet = filebyte.keySet();
		for (Iterator<String> it = keySet.iterator(); it.hasNext();) {
			String name = it.next();
			byte[] value = filebyte.get(name);
			ds.writeBytes("--" + boundary + "\r\n");
			ds.writeBytes("Content-Disposition: form-data; name=\"" + name
					+ "\"; filename=\"" + encode(name) + "\"\r\n");
			ds.writeBytes("Content-Type: " + getContentType1(value) + "\r\n");
			ds.writeBytes("\r\n");
			ds.write(value);
			ds.writeBytes("\r\n");
		}
	}

	//写入文件
	private void writeFileParams() throws Exception {
		Set<String> keySet = fileparams.keySet();
		for (Iterator<String> it = keySet.iterator(); it.hasNext();) {
			String name = it.next();
			File value = fileparams.get(name);
			ds.writeBytes("--" + boundary + "\r\n");
			ds.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + encode(value.getName()) + "\"\r\n");
			ds.writeBytes("Content-Type: " + getContentType(value) + "\r\n");
			ds.writeBytes("\r\n");
			ds.write(getBytes(value));
			ds.writeBytes("\r\n");
		}
	}

	private String getContentType(File f) {
		return "application/octet-stream";
	}
	
	private String getContentType1(byte[] b) {
		return "application/octet-stream";
	}

	//文件转为byte
	private byte[] getBytes(File f) throws Exception {
		FileInputStream in = new FileInputStream(f);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] b = new byte[1024];
		int n;
		while ((n = in.read(b)) != -1) {
			out.write(b, 0, n);
		}
		in.close();
		return out.toByteArray();
	}

	//表格结尾
	private void paramsEnd() throws Exception {
		ds.writeBytes("--" + boundary + "--" + "\r\n");
		ds.writeBytes("\r\n");
	}

	//设置编码
	private String encode(String value) throws Exception {
		return URLEncoder.encode(value, "UTF-8");
	}

}
