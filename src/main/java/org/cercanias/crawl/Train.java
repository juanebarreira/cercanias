package org.cercanias.crawl;

public class Train {
    private String line;
    private String departureTime;
    private String arrivalTime;
    private String travelTime;
    private String trainCode;
    private String origin;
    private String destination;

    public Train(String line, String origin, String destination, String departureTime, String arrivalTime, String travelTime, String trainCode) {
        this.line = line;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.travelTime = travelTime;
        this.trainCode = trainCode;
        this.origin = origin;
        this.destination = destination;
    }

    public boolean departsInRange(String startTime, String endTime) {
        int departureMinutes = convertToMinutes(departureTime);
        int startMinutes = convertToMinutes(startTime);
        int endMinutes = convertToMinutes(endTime);

        return departureMinutes >= startMinutes && departureMinutes <= endMinutes;
    }

    private int convertToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    public boolean takesLessThan(String minutes) {
        if(minutes == null){
            return true;
        }
        // Extraer solo los números del tiempo de viaje (eliminar "min." del final)
        String travelMinutes = travelTime.replaceAll("[^0-9]", "");

        // Convertir ambos valores a enteros para comparar
        int actualTravelTime = Integer.parseInt(travelMinutes);
        int maxTravelTime = Integer.parseInt(minutes);

        return actualTravelTime < maxTravelTime;
    }

    public String telegramFormat() {
        return String.format("🕐 %s → %s\n   %s - %s\n\n",
                origin,
                destination,
                departureTime,
                arrivalTime
        );
    }

    @Override
    public String toString() {
        return "Train{" +
                "line='" + line + '\'' +
                ", departureTime='" + departureTime + '\'' +
                ", arrivalTime='" + arrivalTime + '\'' +
                ", travelTime='" + travelTime + '\'' +
                ", trainCode='" + trainCode + '\'' +
                '}';
    }
}