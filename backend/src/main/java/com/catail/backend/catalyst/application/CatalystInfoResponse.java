package com.catail.backend.catalyst.application;

public record CatalystInfoResponse(
        CatalystBasicInfo basicInfo,
        CatalystMonitoringOperation monitoringOperation
) {
}
