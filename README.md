# Validate CIM data with SHACL
This small repository contains a prototype which demonstrate the usage of SHACL to validate CGMES data.
The repository will be part of the [CGMES-Manager](https://github.com/MBoegers/Validate-CGMES) in an upcoming beta release.

## Usage
The usage is straight forward and is shown in [ShaclValidataionExample](src/main/java/de/boeg/rdf/cim/validate/ShaclValidataionExample.java)
1) Load RDFS files for the Profile under validation
2) Parse the Scheme to SHACL-Shapes
3) load the data file
4) Use Apache Jena's SHACL implementation to validate the Grid Content  
 
 ## References
 * [Common Grid Model Exchange Standard](https://www.entsoe.eu/digital/cim/cim-for-grid-models-exchange/) provides:
   * CGMES definition
   * RDFS files
   * OCL files
 * [Apache Jena Fuseki](https://jena.apache.org/documentation/fuseki2/) SPARQL Server used for testing
 * [Apache Jena](https://jena.apache.org/) Java Semantic Web Framework used
 * Authors [GitHub](https://github.com/MBoegers/jena-examples) Repository with useful Apache Jena snippets