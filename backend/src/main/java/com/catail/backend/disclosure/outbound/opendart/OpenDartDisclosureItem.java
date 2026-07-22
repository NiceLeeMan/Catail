package com.catail.backend.disclosure.outbound.opendart;

import com.fasterxml.jackson.annotation.JsonProperty;

record OpenDartDisclosureItem(
        @JsonProperty("rcept_no") String rceptNo,
        @JsonProperty("rcept_dt") String rceptDt,
        @JsonProperty("report_nm") String reportNm,
        @JsonProperty("flr_nm") String flrNm,
        @JsonProperty("rm") String rm
) {
}
