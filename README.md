# jp1ajs2.jobdoc

`jp1ajs2.jobdoc`はJP1/AJS2ユニット定義ファイルをパースしてドキュメント化するツールです。
現在のところドキュメントの形式はHTMLファイルのみに限定されています。

`java -jar jp1ajs2.jobdoc-(version).jar`コマンドをコマンドライン引数なしに実行するとUSAGEが表示されます。
適切なコマンドを設定して実行するとドキュメント化が行われます。

```
$ java -jar jp1ajs2.jobdoc-1.3.2-RELEASE-jar-with-dependencies.jar 
usage: java -jar jp1ajs2.jobdoc.jar <options>
 -d <destination-directory-path>
                                                  ドキュメントを出力するディレクトリのパス（デフォ
                                                  ルトは"."）
    --dry-run
                                                  ユニット定義のパースとドキュメント化対象の特定ま
                                                  で行うが実際のドキュメント化は行わない
 -s <source-file-path>                            読み取り対象のユニット定義ファイルのパス
    --source-file-charset <source-file-charset>
                                                  読み取り対象のユニット定義ファイルのキャラクタセ
                                                  ット（デフォルトは"Windows-31J"）
 -t <target-unit-name>                            ドキュメント化する対象のユニット名
 -T <target-unit-name-pattern>
                                                  ドキュメント化する対象のユニット名の正規表現パタ
                                                  ーン
```
ドキュメント化が行われるとアプリケーション・ログがカレント・ディレクトリに出力されます。
ログファイルはデフォルトでは10MBに達した段階でリネームされ、過去5世代分まで保管されます。
この挙動は独自の`log4j.properties`ファイルを用意することで変更可能です。

## ビルド方法について

ビルドにはMavenを使用しています。`mvn package`コマンドを実行すると依存するライブラリのコードも含む単一の実行可能JARファイルが作成されます。

なお、このツールが依存するJP1/AJS2のユニット定義をパースするためのライブラリ[jp1ajs2.unitdef](https://github.com/mizukyf/jp1ajs2.unitdef)はオンラインのMavenリポジトリには登録されていないため、
ビルドの前にそれらのライブラリをローカル・リポジトリへ登録する必要があります。

*nix環境であれば、プロジェクトのベース・ディレクトリで以下コマンドを実行することで、ローカル・リポジトリへの登録ができます：

```
cd path/to/unitdef-project
mvn install
```

