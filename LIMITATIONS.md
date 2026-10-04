# Known Limitations & Future Improvements

SnapQuest was built as a final-year project: a complete Android + Firebase app
(email/Google auth, Realtime Database, Storage, Cloud Messaging, and two
Cloud Functions) centered on a "one photo per day" social mechanic. It met
its goals as an FYP. This document is a candid list of the tradeoffs made to
hit that scope, and what the next iteration would address — written as a
reference for code review, interviews, or picking the project back up.

## Architecture

- **No server-side validation layer.** Every Activity talks directly to the
  Firebase SDK. Business rules — most importantly the one-post-per-day limit
  (`HomeActivity.kt`) — are enforced client-side only. A modified client or a
  direct REST call with a stolen ID token could bypass it. The fix is to
  route mutating writes through Cloud Functions (the two notification
  functions in `functions/index.js` already show the pattern) rather than
  letting clients write `posts/` directly.
- **No repository/ViewModel layer.** Activities both render views and own all
  Firebase logic inline. This makes the code hard to unit test, and several
  listeners (`addValueEventListener` in `FeedActivity`, `TrendingActivity`,
  `ProfileActivity`, `CommentsActivity`) are never detached in `onDestroy`,
  which leaks them across activity recreation.
- **Feed/Trending don't scale past a demo.** `FeedActivity` and
  `TrendingActivity` pull the *entire* `posts/` tree on every load, then run
  an extra per-post lookup into `users/` for the username and profile
  picture (N+1 queries). There's no pagination and no server-side index —
  cost and latency grow with total users across the whole app, not with what
  is actually shown on screen. `TrendingActivity` additionally wraps async
  `get()` calls inside `addValueEventListener`, which can produce duplicate
  entries on re-fire. Moving to Firestore (with a composite index) or a
  denormalized per-day fan-out node would fix both the query shape and the
  duplication risk.

## Data integrity

- **Like/comment counters were a read-then-write race** — fixed in this pass
  by moving both to `DatabaseReference.runTransaction`
  (`PostAdapter.kt`, `CommentsActivity.kt`).
- **The fix is not a full guarantee.** The new Realtime Database rules
  (`database.rules.json`) validate that `likes`/`commentCount` are
  non-negative numbers, but they can't validate that a write is a *correct*
  increment — a client could still set `likes` to any non-negative value in
  one write. True integrity for these counters means making a Cloud Function
  the only writer, with clients only ever requesting a like/comment and
  never touching the counter directly.
- **`notifyOnComment` (functions/index.js) picks the "latest" comment via
  `Object.values(comments)[length - 1]`**, which relies on object key
  insertion order rather than an explicit timestamp sort, and — unlike
  `notifyOnLike` — has no guard against notifying a user about their own
  comment.
- **Dead code path:** `PostAdapter.sendNotification` writes to a
  `/notifications` node that no Cloud Function reads. The real push
  notification for likes already comes from `notifyOnLike`; this write is a
  leftover and can be removed.

## Security

- **Fixed in this pass:** the hardcoded Google OAuth client ID in
  `MainActivity.kt` now reads from the generated `R.string.default_web_client_id`
  resource, and `database.rules.json` now locks the Realtime Database down
  (previously there were no rules in the repo at all — anyone could read or
  write any path if the console was still on test rules). The new rules were
  validated against the Firebase Database Emulator: unauthenticated access,
  cross-user writes to another user's profile, impersonated likes/comments,
  and negative-count writes are all correctly denied; owner writes,
  per-user like toggles, and new comments are correctly allowed.
- **Read access is collection-wide.** `SearchActivity` downloads the entire
  `users/` node and filters client-side, and the feed listens at `/posts`,
  so the rules must grant read on both whole collections to any signed-in
  user. That means any authenticated account can enumerate every user record
  — including `email` and `fcmToken` — and every post. Narrowing this is not
  a rules change but a query change: server-side search (or a separate
  public-profile node holding only username and photo) would let the rules
  keep emails and tokens private.
- **Not addressed:** there are no Firebase **Storage** security rules in the
  repo, so uploads to the Storage bucket (`posts/`, `profiles/`) are
  unprotected unless the console has rules the repo doesn't reflect.
- `app/google-services.json` is committed. This is normal and required for
  the app to build — the values in it (API key, app ID) identify the Firebase
  project rather than acting as secrets; the actual access boundary is the
  security rules, not this file. Removing it would break the build for
  anyone cloning the repo.

## Notifications

- `POST_NOTIFICATIONS` is declared in `AndroidManifest.xml` but never
  requested at runtime. On Android 13+ (API 33+), which is most real devices
  given this app's `minSdk 24`/`targetSdk 36` range, notifications will
  silently never appear until the permission is explicitly requested via
  `ActivityResultContracts.RequestPermission`.

## UI / Platform

**Fixed in this pass:**

- `item_search.xml` rendered usernames in black (`#000000`) on the app's
  near-black `#121212` background, making search results effectively
  invisible; its avatar also used a light-grey square background while
  `SearchAdapter` circle-crops the image. Both corrected to match the
  avatar/text treatment used in `item_post.xml` and `item_comment.xml`.
- `item_comment.xml` placed the username and comment text in a horizontal
  row with no weight, so long comments ran off the right edge instead of
  wrapping. Restructured into a weighted column so text wraps, and the
  `Comment.timestamp` field — previously stored but never displayed — now
  renders as a relative time.
- `activity_upload.xml` was a fixed `LinearLayout` tall enough (header +
  320dp preview + caption + button) to push "Upload Photo" off-screen on
  short screens or in landscape, with no way to scroll to it. Now wrapped in
  a `ScrollView`.
- Header top spacing was a different hand-picked value on every screen
  (48/60/70/80/90/100dp), so titles visibly jumped when navigating. All
  roots now set `android:fitsSystemWindows="true"` so the status bar inset
  is applied by the framework rather than guessed, and the title spacing
  above it is uniform per screen class (24dp content screens, 32dp
  Home/Profile, 64dp auth screens).

**Still open:**

- No `android:contentDescription` on most `ImageView`s (added only to the
  avatars and preview touched above) — a gap for screen-reader
  accessibility.
- Colors are hardcoded hex literals throughout every layout rather than
  theme/color resources; `colors.xml` holds only `black` and `white`. The
  app declares a Material3 `DayNight` parent theme but never uses day/night
  attributes, so it is always dark regardless of the system setting.
  Centralizing these is what would have prevented the `item_search` bug.
- No empty, loading, or error state in Feed/Trending beyond a single toast
  on an empty search result.
- `FeedActivity` uses a one-shot `addListenerForSingleValueEvent`, so new
  posts do not appear until the screen is re-entered, while
  `TrendingActivity` uses a live `addValueEventListener` — the two screens
  behave inconsistently, and there is no pull-to-refresh on either.
- No back affordance on any child screen (the theme is `NoActionBar` and no
  toolbars are set up); navigation relies entirely on the system back
  gesture.
- `item_gallery.xml` hardcodes a 120dp cell height in a 3-column grid, so
  cells are not square on wider screens.
- `MainActivity.kt` still uses the deprecated
  `startActivityForResult`/`onActivityResult` pair for Google Sign-In, while
  every other picker in the app already uses the modern
  `ActivityResultContracts` API.

## If continued

In rough priority order:

1. Move the one-post-per-day check and all counter mutations behind Cloud
   Functions, so clients request actions rather than writing state directly.
2. Add Storage security rules alongside the new Database rules.
3. Request `POST_NOTIFICATIONS` at runtime.
4. Restructure the feed query (pagination, or a fan-out write at post time)
   instead of reading the full `posts/` tree per load.
5. Introduce a repository layer so Firebase isn't wired directly into every
   Activity, and detach listeners in `onDestroy`.
