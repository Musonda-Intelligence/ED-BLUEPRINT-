# ED Blueprint Android app: progress

Theme: black, with the logo's red and orange (`ui/theme/Color.kt`). Every screen uses
`EdBlueprintTheme`, `PrimaryButton`, `EdTextField`, `TabScreen`, `EdCard` and `EmptyState`.

## MVP (this version)

Student app
- Logo as app logo, launcher icon, loading, login and register screens
- Register (logs in automatically), login, logout with confirmation; token in encrypted storage
- Home: subscription status, recently added, available quizzes, quiz progress
- Courses: topics, then the lessons in each topic
- Past Papers: free for every logged-in student
- PDF viewer inside the app (temporary file, deleted when you leave the screen)
- Video player inside the app (Media3 ExoPlayer, play/pause, seek bar, full screen)
- Locked premium items open a "Subscription required" screen; a 403 from the server does the same
- Quizzes: timer, question navigation, submit confirmation, server-marked score, review, previous attempts
- Empty, loading and error states with a "Try again" button everywhere

Admin
- Admin accounts (role from the server) land on an admin screen that points to the website
  admin dashboard. Uploading, editing, publishing, students and subscriptions are managed there.

## Not in the MVP

- Admin screens inside the app (uploads, quiz builder, student and subscription management)
- Payments. No payment is faked: the subscription screen says so. Activate subscriptions
  on the server after a payment is confirmed (MTN, Airtel, Zamtel integration comes later).
- Continue-learning tracking, dark/light switch (the brand is one dark theme)
- Automated tests, and this code has NOT been compiled yet (see README "First build")

## Server work still required (cPanel / Laravel)

Every endpoint in `docs/API_CONTRACT.md`, above all the 403 check on `/materials/{id}/file`
and server-side quiz marking.
