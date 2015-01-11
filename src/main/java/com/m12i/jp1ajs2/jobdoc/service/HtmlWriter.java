package com.m12i.jp1ajs2.jobdoc.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
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
	 * ユニット定義一覧のテンプレート名.
	 */
	private static final String UNIT_LIST_TEMPLATE_NAME = "unit_list";
	
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
		resolver.setCharacterEncoding("utf-8");
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
	 * ユニット種別統計を生成して返す.
	 * @param target ドキュメント化対象の基底となるユニット定義
	 * @return ユニット種別統計
	 */
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
	
	/**
	 * ユニット種別統計.
	 */
	public static final class UnitTypeStats {
		private final String code;
		private final String desc;
		private int count = 0;
		public UnitTypeStats(final UnitType t) {
			this.code = t.getCode();
			this.desc = t.getDescription();
		}
		/**
		 * 当該ユニット種別の出現回数を取得する.
		 * @return 出現回数
		 */
		public int getCount() {
			return count;
		}
		/**
		 * 当該ユニット種別の出現回数をカウントアップする.
		 */
		public void addCount() {
			this.count ++;
		}
		/**
		 * 当該ユニット種別のコード値（{@code "g"}・{@code "n"}など）を返す.
		 * @return コード値
		 */
		public String getCode() {
			return code;
		}
		/**
		 * 当該ユニット種別の説明テキストを返す.
		 * @return 説明テキスト
		 */
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
		ctx.setVariable("tmplFunc", new TemplateFunctions(params, target));
		ctx.setVariable("root", target);
		ctx.setVariable("flattenedList", flattenedList);
		ctx.setVariable("maxDepth", trav.measureMaxDepth(target));
		ctx.setVariable("maxArea", trav.measureMaxArea(flattenedList));
		ctx.setVariable("unitTypeStats", makeUnitTypeStats(target));
		
		// ドキュメントのレンダリング処理を行う
		try {
			final Writer index = makeWriter(baseDir, "index.html");
			engine.process(INDEX_TEMPLATE_NAME, ctx, index);
			index.close();
			
			final Writer tree = makeWriter(baseDir, "tree.html");
			engine.process(TREE_TEMPLATE_NAME, ctx, tree);
			tree.close();
			
			final Writer detail = makeWriter(baseDir, "detail.html");
			engine.process(DETAIL_TEMPLATE_NAME, ctx, detail);
			detail.close();
			
		} catch (final IOException e) {
			// IOエラーが発生した場合はアベンド
			throw new JobdocError(Messages.UNEXPECTED_ERROR_HAS_OCCURED, e);
		}
	}
	
	public void renderHtmlList(final List<Unit> list, final TemplateEngine engine, final Parameters params) {
		final File baseDir = params.getDestinationDirectory();
		// コンテキストを初期化
		final Context ctx = makeContext();
		ctx.setVariable("list", list);
		// ドキュメントのレンダリング処理を行う
		try {
			final Writer index = makeWriter(baseDir, "unit_list.html");
			engine.process(UNIT_LIST_TEMPLATE_NAME, ctx, index);
			index.close();
			
		} catch (final IOException e) {
			// IOエラーが発生した場合はアベンド
			throw new JobdocError(Messages.UNEXPECTED_ERROR_HAS_OCCURED, e);
		}
	}
	
	private Writer makeWriter(final File baseDir, final String fileName) throws FileNotFoundException {
		return new BufferedWriter(
				new OutputStreamWriter(
						new FileOutputStream(
								new File(baseDir, fileName)),
								Charset.forName("utf-8")));
	}
}
