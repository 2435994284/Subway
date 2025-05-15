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
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
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
            else if(line.contains("---")||line.contains("—")){
                line = line.replace("—","---");
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
    public Set<Station>getTransferStations()//识别所有地铁中转站
    {
        Set<Station> transferStations = new HashSet<>();
        for (Station station : stations.values()){
            if (station.getLines().size() > 1) {
                transferStations.add(station);
            }
        }
        return transferStations;
    }
    public List<String> findShortestPath(String start,String end)//Dijkstra算法求出两站最短路径
    {
        if (!stations.containsKey(start) || !stations.containsKey(end)) {
            return Collections.emptyList();
        }
        if (start.equals(end)) {
            return Collections.singletonList(start);
        }
    {
        Map<String ,Double> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingDouble(distances::get));

        for (String station : graph.keySet())
        {
            distances.put(station, Double.MAX_VALUE);
        }
        distances.put(start, 0.0);
        pq.add(start);
        while (!pq.isEmpty()) 
    {
        String current = pq.poll();
        if (current.equals(end)) break;
        for (Map.Entry<String, Double> neighbor : graph.get(current).entrySet())
         {
            String nextStation = neighbor.getKey();
            double newDist = distances.get(current) + neighbor.getValue();
            if (newDist < distances.get(nextStation)) {
                distances.put(nextStation, newDist);
                previous.put(nextStation, current);
                pq.add(nextStation);
            }
        }
    }
    List<String> path = new ArrayList<>();
    for (String at = end; at != null; at = previous.get(at)) {
        path.add(at);
    }
    Collections.reverse(path);
    return path;

    }
}

    // 定义一个静态内部类来表示查询结果
    public static class NearbyStationInfo {
        private String stationName;
        private String lineName;
        private double distance;

        public NearbyStationInfo(String stationName, String lineName, double distance) {
            this.stationName = stationName;
            this.lineName = lineName;
            this.distance = distance;
        }

        public String getStationName() {
            return stationName;
        }

        public String getLineName() {
            return lineName;
        }

        public double getDistance() {
            return distance;
        }

        public String toString() {
            return "<" + stationName + "站, " + lineName + ", " + String.format("%.1f", distance) + ">";
        }
    }

    public List<NearbyStationInfo> findNearbyStations(String stationName, double maxDistance) {
        // 检查站点是否存在
        if (!stations.containsKey(stationName)) {
            throw new IllegalArgumentException("站点 " + stationName + " 不存在");
        }

        // 检查距离是否为正数
        if (maxDistance <= 0) {
            throw new IllegalArgumentException("距离必须为正数");
        }

        List<NearbyStationInfo> result = new ArrayList<>();
        Map<String, Double> distances = new HashMap<>();
        PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparingDouble(distances::get));

        // 初始化距离
        for (String station : graph.keySet()) {
            distances.put(station, Double.MAX_VALUE);
        }
        distances.put(stationName, 0.0);
        queue.add(stationName);

        // 广度优先搜索
        while (!queue.isEmpty()) {
            String current = queue.poll();
            double currentDistance = distances.get(current);
            
            // 如果当前站点距离已经超过最大距离，则终止
            if (currentDistance > maxDistance) {
                continue;
            }

            // 如果不是起始站点，则添加到结果中
            if (!current.equals(stationName)) {
                // 获取当前站点所在的所有线路
                Station currentStation = stations.get(current);
                for (String line : currentStation.getLines()) {
                    result.add(new NearbyStationInfo(current, line, currentDistance));
                }
            }

            // 遍历相邻站点
            for (Map.Entry<String, Double> neighbor : graph.get(current).entrySet()) {
                String nextStation = neighbor.getKey();
                double newDistance = currentDistance + neighbor.getValue();
                
                // 如果新的距离更短且在范围内
                if (newDistance <= maxDistance && newDistance < distances.get(nextStation)) {
                    distances.put(nextStation, newDistance);
                    queue.add(nextStation);
                }
            }
        }

        return result;
    }

    public List<List<String>> findAllPaths(String start, String end) {
        // 检查起点站和终点站是否存在
        if (!stations.containsKey(start)) {
            throw new IllegalArgumentException("起点站 " + start + " 不存在");
        }
        if (!stations.containsKey(end)) {
            throw new IllegalArgumentException("终点站 " + end + " 不存在");
        }

        List<List<String>> allPaths = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        List<String> currentPath = new ArrayList<>();

        // 从起点站开始深度优先搜索
        dfs(start, end, visited, currentPath, allPaths);

        return allPaths;
    }

    //深度优先搜索算法找出所有从当前站点到终点站的无环路径
    private void dfs(String current, String end, Set<String> visited, 
                    List<String> currentPath, List<List<String>> allPaths) {
        // 将当前站点加入已访问集合和当前路径
        visited.add(current);
        currentPath.add(current);

        // 如果当前站点是终点站，则找到了一条路径
        if (current.equals(end)) {
            allPaths.add(new ArrayList<>(currentPath));
        } else {
            // 遍历当前站点的所有相邻站点
            if (graph.containsKey(current)) {
                for (String neighbor : graph.get(current).keySet()) {
                    // 只访问未访问过的站点，避免环路
                    if (!visited.contains(neighbor)) {
                        dfs(neighbor, end, visited, currentPath, allPaths);
                    }
                }
            }
        }

        // 回溯：从已访问集合和当前路径中移除当前站点
        visited.remove(current);
        currentPath.remove(currentPath.size() - 1);
    }
}
