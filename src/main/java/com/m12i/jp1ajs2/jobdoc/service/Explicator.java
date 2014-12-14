package com.m12i.jp1ajs2.jobdoc.service;

import com.m12i.jp1ajs2.unitdef.HoldAttrType;
import com.m12i.jp1ajs2.unitdef.Param;
import com.m12i.jp1ajs2.unitdef.Params;
import com.m12i.jp1ajs2.unitdef.UnitType;

/**
 * ユニット定義パラメータ値の解説情報を提供するオブジェクト.
 */
public class Explicator {
	
	private static final String UNIT_TYPE_IS_A1 = "ユニット種別は`%1$s`";
	private static final String UNIT_COMMENT_IS_A1 = "コメントは`%1$s`";
	private static final String HOLD_TYPE_IS_A1 = "保留属性設定は`%1$s`";
	private static final String FIXED_DURATION_IS_A1 = "実行所要時間は`%1$s分`";
	private static final String EXECUTION_HOST_IS_A1 = "実行ホスト名は`%1$s`";
	
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
			
		}
		
		return "";
	}
}
