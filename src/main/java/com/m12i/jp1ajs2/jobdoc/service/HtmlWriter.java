package com.m12i.jp1ajs2.jobdoc.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

import com.m12i.jp1ajs2.jobdoc.Jobdoc;
import com.m12i.jp1ajs2.jobdoc.JobdocError;
import com.m12i.jp1ajs2.jobdoc.Messages;
import com.m12i.jp1ajs2.jobdoc.Parameters;
import com.m12i.jp1ajs2.unitdef.Unit;
import com.m12i.jp1ajs2.unitdef.UnitType;

/**
 * ドキュメント化を担当するオブジェクト.
 * テンプレート・ファイルはクラスパス上から検索される。
 */
public class HtmlWriter {
	
	/**
	 * テンプレート・ファイルのパスの接頭辞（ベース・ディレクトリのパス）.
	 */
	private static final String TEMPLATE_PATH_PREFIX = "templates/";
	/**
	 * テンプレート・ファイルのパスの接尾辞（ファイルの拡張子）.
	 */
	private static final String TEMPLATE_PATH_SUFFIX = ".html";
	/**
	 * テンプレート・エンジンの処理モード.
	 */
	private static final String TEMPLATE_MODE = "XHTML";
	/**
	 * 詳細情報ページ（右ペイン）のテンプレート名.
	 */
	private static final String DETAIL_TEMPLATE_NAME = "detail";
	/**
	 * ツリー表示ページ（左ペイン）のテンプレート名.
	 */
	private static final String TREE_TEMPLATE_NAME = "tree";
	/**
	 * フレームセット（親画面）のテンプレート名.
	 */
	private static final String INDEX_TEMPLATE_NAME = "index";
	
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
		ctx.setVariable("expl", ServiceProvider.getExplicator());
		return ctx;
	}
	
	private Map<String, UnitTypeStats> makeUnitTypeStats(final Unit target) {
		final Map<String, UnitTypeStats> statsList = new LinkedHashMap<String, HtmlWriter.UnitTypeStats>();
		for (final UnitType t: UnitType.values()) {
			statsList.put(t.getCode(), new UnitTypeStats(t));
		}
		for (final Unit u : ServiceProvider.getTraverser().makeFlattenedUnitList(target)) {
			statsList.get(u.getType().getCode()).addCount();
		}
		return statsList;
	}
	
	public static final class UnitTypeStats {
		private final String code;
		private final String desc;
		private int count = 0;
		public UnitTypeStats(final UnitType t) {
			this.code = t.getCode();
			this.desc = t.getDescription();
		}
		public int getCount() {
			return count;
		}
		public void addCount() {
			this.count ++;
		}
		public String getCode() {
			return code;
		}
		public String getDesc() {
			return desc;
		}
	}

	/**
	 * ドキュメントをレンダリングする.
	 * @param target 対象のユニット
	 * @param engine テンプレート・エンジン
	 * @param params パラメータ
	 */
	public void renderHtml(final Unit target, final TemplateEngine engine, final Parameters params) {
		final Traverser trav = ServiceProvider.getTraverser();
		
		// ユニット名を使ってディレクトリを作成
		final File baseDir = new File(params.getDestinationDirectory(), target.getName());
		baseDir.mkdir();

		// ツリー構造のユニット定義情報をリスト構造に変換
		final List<Unit> flattenedList = trav.makeFlattenedUnitList(target);
		// コンテキストを初期化
		final Context ctx = makeContext();
		// 種々のテンプレート変数を追加
		ctx.setVariable("root", target);
		ctx.setVariable("flattenedList", flattenedList);
		ctx.setVariable("maxDepth", trav.measureMaxDepth(target));
		ctx.setVariable("maxArea", trav.measureMaxArea(flattenedList));
		ctx.setVariable("unitTypeStats", makeUnitTypeStats(target));
		
		// ドキュメントのレンダリング処理を行う
		try {
			final Writer index = new FileWriter(new File(baseDir, "index.html"));
			engine.process(INDEX_TEMPLATE_NAME, ctx, index);
			index.close();
			
			final Writer tree = new FileWriter(new File(baseDir, "tree.html"));
			engine.process(TREE_TEMPLATE_NAME, ctx, tree);
			tree.close();
			
			final Writer detail = new FileWriter(new File(baseDir, "detail.html"));
			engine.process(DETAIL_TEMPLATE_NAME, ctx, detail);
			detail.close();
			
		} catch (final IOException e) {
			// IOエラーが発生した場合はアベンド
			throw new JobdocError(Messages.UNEXPECTED_ERROR_HAS_OCCURED, e);
		}
	}
}
