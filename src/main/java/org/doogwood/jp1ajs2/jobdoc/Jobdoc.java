package org.doogwood.jp1ajs2.jobdoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.doogwood.jp1ajs2.jobdoc.service.ConfigService;
import org.doogwood.jp1ajs2.jobdoc.service.HtmlRenderService;
import org.doogwood.jp1ajs2.jobdoc.service.ParseService;
import org.doogwood.jp1ajs2.jobdoc.service.SvgRenderService;
import org.doogwood.jp1ajs2.jobdoc.service.TraverseService;
import org.doogwood.jp1ajs2.unitdef.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;

@Component
public class Jobdoc {
	/**
	 * アプリケーション名.
	 */
	public static final String APPLICATION_NAME = "JP1/AJS2 Jobdoc";
	/**
	 * アプリケーションのバージョン名.
	 */
	public static final String APPLICATION_VERSION = "1.5.2";
	/**
	 * 正常終了時のExit Code.
	 */
	public static final int EXIT_CODE_NORMAL = 0;
	/**
	 * 警告終了時のExit Code.
	 */
	public static final int EXIT_CODE_WARNING = 1;
	/**
	 * 異常終了時のExit Code.
	 */
	public static final int EXIT_CODE_ERROR = 2;
	/**
	 * このアプリケーションのエントリーポイント.
	 * @param args コマンドライン引数
	 */
	public static void main(String[] args) {
		final Class<Jobdoc> classMeta = Jobdoc.class;
		final String packageName = classMeta.getPackage().getName();
		final AnnotationConfigApplicationContext ctx = 
				new AnnotationConfigApplicationContext(packageName);
		int exitCode = EXIT_CODE_NORMAL;
		try {
			ctx.getBean(classMeta).execute(args);
		} catch (final JobdocWarning e) {
			e.printStackTrace();
			exitCode = EXIT_CODE_WARNING;
		} catch (final JobdocError e) {
			e.printStackTrace();
			exitCode = EXIT_CODE_ERROR;
		} catch (final Exception e) {
			e.printStackTrace();
			exitCode = EXIT_CODE_ERROR;
		} finally {
			ctx.close();
		}
		System.exit(exitCode);
	}
	
	/**
	 * アプリケーション固有のロガー.
	 * ログ出力設定は`log4j.properties`で行う。
	 */
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	/**
	 * 設定情報関連を担当するサービス・クラス.
	 */
	@Autowired
	private ConfigService config;
	/**
	 * ユニット定義パースを担当するサービス・クラス.
	 */
	@Autowired
	private ParseService pars;
	/**
	 * ユニット定義をもとに各種情報を収集するサービス・クラス.
	 */
	@Autowired
	private TraverseService trav;
	/**
	 * ドキュメント化のHTML部分を担当するサービス・クラス.
	 */
	@Autowired
	private HtmlRenderService html;
	/**
	 * ドキュメント化のSVG部分を担当するサービス・クラス.
	 */
	@Autowired
	private SvgRenderService svg;
	
	/**
	 * アプリケーションの主処理を実行する.
	 * @param args コマンドライン引数
	 * @throws Exception 主処理実行中にエラーが発生した場合
	 */
	public void execute(final String[] args) throws Exception {
		// オプションの定義を行う
		final Options ops = config.defineOptions();
		// コマンドライン引数をチェック
		if (config.checkIfRequireToPrintUsage(args)) {
			// USAGEを表示してプログラムを終了する
			config.printUsage(ops);
			return;
		}
		
		try {
			logger.info("コマンドライン・オプションを解析します.");
			final CommandLine cmd = config.parseOptions(ops, args);
			
			logger.info("パラメータ・オブジェクトを初期化します.");
			final Parameters params = config.populateParams(cmd);
			logger.info("パラメータ・オブジェクト： {}", params);

			if (params.getDryRun()) {
				logger.warn("ドライ・ラン・モードで起動しました. "
						+ "ユニット定義のパースとドキュメント化対象の特定まで"
						+ "行いますが実際のドキュメント化は行わないません.");
			}
			
			logger.info("ユニット定義ファイルのパースを行います.");
			final List<Unit> root = pars.parseSourceFile(params);
			
			logger.info("指定された条件にマッチするユニットを検索します.");
			final Map<String,Unit> targets = trav.collectTargetUnits(root.get(0), params);
			
			logger.info("マッチするユニット数： {}", targets.size());
			logger.info("検索の結果次のユニットが見つかりました：");
			for (final String fqn : targets.keySet()) {
				logger.info(fqn);
			}

			logger.info("検索結果にネストしたユニットが存在しないかチェックします.");
			final Map<String,Unit> removedUnits = trav.removeNestedUnits(targets);
			
			if (removedUnits.isEmpty()) {
				logger.info("ネストしたユニットはありません.");
			} else {
				logger.warn("ネストしたユニット数： {}", removedUnits.size());
				logger.warn("次のユニットはネストしたユニットとしてドキュメント化対象から除外されました：");
				for (final String fqn : removedUnits.keySet()) {
					logger.warn("- " + fqn);
				}
			}

			logger.info("検索結果に名前の重複したユニットが存在しないかチェックします.");
			final Map<String,Unit> removedUnits2 = trav.removeDuplicatedUnits(targets);

			if (removedUnits2.isEmpty()) {
				logger.info("名前の重複したユニットはありません.");
			} else {
				logger.warn("名前の重複したユニット数： {}", removedUnits2.size());
				logger.warn("次のユニットは名前の重複したユニットとしてドキュメント化対象から除外されました：");
				for (final String fqn : removedUnits2.keySet()) {
					logger.warn("- " + fqn);
				}
			}

			// 対象ユニットが0の場合は警告終了
			if (targets.isEmpty()) {
				throw new JobdocWarning(Messages.TARGET_UNIT_IS_NOT_FOUND);
			}
			
			logger.info("ドキュメント化対象ユニット数： {}", targets.size());
			logger.info("次のユニットがドキュメント化対象となります：");
			for (final String fqn : targets.keySet()) {
				logger.info("- " + fqn);
			}
			
			if (params.getDryRun()) {
				logger.info("すべての処理が完了しました.");
				
				// ドライ・ラン・モードで起動した場合はここで処理を中断
				return;
			}

			logger.info("テンプレート・エンジンを初期化します.");
			final TemplateEngine htmlEngine = html.initializeTemplateEngine();
			final TemplateEngine svgEngine = svg.initializeTemplateEngine();
			
			for (final Map.Entry<String,Unit> e : targets.entrySet()) {
				logger.info("ユニット{}とその配下のユニットをHTMLドキュメント化します.", e.getKey());
				html.renderHtml(e.getValue(), htmlEngine, params);
				
				logger.info("ユニット{}とその配下のユニットのマップをSVGドキュメント化します.", e.getKey());
				svg.renderSvg(e.getValue(), svgEngine, params);
			}
			
			final List<Unit> targetList = new ArrayList<Unit>();
			targetList.addAll(targets.values());
			Collections.sort(targetList, new Comparator<Unit>() {
				@Override
				public int compare(Unit o1, Unit o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			
			logger.info("ドキュメント化したユニットの一覧を出力します.");
			html.renderHtmlList(targetList, htmlEngine, params);
			
		} catch (final JobdocError e) {
			// 異常終了（アプリケーション・エラーの場合）
			logger.error(Messages.APPLICATION_ERROR_HAS_OCCURED, e);
			if (e.getMessage() != null) {
				logger.warn(e.getMessage());
			}
			e.printStackTrace();
			throw e;
			
		} catch (final JobdocWarning e) {
			// 警告終了
			logger.warn(Messages.APPLICATION_WARNING_HAS_OCCURED);
			if (e.getMessage() != null) {
				logger.warn(e.getMessage());
			}
			throw e;
			
		} catch (final Exception e) {
			// 異常終了（想定外のエラーの場合）
			logger.error(Messages.UNEXPECTED_ERROR_HAS_OCCURED, e);
			if (e.getMessage() != null) {
				logger.warn(e.getMessage());
			}
			e.printStackTrace();
			throw e;
		}
		
		logger.info("すべての処理が完了しました.");
	}	
}
