package com.m12i.jp1ajs2.jobdoc.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.m12i.jp1ajs2.unitdef.HoldAttrType;
import com.m12i.jp1ajs2.unitdef.Param;
import com.m12i.jp1ajs2.unitdef.UnitType;

/**
 * ユニット定義パラメータ値の解説情報を提供するオブジェクト.
 */
public class Explicator {
	
	private static final String UNIT_TYPE_IS_A1 = "ユニット種別は`%1$s`";
	private static final String UNIT_COMMENT_IS_A1 = "コメントは`%1$s`";
	private static final String SUBUNIT_A1_TYPE_A2_POSITION_HORIZONTAL_A3_VERTICAL_A4 =
			"下位ユニット`%1$s`（種別は`%2$s`）の位置は水平方向`%3$s`・垂直方向`%4$s`";
	private static final String HOLD_TYPE_IS_A1 = "保留属性設定は`%1$s`";
	private static final String FIXED_DURATION_IS_A1 = "実行所要時間は`%1$s分`";
	private static final String ANTEROPOSTERIOR_RELATION_FROM_A1_TO_A2_TYPE_A3 =
			"下位ユニットの前後関係 `%1$s`から`%2$s`へ（接続種別は`%3$s`）";
	private static final String EXECUTION_HOST_IS_A1 = "実行ホスト名は`%1$s`";
	private static final String RULE_NO_A1_START_DATE_IS_A2 = "ルール番号`%1$s` ジョブネットの実行開始日は`%2$s`";
	private static final String RULE_NO_A1_START_TIME_IS_A2 = "ルール番号`%1$s` ジョブネットの実行開始時刻は`%2$s`";
	
	private static final Pattern EL = Pattern.compile("([^,]+),([^,]+),[^\\d]+(\\d+)[^\\d]+(\\d+)");
	private static final Pattern AR = Pattern.compile("\\(f=([^,]+),t=([^,\\)]+)(,([^\\)]+))?\\)");
	private static final Pattern SD_OUTER = Pattern.compile("((\\d+),\\s*)?(en|ud|.+)");
	private static final Pattern SD_INNER = 
			Pattern.compile("(((\\d\\d\\d\\d)/)?(\\d{1,2})/)?((\\+|\\*|@)?(\\d+)|(\\+|\\*|@)?b(-(\\d+))?|(\\+)?(su|mo|tu|we|th|fr|sa)(\\s*:(n|b))?)");
	private static final Pattern ST = Pattern.compile("((\\d+),\\s*)?(\\+)?(\\d+:\\d+)");
	
	private static String sf(final String tpl, final Object... args) {
		return String.format(tpl, args);
	}
	
	public String explicate(final Param p) {
		final String code = p.getName();
		final String value = p.getValue();
		
		if (code.equals("ar")) {
			final Matcher m = AR.matcher(value);
			if (m.matches()) {
				final String uct = m.group(3) == null 
						? "seq: 順接続" 
						: "cond: 条件接続";
				return sf(ANTEROPOSTERIOR_RELATION_FROM_A1_TO_A2_TYPE_A3,
						m.group(1), m.group(2), uct);
			}
			
		} else if (code.equals("cm")) {
			return sf(UNIT_COMMENT_IS_A1, p.getValue(0).getStringValue());
			
		} else if (code.equals("el")) {
			final Matcher m = EL.matcher(value);
			if (m.matches()) {
				return sf(SUBUNIT_A1_TYPE_A2_POSITION_HORIZONTAL_A3_VERTICAL_A4,
						m.group(1), m.group(2), m.group(3), m.group(4));
			}
			
		} else if (code.equals("ex")) {
			return sf(EXECUTION_HOST_IS_A1, value);
			
		} else if (code.equals("fd")) {
			return sf(FIXED_DURATION_IS_A1, value);
			
		} else if (code.equals("ha")) {
			return sf(HOLD_TYPE_IS_A1, HoldAttrType.forCode(value).getDescription());
			
		} else if (code.equals("sd")) {
			return explicateStartDate(p);
			
		} else if (code.equals("st")) {
			return explicateStartTime(p);
			
		}else if (code.equals("ty")) {
			return sf(UNIT_TYPE_IS_A1, UnitType.forCode(value).getDescription());
			
		}
		
		return "";
	}
	
	private String explicateStartTime(final Param p) {
		final String value = p.getValue();
		final Matcher m = ST.matcher(value);
		
		if (!m.matches()) {
			return "";
		}
		
		//  12            3     4
		// "((\\d+),\\s*)?(\\+)?(\\d+:\\d+)"
		final String ruleNo = m.group(2) == null ? "1" : m.group(2);
		final boolean relative = m.group(3) != null;
		final String time = m.group(4);
		
		return sf(RULE_NO_A1_START_TIME_IS_A2, ruleNo, (relative ? "相対時刻 " : "絶対時刻 ") + time);
	}
	
	private String explicateStartDate(final Param p) {
		final String value = p.getValue();
		final Matcher m0 = SD_OUTER.matcher(value);
		if (m0.matches()) {
			if (m0.group(3).equals("ud")) {
				return sf(RULE_NO_A1_START_DATE_IS_A2, m0.group(2), "ud: ジョブネットのスケジュールは未定義");
			} else if (m0.group(3).equals("en")) {
				return sf(RULE_NO_A1_START_DATE_IS_A2, m0.group(2), "en: ジョブネットを実行登録した日が実行開始日");
			}
			
			//  123                4          56       7      8        9 10       11  12                    13   14
			// "(((\\d\\d\\d\\d)/)?(\\d\\d)/)?((+|*|@)?(\\d+)|(+|*|@)?b(-(\\d+))?|(+)?(su|mo|tu|we|th|fr|sa)(\\s*:(\\d|b))?)"
			final Matcher m1 = SD_INNER.matcher(m0.group(3));
			
			if (!m1.matches()) {
				return "";
			}
			
			final String ruleNo = m0.group(2) == null ? "1" : m0.group(2);
			
			final String yyyy = m1.group(3) == null ? "実行登録した年 " : (m1.group(3) + "年 ");
			final String mm = m1.group(4) == null ? "実行登録した月 " : (m1.group(4) + "月 ");
			
			final String ddPrefix = m1.group(6);
			final boolean ddRelative = ddPrefix != null;
			final String bddPrefix = m1.group(8);
			final boolean bddRelative = bddPrefix != null;
			final String bdd = m1.group(10);
			final String dd = m1.group(7);
			final String day = m1.group(12);
			final String dayNB = m1.group(14);
			
			if (dd != null) {
				final String ddExpl = (ddRelative 
					? (ddPrefix.equals("+") 
							? "相対日" 
							: (ddPrefix.equals("*") 
								? "運用日" 
								: (ddPrefix.equals("@") 
									? "休業日" : "?")))
					: "") + " 第" + dd + "日";
				return sf(RULE_NO_A1_START_DATE_IS_A2, ruleNo, yyyy + mm + ddExpl);
			} else if (day != null) {
				final String dayExpl = (day.equals("su")
					? "日曜"
					: (day.equals("mo")
						? "月曜" 
						: (day.equals("tu")
							? "火曜"
							: (day.equals("we")
								? "水曜"
								: (day.equals("th")
									? "木曜"
									: (day.equals("fr")
										? "火曜" : "土曜"))))));
				final String dayPrefix = 
						dayNB == null ? "直近 " : (dayNB.equals("b") ? "最終 " : "第 " + dayNB);
				return sf(RULE_NO_A1_START_DATE_IS_A2, ruleNo, yyyy + mm + dayPrefix + dayExpl);
			} else {
				final String bddExpl = 
					(bddRelative 
						? (bddPrefix.equals("+") 
								? "相対日" 
								: (bddPrefix.equals("*") 
									? "運用日" 
									: (bddPrefix.equals("@") 
										? "休業日" : "?")))
						: "") + " 第" + bdd + "日";
				return sf(RULE_NO_A1_START_DATE_IS_A2, ruleNo, yyyy + mm +
						(bdd == null ? "月末または最終運用日" : "から逆算で " + bddExpl));
			}
		}
		return "";
	}
	
}
