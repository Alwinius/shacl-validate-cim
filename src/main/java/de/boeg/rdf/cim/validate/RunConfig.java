package de.boeg.rdf.cim.validate;

public class RunConfig {

    final boolean SHOW_COUNTING;
    final boolean SHOW_SUMMARY;
    final boolean isMeasurement;

    private RunConfig(boolean show_counting, boolean show_summary, boolean isMeasurement) {
        SHOW_COUNTING = show_counting;
        SHOW_SUMMARY = show_summary;
        this.isMeasurement = isMeasurement;
    }


    public static RunConfig MEASURMENT = new RunConfig(false, false, true);
    public static RunConfig SUMMARY = new RunConfig(false, true, false);
    public static RunConfig TRACE = new RunConfig(true, false, false);

}
