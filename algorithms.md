---
title: "Enhancement Two: Algorithms and Data Structures"
---

# Enhancement Two: Algorithms and Data Structures

[View WeightTrends.java](weighttracker/app/src/main/java/com/example/weighttracker_ayala/WeightTrends.java)

[View the unit tests](weighttracker/app/src/test/java/com/example/weighttracker_ayala/WeightTrendsTest.java)

WeightTracker is an Android app I built in Java for CS 360. The original app stored weight entries and listed them back, but it never did anything with the data. I pointed that out in my code review as the main weakness in this category.

I added a WeightTrends class that calculates a moving average over the entries and shows whether the user is losing, gaining, or holding. It uses a running sum, so each step adds the new entry and subtracts the old one instead of adding up the whole window again. It's plain Java with no Android code, so I could test it on its own. I wrote 18 unit tests, including empty lists, a single entry, and fewer entries than the window size.

This meets the algorithms and data structures outcome I planned in Module One. The main trade-off was new users. I chose to average whatever entries they have instead of waiting for seven, so the app shows something right away, but the early averages change a lot.

Testing also showed that dates were sorting wrong across a new year. That was a database problem, so I fixed it in the databases enhancement.
