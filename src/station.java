import java.util.*;
public class Station {
    private String name;
    private Set<String> lines;
    public Station (String name){
        this.name = name;
        this.lines = new HashSet<>();
    }
    public String getName() {
        return name;
    }
    public Set<String> getLines() {
        return lines;
    }
    public void addLine(String line) {
        lines.add(line);
    }
    public String toString() {
        return "Station{" +
                "name='" + name + '\'' +
                ", lines=" + lines +
                '}';
    }
    
}
