package com.catail.backend.signal.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "signal_source_query")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignalSourceQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "signal_id", nullable = false)
    private Long signalId;

    @Column(name = "query", nullable = false, length = 200)
    private String query;

    public static SignalSourceQuery create(Long signalId, String query) {
        SignalSourceQuery sourceQuery = new SignalSourceQuery();
        sourceQuery.signalId = signalId;
        sourceQuery.query = query;
        return sourceQuery;
    }
}
