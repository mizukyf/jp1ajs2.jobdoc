package com.m12i.jp1ajs2.jobdoc.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.m12i.jp1ajs2.jobdoc.JobdocError;
import com.m12i.jp1ajs2.jobdoc.Messages;
import com.m12i.jp1ajs2.jobdoc.Parameters;

import usertools.jp1ajs2.unitdef.core.ParseUtils;
import usertools.jp1ajs2.unitdef.core.Unit;
import usertools.jp1ajs2.unitdef.util.Either;

/**
 * ユニット定義ファイルのパース処理を担当するオブジェクト.
 */
public class Parser {
	/**
	 * ユニット定義ファイルをパースする.
	 * @param params パラメータ
	 * @return ユニット定義オブジェクト
	 */
	public Unit parseSourceFile(Parameters params) {
		try {
			final Either<Throwable, Unit> e = ParseUtils
					.parse(new FileInputStream(params.getSourceFile()),
							params.getSourceFileCharset().name());
			if (e.isRight()) {
				return e.right();
			} else {
				throw new JobdocError(Messages.PARSE_ERROR_HAS_OCCURED, e.left());
			}
		} catch (final FileNotFoundException e) {
			throw new JobdocError(Messages.PARSE_ERROR_HAS_OCCURED, e);
		}
	}
}
