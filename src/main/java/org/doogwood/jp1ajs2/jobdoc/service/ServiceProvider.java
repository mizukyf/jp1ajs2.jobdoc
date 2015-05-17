package org.doogwood.jp1ajs2.jobdoc.service;

import org.doogwood.jp1ajs2.jobdoc.Jobdoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServiceProvider {
	private ServiceProvider() {}
	
	/**
	 * アプリケーション固有のロガー.
	 * ログ出力設定は`log4j.properties`で行う。
	 */
	private static final Logger logger = LoggerFactory.getLogger(Jobdoc.class);
	/**
	 * 設定情報関連を担当するサービス・クラス.
	 */
	private static final Configurer config = new Configurer();
	/**
	 * ユニット定義パースを担当するサービス・クラス.
	 */
	private static final Parser pars = new Parser();
	/**
	 * ユニット定義をもとに各種情報を収集するサービス・クラス.
	 */
	private static final Traverser trav = new Traverser();
	/**
	 * ドキュメント化のHTML部分を担当するサービス・クラス.
	 */
	private static final HtmlWriter html = new HtmlWriter();
	/**
	 * ドキュメント化のSVG部分を担当するサービス・クラス.
	 */
	private static final SvgWriter svg = new SvgWriter();
	/**
	 * ユニット定義パラメータ値の解説情報を提供するサービス・クラス.
	 */
	private static final Explicator expl = new Explicator();
	
	public static Parser getParser() {
		return pars;
	}
	public static Traverser getTraverser() {
		return trav;
	}
	public static HtmlWriter getHtmlWriter() {
		return html;
	}
	public static Configurer getConfigurer() {
		return config;
	}
	public static SvgWriter getSvgWriter() {
		return svg;
	}
	public static Explicator getExplicator() {
		return expl;
	}
	public static Logger getLogger() {
		return logger;
	}
}
