# Learn2Play React Frontend — Complete UI Conversion

This React frontend was rebuilt from the original Learn2Play Angular project.

## Included pages
- Login
- Sign Up
- Forgot Password
- Reset Password
- Dashboard
- Upload Study Material
- Quiz
- Results
- Review Answers
- Quiz History
- Leaderboard
- Profile
- Statistics / Learning Analytics
- Badges placeholder

## Backend
The Spring Boot backend remains unchanged. The React app uses the same API base URL by default:

`http://localhost:8080`

Set `VITE_API_URL` in `.env` if needed.

## Run
```bash
npm install
npm run dev
```


## Layout fix
This version fixes the React global SCSS `.page` class collision. Each screen now has its own page root class, so the login and other pages keep their original full-width layouts.
