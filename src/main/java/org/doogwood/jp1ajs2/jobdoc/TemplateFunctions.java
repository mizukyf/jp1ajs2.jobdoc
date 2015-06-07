package org.doogwood.jp1ajs2.jobdoc;

import org.doogwood.jp1ajs2.jobdoc.service.ExplicateService;
import org.doogwood.jp1ajs2.unitdef.Param;
import org.doogwood.jp1ajs2.unitdef.Unit;
import org.doogwood.jp1ajs2.unitdef.UnitType;

/**
 * テンプレート内で使用するユーティリティ.
 */
public final class TemplateFunctions {
	private final ExplicateService expl;
	private final Parameters params;
	private final Unit root;
	public TemplateFunctions(final Parameters params, final Unit root, final ExplicateService expl) {
		this.params = params;
		this.root = root;
		this.expl = expl;
	}
	/**
	 * コマンドラインで指定されたパラメータを取得する.
	 * @return パラメータ
	 */
	public Parameters getJobdocParams() {
		return params;
	}
	/**
	 * ユニット定義パラメータの説明テキストを取得する.
	 * @param p ユニット定義パラメータ
	 * @return 説明テキスト
	 */
	public String explicate(final Param p) {
		return expl.explicate(p);
	}
	/**
	 * マップ（SVGファイル）の相対パスを生成する.
	 * @param target ドキュメント化対象もしくはその子孫にあたり直近ツリー要素出力の対象となっているユニット定義
	 * @return マップの相対パス
	 */
	public String createMapPath(final Unit target) {
		final int rootFqnLen = root.getFullQualifiedName().length();
		final String relativePath = target.getFullQualifiedName().substring(rootFqnLen) + "/map.svg";
		return relativePath.substring(1);
	}
	public String createDetailPathFromMap(final Unit target) {
		final String relativeFqn = createRelativeFqn(target);
		final int n = relativeFqn.split("/").length - 3;
		final StringBuilder buff = new StringBuilder();
		for (int i = 0; i < n; i ++) {
			buff.append("../");
		}
		buff.append("detail.html#");
		buff.append(relativeFqn);
		return buff.toString();
	}
	/**
	 * ドキュメント化対象ユニット定義を基底とするユニット定義完全名を生成する.
	 * @param target ドキュメント化対象もしくはその子孫にあたり直近ツリー要素出力の対象となっているユニット定義
	 * @return ユニット定義完全名
	 */
	public String createRelativeFqn(final Unit target) {
		final int lastSlash = root.getFullQualifiedName().lastIndexOf('/');
		return target.getFullQualifiedName().substring(lastSlash);
	}
	/**
	 * 当該ユニットがマップに表示すべきものかどうか判定する.
	 * @param unit ユニット定義
	 * @return 判定結果
	 */
	public boolean isViewable(final Unit unit) {
		return unit.getType() != UnitType.ROOT_JOBNET_INVOKE_CONDITION
				&& unit.getType() != UnitType.GROUP;
	}
}
