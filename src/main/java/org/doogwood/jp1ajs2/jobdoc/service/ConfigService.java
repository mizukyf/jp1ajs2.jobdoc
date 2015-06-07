package org.doogwood.jp1ajs2.jobdoc.service;

import java.io.File;
import java.nio.charset.Charset;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.doogwood.jp1ajs2.jobdoc.JobdocError;
import org.doogwood.jp1ajs2.jobdoc.Messages;
import org.doogwood.jp1ajs2.jobdoc.Parameters;
import org.springframework.stereotype.Service;

/**
 * アプリケーションのコマンドライン・オプションの定義やパースを担当するオブジェクト.
 */
@Service
public class ConfigService {
	
	private static final String OPTION_NAME_FOR_SOURCE_FILE_PATH = "s";
	private static final String OPTION_NAME_FOR_DEST_DIR_PATH = "d";
	private static final String OPTION_NAME_FOR_SOURCE_FILE_CHARSER = "source-file-charset";
	private static final String OPTION_NAME_FOR_TARGET_UNIT_NAME = "t";
	private static final String OPTION_NAME_FOR_TARGET_UNIT_NAME_PATTERN = "T";
	private static final String OPTION_NAME_FOR_DRY_RUN = "dry-run";
	
	/**
	 * コマンドライン・オプションの定義を行う.
	 * @return コマンドライン・オプション定義情報
	 */
	public Options defineOptions() {
		final Options ops = new Options();
		
		ops.addOption(Option.builder(OPTION_NAME_FOR_SOURCE_FILE_PATH)
				.hasArg()
				.required()
				.desc("読み取り対象のユニット定義ファイルのパス")
				.argName("source-file-path")
				.build());
		
		ops.addOption(Option.builder()
				.hasArg()
				.required(false)
				.desc("読み取り対象のユニット定義ファイルのキャラクタセット（デフォルトは\"Windows-31J\"）")
				.argName("source-file-charset")
				.longOpt(OPTION_NAME_FOR_SOURCE_FILE_CHARSER)
				.build());
		
		ops.addOption(Option.builder(OPTION_NAME_FOR_DEST_DIR_PATH)
				.hasArg()
				.required(false)
				.desc("ドキュメントを出力するディレクトリのパス（デフォルトは\".\"）")
				.argName("destination-directory-path")
				.build());
		
		final OptionGroup og = new OptionGroup();
		og.setRequired(true);
		og.addOption(Option.builder(OPTION_NAME_FOR_TARGET_UNIT_NAME)
				.hasArg()
				.required(false)
				.desc("ドキュメント化する対象のユニット名")
				.argName("target-unit-name")
				.build());
		og.addOption(Option.builder(OPTION_NAME_FOR_TARGET_UNIT_NAME_PATTERN)
				.hasArg()
				.required(false)
				.desc("ドキュメント化する対象のユニット名の正規表現パターン")
				.argName("target-unit-name-pattern")
				.build());
		ops.addOptionGroup(og);
		
		ops.addOption(Option.builder()
				.hasArg()
				.required(false)
				.desc("ユニット定義のパースとドキュメント化対象の特定まで行うが実際のドキュメント化は行わない")
				.longOpt(OPTION_NAME_FOR_DRY_RUN)
				.build());
		
		return ops;
	}
	
	public CommandLine parseOptions(final Options ops, final String[] args) {
		try {
			return new DefaultParser().parse(ops, args);
		} catch (final ParseException e) {
			throw new JobdocError(Messages.SYNTAX_ERROR_FOUND_IN_COMMANDLINE_OPTIONS, e);
		}
	}
	
	/**
	 * USAGEを表示すべきかどうか確認する.
	 * @param args コマンドライン引数
	 * @return 表示すべきかどうか
	 */
	public boolean checkIfRequireToPrintUsage(final String[] args) {
		return args.length == 0 || args[0].equals("-h") ||
				args[0].equals("--help") || args[0].equals("-?");
	}
	
	/**
	 * stdoutに対してUSAGEの出力を行う.
	 * @param ops コマンドライン・オプション定義情報
	 */
	public void printUsage(final Options ops) {
		final HelpFormatter f = new HelpFormatter();
		f.printHelp("java -jar jp1ajs2.jobdoc.jar <options>", ops);
	}
	
	/**
	 * コマンドライン・オブジェクトをもとに{@link Parameters}オブジェクトを初期化する.
	 * @param cmd コマンドライン・オブジェクト
	 * @return {@link Parameters}オブジェクト
	 */
	public Parameters populateParams(CommandLine cmd) {
		final Parameters params = new Parameters();
		
		// sオプションの処理
		final String s = cmd.getOptionValue(OPTION_NAME_FOR_SOURCE_FILE_PATH);
		final File sf = new File(s);
		if (!sf.isFile()) {
			throw new JobdocError(Messages.SOURCE_FILE_DOES_NOT_EXIST);
		}
		params.setSourceFile(sf);
		
		// source-file-charsetオプションの処理
		if (cmd.hasOption(OPTION_NAME_FOR_SOURCE_FILE_CHARSER)) {
			try {
				params.setSourceFileCharset(Charset.forName(cmd.getOptionValue(OPTION_NAME_FOR_SOURCE_FILE_CHARSER)));
			} catch (final Exception e) {
				throw new JobdocError(Messages.ILLEGAL_CHARSET_NAME, e);
			}
		}
		
		// dオプションの処理
		if (cmd.hasOption(OPTION_NAME_FOR_DEST_DIR_PATH)) {
			final String d = cmd.getOptionValue(OPTION_NAME_FOR_DEST_DIR_PATH);
			final File df = new File(d);
			if (!df.isDirectory()) {
				throw new JobdocError(Messages.DESTINATION_DIRECTORY_DOES_NOT_EXIST);
			}
			params.setDestinationDirectory(df);
		}
		
		// tオプションの処理
		if (cmd.hasOption(OPTION_NAME_FOR_TARGET_UNIT_NAME)) {
			final String t = cmd.getOptionValue(OPTION_NAME_FOR_TARGET_UNIT_NAME).trim();
			if (t.isEmpty()) {
				throw new JobdocError(Messages.TARGET_UNIT_NAME_IS_EMPTY);
			}
			params.setTargetUnitName(t);
		}
		
		// Tオプションの処理
		if (cmd.hasOption(OPTION_NAME_FOR_TARGET_UNIT_NAME_PATTERN)) {
			try {
				final Pattern p = Pattern.compile(cmd.getOptionValue(OPTION_NAME_FOR_TARGET_UNIT_NAME_PATTERN));
				params.setTargetUnitNamePattern(p);
			} catch (final PatternSyntaxException e2) {
				throw new JobdocError(Messages.SYNTAX_ERROR_FOUND_IN_TARGET_UNIT_NAME_PATTERN, e2);
			}
		}
		
		// --dry-runオプションの処理
		params.setDryRun(cmd.hasOption(OPTION_NAME_FOR_DRY_RUN));
		
		return params;
	}
}
