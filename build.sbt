import com.banno._

lazy val root = bannoRootProject("heatblast").aggregate(common, lib, scheduler, exampleSamza)

lazy val common = bannoProject("heatblast-common", "heatblast-common", file("./heatblast-common"))

lazy val lib = bannoProject("heatblast-lib", "heatblast-lib", file("./heatblast-lib")).dependsOn(common)

lazy val scheduler = bannoProject("heatblast-scheduler", "heatblast-scheduler", file("./heatblast-scheduler")).dependsOn(common)

lazy val exampleSamza = bannoProject("heatblast-example-samza", "heatblast-example-samza", file("./heatblast-example-samza")).dependsOn(lib)
