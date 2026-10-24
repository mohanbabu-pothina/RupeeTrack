
package com.rupeetrack.ai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final ChatClient chatClient;
    private final ExpenseAiTools expenseAiTools;

    public AiChatController(
            ChatClient.Builder chatClientBuilder,
            ExpenseAiTools expenseAiTools) {

        this.chatClient = chatClientBuilder.build();
        this.expenseAiTools = expenseAiTools;
    }

    @PostMapping("/chat")
    public Map<String, String> chat(
            @Valid @RequestBody ChatRequest request) {

        String message = request.message().trim();
        String lower = message.toLowerCase();

        // 1. Handle budget questions directly.
        if (lower.contains("budget")
                || lower.contains("remaining balance")
                || lower.contains("left to spend")) {

            return Map.of(
                    "answer",
                    expenseAiTools.getBudgetStatus()
            );
        }

        if (lower.contains("yesterday")) {
            return Map.of(
                    "answer",
                    expenseAiTools.getExpensesForYesterday()
            );
        }

        if (lower.contains("last 7 days") || lower.contains("last seven days") || lower.contains("past 7 days") || lower.contains("past seven days")) {
            return Map.of("answer", expenseAiTools.getExpensesForLast7Days());
        }

        if (lower.contains("this week") || lower.contains("weekly spending") || lower.contains("weekly expenses")) {
            return Map.of(
                    "answer",
                    expenseAiTools.getExpensesForThisWeek()
            );
        }

        if (lower.contains("today")
                && (lower.contains("spend")
                || lower.contains("spent")
                || lower.contains("expense")
                || lower.contains("cost"))) {

            return Map.of(
                    "answer",
                    expenseAiTools.getExpensesForToday()
            );
        }
        // 2. Handle monthly spending questions directly.
        if ((lower.contains("this month")
                || lower.contains("monthly"))
                && (lower.contains("spend")
                || lower.contains("spent")
                || lower.contains("spending")
                || lower.contains("expense"))) {

            return Map.of(
                    "answer",
                    expenseAiTools.getMonthlyExpenseTotal()
            );
        }

        // 3. Handle category spending questions directly.
        Matcher matcher = Pattern.compile(
                "(?i)\\b(?:spend|spent|spending|expenses?)\\b.*?\\b(?:on|in|for|under)\\s+([a-z][a-z &-]*)"
        ).matcher(message);

        if (matcher.find()) {
            String category = matcher.group(1).trim();

            category = category.replaceFirst(
                    "(?i)\\s+(this month|last month|today|please)$",
                    ""
            ).trim();

            return Map.of(
                    "answer",
                    expenseAiTools.getExpensesByCategory(category)
            );
        }

        // 4. Use Ollama for other questions.
        String answer = chatClient.prompt()
                .system("""
                        You are RupeeTrack, a personal expense assistant.
                        Use the available Java tools for financial questions.
                        Never invent financial figures.
                        Keep answers short and clear.
                        Format amounts in Indian rupees (₹).
                        You are read-only.
                        """)
                .user(message)
                .tools(expenseAiTools)
                .call()
                .content();

        if (answer == null || answer.isBlank()) {
            answer = "I couldn't generate an answer. Please try again.";
        }

        return Map.of("answer", answer);
    }

    public record ChatRequest(
            @NotBlank(message = "Message is required")
            @Size(
                    max = 1000,
                    message = "Message must not exceed 1000 characters"
            )
            String message) {
    }
}
