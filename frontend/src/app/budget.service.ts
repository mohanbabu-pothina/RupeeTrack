import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Budget {
  id?: number;
  monthlyAmount: number;
}

@Injectable({
  providedIn: 'root'
})
export class BudgetService {
  private apiUrl = 'http://localhost:8080/api/budget';

  constructor(private http: HttpClient) {}

  getBudget(): Observable<Budget> {
    return this.http.get<Budget>(this.apiUrl);
  }

  updateBudget(monthlyAmount: number): Observable<Budget> {
    return this.http.put<Budget>(this.apiUrl, { monthlyAmount });
  }
}