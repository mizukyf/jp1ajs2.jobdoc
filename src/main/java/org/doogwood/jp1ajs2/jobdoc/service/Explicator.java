package org.doogwood.jp1ajs2.jobdoc.service;

import org.doogwood.jp1ajs2.unitdef.FileWatchingCondition;
import org.doogwood.jp1ajs2.unitdef.HoldAttrType;
import org.doogwood.jp1ajs2.unitdef.Param;
import org.doogwood.jp1ajs2.unitdef.Params;
import org.doogwood.jp1ajs2.unitdef.UnitType;

/**
 * ユニット定義パラメータ値の解説情報を提供するオブジェクト.
 */
public class Explicator {
	
	private static final String UNIT_TYPE_IS_A1 = "ユニット種別は`%1$s`";
	private static final String UNIT_COMMENT_IS_A1 = "コメントは`%1$s`";
	private static final String HOLD_TYPE_IS_A1 = "保留属性設定は`%1$s`";
	private static final String FIXED_DURATION_IS_A1 = "実行所要時間は`%1$s分`";
	private static final String EXECUTION_HOST_IS_A1 = "実行ホスト名は`%1$s`";
	private static final String EXECUTION_USER_IS_A1 = "実行ユーザー名は`%1$s`";
	private static final String COMMAND_TEXT_IS_A1 = "コマンドテキストは`%s`";
	private static final String SCRIPT_FILE_NAME_IS_A1 = "スクリプトファイル名は`%s`";
	private static final String PARAMETER_IS_A1 = "パラメーターは`%s`";
	private static final String EXECUTION_TIME_OUT_IS_A1 = "実行打ち切り時間は`%s分`";
	private static final String WARNING_THRESHOLD_IS_A1 = "警告終了の閾値は`%s`";
	private static final String ERROR_THRESHOLD_IS_A1 = "異常終了の閾値は`%s`";
	private static final String SCHEDULE_DEPENDENCY_IS_A1 = "上位ジョブネットのスケジュールとの依存関係は`%s`";
	private static final String RESERVED_GENERATION_NUM_IS_A1 = "ジョブネットの保存世代数は`%s`";
	private static final String TIME_INTERVAL_IS_A1 = "待ち時間は`%s分`";
	private static final String EXIT_CODE_IS_A1 = "判定終了コードは`%s`";
	private static final String WATCHED_FILE_NAME_IS_A1 = "監視対象ファイル名は`%s`";
	private static final String WATCH_INTERVAL_IS_A1 = "監視間隔は`%s秒`";
	private static final String WATCHING_CONDITION_IS_A1 = "監視条件は %s";
	private static final String IF_ALREADY_EXISTING_IS_A1 = "実行開始時すでに監視対象ファイルが存在した場合`%s`";
	private static final String MAIL_ADDRESS_IS_A1 = "メールアドレスは`%s`";
	private static final String MAIL_PROFILE_IS_A1 = "メール・プロファイル名は";
	private static final String MAIL_SUBJECT_IS_A1 = "メール件名は`%s`";
	private static final String MAIL_BODY_IS_A1 = "メール本文は`%s`";
	private static final String MAIL_BODY_FILE_IS_A1 = "メール本文ファイル名は`%s`";
	private static final String MAIL_ATTACHMENT_IS_A1 = "添付ファイル名は`%s`";
	private static final String MAIL_ATTACHMENT_LIST_IS_A1 = "添付ファイルリスト名は`%s`";
	
	private static String sf(final String tpl, final Object... args) {
		return String.format(tpl, args);
	}
	
	public String explicate(final Param p) {
		final String code = p.getName();
		final String value = p.getValue();
		
		if (code.equals("ar")) {
			return Params.getAnteroposteriorRelationship(p).toString();
			
		} else if (code.equals("cm")) {
			return sf(UNIT_COMMENT_IS_A1, p.getValue(0).getStringValue());
			
		} else if (code.equals("el")) {
			return Params.getElement(p).toString();
			
		} else if (code.equals("ex")) {
			return sf(EXECUTION_HOST_IS_A1, value);
			
		} else if (code.equals("un")) {
			return sf(EXECUTION_USER_IS_A1, value);
			
		} else if (code.equals("fd")) {
			return sf(FIXED_DURATION_IS_A1, value);
			
		} else if (code.equals("ha")) {
			return sf(HOLD_TYPE_IS_A1, HoldAttrType.forCode(value).getDescription());
			
		} else if (code.equals("sd")) {
			return Params.getStartDate(p).toString();
			
		} else if (code.equals("st")) {
			return Params.getStartTime(p).toString();
			
		} else if (code.equals("sy")) {
			return Params.getStartDelayingTime(p).toString();
			
		} else if (code.equals("ey")) {
			return Params.getEndDelayingTime(p).toString();
			
		}else if (code.equals("ty")) {
			return sf(UNIT_TYPE_IS_A1, UnitType.forCode(value).getDescription());
			
		} else if (code.equals("ets")) {
			return Params.getExecutionTimedOutStatus(p).toString();
			
		} else if (code.equals("tmitv")) {
			return sf(TIME_INTERVAL_IS_A1, value);
			
		} else if (code.equals("etm")) {
			return sf(EXECUTION_TIME_OUT_IS_A1, value);
			
		} else if (code.equals("sc")) {
			return sf(SCRIPT_FILE_NAME_IS_A1, value);
			
		} else if (code.equals("prm")) {
			return sf(PARAMETER_IS_A1, value);
			
		} else if (code.equals("te")) {
			return sf(COMMAND_TEXT_IS_A1, value);
			
		} else if (code.equals("wth")) {
			return sf(WARNING_THRESHOLD_IS_A1, value);
			
		} else if (code.equals("tho")) {
			return sf(ERROR_THRESHOLD_IS_A1, value);
			
		} else if (code.equals("rg")) {
			return sf(RESERVED_GENERATION_NUM_IS_A1, value);
			
		} else if (code.equals("de")) {
			return sf(SCHEDULE_DEPENDENCY_IS_A1, value.equals("n") 
					? "n：上位ジョブネットのスケジュールに依存"
					: "y：上位ジョブネットのスケジュールに依存");
			
		} else if (code.equals("ej")) {
			return Params.getEvaluateConditionType(p).toString();
			
		} else if (code.equals("ejc")) {
			return sf(EXIT_CODE_IS_A1, value);
			
		} else if (code.equals("flwf")) {
			return sf(WATCHED_FILE_NAME_IS_A1, value);
			
		} else if (code.equals("flwi")) {
			return sf(WATCH_INTERVAL_IS_A1, value);
			
		} else if (code.equals("flwc")) {
			return sf(WATCHING_CONDITION_IS_A1, FileWatchingCondition.forCode(value));
			
		} else if (code.equals("flco")) {
			return sf(IF_ALREADY_EXISTING_IS_A1, value.equals("n") 
					? "n：監視条件成立とみなさない"
					: "y：監視条件成立とみなし正常終了");
			
		} else if (code.equals("mladr")) {
			return sf(MAIL_ADDRESS_IS_A1, value);
			
		} else if (code.equals("mlprf")) {
			return sf(MAIL_PROFILE_IS_A1, value);
			
		} else if (code.equals("mlsbj")) {
			return sf(MAIL_SUBJECT_IS_A1, value);
			
		} else if (code.equals("mltxt")) {
			return sf(MAIL_BODY_IS_A1, value);
			
		} else if (code.equals("mlftx")) {
			return sf(MAIL_BODY_FILE_IS_A1, value);
			
		} else if (code.equals("mlatf")) {
			return sf(MAIL_ATTACHMENT_IS_A1, value);
			
		} else if (code.equals("mlafl")) {
			return sf(MAIL_ATTACHMENT_LIST_IS_A1, value);
			
		}
		
		return "";
	}
}
