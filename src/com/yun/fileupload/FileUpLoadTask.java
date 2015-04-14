package com.yun.fileupload;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;

import android.os.AsyncTask;

import com.yun.fileupload.http.ProgressHttpEntity;
import com.yun.fileupload.http.ProgressHttpEntity.ProgressListenter;

/**
 * 文件上传Task 提供上传进度监听
 * 
 * @author yunye
 * 
 */
public class FileUpLoadTask extends AsyncTask<Integer, Integer, Integer> {
	private MultipartEntityBuilder builder;
	private ProgressListenter progressListenter;
	private static final int SUCCESS = 0;
	private static final int ERROR = 1;
	private String url;

	public FileUpLoadTask(String url) {
		builder = MultipartEntityBuilder.create();
		this.url = url;
	}

	@Override
	protected Integer doInBackground(Integer... params) {
		ProgressHttpEntity entity = new ProgressHttpEntity(builder.build());
		entity.setProgressListenter(progressListenter);
		return uploadFile(url, entity);
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		if (progressListenter != null) {
			if (result == SUCCESS) {
				progressListenter.success();
			} else {
				progressListenter.error();
			}
		}
	}

	public void setProgressListenter(ProgressListenter progressListenter) {
		this.progressListenter = progressListenter;
	}

	public void addPart(String name, String text) {
		builder.addTextBody(name, text);
	}

	public void addPart(String name, File file) {
		builder.addBinaryBody(name, file);
	}

	public void addPart(String name, InputStream stream) {
		builder.addBinaryBody(name, stream);
	}

	public void addPart(String name, byte[] b) {
		builder.addBinaryBody(name, b);
	}

	public void setCharset(Charset charset) {
		builder.setCharset(charset);
	}

	public void setMode(HttpMultipartMode mode) {
		builder.setMode(mode);
	}

	private int uploadFile(String url, ProgressHttpEntity entity) {
		HttpClient httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter(
				CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		httpClient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(entity);
		try {
			HttpResponse httpResponse = httpClient.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				return SUCCESS;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (httpClient != null && httpClient.getConnectionManager() != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}
		return ERROR;
	}

}
