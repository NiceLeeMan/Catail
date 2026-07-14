package com.catail.backend.catalyst.inbound.read;

import com.catail.backend.catalyst.application.CatalystBasicInfo;
import com.catail.backend.catalyst.application.CatalystMonitoringOperation;

public record CatalystInfoResponse(
        CatalystBasicInfo basicInfo,
        CatalystMonitoringOperation monitoringOperation
) {
}
