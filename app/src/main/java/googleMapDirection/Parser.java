package googleMapDirection;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//Parse the given response of Google Map Direction API
public class Parser {

    public ArrayList<LatLng> parse (JSONObject resObj) {

        ArrayList<LatLng> LatLongList = new ArrayList<>();

        try {
            //First level the routes, we always choose the first one
            JSONArray routes = resObj.getJSONArray("routes");
            if (routes.length() > 0) {
                JSONArray legs = ((JSONObject) routes.get(0)).getJSONArray("legs");
                //Only one leg as we have no way point
                JSONObject leg = (JSONObject) legs.get(0);

                //Get all Steps
                JSONArray steps = leg.getJSONArray("steps");
                for (int i = 0; i< steps.length();i++) {
                    String encodePolyline = (String) ((JSONObject) ((JSONObject) steps.get(i)).get("polyline")).get("points");
                    List<LatLng> temp = decodePoly(encodePolyline);
                    for (int j = 0;j < temp.size();j++) {
                        LatLongList.add(temp.get(j));
                    }
                }

            }
        } catch (Exception e) {
            Log.e("TAG", "Error in parsing Google Map Direction Response");
            e.printStackTrace();
        }

        return LatLongList;

    }

    /**
     * Method to decode polyline points
     * Reference : https://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    public String getStartLoc(JSONObject resObj) {
        String startLoc = "";
        try {
            //First level the routes, we always choose the first one
            JSONArray routes = resObj.getJSONArray("routes");
            if (routes.length() > 0) {
                JSONArray legs = ((JSONObject) routes.get(0)).getJSONArray("legs");
                JSONObject leg = (JSONObject) legs.get(0);
                startLoc = (String) leg.get("start_address");
                return startLoc;
            }
        } catch (Exception e) {
            Log.e("TAG", "Error in getting Google Map Direction Response - start_address");
            e.printStackTrace();
        }
        return startLoc;
    }

    public String getEndLoc(JSONObject resObj) {
        String endLoc = "";
        try {
            //First level the routes, we always choose the first one
            JSONArray routes = resObj.getJSONArray("routes");
            if (routes.length() > 0) {
                JSONArray legs = ((JSONObject) routes.get(0)).getJSONArray("legs");
                JSONObject leg = (JSONObject) legs.get(0);
                endLoc = (String) leg.get("end_address");
                return endLoc;
            }
        } catch (Exception e) {
            Log.e("TAG", "Error in getting Google Map Direction Response - end_address");
            e.printStackTrace();
        }
        return endLoc;
    }
}
