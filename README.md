<img src="app/src/main/res/drawable/oneapp_logo.png" width="200">

# OneApp
A Kotlin Compose Android Application

***In order for this project to run, add your own "GOOGLE_MAPS_API_KEY" in your local.properties file***

## API used in this application
1. Google Routes API
2. Google Autocomplete API
3. Google Places API

## Functionality
1. Search "optimal" route to destination in Singapore
2. View all Traffic Cameras in Singapore
3. Favourite location or traffic camera (if user is logged in)

## Pages

### Dashboard
Main page that allows the user the following:
1. Search destination
2. CRUD their favourite locations and traffic cameras
3. Login & Logout on top right corner

### Login & Register
You can manually create a user in UserState.kt. Firebase was not configured due to time constraint. However, I would like to attempt once I have the time

### Favourite
This page allows the user to add a location to their list of favourites (configured to max 3)

### Maps
Requires a PlaceID to be passed. Shows the following
1. Google Maps on the top half of the screen
2. Polyline from user's current location to destination (would like to create an option to change the origin)
3. Traffic Cameras, Red Light Cameras and Speed Cameras that are on route
4. The Traffic Cameras images are displayed in the lower half of the screen. These images can be moved by swipe and the provided arrow controls. Tapping on the Traffic Camera image zooms the Google Maps to that Traffic Camera's location (might make it more intuitive)

### Traffic Camera & Traffic Camera Details
View Traffic Camera in Singapore ordered manually by highway. Tapping the highway will navigate to the Traffic Camera Details page where user can favourite a Traffic Camera if they are logged in
