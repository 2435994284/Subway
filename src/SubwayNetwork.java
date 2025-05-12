import java.io.*;
import java.util.*;
public class SubwayNetwork {
    private Map<String,Station> stations;
    private Map<String,Line> lines;
    private Map<String,Map<String,Double>> graph;
    
    public SubwayNetwork() {
        stations = new HashMap<>();
        lines = new HashMap<>();
        graph = new HashMap<>();
    }

    public void loadData(String filePath) throws Exception{
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        String currentLineName =null;
        while ((line= br.readLine()) !=null) {
            line =line.trim();
            if (line.isEmpty()) {
                continue;
            }
            if(line.matches(".*号线.*")){
                currentLineName = line.split("站点间距")[0].trim();
                lines.putIfAbsent(currentLineName, new Line(currentLineName));
            }
            else if(line.contains("---")||line.contains("-")){
                line = line.replace("-","---");
                String[] parts = line.split("---");
                if(parts.length !=2){
                    continue;
                }
                String stationA = parts[0].trim();
                String [] stationBAndDistance =parts[1].trim().split("\\s+");
                String stationB = stationBAndDistance[0].trim();
                double distance = Double.parseDouble(stationBAndDistance[1].trim());

                stations.putIfAbsent(stationA, new Station(stationA));
                stations.putIfAbsent(stationB, new Station(stationB));

                 if (currentLineName != null) {
                    lines.get(currentLineName).addStation(stations.get(stationA));
                    lines.get(currentLineName).addStation(stations.get(stationB));
                }

                stations.get(stationA).addLine(currentLineName);
                stations.get(stationB).addLine(currentLineName);

                graph.putIfAbsent(stationA, new HashMap<>());
                graph.putIfAbsent(stationB, new HashMap<>());
                graph.get(stationA).put(stationB, distance);
                graph.get(stationB).put(stationA, distance);

            }
            
        }
        br.close();
    }

    public Map<String, Station> getStations() {
        return stations;
    }
    public Map<String, Line> getLines() {
        return lines;
    }
    public Map<String, Map<String, Double>> getGraph() {
        return graph;
    }
}
