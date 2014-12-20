package com.m12i.jp1ajs2.jobdoc;

import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.thymeleaf.TemplateEngine;

import com.m12i.jp1ajs2.jobdoc.service.Configurer;
import com.m12i.jp1ajs2.jobdoc.service.ServiceProvider;
import com.m12i.jp1ajs2.jobdoc.service.Parser;
import com.m12i.jp1ajs2.jobdoc.service.HtmlWriter;
import com.m12i.jp1ajs2.jobdoc.service.SvgWriter;
import com.m12i.jp1ajs2.jobdoc.service.Traverser;
import com.m12i.jp1ajs2.unitdef.Unit;

public class Jobdoc {
	/**
	 * アプリケーション名.
	 */
	public static final String APPLICATION_NAME = "JP1/AJS2 Jobdoc";
	/**
	 * アプリケーションのバージョン名.
	 */
	public static final String APPLICATION_VERSION = "1.1.0";
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
	 * アプリケーション固有のロガー.
	 * ログ出力設定は`log4j.properties`で行う。
	 */
	private final Logger logger = ServiceProvider.getLogger();
	/**
	 * 設定情報関連を担当するサービス・クラス.
	 */
	private Configurer config = ServiceProvider.getConfigurer();
	/**
	 * ユニット定義パースを担当するサービス・クラス.
	 */
	private final Parser pars = ServiceProvider.getParser();
	/**
	 * ユニット定義をもとに各種情報を収集するサービス・クラス.
	 */
	private final Traverser trav = ServiceProvider.getTraverser();
	/**
	 * ドキュメント化のHTML部分を担当するサービス・クラス.
	 */
	private final HtmlWriter html = ServiceProvider.getHtmlWriter();
	/**
	 * ドキュメント化のSVG部分を担当するサービス・クラス.
	 */
	private final SvgWriter svg = ServiceProvider.getSvgWriter();
	
	/**
	 * アプリケーションの主処理を実行する.
	 * @param args コマンドライン引数
	 */
	public void execute(final String[] args) {
		// オプションの定義を行う
		final Options ops = config.defineOptions();
		// コマンドライン引数をチェック
		if (config.checkIfRequireToPrintUsage(args)) {
			// USAGEを表示してプログラムを終了する
			config.printUsage(ops);
			System.exit(EXIT_CODE_NORMAL);
		}
		
		try {
			logger.info("コマンドライン・オプションを解析します.");
			final CommandLine cmd = config.parseOptions(ops, args);
			
			logger.info("パラメータ・オブジェクトを初期化します.");
			final Parameters params = config.populateParams(cmd);
			logger.info("パラメータ・オブジェクト： {}", params);

			logger.info("テンプレート・エンジンを初期化します.");
			final TemplateEngine htmlEngine = html.initializeTemplateEngine();
			final TemplateEngine svgEngine = svg.initializeTemplateEngine();
			
			logger.info("ユニット定義ファイルのパースを行います.");
			final Unit root = pars.parseSourceFile(params);
			
			logger.info("指定された条件にマッチするユニットを検索します.");
			final Map<String,Unit> targets = trav.collectTargetUnits(root, params);
			
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
					logger.warn(fqn);
				}
			}
			
			// 対象ユニットが0の場合は警告終了
			if (targets.isEmpty()) {
				throw new JobdocWarning(Messages.TARGET_UNIT_IS_NOT_FOUND);
			}
			
			logger.info("ドキュメント化対象ユニット数： {}", targets.size());
			logger.info("次のユニットがドキュメント化対象となります：");
			for (final String fqn : targets.keySet()) {
				logger.info(fqn);
			}

			for (final Map.Entry<String,Unit> e : targets.entrySet()) {
				logger.info("ユニット{}とその配下のユニットをHTMLドキュメント化します.", e.getKey());
				html.renderHtml(e.getValue(), htmlEngine, params);
				
				logger.info("ユニット{}とその配下のユニットのマップをSVGドキュメント化します.", e.getKey());
				svg.renderSvg(e.getValue(), svgEngine, params);
			}
			
		} catch (final JobdocError e1) {
			// 異常終了（アプリケーション・エラーの場合）
			logger.error(Messages.APPLICATION_ERROR_HAS_OCCURED, e1);
			if (e1.getMessage() != null) {
				logger.warn(e1.getMessage());
			}
			e1.printStackTrace();
			System.exit(EXIT_CODE_ERROR);
			
		} catch (final JobdocWarning e2) {
			// 警告終了
			logger.warn(Messages.APPLICATION_WARNING_HAS_OCCURED);
			if (e2.getMessage() != null) {
				logger.warn(e2.getMessage());
			}
			System.exit(EXIT_CODE_WARNING);
			
		} catch (final Exception e3) {
			// 異常終了（想定外のエラーの場合）
			logger.error(Messages.UNEXPECTED_ERROR_HAS_OCCURED, e3);
			if (e3.getMessage() != null) {
				logger.warn(e3.getMessage());
			}
			e3.printStackTrace();
			System.exit(EXIT_CODE_ERROR);
		}
		
		logger.info("すべての処理が完了しました.");
		
		// エラーも警告もなければ正常終了
		System.exit(EXIT_CODE_NORMAL);
	}	
}
