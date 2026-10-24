import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";


export interface AiChatResponse{
    answer: string;
}

@Injectable({
    providedIn: 'root'
})

export class AiChatService{


    private http = inject(HttpClient);

    private readonly apiUrl = 'http://localhost:8080/api/ai/chat';

    sendMessage(message: string): Observable<AiChatResponse>{
        return this.http.post<AiChatResponse>(this.apiUrl, {
            message
        });
    }
}