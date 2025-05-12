import java.util.*;
public class Line {
    private String name;
    private List<Station> stations;
    public Line(String name) {
        this.name = name;
        this.stations = new ArrayList<>();
    }
    public void addStation(Station station) {
        stations.add(station);
    }
    public List<Station>getStations(){
        return stations;
    }
    
}
