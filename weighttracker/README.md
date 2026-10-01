# WeightTracker - CS 360 Project Three

  ## App Summary
  The WeightTracker app was built to help users log their daily weight, set a
  goal weight, and receive an SMS notification when they reach that goal. The
  core user needs were a secure login system, a persistent database to store
  weight entries, and a simple way to track progress over time without a
  complicated interface. 

  ## Screens and Features
  The app includes five screens: login, weight log, add/edit entry, settings,
  and SMS permissions. Each screen was designed to keep the user focused on one
  task at a time. The weight log displays a summary card with current and goal
  weight at the top so users can see their progress immediately. Navigation was
  kept simple with a toolbar menu and back buttons on every screen so users
  never feel stuck.

  ## Development Approach
  I built the app in layers, starting with the database and working outward to
  the UI. Getting the data layer solid first made connecting the frontend much
  smoother. I tested each feature individually using the Android Emulator before
  moving on, which kept bugs isolated and easier to fix.

  ## Testing
  I used the Android Emulator throughout development, testing each screen and
  feature after every significant change. This included testing both the granted
  and denied paths for SMS permissions to make sure the app continued
  functioning either way. Frequent testing prevented small issues from
  compounding into bigger ones.

  ## Overcoming Challenges
  The biggest challenge was getting the toolbar menu items to respond to taps.
  The root cause turned out to be a layout issue where the CoordinatorLayout was
  not accounting for the system status bar, causing touch events to be
  intercepted before reaching the toolbar. Adding fitsSystemWindows to the root
  layout fixed it, but finding that required digging into the UI hierarchy with
  ADB tools.
  
  ## Strongest Component
  The database layer is where I feel most confident in this project. The SQLite
  implementation handles all CRUD operations cleanly, keeps user data isolated
  by account, and persists everything across sessions. It was also designed to
  support the SMS feature by storing the goal weight and phone number per user,
  which tied all the major features together.
