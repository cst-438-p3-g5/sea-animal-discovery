# CST 438 Project 03 - Sea Animal Discovery

Sea Animal Discovery is an application that allows users to explore and learn about different sea animals. The project uses Expo to support Web, Android, and iOS from a single codebase.

## Project Structure

```text
sea-animal-discovery/
├── api/          # Backend API
├── app/          # Expo frontend (Web, Android, iOS)
├── docs/         # Project documentation
├── compose.yml   # Docker configuration
└── README.md
```

## Tech Stack

- **Frontend:** React Native, Expo, TypeScript
- **Navigation:** Expo Router
- **Backend:** REST API
- **Platforms:** Web, Android, iOS

## Getting Started

### Frontend

```bash
cd app
npm install
npx expo start
```

Once Expo starts, press:

- `w` to open Web
- `a` to open Android
- `i` to open iOS (macOS)

### Backend

Run the backend using Docker:

```bash
docker compose up --build
```

## Development

The project uses a shared Expo frontend, allowing developers to build features once for Web, Android, and iOS. The backend is maintained separately in the `api/` directory.