package com.m12i.jp1ajs2.jobdoc;

/**
 * 異常終了のトリガーとなるオブジェクト.
 */
public class JobdocError extends RuntimeException {
	private static final long serialVersionUID = -3982064836977789474L;
	
	public JobdocError(final String m) {
		super(m);
	}
	
	public JobdocError(final String m, final Throwable cause) {
		super(m, cause);
	}
}
