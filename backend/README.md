# LocationMapper

## What's this
[NMEA0183](https://www.hiramine.com/physicalcomputing/general/gps_nmeaformat.html)形式のGPSログデータから、DaVinci Resolve等の動画編集ソフトで読み込める連番画像を生成するWebアプリ(の、予定)  

## 背景
愛車に付いているドライブレコーダー(以下、ドラレコ)にはGPSが搭載されていて、映像と一緒にGPSログを記録してくれる機能が付いているので、  
それを利用して地図付きの車載動画を作りたいと思い立つも、いい感じのツールが無かったので作ることにした。

## 構想
### 処理フロー
1. NMEA0183形式のGPSログデータをアップロードする。
2. 1でアップロードしたデータからGPRMCセンテンスを抽出し、地図APIより対応する緯度経度の地図画像を取得する。
3. 2で取得した地図画像をFPSの数だけ複製し、ZIPアーカイブに固める。
4. 3で作成したZIPアーカイブをレスポンスとして返す。

### 実装形式について
#### バックエンド
* 言語 - Kotlin
* F/W - Spring Boot
#### フロントエンド
* 言語 - TypeScript
* F/W - React
#### その他、使用するライブラリとか
* 地図APIとして、OpenStreatMapベースの地図APIである[MapQuest](https://developer.mapquest.com/documentation/)を使用。
