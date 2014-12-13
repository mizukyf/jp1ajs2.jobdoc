package com.m12i.jp1ajs2.jobdoc.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.m12i.jp1ajs2.jobdoc.Parameters;

import com.m12i.jp1ajs2.unitdef.Unit;
import com.m12i.jp1ajs2.unitdef.MapSize;
import com.m12i.jp1ajs2.unitdef.Params;
import com.m12i.jp1ajs2.unitdef.util.Maybe;

/**
 * ユニット定義をもとに各種情報を収集するオブジェクト.
 */
public class Traverser {
	/**
	 * ユニット定義情報のツリー構造をリスト構造に変換する.
	 * @param root ベースとなるユニット
	 * @return ベースとなるユニットとその子孫ユニットからなるリスト
	 */
	public List<Unit> makeFlattenedUnitList(final Unit root) {
		final List<Unit> result = new ArrayList<Unit>();
		makeFlattenedUnitListHelper(result, root);
		return result;
	}
	
	/**
	 * {@link #makeFlattenedUnitList(Unit)}メソッドのためのヘルパ関数.
	 * ユニットのツリー構造を深さ優先で再帰的にたどりながらリストを組み立てていく。
	 * @param list リスト
	 * @param unit ユニット
	 */
	private void makeFlattenedUnitListHelper(final List<Unit> list, final Unit unit) {
		list.add(unit);
		for (final Unit child : unit.getSubUnits()) {
			makeFlattenedUnitListHelper(list, child);
		}
	}
	
	/**
	 * ドキュメント化対象となるユニットを検索・収集する.
	 * 戻り値のマップのキーはユニット名である。
	 * ベースとなるユニットおよびその子孫ユニットのなかに同じユニット名が複数回登場する場合は後勝ちとなる。
	 * @param root ベースとなるユニット
	 * @param params パラメータ
	 * @return 検索・収集結果のマップ（キーはユニット名）
	 */
	public Map<String,Unit> collectTargetUnits(final Unit root, final Parameters params) {
		final Map<String,Unit> result = new HashMap<String, Unit>();
		collectTargetUnitsHelper(result, root, params);
		return result;
	}
	
	/**
	 * {@link #collectTargetUnits(Unit, Parameters)}メソッドのためのヘルパ関数.
	 * ユニットのツリー構造を深さ優先で再帰的にたどりながらマップを組み立てていく。
	 * @param map マップ
	 * @param root ベースとなるユニット
	 * @param params パラメータ
	 */
	private void collectTargetUnitsHelper(Map<String,Unit> map, final Unit root, final Parameters params) {
		final String t = params.getTargetUnitName();
		final Pattern p = params.getTargetUnitNamePattern();
		if (t != null && root.getName().equals(t)) {
			map.put(t, root);
		}
		if (p != null && p.matcher(root.getFullQualifiedName()).matches()) {
			map.put(root.getName(), root);
		}
		for (final Unit child : root.getSubUnits()) {
			collectTargetUnitsHelper(map, child, params);
		}
	}
	
	/**
	 * 対象のユニットとその子孫ユニットからなるツリー構造の最大深度を計測する.
	 * @param unit ユニット
	 * @return 最大深度
	 */
	public int measureMaxDepth(final Unit unit) {
		// 深さの最大値を格納する変数を初期化（深さの規定値は1）
		// ＊もし現在ユニットが子ユニットを持たないならこの深さ1がそのまま計測結果となる
		int max = 1;
		// 子ユニットを総当りでチェック
		for (final Unit child : unit.getSubUnits()) {
			// まず子ユニットの深さを求め、そこに自ユニットの分の深さ1を加える
			int tmp = 1 + measureMaxDepth(child);
			// 直前の子ユニットから求めた深さと現在の子ユニットから求めた深さとを比較
			// 現在のもののほうが深いようであれば、その値を深さの最大値を格納する一次変数に設定
			max = tmp > max ? tmp : max; 
		}
		// 現在ユニットとその子ユニットから算出された深さを呼び出し元に返す
		return max;
	}
	
	/**
	 * 対象のユニットとその子孫ユニットのなかで最大の面積をとるマップサイズ情報を返す.
	 * マップサイズを持つユニット（ジョブネットユニットなど）が存在しない場合、
	 * タテ・ヨコがいずれも0のマップサイズ・オブジェクトが返される。
	 * @param unit ユニット
	 * @return 最大マップサイズ
	 */
	public MapSize measureMaxArea(List<Unit> flattenedList) {
		// 最大のマップサイズ（初期値として0x0を設定）
		MapSize maxMapSize = new MapSize() {
			public int getWidth() {
				return 0;
			}
			public int getHeight() {
				return 0;
			}
		};
		// 最大面積（マップサイズから求まる）
		int max = 0;
		
		// ユニットを総当りでチェック
		for (final Unit u : flattenedList) {
			// マップサイズ情報の取得を試みる
			final Maybe<MapSize> o = Params.getMapSize(u);
			// 当該パラメータが存在するかチェック
			if (o.isOne()) {
				// マップサイズ情報を取得する
				final MapSize tmpMapSize = o.get();
				// 面積を求める
				final int tmp = tmpMapSize.getWidth() * tmpMapSize.getHeight();
				// 現在ユニットの面積と直前までのユニットの面積のうち最大のものとを比較
				if (tmp > max) {
					// もし現在ユニットの面積のほうが大きいようであれそれを一次変数に設定
					maxMapSize = tmpMapSize;
					max = tmp;
				}
			}
		}
		// 最大面積となるマップサイズを呼び出し元に返す
		return maxMapSize;
	}	

}
