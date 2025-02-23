package uk.tw.energy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.tw.energy.service.AccountService;
import uk.tw.energy.service.PricePlanService;
import uk.tw.energy.domain.PricePlan;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;



import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/price-plans")
public class PricePlanComparatorController {

    public final static String PRICE_PLAN_ID_KEY = "pricePlanId";
    public final static String PRICE_PLAN_COMPARISONS_KEY = "pricePlanComparisons";
    private final PricePlanService pricePlanService;
    private final AccountService accountService;

    public PricePlanComparatorController(PricePlanService pricePlanService, AccountService accountService) {
        this.pricePlanService = pricePlanService;
        this.accountService = accountService;
    }

    @GetMapping("/compare-all/{smartMeterId}")
    public ResponseEntity<Map<String, Object>> calculatedCostForEachPricePlan(@PathVariable String smartMeterId) {
        String pricePlanId = accountService.getPricePlanIdForSmartMeterId(smartMeterId);
        Optional<Map<String, BigDecimal>> consumptionsForPricePlans =
                pricePlanService.getConsumptionCostOfElectricityReadingsForEachPricePlan(smartMeterId);

        if (!consumptionsForPricePlans.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> pricePlanComparisons = new HashMap<>();
        pricePlanComparisons.put(PRICE_PLAN_ID_KEY, pricePlanId);
        pricePlanComparisons.put(PRICE_PLAN_COMPARISONS_KEY, consumptionsForPricePlans.get());

        return consumptionsForPricePlans.isPresent()
                ? ResponseEntity.ok(pricePlanComparisons)
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/recommend/{smartMeterId}")
    public ResponseEntity<List<Map.Entry<String, BigDecimal>>> recommendCheapestPricePlans(@PathVariable String smartMeterId,
                                                                                           @RequestParam(value = "limit", required = false) Integer limit) {
        Optional<Map<String, BigDecimal>> consumptionsForPricePlans =
                pricePlanService.getConsumptionCostOfElectricityReadingsForEachPricePlan(smartMeterId);

        if (!consumptionsForPricePlans.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        List<Map.Entry<String, BigDecimal>> recommendations = new ArrayList<>(consumptionsForPricePlans.get().entrySet());
        recommendations.sort(Comparator.comparing(Map.Entry::getValue));

        if (limit != null && limit < recommendations.size()) {
            recommendations = recommendations.subList(0, limit);
        }

        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/viewPricing/{smartMeterId}")
    public ResponseEntity<Map<String,String>> viewAssociatedPricePlan(@PathVariable String smartMeterId){
    	
    	String pricePlanId = accountService.getPricePlanIdForSmartMeterId(smartMeterId);
    	PricePlan pricePlan = pricePlanService.fetchPricePlanDetailsThroughPricePlanId(pricePlanId);
    	if(pricePlan == null) {
    		Map<String, String> badResponse = new HashMap<>();
    		badResponse.put("Message","Invalid Smart Meter Id, Please check.");
    		return new ResponseEntity(badResponse,HttpStatus.BAD_REQUEST);
    	}
    	
    	Map<String, String> pricePlanDetails = new HashMap<>();
    	pricePlanDetails.put("Price Plan ID", pricePlan.getPlanName());
    	pricePlanDetails.put("Unit Rate", pricePlan.getUnitRate().toString());
    	pricePlanDetails.put("Energy Supplier", pricePlan.getEnergySupplier());
    	
    	return ResponseEntity.ok(pricePlanDetails);
    }
    
    @PostMapping("/select")
    	 public ResponseEntity selectNewPricePlan(@RequestBody Map<String,String> pricePlanMappedToMeterId) {
    		 int validOperation = accountService.updatePricePlanForSmartMeterId(pricePlanMappedToMeterId.get("SmartMeterId"),pricePlanMappedToMeterId.get("PricePlan"));
    		 if(validOperation == 0) {
    			 Map<String, String> badResponse = new HashMap<>();
    	    		badResponse.put("Message","Invalid Smart Meter Id or Price Plan. Please check!");
    	    		return new ResponseEntity(badResponse,HttpStatus.BAD_REQUEST);
    		 }
    		 return ResponseEntity.ok("Price Plan changed successfully!");
    	 }
    	 
    }