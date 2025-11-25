# Milestone 1 - "LUDEX CRYPTA" (Unit 7)

## Table of Contents

1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)

## Overview

### Description
It is an app that collects information from many game databases (like Steam, Epic Games, etc) and allows the user to make their own comprehensive list (games/consoles purchased, upcoming games they want (wishlist), on-sale watchlist) from all the databases.

### App Evaluation

[Evaluation of your app across the following attributes]
- **Category:** Gaming/Video Games (Social & Entertainment)
- **Mobile:** Mobile ensures easy accessibilty, which is convenient for gamers. There will be use of audio or video to play game trailers, push notifications to let users know of upcoming sales, and real-time data from the gaming sites, and location and maps usage to ensure deals and sales are relevant to the user's region and time zone.
- **Story:** Gamers tend to have multiple gaming libraries on multiple consoles and would have a hard time accessing or organizing all of them, this is the solution. It would also help them see which games are on sale without having to connect to 50 different sales-watch apps. Young adults, our peers, love gaming and we all have different gaming systems and those of us on PC either use Steam, Epic Games, Ubisoft, EA, or another site to get games and it sometimes becomes too much keeping track of it all; they would really like a centralized and personalized gaming database.
- **Market:** Any gamer, casual or competitive, could use this app. More people have collectively spent time gaming than there has been in Earth's history, so potentially the user base is endless. This app would greatly help the ever growing gaming community.
- **Habit:** Gamers would use this app daily to check sales and update their lists. The average user would create personalized lists, so they would not just be consumers.
- **Scope:** This app would be technically challenging as it would require frequent API calls to multiple frequently updating sources. A stripped down version of this app would still be interesting, as it would only shrink the number of sources we pull from, meaning the user would still have an extensive library to pick their games from. Finally, this app is clearly defined in both the frontend and the backend since the backend would handle a lot of API calls and database management and the front end would focus on having the correct links to different gaming sources.

## Product Spec

<br>

### 1. User Features (Required and Optional)

**Required Features**
- [X] 1. The user can sign-in and sign-out to the app
- [X] 2. The user can register an account
- [ ] 3. The user can connect to EPIC, Steam, etc with their logins
- [ ] 4. The user can search for games from EPIC's, Steam's, etc libraries to add to their lists
- [ ] 5. The user can search for games within their own lists
- [ ] 6. The user can view descriptions and images of the game they are looking for
- [ ] 7. The user can see which games are on sale
- [x] 8. The user can use the navigation bar to go from one page to another
- [ ] 9. The user's data can save between sessions on the app
- [ ] 10. The user can see trending games
- [x] 11. The user can see their wishlist or GameVault list

**Optional Features**
- [ ] 1. The user can play the game trailers for the games they are looking for
- [ ] 2. The user will be able to look for friends they can connect with
- [ ] 3. The user can connect to their console's accounts (XBOX, Nintendo, Playstation, etc)
- [ ] 4. The user can request games/libraries that they want added to the app (in a support page)


<br>

### 2. Screen Archetypes
- Login/Registration Screen
  - The user can register an account
  - The user can sign-in and sign-out to the app
  - The user can connect to EPIC, Steam, etc with their logins
  - The user can connect to their console's accounts (XBOX, Nintendo, Playstation, etc)
- Home (Stream)
  - The user can search for games from EPIC's, Steam's, etc libraries to add to their lists
  - The user can see which games are on sale
  - The user can use the navigation bar to go from one page to another
  - The user can see trending games
  - The user can see their wishlist or GameVault list
- Search (Creation & Stream)
  - The user can search for games from EPIC's, Steam's, etc libraries to add to their lists
  - The user can use the navigation bar to go from one page to another
  - The user can see which games are on sale
  - The user can see trending games
- Game Info (Detail)
  - The user can view descriptions and images of the game they are looking for
  - The user can see which games are on sale
  - The user can play the game trailers for the games they are looking for
  - The user can see trending games
- Trending (Stream)
  - The user can use the navigation bar to go from one page to another
  - The user can see trending games
- On Sale (Stream)
  - The user can use the navigation bar to go from one page to another
  - The user can see trending games
- GameVault (Creation & Stream)
  - The user can search for games from EPIC's, Steam's, etc libraries to add to their lists
  - The user can search for games within their own lists
  - The user can see which games are on sale
  - The user can use the navigation bar to go from one page to another
  - The user's data can save between sessions on the app
  - The user can see trending games
  - The user can see their wishlist or GameVault list
- Profile/Settings
  - The user can connect to EPIC, Steam, etc with their logins
  - The user will be able to look for friends they can connect with
  - The user can connect to their console's accounts (XBOX, Nintendo, Playstation, etc)
  - The user can sign-in and sign-out to the app
  - The user can request games/libraries that they want added to the app (in a support page)
  - The user's data can save between sessions on the app

    
<br>

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Home Feed (Search bar, Trending, Sales, GameVault)
* Search (filter by company (Steam, EPIC, Nintendo, XBOX, Playstation), by genre, by other categories)
* Trending (Popular Game Suggestions, Popular Console Suggestions, Top 10)
* GameVault (Saved Games, Saved Consoles, Wishlist)
* Profile (App settings, Account info)


<br>

**Flow Navigation** (Screen to Screen)
- Login/Registration Screen
  - => Home

- Home
  - => Settings
  - => Search
  - => Trending
  - => On Sale
  - => Game Vault

- Search
  - => Game Info

- Game Info
  - => Search

- Trending
  - => Search
  - => Game Info

- On Sale
  - => Search
  - => Game Info

- Wishlist
  - => Game Info

- Profile/Settings
  - => None


<br>

## Wireframes
<img src="https://github.com/Coding-Just-Takes-Dedication-CJTD/cs388-group-proj/blob/98f7bb298c219d92308e675f5483220f8f2d8b91/WireframePT1.jpg" width=600>

<img src="https://github.com/Coding-Just-Takes-Dedication-CJTD/cs388-group-proj/blob/29d5a9285c42498b93d1fc3b65ae228a3db76fab/WireframePT2.jpg" width=600>

<br>

### [BONUS] Digital Wireframes & Mockups
![image](https://hackmd.io/_uploads/rkQYRr_1Zl.png)
![image](https://hackmd.io/_uploads/By5jRH_1-l.png)
![image](https://hackmd.io/_uploads/H19n0Buybg.png)
![image](https://hackmd.io/_uploads/HJuhw8_kbe.png)


### [BONUS] Interactive Prototype

<br>
<br>

# Milestone 2 - Build Sprint 1 (Unit 8)

## GitHub Project board
<img src="https://github.com/Coding-Just-Takes-Dedication-CJTD/cs388-group-proj/blob/main/Github_Board.jpg" width=600>

## Issue cards

- [Screenshot of your Project Board with the issues that you've been working on for this unit's milestone] <img src="https://github.com/Coding-Just-Takes-Dedication-CJTD/cs388-group-proj/blob/main/github_issues.jpg" width=600>
- [Screenshot of your Project Board with the issues that you're working on in the **NEXT sprint** with assigned owners.] <img src="https://github.com/Coding-Just-Takes-Dedication-CJTD/cs388-group-proj/blob/main/github_issues.jpg" width=600>

## Issues worked on this sprint

- Issues completed this sprint:
   - Created Issues
   - Assigned Issues
   - Basic App Framework
   - Navigation Bar
     
- **GIF:** [https://i.imgur.com/euTC3VR.gif](https://imgur.com/a/cs388-group-project-sprint1-final-M4zYSyb)

<br>
<br>

# Milestone 3 - Build Sprint 2 (Unit 9)

## GitHub Project board

[Add screenshot of your Project Board with the updated status of issues for Milestone 3. Note that these should include the updated issues you worked on for this sprint and not be a duplicate of Milestone 2 Project board.] <img src="YOUR_WIREFRAME_IMAGE_URL" width=600>

## Completed user stories

- List the completed user stories from this unit
- List any pending user stories / any user stories you decided to cut
from the original requirements

[Add video/gif of your current application that shows build progress]
<img src="YOUR_WIREFRAME_IMAGE_URL" width=600>

## App Demo Video

- Embed the YouTube/Vimeo link of your Completed Demo Day prep video
