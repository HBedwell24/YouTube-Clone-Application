# YouTube Clone Application
Following the authentication of an authorized user through a YouTube.Builder client, the workflow of the application allows for an individual to view subscriptions unique to that account, as well as videos that are trending across the entire YouTube platform (with no specific category in mind). In addition, one can also browse comments and suggested videos based on relevance to the video ID of the media of choice, which promotes an exploratory experience similar to that of the official YouTube application.

![YouTube Data API Integration](images/youtube-api-thumbnail.jpg?raw=true "YouTube Data API Integration")

## Configuring the Project
To successfully run the following application, one will need posession of an API key from the Google Developer's Console. The steps to acquiring the said API key can be found as follows:
1. Navigate to https://console.developers.google.com/
2. In the toolbar, up by the hamburger menu and Google APIs icon, one will find a dropdown. Click this dropdown to open a dialog box, and then click 'New Project' in the upper right hand corner.
3. Enter in the Project name of your choice, and then click 'Create'.
4. After the aforementioned step has been completed, one will be brought to a dashboard. This dashboard will be mostly empty, for the reason that it will only populate once the user has enabled an API or service. To accomplish this feat, one will need to click 'Enable APIs and Services' (located beneath the toolbar), and then proceed to YouTube Data API v3. Click the card that this API resides on, and then press 'Enable'.
5. Click on 'Credentials' in the vertical navigation bar (located on the left hand side) in order to begin the credential creation process. 
6. Click the 'Create Credentials' button located beneath the toolbar, and then choose API key. The resulting key will be what will be placed within the Credential.kt file, located within the com.example.youtubeapiintegration folder.
7. (Optional) Click on the now created API key in order to restrict it to your liking. In my case, I forbade the use of the key with any other API other the YouTube Data API V3. 

## Technologies Used
* **Circle Image View by @hdodenhof** - a library built to support ImageViews with preset rounded edges
* **Custom Google Sign-In Button by @shobhitpuri** - custom SignInButton that features theme alterability, a unique android:text attribute, and Google guideline compliance
* **Google Play Services Libraries** - used for network troubleshooting and application adaptability in unforeseen events
* **Gson/Jackson** - used to serialize and deserialize Java objects to (and from) JSON
* **Joda-Time** - used as an alternative to Java's built in date and time library 
* **Kotlin** - a language that is interoperable with Java, while also providing quality of life improvements such as type inference and line of code reduction
* **OAuth2** - used to authenticate potential end-users of the application
* **Picasso** - a rendering library that can improve image loading time and processing
* **Retrofit** - a REST client for Java and Android, that makes it relatively easy to retrieve and upload JSON
* **YouTube Data API V3 Java Client** - supports core YouTube features and can be used to interact with them accordingly, given correct authorization
