public class Test {
    public static void main(String[]args)throws Exception{
        SubwayNetwork subway =new SubwayNetwork();
        subway.loadData("src/subway.txt");
        System.out.println(subway.getStations());

    }

}
