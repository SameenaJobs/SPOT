package com.etatransit.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by innovator on 8/30/2016.
 */
public class RiderAlert {

    private String alertID;
    private String alertName;
    private JSONObject routes;
    private JSONObject allroutes;
    private String alertStartDate;
    private String alertStartTime;
    private String minutes;
    private String is_stopName;
    private String is_repeat;
    private String repeat_type;
    private String selected_days;
    private String repeatStartDate;
    private String repeatEndDate;
    private String stopName;
    private String stopId;
    private String selectedAgency;
    private String selectedAgencyUrl;
    private String is_direction;
    private String direction;



    public RiderAlert(int alertID, String alertName, String selectedAgency, String selectedAgencyUrl,
                      String alertStartDate, String is_stopName, String stopName, String stopId, String alertStartTime,
                      String selected_days, String repeat_type, String repeatEndDate, String repeatStartDate, JSONObject routes,
                      JSONObject allroutes, String is_repeat, String minutes) throws JSONException {
        this.alertID = String.valueOf(alertID);
        this.alertName = alertName;
            this.routes = routes;

        this.allroutes = allroutes;
        this.alertStartDate = alertStartDate;
        this.alertStartTime = alertStartTime;
        this.minutes = minutes;
        this.is_stopName = is_stopName;
        this.is_repeat = is_repeat;
        this.repeat_type = repeat_type;
        this.selected_days = selected_days;
        this.repeatStartDate = repeatStartDate;
        this.repeatEndDate = repeatEndDate;
        this.stopName = stopName;
        this.stopId = stopId;
        this.selectedAgency = selectedAgency;
        this.selectedAgencyUrl = selectedAgencyUrl;
    }

    public RiderAlert() {

    }

    public String getAlertID() {
        return alertID;
    }

    public void setAlertID(String alertID) {
        this.alertID = alertID;
    }

    public String getAlertName() {
        return alertName;
    }

    public void setAlertName(String alertName) {
        this.alertName = alertName;
    }

    public JSONObject getRoutes() {
        return routes;
    }

    public void setRoutes(JSONObject routes) {
        this.routes = routes;
    }

    public String getAlertStartDate() {
        return alertStartDate;
    }

    public void setAlertStartDate(String alertStartDate) {
        this.alertStartDate = alertStartDate;
    }

    public String getAlertStartTime() {
        return alertStartTime;
    }

    public void setAlertStartTime(String alertStartTime) {
        this.alertStartTime = alertStartTime;
    }

    public String getMinutes() {
        return minutes;
    }

    public void setMinutes(String minutes) {
        this.minutes = minutes;
    }

    public String getIs_stopName() {
        return is_stopName;
    }

    public void setIs_stopName(String is_stopName) {
        this.is_stopName = is_stopName;
    }

    public String getIs_repeat() {
        return is_repeat;
    }

    public void setIs_repeat(String is_repeat) {
        this.is_repeat = is_repeat;
    }

    public String getRepeat_type() {
        return repeat_type;
    }

    public void setRepeat_type(String repeat_type) {
        this.repeat_type = repeat_type;
    }

    public String getSelected_days() {
        return selected_days;
    }

    public void setSelected_days(String selected_days) {
        this.selected_days = selected_days;
    }

    public String getRepeatStartDate() {
        return repeatStartDate;
    }

    public void setRepeatStartDate(String repeatStartDate) {
        this.repeatStartDate = repeatStartDate;
    }

    public String getRepeatEndDate() {
        return repeatEndDate;
    }

    public void setRepeatEndDate(String repeatEndDate) {
        this.repeatEndDate = repeatEndDate;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public JSONObject getAllroutes() {
        return allroutes;
    }

    public void setAllroutes(JSONObject allroutes) {
        this.allroutes = allroutes;
    }

    public String getSelectedAgency() {
        return selectedAgency;
    }

    public void setSelectedAgency(String selectedAgency) {
        this.selectedAgency = selectedAgency;
    }

    public String getSelectedAgencyUrl() {
        return selectedAgencyUrl;
    }

    public void setSelectedAgencyUrl(String selectedAgencyUrl) {
        this.selectedAgencyUrl = selectedAgencyUrl;
    }

    public String getIs_direction() {
        return is_direction;
    }

    public void setIs_direction(String is_direction) {
        this.is_direction = is_direction;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
