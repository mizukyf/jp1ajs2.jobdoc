package com.m12i.jp1ajs2.jobdoc.service;

import java.util.Date;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

import com.m12i.jp1ajs2.jobdoc.Jobdoc;
import com.m12i.jp1ajs2.jobdoc.Parameters;

import com.m12i.jp1ajs2.unitdef.Unit;
import com.m12i.jp1ajs2.unitdef.MapSize;
import com.m12i.jp1ajs2.unitdef.Params;
import com.m12i.jp1ajs2.unitdef.util.Maybe;

public class SvgWriter {
	/**
	 * テンプレート・ファイルのパスの接頭辞（ベース・ディレクトリのパス）.
	 */
	private static final String TEMPLATE_PATH_PREFIX = "templates/";
	/**
	 * テンプレート・ファイルのパスの接尾辞（ファイルの拡張子）.
	 */
	private static final String TEMPLATE_PATH_SUFFIX = ".svg";
	/**
	 * テンプレート・エンジンの処理モード.
	 */
	private static final String TEMPLATE_MODE = "XML";
	
	/**
	 * ユニット定義をもとに各種情報を収集するサービス・クラス.
	 */
	private final Traverser trav = new Traverser();
	
	/**
	 * Thymeleafテンプレート・エンジンを初期化する.
	 * @return テンプレート・エンジン
	 */
	public TemplateEngine initializeTemplateEngine() {
		// エンジンをインスタンス化
		final TemplateEngine engine = new TemplateEngine();
		// テンプレート解決子をインスタンス化
		final TemplateResolver resolver = new ClassLoaderTemplateResolver();
		// 種々の設定を行う
		resolver.setPrefix(TEMPLATE_PATH_PREFIX);
		resolver.setSuffix(TEMPLATE_PATH_SUFFIX);
		resolver.setTemplateMode(TEMPLATE_MODE);
		engine.setTemplateResolver(resolver);
		return engine;
	}

	/**
	 * ドキュメントをレンダリングする際に使用するコンテキストを初期化する.
	 * @return コンテキスト
	 */
	public Context makeContext() {
		final Context ctx = new Context();
		ctx.setVariable("applicationName", Jobdoc.APPLICATION_NAME);
		ctx.setVariable("applicationVersion", Jobdoc.APPLICATION_VERSION);
		ctx.setVariable("generatedAt", new Date());
		return ctx;
	}
	
	/**
	 * ドキュメントをレンダリングする.
	 * @param target 対象のユニット
	 * @param engine テンプレート・エンジン
	 * @param params パラメータ
	 */
	public void renderSvg(final Unit root, final TemplateEngine engine, final Parameters params) {
		// TOOD
		
		for (final Unit u : trav.makeFlattenedUnitList(root)) {
			// マップサイズ情報の取得を試みる
			final Maybe<MapSize> size = Params.getMapSize(u);
			if (size.isNothing()) {
				// マップサイズを持たないユニットはレンダリング対象外
				continue;
			}
		}
	}
}
