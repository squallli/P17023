package tw.com.regalscan.evaground.Models;


public class FlightData {

    private String FlightDate;
    private String CartNo;
    private Flight[] Flights;


    public String getFlightDate() {
        return FlightDate;
    }

    public void setFlightDate(String flightDate) {
        FlightDate = flightDate;
    }

    public String getCartNo() {
        return CartNo;
    }

    public void setCartNo(String cartNo) {
        CartNo = cartNo;
    }

    public Flight[] getFlights() {
        return Flights;
    }

    public void setFlights(Flight[] flights) {
        Flights = flights;
    }


}
