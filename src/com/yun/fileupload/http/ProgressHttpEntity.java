package com.yun.fileupload.http;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

/**
 * 可监听进度的httpentity
 * 
 * @author yunye
 */
public class ProgressHttpEntity extends HttpEntityWrapper {
	private ProgressListenter progressListenter;
	private long totalSize;

	public ProgressHttpEntity(HttpEntity wrapped) {
		super(wrapped);
		totalSize = wrapped.getContentLength();
	}

	public static class CountingOutputStream extends FilterOutputStream {
		private final ProgressListenter listener;
		private long transferred;
		private long totalSize;

		CountingOutputStream(final OutputStream out,
				final ProgressListenter listener, long totalSize) {
			super(out);
			this.listener = listener;
			this.transferred = 0;
			this.totalSize = totalSize;
		}

		@Override
		public void write(final byte[] b, final int off, final int len)
				throws IOException {
			out.write(b, off, len);
			this.transferred += len;
			if (this.listener != null) {
				this.listener
						.progressChange((int) (100 * this.transferred / this.totalSize));
			}
		}

		@Override
		public void write(final int b) throws IOException {
			out.write(b);
			this.transferred++;
			if (this.listener != null) {
				this.listener
						.progressChange((int) (100 * this.transferred / this.totalSize));
			}
		}

	}

	@Override
	public void writeTo(final OutputStream out) throws IOException {
		this.wrappedEntity.writeTo(out instanceof CountingOutputStream ? out
				: new CountingOutputStream(out, this.progressListenter,
						totalSize));
	}

	public void setProgressListenter(ProgressListenter progressListenter) {
		this.progressListenter = progressListenter;
	}

	public interface ProgressListenter {
		public void progressChange(int progress);
		public void success();
		public void error();
	}

}
