import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import static java.time.LocalTime.ofSecondOfDay;

public class Report {
    private final FileReader fileReader = new FileReader();
    private final Gson gson = new Gson();
    private final HashMap<String, Integer> carriers = new HashMap<>();
    private final List<Double> prices = new ArrayList<>();

    public void readReport(String path) {
        JsonElement jsonElement = JsonParser.parseString(fileReader.readContent(path));
        Map map = gson.fromJson(jsonElement, Map.class);
        String json = new Gson().toJson(map.get("tickets"));
        Type listTicketType = new TypeToken<ArrayList<Ticket>>() {}.getType();
        ArrayList<Ticket> ticketArrayList = new Gson().fromJson(json, listTicketType);
        calculateFlightTimeAndPrice(ticketArrayList);
    }

    public String getMinFlightTime() {
        return carriers.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .map(item -> " \n" + item.getKey() + ": " + ofSecondOfDay(item.getValue()))
                .collect(Collectors.joining());
    }

    private Double getAveragePrice() {
        return prices.stream().mapToDouble(Double::doubleValue).sum() / prices.size();
    }

    public String getDiffAvgMedian() {
        return String.valueOf(getAveragePrice() - getMedian());
    }

    private Double getMedian() {
        double pp = (double) ((prices.size() - 1) * 50) / 100;
        if (pp % 1 == 0) {
            return prices.get((prices.size() - 1) * 50 / 100);
        } else {
            double k = pp - Math.floor(pp);
            return prices.get((int) Math.floor(pp)) + k *
                    (prices.get((int) Math.ceil(pp)) - prices.get((int) Math.floor(pp)));
        }
    }

    private void calculateFlightTimeAndPrice(List<Ticket> ticketList) {
        ticketList.stream()
                .filter(ticket -> ticket.getDestination_name().equals("Тель-Авив"))
                .filter(ticket -> ticket.getOrigin_name().equals("Владивосток"))
                .forEach(ticket -> {
            String[] values = ticket.getDeparture_time().split(":");
            LocalTime flightTime = LocalTime.parse(ticket.getArrival_time()).minusHours(Long.parseLong(values[0].trim()))
                    .minusMinutes(Long.parseLong(values[1].trim())).plusHours(8);
            Integer convertedFlightTime = flightTime.toSecondOfDay();
            prices.add(ticket.getPrice());
            if (!carriers.containsKey(ticket.getCarrier())) {
                carriers.put(ticket.getCarrier(), convertedFlightTime);
            } else if (carriers.get(ticket.getCarrier()) > convertedFlightTime) {
                carriers.put(ticket.getCarrier(), convertedFlightTime);
            }
        });
        Collections.sort(prices);
    }
}
