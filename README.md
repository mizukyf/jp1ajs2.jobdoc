# jp1ajs2.jobdoc

`jp1ajs2.jobdoc`はJP1/AJS2ユニット定義ファイルをパースしてドキュメント化するツールです。
現在のところドキュメントの形式はHTMLファイルのみに限定されています。

## ビルド方法について

ビルドにはMavenを使用しています。`mvn package`コマンドを実行すると依存するライブラリのコードも含む単一の実行可能JARファイルが作成されます。

なお、このツールが依存するJP1/AJS2のユニット定義をパースするためのライブラリはオンラインのMavenリポジトリには登録されていないため、
ビルドの前にそれらのライブラリをローカル・リポジトリへ登録する必要があります。

*nix環境であれば、プロジェクトのベース・ディレクトリで以下コマンドを実行することで、ローカル・リポジトリへの登録ができます：

```
mvn install:install-file -Dfile=lib/code-parse-3.0.1.jar -DgroupId=com.m12i.code.parse -DartifactId=code-parse -Dversion=3.0.1 -Dpackaging=jar
mvn install:install-file -Dfile=lib/query-parse-1.0.0.jar -DgroupId=com.m12i.query.parse -DartifactId=query-parse -Dversion=1.0.0 -Dpackaging=jar
mvn install:install-file -Dfile=lib/usertools.jp1.ajs2.unitdef-1.3.0.jar -DgroupId=usertools.jp1.ajs2.unitdef -DartifactId=usertools.jp1.ajs2.unitdef -Dversion=1.3.0 -Dpackaging=jar
```

