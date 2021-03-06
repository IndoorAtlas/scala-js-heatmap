# Heatmap visualization using IndoorAtlas Data API and Scala.js
![Example heatmap](images/example_heatmap.png)
 
This project implements a simple web service that can be used to visualize IndoorAtlas session data for
a given account, dates and floor levels.
It demonstrates how
 * IndoorAtlas [Data API](https://indooratlas.freshdesk.com/support/solutions/articles/36000086507-data-rest-api-overview)
  can be used to fetch the session data, based on Data API key and given dates
 * The data can be used with heatmap visualization libraries, such as 
 [MapBox GL JS](https://docs.mapbox.com/mapbox-gl-js/api/)
 
## Live demo [here](https://indooratlas.github.io/scala-js-heatmap/target/scala-2.12/classes/index.html)
* Visualise your own user data or use an example visualization 
[here](https://indooratlas.github.io/scala-js-heatmap/target/scala-2.12/classes/index.html "Live demo").
* In order to visualize your own user data, you will need an IndoorAtlas account with session data and a 
[Data API key](https://indooratlas.freshdesk.com/support/solutions/articles/36000050559-creating-applications-and-api-keys).
* When using your own Data API key and logged in to IndoorAtlas, paths can also be used as a link to view the session in the IndoorAtlas
session viewer:

 ![Example link](images/example_link.png)

## Commercial use
This repository is for demonstration only. Users wishing to use IndoorAtlas Data API for commercial purposes, 
contact [sales@indooratlas.com](mailto:sales@indooratlas.com).   

## Related content
* IndoorAtlas Data REST [API Overview](https://indooratlas.freshdesk.com/support/solutions/articles/36000086507-data-rest-api-overview)
(with examples), and [API Reference](https://docs.indooratlas.com/data-api)
* [Instructions](https://indooratlas.freshdesk.com/support/solutions/articles/36000050559-creating-applications-and-api-keys)
 for how to create Data API keys
* MapBox GL JS [API](https://docs.mapbox.com/mapbox-gl-js/api/) and 
[Heatmap tutorial](https://docs.mapbox.com/help/tutorials/make-a-heatmap-with-mapbox-gl-js/)
* Scala.js [home page](https://www.scala-js.org/) and [tutorial](http://www.lihaoyi.com/hands-on-scala-js/)

## Building and running locally
 * Install [sbt 1.3.0](https://www.scala-sbt.org/download.html)
 * Run sbt from command line in the project root
   * Should automatically resolve all dependencies
   * Build using `sbt fastOptJS` or `fastOptJS` (in sbt console)
     * For production, use `fullOptJS` and change `resources/index.html` dependency accordingly
   * Use `~fastOptJS` for automatic build with code changes (e.g. when using an IDE) and refresh the page when done
 * Open `target/scala_2.12/classes/index.html` in a browser
   * Fill the fields and submit
   * Use Example button for fixed demo query config
 
## Possible future improvements
* Make the project cross compile to JS and JVM
  * command line + save to local json file for JVM
  * src folders already reflect this