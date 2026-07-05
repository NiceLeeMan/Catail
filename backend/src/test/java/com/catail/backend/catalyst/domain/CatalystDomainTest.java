package com.catail.backend.catalyst.domain;

import com.catail.backend.catalyst.DB.CatalystStatus;
import com.catail.backend.global.BusinessException;
import com.catail.backend.global.GlobalErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatalystDomainTest {

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("최소 경계값(title 1자, content 50자, industryIds 1개)이면 정상 생성된다")
        void create_minBoundary_succeeds() {
            CatalystDomain domain = CatalystDomain.create(1L, "a", "a".repeat(50), List.of(1L), "ACTIVE");

            assertThat(domain.getTitle()).isEqualTo("a");
            assertThat(domain.getContent()).hasSize(50);
            assertThat(domain.getIndustryIds()).containsExactly(1L);
            assertThat(domain.getStatus()).isEqualTo(CatalystStatus.ACTIVE);
            assertThat(domain.getSearchConditions()).isEmpty();
        }

        @Test
        @DisplayName("최대 경계값(title 50자, content 500자, industryIds 10개)이면 정상 생성된다")
        void create_maxBoundary_succeeds() {
            List<Long> industryIds = List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);

            CatalystDomain domain = CatalystDomain.create(1L, "a".repeat(50), "a".repeat(500), industryIds, "INACTIVE");

            assertThat(domain.getTitle()).hasSize(50);
            assertThat(domain.getContent()).hasSize(500);
            assertThat(domain.getIndustryIds()).hasSize(10);
        }

        @Test
        @DisplayName("title의 앞뒤 공백은 trim되어 저장된다")
        void create_titleWithSurroundingWhitespace_trims() {
            CatalystDomain domain = CatalystDomain.create(1L, "  제목  ", "a".repeat(50), List.of(1L), "ACTIVE");

            assertThat(domain.getTitle()).isEqualTo("제목");
        }

        @Test
        @DisplayName("title이 trim 후 빈 문자열이면 INVALID_INPUT 예외가 발생한다")
        void create_blankTitle_throwsInvalidInput() {
            assertThatThrownBy(() -> CatalystDomain.create(1L, "   ", "a".repeat(50), List.of(1L), "ACTIVE"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(GlobalErrorCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("title이 trim 후 50자를 초과하면 INVALID_INPUT 예외가 발생한다")
        void create_titleTooLong_throwsInvalidInput() {
            assertThatThrownBy(() -> CatalystDomain.create(1L, "a".repeat(51), "a".repeat(50), List.of(1L), "ACTIVE"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(GlobalErrorCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("title에 제어문자(줄바꿈)가 포함되면 INVALID_INPUT 예외가 발생한다")
        void create_titleWithControlChar_throwsInvalidInput() {
            assertThatThrownBy(() -> CatalystDomain.create(1L, "제목\n입니다", "a".repeat(50), List.of(1L), "ACTIVE"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(GlobalErrorCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("content가 trim 후 50자 미만이면 INVALID_INPUT 예외가 발생한다")
        void create_contentTooShort_throwsInvalidInput() {
            assertThatThrownBy(() -> CatalystDomain.create(1L, "title", "a".repeat(49), List.of(1L), "ACTIVE"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(GlobalErrorCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("content가 trim 후 500자를 초과하면 INVALID_INPUT 예외가 발생한다")
        void create_contentTooLong_throwsInvalidInput() {
            assertThatThrownBy(() -> CatalystDomain.create(1L, "title", "a".repeat(501), List.of(1L), "ACTIVE"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(GlobalErrorCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("content에 제어문자가 포함되면 INVALID_INPUT 예외가 발생한다")
        void create_contentWithControlChar_throwsInvalidInput() {
            assertThatThrownBy(() -> CatalystDomain.create(1L, "title", "a".repeat(25) + "\t" + "a".repeat(25), List.of(1L), "ACTIVE"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(GlobalErrorCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("industryIds가 비어있으면 INVALID_INPUT 예외가 발생한다")
        void create_emptyIndustryIds_throwsInvalidInput() {
            assertThatThrownBy(() -> CatalystDomain.create(1L, "title", "a".repeat(50), List.of(), "ACTIVE"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(GlobalErrorCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("industryIds가 11개 이상이면 INVALID_INPUT 예외가 발생한다")
        void create_tooManyIndustryIds_throwsInvalidInput() {
            List<Long> industryIds = List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L);

            assertThatThrownBy(() -> CatalystDomain.create(1L, "title", "a".repeat(50), industryIds, "ACTIVE"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(GlobalErrorCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("industryIds에 중복이 있으면 INVALID_INPUT 예외가 발생한다")
        void create_duplicateIndustryIds_throwsInvalidInput() {
            assertThatThrownBy(() -> CatalystDomain.create(1L, "title", "a".repeat(50), List.of(1L, 1L), "ACTIVE"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(GlobalErrorCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("status가 PAUSED이면(유효한 enum이지만 생성 시 허용되지 않음) INVALID_INPUT 예외가 발생한다")
        void create_pausedStatus_throwsInvalidInput() {
            assertThatThrownBy(() -> CatalystDomain.create(1L, "title", "a".repeat(50), List.of(1L), "PAUSED"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(GlobalErrorCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("status가 존재하지 않는 값이면 INVALID_INPUT 예외가 발생한다")
        void create_unknownStatus_throwsInvalidInput() {
            assertThatThrownBy(() -> CatalystDomain.create(1L, "title", "a".repeat(50), List.of(1L), "FOO"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(GlobalErrorCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("status 대소문자가 다르면 INVALID_INPUT 예외가 발생한다")
        void create_wrongCaseStatus_throwsInvalidInput() {
            assertThatThrownBy(() -> CatalystDomain.create(1L, "title", "a".repeat(50), List.of(1L), "active"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(GlobalErrorCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("status가 null이면 INVALID_INPUT 예외가 발생한다")
        void create_nullStatus_throwsInvalidInput() {
            assertThatThrownBy(() -> CatalystDomain.create(1L, "title", "a".repeat(50), List.of(1L), null))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(GlobalErrorCode.INVALID_INPUT);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @ParameterizedTest
        @EnumSource(value = CatalystStatus.class, names = {"INACTIVE", "ACTIVE", "PAUSED"})
        @DisplayName("삭제 시 이전 status와 무관하게 ENDED로 전환되고 deletedAt이 기록된다")
        void delete_anyPriorStatus_transitionsToEndedAndStampsDeletedAt(CatalystStatus priorStatus) {
            CatalystDomain domain = CatalystDomain.reconstruct(
                    1L, 1L, "title", "content", priorStatus, List.of(1L), List.of(),
                    LocalDateTime.now(), LocalDateTime.now(), null);

            domain.delete();

            assertThat(domain.getStatus()).isEqualTo(CatalystStatus.ENDED);
            assertThat(domain.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("isActive / isDeleted")
    class StatusChecks {

        @Test
        @DisplayName("status가 ACTIVE이면 isActive()는 true를 반환한다")
        void isActive_activeStatus_returnsTrue() {
            CatalystDomain domain = CatalystDomain.create(1L, "title", "a".repeat(50), List.of(1L), "ACTIVE");

            assertThat(domain.isActive()).isTrue();
        }

        @Test
        @DisplayName("status가 INACTIVE이면 isActive()는 false를 반환한다")
        void isActive_inactiveStatus_returnsFalse() {
            CatalystDomain domain = CatalystDomain.create(1L, "title", "a".repeat(50), List.of(1L), "INACTIVE");

            assertThat(domain.isActive()).isFalse();
        }

        @Test
        @DisplayName("deletedAt이 없으면 isDeleted()는 false를 반환한다")
        void isDeleted_notDeleted_returnsFalse() {
            CatalystDomain domain = CatalystDomain.create(1L, "title", "a".repeat(50), List.of(1L), "ACTIVE");

            assertThat(domain.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("delete() 호출 후에는 isDeleted()가 true를 반환한다")
        void isDeleted_afterDelete_returnsTrue() {
            CatalystDomain domain = CatalystDomain.create(1L, "title", "a".repeat(50), List.of(1L), "ACTIVE");

            domain.delete();

            assertThat(domain.isDeleted()).isTrue();
        }
    }
}
