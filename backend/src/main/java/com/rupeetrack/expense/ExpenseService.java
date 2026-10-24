package com.rupeetrack.expense;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ExpenseService {
    private final ExpenseRepository repository;

    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Expense> findAll() {
        return repository.findAllByOrderByExpenseDateDescIdDesc();
    }

    @Transactional(readOnly = true)
    public Expense findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ExpenseNotFoundException(id));
    }

    public Expense create(Expense expense) {
        expense.setId(null);
        return repository.save(expense);
    }

    public Expense update(Long id, Expense updated) {
        Expense existing = findById(id);
        existing.setDescription(updated.getDescription());
        existing.setAmount(updated.getAmount());
        existing.setCategory(updated.getCategory());
        existing.setPaymentMethod(updated.getPaymentMethod());
        existing.setExpenseDate(updated.getExpenseDate());
        existing.setNotes(updated.getNotes());
        return repository.save(existing);
    }

    public void delete(Long id) {
        Expense existing = findById(id);
        repository.delete(existing);
    }
}
