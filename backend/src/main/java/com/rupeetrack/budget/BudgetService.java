package com.rupeetrack.budget;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;


    public BudgetService(BudgetRepository budgetRepository){
        this.budgetRepository = budgetRepository;
    }


    public Budget getBudget(){
        return budgetRepository.findById(1L)
                .orElseGet(()->{
                    Budget budget = new Budget();
                    budget.setMonthlyAmount(new BigDecimal("18000"));
                    return budgetRepository.save(budget);
                });
    }

    @Transactional
    public Budget updateBudget(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Budget must be greater than zero"
            );
        }

        Budget budget = getBudget();
        budget.setMonthlyAmount(amount);
        return budgetRepository.save(budget);
    }
}
