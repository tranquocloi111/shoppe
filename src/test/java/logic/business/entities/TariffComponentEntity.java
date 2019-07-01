package logic.business.entities;

import logic.utils.Parser;
import logic.utils.TimeStamp;

import java.sql.Date;
import java.util.HashMap;

public class TariffComponentEntity {

    public static HashMap<String, String> dataTMOBUSETariffComponentForFC(Date newStartDate) {
        HashMap<String, String> tariffComponent = new HashMap<String, String>();
        tariffComponent.put("Component Code", "TMOBUSE");
        tariffComponent.put("Description", "TMOBUSE - Usage");
        tariffComponent.put("Start Date", Parser.parseDateFormate(newStartDate, TimeStamp.DATE_FORMAT));
        tariffComponent.put("End Date", "");
        tariffComponent.put("Charge", "FC-SIM-0750-150");

        return tariffComponent;
    }

    public static HashMap<String, String> dataTMOBUSETariffComponentForNC(Date newStartDate) {
        HashMap<String, String> tariffComponent = new HashMap<String, String>();
        tariffComponent.put("Component Code", "TMOBUSE");
        tariffComponent.put("Description", "TMOBUSE - Usage");
        tariffComponent.put("Start Date", Parser.parseDateFormate(newStartDate, TimeStamp.DATE_FORMAT));
        tariffComponent.put("End Date", "");
        tariffComponent.put("Charge", "NC-SIM-1000-250");

        return tariffComponent;
    }

}
