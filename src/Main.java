
public class Main {

    public static void main(String[] args) {

        Report report = new Report();

        report.readReport("tickets.json");

        System.out.println("Min flight time for every carrier: " + report.getMinFlightTime());

        System.out.println("Difference between average and median price: " + report.getDiffAvgMedian());
    }
}
