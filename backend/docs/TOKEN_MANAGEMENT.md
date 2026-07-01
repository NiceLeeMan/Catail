## 1. 가용할 토큰 종류

Access Token + Refresh Token 둘 다 사용.

Access Token 단독 운용은 수명 딜레마가 생김.

- 짧게: 보안은 좋으나 사용자가 자주 재로그인
- 길게: UX는 좋으나 탈취 시 피해 장기화

Refresh Token 도입으로 Access Token은 짧은 수명으로 보안을 챙기고,
Refresh Token으로 자동 재발급해서 UX를 챙김. Catail은 모니터링 서비스 특성상 장시간 세션 유지가 필요하므로 적합한 선택.

## 2. 토큰별 서명 알고리즘, 만료기간, 페이로드

### 2.1 Access Token

- 서명 알고리즘 : HMAC-SHA (HS256+)
- 만료 시간 : 1시간 (3600초)
- **Payload Claims**

| 클레임 | 타입 | 설명 |
| --- | --- | --- |
| `sub` | String | 내부 사용자 PK |
| `role` | String | 사용자 역할 (`USER`) |
| `iat` | Long | 발급 시각 |
| `exp` | Long | 만료 시각 |

### 2.2 Refresh Token

- 서명 알고리즘 : HMAC-SHA (HS256+)
- 만료 시간 : 5일 (432,000초)
- **Payload Claims**

| 클레임 | 타입 | 설명 |
| --- | --- | --- |
| `sub` | String | 내부 사용자 PK |
| `jti` | String (UUID) | Refresh Token 고유 식별자 |
| `iat` | Long | 발급 시각 |
| `exp` | Long | 만료 시각 |
- sub의 밸류값은 userId랑 같음. Long→String해서 담은 값임

## 3.서버단 토큰 관리 방식

### 3.1 저장소 선택

- Refresh Token은 DB에 저장한다.
    - Refresh Token은 DB에 저장한다. 추후 Redis를 도입할 수 있으나, 이는 Refresh Token 저장 목적이 아니며 도입한다 해도 요청 빈도가 높거나 단일 트랜잭션 내 데이터 처리량이 많은 영역에 한정해서 활용할 것이다.
    - Refresh Token은 재발급 요청 시에만 조회가 발생하므로 접근 빈도가 낮고, 발급/검증/무효화 과정에서 트랜잭션 보장이 필요하므로 RDBMS가 적합하다.

### 3.2 테이블 스키마 설계

Refresh Token은 `refresh_tokens` 테이블에 저장한다. `users` 테이블에 컬럼을 추가하는 방식은 사용자당 하나의 토큰만 저장할 수 있어 멀티 디바이스 동시 로그인을 지원할 수 없으므로 채택하지 않는다.

재발급 시 기존 행을 삭제하고 새 행을 INSERT하는 전략을 사용한다. UPDATE 방식은 토큰 이력 추적이 불가능하고 Refresh Token Rotation 구현이 어색해지는 기능적 한계가 있는 반면, DELETE → INSERT 방식의 단점인 행 누적은 만료 토큰 정리 스케줄러로 해결 가능한 운영 부담 수준이다.

#### 스키마

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | BIGINT | PK | 내부 식별자 |
| `user_id` | BIGINT | FK, NOT NULL | 토큰 소유 사용자 |
| `jti` | VARCHAR | UNIQUE, NOT NULL | Refresh Token 고유 식별자 (UUID) |
| `expires_at` | TIMESTAMP | NOT NULL | 만료 시각 |
| `revoked_at` | TIMESTAMP | NULLABLE | 무효화 시각. NULL이면 유효한 상태 |
| `created_at` | TIMESTAMP | NOT NULL | 토큰 발급 시각 |

#### 유효성 판단 조건

토큰이 유효한 상태는 다음 두 조건을 모두 만족할 때다.

> revoked_at IS NULL AND expires_at > NOW()
>

#### 설계 결정 근거

- **`users` 테이블 분리**: 인증 관심사를 User 도메인에서 분리하고, 멀티 디바이스 동시 로그인을 지원하기 위해 별도 테이블로 분리한다.
- **`jti` 저장**: `user_id`만으로는 동일 사용자의 복수 토큰 중 특정 토큰을 식별할 수 없다. `jti`를 저장해 기기 단위 로그아웃과 토큰 탈취 시 핀포인트 무효화를 가능하게 한다.
- **`revoked_at` 사용**: Boolean 타입의 `is_revoked` 대신 Timestamp 타입을 사용해 무효화 여부뿐 아니라 무효화 시각까지 기록한다.
- **`created_at` 추가**: DELETE → INSERT 전략에서 각 행은 하나의 토큰 발급 이력을 의미하므로 발급 시각이 명확한 의미를 가진다.

## 4.브라우저단 토큰 저장 방식

#### 4.1 Access Token

Access Token은 **브라우저 메모리(JS 변수)에 저장**한다. LocalStorage와 Cookie 방식과 비교했을 때 다음 근거로 선택했다.

- LocalStorage는 XSS 공격으로 JS가 값을 직접 읽어갈 수 있어 토큰 탈취에 취약하다.
- Cookie는 CSRF 공격 벡터가 존재하며, Access Token 수준에서는 과한 보호 조치다.
- Access Token 수명이 1시간으로 짧아 탈취되더라도 피해 window가 제한적이다.
- 메모리 저장은 XSS, CSRF 두 공격 모두 저장소 레벨에서 고민할 필요가 없어진다.

**단점**: 새로고침 시 메모리가 초기화되어 Access Token이 날아간다. 이 경우 Refresh Token으로 자동 재발급하여 복구한다.

#### 4.2 Refresh Token

Refresh Token은 HttpOnly Cookie에 저장한다. 수명이 5일로 길기 때문에 탈취 시 피해가 장기화될 수 있어 더 강한 보호가 필요하다.

LocalStorage 대신 Cookie를 선택한 근거는 다음과 같다.

- LocalStorage는 XSS로 토큰 값 자체를 탈취당할 수 있다. 탈취된 값은 공격자가 자신의 환경에서 자유롭게 재발급 요청에 사용 가능하다.
- HttpOnly Cookie는 JS로 값에 접근이 불가능해 XSS로 값 자체를 탈취할 수 없다.
- CSRF는 요청을 위조하는 공격이라 토큰 값을 직접 가져가지 못한다. SameSite 속성으로 추가 방어 가능하다.

#### Cookie 속성

| 속성 | 값 | 설명 |
| --- | --- | --- |
| `HttpOnly` | true | JS 접근 차단, XSS 방어 |
| `Secure` | 운영 true / 개발 false | HTTPS에서만 전송 |
| `Path` | `/auth/refresh` | 재발급 엔드포인트에만 쿠키 전송 |
| `SameSite` | 배포 구조 확정 후 결정 | 같은 도메인이면 `Lax`, 크로스 도메인이면 `None` + `Secure` |

## 5.토큰 재발급 전략

**5.1 재발급 타이밍**

Access Token은 다음 두 시점에 재발급한다.

**앱 초기화 시**

새로고침 등으로 메모리가 초기화되면 Access Token이 소멸한다. 앱 초기화 시점에 무조건 재발급 요청을 1회 보내 복구한다. Refresh Token은 HttpOnly Cookie에 살아있으므로 자동으로 요청에 포함된다.

**만료 5분 전 선제적 재발급**

최초 로그인 및 매 재발급 응답 시 서버가 Access Token 만료 시각을 함께 반환한다. FE는 이를 메모리에 저장하고 만료 5분 전에 선제적으로 재발급 요청을 보낸다. 만료 후 요청 실패 시 재발급하는 방식은 사용자 요청이 한 번 실패하는 UX 저하가 있어 채택하지 않는다.

#### 5.2 Refresh Token Rotation (RTR)

재발급 요청 시 Access Token뿐 아니라 Refresh Token도 함께 교체한다. 기존 Refresh Token을 폐기하고 새 Refresh Token을 발급하는 DELETE → INSERT 구조로 처리한다.

#### 5.3 재발급 시 기존 토큰 처리 방식

재발급 시 기존 Refresh Token은 즉시 물리적으로 삭제(hard delete)하지 않고, `revoked_at`을 기록하는 소프트 삭제 방식으로 처리한다. 행을 즉시 삭제하면 이후 동일 `jti`로 재요청이 들어왔을 때 "이미 폐기된 토큰"인지 "애초에 존재한 적 없는 토큰"인지 구분할 수 없어, RTR의 탈취 감지 기능 자체가 무력화된다.

```
재발급 시: 기존 행 revoked_at 기록 (소프트 삭제) + 새 행 INSERT
스케줄러: revoked_at IS NOT NULL 또는 expires_at < NOW()인 행을 주기적으로 물리 삭제
```

소프트 삭제된 행으로 재요청이 들어오면 탈취 시도로 간주해 해당 사용자의 전체 Refresh Token을 폐기한다. 물리적 삭제는 별도 스케줄러가 일괄 처리하며, User 도메인의 90일 후 실제 삭제 스케줄러와 동일한 패턴이다.

**도입 근거**

Refresh Token이 탈취되더라도 사용자의 다음 재발급 요청 시점에 공격자 토큰을 폐기할 수 있다. 탈취 즉시 감지는 불가능하지만 피해 window를 Access Token 수명(1시간) 단위로 제한한다.

## 6.로그아웃 처리 방식

#### 6.1 로그아웃 범위

현재 기기만 로그아웃한다. 해당 기기의 `jti`에 해당하는 Refresh Token 하나만 폐기한다. 전체 기기 로그아웃은 MVP 범위에 포함하지 않는다.

#### 6.2 처리 흐름

```
사용자 로그아웃 요청
→ BE: 해당 jti의 Refresh Token revoked_at 기록
→ BE: Refresh Token Cookie 삭제
→ FE: Access Token 메모리 초기화 (null)
→ 로그인 화면으로 이동
```

#### 6.3 Access Token 처리

Access Token은 서버에 저장하지 않으므로 서버가 직접 무효화할 수 없다. FE에서 메모리를 null로 초기화하는 것으로 처리한다.

로그아웃 후 Access Token 수명이 남아있더라도 Refresh Token이 폐기되어 재발급이 불가능하므로 최대 1시간 후 자연소멸한다. Access Token 수명을 1시간으로 짧게 유지하는 이유 중 하나이기도 하다.

#### 6.4 Refresh Token 처리

서버단 로그아웃의 실질적인 핵심이다. Refresh Token이 폐기되지 않으면 FE가 Access Token을 null로 만들어도 앱 초기화 시점에 재발급 요청으로 즉시 복구되어 로그인 상태가 유지된다