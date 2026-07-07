---
name: commit-split
description: 작업이 끝난 후 변경사항을 논리적 커밋 단위로 나누고 컨벤션에 맞는 메시지를 작성한다
allowed-tools: Bash(git status:*), Bash(git diff:*), Bash(git add:*), Bash(git commit:*), Read
---

# 커밋 분리 및 메시지 작성

## 1. 변경사항 전체 확인
!`git status`
!`git diff`

## 2. 분리 기준
파일 단위가 아니라 **논리 단위**로 나눈다.
- 하나의 커밋 = 하나의 의미 있는 변경 (기능 하나, 리팩터링 하나, 관련 테스트 포함)
- 서로 무관한 변경(예: 카탈리스트 기능 + 인증 버그 수정)이 섞여 있으면 반드시 분리
- 테스트 코드는 대응하는 구현 코드와 같은 커밋에 포함 (TDD 사이클 단위 유지)

## 3. 커밋 메시지 컨벤션
.claude/PROCESS/02_commit.md 참조.
형식: `유형: 한줄 요약` + 선택적 bullet body

유형 예시: feat, fix, refactor, docs, test, chore

## 4. 실행 절차
1. 커밋 단위 분리안을 먼저 사용자에게 제시한다 (git add 실행 전).
2. 사용자 확인 후에만 `git add -p` 또는 특정 파일 지정으로 스테이징.
3. 커밋 실행.
4. 한 커밋 처리 후 남은 변경사항 다시 확인, 반복.

⚠️ 분리안 제시 없이 임의로 git add/commit을 연속 실행하지 않는다.