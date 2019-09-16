 enablePlugins(ScalaJSPlugin)

 name := "heatmap"
 version := "0.1"
 scalaVersion := "2.11.8"

 libraryDependencies ++= Seq(
   "com.lihaoyi" %%% "upickle" % "0.7.1",
   "org.scala-js" %%% "scalajs-dom" % "0.9.1",
   "com.lihaoyi" %%% "scalatags" % "0.6.1"
 )





