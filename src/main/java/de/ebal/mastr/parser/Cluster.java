package de.ebal.mastr.parser;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;

import java.util.List;

@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public class Cluster {

    private final List<SolarUnit> units;

    @XmlElement
    private final Double avgPower;

    @XmlElement
    private final Integer size;

    @XmlElement
    private final Double power;

    public Cluster() {
        this.units = List.of();
        this.avgPower = 0d;
        this.size = 0;
        this.power = 0d;
    }

    public Cluster(List<SolarUnit> units) {
        this.units = units;
        this.power = units.stream().map(SolarUnit::getNettonennleistung).mapToDouble(Float::doubleValue).sum();
        this.size = units.size();
        this.avgPower = this.power/this.size;
    }

}
