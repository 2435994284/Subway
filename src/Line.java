import java.util.*;
public class Line {
    private String name;
    private List<station> stations;
    public Line(String name) {
        this.name = name;
        this.stations = new ArrayList<>();
    }
    public void addStation(station station) {
        stations.add(station);
    }
    public List<station>getStations(){
        return stations;
    }
    
}
