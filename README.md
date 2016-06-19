# MovieList
Overview: Build a read-only movie listing app using the Movie Database API.

The Movie Database API documentation
Sample API Request: https://api.themoviedb.org/3/movie/now_playing?api_key=a07e22bc18f5cb106bfe4cc1f83ad8ed

User Stories:

User can view a list of movies (title, poster image, and overview) currently playing in theaters from the Movie Database API.

Views should be responsive for both landscape/portrait mode and fully optimized for performance with the ViewHolder pattern.

In portrait mode, the poster image, title, and movie overview is shown.

In landscape mode, the rotated layout should use the backdrop image instead and show the title and movie overview to the right of it.

Other items to addL

Add pull-to-refresh for popular stream with SwipeRefreshLayout
Display a nice default placeholder graphic for each image during loading (read more about Picasso) 
Improve the user interface through styling and coloring
For popular movies (i.e. a movie voted for more than 5 stars), the full backdrop image is displayed. Otherwise, a poster image, the movie title, and overview is listed. 
Expose details of movie (ratings using RatingBar, popularity, and synopsis) in a separate activity.
Allow video posts to be played in full-screen.
When clicking on a popular movie (i.e. a movie voted for more than 5 stars) the video should be played immediately.
Less popular videos rely on the detailed page should show an image preview that can initiate playing a YouTube video.
Add a play icon overlay to popular movies to indicate that the movie can be played 
Add a rounded corners for the images.
