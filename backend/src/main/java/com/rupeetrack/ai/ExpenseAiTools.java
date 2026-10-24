package com.rupeetrack.ai;


import com.rupeetrack.budget.BudgetService;
import com.rupeetrack.expense.Expense;
import com.rupeetrack.expense.ExpenseRepository;
import org.springframework.stereotype.Component;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;

@Component
public class ExpenseAiTools {

    private final ExpenseRepository expenseRepository;
    private final BudgetService budgetService;

    public ExpenseAiTools(
            ExpenseRepository expenseRepository,
            BudgetService budgetService) {
        this.expenseRepository = expenseRepository;
        this.budgetService = budgetService;
    }

    private List<Expense> getCurrentMonthExpenses() {
        YearMonth month = YearMonth.now();
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        return expenseRepository.findAll()
                .stream()
                .filter(e -> e.getExpenseDate() != null)
                .filter(e -> !e.getExpenseDate().isBefore(start))
                .filter(e -> !e.getExpenseDate().isAfter(end))
                .toList();
    }

    private BigDecimal calculateTotal(List<Expense> expenses) {
        return expenses.stream()
                .map(Expense::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Tool(description = "Get the total amount spent during the current calendar month.")
    public String getMonthlyExpenseTotal() {
        List<Expense> expenses = getCurrentMonthExpenses();
        BigDecimal total = calculateTotal(expenses);

        return "Month: " + YearMonth.now()
                + ", number of expenses: " + expenses.size()
                + ", total spent: ₹" + total;
    }

    @Tool(description = "Get the current month's spending for a specific expense category, such as Food, Travel, Shopping or Bills.")
    public String getExpensesByCategory(
            @ToolParam(description = "The expense category to search for")
            String category) {

        if (category == null || category.isBlank()) {
            return "Please provide an expense category.";
        }

        List<Expense> matchingExpenses = getCurrentMonthExpenses()
                .stream()
                .filter(e -> e.getCategory() != null)
                .filter(e -> e.getCategory().trim()
                        .equalsIgnoreCase(category.trim()))
                .toList();

        BigDecimal total = calculateTotal(matchingExpenses);

        return "Category: " + category.trim()
                + ", month: " + YearMonth.now()
                + ", number of expenses: " + matchingExpenses.size()
                + ", total spent: ₹" + total;
    }

    @Tool(description = "Get the monthly budget, current month's total spending, and remaining budget.")
    public String getBudgetStatus() {
        BigDecimal budget =
                budgetService.getBudget().getMonthlyAmount();

        BigDecimal spent =
                calculateTotal(getCurrentMonthExpenses());

        BigDecimal remaining = budget.subtract(spent);

        return "Month: " + YearMonth.now()
                + ", monthly budget: ₹" + budget
                + ", spent this month: ₹" + spent
                + ", remaining budget: ₹" + remaining;
    }

    // Spending today
    @Tool(description = "Get total expenses and expense details for today.")
    public String getExpensesForToday() {
        LocalDate today = LocalDate.now();
        return summarizeExpenses("today", today, today);
    }

    // Spending yesterday
    @Tool(description = "Get total expenses and expense details for yesterday.")
    public String getExpensesForYesterday() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return summarizeExpenses("yesterday", yesterday, yesterday);
    }

    // Last 7 calendar days, including today
    @Tool(description = "Get total expenses and details for the last 7 days, including today.")
    public String getExpensesForLast7Days() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);

        return summarizeExpenses("the last 7 days", start, today);
    }

    // Current week: Monday through today
    @Tool(description = "Get total expenses and details for this week, from Monday through today.")
    public String getExpensesForThisWeek() {
        LocalDate today = LocalDate.now();

        LocalDate monday = today.with(
                TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        );

        return summarizeExpenses("this week", monday, today);
    }

    // Shared date-range calculation
    private String summarizeExpenses(
            String period,
            LocalDate start,
            LocalDate end) {

        List<Expense> expenses = expenseRepository.findAll()
                .stream()
                .filter(e -> e.getExpenseDate() != null)
                .filter(e -> !e.getExpenseDate().isBefore(start))
                .filter(e -> !e.getExpenseDate().isAfter(end))
                .sorted(
                        Comparator.comparing(Expense::getExpenseDate)
                                .reversed()
                )
                .toList();

        BigDecimal total = calculateTotal(expenses);

        StringBuilder result = new StringBuilder();

        result.append("Period: ").append(period)
                .append(" (").append(start)
                .append(" to ").append(end).append(")")
                .append("\nNumber of expenses: ")
                .append(expenses.size())
                .append("\nTotal spent: ₹")
                .append(total);

        if (expenses.isEmpty()) {
            result.append("\nNo expenses recorded for this period.");
            return result.toString();
        }

        result.append("\n\nExpense details:");

        for (Expense expense : expenses) {
            result.append("\n")
                    .append(expense.getExpenseDate())
                    .append(" - ")
                    .append(expense.getCategory() == null
                            ? "Uncategorized"
                            : expense.getCategory())
                    .append(": ₹")
                    .append(expense.getAmount() == null
                            ? BigDecimal.ZERO
                            : expense.getAmount());
        }

        return result.toString();
    }
}
