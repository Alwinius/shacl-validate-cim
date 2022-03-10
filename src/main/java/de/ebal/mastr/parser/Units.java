package de.ebal.mastr.parser;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;

import java.util.List;

@XmlRootElement(name = "EinheitenSolar")
@XmlAccessorType(XmlAccessType.FIELD)
public class Units {

    @XmlElement(name = "EinheitSolar")
    @Getter
    private List<Unit> units;
}
