package com.m12i.jp1ajs2.jobdoc.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import com.m12i.jp1ajs2.unitdef.AnteroposteriorRelationship;
import com.m12i.jp1ajs2.unitdef.Element;
import com.m12i.jp1ajs2.unitdef.Unit;
import com.m12i.jp1ajs2.unitdef.MapSize;
import com.m12i.jp1ajs2.unitdef.Params;
import com.m12i.jp1ajs2.unitdef.UnitConnectionType;
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
	 * マス目の半径.
	 */
	private static final int GRID_R = 60;
	/**
	 * マス目の直径
	 */
	private static final int GRID_DIA = GRID_R * 2;
	/**
	 * 関連線の始点・終点とグリッドの中心の間に設けるマージン.
	 */
	private static final int MARGIN_R = GRID_R - 10;
	
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
	 * ドキュメントをレンダリングする.
	 * @param target 対象のユニット
	 * @param engine テンプレート・エンジン
	 * @param params パラメータ
	 */
	public void renderSvg(final Unit root, final TemplateEngine engine, final Parameters params) {
		// コンテキストを初期化
		final Context ctx = this.makeContext();
		ctx.setVariable("tmplFunc", new TemplateFunctions(params, root));
		
		for (final Unit u : ServiceProvider.getTraverser().makeFlattenedUnitList(root)) {
			// マップサイズ情報を取得
			final Maybe<MapSize> size = Params.getMapSize(u);

			if (size.isNothing()) {
				// マップサイズを持たないものはレンダリング対象外
				continue;
			}
			
			// マップのタテ・ヨコを計算してテンプレート変数として登録
			ctx.setVariable("mapWidth", size.get().getWidth() * GRID_DIA);
			ctx.setVariable("mapHeight", size.get().getHeight() * GRID_DIA);
			
			// `el`と`ar`の情報をテンプレート変数として登録
			final List<Element> els = Params.getElements(u);
			final List<ArrowLine> als = makeArrowLines(els, Params.getAnteroposteriorRelationships(u).getList());
			ctx.setVariable("elements", els);
			ctx.setVariable("arrowLines", als);
			
			// SVGファイルを出力するディレクトリのパスを算出（ベースパス＋完全名）
			final File baseDir = new File(makeDirectoryPath(root, u, params));
			// ディレクトリを一括作成（まだ存在しなければ）
			baseDir.mkdirs();
			
			try {
				// ライターを初期化
				final Writer svgFile = makeWriter(baseDir, "map.svg");
				// テンプレート・エンジンで処理を実施
				final String rendered = engine.process("map", ctx);
				// Thymeleafではxlink:href属性を直接アサインできないので、
				// 別属性名でアサインしレンダリング後に強引に置換する
				final String replaced = rendered
						.replaceAll("xlink:href=\"#\"", "")
						.replaceAll("xlink-href=\"([^\"]*)\"", "xlink:href=\"$1\"");
				// 置換した文字列をファイルに書き出す
				svgFile.write(replaced);
				// ライターを閉じる
				svgFile.close();
			} catch (final IOException e) {
				// IOエラーが発生した場合はアベンド
				throw new JobdocError(Messages.UNEXPECTED_ERROR_HAS_OCCURED, e);
			}
		}
	}
	
	private Writer makeWriter(final File baseDir, final String fileName) throws FileNotFoundException {
		return new BufferedWriter(
				new OutputStreamWriter(
						new FileOutputStream(
								new File(baseDir, fileName)),
								Charset.forName("utf-8")));
	}
	
	/**
	 * SVGファイルを出力するディレクトリ・パスをくみたてる.
	 * @param root ドキュメント化対象のユニット定義
	 * @param target ドキュメント化対象ユニットかその子孫ユニットで直近SVG化しようとしているユニット定義
	 * @param params パラメータ
	 * @return SVGファイルのディレクトリ・パス
	 */
	private String makeDirectoryPath(final Unit root, final Unit target, final Parameters params) {
		final int lastSlash = root.getFullQualifiedName().lastIndexOf('/');
		final String relativePath = target.getFullQualifiedName().substring(lastSlash);
		return params.getDestinationDirectory() + relativePath;
	}
	
	/**
	 * ユニット定義パラメータ{@code "el"}と{@code "ar"}の情報をもとに関連線の情報を生成する.
	 * @param els ユニット定義パラメータ{@code "el"}のリスト
	 * @param ars ユニット定義パラメータ{@code "ar"}のリスト
	 * @return 関連線情報リスト
	 */
	private List<ArrowLine> makeArrowLines(List<Element> els, final List<AnteroposteriorRelationship> ars) {
		final List<ArrowLine> lines = new ArrayList<ArrowLine>();
		final Map<String, Point> cache = new HashMap<String, SvgWriter.Point>();
		
		for (final AnteroposteriorRelationship ar : ars) {
			// 関連線の種別から矢印の付き方を決定
			final boolean twoHeaded = ar.getType() == UnitConnectionType.CONDITIONAL;
			// 先行ユニット名をたよりに座標情報を取得
			final String fromName = ar.getFrom().getName();
			Point from = cache.get(fromName);
			if (from == null) {
				cache.put(fromName, from = makePointFromElement(els, fromName));
			}
			// 後続ユニット名をたよりに座標情報を取得
			final String toName = ar.getTo().getName();
			Point to = cache.get(toName);
			if (to == null) {
				cache.put(toName, to = makePointFromElement(els, toName));
			}
			// ぞれぞれの座標に円形のマージンをとるかたちで、関連線の始点・終点の座標を算出
			final Point marginedTo = takeMarginCircularly(from, to, MARGIN_R);
			final Point marginedFrom = takeMarginCircularly(to, from, MARGIN_R);
			// 関連線の情報をリストに追加
			lines.add(new ArrowLine(marginedFrom, marginedTo, twoHeaded));
		}
		
		return lines;
	}
	
	/**
	 * ユニット定義パラメータ{@code "el"}のリストから名前で指定されたユニットを検索してその座標情報を返す.
	 * @param els ユニット定義パラメータ{@code "el"}のリスト
	 * @param unitName 検索するユニット名
	 * @return 座標情報
	 */
	private static Point makePointFromElement(final List<Element> els, final String unitName) {
		for (final Element el : els) {
			// `el`のユニット名と引数で指定されたユニット名を照合
			if (el.getUnit().getName().equals(unitName)) {
				// 座標情報を生成して返す
				return new Point(el.getX() * GRID_DIA + GRID_R, el.getY() * GRID_DIA + GRID_R);
			}
		}
		// ユニット定義情報が不正である場合エラー
		throw new IllegalArgumentException(String.format("Unit `%s` is not found in `el` list.", unitName));
	}
	
	/**
	 * 関連線の終点に引数で指定された半径分のマージンを設けるかたちで座標を再計算する.
	 * @param from 始点の座標
	 * @param to 終点の座標
	 * @param r 半径
	 * @return 再計算された終点の座標
	 */
	private static Point takeMarginCircularly(final Point from, final Point to, final double r) {
		final double adjacent = to.x - from.x;
		final double opposite = - (to.y - from.y);
		final double hypotenuse = Math.sqrt(adjacent * adjacent + opposite * opposite);
		final double sin = opposite / hypotenuse;
		final double cos = adjacent / hypotenuse;
		final double newX = -1 * (cos * r);
		final double newY = sin * r;
		return new Point(to.x + newX, to.y + newY);
	}
	
	/**
	 * 矢印（関連線）の情報を格納するためのオブジェクト.
	 */
	public static final class ArrowLine {
		private final Point from;
		private final Point to;
		private final boolean twoHeaded;
		private ArrowLine(final Point from, final Point to, final boolean twoHeaded){
			this.from = from;
			this.to = to;
			this.twoHeaded = twoHeaded;
		}
		/**
		 * 先行ユニットの座標情報を取得する.
		 * @return 先行ユニットの座標情報
		 */
		public Point getFrom() {
			return from;
		}
		/**
		 * 後続ユニットの座標情報を取得する.
		 * @return 後続ユニットの座標情報
		 */
		public Point getTo() {
			return to;
		}
		/**
		 * マーカーが両端に付くかどうかを判定する.
		 * @return 判定結果
		 */
		public boolean getTwoHeaded() {
			return twoHeaded;
		}
	}
	
	/**
	 * 座標情報を格納するオブジェクト.
	 */
	public static final class Point {
		private final double x;
		private final double y;
		private Point (final double x, final double y) {
			this.x = x;
			this.y = y;
		}
		/**
		 * x座標値を取得する.
		 * @return x
		 */
		public double getX() {
			return x;
		}
		/**
		 * y座標値を取得する.
		 * @return y
		 */
		public double getY() {
			return y;
		}
		@Override
		public String toString() {
			return String.format("Point(%s,%s)", x, y);
		}
	}
}
