package com.rupeetrack.budget;


import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/budget")
public class BudgetController {

    private final BudgetService service;

    public BudgetController(BudgetService service){
        this.service = service;
    }


    @GetMapping
    public Budget getBudget(){
        return service.getBudget();
    }

    @PutMapping
    public Budget updateBudget(@RequestBody Map<String, BigDecimal> request) {
        return service.updateBudget(request.get("monthlyAmount"));
    }
}


