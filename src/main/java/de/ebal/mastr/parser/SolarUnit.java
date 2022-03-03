package de.ebal.mastr.parser;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;

@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@ToString
public class SolarUnit {

    @XmlElement(name = "MaStRNummer")
    String maStRNummer;

    @XmlElement(name="Bundesland")
    String bundesland;

    @XmlElement(name="Ort")
    String ort;

    @XmlElement(name="Postleitzahl")
    String postleitzahl;

    @XmlElement(name="Nettonennleistung")
    Float nettonennleistung;

    @XmlElement(name="Hauptausrichtung")
    String hauptausrichtung;

    @XmlElement(name="FernsteuerbarkeitNb")
    Integer fernsteuerbarkeit;

    @XmlElement(name="EinheitBetriebsstatus")
    Integer status;

    @XmlElement(name = "Inbetriebnahmedatum")
    Date inbetriebnahmedatum;
}
