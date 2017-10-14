/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Joseph
 */
 public class Coords
 {

        public double latitude = 0;
        public double latitudeMinutes = 0;
        public double latitudeSeconds = 0;
        public String latDirection = "";

        public double longitude = 0;
        public double longitudeMinutes = 0;
        public double longitudeSeconds = 0;
        public String longDirection = "";

        public String city = "";


        public Coords(double lat, double latMin, double latSec, String directionLat, double longitude, double longitudeMin, double longitudeSec, String directionLong, String destCity)
        {
            this.latitude = lat;
            this.latitudeMinutes = latMin;
            this.latitudeSeconds = latSec;
            this.latDirection = directionLat;
            this.longitude = longitude;
            this.longitudeMinutes = longitudeMin;
            this.longitudeSeconds = longitudeSec;
            this.longDirection = directionLong;
            this.city = destCity;
        }
    }
