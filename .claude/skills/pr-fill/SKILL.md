---
name: pr-fill
description: 현재 브랜치의 변경사항을 기반으로 PR 템플릿을 자동 채운다
allowed-tools: Bash(git diff:*), Bash(git log:*), Bash(git branch:*), Read
---

# PR 자동 작성

## 1. 변경 범위 파악
!`git diff develop...HEAD --stat`
!`git log develop..HEAD --oneline`

## 2. 템플릿 선택
브랜치명 prefix로 판단한다 (예: feature/, refactor/, bugfix/, docs/).
해당하는 템플릿을 읽는다:
- feature/* → .github/PULL_REQUEST_TEMPLATE/FEATURE.md
- refactor/* → .github/PULL_REQUEST_TEMPLATE/REFACTOR.md
- bugfix/* 또는 fix/* → .github/PULL_REQUEST_TEMPLATE/BUG.md
- docs/* → .github/PULL_REQUEST_TEMPLATE/DOCS.md

애매하면 diff 내용(코드 변경 vs 문서 변경 vs 버그 패턴)으로 재판단하고, 그래도 애매하면 사용자에게 묻는다.

## 3. 섹션별 작성 규칙

- **작업 배경**: git log 커밋 메시지 + 연결된 이슈 본문(있다면)에서 추론. 추측이면 "(확인 필요)" 표시, 지어내지 않는다.
- **결정 사항 / As-Is→To-Be**: diff에서 구조적 변경(파일 이동, 클래스 분리, 의존성 변경)이 보이면 초안 작성. 코드만 보고 "왜" 그렇게 했는지는 알 수 없으므로, 근거(근거:)는 커밋 메시지에 없으면 빈칸으로 남기고 사용자가 채우게 한다.
- **체크리스트**: 자동 체크하지 않는다. 항목만 템플릿 그대로 유지.
- **연관 도메인 영향**: diff에서 FK가 걸린 테이블/공유 API 파일 변경이 감지되면 표로 초안 작성.

## 4. 출력
템플릿 형식 그대로, 채운 내용과 빈칸을 구분해서 markdown으로 출력한다.
빈칸으로 남긴 항목은 마지막에 "⚠️ 직접 작성 필요:" 목록으로 요약한다.