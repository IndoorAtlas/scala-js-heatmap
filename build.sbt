 enablePlugins(ScalaJSPlugin)
 // enablePlugins(WorkbenchPlugin)

 resolvers += Resolver.typesafeRepo("releases")

 name := "heatmap"
 version := "0.1"
 scalaVersion := "2.12.8"

 libraryDependencies ++= Seq(
   "com.lihaoyi" %%% "upickle" % "0.7.5",
   "org.scala-js" %%% "scalajs-dom" % "0.9.5",
   "com.lihaoyi" %%% "scalatags" % "0.7.0"
 )






