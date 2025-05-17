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

    public void printJourneyGuide(List<String> path) {
        if (path == null || path.size() <= 1) {
            System.out.println("路径不完整，无法提供乘车指南。");
            return;
        }

        System.out.println("=== 乘车路线指南 ===");
        String currentLine = null;
        String startStation = path.get(0);
        String currentStation = startStation;

        for (int i = 1; i < path.size(); i++) {
            String nextStation = path.get(i);
            
            // 确定当前站点和下一站点之间的线路
            String connectionLine = findConnectionLine(currentStation, nextStation);
            
            // 如果线路改变，说明需要换乘
            if (currentLine != null && !connectionLine.equals(currentLine)) {
                System.out.printf("乘坐%s，从%s站到%s站\n", currentLine, startStation, currentStation);
                startStation = currentStation; // 换乘点成为新的起点
            }
            
            // 更新当前线路
            currentLine = connectionLine;
            
            // 如果是路径的最后一站，需要打印最后一段乘车路线
            if (i == path.size() - 1) {
                System.out.printf("乘坐%s，从%s站到%s站\n", currentLine, startStation, nextStation);
            }
            
            currentStation = nextStation;
        }
        System.out.println("=== 全程共经过" + (path.size()) + "站 ===");
    }


    private String findConnectionLine(String stationA, String stationB) {
        // 检查两站是否相邻
        if (!graph.containsKey(stationA) || !graph.get(stationA).containsKey(stationB)) {
            throw new IllegalArgumentException("站点" + stationA + "和" + stationB + "不相邻");
        }

        // 获取两个站点所在的所有线路
        Set<String> linesA = stations.get(stationA).getLines();
        Set<String> linesB = stations.get(stationB).getLines();
        
        // 查找两站点的共同线路
        Set<String> commonLines = new HashSet<>(linesA);
        commonLines.retainAll(linesB);
        
        // 如果有共同线路，返回第一条
        if (!commonLines.isEmpty()) {
            return commonLines.iterator().next();
        }
        
        // 如果没有共同线路（理论上不应该发生，因为相邻站点必须在同一条线路上）
        throw new IllegalStateException("无法找到连接" + stationA + "和" + stationB + "的线路");
    }


    public int calculateFare(List<String> path) {
        if (path == null || path.size() <= 1) {
            return 0;
        }

        // 计算总距离
        double totalDistance = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            String current = path.get(i);
            String next = path.get(i + 1);
            if (graph.containsKey(current) && graph.get(current).containsKey(next)) {
                totalDistance += graph.get(current).get(next);
            } else {
                throw new IllegalArgumentException("路径中存在不相邻的站点: " + current + " 和 " + next);
            }
        }

        // 根据计价标准计算票价
        int fare = 0;
        if (totalDistance <= 4) {
            fare = 2;
        } else if (totalDistance <= 12) {
            fare = 2 + (int) Math.ceil((totalDistance - 4) / 4);
        } else if (totalDistance <= 24) {
            fare = 2 + 2 + (int) Math.ceil((totalDistance - 12) / 6);
        } else if (totalDistance <= 40) {
            fare = 2 + 2 + 2 + (int) Math.ceil((totalDistance - 24) / 8);
        } else if (totalDistance <= 50) {
            fare = 2 + 2 + 2 + 2 + (int) Math.ceil((totalDistance - 40) / 10);
        } else {
            fare = 2 + 2 + 2 + 2 + 1 + (int) Math.ceil((totalDistance - 50) / 20);
        }

        return fare;
    }

    public Map.Entry<List<String>, Integer> selectPathAndCalculateFare(String start, String end) {
        List<List<String>> allPaths = findAllPaths(start, end);
        
        if (allPaths.isEmpty()) {
            System.out.println("没有找到从 " + start + " 到 " + end + " 的路径。");
            return null;
        }

        Scanner scanner = new Scanner(System.in);
        
        System.out.println("以下是从 " + start + " 到 " + end + " 的所有路径：");
        for (int i = 0; i < allPaths.size(); i++) {
            List<String> path = allPaths.get(i);
            System.out.println((i + 1) + ". " + String.join(" -> ", path) + 
                              " (共" + (path.size() - 1) + "站)");
        }
        
        System.out.print("请选择一条路径（输入序号）: ");
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine());
            if (choice < 1 || choice > allPaths.size()) {
                System.out.println("无效的选择。");
                scanner.close();
                return null;
            }
        } catch (NumberFormatException e) {
            System.out.println("请输入有效的数字。");
            scanner.close();
            return null;
        }
        
        List<String> selectedPath = allPaths.get(choice - 1);
        int fare = calculateFare(selectedPath);
        
        System.out.println("\n您选择的路径：" + String.join(" -> ", selectedPath));
        System.out.println("总距离: " + String.format("%.2f", calculatePathDistance(selectedPath)) + " 公里");
        System.out.println("票价: " + fare + " 元");
        
        scanner.close();
        return new AbstractMap.SimpleEntry<>(selectedPath, fare);
    }


    private double calculatePathDistance(List<String> path) {
        double totalDistance = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            String current = path.get(i);
            String next = path.get(i + 1);
            totalDistance += graph.get(current).get(next);
        }
        return totalDistance;
    }
}
