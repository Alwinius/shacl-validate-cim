package de.ebal.mastr.parser;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;

// order of the fields in XML
// @XmlType(propOrder = {"price", "name"})
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
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
}
