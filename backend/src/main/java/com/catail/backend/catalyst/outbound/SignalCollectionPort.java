package com.catail.backend.catalyst.outbound;

public interface SignalCollectionPort {

    void triggerCollection(Long catalystId);
}
