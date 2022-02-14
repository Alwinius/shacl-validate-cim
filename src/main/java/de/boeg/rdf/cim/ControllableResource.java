package de.boeg.rdf.cim;

import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.Map;

@ToString
@Getter
public class ControllableResource {
    private final String id;
    private final String name;
    private final Map<Instant, Double> prodTimeSeries;
    private final Double maxGradientPlus;
    private final Double maxGradientMinus;

    public ControllableResource(String id, String name, Map<Instant, Double> prodTimeSeries, Double maxGradientPlus, Double maxGradientMinus) {
        this.id = id;
        this.name = name;
        this.prodTimeSeries = prodTimeSeries;
        this.maxGradientPlus = maxGradientPlus;
        this.maxGradientMinus = maxGradientMinus;
    }
}
