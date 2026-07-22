package com.catail.backend.disclosure.domain;

public enum DisclosureProvider {

    OPEN_DART {
        @Override
        public String buildSourceUrl(String externalDisclosureId) {
            return "https://dart.fss.or.kr/dsaf001/main.do?rcpNo=" + externalDisclosureId;
        }
    },
    SEC_EDGAR {
        @Override
        public String buildSourceUrl(String externalDisclosureId) {
            throw new UnsupportedOperationException("SEC_EDGAR는 아직 지원하지 않습니다.");
        }
    };

    public abstract String buildSourceUrl(String externalDisclosureId);
}
