## 開発環境
* Android Studio(v3.6.1)

# リリース方法

1. abstracthttp/build.gradle に記載の`abstractHttpVersion`の数字を更新する。
2. プロジェクト直下にある`./buildAndArchive.sh`を実行する
3. GitHubリポジトリにPUSHする

## 内包ライブラリ（サードパーティ）
* なし

## アプリへの組み込み方法
以下を`build.gradle`に記述することでダウンロード可能。

```
repositories {
    maven { url 'http://altonotes.github.io/Android-Kibaan/repository' }
}
```

```
dependencies {
	implementation 'jp.co.altonotes.abstracthttp:abstracthttp:0.7.00' // バージョンは最新を確認する
}
```

## デバッグ方法

AbstractHttpをアプリに組み込んだ状態でAbstractHttp内のクラスのデバッグを行いたい場合は以下の設定を行う。

- 本プロジェクトをGitHubからCloneし、アプリのプロジェクトと同じディレクトリに配置する
- settings.gradle の内容をいかに変更

```
include ':app', ':abstracthttp'
project(':abstracthttp').projectDir = new File('../Android-Kibaan/abstracthttp')
```

- build.gradle のAbstractHttpのdependencyを以下に変更

```
implementation project(':abstracthttp')
```