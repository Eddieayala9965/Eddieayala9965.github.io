---
title: "Professional Self-Assessment"
---

# Professional Self-Assessment

I started the Computer Science program at SNHU in October 2024. When I started, I planned to go into software engineering. During the program my goal changed to Linux system administration, with security as a long term goal. A lot of jobs now mix coding and system administration, and the program gave me a base in both. This ePortfolio shows my code before and after each change.

For communication, I recorded a code review of WeightTracker that explained the problems I found and what I planned to fix. I also wrote a narrative for each enhancement and added comments in the code. I explained the app to my girlfriend, who isn't technical, so I had to describe things like password hashing by what they do for the user. Most of my collaboration in the program has been indirect. My code comments explain why each change was made so another developer can follow it, and my code review and narratives list the problem, the fix, and the trade-offs so a team lead could decide if the change should go in.

For data structures and algorithms, I added a moving average to WeightTracker. For software engineering, I cleaned up repeated database code and added unit tests, and the project now has 27 tests that pass. For databases, I added a foreign key, an index, and a migration that keeps user data when the database is updated. Outside of the capstone, CS 350, Emerging Systems Architectures and Technologies, had me writing code for embedded systems. It was one of my favorite classes.

Security is the area I'm most interested in. In WeightTracker I replaced plaintext passwords with salted PBKDF2 hashing, and in the database work I added a foreign key, removed orphan rows, and made the app reject bad dates. In CS 410, Reverse Software Engineering, I looked at software from an attacker's side, and we talked about real attacks like the 2011 PlayStation Network breach.

All of the work here is on one app, WeightTracker, an Android app I built in Java for CS 360. The code review comes first. The software design enhancement covers password hashing and the database code cleanup. The algorithms enhancement covers the moving average. The databases enhancement covers the foreign key, the index, the date fix, and the migration. Each one has its own page with a narrative and links to the code.

[Back to portfolio home](/)
