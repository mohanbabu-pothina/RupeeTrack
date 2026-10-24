# 💰 RupeeTrack — Smart Expense Tracker

RupeeTrack is a personal expense-tracking application designed to help users record expenses, monitor spending, manage monthly budgets, and understand their financial habits. It also integrates a locally running AI model to answer questions about expenses using read-only tool calling.

## ✨ Features

- **Expense Management** — Record and manage daily expenses.
- **Expense Summaries** — View expense totals and analyze spending.
- **Category-wise Tracking** — Understand where your money goes.
- **Monthly Budget** — Track spending against a monthly budget.
- **AI-Powered Queries** — Ask questions about expense totals, spending categories, and your monthly budget using natural language.
- **Local AI Integration** — Uses Ollama with Spring AI for local model inference.
- **Database Persistence** — Stores expense data in PostgreSQL.

## 🛠️ Tech Stack

| Component | Technology |
|---|---|
| Frontend | Angular |
| Backend | Java, Spring Boot |
| Database | PostgreSQL |
| AI Integration | Spring AI |
| Local AI Model | Ollama (`qwen3:4b`) |
| Version Control | Git and GitHub |

## 📁 Project Structure

```text
RupeeTrack/
├── backend/       # Spring Boot REST API
├── frontend/      # Angular application
├── .gitignore
└── README.md
```

*The folder names may vary slightly depending on your local project structure.*

## ⚙️ Prerequisites

Install the following before running the application:

- Node.js and npm
- Angular CLI
- Java version compatible with your Spring Boot project
- Maven, if not using the included Maven Wrapper
- PostgreSQL
- Ollama (for AI features)

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/mohanbabu-pothina/RupeeTrack.git
cd RupeeTrack
```

### 2. Configure PostgreSQL

Create a PostgreSQL database for RupeeTrack.

Configure your local Spring Boot database settings with the database URL, username, and password.

**Security:** Keep database credentials in a local configuration file or environment variables. Do not commit passwords, API keys, or other secrets to GitHub.

### 3. Start the backend

Navigate to the backend directory:

```bash
cd backend
```

Run the application using the Maven Wrapper if available:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell, use:

```powershell
.\mvnw.cmd spring-boot:run
```

Alternatively, if Maven is installed:

```bash
mvn spring-boot:run
```

Ensure PostgreSQL is running and the database configuration is correct.

### 4. Start the frontend

Open a separate terminal and navigate to your Angular frontend directory:

```bash
cd frontend
npm install
ng serve
```

Open the local development URL printed by Angular, typically:

http://localhost:4200

### 5. Set up local AI (optional)

Install and start Ollama, then pull the model:

```bash
ollama pull qwen3:4b
```

Make sure the Ollama service is running and Spring AI is configured to connect to it. AI features require the relevant backend integration and model configuration.

## 🤖 AI Capabilities

RupeeTrack integrates a local language model to interpret natural-language questions and invoke approved, read-only backend tools.

Example questions:

- "What is my total spending this month?"
- "How much have I spent in each category?"
- "How much of my monthly budget is remaining?"

The AI tools are intended to retrieve expense and budget information rather than modify financial records.

## 🔐 Security Notes

- Never commit database passwords, credentials, or private configuration files.
- Keep local configuration out of version control.
- Use appropriate authentication and access controls before exposing the application to other users or the internet.
- Do not expose PostgreSQL directly to the public internet.
- Review configuration and network security before deploying the application.

## 🗺️ Future Improvements

- Expense charts and visual reports
- Advanced monthly analytics
- Expense filtering and search
- Export reports to CSV or PDF
- Improved AI-driven spending insights
- Secure multi-user support

## 👨‍💻 Author

**Mohan Pothina**

GitHub: [@mohanbabu-pothina](https://github.com/mohanbabu-pothina)

---

*RupeeTrack — Track smarter. Spend better.*
