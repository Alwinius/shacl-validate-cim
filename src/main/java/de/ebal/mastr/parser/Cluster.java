package de.ebal.mastr.parser;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;

import java.util.List;

@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public class Cluster {

    private final List<Unit> units;

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

    public Cluster(List<Unit> units) {
        this.units = units;
        this.power = units.stream().map(Unit::getNettonennleistung).mapToDouble(Float::doubleValue).sum();
        this.size = units.size();
        this.avgPower = this.power / this.size;
    }
}
