# Youtube API Integration
  Following the authentication of an authorized user through a YouTube.Builder client, the workflow of the application allows for an individual to view subscriptions unique to that account, as well as videos that are trending across the entire YouTube platform (with no specific category in mind). In addition, one can also browse suggested videos based on relevance to the video ID of the media of choice, which can promote an exploratory experience similar to that of the official YouTube application.

## Personalizing the User Experience
  To personalize an individual's experience, a Dark theme SwitchPreference has been featured within the Settings activity to compensate for instances where one might open this application within low lit environments, which might invoke more eye strain. Clearing cookies on the local application was another property that I found essential to the application, for the reason that recent search queries that have been submitted within the application's search view are saved within a SQLLite database to be reused at a later point. Though this may not appear daunting to most people, it is still, nevertheless, common courtesy to include this feature, especially considering how trivial it is to wipe such a database's contents.

## Technologies Used
- Circle Image View by @hdodenhof
- Custom Google Sign-In Button by @shobhitpuri
- Google Play Services Libraries
- Gson
- Jackson
- Joda-Time
- Kotlin
- OAuth2
- Picasso
- Retrofit
- YouTube Data API V3 Java Client


