package org.doogwood.jp1ajs2.jobdoc;

/**
 * 警告終了のトリガーとなる例外オブジェクト.
 */
public class JobdocWarning extends RuntimeException {
	private static final long serialVersionUID = -3982064836977789471L;
	
	public JobdocWarning(final String m) {
		super(m);
	}
	
	public JobdocWarning(final String m, final Throwable cause) {
		super(m, cause);
	}
}
