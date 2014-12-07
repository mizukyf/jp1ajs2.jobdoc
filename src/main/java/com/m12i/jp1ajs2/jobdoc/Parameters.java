package com.m12i.jp1ajs2.jobdoc;

import java.io.File;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * アプリケーションの動作を制御するパラメータを格納するオブジェクト.
 */
public class Parameters {
	/**
	 * 読み取り対象のユニット定義ファイルのパス.
	 */
	private File sourceFile = null;
	/**
	 * ドキュメントを出力するディレクトリのパス.
	 */
	private File destinationDirectory = new File(".");
	/**
	 * ドキュメント化する対象のユニット名.
	 */
	private String targetUnitName = null;
	/**
	 * ドキュメント化する対象のユニット名のパターン.
	 */
	private Pattern targetUnitNamePattern = null;
	/**
	 * 読み取り対象のユニット定義ファイルのキャラクタセット.
	 */
	private Charset sourceFileCharset = Charset.forName("Windows-31J");

	public File getSourceFile() {
		return sourceFile;
	}
	public void setSourceFile(File sourceFile) {
		this.sourceFile = sourceFile;
	}
	public File getDestinationDirectory() {
		return destinationDirectory;
	}
	public void setDestinationDirectory(File destinationDirectory) {
		this.destinationDirectory = destinationDirectory;
	}
	public String getTargetUnitName() {
		return targetUnitName;
	}
	public void setTargetUnitName(String targetUnitName) {
		this.targetUnitName = targetUnitName;
	}
	public Pattern getTargetUnitNamePattern() {
		return targetUnitNamePattern;
	}
	public void setTargetUnitNamePattern(Pattern targetUnitNamePattern) {
		this.targetUnitNamePattern = targetUnitNamePattern;
	}
	public Charset getSourceFileCharset() {
		return sourceFileCharset;
	}
	public void setSourceFileCharset(Charset sourceFileCharset) {
		this.sourceFileCharset = sourceFileCharset;
	}
	
	@Override
	public String toString() {
		return String.format("%s {destinationDirectory: %s, "
				+ "sourceFile: %s, "
				+ "sourceFileCharset: %s, "
				+ "targetUnitName: %s, "
				+ "targetUnitNamePattern: %s}",
				this.getClass().getCanonicalName(),
				this.destinationDirectory,
				this.sourceFile,
				this.sourceFileCharset,
				this.targetUnitName,
				this.targetUnitNamePattern);
	}
}
