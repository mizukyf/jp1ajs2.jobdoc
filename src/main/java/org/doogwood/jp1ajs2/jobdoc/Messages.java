package org.doogwood.jp1ajs2.jobdoc;

/**
 * メッセージ定義オブジェクト.
 */
public final class Messages {
	private Messages() {}
	
	public static final String APPLICATION_ERROR_HAS_OCCURED = "アプリケーション実行中にエラーが発生しました.";
	public static final String APPLICATION_WARNING_HAS_OCCURED = "アプリケーション実行中に警告が発生しました.";
	public static final String DESTINATION_DIRECTORY_DOES_NOT_EXIST = "ドキュメントを出力するディレクトリが存在しません.";
	public static final String ILLEGAL_CHARSET_NAME = "キャラクタセット名として不正な値が設定されています.";
	public static final String PARSE_ERROR_HAS_OCCURED = "ユニット定義ファイルのパース中にエラーが発生しました.";
	public static final String SOURCE_FILE_DOES_NOT_EXIST = "読み取り対象のユニット定義ファイルが存在しません.";
	public static final String SYNTAX_ERROR_FOUND_IN_COMMANDLINE_OPTIONS = "コマンドライン・オプションに構文エラーが見つかりました.";
	public static final String SYNTAX_ERROR_FOUND_IN_TARGET_UNIT_NAME_PATTERN = "ドキュメント化対象ユニット名パターンに構文エラーが見つかりました.";
	public static final String TARGET_UNIT_IS_NOT_FOUND = "ユニット定義ファイル内には対象として指定されたユニットが存在しません.";
	public static final String TARGET_UNIT_NAME_IS_EMPTY = "ドキュメント化する対象のユニット名が不正です.";
	public static final String UNEXPECTED_ERROR_HAS_OCCURED = "アプリケーション実行中に予期せぬエラーが発生しました.";
}
