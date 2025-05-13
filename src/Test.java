public class Test {
    public static void main(String[]args)throws Exception{
        SubwayNetwork subway =new SubwayNetwork();
        subway.loadData("src/subway.txt");
        //System.out.println(subway.getStations());
        //System.out.println(subway.getLines());
        //System.out.println(subway.getGraph());
        //System.out.println(subway.getTransferStations());
        System.out.println(subway.findShortestPath("华中科技大学", "江汉路"));
    }

}
