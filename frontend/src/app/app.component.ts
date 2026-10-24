
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Expense, CATEGORIES, PAYMENT_METHODS } from './expense.model';
import { ExpenseService } from './expense.service';
import { BudgetService } from './budget.service';
import { AiChatService } from './ai-chat.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, CurrencyPipe, DatePipe],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  private readonly expenseService = inject(ExpenseService);
  private readonly budgetService = inject(BudgetService);
  readonly aiChatService = inject(AiChatService);

  expenses: Expense[] = [];
  categories = CATEGORIES;
  paymentMethods = PAYMENT_METHODS;

  monthlyBudget = 18000;
  budgetDraft = 18000;
  budgetSaving = false;
  budgetMessage = '';
  budgetError = '';

  selectedMonth = new Date().toISOString().slice(0, 7);
  searchText = '';
  selectedCategory = 'All';
  editingId: number | null = null;

  loading = false;
  error = '';
  success = '';

  // AI floating chat
  isAiChatOpen = false;
  aiMessage = '';
  aiLoading = false;
  aiError = '';

  aiMessages: {
    role: 'user' | 'assistant';
    text: string;
  }[] = [];

  form: Expense = this.emptyForm();

  ngOnInit(): void {
    this.loadExpenses();
    this.loadBudget();
  }

  private emptyForm(): Expense {
    return {
      description: '',
      amount: 0,
      category: 'Food',
      paymentMethod: 'UPI',
      expenseDate: new Date().toISOString().slice(0, 10),
      notes: ''
    };
  }

  // AI chat methods
  askAiSuggestion(message: string): void {
    this.aiMessage = message;
    this.sendAiMessage();
  }

  sendAiMessage(): void {
    const message = this.aiMessage.trim();

    if (!message || this.aiLoading) {
      return;
    }

    this.aiMessages.push({
      role: 'user',
      text: message
    });

    this.aiMessage = '';
    this.aiLoading = true;
    this.aiError = '';

    this.aiChatService.sendMessage(message).subscribe({
      next: (response) => {
        this.aiMessages.push({
          role: 'assistant',
          text: response.answer
        });
      },
      error: (error) => {
        console.error('AI chat error:', error);
        this.aiError =
          'Unable to get a response. Check that the backend and Ollama are running.';
        this.aiLoading = false;
      },
      complete: () => {
        this.aiLoading = false;
      }
    });
  }

  // Expense loading
  loadExpenses(): void {
    this.loading = true;
    this.error = '';

    this.expenseService.getAll().subscribe({
      next: data => {
        this.expenses = data;
        this.loading = false;
      },
      error: () => {
        this.error =
          'Could not connect to the API. Start the Spring Boot backend and check PostgreSQL settings.';
        this.loading = false;
      }
    });
  }

  // Budget loading
  loadBudget(): void {
    this.budgetError = '';

    this.budgetService.getBudget().subscribe({
      next: budget => {
        this.monthlyBudget = Number(budget.monthlyAmount);
        this.budgetDraft = this.monthlyBudget;
      },
      error: () => {
        this.budgetError =
          'Could not load the saved budget. Check the budget API.';
      }
    });
  }

  saveBudget(): void {
    const amount = Number(this.budgetDraft);

    if (!Number.isFinite(amount) || amount <= 0) {
      this.budgetError = 'Enter a budget greater than ₹0.';
      this.budgetMessage = '';
      return;
    }

    this.budgetSaving = true;
    this.budgetError = '';
    this.budgetMessage = '';

    this.budgetService.updateBudget(amount).subscribe({
      next: budget => {
        this.monthlyBudget = Number(budget.monthlyAmount);
        this.budgetDraft = this.monthlyBudget;
        this.budgetMessage = 'Monthly budget saved successfully.';
        this.budgetSaving = false;
      },
      error: () => {
        this.budgetError =
          'Could not save the budget. Please try again.';
        this.budgetSaving = false;
      }
    });
  }

  // Monthly expense calculations
  get monthExpenses(): Expense[] {
    return this.expenses.filter(
      e => e.expenseDate?.startsWith(this.selectedMonth)
    );
  }

  get totalSpent(): number {
    return this.monthExpenses.reduce(
      (sum, e) => sum + Number(e.amount),
      0
    );
  }

  get remainingBudget(): number {
    return this.monthlyBudget - this.totalSpent;
  }

  get filteredExpenses(): Expense[] {
    const search = this.searchText.trim().toLowerCase();

    return this.monthExpenses.filter(e => {
      const matchesSearch =
        !search ||
        e.description.toLowerCase().includes(search) ||
        (e.notes || '').toLowerCase().includes(search);

      const matchesCategory =
        this.selectedCategory === 'All' ||
        e.category === this.selectedCategory;

      return matchesSearch && matchesCategory;
    });
  }

  get categoryTotals(): {
    name: string;
    amount: number;
    percent: number;
  }[] {
    const totals = new Map<string, number>();

    this.monthExpenses.forEach(e => {
      totals.set(
        e.category,
        (totals.get(e.category) || 0) + Number(e.amount)
      );
    });

    return Array.from(totals.entries())
      .map(([name, amount]) => ({
        name,
        amount,
        percent: this.totalSpent
          ? (amount / this.totalSpent) * 100
          : 0
      }))
      .sort((a, b) => b.amount - a.amount);
  }

  get budgetPercent(): number {
    return this.monthlyBudget > 0
      ? Math.min(100, (this.totalSpent / this.monthlyBudget) * 100)
      : 0;
  }

  // Add or update expense
  saveExpense(): void {
    this.error = '';
    this.success = '';

    if (
      !this.form.description.trim() ||
      !this.form.amount ||
      this.form.amount <= 0 ||
      !this.form.expenseDate
    ) {
      this.error =
        'Enter a description, a positive amount, and a date.';
      return;
    }

    const payload: Expense = {
      ...this.form,
      description: this.form.description.trim(),
      amount: Number(this.form.amount),
      notes: (this.form.notes || '').trim()
    };

    const request = this.editingId === null
      ? this.expenseService.create(payload)
      : this.expenseService.update(this.editingId, payload);

    request.subscribe({
      next: saved => {
        if (this.editingId === null) {
          this.expenses = [saved, ...this.expenses];
          this.success = 'Expense saved successfully.';
        } else {
          this.expenses = this.expenses.map(
            e => e.id === saved.id ? saved : e
          );
          this.success = 'Expense updated successfully.';
        }

        this.cancelEdit();
      },
      error: () => {
        this.error =
          'Could not save the expense. Check that the backend is running and the values are valid.';
      }
    });
  }

  editExpense(expense: Expense): void {
    this.editingId = expense.id ?? null;
    this.form = {
      ...expense,
      amount: Number(expense.amount),
      notes: expense.notes || ''
    };

    this.error = '';
    this.success = '';

    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  cancelEdit(): void {
    this.editingId = null;
    this.form = this.emptyForm();
    this.success = this.success || '';
  }

  // Delete expense
  deleteExpense(expense: Expense): void {
    if (
      !expense.id ||
      !confirm(
        `Delete "${expense.description}" for ₹${Number(expense.amount).toFixed(2)}?`
      )
    ) {
      return;
    }

    this.expenseService.delete(expense.id).subscribe({
      next: () => {
        this.expenses = this.expenses.filter(
          e => e.id !== expense.id
        );
        this.success = 'Expense deleted.';
        this.error = '';
      },
      error: () => {
        this.error = 'Could not delete this expense.';
      }
    });
  }

  // Export expenses to CSV
  exportCsv(): void {
    const rows = [
      [
        'Date',
        'Description',
        'Category',
        'Payment method',
        'Amount (INR)',
        'Notes'
      ],
      ...this.filteredExpenses.map(e => [
        e.expenseDate,
        e.description,
        e.category,
        e.paymentMethod,
        String(e.amount),
        e.notes || ''
      ])
    ];

    const csv = rows
      .map(row =>
        row
          .map(value => `"${String(value).replace(/"/g, '""')}"`)
          .join(',')
      )
      .join('\r\n');

    const url = URL.createObjectURL(
      new Blob([csv], { type: 'text/csv;charset=utf-8;' })
    );

    const link = document.createElement('a');
    link.href = url;
    link.download = `rupeetrack-${this.selectedMonth}.csv`;
    link.click();

    URL.revokeObjectURL(url);
  }

  trackById(
    _: number,
    expense: Expense
  ): number | undefined {
    return expense.id;
  }
}
