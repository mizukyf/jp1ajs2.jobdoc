package org.doogwood.jp1ajs2.jobdoc.service;

import java.io.FileInputStream;
import java.io.IOException;

import org.doogwood.jp1ajs2.jobdoc.JobdocError;
import org.doogwood.jp1ajs2.jobdoc.Messages;
import org.doogwood.jp1ajs2.jobdoc.Parameters;
import org.doogwood.jp1ajs2.unitdef.Unit;
import org.doogwood.jp1ajs2.unitdef.Units;
import org.springframework.stereotype.Service;

/**
 * ユニット定義ファイルのパース処理を担当するオブジェクト.
 */
@Service
public class ParseService {
	
	/**
	 * ユニット定義ファイルをパースする.
	 * @param params パラメータ
	 * @return ユニット定義オブジェクト
	 */
	public Unit parseSourceFile(Parameters params) {
		try {
			return Units.fromStream(new FileInputStream(params.getSourceFile()),
							params.getSourceFileCharset());
		} catch (final IllegalArgumentException e) {
			throw new JobdocError(Messages.PARSE_ERROR_HAS_OCCURED, e);
		} catch (final IOException e) {
			throw new JobdocError(Messages.PARSE_ERROR_HAS_OCCURED, e);
		}
	}
}
