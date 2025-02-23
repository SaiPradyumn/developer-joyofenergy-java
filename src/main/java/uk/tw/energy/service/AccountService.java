package uk.tw.energy.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AccountService {

    private final Map<String, String> smartMeterToPricePlanAccounts;

    public AccountService(Map<String, String> smartMeterToPricePlanAccounts) {
        this.smartMeterToPricePlanAccounts = smartMeterToPricePlanAccounts;
    }

    public String getPricePlanIdForSmartMeterId(String smartMeterId) {
        return smartMeterToPricePlanAccounts.get(smartMeterId);
    }
    
    public int updatePricePlanForSmartMeterId(String smartMeterId, String pricePlan) {
    	if(!smartMeterToPricePlanAccounts.containsKey(smartMeterId) || !smartMeterToPricePlanAccounts.containsValue(pricePlan)) {
    		return 0;
    	}
    	smartMeterToPricePlanAccounts.put(smartMeterId,pricePlan);
    	return 1;
    }
}
