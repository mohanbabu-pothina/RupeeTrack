export interface Expense {
  id?: number;
  description: string;
  amount: number;
  category: string;
  paymentMethod: string;
  expenseDate: string;
  notes: string;
  createdAt?: string;
}

export const CATEGORIES = [
  'Food', 'Transport', 'Groceries', 'Rent', 'Bills',
  'Shopping', 'Health', 'Entertainment', 'Education', 'Other'
];

export const PAYMENT_METHODS = ['Cash', 'UPI', 'Debit card', 'Credit card', 'Net banking'];
