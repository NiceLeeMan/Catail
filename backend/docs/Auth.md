> Catail 백엔드는 OAuth2 소셜 로그인 기반의 Stateless 인증 방식을 사용한다. 별도의 자체 계정/패스워드 인증은 없으며, 모든 사용자는 소셜 로그인을 통해서만 가입·로그인할 수 있다.
>

## 1. 인증 방식

| 항목 | 내용 |
| --- | --- |
| 인증 프로토콜 | OAuth 2.0 Authorization Code Flow |
| 지원 제공자 | Google, Naver |
| 세션 정책 | Stateless (서버 세션 없음) |
| 토큰 방식 | JWT (Access Token + Refresh Token) |

## 2. OAuth2 로그인 플로우

```
클라이언트
  │
  ├─(1) GET /oauth2/authorization/{provider}
  │
  ▼
Spring Security OAuth2 Client
  │
  ├─(2) OAuth2 Provider(Google/Kakao/Naver)로 리다이렉트
  │
  ▼
사용자 소셜 로그인 완료
  │
  ├─(3) Authorization Code → 콜백 수신
  │
  ▼
CustomOAuth2UserService
  ├─ provider + providerId로 기존 유저 조회
  └─ 없으면 자동 회원가입 (닉네임 자동 생성)
  │
  ▼
OAuth2AuthenticationSuccessHandler
  ├─ Access Token 발급
  │   └─ 리다이렉트 URL 쿼리 파라미터로 전달
  │       예) redirect_uri?access_token=xxx&expires_at=xxx
  │       FE는 쿼리 파라미터에서 꺼내 메모리에 저장
  ├─ Refresh Token 발급
  │   ├─ refresh_tokens 테이블에 INSERT (user_id, jti, expires_at, created_at)
  │   └─ HttpOnly Cookie로 설정
  └─ 프론트엔드(redirectUri)로 리다이렉트
```

## 3. JWT 검증 필터

#### 3.1 인증 필요 정책

다음 엔드포인트는 인증 없이 접근 가능하다.

```
/oauth2/authorization/**   → OAuth 시작점
/login/oauth2/code/**      → OAuth 콜백
/auth/refresh              → 재발급 (Refresh Token으로 인증)
/auth/logout               → 로그아웃 (Refresh Token으로 처리)
```

`/auth/refresh`와 `/auth/logout`은 Access Token이 만료된 상태에서도 호출 가능해야 하므로 JWT 검증 필터 대상에서 제외한다. Access Token 검증을 걸어두면 재발급·로그아웃 자체가 불가능해지는 순환 문제가 발생한다.

위 네 가지를 제외한 모든 엔드포인트는 인증이 필요하다. 개발 환경과 운영 환경에 동일하게 적용하며, 개발 편의를 위한 전체 허용 설정은 두지 않는다.

#### 3.2 검증 실패 처리

JWT 검증은 서명 검증과 만료 시각(`exp`) 검증을 포함한다. 검증 실패 사유는 크게 세 가지다.

```
토큰 없음
서명이 유효하지 않음 (위조·변조)
만료됨
```

Catail의 토큰 관리 전략상 정상 사용자가 이 세 경우를 직접 겪을 가능성은 낮다.

```
토큰 없음       → 앱 초기화 시 자동 재발급으로 방지
서명 위조       → Refresh Token이 HttpOnly Cookie에 있어 탈취 자체가 어려움
만료됨          → 만료 5분 전 선제적 재발급으로 방지
```

따라서 검증 실패 사유를 구분해 다르게 응답하지 않는다. 사유와 무관하게 동일한 `401 Unauthorized`와 동일한 에러 메시지로 응답한다. 사유를 구분해 응답하면 공격자에게 "변조가 감지됨", "정상 만료" 등 공격 시도에 대한 피드백을 주는 정보 노출 위험이 있다.

## 4. 토큰 재발급 플로우

- `POST /auth/refresh` 요청에 대한 처리 절차다.

```
1. Cookie에서 Refresh Token 추출
2. JWT 서명 검증
   → 유효하지 않으면 401
3. jti로 refresh_tokens 테이블 조회
   → 행이 존재하지 않으면 401 (위조된 토큰)
4. 조회된 행의 유효성 확인
   → revoked_at IS NULL AND expires_at > NOW()
   → 조건 불충족 시: 이미 폐기되었거나 만료된 토큰으로 재요청한 것
     → 탈취 의심으로 간주, 해당 user_id의 모든 Refresh Token 폐기 후 401
5. RTR 수행 (하나의 트랜잭션으로 처리)
   → 기존 행 revoked_at 기록 (소프트 삭제)
   → 새 행 INSERT (새 jti)
6. 새 Access Token 발급
7. 응답
   → Access Token, 만료 시각: 쿼리 파라미터 또는 응답 바디
   → 새 Refresh Token: HttpOnly Cookie로 설정
```

5번과 6번(폐기+발급 / 새 Access Token 발급)은 하나의 트랜잭션 범위 안에서 처리되므로 순서 자체는 결과에 영향을 주지 않는다. 둘 중 하나만 성공하는 상황(부분 실패)을 막는 것이 트랜잭션 처리의 목적이다.

검증 실패(2, 3, 4번 단계)는 사유와 무관하게 동일한 `401 Unauthorized`로 응답한다.

### 5. 로그아웃 플로우

`POST /auth/logout` 요청에 대한 처리 절차다. Refresh Token Cookie 기반으로 인증하며, Access Token 검증은 거치지 않는다.

```
1. Cookie에서 Refresh Token 추출
2. JWT 서명 검증
   → 유효하지 않으면 401
3. jti로 refresh_tokens 테이블에서 revoked_at 기록 (존재하면)
4. Refresh Token Cookie 삭제
5. 응답
```

#### 검증 간소화 근거

재발급(`/auth/refresh`)과 달리 `revoked_at`/`expires_at` 유효성 검증(4번 단계에 해당하는 절차)은 생략한다.

**서명 검증(2번)은 유지한다.** 서명 검증이 없으면 jti가 토큰 소유자 본인의 요청인지 보장할 수 없어, 타인의 jti를 추측·탈취해 임의로 로그아웃시키는 서비스 거부 공격이 가능해진다.

**유효성 검증은 생략한다.** 재발급에서는 "이 토큰으로 새 토큰을 내줘도 되는가"를 가르는 분기였지만, 로그아웃은 토큰을 없애는 동작이라 이미 폐기되었거나 만료된 토큰이 들어와도 결과가 달라지지 않는다. 이미 무효 상태인 토큰에 무효 처리를 한 번 더 해도 변화가 없으므로 거부할 이유가 없다.

로그아웃이 실질적으로 막는 대상은 Refresh Token이 아니라 **아직 살아있는 Access Token의 자동 갱신 경로**다. Refresh Token이 이미 죽어있어도 Access Token은 최대 1시간 동안 유효하므로, 그동안 자동 재발급(`/auth/refresh`)이 차단되지 않으면 로그인 행세가 계속 가능하다. Refresh Token을 무효화함으로써 자동 갱신 경로를 끊고, 남은 유일한 경로를 OAuth 수동 재로그인으로 한정한다.