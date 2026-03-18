# team12-chatbot-api

Project root README.

## Diagrams

- **ERD files:** [SVG](./docs/diagrams/chatbot_erd.svg) | [PNG](./docs/diagrams/chatbot_erd.png)
- **ERD draw.io source:** [Open in draw.io](https://app.diagrams.net/#G1Ix1IFXWZ0Rpvn5BVM68P6WLOcQkdWP59#%7B%22pageId%22%3A%22KjJnbeDkw62hERHSN6ZB%22%7D)
- **ERD (preview):** ![ERD PNG](./docs/diagrams/chatbot_erd.png)

## Setup backend:

- Create .env file and copy data from .env-example inside it
- Ensure that Docker is running in your computer
- Run `docker compose up`
- Testing, API EP: `GET` `localhost:8080/api/users` gives the list of users (initially created on first run as testusers)

