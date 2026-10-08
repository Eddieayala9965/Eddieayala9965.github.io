---
title: "Enhancement Three: Databases"
---

# Enhancement Three: Databases

[View the enhanced DatabaseHelper.java](weighttracker/app/src/main/java/com/example/weighttracker_ayala/DatabaseHelper.java)

[View the original DatabaseHelper.java](weighttracker-original/app/src/main/java/com/example/weighttracker_ayala/DatabaseHelper.java)

WeightTracker is an Android app I built in Java for CS 360. It stores everything in a SQLite database. My code review found three problems there. There was no real foreign key between the tables, no index on user_id, and the onUpgrade method deleted all the data whenever the database version changed. During the algorithms work I also found that dates were sorting wrong across a new year.

I added a foreign key and turned on foreign key checks, added an index on user_id and date, and changed the date format so dates sort correctly. I also replaced onUpgrade with a migration that keeps the existing data. I added eight unit tests, and all 27 tests in the project pass.

This covers the database part of the software engineering outcome and helps the security outcome. The hardest part was learning that SQLite can't add a foreign key to an existing table, so I had to rebuild the table and copy the data over. I tested the migration on the emulator with an old copy of the database.

[Back to portfolio home](/)
