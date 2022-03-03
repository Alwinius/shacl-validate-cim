package de.ebal.mastr.parser;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.Set;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Clusters {

    private final Set<Cluster> clusters;

    public Clusters(Set<Cluster> clusters) {
        this.clusters = clusters;
    }

    public Clusters() {
        this.clusters = Set.of();
    }
}
