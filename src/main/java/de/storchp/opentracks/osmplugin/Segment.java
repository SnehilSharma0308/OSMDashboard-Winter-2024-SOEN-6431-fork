package de.storchp.opentracks.osmplugin;
import org.oscim.core.GeoPoint;
public class Segment {
    public GeoPoint start;
    public GeoPoint end;
    int color;

    public Segment(GeoPoint start, GeoPoint end, int color) {
        this.start = start;
        this.end = end;
        this.color = color;
    }
}