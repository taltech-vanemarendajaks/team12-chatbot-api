# Backend structure
## Structure of packages, classes

- This is an initial idea of the Chatbot API structure by packages / classes, following the feature-based approach recommended by the Spring Boot 4.0.3 documentation

### Packages / classes:

chat

    ChatController
    ChatService
    ChatServiceImpl
    ChatRequest
    ChatResponse

ai

    OpenAIService
    PromptBuilder

rag*

    RagService
    EmbeddingService
    VectorSearchService

rule

    RuleService
    RuleMatcher

question

    Question
    QuestionRepository

answer

    Answer
    AnswerRepository

config

    OpenAIConfig

util

    ResponseExtractor

- **Not in MVP:**

security*

    SecurityConfig
    JwtFilter
    AuthController
    AuthService

user*

    User
    UserRepository
    UserService

*Not in MVP, however RAG is

## Description and purpose of each package

### 1. Chat package (API entry point)

```text
chat
   ChatController
   ChatService
   ChatServiceImpl
   ChatRequest
   ChatResponse
```

#### 1) ChatController

- Purpose: **HTTP endpoint**

- Responsibilities:

```text
receive request
validate input
call ChatService
return response
```

- Endpoint:

```text
POST /chat
```

**The controller does not contain AI logic.**

---

#### 2) ChatService, ChatServiceImpl

- Purpose:
    - **orchestrates the chatbot logic**
    - ChatService decides how the question is answered
    - the brain of the backend

ChatService - interface
ChatServiceImpl - corresponding service class

- An example flow:

```text
user question
   ↓
save question
   ↓
RuleService
   ↓
RagService
   ↓
OpenAIService
   ↓
save answer
   ↓
return answer
```

---

#### 3) ChatRequest

- Purpose: **API input DTO**
- DTOs separate **API data from entities**

Example fields:

```text
question
userId
sessionId
```

---

#### 4) ChatResponse

- Purpose: **API output DTO**

Example:

```text
answer
source (rule/rag/ai)
answerType (text/image/video)
confidence (? not MVP)
```

---

### 2. AI package

```text
ai
   OpenAIService
   PromptBuilder
```

#### 1) OpenAIService

- Purpose: **integration with OpenAI**
- Responsibilities:

```text
send prompt
receive response
extract text
handle errors
```
(This is where `getOpenAIAnswer()` logic will be)


---

#### 2) PromptBuilder

- Purpose:
    - build prompts
    - Separating this keeps prompts clean and maintainable
- Example responsibilities:

```text
add system prompt
add question
add RAG context
format messages
```

- Example prompt:

```text
You are BossBot answering question of company leaders, that they ask from the developer

Question:
...
```

---

### 3. RAG package

```text
rag
   RagService
   EmbeddingService
   VectorSearchService
```

#### 1) RagService

- Purpose: **RAG workflow**

- Responsibilities:

```text
embed question
search similar documents
retrieve context
return documents
```

---

#### 2) EmbeddingService

- Responsibilities:

```text
call embedding model
generate vector
store vector
```

- Example model:

```text
text-embedding-3-small
```

---

#### 3) VectorSearchService

- Responsibilities:

```text
search pgvector
calculate similarity
return best documents
```

---

### 4. Rule package

```text
rule
   RuleService
   RuleMatcher
```

This implements the **rule-based layer** that checks for predefined answers before going to RAG or AI.

---

#### 1) RuleService

- Responsibilities:

```text
load rules from DB
check question
return predefined answer
```

- Example:

```text
Q: Where is Amadeus API docs?
A: https://...
```

No AI needed.

---

#### 2) RuleMatcher

- Responsibilities:

```text
detect rule match
pattern matching
keyword detection
```

- Example:

```text
contains("amadeus api")
```

---

### 5. Question package

```text
question
   Question
   QuestionRepository
```

- Purpose: **store asked questions**

#### 1) Question - entity
- Entity example:

```text
id
questionText
userId
createdAt
```

#### 2) Repository:

```text
save question
find question
search question
```

---

### 6. Answer package

```text
answer
   Answer
   AnswerRepository
```

Purpose: **store responses**

#### 1) Answer - entity


```text
id
questionId
answerText
answerSource (? not MVP)
answerType
created_at
```

---

### 7. Config package

```text
config
   OpenAIConfig
```

- Purpose: **configuration beans**

Contains:

```text
OpenAI client bean
security configuration
cors configuration
```

Bean:

```text
OpenAIClient
```

---

### 8. Util package

```text
util
   ResponseExtractor
```

- Purpose:
    - **helper logic**
    - extract text from OpenAI response
    - keeps services clean.

---

### 9. Users and authentication

When adding users later:

#### 1) User

```text
user
   User
   UserRepository
   UserService
```

- Entity:

```text
User
   id
   username
   password
   role
```

---

### 10. Security package

- Purpose: **authentication and authorization logic**

```text
security
   SecurityConfig
   JwtFilter
   AuthController
   AuthService
```

- Responsibilities:

```text
login
token validation
authentication
authorization
```

---

### 11. MVP, and Full project structure

#### MVP
```text
chat
ai
rule
question
answer
config
util
```

#### Full version with rag, users and authentication
- Add rag, users and authentication

```text
chat
ai
rag
rule
question
answer
user
security
config
util

```

---

### 12. Final architecture

```text
Controller
   ↓
ChatService
   ↓
RuleService
   ↓
RagService
   ↓
OpenAIService
   ↓
Repository
```

The logic of turning the question into an answer is:

```text
rule → rag → LLM fallback
```

---

### 13. Design rules

- Layers:

```text
- API layer
- business logic
- integration layer
- database layer
```

- In current project these layers are represented as:

```text
- chat
- service
- ai
- repository
```

---

## References:
- https://docs.spring.io/spring-boot/reference/using/structuring-your-code.html#using.structuring-your-code
- https://developers.openai.com/api/docs/guides/embeddings
- https://developers.openai.com/api/reference/overview
- https://developers.openai.com/api/reference/resources/responses
- https://github.com/openai/openai-java/tree/main?tab=readme-ov-file#openai-java-api-library
- The structure designed by using the help of https://chatgpt.com/
