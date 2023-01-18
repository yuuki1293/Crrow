# APIドキュメント
## Example
### PowerShell
```shell
# 新しくユーザーを作成する
$uri = 'http://localhost:8081/user'
$body = [System.Text.Encoding]::UTF8.GetBytes('{"name":"高専太郎","birthday":"2004-05-27","sex":1,"introduction":"こんにちは世界","nickname":"kosentr","icon":"dummy","email":"example@example.com","school":"富山高専","password":"1jfc21#fc"}')
curl -Method Post -Uri $uri -Body $body -ContentType 'application/json' 
```

```shell
# アクセストークンを取得する
$uri = 'http://localhost:8081/login'
$body = [System.Text.Encoding]::UTF8.GetBytes('{"name":"高専太郎","password":"1jfc21#fc"}')
curl -Method Post -Uri $uri -Body $body -ContentType 'application/json'
```

```shell
# ユーザーを更新する
$uri = 'http://localhost:8081/user'
$body = [System.Text.Encoding]::UTF8.GetBytes('{"birthday":"2004-12-27","sex":0,"introduction":"こんにちは世界","nickname":"kosentr","icon":"dummy","email":"example@example.com","school":"福島高専","range":10}')
$token = 'access token here...'
$headers = @{
    Authorization="Bearer $token"
}
curl -Method Put -Uri $uri -Body $body -ContentType 'application/json' 
```

## Base URL
http://localhost:8081
## Users
### Userを作成
POST /user  
新しくユーザーを作成する。
#### Body
```json
{
  "name": "string",
  "birthday": "yyyy-mm-dd",
  "sex": 0,
  "introduction": "hello",
  "nickname": "string",
  "icon": "string",
  "email": "example@example.com",
  "school": "string",
  "password": "string"
}
```
#### Detail
**name**: 本名（15文字）  
**birthday**: 誕生日  
**sex**: 性別（0.無回答 1.男 2.女 9.その他）  
**introduction**: 自己紹介（200文字）  
**nickname**: ニックネーム（20文字）  
**icon**: アイコンのファイル名（32文字）  
**email**: メールアドレス（20文字、任意）  
**school**: 学校名（13文字）
**password**: パスワード（50文字以下）
#### Responses
| Status | Description | Schema |
|:------:|:------------|:-------|
|  201   | none        | none   |
|  400   | none        | none   |
|  500   | none        | none   |

### Userを削除
DELETE /user  
既存のユーザーを削除する。
#### Body
```json
{
  "name": "string",
  "password": "string"
}
```
#### Detail
**name**: ユーザー名
**password**: パスワード
#### Responses
| Status | Description      | Schema |
|:------:|:-----------------|:-------|
|  205   | none             | none   |
|  401   | 無効なユーザー名またはパスワード | none   |
|  500   | none             | none   |

### Profileを取得
GET /user
既存のプロフィールを取得する。
#### Responses
| Status | Description | Schema |
|:------:|:------------|:-------|
|  200   | none        | none   |
|  404   | none        | none   |
|  500   | none        | none   |

### Profileを更新
PUT /user
既存のプロフィールを更新する。
#### Detail
**id**: ユーザーのid 
**birthday**: 誕生日  
**sex**: 性別（0.無回答 1.男 2.女 9.その他）  
**introduction**: 自己紹介（200文字）  
**nickname**: ニックネーム（20文字）  
**icon**: アイコンのファイル名（32文字）  
**email**: メールアドレス（20文字、任意）。  
**school**: 学校名（13文字）
#### Responses
| Status | Description | Schema |
|:------:|:------------|:-------|
|  200   | none        | none   |
|  404   | none        | none   |
|  500   | none        | none   |

## Login
### AccessTokenを取得
POST /login  
Tokenを取得する。
#### Body
```json
{
  "name": "string",
  "password": "string"
}
```
#### Detail
**name**: ユーザー名
**password**: パスワード  
#### Responses
| Status | Description      | Schema          |
|:------:|:-----------------|:----------------|
|  200   | none             | [Token](#Token) |
|  401   | 無効なユーザー名またはパスワード | none            |
|  500   | none             | none            |

## Schemas
### Token
```json
{
  "token": "access token"
}
```