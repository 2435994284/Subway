import java.io.*;
import java.util.*;
public class SubwayNetwork {
    private Map<String,station> stations;
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
        while ((line= =br.readLine()) !=null) {
            String[] parts= line.split(",");
            
        }
    }

}
